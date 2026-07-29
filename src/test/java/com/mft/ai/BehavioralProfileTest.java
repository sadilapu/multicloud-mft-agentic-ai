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
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BehavioralProfileTest {

    @Test
    void computesMeanAndStdDevFromHistory() {
        List<TelemetryEvent> history = List.of(
                new TelemetryEvent(1, 100, 1.0, 0),
                new TelemetryEvent(3, 100, 1.0, 0),
                new TelemetryEvent(2, 100, 1.0, 0));

        BehavioralProfile profile = BehavioralProfile.fromHistory("user", history);

        assertEquals(2.0, profile.getFeatureMeans()[0], 1e-9);
        assertEquals(3, profile.getSampleCount());
    }

    @Test
    void rejectsEmptyHistory() {
        assertThrows(IllegalArgumentException.class, () -> BehavioralProfile.fromHistory("user", List.of()));
    }

    @Test
    void avoidsZeroStdDevForConstantFeature() {
        List<TelemetryEvent> history = List.of(
                new TelemetryEvent(1, 100, 1.0, 0),
                new TelemetryEvent(1, 100, 1.0, 0));
        BehavioralProfile profile = BehavioralProfile.fromHistory("user", history);
        for (double stdDev : profile.getFeatureStdDevs()) {
            assertTrue(stdDev > 0);
        }
    }
}
