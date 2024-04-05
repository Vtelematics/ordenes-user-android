package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class ProductOptionsData implements Serializable {

    @SerializedName("product_option_id")
    @Expose
    private String productOptionId;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("minimum_limit")
    @Expose
    private String minimumLimit;

    @SerializedName("maximum_limit")
    @Expose
    private String maximumLimit;

    @SerializedName("required")
    @Expose
    private String required;

    @SerializedName("product_value")
    @Expose
    private ArrayList<ProductOptionValue> productOptionValues = null;

    public String getProductOptionId() {
        return productOptionId;
    }

    public void setProductOptionId(String productOptionId) {
        this.productOptionId = productOptionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMinimumLimit() {
        return minimumLimit;
    }

    public void setMinimumLimit(String minimumLimit) {
        this.minimumLimit = minimumLimit;
    }

    public String getMaximumLimit() {
        return maximumLimit;
    }

    public void setMaximumLimit(String maximumLimit) {
        this.maximumLimit = maximumLimit;
    }

    public String getRequired() {
        return required;
    }

    public void setRequired(String required) {
        this.required = required;
    }

    public ArrayList<ProductOptionValue> getProductOptionValues() {
        return productOptionValues;
    }

    public void setProductValue(ArrayList<ProductOptionValue> productOptionValues) {
        this.productOptionValues = productOptionValues;
    }

}
