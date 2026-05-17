# Tark Phase 1 Status

## VPN / Proxy

Current state: reserved only.

Open source candidates:

- WireGuard Android for VPN tunnel support.
- Outline Client / Shadowsocks for proxy profile support.
- Android VpnService for Android traffic-routing integration.

Default nodes:

- No default node is available in this stage.
- No node URL, access key, private key, endpoint, or credential is bundled.
- Future builds should load user-provided or organization-provided profiles.

Client surface:

- Settings includes a `Network / Proxy` reserve page.
- `TarkNetworkModule` exposes reserved status fields.
- Connection state is `DISABLED`.
- `vpnServiceIntegrated` is `false`.
- `trafficRoutingEnabled` is `false`.

## Workflow

Current state: client-side mock.

Implemented demo flow:

- Telegram message
- Long press `Create AI Task`
- Message context passed to AI Workbench
- Mock `POST /api/tasks/draft`
- Requirement card displayed
- Ops `Approve` / `Reject`
- Mock `POST /api/tasks/{id}/approve`
- Mock GitHub Issue URL displayed

Recommended backend stack:

- Spring Boot
- PostgreSQL
- Redis
- Temporal
- GitHub App
- OpenAI or Codex APIs

Temporal is the preferred first workflow engine because the product needs durable async execution, retries, timers, callback handling, and state recovery. Flowable remains a useful option if the team later wants BPMN diagrams and business-facing workflow modeling.
