package com.laklu.pos.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

@Service
public class SignedUrlGenerator {

    @Value("${app.base.secret}")
    private String secretKey;

    public String generateSignedUrl(String baseUrl, HashMap<String, String> data, long expiryInMillis) {
        try {
            long expiryTime = System.currentTimeMillis() + expiryInMillis;

            String queryString = data.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .reduce((a, b) -> a + "&" + b)
                    .orElse("") + "&expiry=" + expiryTime;
            String signature = generateHmacSHA256Signature(queryString, this.secretKey);

            return baseUrl + "?" + queryString + "&signature=" + URLEncoder.encode(signature, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error generating signed URL", e);
        }
    }

    private static String generateHmacSHA256Signature(String data, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKeySpec);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    public boolean isGeneratedSignedUrl(HashMap<String, String> signedUrlData, long expiry, String signature) throws Exception {
        if (System.currentTimeMillis() > expiry) {
            return false;
        }
        String queryString = signedUrlData.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "&" + b)
                .orElse("") + "&expiry=" + expiry;
        String expectedSignature = generateHmacSHA256Signature(queryString, this.secretKey);

        return expectedSignature.equals(signature);
    }
}
