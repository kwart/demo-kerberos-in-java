package cz.cacek.kerberos.jaas;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Simple {@link CallbackHandler} implementation which holds name and password and fills them into {@link NameCallback} and
 * {@link PasswordCallback}.
 */
public class NamePasswordCbHandler implements CallbackHandler {
    private transient String name;
    private transient char[] password;

    public NamePasswordCbHandler(String name, char[] password) {
        this.name = name;
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