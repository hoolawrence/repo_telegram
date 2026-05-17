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

- Logged-out builds open `Tark Review`, which links to AI Workbench demo and Network / Proxy review.
- Settings includes a `Network / Proxy` reserve page.
- `TarkNetworkModule` exposes reserved status fields.
- Connection state is `DISABLED`.
- `vpnServiceIntegrated` is `false`.
- `trafficRoutingEnabled` is `false`.

## Workflow

Current state: configurable Go backend with client-side mock fallback.

The Android client reads `TARK_WORKFLOW_API_BASE_URL` from `local.properties`. If it is empty, AI Workbench uses the built-in mock flow. If it is set, AI Workbench calls the Go backend endpoints directly.

Implemented demo flow:

- Telegram message
- Long press `Create AI Task`
- Message context passed to AI Workbench
- `POST /api/tasks/draft`
- Requirement card displayed
- Ops `Approve` / `Reject`
- `POST /api/tasks/{id}/approve`
- GitHub Issue URL displayed; dry-run mode is supported by the Go backend

Telegram login:

- Real chat login requires `TARK_API_ID` and `TARK_API_HASH` from `my.telegram.org`.
- APKs built with empty credentials keep the review flow available, but Telegram login is intentionally blocked with an explanatory message.

Selected backend stack:

- Go
- PostgreSQL
- Redis
- Temporal
- GitHub App
- OpenAI or Codex APIs

Temporal is the preferred first workflow engine because the product needs durable async execution, retries, timers, callback handling, and state recovery. Flowable remains a useful option if the team later wants BPMN diagrams and business-facing workflow modeling.
