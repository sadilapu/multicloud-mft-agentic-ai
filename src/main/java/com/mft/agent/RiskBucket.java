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

package com.mft.agent;

/** Discretized risk levels used as states in the reinforcement-learning policy table. */
public enum RiskBucket {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL;

    public static RiskBucket fromScore(double score) {
        if (score < 0.25) return LOW;
        if (score < 0.5) return MODERATE;
        if (score < 0.8) return HIGH;
        return CRITICAL;
    }
}
