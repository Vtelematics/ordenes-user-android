package com.ordenese.DataSets;

import java.util.ArrayList;

/**
 * Created by user on 11/28/2018.
 * Home List
 */

public class ModelHomeList {
    private String type,detail;
    private ArrayList<ProductDataSet> productList;
    private ArrayList<ListDataSet> restaurantList;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<ProductDataSet> getProductList() {
        return productList;
    }

    public void setProductList(ArrayList<ProductDataSet> productList) {
        this.productList = productList;
    }


    public ArrayList<ListDataSet> getRestaurantList() {
        return restaurantList;
    }

    public void setRestaurantList(ArrayList<ListDataSet> restaurantList) {
        this.restaurantList = restaurantList;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }
}
