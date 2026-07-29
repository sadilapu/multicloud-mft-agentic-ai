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

import com.mft.model.TelemetryEvent;

import java.util.List;

/**
 * Baseline behavioral profile for a user, built by the AI analysis
 * processor from historical transfer telemetry: per-feature mean and
 * standard deviation used to compute deviation (z-)scores for newly
 * observed telemetry.
 */
public final class BehavioralProfile {

    private final String userId;
    private final double[] featureMeans;
    private final double[] featureStdDevs;
    private final int sampleCount;

    private BehavioralProfile(String userId, double[] featureMeans, double[] featureStdDevs, int sampleCount) {
        this.userId = userId;
        this.featureMeans = featureMeans;
        this.featureStdDevs = featureStdDevs;
        this.sampleCount = sampleCount;
    }

    /**
     * Builds a behavioral profile from a list of historical telemetry
     * samples representing legitimate, authorized transfer activity.
     */
    public static BehavioralProfile fromHistory(String userId, List<TelemetryEvent> history) {
        if (history.isEmpty()) {
            throw new IllegalArgumentException("Cannot build a behavioral profile from empty history");
        }
        int featureCount = history.get(0).toFeatureArray().length;
        double[] means = new double[featureCount];
        double[] stdDevs = new double[featureCount];

        for (TelemetryEvent event : history) {
            double[] features = event.toFeatureArray();
            for (int i = 0; i < featureCount; i++) {
                means[i] += features[i];
            }
        }
        for (int i = 0; i < featureCount; i++) {
            means[i] /= history.size();
        }

        for (TelemetryEvent event : history) {
            double[] features = event.toFeatureArray();
            for (int i = 0; i < featureCount; i++) {
                double diff = features[i] - means[i];
                stdDevs[i] += diff * diff;
            }
        }
        for (int i = 0; i < featureCount; i++) {
            stdDevs[i] = Math.sqrt(stdDevs[i] / Math.max(1, history.size() - 1));
            // Avoid division by zero for near-constant historical features.
            if (stdDevs[i] < 1e-6) {
                stdDevs[i] = 1e-6;
            }
        }
        return new BehavioralProfile(userId, means, stdDevs, history.size());
    }

    public String getUserId() { return userId; }
    public double[] getFeatureMeans() { return featureMeans; }
    public double[] getFeatureStdDevs() { return featureStdDevs; }
    public int getSampleCount() { return sampleCount; }
}
