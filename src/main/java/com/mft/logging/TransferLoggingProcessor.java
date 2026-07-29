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

package com.mft.logging;

import com.mft.model.TransferLogEntry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Transfer logging processor referenced in the specification: generates
 * detailed transaction logs (packet transmission timestamps, encryption
 * parameters, authentication events, anomaly detection events, and threat
 * mitigation actions) for auditing, regulatory compliance, and machine
 * learning model retraining.
 *
 * <p>Entries are kept in memory (queryable per session) and, if a log
 * file path is configured, appended as JSON-lines to disk.
 */
public final class TransferLoggingProcessor {

    private final Map<String, List<TransferLogEntry>> entriesBySession = new ConcurrentHashMap<>();
    private final Path logFilePath;

    public TransferLoggingProcessor() {
        this(null);
    }

    public TransferLoggingProcessor(Path logFilePath) {
        this.logFilePath = logFilePath;
    }

    public void log(String sessionId, String eventType, String details) {
        TransferLogEntry entry = new TransferLogEntry(sessionId, eventType, details);
        entriesBySession.computeIfAbsent(sessionId, k -> new CopyOnWriteArrayList<>()).add(entry);
        if (logFilePath != null) {
            appendToFile(entry);
        }
    }

    public List<TransferLogEntry> getEntries(String sessionId) {
        return Collections.unmodifiableList(entriesBySession.getOrDefault(sessionId, new ArrayList<>()));
    }

    private void appendToFile(TransferLogEntry entry) {
        try {
            Files.writeString(logFilePath, entry.toJsonLine() + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to append transfer log entry to " + logFilePath, e);
        }
    }
}
