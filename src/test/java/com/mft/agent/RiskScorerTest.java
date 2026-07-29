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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskScorerTest {

    private final RiskScorer scorer = new RiskScorer();

    @Test
    void nonAnomalousLowHistoryProducesLowRisk() {
        AnomalyIndicator benign = new AnomalyIndicator(false, 0.1, "normal");
        double score = scorer.score(benign, 0, 0, false);
        assertTrue(score < 0.25, "expected low risk score, got " + score);
    }

    @Test
    void highDeviationAndFailedAuthProducesHighRisk() {
        AnomalyIndicator severe = new AnomalyIndicator(true, 12.0, "severe deviation");
        double score = scorer.score(severe, 4, 5, false);
        assertTrue(score > 0.7, "expected high risk score, got " + score);
    }

    @Test
    void scoreIsAlwaysClampedToUnitInterval() {
        AnomalyIndicator extreme = new AnomalyIndicator(true, 1000.0, "extreme");
        double score = scorer.score(extreme, 100, 100, true);
        assertTrue(score >= 0.0 && score <= 1.0);
    }
}
