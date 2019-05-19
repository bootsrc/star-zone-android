package com.appjishu.starzone.ui.widget.dialog;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatDialog;

import com.appjishu.starzone.R;
import com.appjishu.starzone.config.SettingShared;
import com.pnikosis.materialishprogress.ProgressWheel;


import butterknife.BindView;
import butterknife.ButterKnife;

public class ProgressDialog extends AppCompatDialog {

    public static ProgressDialog createWithAutoTheme(@NonNull Activity activity) {
        return new ProgressDialog(activity, SettingShared.isEnableThemeDark(activity) ? R.style.AppDialogDark_Progress : R.style.AppDialogLight_Progress);
    }

    @BindView(R.id.progress_wheel)
    ProgressWheel progressWheel;

    private ProgressDialog(@NonNull Activity activity, @StyleRes int theme) {
        super(activity, theme);
        setContentView(R.layout.dialog_progress);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        progressWheel.spin();
    }

    @Override
    protected void onStop() {
        progressWheel.stopSpinning();
        super.onStop();
    }

}
