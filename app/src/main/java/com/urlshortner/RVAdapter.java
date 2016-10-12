package com.urlshortner;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

    private Context context;
    private ArrayList<String> urlHistory;
    private ArrayList<String> longurlHistory;

    RVAdapter(Context context, ArrayList<String> urlHistory, ArrayList<String> longurlHistory){
        this.context = context;
        this.urlHistory = urlHistory;
        this.longurlHistory = longurlHistory;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.single_card, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, final int position) {
        holder.url.setText(urlHistory.get(position));
        holder.longurl.setText(longurlHistory.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlHistory.get(position)));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return urlHistory.size();
    }

    class Holder extends RecyclerView.ViewHolder{

        TextView url,longurl;
        public Holder(View itemView) {
            super(itemView);
            url = (TextView) itemView.findViewById(R.id.url);
            longurl = (TextView) itemView.findViewById(R.id.longurl);
        }
    }
}
