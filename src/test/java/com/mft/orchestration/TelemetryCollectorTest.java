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

package com.mft.orchestration;

import com.mft.model.TelemetryEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the destination-address-pattern tracking required by Claim 4 of
 * the specification ("behavioral profiles comprise parameters including
 * ... destination address patterns").
 */
class TelemetryCollectorTest {

    @Test
    void firstTransferForAUserGetsFullDestinationConsistency() {
        TelemetryCollector collector = new TelemetryCollector();
        TelemetryEvent event = collector.recordAndBuild("alice", 1000, 1.0, 0, "aws-us-east-1");
        assertEquals(1.0, event.getDestinationConsistencyScore(), 1e-9);
    }

    @Test
    void consistentDestinationKeepsHighConsistencyScore() {
        TelemetryCollector collector = new TelemetryCollector();
        for (int i = 0; i < 10; i++) {
            collector.recordAndBuild("bob", 1000, 1.0, 0, "aws-us-east-1");
        }
        TelemetryEvent event = collector.recordAndBuild("bob", 1000, 1.0, 0, "aws-us-east-1");
        assertTrue(event.getDestinationConsistencyScore() > 0.9);
    }

    @Test
    void novelDestinationLowersConsistencyScore() {
        TelemetryCollector collector = new TelemetryCollector();
        for (int i = 0; i < 20; i++) {
            collector.recordAndBuild("carol", 1000, 1.0, 0, "aws-us-east-1");
        }
        // A sudden transfer to a completely different, never-before-used destination
        // (e.g. potential data exfiltration to an unauthorized endpoint).
        TelemetryEvent event = collector.recordAndBuild("carol", 1000, 1.0, 0, "unknown-external-endpoint");
        assertTrue(event.getDestinationConsistencyScore() < 0.1,
                "expected low consistency for a novel destination, got " + event.getDestinationConsistencyScore());
    }

    @Test
    void frequencyIncreasesAcrossCallsWithinWindow() {
        TelemetryCollector collector = new TelemetryCollector();
        TelemetryEvent first = collector.recordAndBuild("dave", 500, 1.0, 0, "endpoint-1");
        TelemetryEvent second = collector.recordAndBuild("dave", 500, 1.0, 0, "endpoint-1");
        assertTrue(second.getTransferFrequency() > first.getTransferFrequency());
    }
}
