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

import com.mft.Bootstrap;
import com.mft.model.FileTransferRequest;
import com.mft.model.TelemetryEvent;
import com.mft.model.TransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration tests exercising the fully wired pipeline
 * (authentication -> segmentation -> encryption -> routing -> AI analysis
 * -> agentic decision -> cyber-threat defense) via {@link MftOrchestrator},
 * without going through the HTTP layer.
 */
class MftOrchestratorIntegrationTest {

    private MftOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = Bootstrap.buildOrchestrator();
        orchestrator.getAuthProcessor().registerUserToken("alice", "alice-token");
        orchestrator.getAuthProcessor().registerUserToken("mallory", "mallory-token");

        List<TelemetryEvent> baseline = new ArrayList<>();
        Random random = new Random(7);
        for (int i = 0; i < 60; i++) {
            baseline.add(new TelemetryEvent(1 + random.nextInt(2), 30_000 + random.nextInt(4_000), 0.9 + random.nextDouble() * 0.1, 0));
        }
        orchestrator.getAiAnalysisProcessor().trainProfile("alice", baseline);
        orchestrator.getAiAnalysisProcessor().trainProfile("mallory", baseline);
    }

    @Test
    void benignTransferCompletesSuccessfully() {
        FileTransferRequest request = new FileTransferRequest(
                "quarterly-report.pdf",
                "ordinary business document contents".getBytes(StandardCharsets.UTF_8),
                "alice", "alice-token", "aws-us-east-1");

        TransferOutcome outcome = orchestrator.processTransfer(request, false);

        assertEquals(TransferStatus.COMPLETED, outcome.getStatus());
        assertNotNull(outcome.getSession().getRoutedEndpointId());
        assertFalse(outcome.getSession().getPackets().isEmpty());
        assertTrue(outcome.getSession().getPackets().stream().noneMatch(p -> p.isQuarantined()));
    }

    @Test
    void invalidCredentialsRejectTheTransfer() {
        FileTransferRequest request = new FileTransferRequest(
                "file.txt", "content".getBytes(StandardCharsets.UTF_8),
                "alice", "wrong-token", "aws-us-east-1");

        TransferOutcome outcome = orchestrator.processTransfer(request, false);

        assertEquals(TransferStatus.REJECTED, outcome.getStatus());
    }

    @Test
    void maliciousPayloadIsQuarantinedRegardlessOfRiskLevel() {
        FileTransferRequest request = new FileTransferRequest(
                "invoice.exe",
                "EICAR-STANDARD-ANTIVIRUS-TEST-FILE".getBytes(StandardCharsets.UTF_8),
                "alice", "alice-token", "aws-us-east-1");

        TransferOutcome outcome = orchestrator.processTransfer(request, false);

        assertEquals(TransferStatus.QUARANTINED, outcome.getStatus());
        assertTrue(outcome.getSession().getPackets().stream().anyMatch(p -> p.isQuarantined()));
    }

    @Test
    void anomalousBehaviorWithLowReputationAndFailedAuthEscalatesRisk() {
        // Drive up failed-auth attempts first.
        orchestrator.getAuthProcessor().authenticate("mallory", "definitely-wrong");
        orchestrator.getAuthProcessor().authenticate("mallory", "definitely-wrong");
        orchestrator.getAuthProcessor().authenticate("mallory", "definitely-wrong");

        FileTransferRequest request = new FileTransferRequest(
                "exfiltrated-data.zip",
                "some outbound payload of unusual size for this user".getBytes(StandardCharsets.UTF_8),
                "mallory", "mallory-token", "aws-us-east-1");

        // Very low source reputation score simulates an untrusted / suspicious network origin.
        TransferOutcome benignBaselineOutcome = orchestrator.processTransfer(request, false, 0.02);

        assertTrue(benignBaselineOutcome.getRiskAssessment().getRiskScore() > 0.3,
                "expected elevated risk score, got " + benignBaselineOutcome.getRiskAssessment().getRiskScore());
    }

    @Test
    void sessionAndLogsAreQueryableAfterCompletion() {
        FileTransferRequest request = new FileTransferRequest(
                "notes.txt", "hello world".getBytes(StandardCharsets.UTF_8),
                "alice", "alice-token", "aws-us-east-1");

        TransferOutcome outcome = orchestrator.processTransfer(request, false);

        assertTrue(orchestrator.getCoordinationProcessor().hasSession(outcome.getSessionId()));
        assertFalse(orchestrator.getLoggingProcessor().getEntries(outcome.getSessionId()).isEmpty());
    }

    @Test
    void reroutesToDifferentEndpointWhenPreferredEndpointIsIsolated() {
        orchestrator.getConnectivityProcessor().isolateEndpoint("aws-us-east-1");

        FileTransferRequest request = new FileTransferRequest(
                "file.txt", "content that should still route successfully".getBytes(StandardCharsets.UTF_8),
                "alice", "alice-token", "azure-west-europe");

        TransferOutcome outcome = orchestrator.processTransfer(request, false);

        assertEquals(TransferStatus.COMPLETED, outcome.getStatus());
        assertNotEquals("aws-us-east-1", outcome.getSession().getRoutedEndpointId());
    }

    @Test
    void transferToNovelDestinationAfterConsistentHistoryRaisesAnomalyScore() {
        // Establish a strong destination-address pattern: alice always sends to the same endpoint.
        for (int i = 0; i < 15; i++) {
            FileTransferRequest routine = new FileTransferRequest(
                    "routine.txt", "ordinary business document contents".getBytes(StandardCharsets.UTF_8),
                    "alice", "alice-token", "aws-us-east-1");
            orchestrator.processTransfer(routine, false);
        }

        // A sudden transfer to a completely different, never-before-used destination mirrors the
        // specification's data-exfiltration threat scenario (Background of the Invention).
        FileTransferRequest exfiltrationLike = new FileTransferRequest(
                "unusual-destination.txt", "ordinary business document contents".getBytes(StandardCharsets.UTF_8),
                "alice", "alice-token", "corp-private-dc1");

        TransferOutcome outcome = orchestrator.processTransfer(exfiltrationLike, false);

        assertTrue(outcome.getRiskAssessment().getRiskScore() > 0.0,
                "expected the novel destination to be reflected in the risk assessment");
    }
}
