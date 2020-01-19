package cz.cacek.test;

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
        kdc.init();

        kdc.createPrincipal("jduke", "theduke");
        kdc.createPrincipal("hnelson", "secret");
        kdc.createAndExportPrincipals(new File("/tmp/http.keytab"), "HTTP/localhost");

        kdc.start();
        System.out.println("Kerberos server has started.");
    }
}
