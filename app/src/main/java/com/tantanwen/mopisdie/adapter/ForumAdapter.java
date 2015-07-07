package com.tantanwen.mopisdie.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tantanwen.mopisdie.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by gundamzaku on 2015/7/7.
 */
public class ForumAdapter extends BaseAdapter {

    private final LayoutInflater myInflater;
    public ArrayList<String> items = new ArrayList<String>();

    public ForumAdapter(Context c){
        myInflater = LayoutInflater.from(c);
    }

    public void setItems(ArrayList fromItems){
        items = fromItems;
    }

    @Override
    public int getCount() {
        //System.out.println(items.size());
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
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = myInflater.inflate(R.layout.array_forum,null);
        TextView title = (TextView)convertView.findViewById(R.id.forum_item);
        title.setText(Html.fromHtml(items.get(position)));

        System.out.println(position);
        return convertView;
    }
}
