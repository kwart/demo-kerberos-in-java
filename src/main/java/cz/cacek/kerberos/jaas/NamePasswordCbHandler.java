package cz.cacek.kerberos.jaas;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

public class NamePasswordCbHandler implements CallbackHandler {
    private transient String name;
    private transient char[] password;

    public NamePasswordCbHandler(String username, char[] password) {
        this.name = username;
        this.password = password;
    }

    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
        for (Callback cb : callbacks) {
            if (cb instanceof NameCallback) {
                NameCallback nc = (NameCallback) cb;
                nc.setName(name);
            } else if (cb instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) cb;
                pc.setPassword(password);
            } else {
                throw new UnsupportedCallbackException(cb);
            }
        }
    }
}