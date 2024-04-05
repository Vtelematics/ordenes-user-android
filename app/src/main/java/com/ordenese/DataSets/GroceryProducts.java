package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GroceryProducts {

    @SerializedName("product")
    @Expose
    private ArrayList<GroceryProductsList> product = new ArrayList<>();
    @SerializedName("total")
    @Expose
    private String total;
    @SerializedName("success")
    @Expose
    private Success success;

    public ArrayList<GroceryProductsList> getProduct() {
        return product;
    }

    public void setProduct(ArrayList<GroceryProductsList> product) {
        this.product = product;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public Success getSuccess() {
        return success;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

}
