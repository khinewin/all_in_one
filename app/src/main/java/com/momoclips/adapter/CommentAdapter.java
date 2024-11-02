package com.momoclips.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.momoclips.android.MyApplication;
import com.momoclips.android.R;
import com.momoclips.android.SignInActivity;
import com.momoclips.item.ItemComment;
import com.momoclips.util.API;
import com.momoclips.util.Constant;
import com.momoclips.util.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ItemRowHolder> {

    ArrayList<ItemComment> dataList;
    Context mContext;
    JsonUtils jsonUtils;
    ProgressDialog pDialog;
    String strMessage;
    ItemComment singleItem;

    public CommentAdapter(Context context, ArrayList<ItemComment> dataList) {
        this.dataList = dataList;
        this.mContext = context;
        jsonUtils = new JsonUtils(mContext);
        pDialog = new ProgressDialog(mContext);
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_comment_item, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(final ItemRowHolder holder, @SuppressLint("RecyclerView") final int position) {
        singleItem = dataList.get(position);

        holder.text_user.setText(singleItem.getCommentName());
        holder.text_msg.setText(singleItem.getCommentMsg());
        if (MyApplication.getInstance().getUserId().equals(singleItem.getCommentUserId())) {
            holder.txt_report.setVisibility(View.GONE);
        }else {
            holder.txt_report.setVisibility(View.VISIBLE);
        }
        holder.txt_report.setOnClickListener(v -> {
            if (MyApplication.getInstance().getIsLogin()) {
             showReportComment(position);
            } else {
                final PrettyDialog dialog = new PrettyDialog(mContext);
                dialog.setTitle(mContext.getString(R.string.dialog_warning))
                        .setTitleColor(R.color.dialog_text)
                        .setMessage(mContext.getString(R.string.login_require))
                        .setMessageColor(R.color.dialog_text)
                        .setAnimationEnabled(false)
                        .setIcon(R.drawable.pdlg_icon_close, R.color.dialog_color, new PrettyDialogCallback() {
                            @Override
                            public void onClick() {
                                dialog.dismiss();
                            }
                        })
                        .addButton(mContext.getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, new PrettyDialogCallback() {
                            @Override
                            public void onClick() {
                                dialog.dismiss();
                                Intent intent_login = new Intent(mContext, SignInActivity.class);
                                intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent_login.putExtra("isfromdetail", true);
                                intent_login.putExtra("islogid", Constant.LATEST_CMT_IDD);
                                mContext.startActivity(intent_login);
                            }
                        });
                dialog.setCancelable(false);
                dialog.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public static class ItemRowHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView text_user, text_msg, txt_report;
        LinearLayout lyt_parent;

        private ItemRowHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            text_user = itemView.findViewById(R.id.text_user);
            lyt_parent = itemView.findViewById(R.id.rootLayout);
            text_msg = itemView.findViewById(R.id.text_comment);
            txt_report = itemView.findViewById(R.id.text_report);
        }
    }

    private void showReportComment(int pos) {
        final Dialog mDialog = new Dialog(mContext, R.style.Theme_AppCompat_Translucent);
        mDialog.setContentView(R.layout.report_dialog_comment);
        EditText editText = mDialog.findViewById(R.id.et_report);
        Button buttonSub = mDialog.findViewById(R.id.button_report_submit);
        Button buttonCancel = mDialog.findViewById(R.id.button_report_cancel);

        buttonCancel.setOnClickListener(view -> mDialog.dismiss());

        buttonSub.setOnClickListener(v -> {
            if (editText.getText().length() == 0) {
                Toast.makeText(mContext, mContext.getString(R.string.require_report), Toast.LENGTH_SHORT).show();
            } else {
                if (JsonUtils.isNetworkAvailable((Activity) mContext)){
                    JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
                    jsObj.addProperty("method_name", "user_report");
                    jsObj.addProperty("report", editText.getText().toString());
                    jsObj.addProperty("user_id", MyApplication.getInstance().getUserId());
                    jsObj.addProperty("post_id", dataList.get(pos).getCommentPostId());
                    jsObj.addProperty("comment_id",dataList.get(pos).getCommentId());
                    new MyTaskComment(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
                } else {
                    showToast(mContext.getString(R.string.no_connect));
                }
                mDialog.dismiss();
            }
         });
        mDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    private class MyTaskComment extends AsyncTask<String, Void, String> {

        String base64;

        private MyTaskComment(String base64) {
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
                showToast(mContext.getString(R.string.no_data));

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
            showToast(mContext.getString(R.string.error_title) + "\n" + strMessage);
        } else {
            showToast( strMessage);
        }
    }
    public void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    }

    public void showProgressDialog() {
        pDialog.setMessage(mContext.getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        pDialog.dismiss();
    }
}
