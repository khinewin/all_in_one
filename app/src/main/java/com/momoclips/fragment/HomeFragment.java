package com.momoclips.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.bosphere.fadingedgelayout.FadingEdgeLayout;
import com.momoclips.adapter.HomeAllAdapter;
import com.momoclips.adapter.HomeCatAdapter;
import com.momoclips.adapter.HomeLatestAdapter;
import com.momoclips.android.ActivityRecent;
import com.momoclips.android.MainActivity;
import com.momoclips.android.R;
import com.momoclips.favorite.DatabaseHelperRecent;
import com.momoclips.item.ItemCategory;
import com.momoclips.item.ItemLatest;
import com.momoclips.util.API;
import com.momoclips.util.BannerAds;
import com.momoclips.util.Constant;
import com.momoclips.util.EnchantedViewPager;
import com.momoclips.util.ItemOffsetDecoration;
import com.momoclips.util.JsonUtils;
import com.momoclips.util.PopUpAds;
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
import com.squareup.picasso.Picasso;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;


public class HomeFragment extends Fragment {

    RecyclerView recyclerViewLatestVideo, recyclerViewAllVideo, recyclerViewCatVideo, rv_cat_video_rec;
    EnchantedViewPager mViewPager;
    CustomViewPagerAdapter mAdapter;
    NestedScrollView mScrollView;
    ProgressBar mProgressBar;
    ArrayList<ItemLatest> mSliderList;
    CircleIndicator circleIndicator;
    Button btnAll, btnLatest, btnCategory, btn_cat_video_rec;
    int currentCount = 0;
    ArrayList<ItemLatest> mLatestList, mAllList, mRecent;
    ArrayList<ItemCategory> mCatList;
    HomeCatAdapter homeCatAdapter;
    HomeLatestAdapter homeLatestAdapter;
    HomeAllAdapter homeAllAdapter;
    private FragmentManager fragmentManager;
    ItemCategory itemCategory;
    TextView txt_latest_video_no, txt_all_video_no, txt_cat_video_no, txt_cat_video_no_rec;
    private ProgressDialog pDialog;
    LinearLayout lay_main;
    FadingEdgeLayout fadingEdgeLayout1, fadingEdgeLayout2, fadingEdgeLayout3, fad_shadow1_rec;
    LinearLayout ad_view;
    RelativeLayout lay_cat_rec;
    HomeLatestAdapter allVideoAdapterRecent;
    DatabaseHelperRecent databaseHelperRecent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        mSliderList = new ArrayList<>();
        mAllList = new ArrayList<>();
        mLatestList = new ArrayList<>();
        mCatList = new ArrayList<>();
        mRecent = new ArrayList<>();
        databaseHelperRecent = new DatabaseHelperRecent(requireActivity());

        fadingEdgeLayout1 = rootView.findViewById(R.id.fad_shadow1);
        fadingEdgeLayout2 = rootView.findViewById(R.id.fad_shadow2);
        fadingEdgeLayout3 = rootView.findViewById(R.id.fad_shadow3);
        fad_shadow1_rec = rootView.findViewById(R.id.fad_shadow1_rec);

        JsonUtils.changeShadowInRtl(requireActivity(), fadingEdgeLayout1);
        JsonUtils.changeShadowInRtl(requireActivity(), fadingEdgeLayout2);
        JsonUtils.changeShadowInRtl(requireActivity(), fadingEdgeLayout3);
        JsonUtils.changeShadowInRtl(requireActivity(), fad_shadow1_rec);

        ad_view = rootView.findViewById(R.id.ad_view);

        fragmentManager = requireActivity().getSupportFragmentManager();
        mProgressBar = rootView.findViewById(R.id.progressBar);
        mScrollView = rootView.findViewById(R.id.scrollView);
        mViewPager = rootView.findViewById(R.id.viewPager);
        circleIndicator = rootView.findViewById(R.id.indicator_unselected_background);
        lay_main = rootView.findViewById(R.id.lay_main);
        recyclerViewLatestVideo = rootView.findViewById(R.id.rv_latest_video);
        recyclerViewAllVideo = rootView.findViewById(R.id.rv_all_video);
        recyclerViewCatVideo = rootView.findViewById(R.id.rv_cat_video);
        txt_cat_video_no_rec = rootView.findViewById(R.id.txt_cat_video_no_rec);
        rv_cat_video_rec = rootView.findViewById(R.id.rv_cat_video_rec);
        lay_cat_rec = rootView.findViewById(R.id.lay_cat_rec);
        mScrollView.setNestedScrollingEnabled(false);

        btnLatest = rootView.findViewById(R.id.btn_latest_video);
        btnAll = rootView.findViewById(R.id.btn_all_video);
        btnCategory = rootView.findViewById(R.id.btn_cat_video);
        btn_cat_video_rec = rootView.findViewById(R.id.btn_cat_video_rec);

        txt_latest_video_no = rootView.findViewById(R.id.txt_latest_video_no);
        txt_all_video_no = rootView.findViewById(R.id.txt_all_video_no);
        txt_cat_video_no = rootView.findViewById(R.id.txt_cat_video_no);

        recyclerViewLatestVideo.setHasFixedSize(false);
        recyclerViewLatestVideo.setNestedScrollingEnabled(false);
        recyclerViewLatestVideo.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(requireActivity(), R.dimen.item_offset);
        recyclerViewLatestVideo.addItemDecoration(itemDecoration);

        recyclerViewAllVideo.setHasFixedSize(false);
        recyclerViewAllVideo.setNestedScrollingEnabled(false);
        recyclerViewAllVideo.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewAllVideo.addItemDecoration(itemDecoration);

        recyclerViewCatVideo.setHasFixedSize(false);
        recyclerViewCatVideo.setNestedScrollingEnabled(false);
        recyclerViewCatVideo.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCatVideo.addItemDecoration(itemDecoration);

        rv_cat_video_rec.setHasFixedSize(false);
        rv_cat_video_rec.setNestedScrollingEnabled(false);
        rv_cat_video_rec.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        rv_cat_video_rec.addItemDecoration(itemDecoration);
        mScrollView.setNestedScrollingEnabled(false);

        btnLatest.setOnClickListener(view -> {

            ((MainActivity) requireActivity()).highLightNavigation(1, getString(R.string.menu_latest));
            LatestVideoFragment latestVideoFragment = new LatestVideoFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(HomeFragment.this);
            fragmentTransaction.add(R.id.Container, latestVideoFragment, getString(R.string.menu_latest));
            fragmentTransaction.addToBackStack(getString(R.string.menu_latest));
            fragmentTransaction.commit();
            ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_latest));

        });

        btnAll.setOnClickListener(view -> {

            ((MainActivity) requireActivity()).highLightNavigation(2, getString(R.string.menu_video));
            AllVideoFragment allVideoFragment = new AllVideoFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(HomeFragment.this);
            fragmentTransaction.add(R.id.Container, allVideoFragment, getString(R.string.menu_video));
            fragmentTransaction.addToBackStack(getString(R.string.menu_video));
            fragmentTransaction.commit();
            ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_video));

        });


        btnCategory.setOnClickListener(view -> {

            ((MainActivity) requireActivity()).highLightNavigation(3, getString(R.string.menu_category));
            CategoryFragment categoryFragment = new CategoryFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide(HomeFragment.this);
            fragmentTransaction.add(R.id.Container, categoryFragment, getString(R.string.menu_category));
            fragmentTransaction.addToBackStack(getString(R.string.menu_category));
            fragmentTransaction.commit();
            ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_category));

        });

        btn_cat_video_rec.setOnClickListener(view -> {
            Intent intent_rec = new Intent(requireActivity(), ActivityRecent.class);
            startActivity(intent_rec);
        });

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
        jsObj.addProperty("method_name", "get_home_video");
        if (JsonUtils.isNetworkAvailable(requireActivity())) {
            new HomeVideo(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
        }

        mViewPager.useScale();
        mViewPager.removeAlpha();
        return rootView;
    }

    private class CustomViewPagerAdapter extends PagerAdapter {
        LayoutInflater inflater;

        private CustomViewPagerAdapter() {
            // TODO Auto-generated constructor stub
            inflater = requireActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mSliderList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            View imageLayout = inflater.inflate(R.layout.row_slider_item, container, false);
            assert imageLayout != null;

            ImageView image = imageLayout.findViewById(R.id.image);
            TextView text = imageLayout.findViewById(R.id.text);
            LinearLayout lyt_parent = imageLayout.findViewById(R.id.rootLayout);

            text.setText(mSliderList.get(position).getLatestVideoName());

            switch (mSliderList.get(position).getLatestVideoType()) {
                case "local":
                case "server_url":
                case "vimeo":
                case "embeded_code":
                    Picasso.get().load(mSliderList.get(position).getLatestVideoImgBig()).into(image);
                    break;
                case "youtube":
                    Picasso.get().load(Constant.YOUTUBE_IMAGE_FRONT + mSliderList.get(position).getLatestVideoPlayId() + Constant.YOUTUBE_SMALL_IMAGE_HD).into(image);
                    break;
                case "dailymotion":
                    Picasso.get().load(Constant.DAILYMOTION_IMAGE_PATH + mSliderList.get(position).getLatestVideoPlayId()).into(image);
                    break;
            }

            imageLayout.setTag(EnchantedViewPager.ENCHANTED_VIEWPAGER_POSITION + position);
            lyt_parent.setOnClickListener(v -> {
                Constant.LATEST_IDD = mSliderList.get(position).getLatestId();
                PopUpAds.ShowInterstitialAds(requireActivity());
            });

            container.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((View) object);
        }
    }

    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private void autoPlay(final ViewPager viewPager) {

        viewPager.postDelayed(() -> {
            try {
                if (mAdapter != null && viewPager.getAdapter().getCount() > 0) {
                    int position = currentCount % mAdapter.getCount();
                    currentCount++;
                    viewPager.setCurrentItem(position);
                    autoPlay(viewPager);
                }
            } catch (Exception e) {
                Log.e("TAG", "auto scroll pager error.", e);
            }
        }, 2500);
    }

    @SuppressLint("StaticFieldLeak")
    private class HomeVideo extends AsyncTask<String, Void, String> {

        String base64;

        private HomeVideo(String base64) {
            this.base64 = base64;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (isAdded()) {
                getResources().getString(R.string.app_name);
            }
            mProgressBar.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            if (null == result || result.length() == 0) {
                showToast(getString(R.string.no_data));
                mScrollView.setVisibility(View.GONE);
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONObject mainJsonob = mainJson.getJSONObject(Constant.LATEST_ARRAY_NAME);
                    JSONArray jsonArray = mainJsonob.getJSONArray("featured_video");
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);

                        ItemLatest objItem = new ItemLatest();

                        objItem.setLatestId(objJson.getString(Constant.LATEST_ID));
                        objItem.setLatestCategoryName(objJson.getString(Constant.LATEST_CAT_NAME));
                        objItem.setLatestCategoryId(objJson.getString(Constant.LATEST_CATID));
                        objItem.setLatestVideoUrl(objJson.getString(Constant.LATEST_VIDEO_URL));
                        objItem.setLatestVideoPlayId(objJson.getString(Constant.LATEST_VIDEO_ID));
                        objItem.setLatestVideoName(objJson.getString(Constant.LATEST_VIDEO_NAME));
                        objItem.setLatestDuration(objJson.getString(Constant.LATEST_VIDEO_DURATION));
                        objItem.setLatestDescription(objJson.getString(Constant.LATEST_VIDEO_DESCRIPTION));
                        objItem.setLatestVideoImgBig(objJson.getString(Constant.LATEST_IMAGE_URL));
                        objItem.setLatestVideoType(objJson.getString(Constant.LATEST_TYPE));
                        objItem.setLatestVideoRate(objJson.getString(Constant.LATEST_RATE));
                        objItem.setLatestVideoView(objJson.getString(Constant.LATEST_VIEW));

                        mSliderList.add(objItem);
                    }

                    JSONArray jsonArrayla = mainJsonob.getJSONArray("latest_video");
                    JSONObject objJsonla;
                    for (int i = 0; i < jsonArrayla.length(); i++) {
                        objJsonla = jsonArrayla.getJSONObject(i);

                        ItemLatest objItem = new ItemLatest();

                        objItem.setLatestId(objJsonla.getString(Constant.LATEST_ID));
                        objItem.setLatestCategoryName(objJsonla.getString(Constant.LATEST_CAT_NAME));
                        objItem.setLatestCategoryId(objJsonla.getString(Constant.LATEST_CATID));
                        objItem.setLatestVideoUrl(objJsonla.getString(Constant.LATEST_VIDEO_URL));
                        objItem.setLatestVideoPlayId(objJsonla.getString(Constant.LATEST_VIDEO_ID));
                        objItem.setLatestVideoName(objJsonla.getString(Constant.LATEST_VIDEO_NAME));
                        objItem.setLatestDuration(objJsonla.getString(Constant.LATEST_VIDEO_DURATION));
                        objItem.setLatestDescription(objJsonla.getString(Constant.LATEST_VIDEO_DESCRIPTION));
                        objItem.setLatestVideoImgBig(objJsonla.getString(Constant.LATEST_IMAGE_URL));
                        objItem.setLatestVideoType(objJsonla.getString(Constant.LATEST_TYPE));
                        objItem.setLatestVideoRate(objJsonla.getString(Constant.LATEST_RATE));
                        objItem.setLatestVideoView(objJsonla.getString(Constant.LATEST_VIEW));

                        mLatestList.add(objItem);
                    }

                    JSONArray jsonArraymost = mainJsonob.getJSONArray("all_video");
                    JSONObject objJsonmost;
                    for (int i = 0; i < jsonArraymost.length(); i++) {
                        objJsonmost = jsonArraymost.getJSONObject(i);

                        ItemLatest objItem = new ItemLatest();

                        objItem.setLatestId(objJsonmost.getString(Constant.LATEST_ID));
                        objItem.setLatestCategoryName(objJsonmost.getString(Constant.LATEST_CAT_NAME));
                        objItem.setLatestCategoryId(objJsonmost.getString(Constant.LATEST_CATID));
                        objItem.setLatestVideoUrl(objJsonmost.getString(Constant.LATEST_VIDEO_URL));
                        objItem.setLatestVideoPlayId(objJsonmost.getString(Constant.LATEST_VIDEO_ID));
                        objItem.setLatestVideoName(objJsonmost.getString(Constant.LATEST_VIDEO_NAME));
                        objItem.setLatestDuration(objJsonmost.getString(Constant.LATEST_VIDEO_DURATION));
                        objItem.setLatestDescription(objJsonmost.getString(Constant.LATEST_VIDEO_DESCRIPTION));
                        objItem.setLatestVideoImgBig(objJsonmost.getString(Constant.LATEST_IMAGE_URL));
                        objItem.setLatestVideoType(objJsonmost.getString(Constant.LATEST_TYPE));
                        objItem.setLatestVideoRate(objJsonmost.getString(Constant.LATEST_RATE));
                        objItem.setLatestVideoView(objJsonmost.getString(Constant.LATEST_VIEW));

                        mAllList.add(objItem);
                    }
                    JSONArray jsonArray2 = mainJsonob.getJSONArray("category");
                    JSONObject objJson2 = null;
                    for (int i = 0; i < jsonArray2.length(); i++) {
                        objJson2 = jsonArray2.getJSONObject(i);

                        ItemCategory objItem = new ItemCategory();

                        objItem.setCategoryId(objJson2.getString(Constant.CATEGORY_CID));
                        objItem.setCategoryImageUrl(objJson2.getString(Constant.CATEGORY_IMAGE));
                        objItem.setCategoryName(objJson2.getString(Constant.CATEGORY_NAME));

                        mCatList.add(objItem);

                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                setHomeVideo();
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setHomeVideo() {

        if (getActivity() != null) {
            BannerAds.showBannerAds(requireActivity(), ad_view);
        }

        if (getActivity() != null) {
            if (!mSliderList.isEmpty()) {
                mAdapter = new CustomViewPagerAdapter();
                mViewPager.setAdapter(mAdapter);
                circleIndicator.setViewPager(mViewPager);
                autoPlay(mViewPager);
            }
            if (mSliderList.isEmpty()) {
                mScrollView.setVisibility(View.GONE);
            } else {
                mScrollView.setVisibility(View.VISIBLE);

            }
        }
        if (getActivity() != null) {
            txt_latest_video_no.setText(String.valueOf(mLatestList.size()) + "\u0020" + getResources().getString(R.string.total_video));
            txt_all_video_no.setText(String.valueOf(mAllList.size()) + "\u0020" + getResources().getString(R.string.total_video));
            txt_cat_video_no.setText(String.valueOf(mCatList.size()) + "\u0020" + getResources().getString(R.string.total_category));
        }

        if (getActivity() != null) {
            homeLatestAdapter = new HomeLatestAdapter(getActivity(), mLatestList);
            recyclerViewLatestVideo.setAdapter(homeLatestAdapter);
        }
        if (getActivity() != null) {
            homeAllAdapter = new HomeAllAdapter(getActivity(), mAllList);
            recyclerViewAllVideo.setAdapter(homeAllAdapter);
        }
        if (getActivity() != null) {
            homeCatAdapter = new HomeCatAdapter(getActivity(), mCatList);
            recyclerViewCatVideo.setAdapter(homeCatAdapter);
        }

        recyclerViewCatVideo.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerViewCatVideo, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                itemCategory = mCatList.get(position);
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

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

    private void gotoNextScreen() {
        CategoryListFragment categoryListFragment = new CategoryListFragment();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.hide(HomeFragment.this);
        fragmentTransaction.add(R.id.Container, categoryListFragment, Constant.CATEGORY_TITLEE);
        fragmentTransaction.addToBackStack(Constant.CATEGORY_TITLEE);
        fragmentTransaction.commit();
        ((MainActivity) requireActivity()).setToolbarTitle(Constant.CATEGORY_TITLEE);
    }

    private void Loading() {
        pDialog = new ProgressDialog(requireActivity());
        pDialog.setMessage(getResources().getString(R.string.loading));
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRecent = databaseHelperRecent.getFavourite();
        displayDataRecent();
        ((MainActivity) requireActivity()).setToolbarTitle(getString(R.string.menu_home));
    }

    @SuppressLint("SetTextI18n")
    private void displayDataRecent() {

        if (mRecent.size() >= 2) {
            lay_cat_rec.setVisibility(View.VISIBLE);
            fad_shadow1_rec.setVisibility(View.VISIBLE);

        } else {
            lay_cat_rec.setVisibility(View.GONE);
            fad_shadow1_rec.setVisibility(View.GONE);
        }

        txt_cat_video_no_rec.setText(String.valueOf(mRecent.size()) + "\u0020" + getResources().getString(R.string.total_video));

        allVideoAdapterRecent = new HomeLatestAdapter(getActivity(), mRecent);
        rv_cat_video_rec.setAdapter(allVideoAdapterRecent);
    }
}