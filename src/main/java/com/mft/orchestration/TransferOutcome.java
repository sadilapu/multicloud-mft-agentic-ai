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

import com.mft.model.RiskAssessment;
import com.mft.model.TransferStatus;
import com.mft.transfer.TransferSession;

/** Final, immutable result returned to callers once a transfer has been fully processed. */
public final class TransferOutcome {

    private final TransferSession session;
    private final RiskAssessment riskAssessment;

    public TransferOutcome(TransferSession session, RiskAssessment riskAssessment) {
        this.session = session;
        this.riskAssessment = riskAssessment;
    }

    public TransferSession getSession() { return session; }
    public RiskAssessment getRiskAssessment() { return riskAssessment; }
    public TransferStatus getStatus() { return session.getStatus(); }
    public String getSessionId() { return session.getSessionId(); }
}
