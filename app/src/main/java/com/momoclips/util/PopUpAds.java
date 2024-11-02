package com.momoclips.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.momoclips.android.ActivityVideoDetails;
import com.momoclips.android.R;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.CacheFlag;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

public class PopUpAds {

    public static ProgressDialog pDialog;

    public static void ShowInterstitialAds(Context context) {

        if (Constant.isInterstitial) {
            switch (Constant.adNetworkType) {
                case "admob":
                    Constant.AD_COUNT++;
                    if (Constant.AD_COUNT == Constant.interstitialClick) {
                        Constant.AD_COUNT = 0;
                        Loading(context);
                        AdRequest.Builder builder = new AdRequest.Builder();
                        InterstitialAd.load(context, Constant.interstitialId, builder.build(), new InterstitialAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                super.onAdLoaded(interstitialAd);
                                interstitialAd.show((Activity) context);
                                pDialog.dismiss();
                                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        super.onAdDismissedFullScreenContent();
                                        goDetailsScreen(context);
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                        super.onAdFailedToShowFullScreenContent(adError);
                                        pDialog.dismiss();
                                        goDetailsScreen(context);
                                    }
                                });
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                super.onAdFailedToLoad(loadAdError);
                                pDialog.dismiss();
                                goDetailsScreen(context);
                            }
                        });
                    } else {
                        goDetailsScreen(context);
                    }
                    break;
                case "facebook":
                    Constant.AD_COUNT++;
                    if (Constant.AD_COUNT == Constant.interstitialClick) {
                        Constant.AD_COUNT = 0;
                        Loading(context);
                        final com.facebook.ads.InterstitialAd mInterstitialfb = new com.facebook.ads.InterstitialAd(context, Constant.interstitialId);
                        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                            @Override
                            public void onInterstitialDisplayed(Ad ad) {
                            }

                            @Override
                            public void onInterstitialDismissed(Ad ad) {
                                goDetailsScreen(context);
                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {
                                pDialog.dismiss();
                                goDetailsScreen(context);
                            }

                            @Override
                            public void onAdLoaded(Ad ad) {
                                pDialog.dismiss();
                                mInterstitialfb.show();
                            }

                            @Override
                            public void onAdClicked(Ad ad) {
                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {
                            }
                        };
                        com.facebook.ads.InterstitialAd.InterstitialLoadAdConfig loadAdConfig = mInterstitialfb.buildLoadAdConfig().withAdListener(interstitialAdListener).withCacheFlags(CacheFlag.ALL).build();
                        mInterstitialfb.loadAd(loadAdConfig);
                    } else {
                        goDetailsScreen(context);
                    }
                    break;
                case "unityds":
                    Constant.AD_COUNT++;
                    if (Constant.AD_COUNT == Constant.interstitialClick) {
                        Constant.AD_COUNT = 0;
                        Loading(context);
                        UnityAds.show((Activity) context, Constant.interstitialId, new IUnityAdsShowListener() {
                            @Override
                            public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                                pDialog.dismiss();
                                goDetailsScreen(context);
                            }

                            @Override
                            public void onUnityAdsShowStart(String s) {
                            }

                            @Override
                            public void onUnityAdsShowClick(String s) {
                            }

                            @Override
                            public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState unityAdsShowCompletionState) {
                                pDialog.dismiss();
                                goDetailsScreen(context);
                            }
                        });

                    } else {
                        goDetailsScreen(context);
                    }
                    break;
                case "startapp":
                    Constant.AD_COUNT++;
                    if (Constant.AD_COUNT == Constant.interstitialClick) {
                        Constant.AD_COUNT = 0;
                        Loading(context);
                        StartAppAd startAppAd = new StartAppAd(context);
                        startAppAd.loadAd(new AdEventListener() {
                            @Override
                            public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                pDialog.dismiss();
                                startAppAd.showAd(new AdDisplayListener() {
                                    @Override
                                    public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                                        pDialog.dismiss();
                                        goDetailsScreen(context);
                                    }

                                    @Override
                                    public void adDisplayed(com.startapp.sdk.adsbase.Ad ad) {

                                    }

                                    @Override
                                    public void adClicked(com.startapp.sdk.adsbase.Ad ad) {
                                        pDialog.dismiss();
                                    }

                                    @Override
                                    public void adNotDisplayed(com.startapp.sdk.adsbase.Ad ad) {
                                        pDialog.dismiss();
                                        goDetailsScreen(context);
                                    }
                                });
                            }

                            @Override
                            public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                pDialog.dismiss();
                                goDetailsScreen(context);
                            }
                        });

                    } else {
                        goDetailsScreen(context);
                    }
                    break;
                case "applovins":
                    Constant.AD_COUNT++;
                    if (Constant.AD_COUNT == Constant.interstitialClick) {
                        Constant.AD_COUNT = 0;
                        Loading(context);
                        MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(Constant.interstitialId, (Activity) context);
                        maxInterstitialAd.setListener(new MaxAdListener() {
                            @Override
                            public void onAdLoaded(MaxAd ad) {
                                pDialog.dismiss();
                                maxInterstitialAd.showAd();
                            }

                            @Override
                            public void onAdDisplayed(MaxAd ad) {
                            }

                            @Override
                            public void onAdHidden(MaxAd ad) {
                                pDialog.dismiss();
                                goDetailsScreen(context);
                            }

                            @Override
                            public void onAdClicked(MaxAd ad) {
                            }

                            @Override
                            public void onAdLoadFailed(String adUnitId, MaxError error) {
                                pDialog.dismiss();
                                goDetailsScreen(context);
                            }

                            @Override
                            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                pDialog.dismiss();
                                goDetailsScreen(context);
                            }
                        });
                        // Load the first ad
                        maxInterstitialAd.loadAd();

                    } else {
                        goDetailsScreen(context);
                    }
                    break;
                case "wortise":
                    Constant.AD_COUNT++;
                    if (Constant.AD_COUNT == Constant.interstitialClick) {
                        Constant.AD_COUNT = 0;
                        Loading(context);
                        com.wortise.ads.interstitial.InterstitialAd wInterstitial = new com.wortise.ads.interstitial.InterstitialAd(context, Constant.interstitialId);
                        wInterstitial.setListener(new com.wortise.ads.interstitial.InterstitialAd.Listener() {
                            @Override
                            public void onInterstitialImpression(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {

                            }

                            @Override
                            public void onInterstitialFailedToShow(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                pDialog.dismiss();
                                goDetailsScreen(context);
                            }

                            @Override
                            public void onInterstitialFailedToLoad(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                pDialog.dismiss();
                                goDetailsScreen(context);
                            }

                            @Override
                            public void onInterstitialClicked(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {

                            }

                            @Override
                            public void onInterstitialDismissed(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                                pDialog.dismiss();
                                goDetailsScreen(context);
                            }

                            @Override
                            public void onInterstitialLoaded(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                                if (wInterstitial.isAvailable()) {
                                    wInterstitial.showAd();
                                }
                            }

                            @Override
                            public void onInterstitialShown(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                                pDialog.dismiss();
                            }
                        });
                        wInterstitial.loadAd();

                    } else {
                        goDetailsScreen(context);
                    }
                    break;
            }
        } else {
            goDetailsScreen(context);
        }
    }

    public static void Loading(Context context) {
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(context.getResources().getString(R.string.loading));
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private static void goDetailsScreen(Context context) {
        Intent intent_single = new Intent(context, ActivityVideoDetails.class);
        intent_single.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent_single);
    }
}
