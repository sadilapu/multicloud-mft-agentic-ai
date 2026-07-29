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

import com.mft.ai.AIAnalysisProcessor;
import com.mft.model.TelemetryEvent;
import com.mft.orchestration.MftOrchestrator;
import com.mft.server.MftHttpServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * CLI / process entry point: assembles the multi-cloud managed file
 * transfer system and starts its REST API on the configured port
 * (default 8080, override with the {@code PORT} environment variable or a
 * single CLI argument).
 */
public final class Main {

    public static void main(String[] args) throws Exception {
        int port = resolvePort(args);

        MftOrchestrator orchestrator = Bootstrap.buildOrchestrator();
        seedDemoData(orchestrator);

        MftHttpServer server = new MftHttpServer(orchestrator, port);
        server.start();

        System.out.println("=================================================================");
        System.out.println(" Multi-Cloud Managed File Transfer System — AI/ML Cyber Defense");
        System.out.println(" Listening on http://localhost:" + server.getPort());
        System.out.println();
        System.out.println(" Demo credentials: user=demo-user token=demo-token");
        System.out.println(" Try:");
        System.out.println("   curl http://localhost:" + server.getPort() + "/api/v1/health");
        System.out.println("=================================================================");
    }

    private static int resolvePort(String[] args) {
        if (args.length > 0) {
            return Integer.parseInt(args[0]);
        }
        String envPort = System.getenv("PORT");
        return envPort != null ? Integer.parseInt(envPort) : 8080;
    }

    /** Registers a demo user and trains a baseline AI behavioral profile so first-time demo calls behave sensibly. */
    private static void seedDemoData(MftOrchestrator orchestrator) {
        orchestrator.getAuthProcessor().registerUserToken("demo-user", "demo-token");

        Random random = new Random(42);
        List<TelemetryEvent> history = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            history.add(new TelemetryEvent(
                    1 + random.nextInt(3),
                    32_000 + random.nextInt(16_000),
                    0.85 + random.nextDouble() * 0.15,
                    0));
        }
        orchestrator.getAiAnalysisProcessor().trainProfile("demo-user", history);
    }
}
