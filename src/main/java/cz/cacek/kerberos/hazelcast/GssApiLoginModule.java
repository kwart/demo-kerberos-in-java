package cz.cacek.kerberos.hazelcast;

import javax.security.auth.callback.Callback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;

import com.hazelcast.security.ClusterLoginModule;
import com.hazelcast.security.CredentialsCallback;
import com.hazelcast.security.TokenCredentials;

public class GssApiLoginModule extends ClusterLoginModule {

    private String name;

    @Override
    public boolean onLogin() throws LoginException {
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
            addRole(name);
        } catch (Exception e) {
            e.printStackTrace();
            new LoginException("Something went wrong during login");
        }
        return true;
    }

    @Override
    protected String getName() {
        return name;
    }

}