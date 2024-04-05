package com.ordenese.DataSets;

public class HotelListDataSet {
    private int mImage;
    private String mTitle;
    private String mTime;
    private String mAmount1;
    private String mAmount2,restaurant_id,image,rating;

    public HotelListDataSet()
    {}



    // create constructor to set the values for all the parameters of the each single view
    public HotelListDataSet(int mImage, String mTitle, String mTime, String mAmount1, String mAmount2) {
        this.mImage=mImage;
        this.mTitle=mTitle;
        this.mTime=mTime;
        this.mAmount1=mAmount1;
        this.mAmount2=mAmount2;
    }

    public String getRestaurant_id() {
        return restaurant_id;
    }

    public void setRestaurant_id(String restaurant_id) {
        this.restaurant_id = restaurant_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getmImage() {
        return mImage;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmTime() {
        return mTime;
    }

    public String getmAmount1() {
        return mAmount1;
    }

    public String getmAmount2() {
        return mAmount2;
    }

    public void setmImage(int mImage) {
        this.mImage = mImage;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public void setmAmount1(String mAmount1) {
        this.mAmount1 = mAmount1;
    }

    public void setmAmount2(String mAmount2) {
        this.mAmount2 = mAmount2;
    }
}
