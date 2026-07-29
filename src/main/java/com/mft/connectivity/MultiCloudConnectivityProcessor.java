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

package com.mft.connectivity;

import com.mft.model.CloudEndpoint;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-cloud connectivity processor (patent element 104).
 *
 * <p>Maintains authenticated endpoint registrations and a dynamic routing
 * table for a plurality of cloud computing infrastructures (public,
 * private, hybrid, enterprise data center). Selects an optimal transmission
 * pathway for each transfer based on latency, available bandwidth and
 * security-compliance status, and supports dynamic rerouting away from a
 * given endpoint when the agentic decision processor determines that a
 * transfer should be rerouted in response to a detected threat or network
 * congestion condition.
 */
public final class MultiCloudConnectivityProcessor {

    private final Map<String, CloudEndpoint> routingTable = new ConcurrentHashMap<>();

    /** Registers (or re-registers) a cloud endpoint in the dynamic routing table. */
    public void registerEndpoint(CloudEndpoint endpoint) {
        routingTable.put(endpoint.getId(), endpoint);
    }

    public Optional<CloudEndpoint> getEndpoint(String id) {
        return Optional.ofNullable(routingTable.get(id));
    }

    public Collection<CloudEndpoint> allEndpoints() {
        return List.copyOf(routingTable.values());
    }

    /**
     * Selects the best available, security-compliant endpoint other than
     * {@code excludeId} for outbound transmission, preferring lower latency
     * and higher available bandwidth. Used both for initial routing and for
     * autonomous rerouting after a threat is detected.
     */
    public Optional<CloudEndpoint> selectOptimalEndpoint(String excludeId) {
        return routingTable.values().stream()
                .filter(CloudEndpoint::isAvailable)
                .filter(CloudEndpoint::isSecurityCompliant)
                .filter(e -> excludeId == null || !e.getId().equals(excludeId))
                .min((a, b) -> {
                    // lower latency and higher bandwidth is better -> composite score
                    double scoreA = a.getLatencyMs() - (a.getAvailableBandwidthMbps() * 0.1);
                    double scoreB = b.getLatencyMs() - (b.getAvailableBandwidthMbps() * 0.1);
                    return Double.compare(scoreA, scoreB);
                });
    }

    /**
     * Updates routing telemetry (latency / bandwidth / availability) for an
     * endpoint, simulating periodic network telemetry refresh described in
     * the specification.
     */
    public void updateTelemetry(String endpointId, double latencyMs, double bandwidthMbps, boolean available) {
        CloudEndpoint endpoint = routingTable.get(endpointId);
        if (endpoint == null) {
            throw new IllegalArgumentException("Unknown endpoint: " + endpointId);
        }
        endpoint.setLatencyMs(latencyMs);
        endpoint.setAvailableBandwidthMbps(bandwidthMbps);
        endpoint.setAvailable(available);
    }

    /** Marks an endpoint as non-compliant / unavailable, e.g. after a suspected compromise. */
    public void isolateEndpoint(String endpointId) {
        CloudEndpoint endpoint = routingTable.get(endpointId);
        if (endpoint != null) {
            endpoint.setAvailable(false);
        }
    }
}
