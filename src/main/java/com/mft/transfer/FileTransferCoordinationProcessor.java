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

import com.mft.crypto.IntegrityValidator;
import com.mft.model.FileTransferRequest;
import com.mft.model.Packet;
import com.mft.model.TransferStatus;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * File transfer coordination processor (patent element 106).
 *
 * <p>Receives file transfer requests, performs file integrity validation by
 * computing a baseline cryptographic hash, executes the segmentation
 * technique to divide the file into transmission packets, and tracks each
 * transfer's {@link TransferSession} for the lifetime of the operation.
 */
public final class FileTransferCoordinationProcessor {

    private final IntegrityValidator integrityValidator;
    private final SegmentationEngine segmentationEngine;
    private final ConcurrentHashMap<String, TransferSession> sessions = new ConcurrentHashMap<>();

    public FileTransferCoordinationProcessor(IntegrityValidator integrityValidator,
                                              SegmentationEngine segmentationEngine) {
        this.integrityValidator = integrityValidator;
        this.segmentationEngine = segmentationEngine;
    }

    /** Begins coordination of a new transfer: validates integrity and segments the file. */
    public TransferSession beginTransfer(FileTransferRequest request) {
        byte[] baselineHash = integrityValidator.computeBaselineHash(request.getFileContent());
        String sessionId = "sess-" + UUID.randomUUID();
        TransferSession session = new TransferSession(sessionId, request, baselineHash);
        session.setStatus(TransferStatus.INTEGRITY_VALIDATED);

        List<Packet> packets = segmentationEngine.segment(request.getFileContent());
        session.addPackets(packets);
        session.setStatus(TransferStatus.SEGMENTED);

        sessions.put(sessionId, session);
        return session;
    }

    public TransferSession getSession(String sessionId) {
        TransferSession session = sessions.get(sessionId);
        if (session == null) {
            throw new NoSuchSessionException(sessionId);
        }
        return session;
    }

    public boolean hasSession(String sessionId) {
        return sessions.containsKey(sessionId);
    }

    public IntegrityValidator getIntegrityValidator() { return integrityValidator; }
    public SegmentationEngine getSegmentationEngine() { return segmentationEngine; }

    public static final class NoSuchSessionException extends RuntimeException {
        public NoSuchSessionException(String sessionId) { super("No transfer session found: " + sessionId); }
    }
}
