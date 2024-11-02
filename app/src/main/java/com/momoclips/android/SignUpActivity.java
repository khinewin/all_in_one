package com.momoclips.android;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.tuyenmonkey.textdecorator.TextDecorator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import libs.mjn.prettydialog.PrettyDialog;

public class SignUpActivity extends AppCompatActivity implements Validator.ValidationListener {

    @NotEmpty
    @Length(min = 3, max = 35, trim = true, message = "Enter Valid Full Name")
    EditText edtFullName;

    @NotEmpty
    @Email(message = "Please Check and Enter a valid Email Address")
    EditText edtEmail;

    @NotEmpty
    @Password(message = "Enter a Valid Password")
    @Length(min = 6, message = "Enter a Password Correctly")
    EditText edtPassword;

    @Length(message = "Enter valid Phone Number", min = 0, max = 14)
    EditText edtMobile;

    Button btnSignUp;

    String strFullname, strPassengerId, strEmail, strPassword, strMobi, strMessage, saveType, saveAId;

    private Validator validator;

    TextView txtLogin;
    JsonUtils jsonUtils;
    Button  buttonMail;
    //Google login
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 007;
    private static final String EMAIL = "email";
    MyApplication myApplication;
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
        setContentView(R.layout.activity_sign_up);
        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());
        myApplication = MyApplication.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        buttonMail = findViewById(R.id.button_gm_activity);
        edtFullName = findViewById(R.id.editText_name_register);
        edtEmail = findViewById(R.id.editText_email_register);
        edtPassword = findViewById(R.id.editText_password_register);
        edtMobile = findViewById(R.id.editText_phoneNo_register);

        btnSignUp = findViewById(R.id.button_submit);
        txtLogin = findViewById(R.id.textView_login_register);

        btnSignUp.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            validator.validate();
        });

        txtLogin.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        tvSignInAccept = findViewById(R.id.textSignUpAccept);
        checkBoxAgree = findViewById(R.id.checkbox);

        setAcceptText();

        buttonMail.setOnClickListener(view -> {
            if (checkBoxAgree.isChecked()) {
                signIn();
            } else {
                Toast.makeText(SignUpActivity.this, getString(R.string.please_accept), Toast.LENGTH_SHORT).show();
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onValidationSucceeded() {
        if (checkBoxAgree.isChecked()) {
            strFullname = edtFullName.getText().toString();
            strEmail = edtEmail.getText().toString();
            strPassword = edtPassword.getText().toString();
            strMobi = edtMobile.getText().toString();

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
            jsObj.addProperty("method_name", "user_register");
            jsObj.addProperty("name", strFullname);
            jsObj.addProperty("email", strEmail);
            jsObj.addProperty("password", strPassword);
            jsObj.addProperty("phone", strMobi);
            jsObj.addProperty("type", "Normal");
            saveType = "Normal";
            if (JsonUtils.isNetworkAvailable(SignUpActivity.this)) {
                new MyTaskRegister(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
                Log.e("dataa",""+API.toBase64(jsObj.toString()));
            } else {
                showToast(getString(R.string.no_connect));
            }
        } else {
            Toast.makeText(SignUpActivity.this, getString(R.string.please_accept), Toast.LENGTH_SHORT).show();
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
    private class MyTaskRegister extends AsyncTask<String, Void, String> {

        String base64;

        private MyTaskRegister(String base64) {
            this.base64 = base64;
        }

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SignUpActivity.this);
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
                        strMessage = objJson.getString(Constant.MSG);
                        Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
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
            edtEmail.setText("");
            edtEmail.requestFocus();
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
            final PrettyDialog dialog = new PrettyDialog(this);
            dialog.setTitle(getString(R.string.dialog_success))
                    .setTitleColor(R.color.dialog_text)
                    .setMessage(strMessage)
                    .setMessageColor(R.color.dialog_text)
                    .setAnimationEnabled(false)
                    .setIcon(R.drawable.pdlg_icon_success, R.color.dialog_color, dialog::dismiss)
                    .addButton(getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, () -> {
                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });
            dialog.setCancelable(false);
            dialog.show();

        }
    }

    public void showToast(String msg) {
        Toast.makeText(SignUpActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    //Google login
    private void signIn() {
        if (JsonUtils.isNetworkAvailable(SignUpActivity.this)) {
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
            if (JsonUtils.isNetworkAvailable(SignUpActivity.this)) {
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
            pDialog = new ProgressDialog(SignUpActivity.this);
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
                            strFullname = objJson.getString(Constant.USER_NAME);
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
            myApplication.saveIsLogin(true);
            myApplication.saveLogin(strPassengerId, strFullname, strEmail, saveType, saveAId);
            ActivityCompat.finishAffinity(SignUpActivity.this);
            Intent i = new Intent(SignUpActivity.this, MainActivity.class);
            startActivity(i);
            finish();

        }
    }

    private void setAcceptText() {
        TextDecorator
                .decorate(tvSignInAccept, getString(R.string.sign_up_accept, getString(R.string.menu_privacy)))
                .setTextColor(R.color.terms_text_menu, getString(R.string.menu_privacy))
                .makeTextClickable((view, text) -> {
                    Intent intent = new Intent(SignUpActivity.this, ActivityPrivacy.class);
                    startActivity(intent);
                }, true, getString(R.string.menu_privacy))
                .build();
    }
}
