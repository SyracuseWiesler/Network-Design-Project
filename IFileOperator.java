package corp;

import java.io.IOException;
import java.net.UnknownHostException;

interface IFileOperator {
    public static final int recvPort = 6035;
    public abstract void download (String Path, String destPath) throws UnknownHostException, IOException, Exception;
    public void upload (String path) throws UnknownHostException, IOException, Exception;
}
