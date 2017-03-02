package corp;
import java.util.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.regex.*;
import java.lang.reflect.*;
public class CorpWindow extends JFrame {
    int width;
    int height;
    String title;
    Emp emp;
    private static JPanel welcomePanel;
    private static JPanel downloadPanel;
    private static JPanel dialogPanel;
    private static String downloadFileName;
    private static String downloadSavePath;
    private static byte[] keys = CorpSystem.retrieveKeys();
    private static byte speakKey = keys[3];

    public CorpWindow (Emp emp, String title) {
        super(title);
        this.emp = emp;
    }
    public ArrayList<JComponent> showWelcomeWindow () {
        GridLayout gridLayout = new GridLayout(3, 1);
        JPanel jPanel = new JPanel();
        welcomePanel = jPanel;
        jPanel.setLayout(gridLayout);
        JPanel jPanel1 = new JPanel();
        jPanel1.setLayout(null);
        JPanel jPanel2 = new JPanel();
        jPanel2.setLayout(null);
        JPanel jPanel3 = new JPanel();
        JButton jButton = new JButton("next");
        JLabel jLabel = new JLabel("Welcome to FTP & Communication System");
        JLabel signature = new JLabel("Author: Zuhui He");
        JRadioButton download = new JRadioButton("Download");
        JRadioButton upload = new JRadioButton("Upload");
        JRadioButton dialog = new JRadioButton("Dialog");
//        JRadioButton hear = new JRadioButton("Hear");
        ButtonGroup bg = new ButtonGroup();
        /*------------separating ling for window style setting------------*/
        addComponent(bg, download, upload, dialog);
        addComponent(jPanel1, jLabel);
        jLabel.setBounds(150, 80, 250, 40);
        signature.setBounds(440, 90, 200, 40);
        addComponent(jPanel2, download, upload, dialog);
        download.setBounds(225, -10, 100, 40);
        upload.setBounds(225, 30, 100, 40);
        dialog.setBounds(225, 70, 100, 40);
//        hear.setBounds(225, 80, 100, 40);
        jButton.setBounds(220, 0, 90, 30);
        addComponent(jPanel3, jButton, signature);
        jPanel3.setLayout(null);
        addComponent(jPanel, jPanel1, jPanel2, jPanel3);
        this.add(jPanel);
        ArrayList<JComponent> jList = new ArrayList<>();
        addComponent(jList, download, upload, dialog, jButton);
        download.setSelected(true);
        int length = jList.size();
        CorpWindow cw = this;
        jButton.addMouseListener(new MouseAdapter(){
            public void mouseClicked (MouseEvent e) {
                for (int i = 0; i < length - 1; i ++) {
                    JRadioButton r = (JRadioButton) jList.get(i);
                    if (r.isSelected()) {
                        switch (i) {
                            case 0:
                                showDownloadWindow();
                                jPanel.setVisible(false);
                                cw.setTitle("System - Download");
                                break;
                            case 1:
                                showUpload();
                                break;
                            case 2:
                                String peerip = "";
                                boolean b = false;
                                while ( !b ) {
                                    peerip = JOptionPane.showInputDialog("Opposite IP Address");
                                    if (peerip == null) break;
                                    b = verifyIP(peerip);
                                    if (!b){
                                        JOptionPane.showMessageDialog(cw, "invalid IP address", "ERROR", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                                if (b) {
                                    ArrayList<JComponent> jList = showDialog(emp);
                                    jPanel.setVisible(false);
                                    String title = emp.getHostname() + "'s Dialog";
                                    cw.setTitle(title);
//                                    try {
//                                        if (i == 2) {
//                                            emp.call(peerip, jList);
//                                        } else {
//                                            emp.hear(peerip, jList);
//                                        }
//                                    } catch (IOException exception) {
//                                        throw new RuntimeException(exception.getMessage());
//                                    }
                                }
                                break;
                        }
                        break;
                    }
                }
            }
        });
        return jList;
    }
    public ArrayList<JComponent> showDownloadWindow () {//return to the radio button list of files available for downloading
        ArrayList<JComponent> jList = new ArrayList<>();
        GridLayout gridLayout = new GridLayout(3, 1);
        JPanel jPanel = new JPanel();
        downloadPanel = jPanel;
        jPanel.setLayout(gridLayout);
        JPanel jPanel0 = new JPanel();
        jPanel0.setLayout(null);
        JPanel jPanel1 = new JPanel();
        JPanel jPanel2 = new JPanel();
        File dir = new File(CorpSystem.FILES_PARENT);
        String[] filenameList = dir.list();
        ButtonGroup bg = new ButtonGroup();
        JLabel jLabel = new JLabel("The files in the System: ");
        JButton jButton1 = new JButton("download");
        JButton jButton2 = new JButton("back");
        addComponent(jList, jButton1, jButton2);
        for (String str: filenameList) {
            JRadioButton jRadioButton = new JRadioButton(str);
            jList.add(jRadioButton);
            bg.add(jRadioButton);
            jPanel1.add(jRadioButton);
        }
        JRadioButton j = (JRadioButton)jList.get(2);
        j.setSelected(true);
        addComponent(jPanel0, jLabel);
        jLabel.setBounds(220, 50, 150, 40);
        addComponent(jPanel2, jButton1, jButton2);
        addComponent(jPanel,jPanel0,jPanel1,jPanel2);
        this.add(jPanel);
        CorpWindow cw = this;
        jButton1.addMouseListener (new MouseAdapter () {//download
            String fileName = "";
            public void mouseClicked (MouseEvent e) {
                int length = jList.size();
                for (int i = 2; i < length; i ++) {
                    JRadioButton r = (JRadioButton) jList.get(i);
                    if (r.isSelected()) {
                        fileName = r.getText();
                        break;
                    }
                }
                FileDialog fileDialog = new FileDialog(cw, "Save", FileDialog.SAVE);
                fileDialog.setVisible(true);
                String parent = fileDialog.getDirectory();
                if (parent != null){
                    downloadFileName = fileName;
                    downloadSavePath = parent;
                    try {
                        emp.download(downloadFileName, downloadSavePath);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception.getMessage());
                    }
                }
            }
        });
        jButton2.addMouseListener(new MouseAdapter () {//back
            public void mouseClicked (MouseEvent e) {
                jPanel.setVisible(false);
                welcomePanel.setVisible(true);
                cw.setTitle("System");
            }
        });
        return jList;
    }
    public String showUpload () {//return the complete uploading path
        FileDialog fileDialog = new FileDialog(this, "Upload", FileDialog.LOAD);
        fileDialog.setVisible(true);
        String parentPath = fileDialog.getDirectory();
        String fileName = fileDialog.getFile();
        String path = parentPath + File.separator + fileName;
        try {
            emp.upload(path);
        } catch (Exception exception) {
            throw new RuntimeException (exception.getMessage());
        }
        return path;
    }
    public ArrayList<JComponent> showDialog (Emp emp) {
        try {
            CommRecord.clear();//for test; just clear the record each time before communication
        } catch (IOException e) {
            e.printStackTrace();
        }
        JPanel jPanel = new JPanel();
        dialogPanel = jPanel;
        jPanel.setBounds(0, 0, 600, 500);
        jPanel.setLayout(null);
        JTextArea showArea = new JTextArea(10, 40);
        JTextArea inputArea = new JTextArea(2, 40);
        JButton jButton0 = new JButton("back");
        JButton jButton = new JButton("send(Enter)");
        addComponent(jPanel, showArea, inputArea, jButton0, jButton);
        showArea.setBounds(20, 35, 540, 300);
        showArea.setEditable(false);//only used to show content, you cannot write in show area
        inputArea.setBounds(20, 345, 540, 50);
        jButton0.setBounds(380, 405, 70, 35);
        jButton.setBounds(460, 405, 100, 35);
        inputArea.requestFocus(true);
        this.add(jPanel);
        ArrayList<JComponent> jList = new ArrayList<>();
        addComponent(jList, showArea, inputArea, jButton0, jButton);
        CorpWindow cw = this;


        String hostname = emp.getHostname();
//        Emp.sendThreadCore es1 = new Emp.sendThreadCore(hostname, jList);
        Emp.recvThreadCore es2 = new Emp.recvThreadCore(emp, jList);
//        Thread t1 = new Thread(es1, "SEND");
        Thread t2 = new Thread(es2, "RECEIVE");
//        t1.start();
        t2.start();
        jButton0.addMouseListener(new MouseAdapter () {//back
            public void mouseClicked (MouseEvent e) {
                dialogPanel.setVisible(false);
                welcomePanel.setVisible(true);
                cw.setTitle("System");
            }
        });
//        jButton.addMouseListener (new MouseAdapter () {//send
//            String content = showArea.getText();
//            public void mouseClicked (MouseEvent e) {
//                String text = inputArea.getText();
//                content += (text + "\n\r");
//                showArea.setText(content);
//                inputArea.setText("");
//                inputArea.requestFocus();
//            }
//        });
        jButton.addActionListener(new ActionListener() {//send
            public void actionPerformed(ActionEvent e) {
                System.out.println("clicked");
                DatagramSocket ds = null;
                String prefix = "";
                try {
                    ds = new DatagramSocket();
                    prefix = "<" + CorpSystem.getFormattedTime() + ">" + hostname + "：";
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                String content = inputArea.getText();
                String codedContent = Encoder.formatContent(emp, content, speakKey);//format the content + encryption
//                System.out.println(content);
                DatagramPacket dp_send = null;
                try {
                    int send_port = "alex".equals(emp.name)? 991 : 992;//only for test, alex: port 992; eric: port 991
                    dp_send = new DatagramPacket(codedContent.getBytes(), codedContent.getBytes().length, InetAddress.getLocalHost(), send_port);//emp.getHearPort()
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }
                try {
                    assert ds != null;
                    ds.send(dp_send);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    CommRecord.store(prefix + content);
                    showArea.setText(CommRecord.load());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                inputArea.setText("");
            }
        });

        inputArea.addKeyListener(new KeyAdapter() {//use the Enter key on the keyboard to send
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    jButton.doClick();
                }
            }
        });
        return jList;
    }
    public void initFrame (int width, int height, boolean visible) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dimension = toolkit.getScreenSize();
        int x = (int) dimension.getWidth();
        int y = (int) dimension.getHeight();
        this.setBounds((x - width) / 2, (y - height) / 2, width, height);
        this.setVisible(visible);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);//the program ends at the last visible window closed
    }
    public static void addComponent (JPanel panel, Component... cs) {
        for (Component ci: cs) {
            panel.add(ci);
        }
    }
    public static void addComponent (ButtonGroup bg, JRadioButton... r) {
        for (JRadioButton ri: r) {
            bg.add(ri);
        }
    }
    public static void addComponent (ArrayList<JComponent> jList, JComponent... r) {
        for (JComponent ri: r) {
             jList.add(ri);
        }
    }
    public static boolean verifyIP (String ip) {
        //a valid ip address is like: 192.168.10.2 or 10.2.2.1, which is composed of four parts and each part should contain 1 to 3 digits of number
        if (! ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) return false;
        Pattern p = Pattern.compile("(\\d{1,3})");
        Matcher m = p.matcher(ip);
        Boolean b = true;
        while (m.find()) {
            int i = Integer.parseInt(m.group());
            if (i > 255) b &= false;
        }
        return b;
    }

}
