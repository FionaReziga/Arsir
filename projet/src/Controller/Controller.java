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
        DatagramSocket ds = new DatagramSocket(69, hostAddress);
        byte[] buf = new byte[3000];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);


        try {

            /**
             * On reçoit les premières infos (nom du fichier etc)
             */
            ds.receive(dp);

            String rcvd = new String(dp.getData(), 0, dp.getLength()) + ", from address: "
                    + dp.getAddress() + ", port: " + dp.getPort();
            System.out.println(rcvd);

            /**
             * On créé un fichier qui nous servira de fichier d'accueil
             */

            File file = new File("C:\\Users\\Epulapp\\Documents\\_Projets\\Arsir\\projet\\Ressources\\outputfile.txt");
            file.createNewFile();
            FileOutputStream fis = new FileOutputStream(file);

            /**
             * Acknoledgement à pumpkin
             */

            byte[] b = ("Acknowledged").getBytes();
            InetAddress ha = InetAddress.getByName("127.0.0.1");
            DatagramSocket ds1 = new DatagramSocket(dp.getPort(), ha);
            System.out.println("Sending back acknowledge to client");
            ds1.send(new DatagramPacket(b,b.length,dp.getAddress(),dp.getPort()));


            /**
             * index pour savoir combien de parties du fichier on récupère
             */
            int index=0;
            while (index != 2){
                System.out.println("Récupération partie "+index + " du fichier.");
                byte[] buffer = new byte[512];
                DatagramPacket dp1 = new DatagramPacket(buffer, buffer.length);

                ds1.receive(dp1);

                String receive = new String(dp1.getData(), 0, dp1.getLength()) + ", from address: "
                        + dp1.getAddress() + ", port: " + dp1.getPort();
                System.out.println(receive);

                index++;
            }

            /*System.out.println("Total file size to read (in bytes) : "
                    + fis.available());

            int content;
            while ((content = fis.read()) != -1) {
                // convert to char and display it
                System.out.print((char) content);
            }*/

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
