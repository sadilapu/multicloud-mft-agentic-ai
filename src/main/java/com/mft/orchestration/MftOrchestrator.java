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

import com.mft.agent.AgenticDecisionProcessor;
import com.mft.ai.AIAnalysisProcessor;
import com.mft.auth.AuthenticationVerificationProcessor;
import com.mft.connectivity.MultiCloudConnectivityProcessor;
import com.mft.crypto.EncryptionProcessingUnit;
import com.mft.defense.CyberThreatDefenseProcessor;
import com.mft.logging.TransferLoggingProcessor;
import com.mft.model.*;
import com.mft.transfer.FileTransferCoordinationProcessor;
import com.mft.transfer.TransferSession;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Optional;

/**
 * Top-level orchestrator implementing the full multi-cloud managed file
 * transfer system with agentic AI/ML orchestration for cyber threat
 * defense described in the specification. Wires together the computing
 * device's constituent processors — multi-cloud connectivity (104), file
 * transfer coordination (106), encryption (108), AI analysis (110),
 * agentic decision (112), and cyber-threat defense (114) — into a single
 * end-to-end pipeline:
 *
 * <ol>
 *   <li>Authenticate the requesting user.</li>
 *   <li>Validate file integrity and segment the file into packets.</li>
 *   <li>Encrypt and sign each packet.</li>
 *   <li>Select an optimal multi-cloud transmission pathway.</li>
 *   <li>Analyze real-time telemetry for behavioral anomalies.</li>
 *   <li>Autonomously assess risk and select a defensive action.</li>
 *   <li>Run cryptographic integrity + sandbox inspection on every packet.</li>
 *   <li>Apply the selected defensive action and finalize the transfer.</li>
 * </ol>
 *
 * Every stage is recorded via the transfer logging processor for auditing
 * and subsequent model retraining.
 */
public final class MftOrchestrator {

    private final AuthenticationVerificationProcessor authProcessor;
    private final FileTransferCoordinationProcessor coordinationProcessor;
    private final EncryptionProcessingUnit encryptionProcessingUnit;
    private final MultiCloudConnectivityProcessor connectivityProcessor;
    private final AIAnalysisProcessor aiAnalysisProcessor;
    private final AgenticDecisionProcessor agenticDecisionProcessor;
    private final CyberThreatDefenseProcessor defenseProcessor;
    private final TransferLoggingProcessor loggingProcessor;
    private final TelemetryCollector telemetryCollector;
    private final com.mft.crypto.KeyManager keyManager;

    public MftOrchestrator(AuthenticationVerificationProcessor authProcessor,
                            FileTransferCoordinationProcessor coordinationProcessor,
                            EncryptionProcessingUnit encryptionProcessingUnit,
                            com.mft.crypto.KeyManager keyManager,
                            MultiCloudConnectivityProcessor connectivityProcessor,
                            AIAnalysisProcessor aiAnalysisProcessor,
                            AgenticDecisionProcessor agenticDecisionProcessor,
                            CyberThreatDefenseProcessor defenseProcessor,
                            TransferLoggingProcessor loggingProcessor,
                            TelemetryCollector telemetryCollector) {
        this.authProcessor = authProcessor;
        this.coordinationProcessor = coordinationProcessor;
        this.encryptionProcessingUnit = encryptionProcessingUnit;
        this.keyManager = keyManager;
        this.connectivityProcessor = connectivityProcessor;
        this.aiAnalysisProcessor = aiAnalysisProcessor;
        this.agenticDecisionProcessor = agenticDecisionProcessor;
        this.defenseProcessor = defenseProcessor;
        this.loggingProcessor = loggingProcessor;
        this.telemetryCollector = telemetryCollector;
    }

    /**
     * Processes one file transfer request end to end and returns the final
     * outcome. Overloads allow tests / API callers to simulate the
     * source-reputation signal that a production system would otherwise
     * derive from network/IP-reputation infrastructure.
     */
    public TransferOutcome processTransfer(FileTransferRequest request, boolean privilegedUser) {
        return processTransfer(request, privilegedUser, 1.0);
    }

    public TransferOutcome processTransfer(FileTransferRequest request, boolean privilegedUser, double sourceReputationScore) {
        String userId = request.getSourceUserId();

        // 1. Authentication
        boolean authenticated = authProcessor.authenticate(userId, request.getAuthToken());
        if (!authenticated) {
            TransferSession rejectedSession = rejectedSessionFor(request);
            loggingProcessor.log(rejectedSession.getSessionId(), "AUTH_FAILED", "Authentication failed for user " + userId);
            return new TransferOutcome(rejectedSession, new RiskAssessment(1.0, DefensiveAction.SUSPEND_TRANSFER, "Authentication failure"));
        }
        int failedAuthAttempts = authProcessor.getFailedAttempts(userId);

        // 2. Integrity validation + segmentation
        TransferSession session = coordinationProcessor.beginTransfer(request);
        loggingProcessor.log(session.getSessionId(), "SEGMENTED", session.getPackets().size() + " packet(s) created");

        // 3. Encryption
        SecretKey sessionKey = keyManager.generateSessionKey();
        session.setSessionKey(sessionKey);
        for (Packet packet : session.getPackets()) {
            encryptionProcessingUnit.encryptAndSign(packet, sessionKey);
        }
        session.setStatus(TransferStatus.ENCRYPTED);
        loggingProcessor.log(session.getSessionId(), "ENCRYPTED", "AES-256/GCM applied to all packets; RSA-signed");

        // 4. Routing
        Optional<CloudEndpoint> endpoint = connectivityProcessor.selectOptimalEndpoint(null);
        if (endpoint.isEmpty()) {
            session.setStatus(TransferStatus.SUSPENDED);
            loggingProcessor.log(session.getSessionId(), "ROUTING_FAILED", "No available, security-compliant cloud endpoint");
            return new TransferOutcome(session, new RiskAssessment(0.5, DefensiveAction.SUSPEND_TRANSFER, "No route available"));
        }
        session.setRoutedEndpointId(endpoint.get().getId());
        session.setStatus(TransferStatus.ROUTED);
        loggingProcessor.log(session.getSessionId(), "ROUTED", "Selected endpoint " + endpoint.get().getId());

        // 5. Telemetry + AI analysis
        int avgPacketSize = (int) session.getPackets().stream().mapToInt(Packet::sizeBytes).average().orElse(0);
        TelemetryEvent telemetry = telemetryCollector.recordAndBuild(userId, avgPacketSize, sourceReputationScore,
                failedAuthAttempts, request.getDestinationEndpointId());
        AnomalyIndicator anomaly = aiAnalysisProcessor.analyze(userId, telemetry);
        loggingProcessor.log(session.getSessionId(), "AI_ANALYSIS", anomaly.toString());

        // 6. Agentic risk assessment + decision
        RiskAssessment assessment = agenticDecisionProcessor.assess(userId, anomaly, failedAuthAttempts, privilegedUser);
        loggingProcessor.log(session.getSessionId(), "RISK_ASSESSMENT", assessment.toString());

        // 7. Cryptographic integrity + sandbox inspection for every packet
        boolean anyQuarantined = false;
        for (Packet packet : session.getPackets()) {
            boolean safe = defenseProcessor.inspectAndDefend(packet);
            if (!safe) {
                anyQuarantined = true;
                loggingProcessor.log(session.getSessionId(), "PACKET_QUARANTINED",
                        packet.getPacketId() + ": " + defenseProcessor.getQuarantineService().getReason(packet.getPacketId()));
            }
        }

        if (anyQuarantined) {
            session.setStatus(TransferStatus.QUARANTINED);
            agenticDecisionProcessor.recordIncident(userId);
            loggingProcessor.log(session.getSessionId(), "TRANSFER_QUARANTINED", "One or more packets failed defense inspection");
            return new TransferOutcome(session, assessment);
        }

        // 8. Apply the agentic decision processor's autonomous action
        switch (assessment.getAction()) {
            case SUSPEND_TRANSFER -> {
                session.setStatus(TransferStatus.SUSPENDED);
                agenticDecisionProcessor.recordIncident(userId);
                loggingProcessor.log(session.getSessionId(), "TRANSFER_SUSPENDED", assessment.getRationale());
            }
            case QUARANTINE -> {
                session.getPackets().forEach(p -> defenseProcessor.getQuarantineService().quarantine(p, "Agentic decision processor: high risk score"));
                session.setStatus(TransferStatus.QUARANTINED);
                agenticDecisionProcessor.recordIncident(userId);
                loggingProcessor.log(session.getSessionId(), "TRANSFER_QUARANTINED", assessment.getRationale());
            }
            case REROUTE -> {
                Optional<CloudEndpoint> alternate = connectivityProcessor.selectOptimalEndpoint(session.getRoutedEndpointId());
                alternate.ifPresent(e -> session.setRoutedEndpointId(e.getId()));
                loggingProcessor.log(session.getSessionId(), "REROUTED", "New endpoint " + session.getRoutedEndpointId());
                completeTransfer(session);
            }
            case REGENERATE_ENCRYPTION_KEYS -> {
                SecretKey newKey = keyManager.generateSessionKey();
                session.setSessionKey(newKey);
                for (Packet packet : session.getPackets()) {
                    encryptionProcessingUnit.encryptAndSign(packet, newKey);
                }
                loggingProcessor.log(session.getSessionId(), "KEYS_REGENERATED", "New AES-256 session key issued");
                completeTransfer(session);
            }
            case REQUIRE_ADDITIONAL_AUTH -> {
                loggingProcessor.log(session.getSessionId(), "ADDITIONAL_AUTH_REQUIRED", assessment.getRationale());
                completeTransfer(session);
            }
            case NONE -> completeTransfer(session);
        }

        return new TransferOutcome(session, assessment);
    }

    private void completeTransfer(TransferSession session) {
        session.setStatus(TransferStatus.IN_TRANSIT);
        session.setStatus(TransferStatus.COMPLETED);
        loggingProcessor.log(session.getSessionId(), "TRANSFER_COMPLETED", "Delivered to endpoint " + session.getRoutedEndpointId());
    }

    private TransferSession rejectedSessionFor(FileTransferRequest request) {
        TransferSession session = new TransferSession("sess-rejected-" + request.getRequestId(), request, new byte[0]);
        session.setStatus(TransferStatus.REJECTED);
        return session;
    }

    public FileTransferCoordinationProcessor getCoordinationProcessor() { return coordinationProcessor; }
    public TransferLoggingProcessor getLoggingProcessor() { return loggingProcessor; }
    public AIAnalysisProcessor getAiAnalysisProcessor() { return aiAnalysisProcessor; }
    public AgenticDecisionProcessor getAgenticDecisionProcessor() { return agenticDecisionProcessor; }
    public MultiCloudConnectivityProcessor getConnectivityProcessor() { return connectivityProcessor; }
    public AuthenticationVerificationProcessor getAuthProcessor() { return authProcessor; }
}
