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

import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class TarkWelcomeActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        actionBar.setTitle(getString(R.string.TarkWelcomeTitle));

        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundGray));
        fragmentView = frameLayout;

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        frameLayout.addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(24), dp(18), dp(28));
        scrollView.addView(content, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addHeader(context, content);
        addTelegramCard(context, content);
        addWorkflowCard(context, content);
        return fragmentView;
    }

    private void addHeader(Context context, LinearLayout content) {
        TextView title = createText(context, 28, Typeface.BOLD, Theme.key_windowBackgroundWhiteBlackText);
        title.setText(getString(R.string.TarkWelcomeTitle));
        content.addView(title, linearParams(0, 0, 0, 10));

        TextView subtitle = createText(context, 15, Typeface.NORMAL, Theme.key_windowBackgroundWhiteGrayText2);
        subtitle.setText(getString(R.string.TarkWelcomeSubtitle));
        subtitle.setLineSpacing(dp(2), 1.0f);
        content.addView(subtitle, linearParams(0, 0, 0, 18));
    }

    private void addTelegramCard(Context context, LinearLayout content) {
        LinearLayout card = createCard(context);
        content.addView(card, linearParams(0, 0, 0, 16));

        TextView title = createText(context, 18, Typeface.BOLD, Theme.key_windowBackgroundWhiteBlackText);
        title.setText(getString(R.string.TarkTelegramStatus));
        card.addView(title, linearParams(0, 0, 0, 10));

        TextView status = createText(context, 14, Typeface.NORMAL, Theme.key_windowBackgroundWhiteGrayText2);
        status.setText(hasTelegramApiCredentials() ? getString(R.string.TarkTelegramConfigured) : getString(R.string.TarkTelegramMissingCredentials));
        status.setLineSpacing(dp(2), 1.0f);
        card.addView(status, linearParams(0, 0, 0, 14));

        TextView loginButton = createActionButton(context, getString(R.string.TarkTelegramLogin), Theme.key_featuredStickers_addButton);
        card.addView(loginButton, linearParams(0, 0, 0, 0));
        loginButton.setOnClickListener(v -> {
            if (hasTelegramApiCredentials()) {
                presentFragment(new LoginActivity());
            } else {
                showMissingCredentialsAlert();
            }
        });
    }

    private void addWorkflowCard(Context context, LinearLayout content) {
        LinearLayout card = createCard(context);
        content.addView(card, linearParams(0, 0, 0, 0));

        TextView title = createText(context, 18, Typeface.BOLD, Theme.key_windowBackgroundWhiteBlackText);
        title.setText(getString(R.string.AIWorkbench));
        card.addView(title, linearParams(0, 0, 0, 10));

        TextView backend = createText(context, 14, Typeface.NORMAL, Theme.key_windowBackgroundWhiteGrayText2);
        backend.setText(getString(R.string.TarkBackendMode) + ": " + (hasWorkflowBackend() ? getString(R.string.TarkBackendConfigured) : getString(R.string.TarkBackendMock)));
        card.addView(backend, linearParams(0, 0, 0, 14));

        TextView demoMessage = createText(context, 14, Typeface.NORMAL, Theme.key_windowBackgroundWhiteBlackText);
        demoMessage.setText(getString(R.string.TarkDemoMessageTitle) + "\n" + getString(R.string.TarkDemoMessageText));
        demoMessage.setLineSpacing(dp(2), 1.0f);
        card.addView(demoMessage, linearParams(0, 0, 0, 14));

        TextView workbenchButton = createActionButton(context, getString(R.string.TarkOpenAIWorkbenchDemo), Theme.key_featuredStickers_addButton);
        card.addView(workbenchButton, linearParams(0, 0, 0, 10));
        workbenchButton.setOnClickListener(v -> openAIWorkbenchDemo());

        TextView networkButton = createActionButton(context, getString(R.string.TarkOpenNetworkProxy), Theme.key_avatar_backgroundBlue);
        card.addView(networkButton, linearParams(0, 0, 0, 0));
        networkButton.setOnClickListener(v -> presentFragment(new TarkNetworkActivity()));
    }

    private void openAIWorkbenchDemo() {
        Bundle args = new Bundle();
        args.putLong(AIWorkbenchActivity.ARG_SOURCE_CHAT_ID, -10020260517L);
        args.putInt(AIWorkbenchActivity.ARG_SOURCE_MESSAGE_ID, 101);
        args.putString(AIWorkbenchActivity.ARG_MESSAGE_TEXT, getString(R.string.TarkDemoMessageText));
        presentFragment(new AIWorkbenchActivity(args));
    }

    private void showMissingCredentialsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(getString(R.string.TarkTelegramCredentialsTitle));
        builder.setMessage(getString(R.string.TarkTelegramCredentialsMessage));
        builder.setPositiveButton(getString(R.string.OK), null);
        showDialog(builder.create());
    }

    private boolean hasTelegramApiCredentials() {
        return BuildVars.APP_ID > 0 && !TextUtils.isEmpty(BuildVars.APP_HASH);
    }

    private boolean hasWorkflowBackend() {
        return !TextUtils.isEmpty(BuildConfig.TARK_WORKFLOW_API_BASE_URL);
    }

    private LinearLayout createCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(Theme.createRoundRectDrawable(dp(8), getThemedColor(Theme.key_windowBackgroundWhite)));
        return card;
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
}
