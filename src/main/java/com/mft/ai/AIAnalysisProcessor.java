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

package com.mft.ai;

import com.mft.model.AnomalyIndicator;
import com.mft.model.TelemetryEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Artificial intelligence analysis processor (patent element 110).
 *
 * <p>Constructs behavioral profiles from historical transfer logs and
 * continuously compares real-time telemetry against those profiles, using
 * a multivariate z-score (statistical anomaly-detection) technique to
 * generate anomaly indicators when observed behavior deviates from
 * baseline patterns beyond a configured threshold. Profiles are retrained
 * as new (legitimate) telemetry is observed, implementing the
 * specification's continuous-learning requirement.
 */
public final class AIAnalysisProcessor {

    /** Default deviation-score threshold above which telemetry is flagged anomalous. */
    public static final double DEFAULT_ANOMALY_THRESHOLD = 3.0;

    private final Map<String, BehavioralProfile> profiles = new ConcurrentHashMap<>();
    private final double anomalyThreshold;

    public AIAnalysisProcessor() {
        this(DEFAULT_ANOMALY_THRESHOLD);
    }

    public AIAnalysisProcessor(double anomalyThreshold) {
        this.anomalyThreshold = anomalyThreshold;
    }

    /** Trains (or retrains) the behavioral profile for a user from historical telemetry. */
    public void trainProfile(String userId, List<TelemetryEvent> history) {
        profiles.put(userId, BehavioralProfile.fromHistory(userId, history));
    }

    public boolean hasProfile(String userId) {
        return profiles.containsKey(userId);
    }

    /**
     * Analyzes a real-time telemetry sample against the user's stored
     * behavioral profile, returning an anomaly indicator with a composite
     * deviation score (the Euclidean norm of per-feature z-scores).
     */
    public AnomalyIndicator analyze(String userId, TelemetryEvent event) {
        BehavioralProfile profile = profiles.get(userId);
        if (profile == null) {
            // No baseline yet: treat conservatively as non-anomalous but flag low confidence.
            return new AnomalyIndicator(false, 0.0, "No behavioral profile established for user " + userId);
        }

        double[] features = event.toFeatureArray();
        double[] means = profile.getFeatureMeans();
        double[] stdDevs = profile.getFeatureStdDevs();

        double sumSquares = 0.0;
        StringBuilder deviatingFeatures = new StringBuilder();
        String[] featureNames = com.mft.model.TelemetryEvent.featureNames();
        for (int i = 0; i < features.length; i++) {
            double z = (features[i] - means[i]) / stdDevs[i];
            sumSquares += z * z;
            if (Math.abs(z) > anomalyThreshold && i < featureNames.length) {
                if (deviatingFeatures.length() > 0) deviatingFeatures.append(", ");
                deviatingFeatures.append(featureNames[i]).append(" (z=").append(String.format("%.2f", z)).append(")");
            }
        }
        double deviationScore = Math.sqrt(sumSquares);
        boolean anomalous = deviationScore > anomalyThreshold;

        String description = anomalous
                ? "Deviation score " + String.format("%.2f", deviationScore) + " exceeds threshold "
                    + anomalyThreshold + "; contributing features: " + deviatingFeatures
                : "Telemetry within expected behavioral range";

        return new AnomalyIndicator(anomalous, deviationScore, description);
    }

    public double getAnomalyThreshold() { return anomalyThreshold; }
}
