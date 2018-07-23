/*
 * COPYRIGHT 2015 SEAGATE LLC
 *
 * THIS DRAWING/DOCUMENT, ITS SPECIFICATIONS, AND THE DATA CONTAINED
 * HEREIN, ARE THE EXCLUSIVE PROPERTY OF SEAGATE TECHNOLOGY
 * LIMITED, ISSUED IN STRICT CONFIDENCE AND SHALL NOT, WITHOUT
 * THE PRIOR WRITTEN PERMISSION OF SEAGATE TECHNOLOGY LIMITED,
 * BE REPRODUCED, COPIED, OR DISCLOSED TO A THIRD PARTY, OR
 * USED FOR ANY PURPOSE WHATSOEVER, OR STORED IN A RETRIEVAL SYSTEM
 * EXCEPT AS ALLOWED BY THE TERMS OF SEAGATE LICENSES AND AGREEMENTS.
 *
 * YOU SHOULD HAVE RECEIVED A COPY OF SEAGATE'S LICENSE ALONG WITH
 * THIS RELEASE. IF NOT PLEASE CONTACT A SEAGATE REPRESENTATIVE
 * http://www.seagate.com/contact
 *
 * Original author:  Arjun Hariharan <arjun.hariharan@seagate.com>
 * Original creation date: 23-Oct-2015
 */
package com.seagates3.authserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.Properties;

import com.seagates3.authencryptutil.JKSUtil;
import com.seagates3.authencryptutil.RSAEncryptDecryptUtil;


/**
 * Store the auth server configuration properties like default endpoint and
 * server end points.
 */
public class AuthServerConfig {

    public static String authResourceDir;
    private static String samlMetadataFilePath;
    private static Properties authServerConfig;
    private static String ldapPasswd;

    /**
     * Read the properties file.
     * @throws GeneralSecurityException
     */
    public static void readConfig(String resourceDir)
                       throws FileNotFoundException, IOException,
                                  GeneralSecurityException, Exception {
        authResourceDir = resourceDir;
        Path authProperties = Paths.get(authResourceDir, "authserver.properties");
        Path authSecureProperties = Paths.get(authResourceDir, "keystore.properties");
        Properties authServerConfig = new Properties();
        InputStream input = new FileInputStream(authProperties.toString());
        authServerConfig.load(input);
        Properties authSecureConfig = new Properties();
        InputStream inSecure = new FileInputStream(authSecureProperties.toString());
        authSecureConfig.load(inSecure);
        authServerConfig.putAll(authSecureConfig);
        AuthServerConfig.init(authServerConfig);
    }

    /**
     * Initialize default endpoint and s3 endpoints etc.
     *
     * @param authServerConfig Server configuration parameters.
     * @throws GeneralSecurityException
     */
    public static void init(Properties authServerConfig) throws GeneralSecurityException, Exception {
        AuthServerConfig.authServerConfig = authServerConfig;

        setSamlMetadataFile(authServerConfig.getProperty(
                "samlMetadataFileName"));
        String jvm = ManagementFactory.getRuntimeMXBean().getName();
        AuthServerConfig.authServerConfig.put("pid", jvm.substring(0, jvm.indexOf("@")));

        String encryptedPasswd = authServerConfig.getProperty("ldapLoginPW");
        Path authProperties = getKeyStorePath();
        PrivateKey privateKey = JKSUtil.getPrivateKeyFromJKS(
                                authProperties.toString(), getCertAlias(),
                                getKeyStorePassword());
        if(privateKey == null) {
             throw new GeneralSecurityException("Failed to find Private Key.");
        }
        ldapPasswd = RSAEncryptDecryptUtil.decrypt(encryptedPasswd,
                     privateKey);
    }

    /**
     * @return the process id
     */
    public static int getPid() {
        return Integer.parseInt(authServerConfig.getProperty("pid"));
    }

    /**
     * Return the end points of S3-Auth server.
     *
     * @return server endpoints.
     */
    public static String[] getEndpoints() {
        return authServerConfig.getProperty("s3Endpoints").split(",");
    }

    /**
     * Return the default end point of S3-Auth server.
     *
     * @return default endpoint.
     */
    public static String getDefaultEndpoint() {
        return authServerConfig.getProperty("defaultEndpoint");
    }

    /**
     * @return Path of SAML metadata file.
     */
    public static String getSAMLMetadataFilePath() {
        return samlMetadataFilePath;
    }

    public static int getHttpPort() {
        return Integer.parseInt(authServerConfig.getProperty("httpPort"));
    }

    public static int getHttpsPort() {
        return Integer.parseInt(authServerConfig.getProperty("httpsPort"));
    }

    public static String getKeyStoreName() {
        return authServerConfig.getProperty("s3KeyStoreName");
    }

    public static Path getKeyStorePath() {
        return Paths.get(authResourceDir, getKeyStoreName());
    }

    public static String getKeyStorePassword() {
        return authServerConfig.getProperty("s3KeyStorePassword");
    }

    public static String getKeyPassword() {
        return authServerConfig.getProperty("s3KeyPassword");
    }

    public static boolean isHttpEnabled() {
        return Boolean.valueOf(authServerConfig.getProperty("enable_http"));
    }

    public static boolean isHttpsEnabled() {
        return Boolean.valueOf(authServerConfig.getProperty("enable_https"));
    }

    public static String getDataSource() {
        return authServerConfig.getProperty("dataSource");
    }

    public static String getLdapHost() {
        return authServerConfig.getProperty("ldapHost");
    }

    public static int getLdapPort() {
        return Integer.parseInt(authServerConfig.getProperty("ldapPort"));
    }

    public static int getLdapSSLPort() {
        return Integer.parseInt(authServerConfig.getProperty("ldapSSLPort"));
    }

    public static Boolean isSSLToLdapEnabled() {
        return Boolean.valueOf(authServerConfig.getProperty("enableSSLToLdap"));
    }

    public static int getLdapMaxConnections() {
        return Integer.parseInt(authServerConfig.getProperty("ldapMaxCons"));
    }

    public static int getLdapMaxSharedConnections() {
        return Integer.parseInt(authServerConfig.getProperty("ldapMaxSharedCons"));
    }

    public static String getLdapLoginDN() {
        return authServerConfig.getProperty("ldapLoginDN");
    }

    public static String getLdapLoginCN() {
        String ldapLoginDN = authServerConfig.getProperty("ldapLoginDN");
        String ldapLoginCN = ldapLoginDN.substring(
              ldapLoginDN.indexOf("cn=") + 3, ldapLoginDN.indexOf(','));
        return ldapLoginCN;
    }

    public static String getLdapLoginPassword() {
        return ldapPasswd;
    }

    public static String getCertAlias() {
        return authServerConfig.getProperty("s3AuthCertAlias");
    }

    public static String getConsoleURL() {
        return authServerConfig.getProperty("consoleURL");
    }

    public static String getLogConfigFile() {
        return authServerConfig.getProperty("logConfigFile");
    }

    public static String getLogLevel() {
        return authServerConfig.getProperty("logLevel");
    }

    public static int getBossGroupThreads() {
        return Integer.parseInt(
                authServerConfig.getProperty("nettyBossGroupThreads"));
    }

    public static int getWorkerGroupThreads() {
        return Integer.parseInt(
                authServerConfig.getProperty("nettyWorkerGroupThreads"));
    }

    public static boolean isPerfEnabled() {
        return Boolean.valueOf(authServerConfig.getProperty("perfEnabled"));
    }

    public static String getPerfLogFile() {
        return authServerConfig.getProperty("perfLogFile");
    }

    public static int getEventExecutorThreads() {
        return Integer.parseInt(
                authServerConfig.getProperty("nettyEventExecutorThreads"));
    }

    public static boolean isFaultInjectionEnabled() {
        return Boolean.valueOf(authServerConfig.getProperty("enableFaultInjection"));
    }

    /**
     * Set the SAML Metadata file Path.
     *
     * @param fileName Name of the metadata file.
     */
    private static void setSamlMetadataFile(String fileName) {
        Path filePath = Paths.get("", "resources", "static", fileName);
        samlMetadataFilePath = filePath.toString();
    }

    public static boolean isEnableHttpsToS3() {
       return Boolean.valueOf(authServerConfig.getProperty("enableHttpsToS3"));
    }
}