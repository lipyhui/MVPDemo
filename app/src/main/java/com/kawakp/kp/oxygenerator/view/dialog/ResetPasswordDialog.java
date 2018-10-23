package com.kawakp.kp.oxygenerator.view.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kawakp.kp.oxygenerator.R;
import com.kawakp.kp.oxygenerator.app.oxygeneratorApplication;
import com.kawakp.kp.oxygenerator.util.RedirectUtil;
import com.kawakp.kp.oxygenerator.view.password.PasswordActivity1;

/**
 * 等待PLC是否成功
 */
public class ResetPasswordDialog extends AlertDialog {

    protected ResetPasswordDialog(Context context, int theme) {
        super(context, theme);
    }
    protected ResetPasswordDialog(Context context) {
        super(context);
    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
     /*   WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity= Gravity.BOTTOM;
        layoutParams.width= LayoutParams.MATCH_PARENT;
        layoutParams.height= LayoutParams.MATCH_PARENT;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);*/
    }

    public static class Builder implements View.OnClickListener {

        private Activity context;
        private LayoutInflater inflater = null;
        private ResetPasswordDialog Mydialog;
        private ImageView reset_del;
        private TextView confirm;

        public Builder(Activity context) {
            if (null == context || context.isFinishing())
                return;

            this.context = context;
            this.inflater= LayoutInflater.from(context);
        }

        @SuppressLint("Override")
        public ResetPasswordDialog create() {
            Mydialog = new ResetPasswordDialog(context, R.style.FullDialog);
            View view = inflater.inflate(R.layout.dialog_reset_password, null);
            reset_del = (ImageView) view.findViewById(R.id.reset_del);
            confirm = (TextView) view.findViewById(R.id.reset_confirm);

            reset_del.setOnClickListener(this);
            confirm.setOnClickListener(this);

            Mydialog.setView(view, 0, 0, 0, 0);
			//Mydialog.getWindow().setDimAmount(0);//设置昏暗度为0(全透明)
            Mydialog.setCancelable(false);
            Mydialog.setCanceledOnTouchOutside(true);// 设置点击屏幕Dialog消失
            Mydialog.show();
            return Mydialog;
        }

        /**
         * 判断dialog是否处于显示状态
         * @return
         */
        public boolean isShowing(){
            return Mydialog.isShowing();
        }

        /**
         * 取消dialog
         */
        public void cancel(){
            Mydialog.dismiss();
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.reset_del:
                    Mydialog.dismiss();
                    break;

                case R.id.reset_confirm:
                    Mydialog.dismiss();
                    oxygeneratorApplication.clearActivity();
                    RedirectUtil.redirectTo(context, PasswordActivity1.class);
                    break;
            }
        }
    }
}
