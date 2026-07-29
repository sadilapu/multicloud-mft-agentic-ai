/*
 * Copyright 2026 Srinivas Adilapuram
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mft.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Secure key generation procedure used by the encryption processing unit
 * (patent element 108): generates per-session AES-256 symmetric keys for
 * packet confidentiality and an RSA key pair used to produce digital
 * signatures over each transmitted packet.
 */
public final class KeyManager {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final KeyPair signingKeyPair;

    public KeyManager() {
        this.signingKeyPair = generateSigningKeyPair();
    }

    public SecretKey generateSessionKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256, SECURE_RANDOM);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("AES key generation not supported by this JVM", e);
        }
    }

    private static KeyPair generateSigningKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, SECURE_RANDOM);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA key generation not supported by this JVM", e);
        }
    }

    public KeyPair getSigningKeyPair() {
        return signingKeyPair;
    }

    public static byte[] randomIv(int length) {
        byte[] iv = new byte[length];
        SECURE_RANDOM.nextBytes(iv);
        return iv;
    }
}
