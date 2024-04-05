package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MyOrderProductDataSet {

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("quantity")
    @Expose
    private String quantity;

    @SerializedName("price")
    @Expose
    private String price;

    @SerializedName("total")
    @Expose
    private String total;

    @SerializedName("option")
    @Expose
    private ArrayList<MyOrderProductOptionDataSet> optionList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public ArrayList<MyOrderProductOptionDataSet> getOptionList() {
        return optionList;
    }

    public void setOptionList(ArrayList<MyOrderProductOptionDataSet> optionList) {
        this.optionList = optionList;
    }
}
