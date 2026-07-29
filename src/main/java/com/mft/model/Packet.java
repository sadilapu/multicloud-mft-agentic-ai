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

package com.mft.model;

/**
 * A single encrypted, signed transmission packet produced by segmenting a
 * source file. Mirrors the "transmission packet" of the patent specification,
 * carrying a packet identification value, sequence index, ciphertext,
 * initialization vector, cryptographic hash and digital signature.
 */
public final class Packet {

    private final String packetId;
    private final int sequenceIndex;
    private final int totalPackets;
    private byte[] plaintext;
    private byte[] ciphertext;
    private byte[] iv;
    private byte[] hash;
    private byte[] signature;
    private boolean quarantined;

    public Packet(String packetId, int sequenceIndex, int totalPackets, byte[] plaintext) {
        this.packetId = packetId;
        this.sequenceIndex = sequenceIndex;
        this.totalPackets = totalPackets;
        this.plaintext = plaintext;
    }

    public String getPacketId() { return packetId; }
    public int getSequenceIndex() { return sequenceIndex; }
    public int getTotalPackets() { return totalPackets; }
    public byte[] getPlaintext() { return plaintext; }
    public byte[] getCiphertext() { return ciphertext; }
    public byte[] getIv() { return iv; }
    public byte[] getHash() { return hash; }
    public byte[] getSignature() { return signature; }
    public boolean isQuarantined() { return quarantined; }

    public void setCiphertext(byte[] ciphertext) { this.ciphertext = ciphertext; }
    public void setIv(byte[] iv) { this.iv = iv; }
    public void setHash(byte[] hash) { this.hash = hash; }
    public void setSignature(byte[] signature) { this.signature = signature; }
    public void setQuarantined(boolean quarantined) { this.quarantined = quarantined; }

    public int sizeBytes() { return plaintext == null ? 0 : plaintext.length; }

    @Override
    public String toString() {
        return "Packet{id='%s', seq=%d/%d, size=%dB, quarantined=%s}"
                .formatted(packetId, sequenceIndex + 1, totalPackets, sizeBytes(), quarantined);
    }
}
