package cz.cacek.kerberos.jgss;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.MessageProp;

/**
 * Sample echo server with GSS-API protection.
 */
public class GSSTestServer {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.security.auth.debug", "gssloginconfig");
//        System.setProperty("sun.security.krb5.debug", "true");
//        System.setProperty("sun.security.jgss.debug", "true");

        System.setProperty("java.security.auth.login.config", "jaas.conf");
        System.setProperty("java.security.krb5.conf", "krb5.conf");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        try (ServerSocket serverSocket = new ServerSocket(10089)) {
            System.out.println("GSSTestServer started");
            while (true) {
                Socket acceptedSocket = serverSocket.accept();
                executorService.execute(new ClientConnectionHandler(acceptedSocket));
            }
        }
    }

    private static class ClientConnectionHandler implements Runnable {

        private final Socket acceptedSocket;

        public ClientConnectionHandler(Socket socket) {
            this.acceptedSocket = socket;
        }

        @Override
        public void run() {
            GSSContext gssContext = null;
            try (Socket socket = acceptedSocket) {
                gssContext = GSSManager.getInstance().createContext((GSSCredential) null);
                System.out.println("Client connected");
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                System.out.println("Initializing GSS context");
                while (!gssContext.isEstablished()) {
                    byte[] inToken = new byte[dataInputStream.readInt()];
                    dataInputStream.readFully(inToken);
                    // Files.write(Paths.get("/tmp/init.token"), inToken);
                    byte[] outToken = gssContext.acceptSecContext(inToken, 0, inToken.length);

                    if (outToken != null) {
                        dataOutputStream.writeInt(outToken.length);
                        dataOutputStream.write(outToken);
                        dataOutputStream.flush();
                    }
                }
                String clientName = gssContext.getSrcName().toString();
                System.out.println("Context Established with Client " + clientName);

                byte[] wrappedMsg = new byte[dataInputStream.readInt()];
                dataInputStream.readFully(wrappedMsg);
                // initial values in the MessageProp are ignored
                MessageProp msgProp = new MessageProp(0, false);
                String message = new String(gssContext.unwrap(wrappedMsg, 0, wrappedMsg.length, msgProp), UTF_8);
                System.out.println("Message: " + message);
                System.out.println("Message privacy used: " + msgProp.getPrivacy());

                String replyMsg = message + ", " + message;
                byte[] replyMsgBytes = replyMsg.getBytes(UTF_8);
                wrappedMsg = gssContext.wrap(replyMsgBytes, 0, replyMsgBytes.length, msgProp);

                dataOutputStream.writeInt(wrappedMsg.length);
                dataOutputStream.write(wrappedMsg);
                dataOutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (gssContext != null) {
                    try {
                        gssContext.dispose();
                    } catch (GSSException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}