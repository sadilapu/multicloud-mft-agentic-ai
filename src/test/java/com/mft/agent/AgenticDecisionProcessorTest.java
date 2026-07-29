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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgenticDecisionProcessorTest {

    private AgenticDecisionProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new AgenticDecisionProcessor(new RiskScorer(), new ReinforcementLearningPolicy());
    }

    @Test
    void benignTelemetryResultsInNoAction() {
        AnomalyIndicator benign = new AnomalyIndicator(false, 0.2, "normal");
        RiskAssessment assessment = processor.assess("alice", benign, 0, false);
        assertEquals(DefensiveAction.NONE, assessment.getAction());
    }

    @Test
    void severeAnomalyWithPriorIncidentsResultsInStrongDefensiveAction() {
        AnomalyIndicator severe = new AnomalyIndicator(true, 15.0, "critical deviation");
        // A user with a history of confirmed incidents pushes the composite risk score
        // into the CRITICAL bucket even before this specific anomaly is weighed.
        processor.recordIncident("mallory");
        processor.recordIncident("mallory");
        processor.recordIncident("mallory");

        RiskAssessment assessment = processor.assess("mallory", severe, 5, false);

        assertTrue(assessment.getRiskScore() >= 0.8, "expected CRITICAL-bucket risk score, got " + assessment.getRiskScore());
        assertTrue(assessment.getAction() == DefensiveAction.QUARANTINE
                || assessment.getAction() == DefensiveAction.SUSPEND_TRANSFER);
    }

    @Test
    void severeAnomalyWithoutHistoryResultsInHighRiskMitigation() {
        AnomalyIndicator severe = new AnomalyIndicator(true, 15.0, "severe deviation");
        RiskAssessment assessment = processor.assess("first-time-user", severe, 5, false);

        // Without a prior incident history, a single severe anomaly lands in the HIGH
        // (not yet CRITICAL) bucket, which selects a proportionate response.
        assertTrue(assessment.getAction() == DefensiveAction.REQUIRE_ADDITIONAL_AUTH
                || assessment.getAction() == DefensiveAction.REROUTE
                || assessment.getAction() == DefensiveAction.REGENERATE_ENCRYPTION_KEYS);
    }

    @Test
    void recordingIncidentsIncreasesFutureRiskScores() {
        AnomalyIndicator moderate = new AnomalyIndicator(true, 3.5, "moderate deviation");
        RiskAssessment first = processor.assess("repeat-offender", moderate, 0, false);
        processor.recordIncident("repeat-offender");
        processor.recordIncident("repeat-offender");
        RiskAssessment second = processor.assess("repeat-offender", moderate, 0, false);

        assertTrue(second.getRiskScore() >= first.getRiskScore());
    }

    @Test
    void feedbackIsForwardedToPolicy() {
        processor.provideFeedback(0.9, DefensiveAction.QUARANTINE, 1.0);
        double value = processor.getPolicy().getValue(RiskBucket.CRITICAL, DefensiveAction.QUARANTINE);
        assertTrue(value > 0);
    }
}
