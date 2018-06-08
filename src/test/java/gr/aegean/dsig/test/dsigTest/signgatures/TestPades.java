/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aegean.dsig.test.dsigTest.signgatures;

import eu.europa.esig.dss.DSSDocument;
import eu.europa.esig.dss.DigestAlgorithm;
import eu.europa.esig.dss.FileDocument;
import eu.europa.esig.dss.SignatureLevel;
import eu.europa.esig.dss.SignaturePackaging;
import eu.europa.esig.dss.SignatureValue;
import eu.europa.esig.dss.ToBeSigned;
import eu.europa.esig.dss.client.crl.OnlineCRLSource;
import eu.europa.esig.dss.client.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.client.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.pades.PAdESSignatureParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.SignatureImageTextParameters;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.validation.CertificateVerifier;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.validation.SignedDocumentValidator;
import eu.europa.esig.dss.validation.reports.Reports;
import eu.europa.esig.dss.x509.CertificateToken;
import eu.europa.esig.dss.x509.CommonTrustedCertificateSource;
import gr.aegean.dsig.test.dsigTest.service.KeystoreService;
import gr.aegean.dsig.test.dsigTest.service.XmlParsingService;
import java.awt.Color;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Optional;
import static org.junit.Assert.assertEquals;
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
public class TestPades {

    private final static Logger log = LoggerFactory.getLogger(TestPades.class);

    @Autowired
    KeystoreService keyServ;

    @Autowired
    XmlParsingService xmlServ;

    @Test
    public void signPades() throws IOException {
        PAdESSignatureParameters parameters = new PAdESSignatureParameters();
        parameters.setSignatureLevel(SignatureLevel.PAdES_BASELINE_B);
        parameters.setSignaturePackaging(SignaturePackaging.ENVELOPED);
        parameters.setDigestAlgorithm(DigestAlgorithm.SHA256);

        SignatureImageParameters imageParameters = new SignatureImageParameters();
        // the origin is the left and top corner of the page
        imageParameters.setxAxis(200);
        imageParameters.setyAxis(200);

        // Initialize text to generate for visual signature
        SignatureImageTextParameters textParameters = new SignatureImageTextParameters();
        textParameters.setFont(new Font("serif", Font.PLAIN, 14));
        textParameters.setTextColor(Color.BLUE);
        textParameters.setText("My visual signature");
        imageParameters.setTextParameters(textParameters);

        parameters.setSignatureImageParameters(imageParameters);

        DSSPrivateKeyEntry privateKey = keyServ.getPrivateKey();
        parameters.setSigningCertificate(privateKey.getCertificate());
        parameters.setCertificateChain(privateKey.getCertificateChain());
        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();
        PAdESService service = new PAdESService(commonCertificateVerifier);

        // This function obtains the signature value for signed information using the
        // private key and specified algorithm
        DigestAlgorithm digestAlgorithm = parameters.getDigestAlgorithm();

        //Get the document to sign
        ClassLoader classLoader = getClass().getClassLoader();
        String toSignPath = classLoader.getResource("testSources/test.pdf").getPath();
        DSSDocument toSignDocument = new FileDocument(toSignPath);

//        SignatureAlgorithm sigAlgorithm = SignatureAlgorithm.getAlgorithm(EncryptionAlgorithm.ECDSA, DigestAlgorithm.SHA1);
        //        SignatureValue signatureValue = new SignatureValue(sigAlgorithm, DatatypeConverter.parseBase64Binary("nikos"));
        // Get the SignedInfo XML segment that need to be signed.
        ToBeSigned dataToSign = service.getDataToSign(toSignDocument, parameters);
        SignatureValue signatureValue = keyServ.getSigningToken().sign(dataToSign, digestAlgorithm, privateKey);

        // We invoke the service to sign the document with the signature value obtained in
        // the previous step.
        DSSDocument signedDocument = service.signDocument(toSignDocument, parameters, signatureValue);
        signedDocument.save("/home/nikos/Desktop/signed.pdf");
    }

    @Test
    public void testValidation() throws IOException, CertificateException {

        final CertificateToken[] certificateChain = keyServ.getPrivateKey().getCertificateChain();
        final CertificateToken trustedCertificate = certificateChain[0];

        CertificateFactory fact = CertificateFactory.getInstance("X.509");

        FileInputStream is = new FileInputStream(keyServ.getCertificate());

        X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
        CommonTrustedCertificateSource trustedCertSource = new CommonTrustedCertificateSource();
        trustedCertSource.addCertificate(new CertificateToken(cer));

        //Get the document to sign
        ClassLoader classLoader = getClass().getClassLoader();
        String toValidatePath = classLoader.getResource("testSources/signed.pdf").getPath();

        DSSDocument toValidate = new FileDocument(toValidatePath);

//        CommonTrustedCertificateSource trustedCertSource = new CommonTrustedCertificateSource();
//        trustedCertSource.addCertificate(trustedCertificate);
//        trustedCertSource.
        // See Trusted Lists loading
//        CertificateSource trustedCertSource = null;
//        CertificateSource adjunctCertSource = null;
        // First, we need a Certificate verifier
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
        xmlSimpleReport =  xmlSimpleReport ;
        Optional<String> result = xmlServ.getValueByTagName(xmlSimpleReport, "Indication");
        assertEquals(result.get(), "TOTAL_PASSED");
    }

}
