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

package com.mft.transfer;

import com.mft.model.FileTransferRequest;
import com.mft.model.Packet;
import com.mft.model.TransferStatus;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Mutable, thread-safe state for one in-flight or completed managed file
 * transfer: the originating request, its packets, session encryption key,
 * routed endpoint, and current lifecycle status.
 */
public final class TransferSession {

    private final String sessionId;
    private final FileTransferRequest request;
    private final byte[] baselineHash;
    private final List<Packet> packets = Collections.synchronizedList(new ArrayList<>());
    private final AtomicReference<TransferStatus> status = new AtomicReference<>(TransferStatus.RECEIVED);
    private final Instant createdAt = Instant.now();

    private volatile SecretKey sessionKey;
    private volatile String routedEndpointId;

    public TransferSession(String sessionId, FileTransferRequest request, byte[] baselineHash) {
        this.sessionId = sessionId;
        this.request = request;
        this.baselineHash = baselineHash;
    }

    public String getSessionId() { return sessionId; }
    public FileTransferRequest getRequest() { return request; }
    public byte[] getBaselineHash() { return baselineHash; }
    public List<Packet> getPackets() { return packets; }
    public TransferStatus getStatus() { return status.get(); }
    public Instant getCreatedAt() { return createdAt; }
    public SecretKey getSessionKey() { return sessionKey; }
    public String getRoutedEndpointId() { return routedEndpointId; }

    public void setStatus(TransferStatus newStatus) { status.set(newStatus); }
    public void setSessionKey(SecretKey sessionKey) { this.sessionKey = sessionKey; }
    public void setRoutedEndpointId(String routedEndpointId) { this.routedEndpointId = routedEndpointId; }
    public void addPackets(List<Packet> newPackets) { packets.addAll(newPackets); }
}
