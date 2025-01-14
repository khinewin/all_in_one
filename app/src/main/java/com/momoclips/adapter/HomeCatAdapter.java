package com.momoclips.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.momoclips.item.ItemCategory;
import com.squareup.picasso.Picasso;
import com.momoclips.android.R;

import java.util.ArrayList;


public class HomeCatAdapter extends RecyclerView.Adapter<HomeCatAdapter.ItemRowHolder> {

    ArrayList<ItemCategory> dataList;
    Context mContext;

    public HomeCatAdapter(Context context, ArrayList<ItemCategory> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @Override
    public ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_home_cat_item, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(final ItemRowHolder holder, final int position) {
        final ItemCategory singleItem = dataList.get(position);

        holder.text.setText(singleItem.getCategoryName());
        Picasso.get().load(singleItem.getCategoryImageUrl()).into(holder.image);

    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public static class ItemRowHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text;
        LinearLayout lyt_parent;

        private ItemRowHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            text = itemView.findViewById(R.id.text);
            lyt_parent = itemView.findViewById(R.id.rootLayout);

        }
    }
}
