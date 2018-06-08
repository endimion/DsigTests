/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aegean.dsig.test.dsigTest.service;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.SignatureTokenConnection;
import java.io.File;

/**
 *
 * @author nikos
 */
public interface KeystoreService {
    
    public DSSPrivateKeyEntry getPrivateKey();
    public SignatureTokenConnection getSigningToken();
    public File getCertificate();
    
}
