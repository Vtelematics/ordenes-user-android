package com.ordenese.DataSets;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class HomeModulesApi {

    @SerializedName("success")
    @Expose
    public SuccessDataSet success;

    @SerializedName("error")
    @Expose
    public ErrorDataSet error;

    @SerializedName("business_types")
    @Expose
    public ArrayList<BusinessTypesDataSet> businessTypesList;

    @SerializedName("banners")
    @Expose
    public ArrayList<BannersDataSet> bannersList;

    @SerializedName("top_pick")
    @Expose
    public ArrayList<TopPickDataSet> topPicksList;

    @SerializedName("best_offer")

    @Expose
    public ArrayList<BestOffersDataSet> bestOffersList;

    @SerializedName("brands")
    @Expose
    public ArrayList<BrandsDataSet> brandsList;

    @SerializedName("drinks")
    @Expose
    public ArrayList<DrinksDataSet> drinksList;

    @SerializedName("delivery_description")
    @Expose
    public String delivery_description;

    @SerializedName("pick_up_description")
    @Expose
    public String pick_up_description;
    @SerializedName("content")
    @Expose
    public HomeModuleContent content;


}
