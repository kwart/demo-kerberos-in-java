package cz.cacek.kerberos.jgss;

import static cz.cacek.kerberos.Krb5Constants.KRB5_OID;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.MessageProp;

import cz.cacek.kerberos.jaas.NamePasswordCbHandler;

/**
 * A client for {@link GSSTestServer}.
 */
public class GSSTestClient implements PrivilegedExceptionAction<String> {

    public static void main(String[] args) throws Exception {
        // System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("java.security.auth.login.config", "jaas.conf");
        System.setProperty("java.security.krb5.conf", "krb5.conf");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

        LoginContext lc = new LoginContext("KerberosWithPrompt",
                new NamePasswordCbHandler("jduke@TEST.REALM", "theduke".toCharArray()));

        lc.login();

        Subject subject = lc.getSubject();
        String response = Subject.doAs(subject, new GSSTestClient());
        System.out.println("Response from GSSTestServer: " + response);
    }

    @Override
    public String run() throws Exception {
        GSSContext gssContext = null;
        try (Socket socket = new Socket("localhost", 10089)) {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            GSSManager manager = GSSManager.getInstance();
            GSSName serviceName = manager.createName("gsstest/localhost@TEST.REALM", null);
            gssContext = manager.createContext(serviceName, KRB5_OID, null, GSSContext.DEFAULT_LIFETIME);

            gssContext.requestMutualAuth(true);
            gssContext.requestConf(false);
            gssContext.requestInteg(false);
            // gssContext.requestCredDeleg(true);
            // gssContext.requestMutualAuth(false);
            // gssContext.requestConf(false);
            // gssContext.requestInteg(false);

            byte[] token = new byte[0];
            while (!gssContext.isEstablished()) {
                token = gssContext.initSecContext(token, 0, token.length);
                if (token != null) {
                    dos.writeInt(token.length);
                    dos.write(token);
                    dos.flush();
                }
                if (!gssContext.isEstablished()) {
                    token = new byte[dis.readInt()];
                    dis.readFully(token);
                }
            }
            String requestMsg = "Hello";
            byte[] requestMsgBytes = requestMsg.getBytes(UTF_8);
            MessageProp msgProp = new MessageProp(true);
            token = gssContext.wrap(requestMsgBytes, 0, requestMsgBytes.length, msgProp);
            System.out.println("Message privacy used for sending: " + msgProp.getPrivacy());

            dos.writeInt(token.length);
            dos.write(token);
            dos.flush();

            token = new byte[dis.readInt()];
            dis.readFully(token);
            msgProp = new MessageProp(false);
            byte[] replyMsgBytes = gssContext.unwrap(token, 0, token.length, msgProp);
            System.out.println("Message privacy used for received reply: " + msgProp.getPrivacy());
            return new String(replyMsgBytes, UTF_8);
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