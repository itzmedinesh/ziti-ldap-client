# ziti-ldap-client
LDAP client to connect and operate on directory servers protected by a ziti network


# 1. Ziti Context Initialization
*********************************************************************
a) Enroll the endpoint using the one time jwt enrollment token file downloaded from nfconsole. The enroll method produces a ziti identity as a base64 encoded string containing keystore file
```html
String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbSI6Im90dCIsImV4cCI6MTYxNTcwNzgyOCwiaXNzIjoiaHR0cHM6Ly8yMy4yMi4xMjcuMTI1OjQ0MyIsImp0aSI6IjU4NGFjYjczLWM3OWQtNDcxOC1iNTg3LTY1MTMzODllNTUzYiIsInN1YiI6Ik1wT0lOeEdCQyJ9.P_2LZR21iRAyh0FM992Jh0oqWKwLmj2YILidLDc7je5zFvbvwHhIaShCnDsj2NW1RUA6rV5fW-RMzDebeAbqeC6Ff0P1DMJkK1M8jUaX3Ggcu2nvSzNi5CoA0v1ggR_WHY_E1-yrDxBGfdG31nmVRRdi9CL8yWkK10PfgUYA-AklvgA_aPNPWlyTLFpSLGq-kQ2bWE_kn7u51dKCht8WCatn4UEWf2W8-MhroclSXGhdG0NCTe8H3KWVPrSCvz1mxkIoUVQzn3V1mLrqGzkmbKJucnxj6eCoBFRTJ0CE4UW27dCGQ5w1ncnCB2FsSsBR89ASO242EPhvSfoTb4itPg";

InputStream tokenStream = new ByteArrayInputStream(token.getBytes());

String identity = ZitiApp.enroll(tokenStream);

ZitiContext zitiContext =
new ZitiApp.CredentialBuilder().fromKey(identity).build().getContext();

```

# 2. Initialize and open LDAP connection
*********************************************************************
```html
ZitiLdapConnectionConfig zitiLdapConnectionConfig = new ZitiLdapConnectionConfig.Builder().service("ad ldap tcp - ad.sandbox.internal").bindDn("sandbox\\xxxx").bindPass("xxxxx").build();

ZitiLdapConnection zitiLdapConnection = new ZitiLdapConnection(zitiContext,zitiLdapConnectionConfig);

zitiLdapConnection.open();
```

# 3. Search and filter LDAP Users
*********************************************************************
```html
SearchRequest searchRequest = new SearchRequest();
searchRequest.setBaseDn("OU=sandbox,DC=ad,DC=sandbox,DC=netfoundry,DC=io");
searchRequest.setFilter("(&(objectClass=user))");
searchRequest.setReturnAttributes("sn","givenName", "samAccountName");

org.ldaptive.SearchOperationHandle searchOperationHandle = zitiLdapConnection.operation(searchRequest);
SearchResponse searchResponse = searchOperationHandle.execute();
log.info("Search response status : {}",searchResponse.isSuccess());

searchResponse.getEntries().stream().forEach(ldapEntry -> ldapEntry.getAttributes().stream().forEach(ldapAttribute -> {
    log.info("Attribute Name : {}  Attributte Value : {}",ldapAttribute.getName(),ldapAttribute.getStringValue());
}));

```

# 4. Close LDAP connection
*********************************************************************
```html
zitiLdapConnection.close();
```
