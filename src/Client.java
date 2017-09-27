import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
 
/*
 * The Client that can be run both as a console or a GUI
 */
public class Client  {
 
    // for I/O
    private ObjectInputStream sInput;       // to read from the socket
    private ObjectOutputStream sOutput;     // to write on the socket
    private SocketChannel socketChannel;
    private Socket socket;
 
    // if I use a GUI or not
    private ClientGUI cg;
     
    // the server, the port and the username
    private String server, username;
    private int port;
    JFrame frm;
    JButton[] btn=new JButton[50];
    int btnid=0;
    /*
     *  Constructor called by console mode
     *  server: the server address
     *  port: the port number
     *  username: the username
     */
    Client(String server, int port, String username) {
        // which calls the common constructor with the GUI set to null
        this(server, port, username, null);
    }
 
    /*
     * Constructor call when used from a GUI
     * in console mode the ClienGUI parameter is null
     */
    Client(String server, int port, String username, ClientGUI cg) {
        this.server = server;
        this.port = port;
        this.username = username;
        // save if we are in GUI mode or not
        this.cg = cg;
//    FlowLayout fl=new FlowLayout();
//    pnl.setLayout(fl);
    }
     
    /*
     * To start the dialog
     */
    public boolean start() {
        // try to connectto the server
        socketChannel = null;
        try {
            socketChannel = SocketChannel.open();
            SocketAddress socketAddress = new InetSocketAddress(server, port);
            socketChannel.connect(socketAddress);
            System.out.println("Connected..Now sending the file");

        } catch (IOException e) {
            cg.appendEvent("Error connectiong to server for file socket:" + e);
        }
        try {
            socket = new Socket(server, port+1);
        }
        // if it failed not much I can so
        catch(Exception ec) {
            cg.appendEvent("Error connectiong to server for message socket:" + ec);
            return false;
        }
         
        String msg = null ;
        try {
            msg = "Connection accepted " + socketChannel.getRemoteAddress(); //+ ":" + port;
        } catch (IOException ex) {
            msg="error getting server address " + ex;
        }
        cg.appendEvent(msg);
     
        /* Creating both Data Stream */
        try
        {
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            cg.appendEvent("Exception creating new Input/output Streams: " + eIO);
            return false;
        }
 
        // creates the Thread to listen from the server
        new Client.ListenFromServer().start();
        //new Client.ReceiveFromServer().start();
        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be AFile objects
        try
        {
            sOutput.writeObject(username);
        }
        catch (IOException eIO) {
            cg.appendEvent("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        // success we inform the caller that it worked
        return true;
    }
 
    /*
     * To send a message to the console or the GUI
     */
    private void display(String msg) {
        if(cg == null)
            System.out.println(msg);      // println in console mode
        else
            cg.appendRoom(msg + "\n");      // append to the ClientGUI JTextArea (or whatever)
    }
     
    /*
     * To send a message to the server
     */
    
    long getSize(AFile fl)
    {
        long sz=0;
        RandomAccessFile aFile = null;
        try {
            //File file = new File("E:\\movie\\The.Jungle.Book.2016.NEW.720p.HDTS.900MB.ShAaNiG.mkv");
            //sz=file.length();
            aFile = new RandomAccessFile(fl.getDir(), "r");
            
            FileChannel inChannel = aFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            long k=0;System.out.println(sz);
            while (inChannel.read(buffer) > 0) {
                buffer.flip();
                //socketChannel.write(buffer);
                buffer.clear();
                System.out.println(++k);
            }
            System.out.println("-->k");
            sz=k;
            Thread.sleep(1000);
            cg.appendEvent("End of file reached..\n");
            //socket.close();
            aFile.close();
        } catch (FileNotFoundException e) {
            cg.appendEvent("File not found exception"+e+"\n");
        } catch (IOException e) {
            cg.appendEvent("IO exception"+e+"\n");
        } catch (InterruptedException e) {
            cg.appendEvent("Interrupted exception"+e+"\n");
        }
        return sz;
    }
    
    public void readFile(AFile fl1) {
            System.out.println(fl1.getFileName());
            String dir="C:\\Users\\rimon\\Downloads\\"+fl1.getFileName();
            long sz=fl1.getFileSize();
            System.out.println(sz);
            System.out.println("dir="+dir);
            RandomAccessFile aFile = null;
            try {
                aFile =  new RandomAccessFile(dir, "rw");
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                FileChannel fileChannel = aFile.getChannel();
                long k=0;
                //cg.jpb2.setStringPainted(true); 
                cg.jpb2.setMinimum(0);
                cg.jpb2.setMaximum((int)sz);
                while (k<sz) {
                     cg.jpb2.setValue((int)k);  
                    socketChannel.read(buffer);
                    buffer.flip();
                    fileChannel.write(buffer);
                    buffer.clear();
                    k++;//System.out.println(k++);
                    if(k==sz) break;
                }
                cg.jpb2.setValue(0); 
                System.out.println("completed");
                Thread.sleep(1000);
                fileChannel.close();
                cg.appendEvent("Download completed");
                sendMessage(new AFile(AFile.DOWNLOAD,"completed",fl1.getDir(),fl1.getFileName(),fl1.getFileSize(),""));
                //socketChannel.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    
    long sendFile(AFile fl) {
        long sz=fl.getFileSize();
        RandomAccessFile aFile = null;
        try {
            File file = new File("E:\\movie\\The.Jungle.Book.2016.NEW.720p.HDTS.900MB.ShAaNiG.mkv");
            //sz=file.length();
            aFile = new RandomAccessFile(fl.getDir(), "r");
            
            FileChannel inChannel = aFile.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            long k=0;System.out.println(sz);
            //cg.jpb2.setStringPainted(true); 
                cg.jpb2.setMinimum(0);
                cg.jpb2.setMaximum((int)sz);
            while (inChannel.read(buffer) > 0) {
                cg.jpb2.setValue((int)k);  
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
                k++;//System.out.println(++k);
            }
            cg.jpb2.setValue(0);
            //System.out.println("-->k");
            sz=k;
            Thread.sleep(1000);
            cg.appendEvent("End of file reached..\n");
            //socket.close();
            aFile.close();
        } catch (FileNotFoundException e) {
            cg.appendEvent("File not found exception"+e+"\n");
        } catch (IOException e) {
            cg.appendEvent("IO exception"+e+"\n");
        } catch (InterruptedException e) {
            cg.appendEvent("Interrupted exception"+e+"\n");
        }
        return sz;
    }
    void sendMessage(AFile fl) {
        try {
            sOutput.writeObject(fl);
        }
        catch(IOException e) {
            cg.appendEvent("Exception writing to server: " + e);
        }
    }
 
    /*
     * When something goes wrong
     * Close the Input/Output streams and disconnect not much to do in the catch clause
     */
    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {} // not much else I can do
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {} // not much else I can do
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {} // not much else I can do
        try{
            if(socketChannel != null) socketChannel.close();
        }
        catch(Exception e) {} // not much else I can do
        // inform the GUI
        if(cg != null)
            cg.connectionFailed();
             
    }
    /*
     * To start the Client in console mode use one of the following command
     * > java Client
     * > java Client username
     * > java Client username portNumber
     * > java Client username portNumber serverAddress
     * at the console prompt
     * If the portNumber is not specified 1500 is used
     * If the serverAddress is not specified "localHost" is used
     * If the username is not specified "Anonymous" is used
     * > java Client
     * is equivalent to
     * > java Client Anonymous 1500 localhost
     * are eqquivalent
     *
     * In console mode, if an error occurs the program simply stops
     * when a GUI id used, the GUI is informed of the disconnection
     */
    
    class ListenFromServer extends Thread {
 
        public void run() {
            while(true) {
                try {
                    AFile fl = (AFile) sInput.readObject();
                    int tp=fl.getType();
                    long flsz=fl.getFileSize();
                    String msg = fl.getMessage(),
                            dir=fl.getDir(),
                            flname=fl.getFileName();
                            
                    if(cg == null) {
                        System.out.println(msg);
                        System.out.print("> ");
                    }
                    else {
                        if(tp==AFile.MESSAGE)
                            cg.appendRoom(msg);
                        else if(tp==AFile.WHOISIN)
                        {
                            cg.whoIsIn.removeAll();
                            String[] users=msg.split("=");
                            for(String user :users)
                            {
                                System.out.println(user);
                                JMenuItem it=new JMenuItem(user);
                                cg.whoIsIn.add(it);
                                it.addActionListener(new ActionListener() {
                                    public void actionPerformed(ActionEvent e) {
                                        cg.sendto=e.getActionCommand();
                                        cg.changeLabel(cg.chatlbl,"CHATTING WIHT "+cg.sendto);
                                        cg.tf.setEnabled(true);
                                        cg.tf.setText("");
                                    }
                                });
                            }
                        }
                        else if(tp==AFile.UPLOAD)
                        {
                            cg.appendEvent("Upload completed\n");
                        }
                        else if(tp==AFile.DOWNLOAD)
                        {
                            readFile(fl);
                        }
                        else if(tp==AFile.SEARCH)
                        {
                            if(cg.lstModel.size()>0)
                                cg.lstModel.removeAllElements();//clear();
                            String[] tks=msg.split("=");
                            for(String tk :tks)
                            {
                                System.out.println(tk);
                                cg.lstModel.addElement(tk);
                            }
                        }
                    }
                }
                catch(IOException e) {
                    cg.appendEvent("Server has close the connection: " + e);
                    if(cg != null)
                        //cg.connectionFailed();
                    break;
                }
                catch(ClassNotFoundException e2) {}
            }
        }
    }
}
