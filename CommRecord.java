package corp;
import java.io.*;
import java.util.*;
public class CommRecord implements Serializable {
    private static final long serialVersionUID = 10L;
    public static void store (String content) throws IOException {
        File f = new File(CorpSystem.DIALOG_RECORD);
        FileOutputStream fos = new FileOutputStream(f, true);
        content += "\r\n";
        fos.write(content.getBytes());
        fos.close();
    }
    public static String load () throws IOException {
        File file = new File (CorpSystem.DIALOG_RECORD);
        FileInputStream fis = new FileInputStream(file);
        int length = 0;
        byte[] buf = new byte[8 * 1024];
        String ret = "";
        while ((length = fis.read(buf)) != -1) {
            ret += new String(buf, 0, length);
        }
        fis.close();
        return ret;
    }
    public static void clear () throws IOException {
        File f = new File(CorpSystem.DIALOG_RECORD);
        FileOutputStream fos = new FileOutputStream(f);
        fos.write("".getBytes());
        fos.close();
    }
}

