package com.momoclips.android;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.momoclips.item.ItemAbout;
import com.momoclips.util.API;
import com.momoclips.util.Constant;
import com.momoclips.util.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.wortise.ads.WortiseSdk;
import com.wortise.ads.consent.ConsentManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import kotlin.Unit;
import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;

public class SplashActivity extends AppCompatActivity {

    boolean mIsBackButtonPressed;
    MyApplication myApplication;
    String str_package;
    ArrayList<ItemAbout> mListItem;
    private Boolean isCancelled = false;
    int WAIT = 2000;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        myApplication = MyApplication.getInstance();
        mListItem = new ArrayList<>();

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_app_details");
        if (JsonUtils.isNetworkAvailable(SplashActivity.this)) {
            new MyTaskDev(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        } else {
            showToast(getString(R.string.no_connect));
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class MyTaskDev extends AsyncTask<String, Void, String> {

        String base64;

        private MyTaskDev(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        if (objJson.has("status")) {
                            final PrettyDialog dialog = new PrettyDialog(SplashActivity.this);
                            dialog.setTitle(getString(R.string.dialog_error))
                                    .setTitleColor(R.color.dialog_text)
                                    .setMessage(objJson.getString("msg"))
                                    .setMessageColor(R.color.dialog_text)
                                    .setAnimationEnabled(false)
                                    .setIcon(libs.mjn.prettydialog.R.drawable.pdlg_icon_close, R.color.dialog_color, new PrettyDialogCallback() {
                                        @Override
                                        public void onClick() {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    })
                                    .addButton(getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, new PrettyDialogCallback() {
                                        @Override
                                        public void onClick() {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    });
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            str_package = objJson.getString(Constant.APP_PACKAGE_NAME);
                            Constant.isBanner = objJson.getBoolean("banner_ad");
                            Constant.isInterstitial = objJson.getBoolean("interstital_ad");
                            Constant.isNative = objJson.getBoolean("native_ad");
                            Constant.adMobPublisherId = objJson.getString("publisher_id");
                            Constant.wortiseAppId=objJson.getString("wortise_app_id");

                            Constant.bannerId = objJson.getString("banner_ad_id");
                            Constant.interstitialId = objJson.getString("interstital_ad_id");
                            Constant.nativeId = objJson.getString("native_ad_id");

                            Constant.startAppId = objJson.getString("startapp_app_id");
                            Constant.adNetworkType = objJson.getString("ad_network");
                            Constant.appGameId = objJson.getString("unity_game_id");

                            Constant.interstitialClick = objJson.getInt("interstital_ad_click");
                            Constant.nativePosition = objJson.getInt("native_position");

                            Constant.appUpdateVersion = objJson.getInt("app_new_version");
                            Constant.appUpdateUrl = objJson.getString("app_redirect_url");
                            Constant.appUpdateDesc = objJson.getString("app_update_desc");
                            Constant.isAppUpdate = objJson.getBoolean("app_update_status");
                            Constant.isAppUpdateCancel = objJson.getBoolean("cancel_update_status");
                            Constant.appPrivacyUrl = objJson.getString("privacy_policy_link");

                            initializeAds();
                            if (str_package.equals(getPackageName())) {
                                new Handler().postDelayed(() -> {
                                    if (!isCancelled) {
                                        if (myApplication.getFirstIsLogin()) {
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    }

                                }, WAIT);

                            } else {
                                final PrettyDialog dialog = new PrettyDialog(SplashActivity.this);
                                dialog.setTitle(getString(R.string.dialog_error))
                                        .setTitleColor(R.color.dialog_text)
                                        .setMessage(getString(R.string.license_msg))
                                        .setMessageColor(R.color.dialog_text)
                                        .setAnimationEnabled(false)
                                        .setIcon(R.drawable.pdlg_icon_close, R.color.dialog_color, dialog::dismiss)
                                        .addButton(getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, () -> {
                                            dialog.dismiss();
                                            finish();
                                        });
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult();
            }
        }
    }

    private void setResult() {

    }

    public void showToast(String msg) {
        Toast.makeText(SplashActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        // set the flag to true so the next activity won't start up
        mIsBackButtonPressed = true;
        super.onBackPressed();

    }

    @Override
    protected void onDestroy() {
        isCancelled = true;
        super.onDestroy();
    }

    private void initializeAds() {
        switch (Constant.adNetworkType) {
            case "unityds":
                UnityAds.initialize(SplashActivity.this, Constant.appGameId, false, new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        Log.d(TAG, "Unity Ads Initialization Complete");
                    }

                    @Override
                    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                        Log.d(TAG, "Unity Ads Initialization Failed: [" + error + "] " + message);
                    }
                });
                break;
            case "applovins":
                AppLovinSdk.getInstance(SplashActivity.this).setMediationProvider(AppLovinMediationProvider.MAX);
                AppLovinSdk.getInstance(SplashActivity.this).initializeSdk(config -> {

                });
                break;
            case "startapp":
                StartAppSDK.init(this, Constant.startAppId, false);
                StartAppAd.disableSplash();

                break;
            case "wortise":
                WortiseSdk.initialize(this, Constant.wortiseAppId, () -> {
                    ConsentManager.request(SplashActivity.this);
                    return Unit.INSTANCE;
                });
                break;
        }

    }
}
