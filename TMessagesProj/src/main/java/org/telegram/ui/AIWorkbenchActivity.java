package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AIWorkbenchActivity extends BaseFragment {

    public static final String ARG_SOURCE_CHAT_ID = "source_chat_id";
    public static final String ARG_SOURCE_MESSAGE_ID = "source_message_id";
    public static final String ARG_MESSAGE_TEXT = "message_text";

    private long sourceChatId;
    private int sourceMessageId;
    private String messageText;

    private LinearLayout contentLayout;
    private TextView statusView;
    private TextView sourceView;
    private TextView titleView;
    private TextView descriptionView;
    private TextView metaView;
    private TextView issueView;
    private TextView approveButton;
    private TextView rejectButton;

    private TaskCard currentTask;

    public AIWorkbenchActivity() {
        this(null);
    }

    public AIWorkbenchActivity(Bundle args) {
        super(args);
        if (args != null) {
            sourceChatId = args.getLong(ARG_SOURCE_CHAT_ID, 0);
            sourceMessageId = args.getInt(ARG_SOURCE_MESSAGE_ID, 0);
            messageText = args.getString(ARG_MESSAGE_TEXT, "");
        }
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(getString(R.string.AIWorkbench));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
        fragmentView = frameLayout;

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        frameLayout.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setPadding(dp(18), dp(18), dp(18), dp(28));
        scrollView.addView(contentLayout, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addHeader(context);
        addTaskCard(context);
        requestDraftIfNeeded();
        return fragmentView;
    }

    private void addHeader(Context context) {
        TextView title = createText(context, 24, Typeface.BOLD, Theme.key_windowBackgroundWhiteBlackText);
        title.setText(getString(R.string.AIWorkbench));
        contentLayout.addView(title, linearParams(0, 0, 0, 10));

        TextView user = createText(context, 15, Typeface.NORMAL, Theme.key_windowBackgroundWhiteGrayText2);
        TLRPC.User currentUser = UserConfig.getInstance(currentAccount).getCurrentUser();
        String userName = currentUser != null ? UserObject.getUserName(currentUser) : "Unknown";
        user.setText(getString(R.string.AIWorkbenchCurrentUser) + ": " + userName);
        contentLayout.addView(user, linearParams(0, 0, 0, 18));
    }

    private void addTaskCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(Theme.createRoundRectDrawable(dp(8), getThemedColor(Theme.key_windowBackgroundWhite)));
        contentLayout.addView(card, linearParams(0, 0, 0, 16));

        statusView = createText(context, 14, Typeface.BOLD, Theme.key_windowBackgroundWhiteBlueHeader);
        card.addView(statusView, linearParams(0, 0, 0, 12));

        sourceView = createText(context, 13, Typeface.NORMAL, Theme.key_windowBackgroundWhiteGrayText2);
        card.addView(sourceView, linearParams(0, 0, 0, 16));

        titleView = createText(context, 18, Typeface.BOLD, Theme.key_windowBackgroundWhiteBlackText);
        card.addView(titleView, linearParams(0, 0, 0, 10));

        descriptionView = createText(context, 15, Typeface.NORMAL, Theme.key_windowBackgroundWhiteBlackText);
        descriptionView.setLineSpacing(dp(2), 1.0f);
        card.addView(descriptionView, linearParams(0, 0, 0, 14));

        metaView = createText(context, 13, Typeface.NORMAL, Theme.key_windowBackgroundWhiteGrayText2);
        card.addView(metaView, linearParams(0, 0, 0, 12));

        issueView = createText(context, 13, Typeface.NORMAL, Theme.key_windowBackgroundWhiteBlueText);
        card.addView(issueView, linearParams(0, 0, 0, 0));

        LinearLayout buttons = new LinearLayout(context);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.addView(buttons, linearParams(0, 0, 0, 0));

        approveButton = createActionButton(context, getString(R.string.AIWorkbenchApprove), Theme.key_featuredStickers_addButton);
        rejectButton = createActionButton(context, getString(R.string.AIWorkbenchReject), Theme.key_text_RedRegular);
        buttons.addView(approveButton, weightedParams(0, 0, 6, 0));
        buttons.addView(rejectButton, weightedParams(6, 0, 0, 0));

        approveButton.setOnClickListener(v -> approveTask());
        rejectButton.setOnClickListener(v -> rejectTask());

        showEmptyState();
    }

    private TextView createText(Context context, int sizeDp, int style, int colorKey) {
        TextView textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, sizeDp);
        textView.setTextColor(getThemedColor(colorKey));
        textView.setTypeface(Typeface.DEFAULT, style);
        textView.setGravity(Gravity.LEFT);
        return textView;
    }

    private TextView createActionButton(Context context, String text, int colorKey) {
        TextView button = createText(context, 15, Typeface.BOLD, Theme.key_featuredStickers_buttonText);
        button.setText(text);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(48));
        button.setBackground(Theme.createRoundRectDrawable(dp(8), getThemedColor(colorKey)));
        return button;
    }

    private LinearLayout.LayoutParams linearParams(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private LinearLayout.LayoutParams weightedParams(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(48), 1f);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private void requestDraftIfNeeded() {
        if (sourceChatId == 0 || sourceMessageId == 0 || TextUtils.isEmpty(messageText)) {
            showEmptyState();
            return;
        }
        setButtonsEnabled(false);
        statusView.setText("POST /api/tasks/draft");
        WorkflowApi.createDraft(sourceChatId, sourceMessageId, messageText, getRequester(), task -> {
            currentTask = task;
            renderTask();
            setButtonsEnabled(true);
        });
    }

    private String getRequester() {
        TLRPC.User currentUser = UserConfig.getInstance(currentAccount).getCurrentUser();
        return currentUser != null ? UserObject.getUserName(currentUser) : "unknown";
    }

    private void showEmptyState() {
        statusView.setText(getString(R.string.AIWorkbenchNoMessage));
        sourceView.setText("");
        titleView.setText("");
        descriptionView.setText("");
        metaView.setText("");
        issueView.setText("");
        setButtonsEnabled(false);
    }

    private void renderTask() {
        if (currentTask == null) {
            showEmptyState();
            return;
        }
        statusView.setText(getString(R.string.AIWorkbenchDraftStatus) + ": " + currentTask.status);
        sourceView.setText(getString(R.string.AIWorkbenchSource) + ": chat " + currentTask.sourceChatId + ", message " + currentTask.sourceMessageId);
        titleView.setText(currentTask.title);
        descriptionView.setText(currentTask.description);
        metaView.setText("requester: " + currentTask.requester + "\npriority: " + currentTask.priority + "\nassigned_role: " + currentTask.assignedRole);
        issueView.setText(TextUtils.isEmpty(currentTask.githubIssueUrl) ? "" : currentTask.githubIssueUrl);
    }

    private void approveTask() {
        if (currentTask == null) {
            return;
        }
        setButtonsEnabled(false);
        statusView.setText("POST /api/tasks/" + currentTask.taskId + "/approve");
        WorkflowApi.approve(currentTask, task -> {
            currentTask = task;
            renderTask();
            statusView.setText(getString(R.string.AIWorkbenchApproved));
            setButtonsEnabled(false);
        });
    }

    private void rejectTask() {
        if (currentTask == null) {
            return;
        }
        setButtonsEnabled(false);
        statusView.setText("POST /api/tasks/" + currentTask.taskId + "/reject");
        WorkflowApi.reject(currentTask, task -> {
            currentTask = task;
            renderTask();
            statusView.setText(getString(R.string.AIWorkbenchRejected));
        });
    }

    private void setButtonsEnabled(boolean enabled) {
        if (approveButton == null || rejectButton == null) {
            return;
        }
        approveButton.setEnabled(enabled);
        rejectButton.setEnabled(enabled);
        approveButton.setAlpha(enabled ? 1f : 0.45f);
        rejectButton.setAlpha(enabled ? 1f : 0.45f);
    }

    private interface TaskCallback {
        void onResult(TaskCard task);
    }

    private static class WorkflowApi {
        static void createDraft(long chatId, int messageId, String messageText, String requester, TaskCallback callback) {
            String baseUrl = baseUrl();
            if (TextUtils.isEmpty(baseUrl)) {
                MockWorkflowApi.createDraft(chatId, messageId, messageText, requester, callback);
                return;
            }
            Utilities.globalQueue.postRunnable(() -> {
                try {
                    JSONObject request = new JSONObject();
                    request.put("requester", requester);
                    request.put("source_chat_id", String.valueOf(chatId));
                    request.put("source_message_id", String.valueOf(messageId));
                    request.put("message_text", messageText == null ? "" : messageText);
                    request.put("priority", "medium");
                    TaskCard task = TaskCard.fromJson(post(baseUrl + "/api/tasks/draft", request));
                    AndroidUtilities.runOnUIThread(() -> callback.onResult(task));
                } catch (Exception e) {
                    MockWorkflowApi.createDraft(chatId, messageId, messageText, requester, callback);
                }
            });
        }

        static void approve(TaskCard task, TaskCallback callback) {
            postTaskAction(task, "approve", () -> MockWorkflowApi.approve(task, callback), callback);
        }

        static void reject(TaskCard task, TaskCallback callback) {
            postTaskAction(task, "reject", () -> {
                task.status = "rejected";
                task.updatedAt = MockWorkflowApi.now();
                AndroidUtilities.runOnUIThread(() -> callback.onResult(task));
            }, callback);
        }

        private static void postTaskAction(TaskCard task, String action, Runnable fallback, TaskCallback callback) {
            String baseUrl = baseUrl();
            if (TextUtils.isEmpty(baseUrl) || TextUtils.isEmpty(task.taskId)) {
                fallback.run();
                return;
            }
            Utilities.globalQueue.postRunnable(() -> {
                try {
                    TaskCard result = TaskCard.fromJson(post(baseUrl + "/api/tasks/" + task.taskId + "/" + action, new JSONObject()));
                    AndroidUtilities.runOnUIThread(() -> callback.onResult(result));
                } catch (Exception e) {
                    fallback.run();
                }
            });
        }

        private static String baseUrl() {
            String baseUrl = BuildConfig.TARK_WORKFLOW_API_BASE_URL;
            if (baseUrl == null) {
                return "";
            }
            baseUrl = baseUrl.trim();
            while (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            return baseUrl;
        }

        private static JSONObject post(String url, JSONObject body) throws Exception {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(12000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(payload);
            }

            int code = connection.getResponseCode();
            InputStream inputStream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
            String response = readResponse(inputStream);
            connection.disconnect();
            if (code < 200 || code >= 300) {
                throw new IllegalStateException(response);
            }
            return new JSONObject(response);
        }

        private static String readResponse(InputStream inputStream) throws Exception {
            if (inputStream == null) {
                return "";
            }
            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }
            return builder.toString();
        }
    }

    private static class MockWorkflowApi {
        static void createDraft(long chatId, int messageId, String messageText, String requester, TaskCallback callback) {
            AndroidUtilities.runOnUIThread(() -> callback.onResult(TaskCard.fromJson(draftJson(chatId, messageId, messageText, requester))), 350);
        }

        static void approve(TaskCard task, TaskCallback callback) {
            AndroidUtilities.runOnUIThread(() -> {
                task.status = "github_issue_created";
                task.githubIssueUrl = "https://github.com/our-org/Tark/issues/mock-" + task.taskId;
                task.updatedAt = now();
                callback.onResult(task);
            }, 350);
        }

        private static JSONObject draftJson(long chatId, int messageId, String messageText, String requester) {
            JSONObject json = new JSONObject();
            try {
                String normalizedText = messageText == null ? "" : messageText.trim();
                String firstLine = normalizedText.split("\\n", 2)[0];
                if (firstLine.length() > 72) {
                    firstLine = firstLine.substring(0, 69) + "...";
                }
                if (TextUtils.isEmpty(firstLine)) {
                    firstLine = "Untitled request";
                }
                String now = now();
                json.put("task_id", "tark-" + Math.abs(chatId) + "-" + messageId);
                json.put("title", firstLine);
                json.put("description", normalizedText);
                json.put("requester", requester);
                json.put("source_chat_id", String.valueOf(chatId));
                json.put("source_message_id", String.valueOf(messageId));
                json.put("priority", "medium");
                json.put("status", "pending_ops_review");
                json.put("assigned_role", "ops");
                json.put("github_issue_url", "");
                json.put("github_pr_url", "");
                json.put("demo_url", "");
                json.put("created_at", now);
                json.put("updated_at", now);
            } catch (Exception ignore) {
            }
            return json;
        }

        static String now() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(new Date());
        }
    }

    private static class TaskCard {
        String taskId;
        String title;
        String description;
        String requester;
        String sourceChatId;
        String sourceMessageId;
        String priority;
        String status;
        String assignedRole;
        String githubIssueUrl;
        String updatedAt;

        static TaskCard fromJson(JSONObject json) {
            TaskCard task = new TaskCard();
            task.taskId = json.optString("task_id");
            task.title = json.optString("title");
            task.description = json.optString("description");
            task.requester = json.optString("requester");
            task.sourceChatId = json.optString("source_chat_id");
            task.sourceMessageId = json.optString("source_message_id");
            task.priority = json.optString("priority");
            task.status = json.optString("status");
            task.assignedRole = json.optString("assigned_role");
            task.githubIssueUrl = json.optString("github_issue_url");
            task.updatedAt = json.optString("updated_at");
            return task;
        }
    }
}
