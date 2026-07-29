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
import com.mft.model.DefensiveAction;
import com.mft.model.RiskAssessment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Agentic decision processor (patent element 112).
 *
 * <p>Evaluates the risk level associated with a detected anomaly and
 * autonomously determines the appropriate defensive response action —
 * suspending the transfer, rerouting the transmission pathway, requiring
 * additional authentication, regenerating encryption keys, or quarantining
 * the payload — using a reinforcement-learning-based decision policy whose
 * action-value estimates are updated from historical response outcomes.
 */
public final class AgenticDecisionProcessor {

    private final RiskScorer riskScorer;
    private final ReinforcementLearningPolicy policy;
    private final Map<String, AtomicInteger> historicalIncidentCounts = new ConcurrentHashMap<>();

    public AgenticDecisionProcessor(RiskScorer riskScorer, ReinforcementLearningPolicy policy) {
        this.riskScorer = riskScorer;
        this.policy = policy;
    }

    /**
     * Produces a {@link RiskAssessment} for the given anomaly, selecting a
     * defensive action from the reinforcement-learning policy for the
     * resulting risk bucket.
     */
    public RiskAssessment assess(String userId, AnomalyIndicator anomaly, int failedAuthAttempts, boolean privilegedUser) {
        int incidents = historicalIncidentCounts.getOrDefault(userId, new AtomicInteger()).get();
        double score = riskScorer.score(anomaly, incidents, failedAuthAttempts, privilegedUser);
        RiskBucket bucket = RiskBucket.fromScore(score);
        DefensiveAction action = policy.selectAction(bucket);

        String rationale = "risk=%.3f bucket=%s anomaly=[%s] incidents=%d failedAuth=%d"
                .formatted(score, bucket, anomaly.getDescription(), incidents, failedAuthAttempts);

        return new RiskAssessment(score, action, rationale);
    }

    /** Records that an anomaly for this user turned out to be a genuine incident, feeding future risk scoring. */
    public void recordIncident(String userId) {
        historicalIncidentCounts.computeIfAbsent(userId, u -> new AtomicInteger()).incrementAndGet();
    }

    /**
     * Feeds an outcome reward back into the reinforcement-learning policy
     * for the (risk bucket, action) pair, allowing the agentic decision
     * processor to improve future action selection.
     */
    public void provideFeedback(double riskScore, DefensiveAction action, double reward) {
        policy.recordOutcome(RiskBucket.fromScore(riskScore), action, reward);
    }

    public ReinforcementLearningPolicy getPolicy() { return policy; }
}
