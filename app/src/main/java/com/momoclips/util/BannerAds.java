package com.momoclips.util;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.applovin.mediation.ads.MaxAdView;
import com.momoclips.android.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.startapp.sdk.ads.banner.Banner;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.wortise.ads.banner.BannerAd;

public class BannerAds {
    public static void showBannerAds(Context context, LinearLayout mAdViewLayout) {
        if (Constant.isBanner) {
            switch (Constant.adNetworkType) {
                case "admob":
                    AdView mAdView = new AdView(context);
                    mAdView.setAdSize(AdSize.BANNER);
                    mAdView.setAdUnitId(Constant.bannerId);
                    AdRequest.Builder builder = new AdRequest.Builder();
                    mAdView.loadAd(builder.build());
                    mAdViewLayout.addView(mAdView);
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
                case "facebook":
                    com.facebook.ads.AdView adView = new com.facebook.ads.AdView(context, Constant.bannerId, com.facebook.ads.AdSize.BANNER_HEIGHT_50);
                    adView.loadAd();
                    mAdViewLayout.addView(adView);
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
                case "startapp":
                    Banner startAppBanner = new Banner((Activity) context);
                    mAdViewLayout.addView(startAppBanner);
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
                case "applovins":
                    MaxAdView maxAdView = new MaxAdView(Constant.bannerId, (Activity) context);
                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int heightPx = context.getResources().getDimensionPixelSize(R.dimen.applovin_banner_height);
                    maxAdView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
                    maxAdView.loadAd();
                    mAdViewLayout.addView(maxAdView);
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
                case "unityds":
                    BannerView bottomBanner = new BannerView((Activity) context, Constant.bannerId, new UnityBannerSize(320, 50));
                    bottomBanner.load();
                    mAdViewLayout.addView(bottomBanner);
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
                case "wortise":
                    BannerAd mBannerAd = new BannerAd(context);
                    mBannerAd.setAdSize(com.wortise.ads.AdSize.HEIGHT_50);
                    mBannerAd.setAdUnitId(Constant.bannerId);
                    mAdViewLayout.addView(mBannerAd);
                    mBannerAd.loadAd();
                    mAdViewLayout.setGravity(Gravity.CENTER);
                    break;
            }
        } else {
            mAdViewLayout.setVisibility(View.GONE);
        }
    }
}
