package com.gmail.card.mousavi.yahoowheather.model;

/**
 * Created by Kourosh on 8/8/2017.
 */

public class weatherModel  {
    String weekday;
    String highTemp;
    String lowTemp;
    int imgResource;

    public weatherModel() {
    }
    public weatherModel(String weekday, String highTemp, String lowTemp, int imgResource) {

        this.weekday = weekday;
        this.highTemp = highTemp;
        this.lowTemp = lowTemp;
        this.imgResource = imgResource;
    }
    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        this.weekday = weekday;
    }

    public String getHighTemp() {
        return highTemp;
    }

    public void setHighTemp(String highTemp) {
        this.highTemp = highTemp;
    }

    public String getLowTemp() {
        return lowTemp;
    }

    public void setLowTemp(String lowTemp) {
        this.lowTemp = lowTemp;
    }

    public int getImgResource() {
        return imgResource;
    }

    public void setImgResource(int imgResource) {
        this.imgResource = imgResource;
    }


}
