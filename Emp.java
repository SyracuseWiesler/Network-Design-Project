package corp;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * the class Emp contains all the features and methods of an employee of the Lanshi Group
 */
public class Emp implements Serializable, IBasicInfo, IFileOperator, ICommunicator {
    private static final long serialVersionUID = 112L;
    private static byte[] keys = CorpSystem.retrieveKeys();//acquire the current set of encryption keys. Each connection
    //shall only request the encryption keys at the beginning of employee initiation
    private static byte fileNameKey = keys[0];
    private static byte downloadKey = keys[1];
    private static byte uploadKey = keys[2];
    private static byte speakKey = keys[3];
    String name;
    private Sex sex;
    private int age;
    String id;
    private String position;
    private Address address;
    private String tel;
    private String hostname;
    private String hostip;
    public static int hearPort = 992;

    /**
     * the constructor to create a new Employee with hostname and hostip. this function is used for test
     * @param hostname hostname of an employee
     * @param hostip ip address of an employee
     */
    public Emp (String hostname, String hostip) {
        this.hostname = hostname;
        this.hostip = hostip;
    }

    /**
     * the constructor to create a new Employee with name, id, hostname and hostip
     * @param name the name of an employee
     * @param id the id of an employee in the company
     * @param hostname the hostname of an employee's PC
     * @param hostip the IP address of an employee's PC
     * @throws IOException
     */
    public Emp (String name, String id, String hostname, String hostip) throws IOException {
        this(hostname, hostip);
        this.name = name;
        this.id = id;
        storeObject(this.name, this.id);
        CorpWindow corpWindow = new CorpWindow(this, "System");
        corpWindow.showWelcomeWindow();
        corpWindow.initFrame(600, 500, true);
    }
    public Emp (String name, String id, String hostname, String hostip, int hearPort) throws IOException {
        this(name, id, hostname, hostip);
        Emp.hearPort = hearPort;
    }
    @Override
    public int getHearPort () {
        return hearPort;
    }
    public void storeObject (String name, String id) throws IOException {
        String fileName = name + "_" + id + ".db";
        String path = CorpSystem.EMPS_PARENT + File.separator + fileName;
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path)));
        oos.writeObject(this);
        oos.close();
    }
    public static void main (String[] args) throws UnknownHostException, IOException, Exception {
        Emp e0 = new Emp ("alex", "ME123", "alex", "192.168.17.1");
//        Thread t = new Thread(new recvThreadCore(e0,))
//        e0.getHearPort();
//        String str = e0.formatContent("hello, I love China_US_Britan ***...\\\\///6789");
//        System.out.println(e0.parseContent(str));
//        CorpWindow corpWindow = new CorpWindow("System");
//        e0.download("a.txt", "D:\\");
//        e0.upload("E:\\d.txt");


//        corpWindow.showWelcomeWindow(corpWindow);
//        corpWindow.showDownloadWindow(corpWindow);
//        corpWindow.showDialog(corpWindow);
//        corpWindow.initFrame(600, 500, false);
//        corpWindow.setVisible(true);

//        e0.call("192.168.17.1");
//        e0.download("E:\\mid.mp4", "D:\\a.mp4");
    }
    @Override
    public void download (String fileName, String destParent) throws UnknownHostException, IOException, Exception {
        // accept the filename and save path
        String path = CorpSystem.FILES_PARENT + File.separator + fileName;
        String destPath = destParent + File.separator + fileName;
        File f = new File(path);
//        System.out.println(CorpSystem.sendPort);
        Socket socket = new Socket(InetAddress.getByName(CorpSystem.hostip), CorpSystem.sendPort);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        FileOutputStream fos = new FileOutputStream(new File(destPath));
        int length = 0;
        //send the sign for download
        os.write(0);
        is.read();
        //send the demanded file path
        byte[] buf = new byte[8 * 1024];
        byte[] tempBuf = path.getBytes();
        for (byte b: tempBuf) {
            b ^= fileNameKey;
        }
        os.write(tempBuf);
        //process of downloading file
        while ((length = is.read(buf)) != -1) {
            for (byte b: buf) b ^= downloadKey;
            fos.write(buf, 0, length);
            fos.flush();
        }
        fos.close();
        is.close();
    }
    @Override
    public void upload (String path) throws UnknownHostException, IOException, Exception {
        File f = new File(path);
        Socket socket = new Socket(InetAddress.getByName(CorpSystem.hostip), CorpSystem.sendPort);
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        FileInputStream fis = new FileInputStream(f);
        int length = 0;
        os.write(1);// send the signal for uploading
        is.read();// receive the reply for fully preparation to upload filename
        byte[] buf = new byte[8 * 1024];
        byte[] tempBuf = f.getName().getBytes();
        for (byte b: tempBuf) b ^= fileNameKey;
        os.write(tempBuf);
        length = is.read();// receive the signal from Server side and begin to upload the file content
        while ((length = fis.read(buf)) != -1) {
            for (byte b: buf) b ^= uploadKey;
            os.write(buf, 0, length);
            os.flush();
        }
        fis.close();
        os.close();
    }

    static class recvThreadCore implements Runnable {//responsible for receiving message in a conversation
        String hostname;
        Emp emp;
        InetAddress peerHost;
        JTextArea showArea;
        JTextArea inputArea;
        JButton back;
        JButton send;
        public recvThreadCore (Emp emp, ArrayList<JComponent> jList) {
            this.emp = emp;
            this.hostname = emp.hostname;
            try {
//                peerHost = InetAddress.getByName(hostname);//real situation
                peerHost = InetAddress.getLocalHost();//for test
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            this.showArea = (JTextArea)jList.get(0);
            this.inputArea = (JTextArea)jList.get(1);
            this.back = (JButton)jList.get(2);
            this.send = (JButton)jList.get(3);
        }
        @Override
        public void run () {
            File commRecord = new File(CorpSystem.DIALOG_RECORD);
            DatagramSocket ds = null;
            FileInputStream fis = null;
            int length = 0;
            byte[] buf = new byte[8 * 1024];
            try {
                fis = new FileInputStream(commRecord);
                ds = new DatagramSocket(hearPort);
                System.out.println(hearPort);
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (true) {
                DatagramPacket dp_recv = new DatagramPacket(buf, buf.length);
                try {
                    assert ds != null;
                    ds.receive(dp_recv);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("received");
                String reply = Encoder.parseContent(new String(buf, 0, dp_recv.getLength()), speakKey);
//                String name = peerHost.getHostName();//real situation
                String name = "alex".equals(hostname) ? "eric" : "alex";//for test
//                showArea.setText("<" + CorpSystem.getFormattedTime() + ">" + name + ": " + reply);//should be peerHost.getHostName()
                try {
                    showArea.setText(CommRecord.load());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public void setSex(Sex sex) {
        this.sex = sex;
    }

    @Override
    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public void setAddress(int num, String street, String city, String province, int zipcode) {
        address = new Address(num, street, city, province, zipcode);
    }

    @Override
    public void setTel(String tel) {
        Pattern pattern = Pattern.compile("[1][357]\\d{9}");// a simple mobile number regular expression fit for China domestic mobile number format
        Matcher matcher = pattern.matcher(tel);
        if (matcher.matches()) {
            this.tel = tel;
        } else {
            System.out.println("wrong format, input invalid");//test message for unmatched format
        }
    }

    @Override
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Override
    public void setHostip(String hostip) {
        this.hostip = hostip;
    }

    @Override
    public int getAge() {
        return this.age;
    }

    @Override
    public Sex getSex() {
        return this.sex;
    }

    @Override
    public String getPosition() {
        return this.position;
    }

    @Override
    public Address getAddress() {
        return this.address;
    }

    @Override
    public String getTel() {
        return this.tel;
    }

    @Override
    public String getHostname() {
        return this.hostname;
    }

    @Override
    public String getHostip() {
        return this.hostip;
    }
}
