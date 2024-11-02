package com.momoclips.android;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.momoclips.adapter.AllVideoAdapter;
import com.momoclips.favorite.DatabaseHelperRecent;
import com.momoclips.item.ItemLatest;
import com.momoclips.util.BannerAds;
import com.momoclips.util.ItemOffsetDecoration;
import com.momoclips.util.JsonUtils;

import java.util.ArrayList;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class ActivityRecent extends AppCompatActivity {

    ArrayList<ItemLatest> mListItem;
    public RecyclerView recyclerView;
    AllVideoAdapter allVideoAdapter;
    Toolbar toolbar;
    JsonUtils jsonUtils;
    LinearLayout adLayout;
    DatabaseHelperRecent databaseHelperRecent;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.recent_video));
        setSupportActionBar(toolbar);
        toolbar.setTitleTextAppearance(this, R.style.RobotoTextViewStyle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        jsonUtils = new JsonUtils(this);
        jsonUtils.forceRTLIfSupported(getWindow());
        databaseHelperRecent = new DatabaseHelperRecent(ActivityRecent.this);

        mListItem = new ArrayList<>();
        recyclerView = findViewById(R.id.rv_video);
      //  recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(ActivityRecent.this, 2));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(ActivityRecent.this, R.dimen.item_offset);
        recyclerView.addItemDecoration(itemDecoration);
        adLayout = findViewById(R.id.ad_view);

        BannerAds.showBannerAds(ActivityRecent.this,adLayout);
    }

    @Override
    public void onResume() {
        super.onResume();
        mListItem = databaseHelperRecent.getFavourite();
        displayDataRecent();
    }

    private void displayDataRecent() {

        allVideoAdapter = new AllVideoAdapter(ActivityRecent.this, mListItem);
        recyclerView.setAdapter(allVideoAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

}
