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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionProcessingUnitTest {

    private KeyManager keyManager;
    private EncryptionProcessingUnit unit;

    @BeforeEach
    void setUp() {
        keyManager = new KeyManager();
        unit = new EncryptionProcessingUnit(keyManager);
    }

    @Test
    void encryptsAndDecryptsRoundTrip() {
        byte[] plaintext = "top secret payload".getBytes(StandardCharsets.UTF_8);
        Packet packet = new Packet("pkt-1", 0, 1, plaintext);
        SecretKey key = keyManager.generateSessionKey();

        unit.encryptAndSign(packet, key);

        assertNotNull(packet.getCiphertext());
        assertFalse(Arrays.equals(plaintext, packet.getCiphertext()), "ciphertext must differ from plaintext");

        byte[] decrypted = unit.decrypt(packet, key);
        assertArrayEquals(plaintext, decrypted);
    }

    @Test
    void verifiesIntegrityOfUnalteredPacket() {
        Packet packet = new Packet("pkt-2", 0, 1, "data".getBytes(StandardCharsets.UTF_8));
        unit.encryptAndSign(packet, keyManager.generateSessionKey());

        assertTrue(unit.verifyIntegrity(packet));
    }

    @Test
    void detectsTamperedCiphertext() {
        Packet packet = new Packet("pkt-3", 0, 1, "data".getBytes(StandardCharsets.UTF_8));
        unit.encryptAndSign(packet, keyManager.generateSessionKey());

        byte[] tampered = packet.getCiphertext().clone();
        tampered[0] ^= 0xFF;
        packet.setCiphertext(tampered);

        assertFalse(unit.verifyIntegrity(packet));
    }

    @Test
    void distinctPacketsUseDistinctIvs() {
        Packet p1 = new Packet("p1", 0, 2, "chunk-one".getBytes(StandardCharsets.UTF_8));
        Packet p2 = new Packet("p2", 1, 2, "chunk-two".getBytes(StandardCharsets.UTF_8));
        SecretKey key = keyManager.generateSessionKey();

        unit.encryptAndSign(p1, key);
        unit.encryptAndSign(p2, key);

        assertFalse(Arrays.equals(p1.getIv(), p2.getIv()), "IVs must be unique per packet");
    }

    @Test
    void decryptionFailsWithWrongKey() {
        Packet packet = new Packet("pkt-4", 0, 1, "secret".getBytes(StandardCharsets.UTF_8));
        unit.encryptAndSign(packet, keyManager.generateSessionKey());

        SecretKey wrongKey = keyManager.generateSessionKey();
        assertThrows(EncryptionProcessingUnit.EncryptionException.class, () -> unit.decrypt(packet, wrongKey));
    }
}
