package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.telegram.messenger.R;
import org.telegram.messenger.TarkNetworkModule;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;

public class TarkNetworkActivity extends BaseFragment {

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(getString(R.string.TarkNetworkProxy));
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

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(18), dp(18), dp(28));
        scrollView.addView(content, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addHeader(context, content);
        addStatusCard(context, content);
        addPlanCard(context, content);
        return fragmentView;
    }

    private void addHeader(Context context, LinearLayout content) {
        TextView title = createText(context, 24, Typeface.BOLD, Theme.key_windowBackgroundWhiteBlackText);
        title.setText(getString(R.string.TarkNetworkProxy));
        content.addView(title, linearParams(0, 0, 0, 10));

        TextView subtitle = createText(context, 15, Typeface.NORMAL, Theme.key_windowBackgroundWhiteGrayText2);
        subtitle.setText(getString(R.string.TarkNetworkPhaseOne));
        content.addView(subtitle, linearParams(0, 0, 0, 18));
    }

    private void addStatusCard(Context context, LinearLayout content) {
        TarkNetworkModule.NetworkStatus status = TarkNetworkModule.getInstance().getStatus();
        LinearLayout card = createCard(context);
        content.addView(card, linearParams(0, 0, 0, 16));

        addRow(context, card, getString(R.string.TarkNetworkStatus), status.state.name());
        addRow(context, card, getString(R.string.TarkNetworkDefaultNode), status.hasDefaultNode ? status.activeProfileName : getString(R.string.TarkNetworkNoDefaultNode));
        addRow(context, card, getString(R.string.TarkNetworkVpnService), status.vpnServiceIntegrated ? getString(R.string.TarkNetworkEnabled) : getString(R.string.TarkNetworkDisabled));
        addRow(context, card, getString(R.string.TarkNetworkTrafficRouting), status.trafficRoutingEnabled ? getString(R.string.TarkNetworkEnabled) : getString(R.string.TarkNetworkDisabled));
    }

    private void addPlanCard(Context context, LinearLayout content) {
        LinearLayout card = createCard(context);
        content.addView(card, linearParams(0, 0, 0, 0));

        addRow(context, card, getString(R.string.TarkNetworkVpnCandidate), TarkNetworkModule.VPN_ENGINE_CANDIDATE);
        addRow(context, card, getString(R.string.TarkNetworkProxyCandidate), TarkNetworkModule.PROXY_ENGINE_CANDIDATE);
        addRow(context, card, getString(R.string.TarkNetworkAndroidVpn), TarkNetworkModule.ANDROID_VPN_SERVICE);

        TextView note = createText(context, 14, Typeface.NORMAL, Theme.key_windowBackgroundWhiteGrayText2);
        note.setLineSpacing(dp(2), 1.0f);
        note.setText(getString(R.string.TarkNetworkNoTrafficNote));
        card.addView(note, linearParams(0, 10, 0, 0));
    }

    private LinearLayout createCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(Theme.createRoundRectDrawable(dp(8), getThemedColor(Theme.key_windowBackgroundWhite)));
        return card;
    }

    private void addRow(Context context, LinearLayout card, String label, String value) {
        TextView row = createText(context, 15, Typeface.NORMAL, Theme.key_windowBackgroundWhiteBlackText);
        row.setText(label + ": " + value);
        card.addView(row, linearParams(0, 0, 0, 10));
    }

    private TextView createText(Context context, int sizeDp, int style, int colorKey) {
        TextView textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, sizeDp);
        textView.setTextColor(getThemedColor(colorKey));
        textView.setTypeface(Typeface.DEFAULT, style);
        textView.setGravity(Gravity.LEFT);
        return textView;
    }

    private LinearLayout.LayoutParams linearParams(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }
}
