package com.gmail.card.mousavi.yahoowheather;

import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.card.mousavi.yahoowheather.adapters.WeatherAdapter;
import com.gmail.card.mousavi.yahoowheather.model.DatabaseModel;
import com.gmail.card.mousavi.yahoowheather.model.weatherModel;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.gmail.card.mousavi.yahoowheather.YahooModel;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    EditText edtSearch;
    TextView txtNowTemp;
    TextView txtCity;
    TextView txtTempHighest;
    TextView txtTempLowest;
    TextView txtRegion;
    ImageButton btnSearch;
    ListView lstWeather;
    ImageView imageView;
    LinearLayout lyrHide;
    LinearLayout lyrTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts").setFontAttrId(R.attr.fontPath).build());
        bindView();
        List<DatabaseModel> dbModels = DatabaseModel.listAll(DatabaseModel.class);

        if( dbModels.size()==0){
            lyrHide.setVisibility(View.GONE);
            lyrTxt.setVisibility(View.VISIBLE);
        }
        else{
            lyrHide.setVisibility(View.VISIBLE);
            lyrTxt.setVisibility(View.GONE);
            getOfflineData();
        }


        edtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(btnSearch.getWindowToken(), 0);
                    if (edtSearch.getVisibility() == View.VISIBLE) {
                        FindTemp();
                        edtSearch.setVisibility(View.GONE);
                        txtCity.setVisibility(View.VISIBLE);
                        txtRegion.setVisibility(View.VISIBLE);
                    } else {
                        edtSearch.setVisibility(View.VISIBLE);
                        txtCity.setVisibility(View.GONE);
                        txtRegion.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }
                return false;
            }
        });


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (edtSearch.getVisibility() == View.VISIBLE) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(btnSearch.getWindowToken(), 0);
                    FindTemp();
                    edtSearch.setVisibility(View.GONE);
                    txtCity.setVisibility(View.VISIBLE);
                    txtRegion.setVisibility(View.VISIBLE);
                } else {
                    edtSearch.post(
                            new Runnable() {
                                public void run() {
                                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                    inputMethodManager.toggleSoftInputFromWindow(edtSearch.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
                                    edtSearch.requestFocus();
                                }
                            });

                    edtSearch.setVisibility(View.VISIBLE);
                    txtCity.setVisibility(View.GONE);
                    txtRegion.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void FindTemp() {
        if (!isOnline()) {
            Toast.makeText(MainActivity.this, "Connection Error!", Toast.LENGTH_LONG).show();
            return;
        }

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        String JSONURL = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22" +
                edtSearch.getText().toString() + "%2C%20ir%22)%20and%20u%3D'c'&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(JSONURL, new TextHttpResponseHandler() {
            @Override
            public void onStart() {
                dialog.show();
                dialog.setCancelable(false);
                super.onStart();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                dialog.dismiss();
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

                lyrHide.setVisibility(View.VISIBLE);
                lyrTxt.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Connection Error!", Toast.LENGTH_LONG).show();
                getOfflineData();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                lyrHide.setVisibility(View.VISIBLE);
                lyrTxt.setVisibility(View.GONE);
                DatabaseModel.deleteAll(DatabaseModel.class);

                Gson gson = new Gson();
                YahooModel yahooModel = gson.fromJson(responseString, YahooModel.class);

                if (yahooModel.getQuery().getResults() != null) {
                    weatherModel tomorrow = new weatherModel(
                            "Tomorrow"
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(1).getHigh()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(1).getLow()
                            , getImageWeather(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(1).getCode()));
                    weatherModel d2th = new weatherModel(
                            yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(2).getDay()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(2).getHigh()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(2).getLow()
                            , getImageWeather(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(2).getCode()));
                    weatherModel d3th = new weatherModel(
                            yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(3).getDay()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(3).getHigh()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(3).getLow()
                            , getImageWeather(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(3).getCode()));
                    weatherModel d4th = new weatherModel(
                            yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(4).getDay()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(4).getHigh()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(4).getLow()
                            , getImageWeather(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(4).getCode()));
                    weatherModel d5th = new weatherModel(
                            yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(5).getDay()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(5).getHigh()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(5).getLow()
                            , getImageWeather(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(5).getCode()));
                    weatherModel d6th = new weatherModel(
                            yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(6).getDay()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(6).getHigh()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(6).getLow()
                            , getImageWeather(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(6).getCode()));
                    weatherModel d7th = new weatherModel(
                            yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(7).getDay()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(7).getHigh()
                            , yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(7).getLow()
                            , getImageWeather(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(7).getCode()));

                    List<weatherModel> weathers = new ArrayList<>();
                    weathers.add(tomorrow);
                    weathers.add(d2th);
                    weathers.add(d3th);
                    weathers.add(d4th);
                    weathers.add(d5th);
                    weathers.add(d6th);
                    weathers.add(d7th);

                    WeatherAdapter adapter = new WeatherAdapter(MainActivity.this, weathers);
                    lstWeather.setAdapter(adapter);
                    txtCity.setText(yahooModel.getQuery().getResults().getChannel().getLocation().getCity());
                    txtRegion.setText(yahooModel.getQuery().getResults().getChannel().getLocation().getRegion());
                    txtNowTemp.setText(yahooModel.getQuery().getResults().getChannel().getItem().getCondition().getTemp() + "°");
                    txtTempHighest.setText(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(0).getHigh() + "°");
                    txtTempLowest.setText(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(0).getLow() + "°");
                    imageView.setImageResource(getImageWeather(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(0).getCode()));
                    edtSearch.setText("");

                    DatabaseModel model = new DatabaseModel();
                    model.setCityName(yahooModel.getQuery().getResults().getChannel().getLocation().getCity());
                    model.setRegionName(yahooModel.getQuery().getResults().getChannel().getLocation().getRegion());
                    model.setCode(yahooModel.getQuery().getResults().getChannel().getItem().getCondition().getCode());
                    model.setTodayTemp(yahooModel.getQuery().getResults().getChannel().getItem().getCondition().getTemp() + "°");
                    model.setTodayHTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(0).getHigh() + "°");
                    model.setTodayLTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(0).getLow() + "°");
                    model.setD1HTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(1).getHigh() + "°");
                    model.setD1LTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(1).getHigh() + "°");
                    model.setCode1(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(1).getCode());
                    model.setD2HTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(2).getHigh() + "°");
                    model.setD2LTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(2).getLow() + "°");
                    model.setCode2(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(2).getCode());
                    model.setNameDay2(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(2).getDay());
                    model.setD3HTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(3).getHigh() + "°");
                    model.setD3LTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(3).getLow() + "°");
                    model.setCode3(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(3).getCode());
                    model.setNameDay3(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(3).getDay());
                    model.setD4HTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(4).getHigh() + "°");
                    model.setD4LTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(4).getLow() + "°");
                    model.setCode4(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(4).getCode());
                    model.setNameDay4(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(4).getDay());
                    model.setD5HTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(5).getHigh() + "°");
                    model.setD5LTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(5).getLow() + "°");
                    model.setCode5(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(5).getCode());
                    model.setNameDay5(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(5).getDay());
                    model.setD6HTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(6).getHigh() + "°");
                    model.setD6LTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(6).getLow() + "°");
                    model.setCode6(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(6).getCode());
                    model.setNameDay6(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(6).getDay());
                    model.setD7HTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(7).getHigh() + "°");
                    model.setD7LTemp(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(7).getLow() + "°");
                    model.setCode7(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(7).getCode());
                    model.setNameDay7(yahooModel.getQuery().getResults().getChannel().getItem().getForecast().get(7).getDay());
                    model.save();
                    Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();


                } else {
                    Toast.makeText(MainActivity.this, "Your Country is invalid", Toast.LENGTH_LONG).show();
                    edtSearch.setText("");

                }
            }

        });
    }

    private void bindView() {
        edtSearch = (EditText) findViewById(R.id.edtSearch);
        btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        txtNowTemp = (TextView) findViewById(R.id.txtNowTemp);
        txtCity = (TextView) findViewById(R.id.txtCity);
        txtRegion = (TextView) findViewById(R.id.txtRegion);
        txtTempHighest = (TextView) findViewById(R.id.temp_highest);
        txtTempLowest = (TextView) findViewById(R.id.temp_lowest);
        lstWeather = (ListView) findViewById(R.id.lstWeather);
        imageView = (ImageView) findViewById(R.id.imageView);
        lyrHide= (LinearLayout) findViewById(R.id.lyrHide);
        lyrTxt= (LinearLayout) findViewById(R.id.lyrTxt);

    }


    private void getOfflineData() {
        List<DatabaseModel> models = DatabaseModel.listAll(DatabaseModel.class);
        for (DatabaseModel model : models) {
            txtCity.setText(model.getCityName());
            txtRegion.setText(model.getRegionName());
            txtNowTemp.setText(model.getTodayTemp());
            txtTempHighest.setText(model.getTodayHTemp());
            txtTempLowest.setText(model.getTodayLTemp());
            imageView.setImageResource(getImageWeather(model.getCode()));
            weatherModel tomorrow = new weatherModel(
                    "Tomorrow"
                    , model.getD1HTemp()
                    , model.getD1LTemp()
                    , getImageWeather(model.getCode1()));
            weatherModel d2th = new weatherModel(
                    model.getNameDay2()
                    , model.getD2HTemp()
                    , model.getD2LTemp()
                    , getImageWeather(model.getCode2()));
            weatherModel d3th = new weatherModel(
                    model.getNameDay3()
                    , model.getD3HTemp()
                    , model.getD3LTemp()
                    , getImageWeather(model.getCode3()));
            weatherModel d4th = new weatherModel(
                    model.getNameDay4()
                    , model.getD4HTemp()
                    , model.getD4LTemp()
                    , getImageWeather(model.getCode4()));
            weatherModel d5th = new weatherModel(
                    model.getNameDay5()
                    , model.getD5HTemp()
                    , model.getD5LTemp()
                    , getImageWeather(model.getCode5()));
            weatherModel d6th = new weatherModel(
                    model.getNameDay6()
                    , model.getD6HTemp()
                    , model.getD6LTemp()
                    , getImageWeather(model.getCode6()));
            weatherModel d7th = new weatherModel(
                    model.getNameDay7()
                    , model.getD7HTemp()
                    , model.getD7LTemp()
                    , getImageWeather(model.getCode7()));

            List<weatherModel> weathers = new ArrayList<>();
            weathers.add(tomorrow);
            weathers.add(d2th);
            weathers.add(d3th);
            weathers.add(d4th);
            weathers.add(d5th);
            weathers.add(d6th);
            weathers.add(d7th);

            WeatherAdapter adapter = new WeatherAdapter(MainActivity.this, weathers);
            lstWeather.setAdapter(adapter);
        }
    }

    public int getImageWeather(String weatherCode) {
        byte code = Byte.parseByte(weatherCode);
        if (code <= 2)
            return R.drawable.w_tornado_day_night;
        else if (code <= 4)
            return R.drawable.w_thundershowers_day_night;
        else if (code <= 7)
            return R.drawable.w_snow_rain_mix_day_night;
        else if (code <= 10)
            return R.drawable.w_freezing_rain_day_night;
        else if (code <= 16)
            return R.drawable.w_snow_day_night;
        else if (code <= 17)
            return R.drawable.w_hail_day_night;
        else if (code <= 18)
            return R.drawable.w_sleet_day_night;
        else if (code <= 24)
            return R.drawable.w_fog_day_night;
        else if (code <= 26)
            return R.drawable.w_cloudy_day_night;
        else if (code <= 28)
            return R.drawable.w_mostly_cloudy_day_night;
        else if (code <= 29)
            return R.drawable.w_partly_cloudy_night;
        else if (code <= 30)
            return R.drawable.w_partly_cloudy_day;
        else if (code <= 31)
            return R.drawable.w_clear_night;
        else if (code <= 32)
            return R.drawable.w_clear_day;
        else if (code <= 33)
            return R.drawable.w_fair_night;
        else if (code <= 34)
            return R.drawable.w_fair_day;
        else if (code <= 35)
            return R.drawable.w_rain_hail;
        else if (code <= 37)
            return R.drawable.w_clear_day;
        else if (code <= 40)
            return R.drawable.w_scattered_showers_day_night;
        else if (code <= 43)
            return R.drawable.w_snow_day_night;
        else if (code <= 44)
            return R.drawable.w_cloudy_day_night;
        else if (code <= 45)
            return R.drawable.w_thundershowers_day_night;
        else if (code <= 46)
            return R.drawable.w_snow_day_night;
        else if (code <= 47)
            return R.drawable.w_thundershowers_day_night;
        else
            return R.drawable.ds_na;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
