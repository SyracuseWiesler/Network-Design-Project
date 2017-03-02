package corp;
import java.util.*;
import java.net.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
interface ICommunicator {
    public static final int callPort = 991;//
    public static final int hearPort = 992;
//    public abstract void call (String peerip, ArrayList<JComponent> jList) throws IOException;
//    public abstract void hear (String peerip, ArrayList<JComponent> jList) throws IOException;
    public int getHearPort();
}
