package cz.cacek.kerberos.kdc;

import static java.util.Arrays.asList;
import static org.apache.kerby.kerberos.kerb.server.KdcConfigKey.PREAUTH_REQUIRED;

import java.io.File;

import org.apache.kerby.kerberos.kerb.KrbException;
import org.apache.kerby.kerberos.kerb.server.SimpleKdcServer;

/**
 * Simple KDC which utilizes Apache Kerby.
 */
public class KerbyServerMain {

    public static void main(String[] args) throws KrbException {
        SimpleKdcServer kdc = new SimpleKdcServer();
        // kdc.enableDebug();
        kdc.setKdcHost("localhost");
        kdc.setKdcRealm("TEST.REALM");
        kdc.setKdcPort(10088);
        kdc.setAllowUdp(false);
        kdc.getKdcConfig().setBoolean(PREAUTH_REQUIRED, false);
        // SimpleKdcServer init also creates krb5.conf file and initializes Kadmin API.
        kdc.init();

        kdc.createPrincipal("jduke", "theduke");
        kdc.createPrincipal("hnelson", "secret");
        kdc.createPrincipal("gsstest/localhost", "servicePassword");
        kdc.createPrincipal("hazelcast/localhost", "s1mpl3+FAST");

        // export service principal's keytab
        File keytabFile = new File("service.keytab");
        if (!keytabFile.exists()) {
            kdc.getKadmin().exportKeytab(keytabFile, asList("gsstest/localhost@TEST.REALM", "hazelcast/localhost@TEST.REALM"));
        }

        kdc.start();
        System.out.println("Kerberos server has started.");
    }
}
