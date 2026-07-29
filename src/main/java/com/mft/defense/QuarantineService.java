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

package com.mft.defense;

import com.mft.model.Packet;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds packets that have been quarantined by the cyber-threat defense
 * processor, preventing them from being delivered to the destination
 * system, and records the reason for quarantine for audit purposes.
 */
public final class QuarantineService {

    private final Map<String, String> quarantinedPackets = new ConcurrentHashMap<>();

    public void quarantine(Packet packet, String reason) {
        packet.setQuarantined(true);
        quarantinedPackets.put(packet.getPacketId(), reason);
    }

    public boolean isQuarantined(String packetId) {
        return quarantinedPackets.containsKey(packetId);
    }

    public String getReason(String packetId) {
        return quarantinedPackets.get(packetId);
    }

    public Map<String, String> allQuarantined() {
        return Collections.unmodifiableMap(quarantinedPackets);
    }
}
