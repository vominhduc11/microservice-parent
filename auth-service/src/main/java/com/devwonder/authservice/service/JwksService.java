package com.devwonder.authservice.service;

import org.springframework.stereotype.Service;

import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.*;
import java.security.spec.RSAPublicKeySpec;
import java.math.BigInteger;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JwksService {

    private static final Logger logger = LoggerFactory.getLogger(JwksService.class);
    
    private KeyPair keyPair;
    private String keyId;

    @PostConstruct
    public void init() {
        generateKeyPair();
    }

    private void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            this.keyPair = keyPairGenerator.generateKeyPair();
            this.keyId = UUID.randomUUID().toString();
            logger.info("Generated new RSA key pair with ID: {}", keyId);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to generate RSA key pair", e);
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }

    public Map<String, Object> getJwks() {
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        
        Map<String, Object> key = new HashMap<>();
        key.put("kty", "RSA");
        key.put("use", "sig");
        key.put("kid", keyId);
        key.put("alg", "RS256");
        key.put("n", encodeToBase64URL(publicKey.getModulus()));
        key.put("e", encodeToBase64URL(publicKey.getPublicExponent()));

        Map<String, Object> jwks = new HashMap<>();
        jwks.put("keys", Collections.singletonList(key));

        return jwks;
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public String getKeyId() {
        return keyId;
    }

    private String encodeToBase64URL(BigInteger bigInteger) {
        byte[] bytes = bigInteger.toByteArray();
        
        // Remove leading zero byte if present (for positive numbers)
        if (bytes.length > 1 && bytes[0] == 0) {
            byte[] tmp = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, tmp, 0, tmp.length);
            bytes = tmp;
        }
        
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}