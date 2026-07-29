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

package com.mft.defense;

import com.mft.crypto.EncryptionProcessingUnit;
import com.mft.crypto.KeyManager;
import com.mft.model.Packet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CyberThreatDefenseProcessorTest {

    private KeyManager keyManager;
    private EncryptionProcessingUnit encryptionUnit;
    private CyberThreatDefenseProcessor defenseProcessor;
    private QuarantineService quarantineService;
    private SecretKey sessionKey;

    @BeforeEach
    void setUp() {
        keyManager = new KeyManager();
        encryptionUnit = new EncryptionProcessingUnit(keyManager);
        quarantineService = new QuarantineService();
        defenseProcessor = new CyberThreatDefenseProcessor(encryptionUnit, new SandboxInspector(), quarantineService);
        sessionKey = keyManager.generateSessionKey();
    }

    @Test
    void passesCleanIntactPacket() {
        Packet packet = new Packet("p1", 0, 1, "clean payload content".getBytes(StandardCharsets.UTF_8));
        encryptionUnit.encryptAndSign(packet, sessionKey);

        assertTrue(defenseProcessor.inspectAndDefend(packet));
        assertFalse(packet.isQuarantined());
    }

    @Test
    void quarantinesPacketWithTamperedCiphertext() {
        Packet packet = new Packet("p2", 0, 1, "clean payload".getBytes(StandardCharsets.UTF_8));
        encryptionUnit.encryptAndSign(packet, sessionKey);
        byte[] tampered = packet.getCiphertext().clone();
        tampered[0] ^= 0x01;
        packet.setCiphertext(tampered);

        assertFalse(defenseProcessor.inspectAndDefend(packet));
        assertTrue(packet.isQuarantined());
        assertNotNull(quarantineService.getReason(packet.getPacketId()));
    }

    @Test
    void quarantinesPacketFlaggedBySandbox() {
        Packet packet = new Packet("p3", 0, 1, "EICAR-STANDARD-ANTIVIRUS-TEST-FILE".getBytes(StandardCharsets.UTF_8));
        encryptionUnit.encryptAndSign(packet, sessionKey);

        assertFalse(defenseProcessor.inspectAndDefend(packet));
        assertTrue(packet.isQuarantined());
    }
}
