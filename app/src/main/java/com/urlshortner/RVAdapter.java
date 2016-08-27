package com.urlshortner;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Rohan on 8/27/2016.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.Holder> {

    Context context;
    ArrayList<String> urlHistory;

    RVAdapter(Context context, ArrayList<String> urlHistory){
        this.context = context;
        this.urlHistory = urlHistory;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.single_card, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.url.setText(urlHistory.get(position));
    }

    @Override
    public int getItemCount() {
        return urlHistory.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        TextView url;
        public Holder(View itemView) {
            super(itemView);
            url = (TextView) itemView.findViewById(R.id.url);
        }
    }
}
