package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GroceryProductInfo {

    @SerializedName("product")
    @Expose
    public GroceryProductDataSet product;

    @SerializedName("success")
    @Expose
    private Success success;


    public GroceryProductDataSet getProduct() {
        return product;
    }

    public void setProduct(GroceryProductDataSet product) {
        this.product = product;
    }

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

}
