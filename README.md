# Kerberos in Java - demo

This project contains several small applications which demonstrates using Kerberos in Java.

The project has following parts:
* KDC server based on Apache Kerby;
* JAAS authentication - using `Krb5LoginModule`;
* GSS-API/Kerberos authentication in Hazelcast Enterprise;
* GSS-API/Kerberos Client/Server application with message protection.

## Simple KDC

Package: `cz.cacek.kerberos.kdc`

[Apache Kerby](https://directory.apache.org/kerby/) allows to simply configure and run embedded KDC.

See [KerbyServerMain.java](src/main/java/cz/cacek/kerberos/kdc/KerbyServerMain.java). When it's launched it regenerates [krb5.conf](krb5.conf) file and also creates the `service.keytab` if necessary. 

## JAAS authentication - Krb5LoginModule

Package: `cz.cacek.kerberos.jaas`

Simple applications [InitiatorAuthenticationMain](src/main/java/cz/cacek/kerberos/jaas/InitiatorAuthenticationMain.java) [AcceptorAuthenticationMain](src/main/java/cz/cacek/kerberos/jaas/AcceptorAuthenticationMain.java) shows how to work with Oracle/OpenJDK `Krb5LoginModule` implementation (`com.sun.security.auth.module.Krb5LoginModule`).

The JAAS login configuration is defined in [jaas.conf](jaas.conf) file.

The `InitiatorAuthenticationMain` needs KDC server running as it asks for TGT from it. On the other hand, the `AcceptorAuthenticationMain` initializes secrets from the keytab and it doesn't communicate to the KDC.

## GSS-API authentication in Hazelcast Enterprise

Package: `cz.cacek.kerberos.hazelcast`

This demo shows how to simply use standard API and plug-in Kerberos Single-sign-on.

[Hazelcast IMDG](https://hazelcast.org/) (in-memory data grid) doesn't support Kerberos authentication in 4.0-BETA-2 version, but it does support JAAS authentication in its Enterprise edition. 

The demo introduces a simple login module [GssApiLoginModule](src/main/java/cz/cacek/kerberos/hazelcast/GssApiLoginModule.java) which uses the GSS-API to accept Kerberos tokens.

The new login module is configured on Hazelcast servers (members) to authenticate client connections. See [HazelcastServerMain](src/main/java/cz/cacek/kerberos/hazelcast/HazelcastServerMain.java).

Clients need to configure valid GSS-API/Kerberos token to authenticate the connection into the Hazelcast cluster. See [HazelcastClientMain](src/main/java/cz/cacek/kerberos/hazelcast/HazelcastClientMain.java).

The JAAS login configuration for this demo is also defined in the [jaas.conf](jaas.conf) file.

The KDC has to be running to test this demo. 

## GSS-API/Kerberos Client/Server echo application with message protection

Package: `cz.cacek.kerberos.jgss`

Client/server demo application, which is able to provide one-way or mutual GSS-API/Kerberos authentication and message encryption.
See [GSSTestServer](src/main/java/cz/cacek/kerberos/jgss/GSSTestServer.java) and [GSSTestServer](src/main/java/cz/cacek/kerberos/jgss/GSSTestClient.java).

The client needs the KDC running.