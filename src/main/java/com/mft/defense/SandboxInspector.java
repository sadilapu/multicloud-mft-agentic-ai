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

/**
 * Simulated sandbox inspection environment: executes/inspects a packet
 * payload within an isolated analysis routine to identify malicious
 * executable behavior, as described in the specification. This reference
 * implementation uses payload-entropy analysis and known-signature matching
 * as inexpensive, dependency-free heuristics standing in for a full dynamic
 * execution sandbox; production deployments would substitute a real
 * containerized/VM-based sandbox here.
 */
public final class SandboxInspector {

    /** Shannon-entropy threshold (bits/byte) above which a payload is treated as suspicious (e.g. packed/encrypted malware). */
    public static final double ENTROPY_SUSPICION_THRESHOLD = 7.5;

    private static final byte[][] KNOWN_MALICIOUS_SIGNATURES = {
            "EICAR-STANDARD-ANTIVIRUS-TEST-FILE".getBytes(java.nio.charset.StandardCharsets.US_ASCII),
    };

    public SandboxVerdict inspect(byte[] payload) {
        for (byte[] signature : KNOWN_MALICIOUS_SIGNATURES) {
            if (containsSubsequence(payload, signature)) {
                return new SandboxVerdict(true, "Matched known malicious signature");
            }
        }

        double entropy = shannonEntropy(payload);
        if (entropy > ENTROPY_SUSPICION_THRESHOLD) {
            return new SandboxVerdict(true, "High payload entropy (%.2f bits/byte) suggestive of packed or obfuscated content".formatted(entropy));
        }

        return new SandboxVerdict(false, "No malicious indicators observed");
    }

    private static double shannonEntropy(byte[] data) {
        if (data.length == 0) return 0.0;
        int[] counts = new int[256];
        for (byte b : data) {
            counts[b & 0xFF]++;
        }
        double entropy = 0.0;
        for (int count : counts) {
            if (count == 0) continue;
            double p = count / (double) data.length;
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    private static boolean containsSubsequence(byte[] haystack, byte[] needle) {
        if (needle.length == 0 || haystack.length < needle.length) return false;
        outer:
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return true;
        }
        return false;
    }

    public record SandboxVerdict(boolean malicious, String explanation) {}
}
