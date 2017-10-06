package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

import java.io.*;
import java.net.*;

public class Controller {

    @FXML
    Button buttonReceive;

    public Controller(){

    }

    @FXML
    public void initialize() {

    }

    @FXML
    public void handleLaunchReceive() throws SocketException, UnknownHostException {
        System.out.println("Hello");
        InetAddress hostAddress = InetAddress.getByName("127.0.0.1");
        DatagramSocket ds = new DatagramSocket(51999, hostAddress);
        byte[] buf = new byte[3000];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);


        try {
            ds.receive(dp);

            File file = new File("C:/robots.txt");
            FileInputStream fis = null;

            String rcvd = new String(dp.getData(), 0, dp.getLength()) + ", from address: "
                    + dp.getAddress() + ", port: " + dp.getPort();
            System.out.println(rcvd);

            /*System.out.println("Hello");
            String rcvd = new String(dp.getData(), 0, dp.getLength()) + ", from address: "
                    + dp.getAddress() + ", port: " + dp.getPort();
            System.out.println(rcvd);
            while (true) {
                BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
                String outMessage = stdin.readLine();

                if (outMessage.equals("bye"))
                    break;
                String outString = "Client say: " + outMessage;
                buf = outString.getBytes();

                DatagramPacket out = new DatagramPacket(buf, buf.length, hostAddress, 9999);
                ds.send(out);

                ds.receive(dp);
                String rcvd = "rcvd from " + dp.getAddress() + ", " + dp.getPort() + ": "
                        + new String(dp.getData(), 0, dp.getLength());
                System.out.println(rcvd);
            }*/

        } catch (IOException e) {
            System.out.println("Exception : "+e);
            e.printStackTrace();
        }
    }
}
