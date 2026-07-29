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

import java.time.Instant;

/**
 * A single, immutable transaction log record capturing an event that
 * occurred during a file transfer's lifecycle (routing decisions,
 * encryption parameters applied, authentication events, anomaly
 * detections, and mitigation actions) for auditing, compliance and model
 * retraining purposes.
 */
public final class TransferLogEntry {

    private final Instant timestamp;
    private final String sessionId;
    private final String eventType;
    private final String details;

    public TransferLogEntry(String sessionId, String eventType, String details) {
        this.timestamp = Instant.now();
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.details = details;
    }

    public Instant getTimestamp() { return timestamp; }
    public String getSessionId() { return sessionId; }
    public String getEventType() { return eventType; }
    public String getDetails() { return details; }

    public String toJsonLine() {
        return "{\"timestamp\":\"%s\",\"sessionId\":\"%s\",\"eventType\":\"%s\",\"details\":\"%s\"}"
                .formatted(timestamp, sessionId, eventType, details.replace("\"", "'"));
    }
}
