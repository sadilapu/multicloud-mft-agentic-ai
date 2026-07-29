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
 * Output of the artificial intelligence analysis processor when observed
 * telemetry deviates from a user's baseline behavioral profile beyond a
 * configured threshold.
 */
public final class AnomalyIndicator {

    private final boolean anomalous;
    private final double deviationScore;
    private final String description;

    public AnomalyIndicator(boolean anomalous, double deviationScore, String description) {
        this.anomalous = anomalous;
        this.deviationScore = deviationScore;
        this.description = description;
    }

    public boolean isAnomalous() { return anomalous; }
    public double getDeviationScore() { return deviationScore; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return "AnomalyIndicator{anomalous=%s, deviationScore=%.3f, description='%s'}"
                .formatted(anomalous, deviationScore, description);
    }
}
