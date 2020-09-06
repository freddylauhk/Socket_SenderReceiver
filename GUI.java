import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GUI extends JFrame
{
    public JButton sendButton;

    public JButton connectButton;

    public JTextArea txArea, rxArea;

    public Container container;


    public JTextField inputIP;

    public JTextField inputPort;

    public JLabel ipLabel;

    public JLabel portLabel;


    public JLabel commandLabel;

    public JLabel jLabel1;

    public  GUI (String title)
    {
        super (title);

        container = getContentPane();
        container.setLayout( new FlowLayout() );

        setPreferredSize(new Dimension(550, 550));
        container.setPreferredSize(new Dimension(500, 500));


        txArea = new JTextArea (4, 30);
        txArea.setLineWrap(true);
        JScrollPane txArea0 = new JScrollPane(txArea);

        rxArea = new JTextArea (8, 45);
        rxArea.setLineWrap(true);
        JScrollPane rxArea0 = new JScrollPane(rxArea);

        inputIP = new JTextField(15);

        inputPort = new JTextField(15);

        sendButton = new JButton ("Send");

        connectButton = new JButton ("connect");

        ipLabel = new JLabel("IP:");

        portLabel = new JLabel("Port:");

        commandLabel = new JLabel("Command:");

        jLabel1 = new JLabel();

        String  sText  = "<html><br/>Command List:<br/><table><tr><td>send &lt;no-of-items&gt;</td><td>The sender will deduct its inventory by &lt;no-of-items&gt; <br/> and the receiver will add &lt;no-of-items&gt; to its inventory.</td></tr><tr><td>request &lt;no-of-items&gt;</td><td>The receiver will deduct its inventory by &lt;no-of-items&gt;<br/> and issue the SEND command with &lt;no-of-items&gt;.<br/> If the receiver does not have sufficient items, <br/>it should reply with an INSUFFICIENT command. </td></tr><tr><td>check</td><td>The receiver will respond with the<br/> number of available items in its inventory.</td></tr><tr><td>refill</td><td>If the inventory reaches zero, <br/>the program will automatically send a REFILL command. <br/>Then, the receiver has to issue a SEND <br/>command and send 500 items to the sender. </td></tr></table></html>";

        jLabel1.setText (sText);


        container.add (ipLabel);
        container.add (inputIP);
        container.add (portLabel);
        container.add (inputPort);
        container.add (connectButton);
        container.add (rxArea0);
        container.add (commandLabel);
        container.add (txArea0);
        container.add (sendButton);
        container.add(jLabel1);
        setResizable(false);
        rxArea.setEditable(false);


    }
    public static void main(String[] args){
        GUI gui = new GUI("inv");
        gui.pack();
        gui.show();
    }
}