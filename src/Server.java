import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.*;
 
/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
    // a unique ID for each connection
    private static int uniqueId;
    // an ArrayList to keep the list of the Client
    private ArrayList<Server.ClientThread> al;
    private ArrayList<String> flList;
    // if I am in a GUI
    private ServerGUI sg;
    // to display time
    private SimpleDateFormat sdf;
    // the port number to listen for connection
    private int port;
    // the boolean that will be turned of to stop the server
    private boolean keepGoing;
     
 
    /*
     *  server constructor that receive the port to listen to for connection as parameter
     *  in console
     */
    public Server(int port) {
        this(port, null);
    }
     
    public Server(int port, ServerGUI sg) {
        // GUI or not
        this.sg = sg;
        this.port = port;
        sdf = new SimpleDateFormat("HH:mm:ss");
        al = new ArrayList<Server.ClientThread>();
        flList = new ArrayList<String>();
    }
     
    public void start() {
        keepGoing = true;
            ServerSocketChannel serverSocketChannel = null;
            ServerSocket serverSocket = null;
        try
        {
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.socket().bind(new InetSocketAddress(port));
                serverSocket = new ServerSocket(port+1);
            while(keepGoing)
            {
                display("Server waiting for Clients on port " + port + ".");
                 
                SocketChannel socketChannel = null;
                socketChannel = serverSocketChannel.accept();
                Socket socket = serverSocket.accept();      // accept connection
                display("Connection established...." + socketChannel.getRemoteAddress());
                if(!keepGoing)
                    break;
                Server.ClientThread t = new Server.ClientThread(socketChannel,socket);  // make a thread of it
                al.add(t);                                  // save it in the ArrayList
                t.start();
            }
            // I was asked to stop
            try {
                serverSocketChannel.close();
                serverSocket.close();
                for(int i = 0; i < al.size(); ++i) {
                    Server.ClientThread tc = al.get(i);
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }
                    catch(IOException ioE) {
                        // not much I can do
                    }
                }
            }
            catch(Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        }
        // something went bad
        catch (IOException e) {
            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }      
    /*
     * For the GUI to stop the server
     */
    protected void stop() {
        keepGoing = false;
        // connect to myself as Client to exit statement
        try {
            new Socket("localhost", port);
        }
        catch(Exception e) {
            // nothing I can really do
        }
    }
    /*
     * Display an event (not a message) to the console or the GUI
     */
    private void display(String msg) {
        String time = sdf.format(new Date()) + " " + msg;
        if(sg == null)
            System.out.println(time);
        else
            sg.appendEvent(time + "\n");
    }
    /*
     *  to broadcast a message to all Clients
     */
    private synchronized void broadcast(String username,AFile cm1) {
        //String time = sdf.format(new Date());
        String message="me > "+cm1.getMessage(), sendto=cm1.getTowhome(),messageLf="";
        if(sendto.equals("")){
            messageLf="Server > "+cm1.getMessage();
        }
        else
        messageLf = username+" > " + cm1.getMessage();
        int i;
        for(i = al.size(); --i >= 0;) {
            if(al.get(i).username.equalsIgnoreCase(sendto))
            {
                messageLf=convert1(messageLf);
               if(!al.get(i).writeMsg(new AFile(AFile.MESSAGE,messageLf,"","",0,"")))
               {
                    al.remove(i);
                    display("Disconnected Client " + al.get(i).username + " removed from list.");
               }
               break;
            }
        }
        boolean ch=true;
        if(i<0) ch=false;
        for(i = al.size(); --i >= 0;) {
            if(al.get(i).username.equalsIgnoreCase(username))
            {
                message=convert2(message);
                al.get(i).writeMsg(new AFile(1,message,"","",0,""));
                messageLf=convert1(messageLf);
                if(sendto.equals("")) al.get(i).writeMsg(new AFile(AFile.MESSAGE,messageLf,"","",0,""));
                else if(!ch)
                {
                    message=username+" > I am offline\n";
                    message=convert1(message);
                    al.get(i).writeMsg(new AFile(1,message,"","",0,""));
                }
               break;
            }
        }
        return;
    }
    
    String convert1(String st)
    {
        //String[] tks=st.split(" ");
        StringTokenizer tkn=new StringTokenizer(st);
        String str="",strt="",tmp="",tmp1="";
        while(tkn.hasMoreTokens())
        {
            tmp=tkn.nextToken();
            tmp1=strt+" "+tmp;
            if(tmp1.length()>25) {str=str+"\n"+tmp;strt=tmp;}
            else {str=str+" "+tmp;strt=strt+" "+tmp;}
        }
        return str;
    }
    
    String convert2(String st)
    {
        //String[] tks=st.split(" ");
        StringTokenizer tkn=new StringTokenizer(st);
        String str="",fixed="                                                          ";
        String strt="";
        str=fixed;
        while(tkn.hasMoreTokens())
        {
            String tmp=tkn.nextToken();
            String tmp1=strt+" "+tmp;
            if(tmp1.length()>25) {str=str+"\n"+fixed+tmp;strt=tmp;}
            else {str=str+" "+tmp;strt=strt+" "+tmp;}
        }
        return str;
    }
 
    // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for(int i = 0; i < al.size(); ++i) {
            Server.ClientThread ct = al.get(i);
            // found it
            if(ct.id == id) {
                al.remove(i);
                return;
            }
        }
    }
    
    public static void main(String[] args) {
        int portNumber = 1500;
        switch(args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;
                 
        }
        Server server = new Server(portNumber);
        server.start();
    }
 
    /** One instance of this thread will run for each client */
    class ClientThread extends Thread {
        // the socket where to listen/talk
        SocketChannel socketChannel;
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        AFile fl;
        String date;
        
        ClientThread(SocketChannel socketChannel,Socket socket) {
            id = ++uniqueId;
            this.socketChannel = socketChannel;
            this.socket = socket;
            try
            {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
                display(username + " just connected.");
            }
            catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            // have to catch ClassNotFoundException
            // but I read a String, I am sure it will work
            catch (ClassNotFoundException e) {
            }
            date = new Date().toString() + "\n";
        }
 
        // what will run forever
        public void run() {
            boolean ok = true;
            while(ok) {
                try {
                    fl = (AFile) sInput.readObject();
                }
                catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;             
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                
                String message = fl.getMessage(),
                        dir=fl.getDir(),
                        flname=fl.getFileName(),
                        towhome=fl.getTowhome();
                long flsz=0;
                // Switch on the type of message receive
                    int cnt=0;
                    String lst="";
                switch(fl.getType()) {
 
                case AFile.MESSAGE:
                    //if(towhome.equals("")) writeMsg(fl);
                    broadcast(username,new AFile(1, message,"","",0,towhome));
                    break;
                case AFile.LOGOUT:
                    display(username + " disconnected with a LOGOUT message.");
                    ok = false;
                    break;
                case AFile.WHOISIN:
                    //writeMsg("List of the users connected at " + sdf.format(new Date()) + "\n");
                    // scan al the users connected
                    lst="";
                    cnt=0;
                    for(int i = 0; i < al.size(); ++i) {
                        Server.ClientThread ct = al.get(i);
                        String nm=ct.username;
                        if(!nm.equalsIgnoreCase(username))
                        {
                            if(cnt==0) lst=lst+nm;
                            else lst=lst+"="+nm;
                            cnt++;
                        }
                    }
                    writeMsg(new AFile(AFile.WHOISIN,lst,"","",0,""));
                    break;
                case AFile.UPLOAD:
                    System.out.println("message got");
                    readFile(fl);
                    break;
                case AFile.DOWNLOAD:
                    if(message.equalsIgnoreCase("completed"))
                    {
                        sg.appendEvent("sending completed");
                    }
                    else{
                        flsz=sendFile(false, fl);
                        writeMsg(new AFile(AFile.DOWNLOAD,"download","",flname,flsz,""));
                        sendFile(true, fl);
                    }
                    break;
                case AFile.SEARCH:
                    lst="";
                    cnt=0;
                    File[] files = new File("G:\\prjct_L0302\\fileServer").listFiles();
                    //If this pathname does not denote a directory, then listFiles() returns null. 

                    if(message.equalsIgnoreCase("search"))
                    {
                        for (File file : files) {
                            if (file.isFile()) {
                                String tmp=file.getName();
                                if(tmp.equalsIgnoreCase(flname))
                                {
                                    if(cnt==0) lst=lst+tmp;
                                    else lst=lst+"="+tmp;
                                    cnt++;
                                }
                            }
                        }
                    }
                    else
                    {
                        for (File file : files) {
                            if (file.isFile()) {
                                if(cnt==0) lst=lst+file.getName();
                                    else lst=lst+"="+file.getName();
                                    cnt++;
                            }
                        }
                    }
                    writeMsg(new AFile(AFile.SEARCH,lst,"","",0,""));
                    break;
                }
            }
            // remove myself from the arrayList containing the list of the
            // connected Clients
            remove(id);
            close();
        }
         
        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}
            try {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }
 
        /*
         * Write a String to the Client output stream
         */
        private boolean writeMsg(AFile fl1) {
            // if Client is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(fl1);
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
        
        public void readFile(AFile fl1) {
            String flname=fl1.getFileName();
            System.out.println(flname);
            String dir="G:\\prjct_L0302\\fileServer\\"+flname;
            long sz=fl1.getFileSize();
            System.out.println(sz);
            System.out.println("dir="+dir);
            RandomAccessFile aFile = null;
            try {
                aFile =  new RandomAccessFile(dir, "rw");
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                FileChannel fileChannel = aFile.getChannel();
                long k=0;
                while (k<sz) {
                    socketChannel.read(buffer);
                    buffer.flip();
                    fileChannel.write(buffer);
                    buffer.clear();
                    k++;//System.out.println(k++);
                    if(k==sz) break;
                }
                System.out.println("completed");
                Thread.sleep(1000);
                fileChannel.close();
                sg.appendEvent("End of file reached..Closing channel");
                //flList.add(flname);
                writeMsg(fl1);
                //socketChannel.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        long sendFile(boolean ch,AFile fl) {
            long sz=0;
            RandomAccessFile aFile = null;
            try {
                //File file = new File("E:\\movie\\The.Jungle.Book.2016.NEW.720p.HDTS.900MB.ShAaNiG.mkv");
                //sz=file.length();
                String dir="G:\\prjct_L0302\\fileServer\\"+fl.getFileName();
                aFile = new RandomAccessFile(dir, "r");

                FileChannel inChannel = aFile.getChannel();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                long k=0;System.out.println(sz);
                while (inChannel.read(buffer) > 0) {
                    buffer.flip();
                    if(ch) socketChannel.write(buffer);
                    buffer.clear();
                    System.out.println(++k);
                }
                System.out.println("-->k");
                sz=k;
                Thread.sleep(1000);
                sg.appendEvent("End of file sending..\n");
                //socket.close();
                aFile.close();
            } catch (FileNotFoundException e) {
                sg.appendEvent("File not found exception"+e+"\n");
            } catch (IOException e) {
                sg.appendEvent("IO exception"+e+"\n");
            } catch (InterruptedException e) {
                sg.appendEvent("Interrupted exception"+e+"\n");
            }
            return sz;
        }
    }
}
