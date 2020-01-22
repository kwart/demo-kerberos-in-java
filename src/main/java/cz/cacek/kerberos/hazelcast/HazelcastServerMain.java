package cz.cacek.kerberos.hazelcast;

import java.security.PrivilegedAction;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.LoginModuleConfig;
import com.hazelcast.config.LoginModuleConfig.LoginModuleUsage;
import com.hazelcast.config.PermissionConfig;
import com.hazelcast.config.PermissionConfig.PermissionType;
import com.hazelcast.config.security.JaasAuthenticationConfig;
import com.hazelcast.config.security.RealmConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class HazelcastServerMain {

    public static void main(String[] args) throws LoginException {
        System.setProperty("java.security.auth.login.config", "jaas.conf");
        System.setProperty("java.security.krb5.conf", "krb5.conf");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

        // Create config and set the evaluation license key
        Config config = new Config().setLicenseKey(
                "ENTERPRISE_2020#10Nodes#ig8luOAwUDZqPY9Gf5ETQCKkHNXn6jyb20WBJmd1SM21910200091000100001102000111001000101012290");

        // use TCP/IP cluster members discovery instead of the default UDP Multicast
        JoinConfig joinConfig = config.getNetworkConfig().getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(true).addMember("localhost");

        // Configure Kerberos authentication for clients
        JaasAuthenticationConfig jaasAuthenticationConfig = new JaasAuthenticationConfig().addLoginModuleConfig(
                new LoginModuleConfig(GssApiLoginModule.class.getName(), LoginModuleUsage.REQUIRED));
        config.getSecurityConfig()
            .setEnabled(true)
            .addClientPermissionConfig(new PermissionConfig(PermissionType.ALL, "*", null))
            .setClientRealmConfig("kerberos",
                new RealmConfig().setJaasAuthenticationConfig(jaasAuthenticationConfig));

        LoginContext lc = new LoginContext("HazelcastMember");
        lc.login();
        Subject.doAs(lc.getSubject(), (PrivilegedAction<HazelcastInstance>) () -> Hazelcast.newHazelcastInstance(config));
    }

}
