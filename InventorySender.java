import java.io.*;
import java.net.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.text.SimpleDateFormat;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.io.*;
import java.security.*;
import javax.crypto.Cipher;


public class InventorySender extends GUI{
    public ButtonHandler bHandler = new ButtonHandler();
    public CuttonHandler cHandler = new CuttonHandler();
    private int numberOfInventory;
    DatagramSocket socket;

    PublicKey target_pubkey;
    PublicKey pubKey;
    PrivateKey privateKey;


    public InventorySender(String invName) throws IOException
    {
        //build GUI;
        super (invName);
        bHandler = new ButtonHandler ();
        sendButton.addActionListener (bHandler);
        connectButton.addActionListener (cHandler);

        inputPort.setText(Integer.toString(4455));
        inputPort.setEditable(false);
        //init socket
        socket = new DatagramSocket (4466);

        //initialize Inventory
        numberOfInventory = 1000;
        target_pubkey = null;
    }

    private class CuttonHandler implements ActionListener {
        public void actionPerformed (ActionEvent event){
            final String ip;
            final int port;
            try {
                KeyPair keyPair = buildKeyPair();
                pubKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();

                DatagramSocket socket = new DatagramSocket();

                byte[] buf = new byte[162];

                ip = inputIP.getText();
                port = Integer.valueOf(inputPort.getText());

                buf = encodePublicKey(pubKey);
                InetAddress address = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                System.out.println("About to message");
                socket.send(packet);
                rxArea.append("\n Public key sent.");
                connectButton.setEnabled(false);
            }
            catch (IOException e)
            {
                System.out.println("C Button Handler fail.");
            }
            catch (NoSuchAlgorithmException e){
            }
        }
    }

    private class ButtonHandler implements ActionListener
    {


        public void actionPerformed (ActionEvent event) //throws IOException
        {
            final String ip;
            final int port;
            try
            {
                DatagramSocket socket = new DatagramSocket ();

                byte[] buf = new byte[256];

                String outputLine = txArea.getText ();

                ip = inputIP.getText();
                port = Integer.valueOf(inputPort.getText());

                buf = outputLine.getBytes ();
                InetAddress address = InetAddress.getByName (ip);
                DatagramPacket packet = new DatagramPacket (buf, buf.length, address, port);
                System.out.println ("About to message");


                String input = new String(packet.getData());
                // scan command
                StringBuffer sb = new StringBuffer();
                StringBuffer sb2 = new StringBuffer();
                int i;
                int flag = 0;
                for (i = 0; i < input.length(); i++){
                    if(!Character.isLetter(input.charAt(i))){
                        if(!Character.isDigit(input.charAt(i+1))){
                            break;
                        }
                        else {
                            flag = 1;
                            break;
                        }
                    }
                    sb.append(input.charAt(i));
                }
                if(flag == 1){
                    for(i += 1; i< input.length(); i++){
                        if(!Character.isDigit(input.charAt(i))){
                            break;
                        }
                        else{
                            sb2.append(input.charAt(i));
                        }
                    }
                }
                int items = -1;
                String realCommand = sb.toString();
                System.out.println(realCommand);
                if(flag == 1) {
                    String itemTemp = sb2.toString();
                    items = Integer.valueOf(itemTemp);
                }

                // recognize command
                //send
                if(realCommand.equals("send")) {
                    if(getInventoryNumber()>=items) {
                        if (!changeInventoryNumber(-1 * items)) {
                            rxArea.append("\n Change Inventory fail.");
                        }
                        socket.send(packet);
                        rxArea.append("\nThe current Inventory: " + getInventoryNumber());
                    }
                    else{
                        rxArea.append("\ninsufficient request.");
                    }
                }
                else{
                    socket.send(packet);
                }
                System.out.println ("Sent message");

                // if inventory reach zero, refill
                if(getInventoryNumber()<=0){
                    rxArea.append("\nInventory reached 0, Now refill.");
                    try {
                        socket = new DatagramSocket();

                        buf = new byte[256];

                        outputLine = "refill";

                        buf = outputLine.getBytes();
                        address = InetAddress.getByName(ip);
                        packet = new DatagramPacket(buf, buf.length, address, port);
                        System.out.println("About to message");
                        socket.send(packet);
                        System.out.println("Sent message");
                        rxArea.append("\nRefill request sent.");
                    } catch (IOException e) {
                        rxArea.append("\nRefill command fail.");
                    }
                    rxArea.setCaretPosition(rxArea.getDocument().getLength());
                }
            }
            catch (IOException e)
            {
                System.out.println("Button Handler fail.");
            }

        }
    }

    // Receive Message
    public void receive () throws IOException
    {
        try
        {
            DatagramPacket packet;
            byte[] buf = new byte[256];

            while (true)
            {
                buf = new byte[256];
                packet = new DatagramPacket (buf, buf.length);
                socket.receive (packet);
                System.out.println ("Received packet");
                String received = new String (packet.getData());
                rxArea.append ("\nReceived Command: "+received);
                scanCommand(received);

                // if inventory reach zero, refill
                if(getInventoryNumber()<=0){
                    final String ip;
                    final int port;
                    rxArea.append("\nInventory reached 0, Now refill.");
                    try {
                        DatagramSocket socket = new DatagramSocket();

                        buf = new byte[256];

                        String outputLine = "refill";

                        ip = inputIP.getText();
                        port = Integer.valueOf(inputPort.getText());

                        buf = outputLine.getBytes();
                        InetAddress address = InetAddress.getByName(ip);
                        packet = new DatagramPacket(buf, buf.length, address, port);
                        System.out.println("About to message");
                        socket.send(packet);
                        System.out.println("Sent message");
                        rxArea.append("\nRefill request sent.");
                    } catch (IOException e) {
                        rxArea.append("\nRefill command fail.");
                    }
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("fail in receive ()");
            System.exit(1);
        }
    }
    public void scanCommand(String input){
        final String ip;
        final int port;

        // scan command
        StringBuffer sb = new StringBuffer();
        StringBuffer sb2 = new StringBuffer();
        int i;
        int flag = 0;
        for (i = 0; i < input.length(); i++){
            if(!Character.isLetter(input.charAt(i))){
                if(!Character.isDigit(input.charAt(i+1))){
                    break;
                }
                else {
                    flag = 1;
                    break;
                }
            }
            sb.append(input.charAt(i));
        }
        if(flag == 1){
            for(i += 1; i< input.length(); i++){
                if(!Character.isDigit(input.charAt(i))){
                    break;
                }
                else{
                    sb2.append(input.charAt(i));
                }
            }
        }
        int items = -1;
        String realCommand = sb.toString();
        if(flag == 1) {
            String itemTemp = sb2.toString();
            items = Integer.valueOf(itemTemp);
        }

        // recognize command
        //send
        if(realCommand.equals("send")) {
            if (!changeInventoryNumber(items)) {
                rxArea.append("\n Change Inventory fail.");
            }
            rxArea.append("\nReceived "+ items+" items.");
            rxArea.append("\nThe current Inventory: "+getInventoryNumber());
        }
        else if(realCommand.equals("request")){
            if(getInventoryNumber()>=items) {
                if (!changeInventoryNumber(-1 * items)) {
                    rxArea.append("\n Change Inventory fail.");
                }
                try {
                    DatagramSocket socket = new DatagramSocket();

                    byte[] buf = new byte[256];

                    String outputLine = "send " + items;

                    ip = inputIP.getText();
                    port = Integer.valueOf(inputPort.getText());

                    buf = outputLine.getBytes();
                    InetAddress address = InetAddress.getByName(ip);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                    System.out.println("About to message");
                    socket.send(packet);
                    System.out.println("Sent message");
                    rxArea.append("\nSent "+ items+" items.");
                    rxArea.append("\nThe current Inventory: " + getInventoryNumber());
                } catch (IOException e) {
                    rxArea.append("\nsend command fail.");
                    changeInventoryNumber(items);
                    rxArea.append("\nThe current Inventory: " + getInventoryNumber());
                }
            }
            else{
                try {
                    DatagramSocket socket = new DatagramSocket();

                    byte[] buf = new byte[256];

                    String outputLine = "insufficient";

                    ip = inputIP.getText();
                    port = Integer.valueOf(inputPort.getText());

                    buf = outputLine.getBytes();
                    InetAddress address = InetAddress.getByName(ip);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                    System.out.println("About to message");
                    socket.send(packet);
                    System.out.println("Sent message");
                    rxArea.append("\ninsufficient request. Warning sent.");
                } catch (IOException e) {
                    rxArea.append("\ninsufficient command fail.");
                    changeInventoryNumber(items);
                    rxArea.append("\nThe current Inventory: " + getInventoryNumber());
                }
            }
        }
        else if(realCommand.equals("check")){
            try
            {
                DatagramSocket socket = new DatagramSocket ();

                byte[] buf = new byte[256];
                items = getInventoryNumber();
                String outputLine = "The receiver's Inventory number is " + items;

                ip = inputIP.getText();
                port = Integer.valueOf(inputPort.getText());

                buf = outputLine.getBytes ();
                InetAddress address = InetAddress.getByName (ip);
                DatagramPacket packet = new DatagramPacket (buf, buf.length, address, port);
                System.out.println ("About to message");
                socket.send(packet);
                System.out.println ("Sent message");
                rxArea.append("\nInventory status sent.");
            }
            catch (IOException e)
            {
                rxArea.append("\nsend information fail.");
            }
        }
        else if(realCommand.equals("refill")){
            if (!changeInventoryNumber(-500)) {
                rxArea.append("\n Change Inventory fail.");
            }
            rxArea.append("\nThe current Inventory: "+getInventoryNumber());
            try
            {
                DatagramSocket socket = new DatagramSocket ();

                byte[] buf = new byte[256];

                String outputLine = "send "+500;

                ip = inputIP.getText();
                port = Integer.valueOf(inputPort.getText());

                buf = outputLine.getBytes ();
                InetAddress address = InetAddress.getByName (ip);
                DatagramPacket packet = new DatagramPacket (buf, buf.length, address, port);
                System.out.println ("About to message");
                socket.send(packet);
                System.out.println ("Sent message");
                rxArea.append("\nRefill inventory sent.");
            }
            catch (IOException e)
            {
                rxArea.append("\nsend command fail.");
                changeInventoryNumber(items);
                rxArea.append("\nThe current Inventory: "+getInventoryNumber());
            }
        }
        else if(realCommand.equals("insufficient")){
            rxArea.append("\ninsufficient request!!!");
            rxArea.append("\nThe current Inventory: "+getInventoryNumber());
        }
        else{
            rxArea.append("\nNo such command.");
        }
        rxArea.setCaretPosition(rxArea.getDocument().getLength());
    }

    public int getInventoryNumber(){
        return numberOfInventory;
    }

    public Boolean changeInventoryNumber(int change){
        numberOfInventory += change;

        return true;
    }

    //security method
    public static KeyPair buildKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 1024;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);
        return keyPairGenerator.genKeyPair();
    }

    //key encoding and decoding
    public static byte[] encodePrivateKey(PrivateKey privateKey){
        byte[] privateKeyBytes = privateKey.getEncoded();
        return privateKeyBytes;
    }

    public static byte[] encodePublicKey(PublicKey publicKey){
        byte[] publicKeyBytes = publicKey.getEncoded();
        return publicKeyBytes;
    }

    public static PrivateKey decodePrivateKey(byte[] privateKeyBytes){
        try {
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey newPrivateKey = keyFactory.generatePrivate(privateKeySpec);
            return newPrivateKey;

        }catch (InvalidKeySpecException e) {
        }catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static PublicKey decodePublicKey(byte[] publicKeyBytes){
        try {
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey newPublicKey = keyFactory.generatePublic(publicKeySpec);
            return newPublicKey;

        }catch (InvalidKeySpecException e) {
        }catch (NoSuchAlgorithmException e) {
        }
        return null;
    }


    //enctypt and decryption
    public static byte[] encrypt(PrivateKey privateKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);

        return cipher.doFinal(message.getBytes());
    }

    public static byte[] decrypt(PublicKey publicKey, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        return cipher.doFinal(encrypted);
    }

    public static byte[] encrypt(PublicKey publicKey, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(message.getBytes());
    }

    public static byte[] decrypt(PrivateKey privateKey, byte [] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(encrypted);
    }

    public void authenication(){
        try {

            byte[] buf = new byte[162];

            //buf = new byte[256];
            DatagramPacket packet = new DatagramPacket (buf, buf.length);
            socket.receive (packet);
            System.out.println ("Received packet");
            System.out.println(packet.getData());
            target_pubkey = decodePublicKey(packet.getData());
            System.out.println("Public Key received: "+target_pubkey.toString());
            rxArea.append("\n Public key received.");

        }
        catch (IOException e)
        {
            System.out.println("key exchange fail");
        }


    }

    public static void main(String[] args){
        try {
            InventorySender inv = new InventorySender("Inventory Sender");

        inv.pack();
        inv.show();
        inv.rxArea.setText("The current Inventory: "+inv.getInventoryNumber());
        inv.rxArea.append("\nPlease enter target ip and press connect.");
        inv.sendButton.setEnabled(false);
        inv.authenication();
        inv.sendButton.setEnabled(true);
        inv.receive ();
        }
        catch (IOException e)
        {
            System.err.println("Couldn't get I/O for the connection to target ip.");
            System.exit(1);
        }
    }



}

