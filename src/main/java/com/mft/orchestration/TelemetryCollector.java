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

package com.mft.orchestration;

import com.mft.model.TelemetryEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Collects lightweight, per-user telemetry used to build the feature vectors
 * consumed by the AI analysis processor: transfer frequency, and (per
 * Claim 4 of the specification) a destination-address-pattern consistency
 * score derived from each user's historical destination cloud endpoints.
 * Source reputation and failed-authentication-attempt counts are supplied
 * by the caller / authentication processor respectively, mirroring how a
 * production deployment would source those signals from network and
 * identity infrastructure.
 */
public final class TelemetryCollector {

    private final Map<String, AtomicLong> transferCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStartMillis = new ConcurrentHashMap<>();
    private final Map<String, Map<String, AtomicLong>> destinationHistory = new ConcurrentHashMap<>();
    private static final long WINDOW_MILLIS = 60_000L;

    public TelemetryEvent recordAndBuild(String userId, int packetSizeBytes, double sourceReputationScore,
                                          int failedAuthAttempts, String destinationEndpointId) {
        long now = System.currentTimeMillis();
        windowStartMillis.putIfAbsent(userId, now);
        long windowStart = windowStartMillis.get(userId);
        if (now - windowStart > WINDOW_MILLIS) {
            windowStartMillis.put(userId, now);
            transferCounts.put(userId, new AtomicLong(0));
        }
        long count = transferCounts.computeIfAbsent(userId, u -> new AtomicLong(0)).incrementAndGet();
        double frequencyPerMinute = count; // transfers observed within the current 1-minute window

        double destinationConsistencyScore = destinationConsistencyScore(userId, destinationEndpointId);
        recordDestination(userId, destinationEndpointId);

        return new TelemetryEvent(frequencyPerMinute, packetSizeBytes, sourceReputationScore,
                destinationConsistencyScore, failedAuthAttempts);
    }

    /** Backward-compatible overload assuming full destination consistency (e.g. for tests/demos that don't track destination history). */
    public TelemetryEvent recordAndBuild(String userId, int packetSizeBytes, double sourceReputationScore, int failedAuthAttempts) {
        return recordAndBuild(userId, packetSizeBytes, sourceReputationScore, failedAuthAttempts, null);
    }

    /**
     * Computes the proportion of this user's past transfers that were sent
     * to the same destination endpoint as the current request. Users with
     * no destination history yet are given the benefit of the doubt
     * (score 1.0) so a first-ever transfer is not flagged purely for lack
     * of history.
     */
    private double destinationConsistencyScore(String userId, String destinationEndpointId) {
        if (destinationEndpointId == null) {
            return 1.0;
        }
        Map<String, AtomicLong> perDestination = destinationHistory.get(userId);
        if (perDestination == null || perDestination.isEmpty()) {
            return 1.0;
        }
        long totalTransfers = perDestination.values().stream().mapToLong(AtomicLong::get).sum();
        long toThisDestination = perDestination.getOrDefault(destinationEndpointId, new AtomicLong(0)).get();
        return toThisDestination / (double) totalTransfers;
    }

    private void recordDestination(String userId, String destinationEndpointId) {
        if (destinationEndpointId == null) {
            return;
        }
        destinationHistory.computeIfAbsent(userId, u -> new ConcurrentHashMap<>())
                .computeIfAbsent(destinationEndpointId, d -> new AtomicLong(0))
                .incrementAndGet();
    }
}
