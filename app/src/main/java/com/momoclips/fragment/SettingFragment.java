package com.momoclips.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.momoclips.android.ActivityAboutUs;
import com.momoclips.android.ActivityPrivacy;
import com.momoclips.android.MyApplication;
import com.momoclips.android.R;
import com.momoclips.android.SignInActivity;
import com.momoclips.util.API;
import com.momoclips.util.Constant;
import com.momoclips.util.JsonUtils;
import com.momoclips.util.NotificationTiramisu;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;


public class SettingFragment extends Fragment {

    MyApplication MyApp;
    SwitchCompat notificationSwitch, notificationSwitchMode;
    LinearLayout lytAbout, lytPrivacy, lytMoreApp, layRateApp, layShareApp, layDeleteAcc;
    View viewDeleteAcc;
    ProgressDialog pDialog;
    String strMessage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_setting, container, false);

        MyApp = MyApplication.getInstance();
        notificationSwitch = rootView.findViewById(R.id.switch_notification);
        lytAbout = rootView.findViewById(R.id.lytAbout);
        lytPrivacy = rootView.findViewById(R.id.lytPrivacy);
        lytMoreApp = rootView.findViewById(R.id.lytMoreApp);
        layRateApp = rootView.findViewById(R.id.lytRateApp);
        layShareApp = rootView.findViewById(R.id.lytShareApp);
        notificationSwitch.setChecked(MyApp.getNotification());
        notificationSwitchMode = rootView.findViewById(R.id.switch_notification_night);
        layDeleteAcc = rootView.findViewById(R.id.lytDeleteAcc);
        viewDeleteAcc=rootView.findViewById(R.id.viewDeleteAcc);
        pDialog = new ProgressDialog(getActivity());

        if (MyApp.getIsLogin()) {
            layDeleteAcc.setVisibility(View.VISIBLE);
            viewDeleteAcc.setVisibility(View.VISIBLE);
        } else {
            layDeleteAcc.setVisibility(View.GONE);
            viewDeleteAcc.setVisibility(View.GONE);
        }

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            notificationSwitchMode.setChecked(true);

        notificationSwitchMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MyApplication.getInstance().setIsNightModeEnabled(isChecked);
            MyApplication.getInstance().onCreate();
            Intent intent = requireActivity().getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            requireActivity().finish();
            startActivity(intent);
        });

        layRateApp.setOnClickListener(view -> {
            Uri uri = Uri.parse("market://details?id=" + requireActivity().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + requireActivity().getPackageName())));
            }
        });

        notificationSwitch.setChecked(NotificationTiramisu.isNotificationChecked(requireContext()));//myApplication.getNotification()
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                NotificationTiramisu.takePermissionSettings(requireActivity(), notificationSwitch, activityResultLauncher);
            } else {
                OneSignal.disablePush(false);
                MyApp.saveIsNotification(false);
                OneSignal.unsubscribeWhenNotificationsAreDisabled(false);
            }
        });

        lytAbout.setOnClickListener(v -> {
            Intent intent_ab = new Intent(requireActivity(), ActivityAboutUs.class);
            startActivity(intent_ab);
        });

        lytPrivacy.setOnClickListener(v -> {
            Intent intent_pri = new Intent(requireActivity(), ActivityPrivacy.class);
            startActivity(intent_pri);
        });

        lytMoreApp.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps)))));

        layShareApp.setOnClickListener(view -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_msg) + "\n" + "https://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        });

        layDeleteAcc.setOnClickListener(view -> DeleteAccount());

        return rootView;
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        NotificationTiramisu.setCheckedFromSetting(requireActivity(), notificationSwitch);
    });


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private void DeleteAccount() {
        new AlertDialog.Builder(requireActivity())
                .setTitle(getString(R.string.delete_acc))
                .setMessage(getString(R.string.delete_acc_info))
                .setPositiveButton(R.string.delete_acc_btn, (dialog, which) -> {
                    if (JsonUtils.isNetworkAvailable(requireActivity())) {
                        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
                        jsObj.addProperty("method_name", "delete_user_account");
                        jsObj.addProperty("user_id", MyApp.getUserId());
                        new DeleteAccData(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
                    } else {
                        showToast(getString(R.string.no_connect));
                    }
                })
                .setNegativeButton(R.string.delete_acc_no, (dialog, which) -> {
                    // do nothing
                })
                .setIcon(R.mipmap.app_icon)
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteAccData extends AsyncTask<String, Void, String> {

        String base64;

        private DeleteAccData(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            dismissProgressDialog();

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    strMessage = mainJson.getString(Constant.MSG);
                    Constant.GET_SUCCESS_MSG = mainJson.getInt(Constant.SUCCESS);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult();
            }
        }
    }

    public void setResult() {

        if (Constant.GET_SUCCESS_MSG == 0) {
            showToast(getString(R.string.error_title) + "\n" + strMessage);
        } else {
            showToast(strMessage);
            MyApp.saveIsLogin(false);
            MyApp.setUserId("");
            Intent intent = new Intent(requireActivity(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            requireActivity().finishAffinity();
        }
    }

    public void showToast(String msg) {
        Toast.makeText(requireActivity(), msg, Toast.LENGTH_LONG).show();
    }

    public void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        pDialog.dismiss();
    }

}
