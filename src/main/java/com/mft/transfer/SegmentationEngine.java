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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data segmentation procedure performed by the file transfer coordination
 * processor (patent element 106): divides a received file into a plurality
 * of variable-length packets based on a configured packet-size threshold,
 * assigning each packet a unique packet identification value and sequence
 * index so that it can be reassembled at the destination.
 */
public final class SegmentationEngine {

    /** Default maximum packet payload size in bytes (64 KiB). */
    public static final int DEFAULT_MAX_PACKET_SIZE = 64 * 1024;

    private final int maxPacketSizeBytes;

    public SegmentationEngine() {
        this(DEFAULT_MAX_PACKET_SIZE);
    }

    public SegmentationEngine(int maxPacketSizeBytes) {
        if (maxPacketSizeBytes <= 0) {
            throw new IllegalArgumentException("maxPacketSizeBytes must be positive");
        }
        this.maxPacketSizeBytes = maxPacketSizeBytes;
    }

    public List<Packet> segment(byte[] fileContent) {
        if (fileContent.length == 0) {
            throw new IllegalArgumentException("Cannot segment an empty file");
        }
        int totalPackets = (int) Math.ceil(fileContent.length / (double) maxPacketSizeBytes);
        List<Packet> packets = new ArrayList<>(totalPackets);
        for (int i = 0; i < totalPackets; i++) {
            int start = i * maxPacketSizeBytes;
            int end = Math.min(start + maxPacketSizeBytes, fileContent.length);
            byte[] chunk = new byte[end - start];
            System.arraycopy(fileContent, start, chunk, 0, chunk.length);
            packets.add(new Packet("pkt-" + UUID.randomUUID(), i, totalPackets, chunk));
        }
        return packets;
    }

    /** Reassembles a file from an ordered (by sequence index) list of decrypted packet plaintexts. */
    public byte[] reassemble(List<byte[]> orderedPlaintexts) {
        int totalSize = orderedPlaintexts.stream().mapToInt(b -> b.length).sum();
        byte[] result = new byte[totalSize];
        int offset = 0;
        for (byte[] chunk : orderedPlaintexts) {
            System.arraycopy(chunk, 0, result, offset, chunk.length);
            offset += chunk.length;
        }
        return result;
    }

    public int getMaxPacketSizeBytes() { return maxPacketSizeBytes; }
}
