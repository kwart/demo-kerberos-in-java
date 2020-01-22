package cz.cacek.kerberos.hazelcast;

import static cz.cacek.kerberos.Krb5Constants.KRB5_OID;

import java.time.LocalTime;
import java.util.Map;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.security.SimpleTokenCredentials;

/**
 * Sample GSS-API/Kerberos authentication in Hazelcast Enterprise.
 * <p>
 * KDC has to be available/reachable for the authentication.
 */
public class HazelcastClientMain {

    public static void main(String[] args) throws GSSException {
        // System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("java.security.auth.login.config", "jaas.conf");
        System.setProperty("java.security.krb5.conf", "krb5.conf");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

        GSSManager manager = GSSManager.getInstance();
        GSSName servicePrincipalName = manager.createName("hazelcast/localhost@TEST.REALM", null);
        GSSContext gssContext = manager.createContext(servicePrincipalName, KRB5_OID, null, GSSContext.DEFAULT_LIFETIME);
        gssContext.requestMutualAuth(false);
        byte[] token = gssContext.initSecContext(new byte[0], 0, 0);
        if (!gssContext.isEstablished()) {
            System.err.println("Multi-step GSS-API context initialization is not supported");
            System.exit(1);
        }

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getSecurityConfig().setCredentials(new SimpleTokenCredentials(token));
        clientConfig.getNetworkConfig().addAddress("localhost");

        HazelcastInstance hz = HazelcastClient.newHazelcastClient(clientConfig);

        Map<String, String> testMap = hz.getMap("test");
        String oldVal = testMap.put("lastRun", LocalTime.now().toString());
        if (oldVal == null) {
            System.out.println("This is the first run of the client application");
        } else {
            System.out.println("Last client application run was at " + oldVal);
        }
        hz.shutdown();
    }

}
