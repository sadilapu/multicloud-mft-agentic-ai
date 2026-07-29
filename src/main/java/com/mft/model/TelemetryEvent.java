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

/**
 * A structured feature vector describing one file-transfer telemetry sample,
 * directly mirroring the behavioral-profile parameters enumerated in Claim 4
 * of the specification: "average transfer frequency, packet size
 * distribution, source address patterns, destination address patterns, and
 * user authentication characteristics."
 *
 * <ul>
 *   <li>{@code transferFrequency} — transfers/minute observed for this user (average transfer frequency).</li>
 *   <li>{@code packetSizeBytes} — size of the transfer's packets (packet size distribution).</li>
 *   <li>{@code sourceReputationScore} — trust score in [0,1] for the request's network origin (source address patterns).</li>
 *   <li>{@code destinationConsistencyScore} — in [0,1], how consistent the requested destination cloud endpoint is with this user's historical destinations (destination address patterns).</li>
 *   <li>{@code failedAuthAttempts} — recent failed authentication attempts for this user (user authentication characteristics).</li>
 * </ul>
 */
public final class TelemetryEvent {

    private final double transferFrequency;
    private final double packetSizeBytes;
    private final double sourceReputationScore;
    private final double destinationConsistencyScore;
    private final int failedAuthAttempts;

    public TelemetryEvent(double transferFrequency, double packetSizeBytes, double sourceReputationScore,
                           double destinationConsistencyScore, int failedAuthAttempts) {
        this.transferFrequency = transferFrequency;
        this.packetSizeBytes = packetSizeBytes;
        this.sourceReputationScore = sourceReputationScore;
        this.destinationConsistencyScore = destinationConsistencyScore;
        this.failedAuthAttempts = failedAuthAttempts;
    }

    /** Convenience constructor for callers that don't yet track destination-address history; assumes a fully consistent (trusted) destination. */
    public TelemetryEvent(double transferFrequency, double packetSizeBytes, double sourceReputationScore, int failedAuthAttempts) {
        this(transferFrequency, packetSizeBytes, sourceReputationScore, 1.0, failedAuthAttempts);
    }

    public double getTransferFrequency() { return transferFrequency; }
    public double getPacketSizeBytes() { return packetSizeBytes; }
    public double getSourceReputationScore() { return sourceReputationScore; }
    public double getDestinationConsistencyScore() { return destinationConsistencyScore; }
    public int getFailedAuthAttempts() { return failedAuthAttempts; }

    public double[] toFeatureArray() {
        return new double[] { transferFrequency, packetSizeBytes, sourceReputationScore, destinationConsistencyScore, failedAuthAttempts };
    }

    public static String[] featureNames() {
        return new String[] { "transferFrequency", "packetSizeBytes", "sourceReputationScore", "destinationConsistencyScore", "failedAuthAttempts" };
    }
}
