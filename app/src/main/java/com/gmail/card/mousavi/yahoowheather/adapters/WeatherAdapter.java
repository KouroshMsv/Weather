package com.gmail.card.mousavi.yahoowheather.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gmail.card.mousavi.yahoowheather.R;
import com.gmail.card.mousavi.yahoowheather.model.weatherModel;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Kourosh on 8/8/2017.
 */

public class WeatherAdapter extends BaseAdapter {
    Context mContext;

    List<weatherModel> weatherModels;
    TextView weekday;
    TextView hightemp;
    TextView lowtemp;
    ImageView img;

    public WeatherAdapter(Context mContext, List<weatherModel> weatherModels) {
        this.mContext = mContext;
        this.weatherModels = weatherModels;
    }

    @Override
    public int getCount() {
        return weatherModels.size();
    }

    @Override
    public Object getItem(int position) {
        return weatherModels.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = LayoutInflater.from(mContext).inflate(R.layout.weather_item, parent, false);
        weekday = (TextView) rowView.findViewById(R.id.txtAdptWeekdays);
        hightemp = (TextView) rowView.findViewById(R.id.txtAdptHighTemp);
        lowtemp = (TextView) rowView.findViewById(R.id.txtAdptLowTemp);
        img = (ImageView) rowView.findViewById(R.id.imgAdpt);

        weekday.setText(weatherModels.get(position).getWeekday());
        hightemp.setText(weatherModels.get(position).getHighTemp());
        lowtemp.setText(weatherModels.get(position).getLowTemp());
        img.setImageResource(weatherModels.get(position).getImgResource());


        return rowView;
    }
}
