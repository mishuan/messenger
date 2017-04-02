package com.ece416.aruproy.messengerclient;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ilikecalculus on 2017-04-01.
 */

public class GroupArrayAdapter extends ArrayAdapter<String> {

    public GroupArrayAdapter(Context context, int layoutResourceId, List<String> data) {
        super(context, layoutResourceId, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View returnedView = super.getView(position, convertView, parent);

        TextView text = (TextView) returnedView.findViewById(R.id.text);
        text.setTypeface(null, Typeface.BOLD);

        return returnedView;
    }
}
