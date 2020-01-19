package cz.cacek.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosTicket;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.kerby.kerberos.kerb.KrbException;
import org.junit.Test;

/**
 * A test template.
 */
public class KerbyServerMainTest {

    @Test
    public void testNullToMain() throws KrbException, LoginException {
        KerbyServerMain.main(null);

        Configuration.setConfiguration(new Configuration() {
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                Map<String, String> options = new HashMap<>();
                options.put("debug", "true");
                return new AppConfigurationEntry[] { new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                        LoginModuleControlFlag.REQUIRED, options) };
            }
        });
        LoginContext lc = new LoginContext("kerberos", new NamePasswordCbHandler("jduke@TEST.REALM", "theduke".toCharArray()));
        lc.login();
        Subject subj = lc.getSubject();
        Set<Object> privateCredentials = subj.getPrivateCredentials();
        assertEquals(1, privateCredentials.size());
        KerberosTicket kt = (KerberosTicket) privateCredentials.iterator().next();

        System.out.println(kt.getClient());
        System.out.println(kt.getServer());
        System.out.println(subj.getPrincipals());
    }

}
