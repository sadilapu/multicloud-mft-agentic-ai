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

import com.mft.model.Packet;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;

/**
 * Encryption processing unit (patent element 108).
 *
 * <p>Applies AES-256/GCM symmetric encryption to each segmented data packet,
 * and generates a SHA-256 cryptographic hash together with an RSA digital
 * signature over the ciphertext, enabling destination systems to verify
 * authenticity and integrity of received packets during reconstruction.
 */
public final class EncryptionProcessingUnit {

    private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String HASH_ALGORITHM = "SHA-256";

    private final KeyManager keyManager;

    public EncryptionProcessingUnit(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    /**
     * Encrypts a packet's plaintext in place, populating its ciphertext, IV,
     * SHA-256 hash and RSA digital signature fields.
     */
    public void encryptAndSign(Packet packet, SecretKey sessionKey) {
        try {
            byte[] iv = KeyManager.randomIv(GCM_IV_LENGTH_BYTES);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey, spec);
            byte[] ciphertext = cipher.doFinal(packet.getPlaintext());

            packet.setIv(iv);
            packet.setCiphertext(ciphertext);
            packet.setHash(sha256(ciphertext));
            packet.setSignature(sign(ciphertext));
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt/sign packet " + packet.getPacketId(), e);
        }
    }

    /** Decrypts a packet's ciphertext, returning the recovered plaintext. */
    public byte[] decrypt(Packet packet, SecretKey sessionKey) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, packet.getIv());
            cipher.init(Cipher.DECRYPT_MODE, sessionKey, spec);
            return cipher.doFinal(packet.getCiphertext());
        } catch (Exception e) {
            throw new EncryptionException("Failed to decrypt packet " + packet.getPacketId(), e);
        }
    }

    /**
     * Cryptographic integrity verification: recomputes the SHA-256 hash of
     * the ciphertext and verifies the RSA signature, as performed by the
     * cyber-threat defense processor before completing a transfer.
     */
    public boolean verifyIntegrity(Packet packet) {
        try {
            byte[] recomputedHash = sha256(packet.getCiphertext());
            if (!MessageDigest.isEqual(recomputedHash, packet.getHash())) {
                return false;
            }
            Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifier.initVerify(keyManager.getSigningKeyPair().getPublic());
            verifier.update(packet.getCiphertext());
            return verifier.verify(packet.getSignature());
        } catch (Exception e) {
            return false;
        }
    }

    public static SecretKey keyFromBytes(byte[] rawKey) {
        return new SecretKeySpec(rawKey, "AES");
    }

    private byte[] sign(byte[] data) throws Exception {
        PrivateKey privateKey = keyManager.getSigningKeyPair().getPrivate();
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    private static byte[] sha256(byte[] data) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(HASH_ALGORITHM).digest(data);
    }

    public static final class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) { super(message, cause); }
    }
}
