package com.kawakp.kp.oxygenerator.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kawakp.kp.oxygenerator.view.MainActivity;
import com.kawakp.kp.oxygenerator.view.LoginActivity;
import com.kawakp.kp.oxygenerator.view.password.PasswordActivity2;

/**
 * Created by lipy on 17-1-10.
 */

public class RedirectUtil {

    public enum BundleKey{
        MAIN(8001, "main"), /**{@link MainActivity}*/
        LOGIN(8002, "login"),    /**{@link LoginActivity}*/
        PASSWORD(8003, "password")    /**{@link PasswordActivity2}*/
        ;

        private String mHeader = "kawakp_ox_bundle_";
        private String mKey;
        private int mRequestCode;

        BundleKey(int requestCode, String key) {
            mRequestCode = requestCode;
            mKey = key;
        }

        public String getKey() {
            return mHeader + mKey;
        }

        public int getRequestCode() {
            return mRequestCode;
        }
    }

    /**
     * 不带数据传递的普通跳转
     * @param context
     * @param c
     */
    public static void redirectTo(Context context, Class c){
        Intent intent = new Intent(context, c);
        context.startActivity(intent);
        //((Activity)context).overridePendingTransition(R.anim.alpha_in, R.anim.alpha_out);
        //((Activity)context).overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    /**
     * 带数据传递的页面跳转
     * @param context
     * @param c
     * @param key
     * @param bundle
     */
    public static void redirectTo(Context context, Class c, BundleKey key, Bundle bundle){
        Intent intent = new Intent(context, c);
        if (bundle != null)
            intent.putExtra(key.getKey(), bundle);
        context.startActivity(intent);
    }

    /**
     * 不带数据传递的普通跳转,带数据返回
     * @param activity
     * @param c
     */
    public static void redirectToForResult(Activity activity, Class c, BundleKey key){
        Intent intent = new Intent(activity, c);
        activity.startActivityForResult(intent, key.getRequestCode());
    }

    /**
     * 带数据传递的页面跳转,带数据返回
     * @param activity
     * @param c
     * @param key
     * @param bundle
     */
    public static void redirectToForResult(Activity activity, Class c, BundleKey key, Bundle bundle){
        Intent intent = new Intent(activity, c);
        if (bundle != null)
            intent.putExtra(key.getKey(), bundle);
        activity.startActivityForResult(intent, key.getRequestCode());
    }
}
