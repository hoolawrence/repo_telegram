# Tark Telegram Workflow Client

Tark is an unofficial Android client forked from the official open source Telegram Android client. The product goal is not to rewrite Telegram, but to keep Telegram-compatible messaging while adding an internal AI workflow layer for task drafting, ops review, and GitHub handoff.

Source base: https://github.com/TelegramOrg/Telegram-Android

## Current Phase

This repository is in the Phase 1 demo stage.

Implemented:

- Telegram Android fork builds as `app.tark.client`.
- App name, launcher icon, splash mark, package name, and basic About text are replaced for Tark.
- Telegram API credentials are read from `local.properties` through `TARK_API_ID` and `TARK_API_HASH`.
- AI workflow backend URL is read from `TARK_WORKFLOW_API_BASE_URL`; empty value keeps the built-in mock flow.
- Logged-out builds open a Tark review screen so workflow and VPN/Proxy can be tested before Telegram login.
- `AI Workbench` is available from the main navigation and Settings.
- Chat message long-press menu has `Create AI Task`.
- `Create AI Task` passes `chat_id`, `message_id`, and message text into AI Workbench.
- `POST /api/tasks/draft` creates a draft requirement card through the configured Go backend, with mock fallback.
- Ops review buttons support `Approve` and `Reject`.
- Approve calls `POST /api/tasks/{id}/approve` through the configured Go backend, with mock fallback.
- `Network / Proxy` has a reserve page and status model, but no traffic routing.

Not implemented yet:

- Real AI request summarization API.
- Production GitHub App issue creation.
- Real organization, department, role, and permission model.
- Real VPN tunnel, proxy routing, node management, or Android `VpnService` integration.

## VPN / Proxy Plan

Phase 1 does not merge a VPN engine and does not include default nodes.

Candidate open source projects:

- WireGuard Android: candidate for a real VPN tunnel engine.
- Outline Client / Shadowsocks: candidate for proxy-style access profiles.
- Android `VpnService`: Android integration point reserved for future traffic routing.

Current behavior:

- No default server nodes are bundled.
- No credentials are bundled.
- No traffic is routed through VPN or proxy.
- The `Network / Proxy` page only shows reserved status fields.

Default nodes should be supplied later by the user or organization. Tark should not ship public shared nodes in the open source client.

## Workflow Engine Plan

The client uses `TARK_WORKFLOW_API_BASE_URL` to call the Go workflow backend. If no backend URL is configured, it falls back to the in-app mock workflow API so the product loop can still be tested without a server.

Selected backend direction:

- Go for API services.
- PostgreSQL for durable workflow and task state.
- Redis for cache, locks, and async coordination.
- Temporal for durable execution, retries, timers, async callbacks, and state recovery.
- GitHub App for Issue and PR automation.
- OpenAI or Codex APIs for requirement drafting and later code-generation workflows.

Flowable is a reasonable alternative if the team wants BPMN-style visual process modeling. For the first server version, Temporal is the preferred fit because the workflow is asynchronous and needs reliable retries around AI calls, GitHub calls, PR callbacks, demo deployment, and ops acceptance.

## Demo You Can Try

Without Telegram API credentials:

1. Build and install the APK.
2. Open `Tark Review`.
3. Tap `Open AI Workbench Demo`.
4. Review the simulated requirement card.
5. Tap `Approve` or `Reject`.
6. Open `Network / Proxy` to see the Phase 1 VPN/Proxy reservation status.

With Telegram API credentials:

1. Set `TARK_API_ID` and `TARK_API_HASH` in `local.properties`.
2. Rebuild and install the APK.
3. Tap `Telegram Login` from `Tark Review`.
4. Log in with Telegram.
5. Long press a normal chat message and choose `Create AI Task`.
6. Review and approve the generated task card.

## Build

Install Android SDK 35, Build Tools 35.0.0, NDK 21.4.7075529, and CMake 3.22.1.

Create `local.properties`:

```properties
sdk.dir=/path/to/android/sdk
TARK_API_ID=your_api_id
TARK_API_HASH=your_api_hash
TARK_WORKFLOW_API_BASE_URL=
```

Leave `TARK_WORKFLOW_API_BASE_URL` empty for mock mode. For an Android emulator talking to a local Go backend on your Mac, use:

```properties
TARK_WORKFLOW_API_BASE_URL=http://10.0.2.2:8080
```

Build:

```bash
GRADLE_USER_HOME=.gradle-user ./gradlew :TMessagesProj_App:assembleDebug --no-daemon
```

The debug APK is generated at:

```text
TMessagesProj_App/build/outputs/apk/afat/debug/app.apk
```

## Important Branding Note

Tark is not an official Telegram app. Do not use Telegram's official name, logo, production API credentials, Firebase configuration, or signing keys for a distributed Tark build.
