package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

import static java.lang.String.format;
import static javafx.scene.control.Alert.AlertType.ERROR;

public class Controller {

    private static String SRV_ADDR = "";
    private static int SRV_PORT = 0;

    private static final byte ZERO_BYTE = 0;

    private static final byte CODE_READ = 1;
    private static final byte CODE_DATA = 3;
    private static final byte CODE_ACK = 4;
    private static final byte CODE_ERROR = 5;

    private static final String MODE = "octet";

    private static final int BUFFER_SIZE = 516;

    private static String FILE_INPUT_NAME = "";
    private static String FILE_OUTPUT_NAME = "";

    private static boolean hasError = false;

    @FXML
    Button btnReceiveFile;
    @FXML
    TextField txtIP, txtPort, txtFileInputName;

    @FXML
    public void handleLaunchReceive() throws SocketException, UnknownHostException {
        try {
            String msg = verifyInformation();
            if (msg.equals("")) {
                SRV_ADDR = txtIP.getText();
                SRV_PORT = Integer.valueOf(txtPort.getText());
                FILE_INPUT_NAME = txtFileInputName.getText();
                String[] fileOutputName = txtFileInputName.getText().split("/");
                FILE_OUTPUT_NAME = fileOutputName[fileOutputName.length - 1];

                InetAddress hostAddress = InetAddress.getByName(SRV_ADDR);
                DatagramSocket ds = new DatagramSocket();

                // Demande du fichier au serveur
                sendRequest(hostAddress, ds, createReadRequest(), SRV_PORT);
                System.out.println(format("Demannde du fichier %s au serveur %s sur le port %d", FILE_INPUT_NAME, SRV_ADDR, SRV_PORT));

                // Lecture de la réponse du serveur
                readResponse(hostAddress, ds);

                // Fermeture de la connexion
                ds.close();

                if (!hasError) {
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Le fichier a bien été récupéré.");
                    a.show();
                }
            } else {
                Alert a = new Alert(ERROR, msg);
                a.show();
            }
        } catch (IOException e) {
            System.out.println("Exception : " + e);
        }
    }

    private String verifyInformation() {
        String msg = "";

        if (txtIP.getText().equals("")) msg += "Veuillez indiquer l'adresse IP du serveur.\n";
        if (txtPort.getText().equals("")) msg += "Veuillez indiquer le port du serveur.\n";
        try {
            Integer.valueOf(txtPort.getText());
        } catch (NumberFormatException e) {
            msg += "Veuillez indiquer un numéro de port valide.\n";
        }
        if (txtFileInputName.getText().equals("")) msg += "Veuillez indiquer le nom et l'extension du fichier à récupérer.\n";
        if (!txtFileInputName.getText().matches("\\*.*"))
            msg += "Veuillez indiquer un nom de fichier avec une extension valide.\n";
        return msg;
    }

    private byte[] createReadRequest() {
        byte[] bufSend = new byte[2 + FILE_INPUT_NAME.length() + 1 + MODE.length() + 1];
        // Code
        bufSend[0] = ZERO_BYTE;
        bufSend[1] = CODE_READ;
        // Filename
        int i;
        for (i = 0; i < FILE_INPUT_NAME.length(); i++) {
            bufSend[i + 2] = (byte) FILE_INPUT_NAME.charAt(i);
        }
        // Byte null
        bufSend[i + 2] = ZERO_BYTE;
        // Mode
        for (int j = 0; j < MODE.length(); j++) {
            bufSend[i + 3] = (byte) MODE.charAt(j);
            i++;
        }
        // Byte null
        bufSend[i + 3] = ZERO_BYTE;

        return bufSend;
    }

    private byte[] createACKRequest(byte[] block) {
        byte[] bufSend = new byte[4];
        // Code
        bufSend[0] = ZERO_BYTE;
        bufSend[1] = CODE_ACK;
        // Block
        bufSend[2] = block[0];
        bufSend[3] = block[1];

        return bufSend;
    }

    private void sendRequest(InetAddress hostAddress, DatagramSocket ds, byte[] buffer, int port) {
        try {
            DatagramPacket dpSend = new DatagramPacket(buffer, buffer.length, hostAddress, port);
            ds.send(dpSend);
        } catch (IOException e) {
            System.out.println("Exception : " + e);
        }
    }

    private void readResponse(InetAddress hostAddress, DatagramSocket ds) {
        try {
            File file = new File(format(".\\Ressources\\%s", FILE_OUTPUT_NAME));
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);

            boolean again = readData(hostAddress, ds, fos, file);
            while (again) {
                again = readData(hostAddress, ds, fos, file);
            }

            fos.close();

        } catch (IOException e) {
            System.out.println("Exception : " + e);
        }
    }

    private boolean readData(InetAddress hostAddress, DatagramSocket ds, FileOutputStream fos, File file) {
        boolean again = true;

        try {
            byte[] bufReceive = new byte[BUFFER_SIZE];
            DatagramPacket dpReceive = new DatagramPacket(bufReceive, bufReceive.length);
            ds.receive(dpReceive);

            if (dpReceive.getLength() < 516) again = false;

            byte code;
            byte[] block = new byte[2];
            code = dpReceive.getData()[1];
            block[0] = dpReceive.getData()[2];
            block[1] = dpReceive.getData()[3];

            if (code == CODE_DATA) {
                fos.write(dpReceive.getData(), 4, dpReceive.getLength() - 4);

                sendRequest(hostAddress, ds, createACKRequest(block), dpReceive.getPort());
            } else if (code == CODE_ERROR) {
                again = false;
                handleError(dpReceive.getData(), fos, file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return again;
    }

    private void handleError(byte[] errorData, FileOutputStream fos, File file) {
        try {
            byte errorCode = errorData[3];
            String msg = "Une erreur est survenue.";

            hasError = true;

            fos.close();
            file.delete();

            switch (errorCode) {
                case 1:
                    msg = "Fichier non trouvé.";
                    break;
                case 2:
                    msg = "Violation de l'accès";
                    break;
                case 3:
                    msg = "Disque plein.";
                    break;
                case 4:
                    msg = "Opération illégale.";
                    break;
                case 5:
                    msg = "Transfert ID inconnu.";
                    break;
                case 6:
                    msg = "Le fichier existe déjà.";
                    break;
                case 7:
                    msg = "Utilisateur inconnu.";
                    break;
            }

            Alert a = new Alert(ERROR, msg);
            a.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
