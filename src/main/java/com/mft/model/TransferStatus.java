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

/**
 * Lifecycle states of a managed file transfer session as it moves through
 * the coordination, encryption, routing, AI-analysis and defense stages.
 */
public enum TransferStatus {
    RECEIVED,
    AUTHENTICATED,
    INTEGRITY_VALIDATED,
    SEGMENTED,
    ENCRYPTED,
    ROUTED,
    IN_TRANSIT,
    COMPLETED,
    SUSPENDED,
    QUARANTINED,
    REJECTED
}
