package cz.cacek.kerberos;

import org.ietf.jgss.GSSException;
import org.ietf.jgss.Oid;

/**
 * Constants for Kerberos demo applications.
 */
public class Krb5Constants {

    /**
     * OID of Kerberos v5 mechanism in GSS-API.
     * {@code iso(1) member-body(2) us(840) mit(113554) infosys(1) gssapi(2) krb5(2)}
     */
    public final static Oid KRB5_OID;

    static {
        try {
            KRB5_OID = new Oid("1.2.840.113554.1.2.2");
        } catch (GSSException e) {
            throw new RuntimeException(e);
        }
    }
}
