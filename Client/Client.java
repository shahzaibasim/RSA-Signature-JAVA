
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public class Client {
    private final String hostName;
    private final String userId;
    private final int port;

    public Client(String userId, int port, String hostName) {
        this.userId = userId;
        this.port = port;
        this.hostName = hostName;
    }

    private byte[] getEncodedAndEncryptedMessage(String message) throws BadPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, ClassNotFoundException, NoSuchPaddingException, InvalidKeyException, IOException {
        return Base64.getEncoder().encode(encrypt(message));
    }

    public void execute() {
        try {
            Socket socket = new Socket(hostName, port);

            System.out.println("Connected to the server");
            String message = "This is post message";
            boolean addPost = true;
            if(addPost) {
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);
                byte[] encodedMessage = getEncodedAndEncryptedMessage(message);
                Date date = new Date();
                byte[] signature = createSignature((userId + new String(encodedMessage) + date.toString()).getBytes());
                String encodedSignature = Base64.getEncoder().encodeToString(signature);
                writer.println(addPost);
                writer.println(userId);
                writer.println(new String(encodedMessage));
                writer.println(date.toString());
                writer.println(encodedSignature);
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private PublicKey getPublicKeyFromFile() throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(userId + ".pub"));
        return (PublicKey) objectInputStream.readObject();
    }

    private PrivateKey getPrivateKeyFromFile() throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(userId + ".prv"));
        return (PrivateKey) objectInputStream.readObject();
    }

    private byte[] createSignature(byte[] data) {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initSign(getPrivateKeyFromFile());
            sign.update(data);
            return sign.sign();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private byte[] encrypt(String data) throws BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, IOException, ClassNotFoundException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/NOPADDING");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromFile());
        return cipher.doFinal(data.getBytes());
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("HostName, Port and User id is required");
            return;
        }

        String hostName = args[0];
        int port = Integer.parseInt(args[1]);
        String userId = args[2];

        Client client = new Client(userId, port, hostName);
        client.execute();
    }
}
