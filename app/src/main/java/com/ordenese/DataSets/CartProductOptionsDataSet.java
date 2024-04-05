package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CartProductOptionsDataSet {

    @SerializedName("option_id")
    @Expose
    public String option_id;

    @SerializedName("option_value_id")
    @Expose
    public String option_value_id;

    @SerializedName("price")
    @Expose
    public String price;

    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("value")
    @Expose
    public String value;

    @SerializedName("type")
    @Expose
    public String type;

    @SerializedName("quantity")
    @Expose
    public String quantity;

    public String getOption_id() {
        return option_id;
    }

    public void setOption_id(String option_id) {
        this.option_id = option_id;
    }

    public String getOption_value_id() {
        return option_value_id;
    }

    public void setOption_value_id(String option_value_id) {
        this.option_value_id = option_value_id;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
