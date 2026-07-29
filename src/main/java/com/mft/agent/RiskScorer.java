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

import com.mft.model.AnomalyIndicator;

/**
 * Computes a composite threat risk score in [0, 1] by combining the AI
 * analysis processor's deviation score with the current user's historical
 * incident count and authorization level, as described in the
 * specification's risk-assessment procedure.
 */
public final class RiskScorer {

    public double score(AnomalyIndicator anomaly, int historicalIncidentCount, int failedAuthAttempts, boolean privilegedUser) {
        double deviationComponent = clamp(anomaly.getDeviationScore() / 10.0, 0, 1) * 0.55;
        double historyComponent = clamp(historicalIncidentCount / 5.0, 0, 1) * 0.2;
        double authComponent = clamp(failedAuthAttempts / 5.0, 0, 1) * 0.2;
        double privilegeComponent = privilegedUser ? 0.05 : 0.0;
        return clamp(deviationComponent + historyComponent + authComponent + privilegeComponent, 0, 1);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
