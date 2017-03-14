package com.evistek.gallery.activity.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.evistek.gallery.R;
import com.evistek.gallery.activity.LocalResourceActivity;

/**
 * Created by evis on 2016/8/16.
 */
public class ErrorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    public ErrorAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ErrorViewHolder(mLayoutInflater.inflate(R.layout.network_unavailable, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    public class ErrorViewHolder extends RecyclerView.ViewHolder {

        public ErrorViewHolder(View view) {
            super(view);

            ImageView imageView = (ImageView) view.findViewById(R.id.go_to_local_resources);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, LocalResourceActivity.class);
                    mContext.startActivity(intent);
                }
            });
        }
    }
}
