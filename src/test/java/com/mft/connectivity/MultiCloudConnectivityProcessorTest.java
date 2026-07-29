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

package com.mft.connectivity;

import com.mft.model.CloudEndpoint;
import com.mft.model.CloudType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MultiCloudConnectivityProcessorTest {

    private MultiCloudConnectivityProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new MultiCloudConnectivityProcessor();
    }

    @Test
    void registersAndRetrievesEndpoint() {
        CloudEndpoint endpoint = new CloudEndpoint("aws-1", "AWS 1", CloudType.PUBLIC, 20, 500, true, true);
        processor.registerEndpoint(endpoint);

        Optional<CloudEndpoint> retrieved = processor.getEndpoint("aws-1");
        assertTrue(retrieved.isPresent());
        assertEquals("AWS 1", retrieved.get().getName());
    }

    @Test
    void selectsLowestLatencyCompliantEndpoint() {
        processor.registerEndpoint(new CloudEndpoint("slow", "Slow DC", CloudType.PUBLIC, 200, 100, true, true));
        processor.registerEndpoint(new CloudEndpoint("fast", "Fast DC", CloudType.PRIVATE, 5, 100, true, true));

        Optional<CloudEndpoint> best = processor.selectOptimalEndpoint(null);
        assertTrue(best.isPresent());
        assertEquals("fast", best.get().getId());
    }

    @Test
    void excludesNonCompliantAndUnavailableEndpoints() {
        processor.registerEndpoint(new CloudEndpoint("noncompliant", "Bad DC", CloudType.PUBLIC, 1, 1000, true, false));
        processor.registerEndpoint(new CloudEndpoint("down", "Down DC", CloudType.PUBLIC, 1, 1000, false, true));
        processor.registerEndpoint(new CloudEndpoint("ok", "OK DC", CloudType.PUBLIC, 50, 200, true, true));

        Optional<CloudEndpoint> best = processor.selectOptimalEndpoint(null);
        assertTrue(best.isPresent());
        assertEquals("ok", best.get().getId());
    }

    @Test
    void excludesSpecifiedEndpointForRerouting() {
        processor.registerEndpoint(new CloudEndpoint("a", "A", CloudType.PUBLIC, 10, 100, true, true));
        processor.registerEndpoint(new CloudEndpoint("b", "B", CloudType.PUBLIC, 20, 100, true, true));

        Optional<CloudEndpoint> rerouted = processor.selectOptimalEndpoint("a");
        assertTrue(rerouted.isPresent());
        assertEquals("b", rerouted.get().getId());
    }

    @Test
    void returnsEmptyWhenNoEndpointsAvailable() {
        assertTrue(processor.selectOptimalEndpoint(null).isEmpty());
    }

    @Test
    void telemetryUpdateReflectsInSelection() {
        processor.registerEndpoint(new CloudEndpoint("x", "X", CloudType.PUBLIC, 10, 100, true, true));
        processor.updateTelemetry("x", 500, 5, true);
        assertEquals(500, processor.getEndpoint("x").get().getLatencyMs());
    }

    @Test
    void isolateEndpointMarksItUnavailable() {
        processor.registerEndpoint(new CloudEndpoint("y", "Y", CloudType.PUBLIC, 10, 100, true, true));
        processor.isolateEndpoint("y");
        assertFalse(processor.getEndpoint("y").get().isAvailable());
        assertTrue(processor.selectOptimalEndpoint(null).isEmpty());
    }
}
