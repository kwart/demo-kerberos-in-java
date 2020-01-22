package cz.cacek.kerberos.hazelcast;

import java.security.Principal;
import java.util.Iterator;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;

import com.hazelcast.security.ClusterIdentityPrincipal;
import com.hazelcast.security.ClusterRolePrincipal;
import com.hazelcast.security.CredentialsCallback;
import com.hazelcast.security.HazelcastPrincipal;
import com.hazelcast.security.TokenCredentials;

/**
 * Sample JAAS {@link LoginModule} implementation for Hazelcast. It accepts Kerberos GSS-API tokens.
 */
public class GssApiLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private String name;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.name = null;
    }

    @Override
    public boolean login() throws LoginException {
        try {
            CredentialsCallback cc = new CredentialsCallback();
            callbackHandler.handle(new Callback[] { cc });
            TokenCredentials creds = (TokenCredentials) cc.getCredentials();

            byte[] token = creds.getToken();

            GSSContext gssContext = GSSManager.getInstance().createContext((GSSCredential) null);
            token = gssContext.acceptSecContext(token, 0, token.length);

            if (!gssContext.isEstablished()) {
                throw new FailedLoginException("Multi-step negotiation is not supported by this login module");
            }
            name = gssContext.getSrcName().toString();
        } catch (Exception e) {
            e.printStackTrace();
            new LoginException("Something went wrong during login");
        }
        return true;
    }

    /**
     * Store identity and role principals into the JAAS {@link Subject} when authentication is successful. Identity name is name
     * from Kerberos ticket and role name is always {@code "kerberos"}.
     */
    @Override
    public boolean commit() throws LoginException {
        if (name == null) {
            throw new LoginException("No name available.");
        }
        subject.getPrincipals().add(new ClusterIdentityPrincipal(name));
        subject.getPrincipals().add(new ClusterRolePrincipal("kerberos"));
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return logout();
    }

    /**
     * Cleanup Hazelcast principals from the JAAS {@link Subject}.
     */
    @Override
    public boolean logout() throws LoginException {
        for (Iterator<Principal> it = subject.getPrincipals().iterator(); it.hasNext();) {
            Principal p = it.next();
            if (p instanceof HazelcastPrincipal) {
                it.remove();
            }
        }
        return true;
    }

}