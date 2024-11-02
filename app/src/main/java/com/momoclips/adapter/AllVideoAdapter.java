package com.momoclips.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.multidex.BuildConfig;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.momoclips.android.MyApplication;
import com.momoclips.android.R;
import com.momoclips.android.SignInActivity;
import com.momoclips.favorite.DatabaseHelper;
import com.momoclips.fragment.ReportFragment;
import com.momoclips.item.ItemLatest;
import com.momoclips.util.Constant;
import com.momoclips.util.JsonUtils;
import com.momoclips.util.PopUpAds;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.wortise.ads.natives.GoogleNativeAd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import libs.mjn.prettydialog.PrettyDialog;
import libs.mjn.prettydialog.PrettyDialogCallback;


public class AllVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    Activity activity;
    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_Ad = 0;
    ArrayList<ItemLatest> dataList;
    DatabaseHelper databaseHelper;
    MyApplication myApplication;

    public AllVideoAdapter(Activity context, ArrayList<ItemLatest> dataList) {
        this.dataList = dataList;
        this.activity = context;
        databaseHelper = new DatabaseHelper(activity);
        myApplication = MyApplication.getInstance();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.row_all_item, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_Ad) {
            View view = LayoutInflater.from(activity).inflate(R.layout.admob_adapter, parent, false);
            return new AdOption(view);
        }
        return null;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            final ViewHolder viewHolder = (ViewHolder) holder;
            final ItemLatest singleItem = dataList.get(position);
            viewHolder.text.setText(singleItem.getLatestVideoName());
            viewHolder.text_view.setText(JsonUtils.Format(Integer.parseInt(singleItem.getLatestVideoView())));

            switch (singleItem.getLatestVideoType()) {
                case "local":
                case "server_url":
                case "vimeo":
                case "embeded_code":
                    Picasso.get().load(singleItem.getLatestVideoImgBig()).into(viewHolder.image);
                    break;
                case "youtube":
                    Picasso.get().load(Constant.YOUTUBE_IMAGE_FRONT + singleItem.getLatestVideoPlayId() + Constant.YOUTUBE_SMALL_IMAGE_BACK).into(viewHolder.image);
                    break;
                case "dailymotion":
                    Picasso.get().load(Constant.DAILYMOTION_IMAGE_PATH + singleItem.getLatestVideoPlayId()).into(viewHolder.image);
                    break;
            }

            viewHolder.lyt_parent.setOnClickListener(v -> {
                Constant.LATEST_IDD = singleItem.getLatestId();
                PopUpAds.ShowInterstitialAds(activity);
            });

            viewHolder.image_pop_up.setOnClickListener(view -> {
                PopupMenu popup = new PopupMenu(activity, viewHolder.image_pop_up);
                popup.inflate(R.menu.popup_menu);
                Menu popupMenu = popup.getMenu();
                if (databaseHelper.getFavouriteById(singleItem.getLatestId())) {
                    popupMenu.findItem(R.id.option_add_favourite).setVisible(false);
                } else {
                    popupMenu.findItem(R.id.option_remove_favourite).setVisible(false);
                }

                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.option_add_favourite:
                            ContentValues fav_list = new ContentValues();
                            fav_list.put(DatabaseHelper.KEY_ID, singleItem.getLatestId());
                            fav_list.put(DatabaseHelper.KEY_TITLE, singleItem.getLatestVideoName());
                            fav_list.put(DatabaseHelper.KEY_IMAGE, singleItem.getLatestVideoImgBig());
                            fav_list.put(DatabaseHelper.KEY_VIEW, singleItem.getLatestVideoView());
                            fav_list.put(DatabaseHelper.KEY_TYPE, singleItem.getLatestVideoType());
                            fav_list.put(DatabaseHelper.KEY_PID, singleItem.getLatestVideoPlayId());
                            fav_list.put(DatabaseHelper.KEY_TIME, singleItem.getLatestDuration());
                            fav_list.put(DatabaseHelper.KEY_CNAME, singleItem.getLatestCategoryName());
                            databaseHelper.addFavourite(DatabaseHelper.TABLE_FAVOURITE_NAME, fav_list, null);
                            Toast.makeText(activity, activity.getString(R.string.favourite_add), Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.option_remove_favourite:
                            databaseHelper.removeFavouriteById(singleItem.getLatestId());
                            Toast.makeText(activity, activity.getString(R.string.favourite_remove), Toast.LENGTH_SHORT).show();
                            break;
                        case R.id.option_share:
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT, activity.getString(R.string.share_msg) + "\n" + "https://play.google.com/store/apps/details?id=" + activity.getPackageName());
                            sendIntent.setType("text/plain");
                            activity.startActivity(sendIntent);
                            break;
                        case R.id.option_report:
                            if (myApplication.getIsLogin()) {
                                Bundle bundle = new Bundle();
                                bundle.putString("postId", singleItem.getLatestId());
                                ReportFragment reportFragment = new ReportFragment();
                                reportFragment.setArguments(bundle);
                                reportFragment.show(((FragmentActivity) activity).getSupportFragmentManager(), reportFragment.getTag());

                            } else {
                                final PrettyDialog dialog = new PrettyDialog(activity);
                                dialog.setTitle(activity.getString(R.string.dialog_warning))
                                        .setTitleColor(R.color.dialog_text)
                                        .setMessage(activity.getString(R.string.login_require))
                                        .setMessageColor(R.color.dialog_text)
                                        .setAnimationEnabled(false)
                                        .setIcon(R.drawable.pdlg_icon_close, R.color.dialog_color, new PrettyDialogCallback() {
                                            @Override
                                            public void onClick() {
                                                dialog.dismiss();
                                            }
                                        })
                                        .addButton(activity.getString(R.string.dialog_ok), R.color.dialog_white_text, R.color.dialog_color, new PrettyDialogCallback() {
                                            @Override
                                            public void onClick() {
                                                dialog.dismiss();
                                                Intent intent_login = new Intent(activity, SignInActivity.class);
                                                intent_login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                activity.startActivity(intent_login);
                                            }
                                        });
                                dialog.setCancelable(false);
                                dialog.show();

                            }
                            break;
                    }
                    return false;
                });
                popup.show();
            });

        } else if (holder.getItemViewType() == VIEW_TYPE_Ad) {

            final AdOption adOption = (AdOption) holder;
            if (Constant.isNative) {
                switch (Constant.adNetworkType) {
                    case "admob":
                        if (adOption.linearLayout.getChildCount() == 0) {
                            AdLoader adLoader = new AdLoader.Builder(activity, Constant.nativeId)
                                    .forNativeAd(nativeAd -> {
                                        NativeAdView adView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.admob_ad, adOption.linearLayout, false);
                                        populateUnifiedNativeAdView(nativeAd, adView);
                                        adOption.linearLayout.removeAllViews();
                                        adOption.linearLayout.addView(adView);
                                        adOption.linearLayout.setVisibility(View.VISIBLE);
                                    }).build();
                            AdRequest.Builder builder = new AdRequest.Builder();
                            adLoader.loadAd(builder.build());
                        }

                        break;
                    case "facebook":
                        if (adOption.linearLayout.getChildCount() == 0) {

                            LayoutInflater inflater = LayoutInflater.from(activity);
                            LinearLayout adView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout, adOption.linearLayout, false);

                            adOption.linearLayout.addView(adView);

                            // Add the AdOptionsView
                            final LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);

                            // Create native UI using the ad metadata.
                            final TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
                            final MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
                            final TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
                            final TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
                            final TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
                            final Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

                            final NativeAd nativeAd = new NativeAd(activity, Constant.nativeId);
                            NativeAdListener nativeAdListener = new NativeAdListener() {
                                @Override
                                public void onMediaDownloaded(Ad ad) {
                                    Log.d("status_data", "MediaDownloaded" + " " + ad.toString());
                                }

                                @Override
                                public void onError(Ad ad, AdError adError) {
                                    Toast.makeText(activity, adError.toString(), Toast.LENGTH_SHORT).show();
                                    Log.d("status_data", "error" + " " + adError.toString());
                                }

                                @Override
                                public void onAdLoaded(Ad ad) {
                                    // Race condition, load() called again before last ad was displayed
                                    if (nativeAd == null || nativeAd != ad) {
                                        return;
                                    }
                                    // Inflate Native Ad into Container
                                    Log.d("status_data", "on load" + " " + ad.toString());

                                    NativeAdLayout nativeAdLayout = new NativeAdLayout(activity);
                                    AdOptionsView adOptionsView = new AdOptionsView(activity, nativeAd, nativeAdLayout);
                                    adChoicesContainer.removeAllViews();
                                    adChoicesContainer.addView(adOptionsView, 0);

                                    // Set the Text.
                                    nativeAdTitle.setText(nativeAd.getAdvertiserName());
                                    nativeAdBody.setText(nativeAd.getAdBodyText());
                                    nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                                    nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                                    nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                                    sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

                                    // Create a list of clickable views
                                    List<View> clickableViews = new ArrayList<>();
                                    clickableViews.add(nativeAdTitle);
                                    clickableViews.add(nativeAdCallToAction);

                                    // Register the Title and CTA button to listen for clicks.
                                    nativeAd.registerViewForInteraction(
                                            adOption.linearLayout,
                                            nativeAdMedia,
                                            clickableViews);

                                }

                                @Override
                                public void onAdClicked(Ad ad) {
                                    Log.d("status_data", "AdClicked" + " " + ad.toString());
                                }

                                @Override
                                public void onLoggingImpression(Ad ad) {
                                    Log.d("status_data", "Impression" + " " + ad.toString());
                                }

                            };
                            // Request an ad
                            nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());
                        }
                        break;
                    case "startapp":
                        if (adOption.linearLayout.getChildCount() == 0) {

                            LayoutInflater inflater = LayoutInflater.from(activity);
                            CardView adView = (CardView) inflater.inflate(R.layout.native_start_item, adOption.linearLayout, false);

                            adOption.linearLayout.addView(adView);

                            ImageView icon = adView.findViewById(R.id.icon);
                            TextView title = adView.findViewById(R.id.title);
                            TextView description = adView.findViewById(R.id.description);
                            Button button = adView.findViewById(R.id.button);

                            final StartAppNativeAd nativeAd = new StartAppNativeAd(activity);

                            nativeAd.loadAd(new NativeAdPreferences()
                                    .setAdsNumber(1)
                                    .setAutoBitmapDownload(true)
                                    .setPrimaryImageSize(1), new AdEventListener() {
                                @Override
                                public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                    ArrayList<NativeAdDetails> ads = nativeAd.getNativeAds();    // get NativeAds list
                                    NativeAdDetails nativeAdDetails = ads.get(0);
                                    if (nativeAdDetails != null) {
                                        icon.setImageBitmap(nativeAdDetails.getImageBitmap());
                                        title.setText(nativeAdDetails.getTitle());
                                        description.setText(nativeAdDetails.getDescription());
                                        button.setText(nativeAdDetails.isApp() ? "Install" : "Open");

                                        nativeAdDetails.registerViewForInteraction(adView);

                                    }
                                }

                                @Override
                                public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                    if (BuildConfig.DEBUG) {
                                        Log.e("onFailedToReceiveAd: ", "" + ad.getErrorMessage());
                                    }
                                }
                            });
                        }
                        break;
                    case "applovins":
                        if (adOption.linearLayout.getChildCount() == 0) {
                            LayoutInflater inflater = LayoutInflater.from(activity);
                            FrameLayout nativeAdLayout = (FrameLayout) inflater.inflate(R.layout.activity_native_max_template, adOption.linearLayout, false);
                            MaxNativeAdLoader nativeAdLoader = new MaxNativeAdLoader(Constant.nativeId, activity);
                            nativeAdLoader.loadAd();
                            nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                                @Override
                                public void onNativeAdLoaded(@Nullable MaxNativeAdView maxNativeAdView, MaxAd maxAd) {
                                    super.onNativeAdLoaded(maxNativeAdView, maxAd);

                                    // Add ad view to view.
                                    nativeAdLayout.removeAllViews();
                                    nativeAdLayout.addView(maxNativeAdView);
                                    adOption.linearLayout.addView(nativeAdLayout);
                                }

                                @Override
                                public void onNativeAdLoadFailed(String s, MaxError maxError) {
                                    super.onNativeAdLoadFailed(s, maxError);
                                }

                                @Override
                                public void onNativeAdClicked(MaxAd maxAd) {
                                    super.onNativeAdClicked(maxAd);
                                }
                            });
                        }
                        break;
                    case "wortise":
                        if (adOption.linearLayout.getChildCount() == 0) {
                            GoogleNativeAd googleNativeAd = new GoogleNativeAd(
                                    activity, Constant.nativeId, new GoogleNativeAd.Listener() {
                                @Override
                                public void onNativeFailedToLoad(@NonNull GoogleNativeAd googleNativeAd, @NonNull com.wortise.ads.AdError adError) {

                                }

                                @Override
                                public void onNativeLoaded(@NonNull GoogleNativeAd googleNativeAd, @NonNull com.google.android.gms.ads.nativead.NativeAd nativeAd) {
                                    NativeAdView adView = (NativeAdView) activity.getLayoutInflater().inflate(R.layout.layout_native_ad_wortise, null);
                                    populateUnifiedNativeAdView(nativeAd, adView);
                                    adOption.linearLayout.removeAllViews();
                                    adOption.linearLayout.addView(adView);

                                    adOption.linearLayout.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onNativeClicked(@NonNull GoogleNativeAd googleNativeAd) {

                                }

                                @Override
                                public void onNativeImpression(@NonNull GoogleNativeAd googleNativeAd) {

                                }
                            });
                            googleNativeAd.load();
                        }
                        break;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position) != null ? VIEW_TYPE_ITEM : VIEW_TYPE_Ad;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image, image_pop_up;
        TextView text, text_view;
        LinearLayout lyt_parent;

        public ViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
            lyt_parent = itemView.findViewById(R.id.rootLayout);
            text_view = itemView.findViewById(R.id.text_view);
            image_pop_up = itemView.findViewById(R.id.image_pop_up);
        }
    }

    public static class AdOption extends RecyclerView.ViewHolder {

        LinearLayout linearLayout;

        public AdOption(View itemView) {
            super(itemView);

            linearLayout = itemView.findViewById(R.id.adView_admob);

        }
     }
    private void populateUnifiedNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd, NativeAdView adView) {
        com.google.android.gms.ads.nativead.MediaView mediaView = adView.findViewById(R.id.ad_media);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline is guaranteed to be in every UnifiedNativeAd.
        ((TextView) Objects.requireNonNull(adView.getHeadlineView())).setText(nativeAd.getHeadline());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getBodyView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getCallToActionView()).setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            Objects.requireNonNull(adView.getIconView()).setVisibility(View.GONE);
        } else {
            ((ImageView) Objects.requireNonNull(adView.getIconView())).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getPriceView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.INVISIBLE);
        } else {
            Objects.requireNonNull(adView.getStoreView()).setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            Objects.requireNonNull(adView.getStarRatingView()).setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) Objects.requireNonNull(adView.getStarRatingView()))
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            Objects.requireNonNull(adView.getAdvertiserView()).setVisibility(View.INVISIBLE);
        } else {
            ((TextView) Objects.requireNonNull(adView.getAdvertiserView())).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad. The SDK will populate the adView's MediaView
        // with the media content from this native ad.
        adView.setNativeAd(nativeAd);
    }

}
