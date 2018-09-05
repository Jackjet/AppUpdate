package cn.lsmya.apkupdate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class AppUpdateDialog extends Dialog {
    private Context context;
    private onDialogListener mListener;
    private String title = "版本更新";
    private String oldVersion;
    private String newVersion;
    private String remark;
    private String left = "取消";
    private String right = "确定";

    public AppUpdateDialog(@NonNull Context context, String oldVersion, String newVersion, String remark) {
        super(context, R.style.appUpdateDialog);
        this.context = context;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.remark = remark;
    }

    public AppUpdateDialog(@NonNull Context context, String title, String oldVersion, String newVersion, String remark, String left, String right) {
        super(context, R.style.appUpdateDialog);
        this.context = context;
        this.title = title;
        this.oldVersion = oldVersion;
        this.newVersion = newVersion;
        this.remark = remark;
        this.left = left;
        this.right = right;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_update_dialog);
        // 宽度全屏
        WindowManager windowManager = ((Activity) context).getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.width = display.getWidth() * 8 / 10; // 设置宽度
        getWindow().setAttributes(lp);
        setCanceledOnTouchOutside(true);
        TextView titleView = findViewById(R.id.update_title);
        TextView oldView = findViewById(R.id.update_old);
        TextView newView = findViewById(R.id.update_new);
        TextView remarkView = findViewById(R.id.update_remark);
        TextView leftView = findViewById(R.id.update_left);
        TextView rightView = findViewById(R.id.update_right);

        titleView.setText(title);
        oldView.setText("当前版本为:" + oldVersion);
        newView.setText("最新版本为:" + newVersion);
        if (TextUtils.isEmpty(remark)) {
            remarkView.setVisibility(View.GONE);
        } else {
            remarkView.setVisibility(View.VISIBLE);
            remarkView.setText(remark);
        }
        leftView.setText(left);
        rightView.setText(right);

        leftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
                mListener.left(AppUpdateDialog.this);
            }
        });
        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
                mListener.right(AppUpdateDialog.this);
            }
        });
    }

    public interface onDialogListener {
        void left(Dialog appUpdateDialog);

        void right(Dialog appUpdateDialog);
    }

    public void setOnDialogListener(onDialogListener listener) {
        this.mListener = listener;
    }
}
