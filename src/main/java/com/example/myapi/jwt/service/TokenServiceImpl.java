package com.example.myapi.jwt.service;

import com.example.myapi.jwt.model.TokenRevokerRepository;
import com.example.myapi.jwt.util.TokenCipher;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
JWT management.
jwt in the form of [Base64(HEADER)].[Base64(PAYLOAD)].[Base64(SIGNATURE)]
 */
public class TokenServiceImpl implements TokenService {

    // Accessor for HMAC key - Block serialization and storage as String in JVM memory
    private transient byte[] keyHMAC = null;

    // Accessor for Ciphering key - Block serialization
    private transient KeysetHandle keyCiphering = null;

    //Accessor for Issuer ID - Block serialization
    private transient String issuerID = null;

    private TokenCipher tokenCipher;
    private TokenRevokerRepository tokenRevokerRepository;

    public TokenServiceImpl() throws Exception {
        //Load keys from configuration text/json files in order to avoid to store keys as String in JVM memory
        this.keyHMAC = Files.readAllBytes(Paths.get("src", "main", "conf", "key-hmac.txt"));
        this.keyCiphering = CleartextKeysetHandle.read(JsonKeysetReader.withInputStream(new FileInputStream("src/main/conf/key-ciphering.json")));

        //Load issuer ID from configuration text file
        this.issuerID = Files.readAllLines(Paths.get("src", "main", "conf", "issuer-id.txt")).get(0);
    }



    @Override
    public String getToken(String username) {
        return null;
    }

    @Override
    public String validateToken(String token) {
        return null;
    }

    @Override
    public void revokeToken(String token) {

    }
}
