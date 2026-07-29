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
import java.util.Objects;
import java.util.UUID;

/**
 * Incoming request to transfer a file from a source computing system to a
 * destination cloud infrastructure. Carries file metadata, source
 * identification information, authentication credentials, and the
 * destination cloud endpoint identifier, as described in the specification.
 */
public final class FileTransferRequest {

    private final String requestId;
    private final String fileName;
    private final byte[] fileContent;
    private final String sourceUserId;
    private final String authToken;
    private final String destinationEndpointId;
    private final Instant submittedAt;

    public FileTransferRequest(String fileName, byte[] fileContent, String sourceUserId,
                                String authToken, String destinationEndpointId) {
        this.requestId = "req-" + UUID.randomUUID();
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        this.fileContent = Objects.requireNonNull(fileContent, "fileContent");
        this.sourceUserId = Objects.requireNonNull(sourceUserId, "sourceUserId");
        this.authToken = Objects.requireNonNull(authToken, "authToken");
        this.destinationEndpointId = Objects.requireNonNull(destinationEndpointId, "destinationEndpointId");
        this.submittedAt = Instant.now();
    }

    public String getRequestId() { return requestId; }
    public String getFileName() { return fileName; }
    public byte[] getFileContent() { return fileContent; }
    public String getSourceUserId() { return sourceUserId; }
    public String getAuthToken() { return authToken; }
    public String getDestinationEndpointId() { return destinationEndpointId; }
    public Instant getSubmittedAt() { return submittedAt; }
}
