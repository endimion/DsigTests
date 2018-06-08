/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aegean.dsig.test.dsigTest.service.impl;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import gr.aegean.dsig.test.dsigTest.service.KeystoreService;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author nikos
 */
@Service
public class KeystoreServiceImpl implements KeystoreService {

//    private static final String KEYSTORE_TYPE = "PKCS12";
//    private static final String KEYSTORE_FILEPATH = "target/keystore.p12";
    private static final String KEYSTORE_PASSWORD = "password";

    private final static Logger log = LoggerFactory.getLogger(KeystoreService.class);

    @Override
    public DSSPrivateKeyEntry getPrivateKey() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String path = classLoader.getResource("testSources/keystore.pkcs12").getPath();
            Pkcs12SignatureToken signingToken = new Pkcs12SignatureToken(path, new KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray()));
            return signingToken.getKeys().get(0);
        } catch (IOException ex) {
            log.info("Keystore not error", ex);
        }
        return null;
    }

    @Override
    public File getCertificate() {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource("testSources/server.crt").getPath();
        return new File(path);
    }

    @Override
    public SignatureTokenConnection getSigningToken() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String path = classLoader.getResource("testSources/keystore.pkcs12").getPath();
            return new Pkcs12SignatureToken(path, new KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray()));
        } catch (IOException ex) {
            log.info("Keystore not error", ex);
        }
        return null;
    }

}
