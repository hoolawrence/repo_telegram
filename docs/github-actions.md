# GitHub Actions

## Android APK

Workflow: `.github/workflows/android-apk.yml`

Purpose:

- Build a review APK from GitHub Actions.
- Inject Telegram API credentials from the `production` Environment.
- Upload `app.apk` as a downloadable artifact.

Required repository:

- `hoolawrence/repo_telegram`

Required Environment:

- `production`

Required Environment secrets:

- `TELEGRAM_API_ID`
- `TELEGRAM_API_HASH`

Optional workflow input:

- `workflow_api_base_url`

Use `workflow_api_base_url` when the APK should call a deployed Tark workflow backend. Leave it empty for frontend mock mode.

Run path:

1. Open `repo_telegram` on GitHub.
2. Go to `Actions`.
3. Select `Android APK`.
4. Click `Run workflow`.
5. Enter the backend URL if needed.
6. Download the `tark-review-apk` artifact after the job succeeds.

Important:

- Secrets configured on `tark-workflow-server` are not visible to `repo_telegram`.
- To build a login-capable APK, copy `TELEGRAM_API_ID` and `TELEGRAM_API_HASH` into the `production` Environment of `repo_telegram`.
