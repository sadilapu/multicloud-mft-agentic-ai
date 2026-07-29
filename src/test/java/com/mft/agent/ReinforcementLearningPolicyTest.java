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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReinforcementLearningPolicyTest {

    @Test
    void lowRiskSelectsNoAction() {
        ReinforcementLearningPolicy policy = new ReinforcementLearningPolicy();
        assertEquals(DefensiveAction.NONE, policy.selectAction(RiskBucket.LOW));
    }

    @Test
    void criticalRiskPrefersQuarantineByDefault() {
        ReinforcementLearningPolicy policy = new ReinforcementLearningPolicy();
        assertEquals(DefensiveAction.QUARANTINE, policy.selectAction(RiskBucket.CRITICAL));
    }

    @Test
    void positiveFeedbackIncreasesActionValue() {
        ReinforcementLearningPolicy policy = new ReinforcementLearningPolicy();
        double before = policy.getValue(RiskBucket.HIGH, DefensiveAction.REROUTE);

        for (int i = 0; i < 10; i++) {
            policy.recordOutcome(RiskBucket.HIGH, DefensiveAction.REROUTE, 1.0);
        }

        double after = policy.getValue(RiskBucket.HIGH, DefensiveAction.REROUTE);
        assertTrue(after > before, "repeated positive reward should raise the action's estimated value");
    }

    @Test
    void repeatedPositiveFeedbackCanChangeSelectedAction() {
        ReinforcementLearningPolicy policy = new ReinforcementLearningPolicy();
        // Reinforce REROUTE heavily for HIGH risk until it becomes preferred.
        for (int i = 0; i < 20; i++) {
            policy.recordOutcome(RiskBucket.HIGH, DefensiveAction.REROUTE, 1.0);
            policy.recordOutcome(RiskBucket.HIGH, DefensiveAction.REQUIRE_ADDITIONAL_AUTH, -1.0);
            policy.recordOutcome(RiskBucket.HIGH, DefensiveAction.REGENERATE_ENCRYPTION_KEYS, -1.0);
        }
        assertEquals(DefensiveAction.REROUTE, policy.selectAction(RiskBucket.HIGH));
    }
}
