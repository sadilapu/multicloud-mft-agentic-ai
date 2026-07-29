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
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AIAnalysisProcessorTest {

    @Test
    void returnsNonAnomalousWhenNoProfileTrained() {
        AIAnalysisProcessor processor = new AIAnalysisProcessor();
        AnomalyIndicator indicator = processor.analyze("unknown-user", new TelemetryEvent(1, 1000, 1.0, 0));
        assertFalse(indicator.isAnomalous());
    }

    @Test
    void flagsTelemetryFarOutsideBaselineAsAnomalous() {
        AIAnalysisProcessor processor = new AIAnalysisProcessor();
        List<TelemetryEvent> history = baselineHistory(60, new Random(1));
        processor.trainProfile("bob", history);

        // Massive spike in transfer frequency and packet size, far beyond training range.
        TelemetryEvent suspicious = new TelemetryEvent(500, 50_000_000, 0.05, 6);
        AnomalyIndicator indicator = processor.analyze("bob", suspicious);

        assertTrue(indicator.isAnomalous());
        assertTrue(indicator.getDeviationScore() > processor.getAnomalyThreshold());
    }

    @Test
    void treatsTypicalTelemetryAsNonAnomalous() {
        AIAnalysisProcessor processor = new AIAnalysisProcessor();
        Random random = new Random(2);
        List<TelemetryEvent> history = baselineHistory(60, random);
        processor.trainProfile("carol", history);

        TelemetryEvent typical = new TelemetryEvent(2, 32_000, 0.9, 0);
        AnomalyIndicator indicator = processor.analyze("carol", typical);

        assertFalse(indicator.isAnomalous());
    }

    @Test
    void profilesAreIndependentPerUser() {
        AIAnalysisProcessor processor = new AIAnalysisProcessor();
        processor.trainProfile("dave", baselineHistory(30, new Random(3)));
        assertTrue(processor.hasProfile("dave"));
        assertFalse(processor.hasProfile("erin"));
    }

    private static List<TelemetryEvent> baselineHistory(int count, Random random) {
        List<TelemetryEvent> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            events.add(new TelemetryEvent(1 + random.nextInt(3), 30_000 + random.nextInt(5_000), 0.85 + random.nextDouble() * 0.15, 0));
        }
        return events;
    }
}
