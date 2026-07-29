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

package com.mft;

import com.mft.agent.AgenticDecisionProcessor;
import com.mft.agent.ReinforcementLearningPolicy;
import com.mft.agent.RiskScorer;
import com.mft.ai.AIAnalysisProcessor;
import com.mft.auth.AuthenticationVerificationProcessor;
import com.mft.connectivity.MultiCloudConnectivityProcessor;
import com.mft.crypto.EncryptionProcessingUnit;
import com.mft.crypto.IntegrityValidator;
import com.mft.crypto.KeyManager;
import com.mft.defense.CyberThreatDefenseProcessor;
import com.mft.defense.QuarantineService;
import com.mft.defense.SandboxInspector;
import com.mft.logging.TransferLoggingProcessor;
import com.mft.model.CloudEndpoint;
import com.mft.model.CloudType;
import com.mft.orchestration.MftOrchestrator;
import com.mft.orchestration.TelemetryCollector;
import com.mft.transfer.FileTransferCoordinationProcessor;
import com.mft.transfer.SegmentationEngine;

/**
 * Convenience factory that wires together the computing device (102) and
 * its constituent processors (104, 106, 108, 110, 112, 114) into a fully
 * assembled {@link MftOrchestrator}. Used by the REST server, the CLI demo,
 * and the test suite so wiring is defined in exactly one place.
 */
public final class Bootstrap {

    private Bootstrap() {}

    public static MftOrchestrator buildOrchestrator() {
        KeyManager keyManager = new KeyManager();
        AuthenticationVerificationProcessor authProcessor = new AuthenticationVerificationProcessor();
        FileTransferCoordinationProcessor coordinationProcessor =
                new FileTransferCoordinationProcessor(new IntegrityValidator(), new SegmentationEngine());
        EncryptionProcessingUnit encryptionProcessingUnit = new EncryptionProcessingUnit(keyManager);
        MultiCloudConnectivityProcessor connectivityProcessor = new MultiCloudConnectivityProcessor();
        AIAnalysisProcessor aiAnalysisProcessor = new AIAnalysisProcessor();
        AgenticDecisionProcessor agenticDecisionProcessor =
                new AgenticDecisionProcessor(new RiskScorer(), new ReinforcementLearningPolicy());
        CyberThreatDefenseProcessor defenseProcessor =
                new CyberThreatDefenseProcessor(encryptionProcessingUnit, new SandboxInspector(), new QuarantineService());
        TransferLoggingProcessor loggingProcessor = new TransferLoggingProcessor();
        TelemetryCollector telemetryCollector = new TelemetryCollector();

        registerDemoCloudEndpoints(connectivityProcessor);

        return new MftOrchestrator(authProcessor, coordinationProcessor, encryptionProcessingUnit, keyManager,
                connectivityProcessor, aiAnalysisProcessor, agenticDecisionProcessor, defenseProcessor,
                loggingProcessor, telemetryCollector);
    }

    /** Registers a representative set of public/private/hybrid cloud endpoints for demo purposes. */
    public static void registerDemoCloudEndpoints(MultiCloudConnectivityProcessor connectivityProcessor) {
        connectivityProcessor.registerEndpoint(new CloudEndpoint("aws-us-east-1", "AWS US-East-1", CloudType.PUBLIC, 18.0, 950.0, true, true));
        connectivityProcessor.registerEndpoint(new CloudEndpoint("azure-west-europe", "Azure West Europe", CloudType.PUBLIC, 32.0, 800.0, true, true));
        connectivityProcessor.registerEndpoint(new CloudEndpoint("gcp-asia-southeast1", "GCP Asia Southeast 1", CloudType.PUBLIC, 45.0, 700.0, true, true));
        connectivityProcessor.registerEndpoint(new CloudEndpoint("corp-private-dc1", "Corporate Private DC1", CloudType.PRIVATE, 5.0, 500.0, true, true));
        connectivityProcessor.registerEndpoint(new CloudEndpoint("hybrid-edge-1", "Hybrid Edge Node 1", CloudType.HYBRID, 12.0, 300.0, true, true));
    }
}
