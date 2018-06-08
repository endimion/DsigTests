/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aegean.dsig.test.dsigTest.signgatures;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.EncryptionAlgorithm;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.SignatureAlgorithm;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.SignaturePackaging;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.client.crl.OnlineCRLSource;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.client.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.signature.XAdESService;
import gr.aegean.dsig.test.dsigTest.service.KeystoreService;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.xml.bind.DatatypeConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author nikos
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestXades {

    
    private final Logger log = LoggerFactory.getLogger(TestXades.class);
    
    @Autowired
    KeystoreService keyServ;

    @Test
    public void testSignXades() throws IOException {
        XAdESSignatureParameters parameters = new XAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.XAdES_BASELINE_B);
//        // We choose the type of the signature packaging (ENVELOPED, ENVELOPING, DETACHED).
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
//        // We set the digest algorithm to use with the signature algorithm. You must use the
//        // same parameter when you invoke the method sign on the token. The default value is SHA256
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);
        DSSPrivateKeyEntry privateKey = keyServ.getPrivateKey();
//        // We set the signing certificate
        parameters.setSigningCertificate(privateKey.getCertificate());
////        // We set the certificate chain
        parameters.setCertificateChain(privateKey.getCertificateChain());
//        // Create common certificate verifier
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
//// Create XAdES service for signature
        XAdESService service = new XAdESService(commonCertificateVerifier);

        //Get the document to sign
        ClassLoader classLoader = getClass().getClassLoader();
        String toSignPath = classLoader.getResource("testSources/test.xml").getPath();
        DSSDocument toSignDocument = new FileDocument(toSignPath);

        // Get the SignedInfo XML segment that need to be signed.
        ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);

        // This function obtains the signature value for signed information using the
        // private key and specified algorithm
//        SignatureValue signatureValue = tokenServ.getToken().sign(dataToSign, parameters.getDigestAlgorithm(), privateKey);
        SignatureAlgorithm sigAlgorithm = SignatureAlgorithm.getAlgorithm(EncryptionAlgorithm.ECDSA, DigestAlgorithm.SHA1);
        SignatureValue signatureValue = new SignatureValue(sigAlgorithm, DatatypeConverter.parseBase64Binary("nikos"));

        // We invoke the service to sign the document with the signature value obtained in
        // the previous step.
        DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
        signedDocument.save("/home/nikos/Desktop/signed.xml");
    }

    @Test
    public void testValidation() throws IOException, CertificateException {

        final CertificateToken[] certificateChain = keyServ.getPrivateKey().getCertificateChain();
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        FileInputStream is = new FileInputStream(keyServ.getCertificate());

        X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
        CommonTrustedCertificateSource trustedCertSource = new CommonTrustedCertificateSource();
        trustedCertSource.addCertificate(new CertificateToken(cer));

        //Get the document to sign
        ClassLoader classLoader = getClass().getClassLoader();
        String toValidatePath = classLoader.getResource("testSources/signed.xml").getPath();

        DSSDocument toValidate = new FileDocument(toValidatePath);

 
        CertificateVerifier cv = new CommonCertificateVerifier();
        // We can inject several sources. eg: OCSP, CRL, AIA, trusted lists
        // Capability to download resources from AIA
        cv.setDataLoader(new CommonsDataLoader());
        // Capability to request OCSP Responders
        cv.setOcspSource(new OnlineOCSPSource());
        // Capability to download CRL
        cv.setCrlSource(new OnlineCRLSource());
        // We now add trust anchors (trusted list, keystore,...)
        cv.setTrustedCertSource(trustedCertSource);
        // We also can add missing certificates
//        cv.setAdjunctCertSource(adjunctCertSource);
        cv.setAdjunctCertSource(trustedCertSource);

        // We create an instance of DocumentValidator
        // It will automatically select the supported validator from the classpath
        SignedDocumentValidator documentValidator = SignedDocumentValidator.fromDocument(toValidate);

        // We add the certificate verifier (which allows to verify and trust certificates)
        documentValidator.setCertificateVerifier(cv);

        // Here, everything is ready. We can execute the validation (for the example, we use the default and embedded
        // validation policy)
        Reports reports = documentValidator.validateDocument();

        String xmlSimpleReport = reports.getXmlSimpleReport();
        log.info(xmlSimpleReport);
    }

}
