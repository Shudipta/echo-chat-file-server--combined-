import java.io.*;
/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server.
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no
 * need to count bytes or to wait for a line feed at the end of the frame
 */
public class AFile implements Serializable {
 
    protected static final long serialVersionUID = 1112122200L;
 
    static final int WHOISIN = 6, MESSAGE = 1, LOGOUT = 2,UPLOAD=3,DOWNLOAD=4,SEARCH=5;
    private int type;
    private long flsz;
    private String message,dir,flname,towhome;
     
    // constructor
    AFile(int type, String message, String dir, String flname,long flsz,String towhome) {
        this.type = type;
        this.message = message;
        this.dir=dir;
        this.flname=flname;
        this.flsz=flsz;
        this.towhome=towhome;
    }
     
    // getters
    int getType() {
        return type;
    }
    String getMessage() {
        return message;
    }
    String getDir() {
        return dir;
    }
    String getFileName() {
        return flname;
    }
    long getFileSize() {
        return flsz;
    }
    String getTowhome() {
        return towhome;
    }
}
