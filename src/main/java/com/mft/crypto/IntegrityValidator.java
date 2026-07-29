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

package com.mft.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Computes the baseline cryptographic hash value for an entire received
 * file, used as a reference for subsequent end-to-end integrity
 * verification once the file is reconstructed at the destination.
 */
public final class IntegrityValidator {

    public byte[] computeBaselineHash(byte[] fileContent) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(fileContent);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public boolean matches(byte[] fileContent, byte[] baselineHash) {
        return MessageDigest.isEqual(computeBaselineHash(fileContent), baselineHash);
    }
}
