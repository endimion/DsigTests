/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.aegean.dsig.test.dsigTest.signgatures;

import gr.aegean.dsig.test.dsigTest.service.KeystoreService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author nikos
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestKeystoreService {

    @Autowired
    KeystoreService keyServ;

    @Test
    public void testGetPrivateKey() {
        assertNotNull(keyServ.getPrivateKey());

        assertEquals(true, true);
    }

}
