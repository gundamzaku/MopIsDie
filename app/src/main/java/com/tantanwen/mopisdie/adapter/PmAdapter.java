package com.tantanwen.mopisdie.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tantanwen.mopisdie.R;
import com.tantanwen.mopisdie.ViewTopic;
import com.tantanwen.mopisdie.utils.Config;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by gundamzaku on 2015/7/7.
 */
public class PmAdapter extends BaseAdapter {

    private final LayoutInflater myInflater;
    private TranslateAnimation mShowAction;
    private TranslateAnimation mHiddenAction;
    public ArrayList<PmContainer> items = new ArrayList<>();
    private Context mContext;

    public PmAdapter(Context c){

        myInflater = LayoutInflater.from(c);
        mContext = c;

    }

    public void setItems(ArrayList<PmContainer> fromItems){
        items = fromItems;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        convertView = myInflater.inflate(R.layout.array_pm, null);
        TextView nick = (TextView)convertView.findViewById(R.id.pm_nick);
        nick.setText(Html.fromHtml(items.get(position).getSendUserNick()));

        TextView time = (TextView)convertView.findViewById(R.id.pm_time);
        time.setText(items.get(position).getSendTime());

        TextView content = (TextView)convertView.findViewById(R.id.pm_content);
        content.setText(Html.fromHtml(items.get(position).getContent()));

        final LinearLayout mContent = (LinearLayout)convertView.findViewById(R.id.lay_content);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visible = mContent.getVisibility();
                System.out.println(visible);
                if (visible == 0) {
                    mContent.setVisibility(View.GONE);
                }else {
                    mContent.setVisibility(View.VISIBLE);
                }
            }
        });
        return convertView;
    }
}
