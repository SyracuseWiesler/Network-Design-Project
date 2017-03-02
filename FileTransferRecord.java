package corp;

public class FileTransferRecord {
    int sign;//uploading: 0  or  downloading: 1
    long timeMillis;//time stamp: System.currentTimeMillis
    String hostname;//host name of the host that is downloading
    String hostip;//host ip of the host that is downloading
    String type;
    public FileTransferRecord(int sign, long timeMillis, String hostname, String hostip) {
        this.sign = sign;
        this.timeMillis = timeMillis;
        this.hostname = hostname;
        this.hostip = hostip;
    }
    @Override
    public String toString () {
        if (sign == 0) {
            type = "download";
        } else {
            type = "upload";
        }
        String ret = type + ":" + timeMillis + ":" + hostname + ":" + hostip;
        return "<" + ret + ">";
    }
}
