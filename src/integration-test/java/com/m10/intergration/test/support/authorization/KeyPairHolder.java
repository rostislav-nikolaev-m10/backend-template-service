package com.m10.intergration.test.support.authorization;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public record KeyPairHolder(
        RSAPublicKey publicKey,
        RSAPrivateKey privateKey
) {

    public KeyPairHolder {
        if (publicKey == null || privateKey == null) {
            throw new IllegalArgumentException("KeyPair cannot be null");
        }
    }

    public KeyPairHolder(KeyPair keyPair) {
        this((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
    }

}
