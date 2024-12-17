package com.good.ivrstand.app;

import com.good.ivrstand.app.service.EncodeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EncodeServiceTest {

    private EncodeService encodeService;

    @BeforeEach
    void setUp() {
        encodeService = new EncodeService("BC1D9VG58QKH6CYK99DGB1UBESR8VRXD");
    }

    @Test
    public void encryptTest() {
        String plainText = "text123";
        String expectedText = "bQd+8RRXsEd8DaOsQFkGmw==";
        String encryptedText = encodeService.encrypt(plainText);

        Assertions.assertNotNull(encryptedText);
        Assertions.assertEquals(expectedText, encryptedText);
    }

    @Test
    public void decryptTest() {
        String expectedText = "text123";
        String encryptedText = "bQd+8RRXsEd8DaOsQFkGmw==";
        String decryptedText = encodeService.decrypt(encryptedText);

        Assertions.assertNotNull(decryptedText);
        Assertions.assertEquals(expectedText, decryptedText);
    }
}
