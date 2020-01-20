package cz.cacek.kerby.jaas;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;

/**
 * Authentication which uses Krb5LoginModule in non-interactive mode. Keytab and principal options are defined in jaas.conf.
 * KDC doesn't need to be available/reachable for the acceptor's authentication.
 */
public class AcceptorAuthenticationMain {

    public static void main(String[] args) throws Exception {
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("java.security.auth.login.config", "jaas.conf");
        System.setProperty("java.security.krb5.conf", "krb5.conf");

        LoginContext lc = new LoginContext("KerberosAcceptorWithKeytab");
        lc.login();
        Subject subj = lc.getSubject();

        System.out.println("Principals: " + subj.getPrincipals());
        System.out.println("PrivateCredentials: " + subj.getPrivateCredentials());
    }
}
