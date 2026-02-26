package com.m10.demo.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.m10.demo.service.HashingService;

@Service
public class HashingServiceImpl implements HashingService {

    private final Mac hmacSha256;

    public HashingServiceImpl(@Value("hashing.secret") String secret) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            this.hmacSha256 = Mac.getInstance("HmacSHA256");
            this.hmacSha256.init(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot initialize HMAC", e);
        }
    }

    @Override
    public String hash(String input) {
        byte[] hmac = hmacSha256.doFinal(input.toLowerCase().getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmac);
    }

}
