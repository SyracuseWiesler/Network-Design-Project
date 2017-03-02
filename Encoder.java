package corp;
import java.util.*;
import java.io.*;

public class Encoder implements Serializable {
    private static final long serialVersionUID = 112L;
    public static String formatBytes (Iterator<byte[]> bytes, byte key) {
        String str = "";
        while (bytes.hasNext()) {
            byte[] buf = bytes.next();
            encode(buf, key);
            for (byte b: buf) {
                str += (b + ",");
            }
            if (bytes.hasNext())str += "_";
        }
        return str;
    }
    public static String formatContent (Emp emp, String content, byte key) {//format content + encryption
        ArrayList<byte[]> list = new ArrayList<>();
        list.add(CorpSystem.getFormattedTime().getBytes());
        list.add(emp.getHostname().getBytes());
        list.add(emp.getHostip().getBytes());
        list.add(content.getBytes());
        list.add(Integer.toString(content.hashCode()).getBytes());
        Iterator<byte[]> iterator = list.iterator();
        return formatBytes(iterator, key);
    }
    public static String parseContent (String formattedContent, byte key) {//decryption + anti-format content
        String[] strs = formattedContent.split("_");
        String substrs = strs[3];
        ArrayList<Byte> byteList = new ArrayList<>();
        for (String s: substrs.split(",")) {
            byteList.add((byte)Integer.parseInt(s));
        }
        Byte[] bytes = byteList.toArray(new Byte[byteList.size()]);
        byte[] buf = new byte[bytes.length];
        int i = 0;
        for (Byte b: bytes) {
            buf[i ++] = b;
        }
        encode(buf, key);
        return new String(buf);
    }
    public static void encode (byte[] buf, int length, byte key) {
        for (int i = 0; i < length; i ++) {
            buf[i] ^= key;
        }
    }
    public static void encode (byte[] buf, byte key) {
        for (byte b: buf) {
            b ^= key;
        }
    }
}
