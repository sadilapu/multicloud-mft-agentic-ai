# Multi-Cloud Managed File Transfer System with Agentic AI/ML Orchestration

[![CI](https://github.com/sadilapu/multicloud-mft-agentic-ai/actions/workflows/ci.yml/badge.svg)](https://github.com/sadilapu/multicloud-mft-agentic-ai/actions/workflows/ci.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)

An open-source, dependency-free Java reference implementation of a **multi-cloud
managed file transfer (MFT) system with agentic AI/ML orchestration for
autonomous cyber-threat defense**. It segments, encrypts, signs, routes, and
delivers files across multiple cloud endpoints, while a statistical AI
analysis processor and a reinforcement-learning-based agentic decision
processor continuously watch for anomalous behavior and autonomously choose
mitigation actions — rerouting, requiring additional authentication,
regenerating encryption keys, suspending, or quarantining.

> ⚠️ **Relationship to IP registration**: This repository is a
> reference/demonstration implementation of the system described in German
> Utility Model **DE 20 2026 101 328 U1** (Gebrauchsmuster), registered
> May 22, 2026, titled **"Multi-Cloud Managed File Transfer System with
> Agentic Artificial Intelligence–Machine Learning Orchestration for Cyber
> Threat Defense"** — German title (official): *Multi-Cloud-verwaltetes
> Dateiübertragungssystem mit agentenbasierter künstlicher Intelligenz und
> maschinellem Lernen zur Abwehr von Cyberbedrohungen*. The code
> intentionally uses simplified, auditable stand-ins for a few production
> concerns (see [Design notes & limitations](#design-notes--limitations)
> below) so it can be built and run by anyone with just a JDK — no cloud
> accounts, API keys, or paid services required.
>
> ### Claim-to-code mapping
>
> | Claim | Element | Implementation |
> |---|---|---|
> | 1 | Computing device (102) | [`Bootstrap`](src/main/java/com/mft/Bootstrap.java), [`MftOrchestrator`](src/main/java/com/mft/orchestration/MftOrchestrator.java) |
> | 1 | Multi-cloud connectivity processor (104) | [`MultiCloudConnectivityProcessor`](src/main/java/com/mft/connectivity/MultiCloudConnectivityProcessor.java) |
> | 1 | File transfer coordination processor (106) | [`FileTransferCoordinationProcessor`](src/main/java/com/mft/transfer/FileTransferCoordinationProcessor.java) |
> | 1 | Encryption processing unit (108) | [`EncryptionProcessingUnit`](src/main/java/com/mft/crypto/EncryptionProcessingUnit.java) |
> | 1 | AI analysis processor (110) | [`AIAnalysisProcessor`](src/main/java/com/mft/ai/AIAnalysisProcessor.java), [`BehavioralProfile`](src/main/java/com/mft/ai/BehavioralProfile.java) |
> | 1 | Agentic decision processor (112) | [`AgenticDecisionProcessor`](src/main/java/com/mft/agent/AgenticDecisionProcessor.java) |
> | 1 | Cyber-threat defense processor (114) | [`CyberThreatDefenseProcessor`](src/main/java/com/mft/defense/CyberThreatDefenseProcessor.java) |
> | 2 | Variable-length packet segmentation with ID + sequence index | [`SegmentationEngine`](src/main/java/com/mft/transfer/SegmentationEngine.java) |
> | 3 | Secure key generation + symmetric encryption + digital signatures | [`KeyManager`](src/main/java/com/mft/crypto/KeyManager.java), [`EncryptionProcessingUnit`](src/main/java/com/mft/crypto/EncryptionProcessingUnit.java) (AES-256/GCM + RSA-2048) |
> | 4 | Behavioral profiles (transfer frequency, packet size, source/destination address patterns, auth characteristics) | [`TelemetryEvent`](src/main/java/com/mft/model/TelemetryEvent.java), [`BehavioralProfile`](src/main/java/com/mft/ai/BehavioralProfile.java), [`TelemetryCollector`](src/main/java/com/mft/orchestration/TelemetryCollector.java) |
> | 5 | Real-time deviation comparison + anomaly indicators | [`AIAnalysisProcessor.analyze`](src/main/java/com/mft/ai/AIAnalysisProcessor.java) |
> | 6 | Reinforcement-learning-based decision policy | [`ReinforcementLearningPolicy`](src/main/java/com/mft/agent/ReinforcementLearningPolicy.java) |
> | 7 | Sandbox inspection environment | [`SandboxInspector`](src/main/java/com/mft/defense/SandboxInspector.java) |
> | 8–9 | Dynamic routing tables, telemetry-based rerouting | [`MultiCloudConnectivityProcessor`](src/main/java/com/mft/connectivity/MultiCloudConnectivityProcessor.java) |
> | 10 | Transfer logging processor (transaction logs, audit trail) | [`TransferLoggingProcessor`](src/main/java/com/mft/logging/TransferLoggingProcessor.java) |
> | (detailed description) | Authentication verification processor | [`AuthenticationVerificationProcessor`](src/main/java/com/mft/auth/AuthenticationVerificationProcessor.java) |

## Why this exists

This project is published so that other engineers can freely download, run,
audit, and build on a working implementation of the system's core techniques:
multi-cloud routing, packet-level encryption with per-packet digital
signatures, statistical behavioral-anomaly detection, and a
reinforcement-learning-driven autonomous defense policy — all in an
end-to-end-tested, single-command-to-run package.

## Architecture

The system is composed of six cooperating processors, wired together by
[`MftOrchestrator`](src/main/java/com/mft/orchestration/MftOrchestrator.java):

```
                       ┌─────────────────────────────────────────────┐
                       │              MftOrchestrator                │
                       └─────────────────────────────────────────────┘
 FileTransferRequest ──▶ 1. AuthenticationVerificationProcessor
                       ──▶ 2. FileTransferCoordinationProcessor
                              (integrity hash + packet segmentation)
                       ──▶ 3. EncryptionProcessingUnit
                              (AES-256/GCM + RSA-2048 signatures)
                       ──▶ 4. MultiCloudConnectivityProcessor
                              (latency/bandwidth-aware routing)
                       ──▶ 5. AIAnalysisProcessor
                              (z-score behavioral anomaly detection)
                       ──▶ 6. AgenticDecisionProcessor
                              (reinforcement-learning defensive policy)
                       ──▶ 7. CyberThreatDefenseProcessor
                              (integrity re-verification + sandbox scan)
                       ──▶ TransferLoggingProcessor (every stage, audited)
```

| Processor | Package | Responsibility |
|---|---|---|
| Multi-cloud connectivity | `com.mft.connectivity` | Maintains a dynamic routing table of public/private/hybrid/enterprise cloud endpoints; selects and reroutes based on latency, bandwidth, availability, and security compliance. |
| File transfer coordination | `com.mft.transfer` | Computes a baseline SHA-256 integrity hash and segments files into variable-length, sequence-indexed packets. |
| Encryption processing unit | `com.mft.crypto` | AES-256/GCM encryption per packet with unique IVs, SHA-256 hashing, and RSA-2048 digital signatures; verifies integrity on demand. |
| AI analysis | `com.mft.ai` | Builds a per-user behavioral profile (mean/stddev per feature) from historical telemetry and computes a multivariate z-score deviation for new telemetry. |
| Agentic decision | `com.mft.agent` | Combines the anomaly score, auth-failure count, and incident history into a composite risk score, then selects a defensive action via a tabular reinforcement-learning policy that improves from outcome feedback. |
| Cyber-threat defense | `com.mft.defense` | Re-verifies cryptographic integrity and runs sandbox-style payload inspection (entropy analysis + signature matching); quarantines packets that fail either check. |
| Transfer logging | `com.mft.logging` | Structured, timestamped, per-session audit log of every stage (in-memory + optional JSON-lines file). |
| REST API | `com.mft.server` | Dependency-free HTTP API built on the JDK's built-in `HttpServer`. |

## Quick start

Requires only a JDK 21 (no other tools, no API keys, no cloud accounts).

```bash
git clone https://github.com/sadilapu/multicloud-mft-agentic-ai.git
cd multicloud-mft-agentic-ai
mvn package
java -jar target/multicloud-mft-agentic-ai.jar
```

The server starts on port 8080 (override with `PORT=9090 java -jar ...` or a
CLI argument) and seeds a demo user (`demo-user` / `demo-token`) with a
trained baseline behavioral profile.

```bash
curl http://localhost:8080/api/v1/health

curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Content-Type: application/json" \
  -d '{
        "fileName": "report.pdf",
        "fileContentBase64": "'"$(echo -n 'hello world' | base64)"'",
        "sourceUserId": "demo-user",
        "authToken": "demo-token",
        "destinationEndpointId": "aws-us-east-1"
      }'

# Response includes a sessionId — use it to check status and logs:
curl http://localhost:8080/api/v1/transfers/<sessionId>
curl http://localhost:8080/api/v1/transfers/<sessionId>/logs
```

## Running the tests

The project ships with 63 tests: unit tests for every processor, full
pipeline integration tests, and true end-to-end tests that start the real
embedded HTTP server and drive it over real HTTP requests.

```bash
mvn test
```

## API reference

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/health` | Liveness check. |
| `POST` | `/api/v1/transfers` | Submit a file transfer. Body: `fileName`, `fileContentBase64`, `sourceUserId`, `authToken`, `destinationEndpointId`, optional `privilegedUser` (bool), optional `sourceReputationScore` (0–1). |
| `GET` | `/api/v1/transfers/{sessionId}` | Current status of a transfer. |
| `GET` | `/api/v1/transfers/{sessionId}/logs` | Full audit log for a transfer. |

## Design notes & limitations

This is a reference implementation optimized for clarity, auditability, and
zero external dependencies — not a drop-in production deployment. In
particular:

- **Sandbox inspection** (`SandboxInspector`) uses Shannon-entropy analysis
  and known-signature matching as fast, dependency-free stand-ins for a real
  containerized/VM dynamic-execution sandbox. Swap in a real sandbox for
  production use.
- **Cloud endpoints** are in-memory simulated `CloudEndpoint` records with
  configurable latency/bandwidth, not live connections to AWS/Azure/GCP.
  Wiring `MultiCloudConnectivityProcessor` to real cloud SDKs is a natural
  next step.
- **The REST API** uses the JDK's built-in `com.sun.net.httpserver.HttpServer`
  and a minimal hand-rolled JSON reader/writer so the project builds with
  nothing but a JDK. It is not a hardened, production web framework.
- **The reinforcement-learning policy** (`ReinforcementLearningPolicy`) is an
  intentionally simple, fully-auditable tabular Q-learning implementation —
  a genuine, functioning learning policy, not a placeholder — rather than a
  deep-learning model, again to keep the reference implementation
  dependency-free and easy to read end to end.
- **Authentication** (`AuthenticationVerificationProcessor`) implements a
  single-factor bearer-token check as a stand-in for the specification's
  fuller description (password authentication, digital certificate
  validation, and one-time authentication tokens). The failed-attempt
  counter it maintains is real and feeds the AI analysis / risk-scoring
  pipeline exactly as described; swapping in additional verification
  factors would not require changes elsewhere in the pipeline.

Contributions that advance any of the above toward production-hardened
equivalents are welcome — see [Contributing](#contributing).

## Contributing

Issues and pull requests are welcome. Please include tests for any new
behavior; `mvn test` must pass in CI before a PR can be merged.

## Community

Using this project or have questions? Feel free to start a
[Discussion](https://github.com/sadilapu/multicloud-mft-agentic-ai/discussions)
or open an issue — I'd love to hear about it.

## License

Licensed under the [Apache License, Version 2.0](LICENSE). See [NOTICE](NOTICE).
