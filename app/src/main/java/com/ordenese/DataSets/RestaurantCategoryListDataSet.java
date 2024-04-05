package com.ordenese.DataSets;

public class RestaurantCategoryListDataSet {
    private int mImage;
    private String mTitle;

    public RestaurantCategoryListDataSet(int mImage, String mTitle)
    {
        this.mImage=mImage;
        this.mTitle=mTitle;
    }

    public RestaurantCategoryListDataSet()
    {}

    public int getmImage() {
        return mImage;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmImage(int mImage) {
        this.mImage = mImage;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }
}
