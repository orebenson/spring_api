package com.example.myapi.jwt.util;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;

import javax.xml.bind.DatatypeConverter;

public class TokenCipher {
    /*
    Ciphering using AES-GCM
    Cipher and decipher token using google tink library for best practises
    Aead hides all ciphering logic for security
     */
    public TokenCipher() throws Exception {
        AeadConfig.register(); // registers key manager in 'Registry', a global storage of key managers
    }

    /*
    Cipher a JWT
    - takes jwt to cipher, pointer to keyset handle
    - returns ciphered token in hex
     */
    public String cipherToken(String jwt, KeysetHandle keysetHandle) throws Exception {
        if (jwt == null || jwt.isEmpty() || keysetHandle == null) {
            throw new IllegalArgumentException("Both parameters must be present");
        }

        // get primitive aead
        Aead aead = keysetHandle.getPrimitive(Aead.class);

        // cipher the token
        byte[] cipheredToken = aead.encrypt(jwt.getBytes(), null);

        //convert token to hex
        return DatatypeConverter.printHexBinary(cipheredToken);
    }

    /*
    Decipher a jwt
    - takes jwt in hex
    - takes keyset handle
    - returne cleartext token
     */
    public String decipherToken(String jwtInHex, KeysetHandle keysetHandle) throws Exception {
        if (jwtInHex == null || jwtInHex.isEmpty() || keysetHandle == null) {
            throw new IllegalArgumentException("Both parameters must be present");
        }

        // convert token back to byte
        byte[] cipheredToken = DatatypeConverter.parseHexBinary(jwtInHex);

        // get primitive
        Aead aead = keysetHandle.getPrimitive(Aead.class);

        // decipher token using primitive
        byte[] decipheredToken = aead.decrypt(cipheredToken, null);

        return new String(decipheredToken);
    }


}
