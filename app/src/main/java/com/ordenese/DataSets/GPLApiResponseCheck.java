package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GPLApiResponseCheck {

    @SerializedName("success")
    @Expose
    public SuccessDataSet success;

    @SerializedName("error")
    @Expose
    public ErrorDataSet error;

    @SerializedName("product_info")
    @Expose
    public GroceryProductsList GroceryProduct;


}
