/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aegean.dsig.test.dsigTest.controllers;

//import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import eu.europa.esig.dss.xades.XAdESSignatureParameters;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author nikos
 */
@Controller
public class Controllers {

    @RequestMapping("/test")
    public @ResponseBody
    String testControllers() {
          XAdESSignatureParameters parameters = new XAdESSignatureParameters();
//        CommonCertificateVerifier commonCertificateVerifier = new CommonCertificateVerifier();

        return "TEST";
    }
}
