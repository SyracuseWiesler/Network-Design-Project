package corp;
//import jdk.internal.util.xml.impl.Input;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.*;
import java.text.*;
//import java.awt.*;
//import java.awt.event.*;
//import javax.swing.*;

/**
 * class CorpSystem is the combination of File Transfer System and real time communication system
 * The CorpSystem performs as a server for file transfer.
 * The CorpSystem will manage all the configuration files and file transfer and communication record
 * The CorpSystem has high level of security measurement, which included software level encryption
 * @author Zuhui He
 *
 * */
public class CorpSystem implements IFileServer {
    public static final String hostip = "192.168.17.1";//set up a host ip for test
    public static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";//time format 2016-11-12 12:30:52
    private static CorpSystem sys;//Singleton mode for the System. The Corporation only has one system.
    //fileNameKey, downloadKey, uploadKey, speakKey
//    private static int EN_KEY = 5;
    private static byte[] keys = retrieveKeys();//acquire the set of the encryption key
    private static byte fileNameKey = keys[0];//encryption key which will be used in file name transferring
    private static byte downloadKey = keys[1];//encryption key which will be used in downloading
    private static byte uploadKey = keys[2];//encryption key which will be used in uploading

    public static final String CONFIG = "E:\\project\\config\\config.db";
    public static final String FILES_PARENT = "E:\\project\\files";
    public static final String EMPS_PARENT = "E:\\project\\emps";
    public static final String DOWNLOAD_RECORD = "E:\\project\\records\\download.db";
    public static final String UPLOAD_RECORD = "E:\\project\\records\\upload.db";
    public static final String DIALOG_RECORD = "E:\\project\\records\\dialog.db";
    private CorpSystem () {}
    private static final String instanceInitiated = "SYS";//the thread lock used in singleton mode to acquire the system's single instance

    public static CorpSystem getSys () {
        if (sys == null) {
            synchronized (instanceInitiated) { // for thread security purpose
                if (sys == null) {
                    sys = new CorpSystem();
                }
            }
        }
        return sys;
    }
    public static void main(String[] args) throws IOException, Exception {
        CorpSystem sys = CorpSystem.getSys();//get the CorpSystem's single instance
        Thread t1 = new Thread(new KeySetter());//create a new thread for the encryption key setter
        t1.start();
        sys.launch();
    }

    /**
     * the method loadObject will load the file for a specific employee into the CorpSystem.
     * @param name the employee name
     * @param id the employee id
     * @return the specific employee object
     * @throws IOException
     * @throws Exception
     */
    public static Emp loadObject (String name, String id) throws IOException, Exception {
        String fileName = name + "_" + id + ".db";
        String path = EMPS_PARENT + File.separator + fileName;
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(path)));
        Emp emp = (Emp)ois.readObject();
        ois.close();
        return emp;
    }

    /**
     * the class KeySetter is a static internal class of CorpSystem, which will initiate a new thread for setting the
     * encryption key periodically.
     */
    static class KeySetter implements Runnable{
        @Override
        public void run () {
            try {
                resetKeys();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * initiate the whole system
     * @throws IOException
     * @throws Exception
     */
    public void launch () throws IOException, Exception { // FTP server

        ServerSocket ss = new ServerSocket(sendPort);
        Socket socket  = ss.accept();
        String[] connInfo = getConnInfo(socket);
        byte[] buf = new byte[8 * 1024];
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        //accept a number to sign for different task: 0 for downloading; 1 for uploading
        int sign = is.read();
        os.write(0);//reply a random number meaning that I got the content. It is important to separate the content for different transfer
        String fileName = "";
        switch (sign) {
            case 0:
                fileName = forDownload(buf, socket, is, os);
                break;
            case 1:
                fileName = forUpload(buf, socket, is, os);
                break;
        }
        System.out.println(fileName);
        addNewRecord(sign, connInfo);

    }

    /**
     * the method forDownload is used to serve the downloading function of file transfer
     * @param buf the buffered byte array used for store the content read per time
     * @param socket the socket for this download operation
     * @param is a specific input stream to read content of a file
     * @param os a specific output stream to write content into a file
     * @return the parent path of the file in the system
     * @throws IOException
     */
    public String forDownload (byte[] buf, Socket socket, InputStream is, OutputStream os) throws IOException {
//        byte[] buf = new byte[8 * 1024];
//        InputStream is = socket.getInputStream();
//        OutputStream os = socket.getOutputStream();
//        int length = 0;
        int length = is.read(buf);
        //accept the file path that need to be transferred to the user
        for (byte b: buf) {
            b ^= fileNameKey;
        }
        String path = new String(buf, 0, length);
        File f = new File(path);
        FileInputStream fis = new FileInputStream(f);
        //send the file
        while ((length = fis.read(buf)) != -1) {
            for (byte b: buf) b ^= downloadKey;
            os.write(buf, 0, length);
            os.flush();
        }
        socket.close();
        String[] pathSections = path.split("\\\\");
        return pathSections[pathSections.length - 1];
    }

    /**
     * the method forUpload is used for user to upload file in the system.
     * @param buf the buffered byte array used for store the content read per time
     * @param socket the socket for this download operation
     * @param is a specific input stream to read content of a file
     * @param os a specific output stream to write content into a file
     * @return the name of the file that is uploaded in the system
     * @throws IOException
     */
    public String forUpload (byte[] buf, Socket socket, InputStream is, OutputStream os) throws IOException {
        System.out.println("Uploading... Do not shut down the window");
        int length = is.read(buf);
        for (byte b: buf) b ^= fileNameKey;
        String fileName = new String(buf, 0, length);
        String path = FILES_PARENT + File.separator + forcedNewName(fileName);
        os.write(0);//send a signal meaning I finished receiving and you could begin to send the file
        FileOutputStream fos = new FileOutputStream(new File(path));
        while ((length = is.read(buf)) != -1) {
            for (byte b: buf) b ^= uploadKey;
            fos.write(buf, 0, length);
            fos.flush();
        }
        fos.close();
        socket.close();
        System.out.println("Uploading Accomplished");
        return fileName;
    }

    /**
     * the method getFormattedTime is used to get the current time of the system
     * @return the formatted time string
     */
    public static String getFormattedTime () {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT);
        return simpleDateFormat.format(System.currentTimeMillis());
    }

    /**
     * the method getConnInfo is used to acquire the information of the host connected to the CorpSystem
     * @param socket the socket of a specific connection
     * @return an array of string containing the connected hostname and ip address
     */
    private static String[] getConnInfo (Socket socket) {
        InetAddress connectedHost = socket.getInetAddress();
        return new String[]{connectedHost.getHostName(), connectedHost.getHostAddress()};
    }

    /**
     * the method addNewRecord is used to record the history of file downloading and uploading and store the history in
     * a separate file
     * @param sign an integer value to distinct the download and upload operation and save in different history file
     * @param connInfo an array of string which include the information of host: hostname and ip address
     * @throws IOException
     */
    public void addNewRecord (int sign, String[] connInfo) throws IOException {
        String comments = "";
        String path = "";
        switch (sign) {
            case 0:
                comments = "Download";
                path = DOWNLOAD_RECORD;
                break;
            case 1:
                comments = "Upload";
                path = UPLOAD_RECORD;
        }
        Properties p = new Properties();
        FileTransferRecord record = new FileTransferRecord(sign, System.currentTimeMillis(), connInfo[0], connInfo[1]);
        File file = new File(path);
        p.load(new FileInputStream(file));//read the record
        p.setProperty(getFormattedTime(), record.toString());//add new record
        p.store(new FileOutputStream(file), comments);//save record
    }

    /**
     * the method forcedNewName is to set up a new name for file which is uploaded in the system when there is
     * a file already exists in the system with a same name
     * @param fileName the name of the file which
     * @return the name which is proper for the file to save in the correct path
     */
    public static String forcedNewName (String fileName) {
        //if you want to save a.txt, T0 means a.txt has already existed, T1 means a(num).txt has already existed
        //if T0 not existed, save a.txt directly; if T0 existed, save a(num).txt
        //if both T0 and T1 existed, acquire the num in a(num).txt then save a(num + 1).txt. For example, a(9).txt will change to a(10).txt
        File file = new File(CorpSystem.FILES_PARENT + File.separator + fileName);
        boolean B0 = file.exists();
        String fName = fileName.split("\\.")[0];
        String type = "." + fileName.split("\\.")[1];
        Pattern pattern = Pattern.compile(fName + "\\((\\d+)\\)" + type);
        String[] fileNames = new File(CorpSystem.FILES_PARENT).list();
        int num = 0;
        ArrayList<Integer> list = new ArrayList<>();
        boolean B1 = false;
        for (String fileName0: fileNames) {
            Matcher matcher = pattern.matcher(fileName0);
            if (matcher.find()) {
                num = Integer.parseInt(matcher.group(1));
                list.add(num);
                B1 = true;
            }
        }
        if (!list.isEmpty()) num = Collections.max(list);
        String ret = "";
        if (!B0) {
            ret = fileName;
        } else if (!B1) {
            ret = fName + "(0)" + type;
        } else {
            ret = fName + "(" + (++ num) + ")" + type;
        }
        return ret;
    }

    /**
     * The method resetKeys is used to set up a set of encryption keys used in file downloading and uploading
     * @throws IOException
     */
    public static void resetKeys () throws IOException {
        while (true) {
            byte[] keys = new byte[4];//four different encryption keys: fileNameKey, downloadKey, uploadKey, speakKey
            Random random = new Random();
            random.nextBytes(keys);
            int i = 0;
            String str = "";
            for (byte b : keys) {
                str += b;
                if (++i < keys.length) str += ":";
            }
            Properties properties = new Properties();
            File config = new File(CONFIG);
            properties.load(new FileInputStream(config));
            properties.setProperty("KEYS", str);
            properties.store(new FileOutputStream(config), "KEYS");
            try {
                Thread.sleep(1000 * 60 * 60 * 2);//encryption key will be reset every two hours
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * the method retrieveKeys is used to acquire the current set of encryption key
     * @return an array of byte numbers which will be regarded as the encryption keys
     */
    public static byte[] retrieveKeys () {
        byte[] bytes = new byte[4];
        Properties properties = new Properties();
        File config = new File(CONFIG);
        try {
            properties.load(new FileInputStream(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String str = properties.getProperty("KEYS");
        int i = 0;
        for (String s: str.split(":")) {
            bytes[i ++] = (byte)Integer.parseInt(s);
        }
        return bytes;
    }
}

