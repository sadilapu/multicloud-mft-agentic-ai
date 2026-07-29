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

package com.mft.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Authentication verification processor referenced in the specification:
 * validates credentials (here, a simple bearer-token scheme suitable for a
 * reference implementation) associated with an incoming file transfer
 * request, and tracks per-user failed-attempt counters that feed into the
 * AI analysis processor's telemetry.
 */
public final class AuthenticationVerificationProcessor {

    private final Map<String, String> tokensByUser = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> failedAttempts = new ConcurrentHashMap<>();

    /** Registers a valid authentication token for a user (simulating an identity provider). */
    public void registerUserToken(String userId, String token) {
        tokensByUser.put(userId, token);
    }

    /**
     * Verifies that the supplied token matches the registered token for the
     * user. Increments a failed-attempt counter on mismatch, which the AI
     * analysis processor uses as an additional anomaly signal.
     */
    public boolean authenticate(String userId, String suppliedToken) {
        String expected = tokensByUser.get(userId);
        boolean ok = expected != null && expected.equals(suppliedToken);
        if (!ok) {
            failedAttempts.computeIfAbsent(userId, u -> new AtomicInteger()).incrementAndGet();
        }
        return ok;
    }

    public int getFailedAttempts(String userId) {
        AtomicInteger counter = failedAttempts.get(userId);
        return counter == null ? 0 : counter.get();
    }

    public void resetFailedAttempts(String userId) {
        failedAttempts.remove(userId);
    }
}
