import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.nio.*;//ByteBuffer;
import java.nio.channels.*;//SocketChannel;
import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class ClientGUI extends JFrame implements ActionListener, MenuListener {
 
    private static final long serialVersionUID = 1L;
    // will first hold "Username:", later on "Enter message"
    protected JLabel chatlbl,label,lbl1,lbl2,lbl3,lbl4,lbl5,lbl6,lbl7,lbl8;
    // to hold the Username and later on the messages
    JTextField tf,tfSearch;
    // to hold the server address an the port number
    private JTextField tfServer, tfPort,filename,tfName;
    // to Logout and get the list of the users
    private JButton login, logout,choose,upload,download,search,searchAll,send;//, whoIsIn;
    protected JMenu whoIsIn;
    protected DefaultListModel lstModel;
    protected JList list;
    protected JProgressBar jpb1,jpb2;
    // for the chat room
    private JTextArea chat,event;
    private JScrollPane scrollPane1,scrollPane2,scrollPane3,scrollPane4;
    // if it is for connection
    private boolean connected;
    private Client client;
    protected String sendto;
    // the Client object
    //private Client client;
    // the default port number
    File srcfile;
    private int defaultPort;
    private String defaultHost;
    
    // Constructor connection receiving a socket number
    ClientGUI(String host, int port) {
 
        super("Chat Client");
        defaultPort = port;
        defaultHost = host;
        setLayout(null);
        
        chatlbl=new JLabel("");chatlbl.setFont(Font.decode("Arial-14"));
        getContentPane().add(chatlbl);
        chatlbl.setBounds(780, 290,200, 30);
        
        whoIsIn = new JMenu("Who is in");whoIsIn.setFont(Font.decode("Arial-14"));
        JMenuBar mb=new JMenuBar();
        //setJMenuBar(mb);
        mb.add(whoIsIn);
        whoIsIn.addMenuListener(this);
        getContentPane().add(mb);
        mb.setBounds(890, 10,80, 30);
        whoIsIn.setEnabled(false);
        
        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);
        label=new JLabel("Server Address:");label.setFont(Font.decode("Arial-14"));
        getContentPane().add(label);
        label.setBounds(10, 20,120, 10);
        
        getContentPane().add(tfServer);tfServer.setFont(Font.decode("Arial-14"));
        tfServer.setBounds(120, 10,80, 30);
        
        lbl1=new JLabel("Port No.:");lbl1.setFont(Font.decode("Arial-14"));
        getContentPane().add(lbl1);
        lbl1.setBounds(240, 20,95, 10);
        
        getContentPane().add(tfPort);tfPort.setFont(Font.decode("Arial-14"));
        tfPort.setBounds(300, 10,80, 30);
        
        lbl2=new JLabel("Username:");lbl2.setFont(Font.decode("Arial-14"));
        getContentPane().add(lbl2);
        lbl2.setBounds(420, 20,95, 10);
        
        tfName=new JTextField();tfName.setFont(Font.decode("Arial-14"));
        getContentPane().add(tfName);
        tfName.setBounds(490, 10,100, 30);
        
        login =new JButton("Login");login.setFont(Font.decode("Arial-14"));
        getContentPane().add(login);
        login.setBounds(630, 10,80, 30);
        login.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                loginActionPerformed(evt);
	            }
        });
        
        logout =new JButton("Logout");logout.setFont(Font.decode("Arial-14"));
        logout.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                logoutActionPerformed(evt);
	            }
        });
        getContentPane().add(logout);
        logout.setBounds(765, 10,80, 30);
        logout.setEnabled(false);
        
        lbl3 =new JLabel("Events Log");lbl3.setFont(Font.decode("Arial-14"));
        getContentPane().add(lbl3);
        lbl3.setBounds(550, 50,100, 30);
        event = new JTextArea();
        event.setColumns(80);
	event.setFont(new Font("Arial", 0, 14)); // NOI18N
        event.setRows(5);
	scrollPane1=new JScrollPane();
        scrollPane1.setViewportView(event);
	getContentPane().add(scrollPane1);
	scrollPane1.setBounds(550, 80, 420, 210);
        event.setEnabled(false);
        
        lbl7 =new JLabel("Chat area");lbl7.setFont(Font.decode("Arial-14"));
        getContentPane().add(lbl7);
        lbl7.setBounds(550, 290,100, 30);
        chat = new JTextArea();
        chat.setColumns(80);
	chat.setFont(new Font("Arial", 0, 14)); // NOI18N
        chat.setRows(5);
	scrollPane3=new JScrollPane();
        scrollPane3.setViewportView(chat);
	getContentPane().add(scrollPane3);
	scrollPane3.setBounds(550,320, 420, 210);
        chat.setEnabled(false);
        
        lbl8 =new JLabel("Type your message below");lbl8.setFont(Font.decode("Arial-14"));
        getContentPane().add(lbl8);
        lbl8.setBounds(650, 530,200, 30);
        tf = new JTextField();
        //tf.setColumns(80);
	tf.setFont(Font.decode("Arial-14")); // NOI18N
        //tf.setRows(5);
	scrollPane4=new JScrollPane();
        scrollPane4.setViewportView(tf);
        tf.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                tfActionPerformed(evt);
	            }
	        });
	getContentPane().add(scrollPane4);
	scrollPane4.setBounds(550,560, 320, 50);
        tf.setEnabled(false);
        send=new JButton();
        send.setFont(new Font("Tahoma", 0, 14)); // NOI18N
	        send.setText("Send");
	        send.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                sendActionPerformed(evt);
	            }
	        });
	        getContentPane().add(send);
	        send.setBounds(890, 570, 80, 30);
        send.setEnabled(false);
        
        lbl4 =new JLabel("For uploading");lbl4.setFont(Font.decode("Arial-14"));
        getContentPane().add(lbl4);
        lbl4.setBounds(100, 50,100, 30);
                filename=new JTextField();filename.setFont(Font.decode("Arial-14"));
	        getContentPane().add(filename);
	        filename.setBounds(10, 80, 180, 30);
                filename.setEnabled(false);
        choose=new JButton();
        choose.setFont(new Font("Tahoma", 0, 18)); // NOI18N
	        choose.setText("+");
	        choose.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                chooseActionPerformed(evt);
	            }
	        });
	        getContentPane().add(choose);
	        choose.setBounds(195, 80, 50, 30);
                choose.setEnabled(false);
                upload=new JButton();
	        upload.setFont(new Font("Tahoma", 0, 14)); // NOI18N
	        upload.setText("Upload");
	        upload.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                uploadActionPerformed(evt);
	            }
	        });
	        getContentPane().add(upload);
	        upload.setBounds(250, 80, 80, 30);
                upload.setEnabled(false);
        
        lbl5 =new JLabel("For downloading");lbl5.setFont(Font.decode("Arial-14"));
        getContentPane().add(lbl5);
        lbl5.setBounds(350, 50,120, 30);
        download=new JButton();
                //download.setBackground(new Color(51, 51, 255));
	        download.setFont(new Font("Tahoma", 0, 14)); // NOI18N
	        //download.setForeground(new Color(255, 255, 255));
	        download.setText("download");
	        //download.setBorder(null);
	        //download.setBorderPainted(false);
	        //download.setRequestFocusEnabled(false);
	        download.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                downloadActionPerformed(evt);
	            }
	        });
	        getContentPane().add(download);
	        download.setBounds(350, 80, 100, 30);
                download.setEnabled(false);
                jpb2=new JProgressBar();
                jpb2.setValue(0);  
                jpb2.setStringPainted(true);  
                //jpb2.setVisible(false);
                getContentPane().add(jpb2);
                jpb2.setBounds(140, 130,200, 20);
                jpb2.setEnabled(false);
                
        lbl6 =new JLabel("Searching file");lbl6.setFont(Font.decode("Arial-14"));
        getContentPane().add(lbl6);
        lbl6.setBounds(100, 170,120, 30);
        tfSearch=new JTextField();
        getContentPane().add(tfSearch);
        tfSearch.setBounds(10, 220,230, 30);
        tfSearch.setEnabled(false);
        search=new JButton();
	        search.setFont(new Font("Tahoma", 0, 14));
	        search.setText("Search");
                search.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                searchActionPerformed(evt);
	            }
	        });
	        getContentPane().add(search);
	        search.setBounds(250, 220, 90, 30);
                search.setEnabled(false);
         searchAll=new JButton();
	        searchAll.setFont(new Font("Tahoma", 0, 14));
	        searchAll.setText("Search all");
                searchAll.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent evt) {
	                searchallActionPerformed(evt);
	            }
	        });
	        getContentPane().add(searchAll);
	        searchAll.setBounds(350, 220, 100, 30);
                searchAll.setEnabled(false);
        lstModel=new DefaultListModel();
        list = new JList(lstModel);
	        list.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
                scrollPane2=new JScrollPane();
	        scrollPane2.setViewportView(list);
	        getContentPane().add(scrollPane2);
	        scrollPane2.setBounds(10, 260, 440, 350);
                list.setEnabled(false);
 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000,675);
        setVisible(true);
        //tf.requestFocus();
 
    }
    
    void appendRoom(String str) {
        chat.append(str+"\n\n");
        chat.setCaretPosition(chat.getText().length() - 1);
    }
    void appendEvent(String str) {
        if(str.equalsIgnoreCase("Events log.\n")) event.append(str);
        else event.append("\n-->"+str);
        //event.setCaretPosition(chat.getText().length() - 1);
    }
    
    void toggleObj()
    {
        if(tfServer.isEnabled()) tfServer.setEnabled(false);
        else tfServer.setEnabled(true);
        
        if(tfPort.isEnabled()) tfPort.setEnabled(false);
        else tfPort.setEnabled(true);
        
        if(tfName.isEnabled()) {tfName.setEnabled(false);tfName.setText("");}
        else tfName.setEnabled(true);
        
        if(login.isEnabled()) login.setEnabled(false);
        else login.setEnabled(true);
        
        if(logout.isEnabled()) logout.setEnabled(false);
        else logout.setEnabled(true);
        
        if(whoIsIn.isEnabled()) whoIsIn.setEnabled(false);
        else whoIsIn.setEnabled(true);
        
        if(filename.isEnabled()) {filename.setEnabled(false);filename.setText("");}
        else filename.setEnabled(true);
        
        if(choose.isEnabled()) choose.setEnabled(false);
        else choose.setEnabled(true);
        
        if(upload.isEnabled()) upload.setEnabled(false);
        else upload.setEnabled(true);
        
        if(download.isEnabled()) download.setEnabled(false);
        else download.setEnabled(true);
        
        if(jpb2.isEnabled()) jpb2.setEnabled(false);
        else jpb2.setEnabled(true);
        
        if(tfSearch.isEnabled()) {tfSearch.setEnabled(false);tfSearch.setText("");}
        else tfSearch.setEnabled(true);
        
        if(search.isEnabled()) search.setEnabled(false);
        else search.setEnabled(true);
        
        if(searchAll.isEnabled()) searchAll.setEnabled(false);
        else searchAll.setEnabled(true);
        
        if(list.isEnabled()) {list.setEnabled(false);lstModel.removeAllElements();}
        else list.setEnabled(true);
        
        if(event.isEnabled()) {event.setEnabled(false);event.setText("");}
        else event.setEnabled(true);
        
        if(chat.isEnabled()) {chat.setEnabled(false);chat.setText("");}
        else chat.setEnabled(true);
        
        if(tf.isEnabled()) {tf.setEnabled(false);tf.setText("");}
        else tf.setEnabled(true);
        
        if(send.isEnabled()) send.setEnabled(false);
        else send.setEnabled(true);
    }
    
    void connectionFailed() {
        login.setEnabled(true);
        logout.setEnabled(false);
        whoIsIn.setEnabled(false);
        changeLabel(label,"Enter your username below");
        tf.setEnabled(true);
        tf.setText("Anonymous");
        // reset port number and host name as a construction time
        tfPort.setEnabled(true);
        tfPort.setText("" + defaultPort);
        tfServer.setEnabled(true);
        tfServer.setText(defaultHost);
        // let the user change them
        tfServer.setEditable(true);
        tfPort.setEditable(true);
        // don't react to a <CR> after the username
        tf.removeActionListener(this);
        connected = false;
    }
    
    void changeLabel(JLabel lbl,String str)
    {
        lbl.setText(str);
    }
    /*
    * Button or JTextField clicked
    */
//    public void actionPerformed(ActionEvent e) {
//        Object o = e.getSource();
//        // if it is the Logout button
//        if(o == logout) {
//            client.sendMessage(new AFile(AFile.LOGOUT, "","","",0));
//            return;
//        }
//        
//        //if(o==upload)
//        {
//            
//        }
//        if(connected) {
//            // just have to send the message
//            //appendRoom("me > "+tf.getText()+"\n");
//            client.sendMessage(new AFile(AFile.MESSAGE, tf.getText(),client.sendto));
//            tf.setText("");
//            return;
//        }
        
//        if(o == login) {
//            // ok it is a connection request
//            String username = tf.getText().trim();
//            // empty username ignore it
//            if(username.length() == 0)
//                return;
//            // empty serverAddress ignore it
//            String server = tfServer.getText().trim();
//            if(server.length() == 0)
//                return;
//            // empty or invalid port numer, ignore it
//            String portNumber = tfPort.getText().trim();
//            if(portNumber.length() == 0)
//                return;
//            int port = 0;
//            try {
//                port = Integer.parseInt(portNumber);
//            }
//            catch(Exception en) {
//                return;   // nothing I can do if port number is not valid
//            }
// 
//            // try creating a new Client with GUI
//            client = new Client(server, port, username, this);
//            // test if we can start the Client
//            if(!client.start())
//                return;
//            setTitle(username);
//            tf.setEnabled(false);
////            tf.setText("");
//            //changeLabel(label,"Enter your message bellow");
//            connected = true;
//            
//            // disable login button
//            login.setEnabled(false);
//            // enable the 2 buttons
//            logout.setEnabled(true);
//            whoIsIn.setEnabled(true);
//            // disable the Server and Port JTextField
//            tfServer.setEditable(false);
//            tfPort.setEditable(false);
//            // Action listener for when the user enter a message
//            //tf.addActionListener(this);
//        }
 
//    }
 
    // to start the whole thing the server
    public static void main(String[] args) {
        new ClientGUI("localhost", 1500);
    }
    
    private void loginActionPerformed(ActionEvent evt) {//GEN-FIRST:event_uploadActionPerformed
        // ok it is a connection request
            String username = tfName.getText().trim();
            // empty username ignore it
            if(username.length() == 0)
                return;
            // empty serverAddress ignore it
            String server = tfServer.getText().trim();
            if(server.length() == 0)
                return;
            // empty or invalid port numer, ignore it
            String portNumber = tfPort.getText().trim();
            if(portNumber.length() == 0)
                return;
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            }
            catch(Exception en) {
                return;   // nothing I can do if port number is not valid
            }
 
            // try creating a new Client with GUI
            client = new Client(server, port, username, this);
            // test if we can start the Client
            if(!client.start())
                return;
            sendto="";
            setTitle(username);
            toggleObj();
            return;
    }
    
    private void logoutActionPerformed(ActionEvent evt) {//GEN-FIRST:event_uploadActionPerformed
        client.sendMessage(new AFile(AFile.LOGOUT, "","","",0,""));
        tfServer.setText("localhost");
        tfPort.setText("1500");
        tfName.setText("");
        changeLabel(chatlbl, "");
        sendto="";
        setTitle("Chat Client");
        toggleObj();
        return;
    }
    
    private void chooseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadActionPerformed
	        JFileChooser fileChooser = new JFileChooser();
	        fileChooser.showDialog(this, "Select File");
	        srcfile = fileChooser.getSelectedFile();
	 
	        if (srcfile != null) {
	            if (!srcfile.getName().isEmpty()) {
	                String str;
	                    str = srcfile.getPath();
	                filename.setText(str);
	            }
	        }
	    }//GEN-LAST:event_uploadActionPerformed
    
    private void uploadActionPerformed(ActionEvent evt) {
        long sz=0;    
        String dir=filename.getText(),flname;
            if(dir.length()>0)
            {
                flname=dir.substring(dir.lastIndexOf("\\") + 1, dir.length());
               // System.out.println(flname);
                sz=client.getSize(new AFile(AFile.UPLOAD,"upload",dir,flname,0,""));
                client.sendMessage(new AFile(AFile.UPLOAD,"upload",dir,flname,sz,""));
                client.sendFile(new AFile(AFile.UPLOAD,"upload",dir,flname,sz,""));
                //System.out.println("file buffered");
            }
            else event.append("First choose a file");
    }
    
    private void downloadActionPerformed(ActionEvent evt) {
        if(list.isSelectionEmpty())
        {
            event.append("First select a file");
            return;
        }
        String flname=(String) list.getSelectedValue();
        client.sendMessage(new AFile(AFile.DOWNLOAD,"download","",flname,0,""));
    }
    
    private void searchActionPerformed(ActionEvent evt) {
        String flname=tfSearch.getText();
        if(flname.length()>0)
            client.sendMessage(new AFile(AFile.SEARCH,"search","",flname,0,""));
        else event.append("First write a file name");
    }
    
    private void searchallActionPerformed(ActionEvent evt) {
        client.sendMessage(new AFile(AFile.SEARCH,"all","","",0,""));
    }
    private void sendActionPerformed(ActionEvent evt) {
//        if(sendto.equals(""))
//        {
//            event.append("First select an user");
//            return;
//        }
        String msg=tf.getText();
        if(!msg.equals(""))
        client.sendMessage(new AFile(AFile.MESSAGE,msg,"","",0,sendto));
        tf.setText("");
    }
    
    private void tfActionPerformed(ActionEvent evt) {
//        if(sendto.equals(""))
//        {
//            event.append("First select an user");
//            return;
//        }
        String msg=tf.getText();
        if(!msg.equals(""))
        client.sendMessage(new AFile(AFile.MESSAGE,msg,"","",0,sendto));
        tf.setText("");
    }
    
    @Override
    public void menuSelected(MenuEvent me) {
            client.sendMessage(new AFile(AFile.WHOISIN, "","","",0,""));
            return;
    }

    @Override
    public void menuDeselected(MenuEvent me) {
        
    }

    @Override
    public void menuCanceled(MenuEvent me) {
        
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 
}
