package com.sortedunderbelly.motomileage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;

/**
 * Created by max.ross on 5/12/14.
 */
public class MyArrayAdapter extends ArrayAdapter<Trip> {

    private final DateFormat dateFormat;

    public MyArrayAdapter(Context context, int resource, List<Trip> objects, DateFormat dateFormat) {
        super(context, resource, objects);
        this.dateFormat = dateFormat;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater vi = LayoutInflater.from(getContext());
            view = vi.inflate(R.layout.list_item, null);
        }

        Trip trip = getItem(position);

        if (trip != null) {
            TextView tripDate = (TextView) view.findViewById(R.id.tripDateListItem);
            TextView tripDistance = (TextView) view.findViewById(R.id.tripDistanceListItem);
            TextView tripDesc = (TextView) view.findViewById(R.id.tripDescListItem);

            tripDate.setText(dateFormat.format(trip.getDate()) + ":");
            tripDistance.setText(String.format("%d miles", trip.getDistance()));
            tripDesc.setText(trip.getDesc());
        }
        if (position % 2 == 1) {
            view.setBackgroundColor(getContext().getResources().getColor(R.color.mileage_list_item_background));
//            view.setBackgroundColor(Color.argb(255, 206, 208, 143));
        } else {
            view.setBackgroundColor(getContext().getResources().getColor(R.color.mileage_list_item_background_alt));
//            view.setBackgroundColor(Color.argb(255, 202, 208, 87));
        }
        return view;
    }

}
