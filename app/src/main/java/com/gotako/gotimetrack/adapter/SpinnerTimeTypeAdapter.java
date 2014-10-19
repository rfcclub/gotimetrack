package com.gotako.gotimetrack.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gotako.gotimetrack.R;

import java.util.List;

public class SpinnerTimeTypeAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> itemList;

    public SpinnerTimeTypeAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
        itemList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.spinner_time_type, null);

        String type = itemList.get(position);

        TextView textViewType = (TextView) convertView.findViewById(R.id.textViewTimeType);
        ImageView imageViewIcon = (ImageView) convertView.findViewById(R.id.timeTypeIcon);

        textViewType.setText(type);
        if (type.equals(context.getResources().getString(R.string.in_string))) {
            imageViewIcon.setImageResource(R.drawable.in);
        } else {
            imageViewIcon.setImageResource(R.drawable.out);
        }

        return convertView;
    }
}
