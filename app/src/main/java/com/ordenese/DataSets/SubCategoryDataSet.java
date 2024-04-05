package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SubCategoryDataSet {

    @SerializedName("sub_category")
    @Expose
    private ArrayList<SubCategoryList> subCategory = new ArrayList<>();
    @SerializedName("success")
    @Expose
    private Success success;

    public void setSubCategory(ArrayList<SubCategoryList> subCategory) {
        this.subCategory = subCategory;
    }

    public void setSuccess(Success success) {
        this.success = success;
    }

    public ArrayList<SubCategoryList> getSubCategory() {
        return subCategory;
    }

    public Success getSuccess() {
        return success;
    }


}
