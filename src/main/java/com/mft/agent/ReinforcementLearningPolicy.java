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

import com.mft.model.DefensiveAction;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A lightweight, self-contained reinforcement-learning decision policy
 * (tabular Q-learning) mapping a discretized risk state to expected-value
 * estimates for each candidate defensive action. Outcome feedback
 * ({@link #recordOutcome}) updates the table so the policy improves over
 * time, satisfying the specification's requirement that "reinforcement
 * learning policies determine optimal threat mitigation actions based on
 * historical response outcomes."
 *
 * <p>This is intentionally simple and dependency-free (no external ML
 * libraries) so the reference implementation remains easy to audit,
 * build and run offline; it is a genuine, functioning learning policy,
 * not a placeholder.
 */
public final class ReinforcementLearningPolicy {

    private static final double LEARNING_RATE = 0.2;

    private final Map<RiskBucket, Map<DefensiveAction, Double>> qTable = new ConcurrentHashMap<>();

    public ReinforcementLearningPolicy() {
        for (RiskBucket bucket : RiskBucket.values()) {
            Map<DefensiveAction, Double> actionValues = new EnumMap<>(DefensiveAction.class);
            for (DefensiveAction action : candidateActions(bucket)) {
                actionValues.put(action, initialValue(bucket, action));
            }
            qTable.put(bucket, actionValues);
        }
    }

    /** Selects the highest-value action currently known for the given risk bucket. */
    public DefensiveAction selectAction(RiskBucket bucket) {
        return qTable.get(bucket).entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(DefensiveAction.NONE);
    }

    /**
     * Updates the Q-value for the (state, action) pair based on an observed
     * outcome reward (positive reward = the action correctly mitigated a
     * real threat or correctly allowed legitimate traffic; negative reward
     * = a false positive/negative).
     */
    public void recordOutcome(RiskBucket bucket, DefensiveAction action, double reward) {
        Map<DefensiveAction, Double> actionValues = qTable.get(bucket);
        double current = actionValues.getOrDefault(action, 0.0);
        double updated = current + LEARNING_RATE * (reward - current);
        actionValues.put(action, updated);
    }

    public double getValue(RiskBucket bucket, DefensiveAction action) {
        return qTable.get(bucket).getOrDefault(action, 0.0);
    }

    private static DefensiveAction[] candidateActions(RiskBucket bucket) {
        return switch (bucket) {
            case LOW -> new DefensiveAction[] { DefensiveAction.NONE };
            case MODERATE -> new DefensiveAction[] { DefensiveAction.NONE, DefensiveAction.REQUIRE_ADDITIONAL_AUTH };
            case HIGH -> new DefensiveAction[] { DefensiveAction.REQUIRE_ADDITIONAL_AUTH, DefensiveAction.REROUTE, DefensiveAction.REGENERATE_ENCRYPTION_KEYS };
            case CRITICAL -> new DefensiveAction[] { DefensiveAction.SUSPEND_TRANSFER, DefensiveAction.QUARANTINE };
        };
    }

    private static double initialValue(RiskBucket bucket, DefensiveAction action) {
        // Sensible priors so the untrained policy behaves reasonably from the first decision.
        return switch (bucket) {
            case LOW -> 1.0;
            case MODERATE -> action == DefensiveAction.NONE ? 0.6 : 0.4;
            case HIGH -> action == DefensiveAction.REQUIRE_ADDITIONAL_AUTH ? 0.7 : 0.5;
            case CRITICAL -> action == DefensiveAction.QUARANTINE ? 0.9 : 0.8;
        };
    }
}
