package com.example.myapi.jwt.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.myapi.jwt.model.TokenRevokerRepository;
import com.example.myapi.jwt.util.TokenCipher;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import org.springframework.stereotype.Service;

import javax.xml.bind.DatatypeConverter;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/*
JWT management.
jwt in the form of [Base64(HEADER)].[Base64(PAYLOAD)].[Base64(SIGNATURE)]
Chunk 1: Header

{
  "alg": "HS256",
  "typ": "JWT"
}
Chunk 2: Payload

{
  "sub": "1234567890",
  "name": "John Doe",
  "admin": true
}
Chunk 3: Signature

HMACSHA256( base64UrlEncode(header) + "." + base64UrlEncode(payload), KEY )
 */

@Service
public class TokenServiceImpl implements TokenService {

    // Accessor for HMAC key - Block serialization and storage as String in JVM memory
    private transient byte[] keyHMAC = null;

    // Accessor for Ciphering key - Block serialization
    private transient KeysetHandle keyCiphering = null;

    //Accessor for Issuer ID - Block serialization
    private transient String issuerID = null;
    private SecureRandom secureRandom = new SecureRandom();

    private TokenCipher tokenCipher;
    private TokenRevokerRepository tokenRevokerRepository;

    public TokenServiceImpl(TokenRevokerRepository tokenRevokerRepository) throws Exception {
        //Load keys from configuration text/json files in order to avoid to store keys as String in JVM memory
        this.keyHMAC = Files.readAllBytes(Paths.get("src", "main", "conf", "key-hmac.txt"));
        this.keyCiphering = CleartextKeysetHandle.read(JsonKeysetReader.withInputStream(new FileInputStream("src/main/conf/key-ciphering.json")));

        //Load issuer ID from configuration text file
        this.issuerID = Files.readAllLines(Paths.get("src", "main", "conf", "issuer-id.txt")).get(0);

        this.tokenCipher = new TokenCipher();
        this.tokenRevokerRepository = tokenRevokerRepository;
    }

    /*
    Assuming user is already authenticated, generate JWT from their username
    - take in username
    - generate and return token
    - generate and return fingerprint (to be put in a secure cookie):
        - String fingerprintCookie = "__Secure-Fgp=" + userFingerprint + "; SameSite=Strict; HttpOnly; Secure";
        - response.addHeader("Set-Cookie", fingerprintCookie);
    - return status
     */
    @Override
    public Map<String, String> getToken(String username) {
        Map<String, String> map = new HashMap<>(); // use 'token' and 'fingerprint' keys

        if(!Pattern.matches("[a-zA-Z0-9]{1,10}", username)){
            map.put("token", "-");
            map.put("fingerprint", "-");
            map.put("status", "Invalid input");
            return map;
        }

        try {
            // Generate random string to be used as users fingerprint
            byte[] randomFgp = new byte[50];
            this.secureRandom.nextBytes(randomFgp);
            String userFingerprint = DatatypeConverter.printHexBinary(randomFgp);
            map.put("fingerprint", userFingerprint);

            // use a SHA256 hash of the fingerprint to store the fingerprint hash (instead of the raw value) in the token
            // to prevent an XSS to be able to read the fingerprint and set the expected cookie itself
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] userFingerprintDigest = digest.digest(userFingerprint.getBytes(StandardCharsets.UTF_8));
            String userFingerprintHash = DatatypeConverter.printHexBinary(userFingerprintDigest);

            // create token with 15 minute validity, with fingerprint
            Calendar c = Calendar.getInstance();
            Date now = c.getTime();
            c.add(Calendar.MINUTE, 15);
            Date expirationDate = c.getTime();
            Map<String, Object> headerClaims = new HashMap<>();
            headerClaims.put("typ", "JWT");
            String token = JWT.create().withSubject(username)
                    .withExpiresAt(expirationDate)
                    .withIssuer(this.issuerID)
                    .withIssuedAt(now)
                    .withNotBefore(now)
                    .withClaim("userFingerprint", userFingerprintHash)
                    .withHeader(headerClaims)
                    .sign(Algorithm.HMAC256(this.keyHMAC));
            System.out.println(token);

            // cipher the token to hex
            String cipheredToken = this.tokenCipher.cipherToken(token, this.keyCiphering);
            map.put("token", cipheredToken);
            map.put("status", "Successful generation");
            return map;

        } catch (Exception e) {
            map.put("token", "-");
            map.put("fingerprint", "-");
            map.put("status", "Error during generation");
            return map;
        }
    }

    @Override
    public String validateToken(String token) {
        return null;
    }

    @Override
    public void revokeToken(String token) {

    }
}
