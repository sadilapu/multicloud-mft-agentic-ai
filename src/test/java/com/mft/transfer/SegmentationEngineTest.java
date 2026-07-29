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

package com.mft.transfer;

import com.mft.model.Packet;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SegmentationEngineTest {

    @Test
    void segmentsFileSmallerThanThresholdIntoOnePacket() {
        SegmentationEngine engine = new SegmentationEngine(1024);
        byte[] file = "small file".getBytes(StandardCharsets.UTF_8);

        List<Packet> packets = engine.segment(file);

        assertEquals(1, packets.size());
        assertEquals(1, packets.get(0).getTotalPackets());
        assertArrayEquals(file, packets.get(0).getPlaintext());
    }

    @Test
    void segmentsLargeFileIntoMultiplePacketsWithSequenceIndices() {
        SegmentationEngine engine = new SegmentationEngine(10);
        byte[] file = new byte[35];
        for (int i = 0; i < file.length; i++) file[i] = (byte) i;

        List<Packet> packets = engine.segment(file);

        assertEquals(4, packets.size()); // 10,10,10,5
        for (int i = 0; i < packets.size(); i++) {
            assertEquals(i, packets.get(i).getSequenceIndex());
            assertEquals(4, packets.get(i).getTotalPackets());
        }
        assertEquals(5, packets.get(3).sizeBytes());
    }

    @Test
    void rejectsEmptyFile() {
        SegmentationEngine engine = new SegmentationEngine();
        assertThrows(IllegalArgumentException.class, () -> engine.segment(new byte[0]));
    }

    @Test
    void reassembleReconstructsOriginalContent() {
        SegmentationEngine engine = new SegmentationEngine(4);
        byte[] original = "reassembly-round-trip-test".getBytes(StandardCharsets.UTF_8);

        List<Packet> packets = engine.segment(original);
        List<byte[]> plaintexts = new ArrayList<>();
        for (Packet p : packets) plaintexts.add(p.getPlaintext());

        byte[] reassembled = engine.reassemble(plaintexts);
        assertArrayEquals(original, reassembled);
    }

    @Test
    void eachPacketHasUniqueId() {
        SegmentationEngine engine = new SegmentationEngine(5);
        byte[] file = new byte[50];
        List<Packet> packets = engine.segment(file);
        long distinctIds = packets.stream().map(Packet::getPacketId).distinct().count();
        assertEquals(packets.size(), distinctIds);
    }
}
