package cz.cacek.kerberos.kdc;

import static java.util.Arrays.asList;
import static org.apache.kerby.kerberos.kerb.server.KdcConfigKey.PREAUTH_REQUIRED;

import java.io.File;
import java.util.Arrays;

import org.apache.kerby.kerberos.kerb.KrbException;
import org.apache.kerby.kerberos.kerb.server.SimpleKdcServer;

/**
 * Simple KDC
 */
public class KerbyServerMain {

    public static void main(String[] args) throws KrbException {
        SimpleKdcServer kdc = new SimpleKdcServer();
        kdc.enableDebug();
        kdc.setKdcHost("localhost");
        kdc.setKdcRealm("TEST.REALM");
        kdc.setKdcPort(10088);
        kdc.setAllowUdp(false);
        kdc.getKdcConfig().setBoolean(PREAUTH_REQUIRED, false);
        kdc.init();

        kdc.createPrincipal("jduke", "theduke");
        kdc.createPrincipal("hnelson", "secret");
        kdc.createPrincipal("gsstest/localhost", "servicePassword");
        kdc.createPrincipal("hazelcast/localhost", "s1ml3+FAST");

        // export service principal's keytab
        File keytabFile = new File("service.keytab");
        if (!keytabFile.exists()) {
            kdc.getKadmin().exportKeytab(keytabFile, 
                    asList("gsstest/localhost@TEST.REALM", "hazelcast/localhost@TEST.REALM"));
        }

        kdc.start();
        System.out.println("Kerberos server has started.");
    }
}
