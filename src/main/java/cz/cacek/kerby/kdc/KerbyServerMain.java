package cz.cacek.kerby.kdc;

import java.io.File;

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
        // kdc.getKdcConfig().setBoolean(org.apache.kerby.kerberos.kerb.server.KdcConfigKey.PREAUTH_REQUIRED, false);
        kdc.init();

        kdc.createPrincipal("jduke", "theduke");
        kdc.createPrincipal("hnelson", "secret");
        kdc.createPrincipal("HTTP/localhost", "SpN-eGo");

        // export service principal's keytab
        File httpKeytabFile = new File("http.keytab");
        if (!httpKeytabFile.exists()) {
            kdc.exportPrincipal("HTTP/localhost", httpKeytabFile);
        }

        kdc.start();
        System.out.println("Kerberos server has started.");
    }
}
