package cz.cacek.kerberos.kdc;

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
        kdc.getKdcConfig().setBoolean(PREAUTH_REQUIRED, false);
        kdc.init();

        kdc.createPrincipal("jduke", "theduke");
        kdc.createPrincipal("hnelson", "secret");
        kdc.createPrincipal("HTTP/localhost", "SpN-eGo");
        kdc.createPrincipal("gsstest/localhost", "servicePassword");

        // export service principal's keytab
        File keytabFile = new File("service.keytab");
        if (!keytabFile.exists()) {
            kdc.getKadmin().exportKeytab(keytabFile, 
                    Arrays.asList("HTTP/localhost@TEST.REALM", "gsstest/localhost@TEST.REALM"));
        }

        kdc.start();
        System.out.println("Kerberos server has started.");
    }
}
