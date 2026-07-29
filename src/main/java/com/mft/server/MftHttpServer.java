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

package com.mft.server;

import com.mft.model.FileTransferRequest;
import com.mft.model.Packet;
import com.mft.model.TransferLogEntry;
import com.mft.orchestration.MftOrchestrator;
import com.mft.orchestration.TransferOutcome;
import com.mft.transfer.FileTransferCoordinationProcessor.NoSuchSessionException;
import com.mft.transfer.TransferSession;
import com.mft.util.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Minimal, dependency-free embedded REST API exposing the multi-cloud
 * managed file transfer system over HTTP, built on the JDK's built-in
 * {@link HttpServer} so the reference implementation requires no external
 * web framework to build or run.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code GET  /api/v1/health} — liveness check.</li>
 *   <li>{@code POST /api/v1/transfers} — submit a new file transfer request.</li>
 *   <li>{@code GET  /api/v1/transfers/{sessionId}} — retrieve transfer status.</li>
 *   <li>{@code GET  /api/v1/transfers/{sessionId}/logs} — retrieve the transaction log for a transfer.</li>
 * </ul>
 */
public final class MftHttpServer {

    private final MftOrchestrator orchestrator;
    private final HttpServer httpServer;

    public MftHttpServer(MftOrchestrator orchestrator, int port) throws IOException {
        this.orchestrator = orchestrator;
        this.httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        this.httpServer.setExecutor(Executors.newCachedThreadPool());
        registerRoutes();
    }

    private void registerRoutes() {
        httpServer.createContext("/api/v1/health", this::handleHealth);
        httpServer.createContext("/api/v1/transfers", this::handleTransfers);
    }

    public void start() { httpServer.start(); }
    public void stop() { httpServer.stop(0); }
    public int getPort() { return httpServer.getAddress().getPort(); }

    private void handleHealth(HttpExchange exchange) throws IOException {
        String body = Json.object().put("status", "UP").build();
        writeJson(exchange, 200, body);
    }

    private void handleTransfers(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("POST".equalsIgnoreCase(method) && path.equals("/api/v1/transfers")) {
                handleSubmitTransfer(exchange);
            } else if ("GET".equalsIgnoreCase(method) && path.matches("/api/v1/transfers/[^/]+/logs")) {
                handleGetLogs(exchange, extractSessionId(path, "/logs"));
            } else if ("GET".equalsIgnoreCase(method) && path.matches("/api/v1/transfers/[^/]+")) {
                handleGetStatus(exchange, extractSessionId(path, ""));
            } else {
                writeJson(exchange, 404, Json.object().put("error", "Not found").build());
            }
        } catch (NoSuchSessionException e) {
            writeJson(exchange, 404, Json.object().put("error", e.getMessage()).build());
        } catch (IllegalArgumentException e) {
            writeJson(exchange, 400, Json.object().put("error", e.getMessage()).build());
        } catch (Exception e) {
            writeJson(exchange, 500, Json.object().put("error", "Internal error: " + e.getMessage()).build());
        }
    }

    private void handleSubmitTransfer(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        String fileName = required(Json.extractString(requestBody, "fileName"), "fileName");
        String fileContentBase64 = required(Json.extractString(requestBody, "fileContentBase64"), "fileContentBase64");
        String sourceUserId = required(Json.extractString(requestBody, "sourceUserId"), "sourceUserId");
        String authToken = required(Json.extractString(requestBody, "authToken"), "authToken");
        String destinationEndpointId = required(Json.extractString(requestBody, "destinationEndpointId"), "destinationEndpointId");

        Boolean privileged = Json.extractBoolean(requestBody, "privilegedUser");
        Double reputation = Json.extractNumber(requestBody, "sourceReputationScore");

        byte[] fileContent = Base64.getDecoder().decode(fileContentBase64);
        FileTransferRequest request = new FileTransferRequest(fileName, fileContent, sourceUserId, authToken, destinationEndpointId);

        TransferOutcome outcome = (reputation != null)
                ? orchestrator.processTransfer(request, Boolean.TRUE.equals(privileged), reputation)
                : orchestrator.processTransfer(request, Boolean.TRUE.equals(privileged));

        writeJson(exchange, 201, renderOutcome(outcome));
    }

    private void handleGetStatus(HttpExchange exchange, String sessionId) throws IOException {
        TransferSession session = orchestrator.getCoordinationProcessor().getSession(sessionId);
        writeJson(exchange, 200, renderSession(session));
    }

    private void handleGetLogs(HttpExchange exchange, String sessionId) throws IOException {
        List<TransferLogEntry> entries = orchestrator.getLoggingProcessor().getEntries(sessionId);
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(entries.get(i).toJsonLine());
        }
        sb.append("]");
        writeJson(exchange, 200, sb.toString());
    }

    private String renderOutcome(TransferOutcome outcome) {
        return Json.object()
                .put("sessionId", outcome.getSessionId())
                .put("status", outcome.getStatus().name())
                .put("riskScore", outcome.getRiskAssessment().getRiskScore())
                .put("action", outcome.getRiskAssessment().getAction().name())
                .put("packetCount", outcome.getSession().getPackets().size())
                .put("routedEndpointId", outcome.getSession().getRoutedEndpointId())
                .build();
    }

    private String renderSession(TransferSession session) {
        long quarantinedCount = session.getPackets().stream().filter(Packet::isQuarantined).count();
        return Json.object()
                .put("sessionId", session.getSessionId())
                .put("status", session.getStatus().name())
                .put("fileName", session.getRequest().getFileName())
                .put("packetCount", session.getPackets().size())
                .put("quarantinedPacketCount", quarantinedCount)
                .put("routedEndpointId", session.getRoutedEndpointId())
                .build();
    }

    private static String extractSessionId(String path, String suffix) {
        String withoutPrefix = path.substring("/api/v1/transfers/".length());
        if (!suffix.isEmpty() && withoutPrefix.endsWith(suffix)) {
            withoutPrefix = withoutPrefix.substring(0, withoutPrefix.length() - suffix.length());
        }
        return withoutPrefix;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + field);
        }
        return value;
    }

    private static void writeJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
