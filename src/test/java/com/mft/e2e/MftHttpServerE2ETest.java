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

package com.mft.e2e;

import com.mft.Bootstrap;
import com.mft.orchestration.MftOrchestrator;
import com.mft.server.MftHttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * True end-to-end test: starts the real embedded HTTP server on an
 * ephemeral local port, then drives it with genuine HTTP requests over a
 * real socket via {@link HttpClient} — no mocking of the server or
 * orchestrator. Validates the complete request lifecycle a real API
 * consumer would experience: health check, transfer submission,
 * status retrieval, and log retrieval.
 */
class MftHttpServerE2ETest {

    private MftHttpServer server;
    private HttpClient client;
    private String baseUrl;

    @BeforeEach
    void startServer() throws Exception {
        MftOrchestrator orchestrator = Bootstrap.buildOrchestrator();
        orchestrator.getAuthProcessor().registerUserToken("e2e-user", "e2e-token");

        server = new MftHttpServer(orchestrator, 0); // ephemeral port
        server.start();
        baseUrl = "http://localhost:" + server.getPort();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void healthEndpointReportsUp() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/health")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"UP\""));
    }

    @Test
    void submitTransferThenRetrieveStatusAndLogsOverRealHttp() throws Exception {
        String contentBase64 = Base64.getEncoder().encodeToString("end to end test payload".getBytes(StandardCharsets.UTF_8));
        String requestBody = """
                {"fileName":"e2e.txt","fileContentBase64":"%s","sourceUserId":"e2e-user","authToken":"e2e-token","destinationEndpointId":"aws-us-east-1"}
                """.formatted(contentBase64);

        HttpRequest submit = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/transfers"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> submitResponse = client.send(submit, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, submitResponse.statusCode());
        assertTrue(submitResponse.body().contains("\"status\":\"COMPLETED\""));

        String sessionId = extractField(submitResponse.body(), "sessionId");
        assertNotNull(sessionId);

        HttpRequest statusReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/transfers/" + sessionId)).GET().build();
        HttpResponse<String> statusResponse = client.send(statusReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, statusResponse.statusCode());
        assertTrue(statusResponse.body().contains(sessionId));

        HttpRequest logsReq = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/transfers/" + sessionId + "/logs")).GET().build();
        HttpResponse<String> logsResponse = client.send(logsReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, logsResponse.statusCode());
        assertTrue(logsResponse.body().contains("TRANSFER_COMPLETED"));
    }

    @Test
    void rejectsTransferWithInvalidCredentialsOverRealHttp() throws Exception {
        String contentBase64 = Base64.getEncoder().encodeToString("payload".getBytes(StandardCharsets.UTF_8));
        String requestBody = """
                {"fileName":"x.txt","fileContentBase64":"%s","sourceUserId":"e2e-user","authToken":"wrong","destinationEndpointId":"aws-us-east-1"}
                """.formatted(contentBase64);

        HttpRequest submit = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/transfers"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = client.send(submit, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"REJECTED\""));
    }

    @Test
    void returns404ForUnknownSession() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/transfers/does-not-exist")).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    void returns400ForMissingRequiredField() throws Exception {
        String requestBody = "{\"fileName\":\"x.txt\"}";
        HttpRequest submit = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v1/transfers"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        HttpResponse<String> response = client.send(submit, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
    }

    private static String extractField(String json, String field) {
        var matcher = java.util.regex.Pattern.compile("\"" + field + "\":\"([^\"]+)\"").matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }
}
