import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class Server {

    public static class Post {
        private final String sender;
        private final String message;
        private final String timeStamp;

        public Post(String sender, String message, String timeStamp) {
            this.sender = sender;
            this.message = message;
            this.timeStamp = timeStamp;
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final PrintWriter os;
        private final BufferedReader is;

        private final List<Post> posts;


        public ClientHandler(Socket socket)
                throws IOException {
            this.socket = socket;
            this.os = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream()));
            this.is = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
            posts = new ArrayList<>();
        }

        @Override
        public void run() {
            try {
                if (posts.size() > 0) {
                    os.print(posts);
                }
                String addPost = is.readLine();

                if (addPost.compareToIgnoreCase("true") == 0) {
                    String userId = is.readLine();
                    String message = is.readLine();
                    String timestamp = is.readLine();
                    String signature = is.readLine();
                    String signaturePayload = userId + message + timestamp;
                    if (verifySignature(userId, Base64.getDecoder().decode(signature), signaturePayload.getBytes())) {
                        Post post = new Post(userId, message, timestamp);
                        posts.add(post);
                        System.out.println("Post Accepted");
                    } else {
                        System.out.println("Post Rejected");
                    }

                    System.out.println("userId: " + userId);
                    System.out.println("Message: " + message);
                    System.out.println("Timestamp: " + timestamp);
                } else {
                    System.out.println("No post to add");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("Client terminated.");
                try {
                    socket.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }

        private Boolean verifySignature(String clientId, byte[] signature, byte[] signaturePayload) throws NoSuchAlgorithmException, IOException, ClassNotFoundException, InvalidKeyException, SignatureException {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(getPublicKeyFromFile(clientId));
            sign.update(signaturePayload);
            return sign.verify(signature);
        }

        private PublicKey getPublicKeyFromFile(String clientId) throws IOException, ClassNotFoundException {
            ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(clientId + ".pub"));
            return (PublicKey) objectInputStream.readObject();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Port is required");
            return;
        }

        int port = Integer.parseInt(args[0]);
        ServerSocket ss = new ServerSocket(port);
        for (Socket socket = ss.accept(); socket != null; socket = ss.accept()) {
            Runnable handler = new ClientHandler(socket);
            new Thread(handler).start();
        }
    }
}
