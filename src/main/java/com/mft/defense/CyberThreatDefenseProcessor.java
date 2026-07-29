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
import com.mft.model.Packet;

/**
 * Cyber-threat defense processor (patent element 114).
 *
 * <p>Performs cryptographic integrity verification (hash comparison and
 * digital signature validation) and, for packets whose plaintext payload
 * warrants closer inspection, redirects them to a sandbox inspection
 * environment. Packets found to carry malicious content or that fail
 * integrity verification are quarantined before the transfer completes.
 */
public final class CyberThreatDefenseProcessor {

    private final EncryptionProcessingUnit encryptionProcessingUnit;
    private final SandboxInspector sandboxInspector;
    private final QuarantineService quarantineService;

    public CyberThreatDefenseProcessor(EncryptionProcessingUnit encryptionProcessingUnit,
                                        SandboxInspector sandboxInspector,
                                        QuarantineService quarantineService) {
        this.encryptionProcessingUnit = encryptionProcessingUnit;
        this.sandboxInspector = sandboxInspector;
        this.quarantineService = quarantineService;
    }

    /**
     * Inspects a single packet: verifies cryptographic integrity, then
     * (if intact) runs sandbox payload inspection on the plaintext.
     * Quarantines the packet if either check fails.
     *
     * @return {@code true} if the packet passed inspection and is safe to deliver.
     */
    public boolean inspectAndDefend(Packet packet) {
        if (!encryptionProcessingUnit.verifyIntegrity(packet)) {
            quarantineService.quarantine(packet, "Cryptographic integrity verification failed (hash/signature mismatch)");
            return false;
        }

        SandboxInspector.SandboxVerdict verdict = sandboxInspector.inspect(packet.getPlaintext());
        if (verdict.malicious()) {
            quarantineService.quarantine(packet, "Sandbox inspection flagged payload: " + verdict.explanation());
            return false;
        }

        return true;
    }

    public QuarantineService getQuarantineService() { return quarantineService; }
}
