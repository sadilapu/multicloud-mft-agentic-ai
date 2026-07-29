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

package com.mft.model;

import java.util.Objects;

/**
 * Represents a single cloud computing infrastructure endpoint (public,
 * private, hybrid or enterprise data center) that the multi-cloud
 * connectivity processor can route transfers to.
 */
public final class CloudEndpoint {

    private final String id;
    private final String name;
    private final CloudType type;
    private volatile double latencyMs;
    private volatile double availableBandwidthMbps;
    private volatile boolean available;
    private volatile boolean securityCompliant;

    public CloudEndpoint(String id, String name, CloudType type, double latencyMs,
                          double availableBandwidthMbps, boolean available, boolean securityCompliant) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.latencyMs = latencyMs;
        this.availableBandwidthMbps = availableBandwidthMbps;
        this.available = available;
        this.securityCompliant = securityCompliant;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public CloudType getType() { return type; }
    public double getLatencyMs() { return latencyMs; }
    public double getAvailableBandwidthMbps() { return availableBandwidthMbps; }
    public boolean isAvailable() { return available; }
    public boolean isSecurityCompliant() { return securityCompliant; }

    public void setLatencyMs(double latencyMs) { this.latencyMs = latencyMs; }
    public void setAvailableBandwidthMbps(double availableBandwidthMbps) { this.availableBandwidthMbps = availableBandwidthMbps; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setSecurityCompliant(boolean securityCompliant) { this.securityCompliant = securityCompliant; }

    @Override
    public String toString() {
        return "CloudEndpoint{id='%s', name='%s', type=%s, latencyMs=%.2f, bandwidthMbps=%.2f, available=%s, compliant=%s}"
                .formatted(id, name, type, latencyMs, availableBandwidthMbps, available, securityCompliant);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CloudEndpoint that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
