package cz.cacek.kerberos.jaas;

import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.LoginContext;

import org.apache.kerby.asn1.Asn1;

/**
 * This demo application shows client authentication in Kerberos by providing name and password. KerberosTicket (TGT) is
 * available in the JAAS Subject after authentication.
 * <p>
 * KDC has to be available/reachable for the authentication.
 */
public class InitiatorAuthenticationMain {

    public static void main(String[] args) throws Exception {
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("java.security.auth.login.config", "jaas.conf");
        System.setProperty("java.security.krb5.conf", "krb5.conf");

        LoginContext lc = new LoginContext("KerberosWithPrompt",
                new NamePasswordCbHandler("jduke@TEST.REALM", "theduke".toCharArray()));
        lc.login();
        Subject subj = lc.getSubject();

        Set<Object> privateCredentials = subj.getPrivateCredentials();
        KerberosTicket kt = (KerberosTicket) privateCredentials.iterator().next();

        Asn1.decodeAndDump(kt.getEncoded());

        System.out.println("Client principal: " + kt.getClient());
        System.out.println("Server principal: " + kt.getServer());
        System.out.println("Principals: " + subj.getPrincipals());
    }
}
