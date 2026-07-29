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
 * Result of the agentic decision processor's risk evaluation: a numeric
 * risk score in [0, 1] together with the chosen defensive action and a
 * human-readable explanation of the contributing factors.
 */
public final class RiskAssessment {

    private final double riskScore;
    private final DefensiveAction action;
    private final String rationale;

    public RiskAssessment(double riskScore, DefensiveAction action, String rationale) {
        this.riskScore = riskScore;
        this.action = action;
        this.rationale = rationale;
    }

    public double getRiskScore() { return riskScore; }
    public DefensiveAction getAction() { return action; }
    public String getRationale() { return rationale; }

    @Override
    public String toString() {
        return "RiskAssessment{riskScore=%.3f, action=%s, rationale='%s'}"
                .formatted(riskScore, action, rationale);
    }
}
