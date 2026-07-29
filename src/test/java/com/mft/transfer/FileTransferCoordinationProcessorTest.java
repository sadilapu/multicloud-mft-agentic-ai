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
import com.mft.model.TransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FileTransferCoordinationProcessorTest {

    private FileTransferCoordinationProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new FileTransferCoordinationProcessor(new IntegrityValidator(), new SegmentationEngine(16));
    }

    @Test
    void beginTransferValidatesIntegrityAndSegments() {
        FileTransferRequest request = new FileTransferRequest(
                "report.pdf", "some file content to segment".getBytes(StandardCharsets.UTF_8),
                "alice", "token-alice", "endpoint-1");

        TransferSession session = processor.beginTransfer(request);

        assertEquals(TransferStatus.SEGMENTED, session.getStatus());
        assertFalse(session.getPackets().isEmpty());
        assertNotNull(session.getBaselineHash());
        assertTrue(processor.hasSession(session.getSessionId()));
    }

    @Test
    void getSessionThrowsForUnknownId() {
        assertThrows(FileTransferCoordinationProcessor.NoSuchSessionException.class,
                () -> processor.getSession("does-not-exist"));
    }

    @Test
    void multipleTransfersTrackedIndependently() {
        FileTransferRequest r1 = new FileTransferRequest("a.txt", "AAAA".getBytes(StandardCharsets.UTF_8), "u1", "t1", "e1");
        FileTransferRequest r2 = new FileTransferRequest("b.txt", "BBBB".getBytes(StandardCharsets.UTF_8), "u2", "t2", "e1");

        TransferSession s1 = processor.beginTransfer(r1);
        TransferSession s2 = processor.beginTransfer(r2);

        assertNotEquals(s1.getSessionId(), s2.getSessionId());
        assertEquals("a.txt", processor.getSession(s1.getSessionId()).getRequest().getFileName());
        assertEquals("b.txt", processor.getSession(s2.getSessionId()).getRequest().getFileName());
    }
}
