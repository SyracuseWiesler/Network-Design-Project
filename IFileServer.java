package corp;

import java.io.IOException;

interface IFileServer {
    public static final int sendPort = 6033;
    public abstract void launch () throws IOException, Exception;
}
