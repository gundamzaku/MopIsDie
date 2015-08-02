package com.tantanwen.mopisdie.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tantanwen.mopisdie.Forum;
import com.tantanwen.mopisdie.R;
import com.tantanwen.mopisdie.ViewTopic;

import java.util.ArrayList;

/**
 * Created by gundamzaku on 2015/7/7.
 */
public class ForumAdapter extends BaseAdapter {

    private final LayoutInflater myInflater;
    public ArrayList<String[]> items = new ArrayList<String[]>();
    private Context mContext;

    public ForumAdapter(Context c){

        myInflater = LayoutInflater.from(c);
        mContext = c;
    }

    public void setItems(ArrayList<String[]> fromItems){
        items = fromItems;
        //不能用克隆
        //items = (ArrayList<String[]>) fromItems.clone();
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

        convertView = myInflater.inflate(R.layout.array_forum,null);
        final TextView title = (TextView)convertView.findViewById(R.id.forum_item);
        title.setText(Html.fromHtml(items.get(position)[1]));

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(items.get(position)[0] == null){
                    return;
                }
                title.setBackgroundResource(R.color.material_yellow_50);
                //Log.d(Config.TAG,items.get(position)[0]);
                //跳新的页面
                Intent list = new Intent(mContext,ViewTopic.class);
                list.putExtra("tid",items.get(position)[0]);
                mContext.startActivity(list);
                /*
                提示信息
                MP达到9999才能查看该帖。
                */
            }
        });
        //System.out.println(position);
        return convertView;
    }
}
