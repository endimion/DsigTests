/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aegean.dsig.test.dsigTest.service.impl;

import gr.aegean.dsig.test.dsigTest.service.XmlParsingService;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author nikos
 */
@Service
public class XmlParsingServiceIml implements XmlParsingService {

    private DocumentBuilder dBuilder;
    private final static Logger log = LoggerFactory.getLogger(XmlParsingServiceIml.class);

    public XmlParsingServiceIml() throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dBuilder = dbFactory.newDocumentBuilder();
    }

    @Override
    public Optional<String> getValueByTagName(String xmlString, String tagName) {
        Document doc;
        try {
            doc = dBuilder.parse(new StringBufferInputStream(xmlString));
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            return Optional.of(doc.getElementsByTagName(tagName).item(0).getTextContent());
        } catch (SAXException | IOException ex) {
            log.info("error ", ex);
        }
        return Optional.empty();
    }

}
