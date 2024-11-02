package com.momoclips.android;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import com.momoclips.util.API;
import com.momoclips.util.Constant;
import com.momoclips.util.JsonUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.tuyenmonkey.textdecorator.TextDecorator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cn.refactor.library.SmoothCheckBox;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import libs.mjn.prettydialog.PrettyDialog;

public class SignInActivity extends AppCompatActivity implements Validator.ValidationListener {

    String strEmail, strPassword, strMessage, strName, strPassengerId, saveType, saveAId, saveImage;
    @NotEmpty
    @Email(message = "Please Check and Enter a valid Email Address")
    EditText edtEmail;

    @NotEmpty
    @Password(message = "Enter a Valid Password")
    EditText edtPassword;
    private Validator validator;
    Button btnSingIn, btnSkip;
    MyApplication MyApp;
    TextView textForgot, textSignUp;
    JsonUtils jsonUtils;
    public static final String mypreference = "mypref";
    public static final String pref_email = "pref_email";
    public static final String pref_password = "pref_password";
    public static final String pref_check = "pref_check";
    static SharedPreferences pref;
    private static SharedPreferences.Editor editor;
    boolean iswhichscreen;
    String newsdetail;
    Button  buttonMail;
    SmoothCheckBox checkBox;
    //Google login
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 007;
    private static final String EMAIL = "email";
    AppCompatTextView tvSignInAccept;
    AppCompatCheckBox checkBoxAgree;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_in);
        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());
        pref = getSharedPreferences(mypreference, 0); // 0 - for private mode
        editor = pref.edit();

        MyApp = MyApplication.getInstance();
        MyApp.saveFirstIsLogin(true);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        edtEmail = findViewById(R.id.editText_email_login_activity);
        edtPassword = findViewById(R.id.editText_password_login_activity);
        btnSingIn = findViewById(R.id.button_login_activity);
        btnSkip = findViewById(R.id.button_skip_login_activity);
        buttonMail = findViewById(R.id.button_gm_activity);
        textForgot = findViewById(R.id.textView_forget_password_login);
        textSignUp = findViewById(R.id.textView_signup_login);
        checkBox = findViewById(R.id.checkbox_login_activity);
        checkBox.setChecked(false);
        if (pref.getBoolean(pref_check, false)) {
            edtEmail.setText(pref.getString(pref_email, null));
            edtPassword.setText(pref.getString(pref_password, null));
            checkBox.setChecked(true);
        } else {
            edtEmail.setText("");
            edtPassword.setText("");
            checkBox.setChecked(false);
        }

        tvSignInAccept = findViewById(R.id.textSignUpAccept);
        checkBoxAgree = findViewById(R.id.checkbox);

        setAcceptText();

        Intent intent = getIntent();
        iswhichscreen = intent.getBooleanExtra("isfromdetail", false);
        newsdetail = intent.getStringExtra("islogid");

        btnSingIn.setOnClickListener(v -> validator.validate());

        textSignUp.setOnClickListener(v -> {
            Intent intent1 = new Intent(SignInActivity.this, SignUpActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent1);
        });

        btnSkip.setOnClickListener(v -> {
            Intent intent12 = new Intent(SignInActivity.this, MainActivity.class);
            intent12.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent12);
            finish();
        });

        textForgot.setOnClickListener(v -> {
            Intent intent13 = new Intent(SignInActivity.this, ActivityForgot.class);
            intent13.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent13);
        });


        buttonMail.setOnClickListener(view -> {
            if (checkBoxAgree.isChecked()) {
                signIn();
            } else {
                Toast.makeText(SignInActivity.this, getString(R.string.please_accept), Toast.LENGTH_SHORT).show();
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onValidationSucceeded() {
        if (checkBoxAgree.isChecked()) {
            strEmail = edtEmail.getText().toString();
            strPassword = edtPassword.getText().toString();

            if (checkBox.isChecked()) {
                editor.putString(pref_email, edtEmail.getText().toString());
                editor.putString(pref_password, edtPassword.getText().toString());
                editor.putBoolean(pref_check, true);
                editor.commit();
            } else {
                editor.putBoolean(pref_check, false);
                editor.commit();
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
            jsObj.addProperty("method_name", "user_login");
            jsObj.addProperty("email", strEmail);
            jsObj.addProperty("password", strPassword);
            jsObj.addProperty("auth_id", "");
            jsObj.addProperty("type", "Normal");
            saveType = "Normal";
            if (JsonUtils.isNetworkAvailable(SignInActivity.this)) {
                new MyTaskLogin(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
            } else {
                showToast(getString(R.string.no_connect));
            }
        } else {
            Toast.makeText(SignInActivity.this, getString(R.string.please_accept), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class MyTaskLogin extends AsyncTask<String, Void, String> {

        String base64;

        private MyTaskLogin(String base64) {
            this.base64 = base64;
        }

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SignInActivity.this);
            pDialog.setMessage(getString(R.string.loading));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (null != pDialog && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
                        if (objJson.has(Constant.USER_NAME)) {
                            strName = objJson.getString(Constant.USER_NAME);
                            strPassengerId = objJson.getString(Constant.USER_ID);
                            strEmail = objJson.getString(Constant.USER_EMAIL);
                        } else {
                            strMessage = objJson.getString("msg");
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResult();
            }

        }
    }

    public void setResult() {

        if (Constant.GET_SUCCESS_MSG == 0) {
            final PrettyDialog dialog = new PrettyDialog(this);
            dialog.setTitle(getString(R.string.dialog_error))
                    .setTitleColor(R.color.dialog_text)
                    .setMessage(strMessage)
                    .setMessageColor(R.color.dialog_text)
                    .setAnimationEnabled(false)
                    .setIcon(R.drawable.pdlg_icon_close, R.color.dialog_color, dialog::dismiss)
                    .addButton(getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, () -> dialog.dismiss());
            dialog.setCancelable(false);
            dialog.show();

        } else {
            MyApp.saveIsLogin(true);
            MyApp.saveLogin(strPassengerId, strName, strEmail, saveType, saveAId);
            if (iswhichscreen) {
                Intent i = new Intent(SignInActivity.this, ActivityVideoDetails.class);
                i.putExtra("isfromdetail", newsdetail);
                i.putExtra("Id", Constant.LATEST_IDD);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
            } else {
                ActivityCompat.finishAffinity(SignInActivity.this);
                Intent i = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }

        }
    }

    public void showToast(String msg) {
        Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    //Google login
    private void signIn() {
        if (JsonUtils.isNetworkAvailable(SignInActivity.this)) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        } else {
            showToast(getString(R.string.no_connect));
        }
    }

    //Google login get callback
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    //Google login
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            assert account != null;
            String id = account.getId();
            String name = account.getDisplayName();
            String email = account.getEmail();
            if (account.getPhotoUrl() == null) {
                saveImage = "";
            } else {
                saveImage = account.getPhotoUrl().toString();
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
            jsObj.addProperty("method_name", "user_register");
            jsObj.addProperty("name", name);
            jsObj.addProperty("email", email);
            jsObj.addProperty("password", "");
            jsObj.addProperty("phone", "");
            jsObj.addProperty("auth_id", id);
            jsObj.addProperty("type", "Google");
            saveType = "Google";
            saveAId = id;
            if (JsonUtils.isNetworkAvailable(SignInActivity.this)) {
                new MyTaskLoginSocial(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
            } else {
                showToast(getString(R.string.no_connect));
            }

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class MyTaskLoginSocial extends AsyncTask<String, Void, String> {

        String base64;

        private MyTaskLoginSocial(String base64) {
            this.base64 = base64;
        }

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SignInActivity.this);
            pDialog.setMessage(getString(R.string.loading));
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (null != pDialog && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
                        if (objJson.has(Constant.USER_NAME)) {
                            strName = objJson.getString(Constant.USER_NAME);
                            strPassengerId = objJson.getString(Constant.USER_ID);
                            strEmail = objJson.getString(Constant.USER_EMAIL);
                        } else {
                            strMessage = objJson.getString("msg");
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setResultSocial();
            }

        }
    }

    public void setResultSocial() {

        if (Constant.GET_SUCCESS_MSG == 0) {
            final PrettyDialog dialog = new PrettyDialog(this);
            dialog.setTitle(getString(R.string.dialog_error))
                    .setTitleColor(R.color.dialog_text)
                    .setMessage(strMessage)
                    .setMessageColor(R.color.dialog_text)
                    .setAnimationEnabled(false)
                    .setIcon(R.drawable.pdlg_icon_close, R.color.dialog_color, dialog::dismiss)
                    .addButton(getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, dialog::dismiss);
            dialog.setCancelable(false);
            dialog.show();

        } else {
            MyApp.saveIsLogin(true);
            MyApp.saveLogin(strPassengerId, strName, strEmail, saveType, saveAId);
            ActivityCompat.finishAffinity(SignInActivity.this);
            Intent i = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(i);
            finish();

        }
    }

    private void setAcceptText() {
        TextDecorator
                .decorate(tvSignInAccept, getString(R.string.sign_in_accept, getString(R.string.menu_privacy)))
                .makeTextClickable((view, text) -> {
                    Intent intent = new Intent(SignInActivity.this, ActivityPrivacy.class);
                    startActivity(intent);
                }, true, getString(R.string.menu_privacy))
                .setTextColor(R.color.terms_text_menu, getString(R.string.menu_privacy))
                .build();
    }
}
