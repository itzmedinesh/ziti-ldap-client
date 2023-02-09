package org.microcrafts.openzitildap;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.ldaptive.LdapException;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.openziti.ZitiContext;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Unit test for Ziti App
 */
public class ZitiAppTest {

    static Logger logger;

    @BeforeAll
    public static void setUp() {
        logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.TRACE);
    }

    public void tstGetContext() {
        String credJson =
            "{\"ztAPI\":\"https://10.0.0.1:443\",\"id\":{\"key\":\"pem:-----BEGIN EC PRIVATE KEY-----\\nMIGvJiQISo=\\n-----END EC PRIVATE KEY-----\\n\",\"cert\":\"pem:-----BEGIN CERTIFICATE-----\\nMIID3jZEFQ==\\n-----END CERTIFICATE-----\\n\",\"ca\":\"pem:-----BEGIN CERTIFICATE-----\\nMIIFvNd+oI=\\n-----END CERTIFICATE-----\\n\"},\"configTypes\":null}";
        try {
            ZitiApp zitiApp = new ZitiApp.CredentialBuilder().fromJson(credJson).build();
            Assertions.assertNotNull(zitiApp);
            Assertions.fail("Not expected to pass");
        } catch (Exception e) {
            Assertions.assertTrue(e.getMessage().contains("unsupported format"));
        }
    }

    @Test
    public void tstEnrollConnectActiveDS() throws LdapException {

        String token = "";
        InputStream tokenStream = new ByteArrayInputStream(token.getBytes());
        //String identity = ZitiApp.enroll(tokenStream);

        String identity = "";

        ZitiContext zitiContext =
            new ZitiApp.CredentialBuilder().fromKey(identity).build().getContext();

        ZitiLdapConnectionConfig zitiLdapConnectionConfig =
            new ZitiLdapConnectionConfig.Builder().service("ad.ziti.netfoundry.io")
                .bindDn("sandbox\\xxxx").bindPass("xxxxxxx").build();
        ZitiLdapConnection
            zitiLdapConnection = new ZitiLdapConnection(zitiContext, zitiLdapConnectionConfig);
        zitiLdapConnection.open();

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setBaseDn("OU=sandbox,DC=ad,DC=sandbox,DC=netfoundry,DC=io");
        searchRequest.setFilter("(&(objectClass=user))");
        searchRequest.setReturnAttributes("sn", "givenName", "samAccountName", "cn", "displayName");

        org.ldaptive.SearchOperationHandle searchOperationHandle =
            zitiLdapConnection.operation(searchRequest);
        SearchResponse searchResponse = searchOperationHandle.execute();

        Assertions.assertTrue(searchResponse.isSuccess());
        searchResponse.getEntries().forEach(ldapEntry -> ldapEntry.getAttributes().forEach(
            ldapAttribute -> logger.info("Attribute Name : {}  Attribute Value : {}",
                ldapAttribute.getName(), ldapAttribute.getStringValue())));

        zitiLdapConnection.close();
    }


    @Test
    public void tstEnrollConnectApacheDS() throws LdapException {

        String token = "";
        InputStream tokenStream = new ByteArrayInputStream(token.getBytes());
        //String identity = ZitiApp.enroll(tokenStream);

        String identity = "";

        ZitiContext zitiContext =
            new ZitiApp.CredentialBuilder().fromKey(identity).build().getContext();

        ZitiLdapConnectionConfig zitiLdapConnectionConfig =
            new ZitiLdapConnectionConfig.Builder().service("apachedir.ziti.netfoundry.io")
                .bindDn("uid=admin,ou=system").bindPass("xxxxx").build();
        ZitiLdapConnection zitiLdapConnection =
            new ZitiLdapConnection(zitiContext, zitiLdapConnectionConfig);
        zitiLdapConnection.open();

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setBaseDn("ou=nfusers,ou=system");
        searchRequest.setFilter("(&(objectClass=account))");
        searchRequest.setReturnAttributes("uid");

        org.ldaptive.SearchOperationHandle searchOperationHandle =
            zitiLdapConnection.operation(searchRequest);
        SearchResponse searchResponse = searchOperationHandle.execute();

        Assertions.assertTrue(searchResponse.isSuccess());

        searchResponse.getEntries().forEach(ldapEntry -> ldapEntry.getAttributes().forEach(
            ldapAttribute -> logger.info("Attribute Name : {}  Attribute Value : {}",
                ldapAttribute.getName(), ldapAttribute.getStringValue())));

        zitiLdapConnection.close();
    }

}
