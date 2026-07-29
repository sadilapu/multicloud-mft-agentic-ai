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

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class SandboxInspectorTest {

    private final SandboxInspector inspector = new SandboxInspector();

    @Test
    void flagsKnownMaliciousSignature() {
        byte[] payload = "prefix EICAR-STANDARD-ANTIVIRUS-TEST-FILE suffix".getBytes(StandardCharsets.UTF_8);
        SandboxInspector.SandboxVerdict verdict = inspector.inspect(payload);
        assertTrue(verdict.malicious());
    }

    @Test
    void allowsOrdinaryTextPayload() {
        byte[] payload = "Quarterly financial report for internal review.".getBytes(StandardCharsets.UTF_8);
        SandboxInspector.SandboxVerdict verdict = inspector.inspect(payload);
        assertFalse(verdict.malicious());
    }

    @Test
    void flagsHighEntropyPayloadAsSuspicious() {
        byte[] payload = new byte[4096];
        new SecureRandom().nextBytes(payload); // near-maximum entropy, like packed/encrypted malware
        SandboxInspector.SandboxVerdict verdict = inspector.inspect(payload);
        assertTrue(verdict.malicious());
    }
}
