package com.ece416.aruproy.messengerclient;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ilikecalculus on 2017-04-01.
 */

public class GroupArrayAdapter extends ArrayAdapter<String> {

    public GroupArrayAdapter(Context context, List<String> data) {
        super(context, R.layout.custom_group_view, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.custom_group_view, parent, false);

        String group = getItem(position);
        TextView tvGroupName = (TextView) customView.findViewById(R.id.group_name_text);
        if (ConnectTask.hasNewMessagesForGroup(group)) {
            tvGroupName.setTypeface(null, Typeface.BOLD);
            tvGroupName.setTextColor(Color.parseColor("#FF4081"));
        }
        tvGroupName.setText(group);

        return customView;
    }
}
