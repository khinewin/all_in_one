package com.momoclips.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.momoclips.adapter.CategoryAdapter;
import com.momoclips.android.MainActivity;
import com.momoclips.android.R;
import com.momoclips.item.ItemCategory;
import com.momoclips.util.API;
import com.momoclips.util.Constant;
import com.momoclips.util.ItemOffsetDecoration;
import com.momoclips.util.JsonUtils;
import com.momoclips.util.RecyclerTouchListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.CacheFlag;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class CategoryFragment extends Fragment {

    ArrayList<ItemCategory> mListItem;
    public RecyclerView recyclerView;
    CategoryAdapter categoryAdapter;
    private ProgressBar progressBar;
    private FragmentManager fragmentManager;
    ItemCategory itemCategory;
    private ProgressDialog pDialog;
    int j = 1;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_category, container, false);

        mListItem = new ArrayList<>();
        ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_category));
        progressBar = rootView.findViewById(R.id.progressBar);
        recyclerView = rootView.findViewById(R.id.rv_video);
        //recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(requireActivity(), R.dimen.item_offset);
        recyclerView.addItemDecoration(itemDecoration);
        fragmentManager = getFragmentManager();
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (categoryAdapter.getItemViewType(position) == 0) {
                    return 2;
                }
                return 1;
            }
        });

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_category");
        if (JsonUtils.isNetworkAvailable(requireActivity())) {
            new getSubCat(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                itemCategory = mListItem.get(position);

                if (itemCategory != null) {
                    Constant.CATEGORY_IDD = itemCategory.getCategoryId();
                    Constant.CATEGORY_TITLEE = itemCategory.getCategoryName();
                    if (Constant.isInterstitial) {
                        switch (Constant.adNetworkType) {
                            case "admob":
                                Constant.AD_COUNT++;
                                if (Constant.AD_COUNT == Constant.interstitialClick) {
                                    Constant.AD_COUNT = 0;
                                    Loading();

                                    AdRequest.Builder builder = new AdRequest.Builder();
                                    InterstitialAd.load(requireActivity(), Constant.interstitialId, builder.build(), new InterstitialAdLoadCallback() {
                                        @Override
                                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                            super.onAdLoaded(interstitialAd);
                                            interstitialAd.show((Activity) requireActivity());
                                            pDialog.dismiss();
                                            interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                                @Override
                                                public void onAdDismissedFullScreenContent() {
                                                    super.onAdDismissedFullScreenContent();
                                                    gotoNextScreen();
                                                }

                                                @Override
                                                public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                                                    super.onAdFailedToShowFullScreenContent(adError);
                                                    pDialog.dismiss();
                                                    gotoNextScreen();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                            super.onAdFailedToLoad(loadAdError);
                                            pDialog.dismiss();
                                            gotoNextScreen();
                                        }
                                    });
                                } else {
                                    gotoNextScreen();
                                }
                                break;
                            case "facebook":
                                Constant.AD_COUNT++;
                                if (Constant.AD_COUNT == Constant.interstitialClick) {
                                    Constant.AD_COUNT = 0;
                                    Loading();
                                    final com.facebook.ads.InterstitialAd mInterstitialfb = new com.facebook.ads.InterstitialAd(requireActivity(), Constant.interstitialId);
                                    InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                                        @Override
                                        public void onInterstitialDisplayed(Ad ad) {
                                        }

                                        @Override
                                        public void onInterstitialDismissed(Ad ad) {
                                            gotoNextScreen();
                                        }

                                        @Override
                                        public void onError(Ad ad, AdError adError) {
                                            pDialog.dismiss();
                                            gotoNextScreen();
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
                                    gotoNextScreen();
                                }
                                break;
                            case "unityds":
                                Constant.AD_COUNT++;
                                if (Constant.AD_COUNT == Constant.interstitialClick) {
                                    Constant.AD_COUNT = 0;
                                    Loading();
                                    UnityAds.show((Activity) requireActivity(), Constant.interstitialId, new IUnityAdsShowListener() {
                                        @Override
                                        public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {
                                            pDialog.dismiss();
                                            gotoNextScreen();
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
                                            gotoNextScreen();
                                        }
                                    });

                                } else {
                                    gotoNextScreen();
                                }
                                break;
                            case "startapp":
                                Constant.AD_COUNT++;
                                if (Constant.AD_COUNT == Constant.interstitialClick) {
                                    Constant.AD_COUNT = 0;
                                    Loading();
                                    StartAppAd startAppAd = new StartAppAd(requireActivity());
                                    startAppAd.loadAd(new AdEventListener() {
                                        @Override
                                        public void onReceiveAd(@NonNull com.startapp.sdk.adsbase.Ad ad) {
                                            pDialog.dismiss();
                                            startAppAd.showAd(new AdDisplayListener() {
                                                @Override
                                                public void adHidden(com.startapp.sdk.adsbase.Ad ad) {
                                                    pDialog.dismiss();
                                                    gotoNextScreen();
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
                                                    gotoNextScreen();
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailedToReceiveAd(@Nullable com.startapp.sdk.adsbase.Ad ad) {
                                            pDialog.dismiss();
                                            gotoNextScreen();
                                        }
                                    });

                                } else {
                                    gotoNextScreen();
                                }
                                break;
                            case "applovins":
                                Constant.AD_COUNT++;
                                if (Constant.AD_COUNT == Constant.interstitialClick) {
                                    Constant.AD_COUNT = 0;
                                    Loading();
                                    MaxInterstitialAd maxInterstitialAd = new MaxInterstitialAd(Constant.interstitialId, (Activity) requireActivity());
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
                                            gotoNextScreen();
                                        }

                                        @Override
                                        public void onAdClicked(MaxAd ad) {
                                        }

                                        @Override
                                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                                            pDialog.dismiss();
                                            gotoNextScreen();
                                        }

                                        @Override
                                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                                            pDialog.dismiss();
                                            gotoNextScreen();
                                        }
                                    });
                                    // Load the first ad
                                    maxInterstitialAd.loadAd();

                                } else {
                                    gotoNextScreen();
                                }
                                break;
                            case "wortise":
                                Constant.AD_COUNT++;
                                if (Constant.AD_COUNT == Constant.interstitialClick) {
                                    Constant.AD_COUNT = 0;
                                    Loading();
                                    com.wortise.ads.interstitial.InterstitialAd wInterstitial = new com.wortise.ads.interstitial.InterstitialAd(requireActivity(), Constant.interstitialId);
                                    wInterstitial.setListener(new com.wortise.ads.interstitial.InterstitialAd.Listener() {
                                        @Override
                                        public void onInterstitialImpression(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {

                                        }

                                        @Override
                                        public void onInterstitialFailedToShow(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                            pDialog.dismiss();
                                            gotoNextScreen();
                                        }

                                        @Override
                                        public void onInterstitialFailedToLoad(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd, @NonNull com.wortise.ads.AdError adError) {
                                            pDialog.dismiss();
                                            gotoNextScreen();
                                        }

                                        @Override
                                        public void onInterstitialClicked(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {

                                        }

                                        @Override
                                        public void onInterstitialDismissed(@NonNull com.wortise.ads.interstitial.InterstitialAd interstitialAd) {
                                            pDialog.dismiss();
                                            gotoNextScreen();
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
                                    gotoNextScreen();
                                }
                                break;
                        }
                    } else {
                        gotoNextScreen();
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        return rootView;
    }

    @SuppressLint("StaticFieldLeak")
    private class getSubCat extends AsyncTask<String, Void, String> {

        String base64;

        private getSubCat(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));
            } else {
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.LATEST_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemCategory objItem = new ItemCategory();

                        objItem.setCategoryName(objJson.getString(Constant.CATEGORY_NAME));
                        objItem.setCategoryId(objJson.getString(Constant.CATEGORY_CID));
                        objItem.setCategoryImageUrl(objJson.getString(Constant.CATEGORY_IMAGE));

                        if (Constant.isNative) {
                            if (j % Constant.nativePosition == 0) {
                                mListItem.add(null);
                                j++;
                            }
                        }
                        mListItem.add(objItem);
                        j++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                displayData();
            }
        }
    }

    private void displayData() {

        if (getActivity() != null) {
            categoryAdapter = new CategoryAdapter(getActivity(), mListItem);
            recyclerView.setAdapter(categoryAdapter);
        }
    }

    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_category));
    }

    private void Loading() {
        pDialog = new ProgressDialog(requireActivity());
        pDialog.setMessage(getResources().getString(R.string.loading));
        pDialog.setCancelable(false);
        pDialog.show();
    }

    private void gotoNextScreen(){
        CategoryListFragment categoryListFragment = new CategoryListFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(CategoryFragment.this);
        fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
        fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
        fragmentTransaction.commitAllowingStateLoss();
        ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
    }
}
