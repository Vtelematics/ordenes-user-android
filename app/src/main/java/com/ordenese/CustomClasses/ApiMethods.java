package com.ordenese.CustomClasses;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ordenese.DataSets.CategoryListDataSet;
import com.ordenese.DataSets.CategoryTypeModel;
import com.ordenese.DataSets.HomeBannerDataSet;
import com.ordenese.DataSets.ListDataSet;
import com.ordenese.DataSets.ModelHomeList;
import com.ordenese.DataSets.ProductDataSet;
import com.ordenese.DataSets.ResponseDataSet;
import com.ordenese.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import okhttp3.ResponseBody;

public class ApiMethods {

    @Nullable
    public static ResponseDataSet apiResponse(String result) {
        ResponseDataSet mResponseDS = new ResponseDataSet();

        try {

            JSONObject mResponseObject = new JSONObject(result);
            if (!mResponseObject.isNull(DefaultNames.success)) {
                mResponseDS.setIsSuccess(true);

                JSONObject mSuccessObject = mResponseObject.getJSONObject(DefaultNames.success);
                if (mSuccessObject.length() > 0) {

                    mResponseDS.setResponseEmpty(false);

                    if (!mSuccessObject.isNull(DefaultNames.status)) {
                        mResponseDS.setStatus(mSuccessObject.getString(DefaultNames.status));
                    } else {
                        mResponseDS.setStatus("");
                    }

                    if (!mSuccessObject.isNull(DefaultNames.message)) {
                        mResponseDS.setMessage(mSuccessObject.getString(DefaultNames.message));
                    } else {
                        mResponseDS.setMessage("");
                    }

                } else {
                    mResponseDS.setResponseEmpty(true);
                    mResponseDS.setIfThereIsNoSuccessError(false);
                }

                // ***************************************************************************

            } else if (!mResponseObject.isNull(DefaultNames.error)) {
                mResponseDS.setIsSuccess(false);

                JSONObject mErrorObject = mResponseObject.getJSONObject(DefaultNames.error);
                if (mErrorObject.length() > 0) {

                    mResponseDS.setResponseEmpty(false);

                    if (!mErrorObject.isNull(DefaultNames.status)) {
                        mResponseDS.setStatus(mErrorObject.getString(DefaultNames.status));
                    } else {
                        mResponseDS.setStatus("");
                    }

                    if (!mErrorObject.isNull(DefaultNames.message)) {
                        mResponseDS.setMessage(mErrorObject.getString(DefaultNames.message));
                    } else {
                        mResponseDS.setMessage("");
                    }

                } else {
                    mResponseDS.setResponseEmpty(true);
                    mResponseDS.setIfThereIsNoSuccessError(false);
                }

            } /*else if (!mResponseObject.isNull(DefaultNames.error_warning)) {
                mResponseDS.setIsSuccess(false);

                JSONObject mErrorObject = mResponseObject.getJSONObject(DefaultNames.error_warning);
                if (mErrorObject.length() > 0) {

                    mResponseDS.setResponseEmpty(false);

                    if (!mErrorObject.isNull(DefaultNames.status)) {
                        mResponseDS.setStatus(mErrorObject.getString(DefaultNames.status));
                    } else {
                        mResponseDS.setStatus("");
                    }

                    if (!mErrorObject.isNull(DefaultNames.message)) {
                        mResponseDS.setMessage(mErrorObject.getString(DefaultNames.message));
                    } else {
                        mResponseDS.setMessage("");
                    }

                } else {
                    mResponseDS.setResponseEmpty(true);
                    mResponseDS.setIfThereIsNoSuccessError(false);
                }

            }*/ else {
                //mResponseDS.setResponseEmpty(true);
                mResponseDS.setIfThereIsNoSuccessError(true);
            }

            return mResponseDS;
        } catch (Exception e) {
            //Log.e("apiResponse Execp ", e.toString());
            return null;
        }


    }

    @Nullable
    public static ArrayList<ListDataSet> restaurantList(String result) {

        ArrayList<ListDataSet> mARestaurantList = new ArrayList<>();
        ListDataSet mRestaurantListDs;
        try {

            JSONObject mRestaurantListObject = new JSONObject(result);
            if (!mRestaurantListObject.isNull(DefaultNames.vendor)) {
                JSONArray mRestaurantListArray = mRestaurantListObject.getJSONArray(DefaultNames.vendor);

                //To Check restaurant array length :-
                if (mRestaurantListArray.length() > 0) {

                    for (int rList = 0; rList < mRestaurantListArray.length(); rList++) {

                        JSONObject mRestaurantList = mRestaurantListArray.getJSONObject(rList);
                        mRestaurantListDs = new ListDataSet();

                        mRestaurantListDs.setRestaurantsListEmpty(false);

                        if (mRestaurantList.getString(DefaultNames.vendor_id) != null) {
                            mRestaurantListDs.setRestaurantId(mRestaurantList.getString(DefaultNames.vendor_id));
                        } else {
                            mRestaurantListDs.setRestaurantId("");
                        }

                        if (mRestaurantList.getString(DefaultNames.name) != null) {
                            mRestaurantListDs.setName(mRestaurantList.getString(DefaultNames.name));
                        } else {
                            mRestaurantListDs.setName("");
                        }


                        if (mRestaurantList.getString(DefaultNames.image) != null) {
                            mRestaurantListDs.setImage(mRestaurantList.getString(DefaultNames.image));
                        } else {
                            mRestaurantListDs.setImage("");
                        }


                        if (mRestaurantList.getString(DefaultNames.logo) != null) {
                            mRestaurantListDs.setLogo(mRestaurantList.getString(DefaultNames.logo));
                        } else {
                            mRestaurantListDs.setLogo("");
                        }


                        if (mRestaurantList.getString(DefaultNames.preparing_time) != null) {
                            mRestaurantListDs.setPreparingTime(mRestaurantList.getString(DefaultNames.preparing_time));
                        } else {
                            mRestaurantListDs.setPreparingTime("");
                        }

                       /* if (mRestaurantList.getString(DefaultNames.new_status) != null) {
                            mRestaurantListDs.setNewStatus(mRestaurantList.getString(DefaultNames.new_status));
                        } else {
                            mRestaurantListDs.setNewStatus("");
                        }*/

                        // mRestaurantListDs.setNewStatus("trending_status");



                        /*if (mRestaurantList.getString(DefaultNames.working_status) != null) {
                            mRestaurantListDs.setWorkingStatus(mRestaurantList.getString(DefaultNames.working_status));
                        } else {
                            mRestaurantListDs.setWorkingStatus("");
                        }*/

                        mRestaurantListDs.setWorkingStatus("");

                       /* if (mRestaurantList.getString(DefaultNames.rating) != null) {
                            mRestaurantListDs.setRating(mRestaurantList.getString(DefaultNames.rating));
                        } else {
                            mRestaurantListDs.setRating("0");
                        }*/

                        mRestaurantListDs.setRating("0");

                        /*if (mRestaurantList.getString(DefaultNames.cuisine) != null) {
                            mRestaurantListDs.setCuisine(mRestaurantList.getString(DefaultNames.cuisine));
                        } else {
                            mRestaurantListDs.setCuisine("");
                        }*/
                        mRestaurantListDs.setCuisine("");


                        //vendor_status for to know restaurant open/close.
                        // 0 - close , 1 - open.
                        if (mRestaurantList.getString(DefaultNames.vendor_status) != null) {
                            mRestaurantListDs.setRestaurantStatus(mRestaurantList.getString(DefaultNames.vendor_status));
                        } else {
                            mRestaurantListDs.setRestaurantStatus("");
                        }


                        //vendor_status - its a working status

                       /* if (mRestaurantList.getString(DefaultNames.sort_status) != null) {
                            mRestaurantListDs.setSortStatus(mRestaurantList.getString(DefaultNames.sort_status));
                        } else {
                            mRestaurantListDs.setSortStatus("");
                        }*/
                        mRestaurantListDs.setSortStatus("");

                        if (mRestaurantList.getString(DefaultNames.min_delivery_charge) != null) {
                            mRestaurantListDs.setDeliveryCharge(mRestaurantList.getString(DefaultNames.min_delivery_charge));
                        } else {
                            mRestaurantListDs.setDeliveryCharge("");
                        }

                        if (mRestaurantList.getString(DefaultNames.minimum_amount) != null) {
                            mRestaurantListDs.setMinimumAmount(mRestaurantList.getString(DefaultNames.minimum_amount));
                        } else {
                            mRestaurantListDs.setMinimumAmount("");
                        }

                        if (mRestaurantList.getString(DefaultNames.delivery_time) != null) {
                            mRestaurantListDs.setDeliveryTime(mRestaurantList.getString(DefaultNames.delivery_time));
                        } else {
                            mRestaurantListDs.setDeliveryTime("");
                        }

                        mARestaurantList.add(mRestaurantListDs);

                    }

                    return mARestaurantList;
                } /*else {
                    //  return null;
                    mRestaurantListDs = new ListDataSet();
                    mRestaurantListDs.setRestaurantsListEmpty(true);
                    mARestaurantList.add(mRestaurantListDs);
                    return mARestaurantList;
                }*/

            }
            return null;
        } catch (Exception e) {
            //Log.e("RestList Execp ", e.toString());
            return null;
        }
    }

    public static String getErrorResponseMessage(ResponseBody request_Body, Activity activity) {
        String mResponseMsg = "";

        try {
            //ResponseBody requestBody = request_Body;
            if (request_Body != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(request_Body.byteStream()));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }

                JSONObject jObjError = new JSONObject(total.toString());
                if (!jObjError.isNull("errorDataSet")) {
                    JSONObject jsonErrorObject = jObjError.getJSONObject("errorDataSet");
                    if (!jsonErrorObject.isNull("message")) {
                        //AppFunctions.msgDialogOk(getActivity(), "", jsonErrorObject.getString("message"));
                        mResponseMsg = jsonErrorObject.getString("message");
                    } else {
                       /* if (getActivity() != null) {
                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.process_failed_please_try_again));
                        }*/
                        if (activity != null) {
                            mResponseMsg = activity.getResources().getString(R.string.process_failed_please_try_again);
                        }

                    }
                } else {
                    if (activity != null) {
                        mResponseMsg = activity.getResources().getString(R.string.process_failed_please_try_again);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (activity != null) {
                mResponseMsg = activity.getResources().getString(R.string.process_failed_please_try_again);
            }
        }

        return mResponseMsg;
    }

    @Nullable
    //  @Contract(pure = true)
    public static ArrayList<ModelHomeList> getHomeProductList(/*String bannerDetail,*/ String featureList) {
        try {
            ArrayList<ModelHomeList> modelHomeLists = new ArrayList<>();

           /* ArrayList<HomeBannerDataSet> mHomeBannerList = AppFunctions.sortedHomeBannerList(ApiMethods.homeBannerList(bannerDetail));

            if (mHomeBannerList != null) {
                if (mHomeBannerList.size() > 0) {
                    ModelHomeList modelHomeListBanner = new ModelHomeList();
                    modelHomeListBanner.setType(DefaultNames.TYPE_BANNER);
                    modelHomeListBanner.setBannerList(mHomeBannerList);
                    modelHomeLists.add(modelHomeListBanner);
                }
            }*/

            JSONObject jsonObject = new JSONObject(featureList);

            if (!jsonObject.isNull("products_special")) {
                JSONArray jsonArrayProductList = jsonObject.getJSONArray("products_special");
                if (jsonArrayProductList != null) {
                    if (jsonArrayProductList.length() > 0) {
                        ArrayList<ProductDataSet> productList = new ArrayList<>();
                        for (int i = 0; i < jsonArrayProductList.length(); i++) {
                            JSONObject jsonProductDetail = jsonArrayProductList.getJSONObject(i);
                            ProductDataSet mProductDs = new ProductDataSet();
                            if (jsonProductDetail.getString(DefaultNames.product_id) != null) {
                                mProductDs.setProductId(jsonProductDetail.getString(DefaultNames.product_id));
                            } else {
                                mProductDs.setProductId("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.name) != null) {
                                mProductDs.setName(jsonProductDetail.getString(DefaultNames.name));
                            } else {
                                mProductDs.setName("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.product_description) != null) {
                                mProductDs.setProductDescription(jsonProductDetail.getString(DefaultNames.product_description));
                            } else {
                                mProductDs.setProductDescription("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.delivery_note) != null) {
                                mProductDs.setDeliveryNote(jsonProductDetail.getString(DefaultNames.delivery_note));
                            } else {
                                mProductDs.setDeliveryNote("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.item_note) != null) {
                                mProductDs.setItemNote(jsonProductDetail.getString(DefaultNames.item_note));
                            } else {
                                mProductDs.setItemNote("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.image) != null) {
                                mProductDs.setImage(jsonProductDetail.getString(DefaultNames.image));
                            } else {
                                mProductDs.setImage("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.minimum) != null) {
                                mProductDs.setMinimum(jsonProductDetail.getString(DefaultNames.minimum));
                            } else {
                                mProductDs.setMinimum("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.price) != null) {
                                mProductDs.setPrice(jsonProductDetail.getString(DefaultNames.price));
                            } else {
                                mProductDs.setPrice("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.offer_price) != null) {
                                mProductDs.setOfferPrice(jsonProductDetail.getString(DefaultNames.offer_price));
                            } else {
                                mProductDs.setOfferPrice("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.is_offer) != null) {
                                mProductDs.setIsOffer(jsonProductDetail.getString(DefaultNames.is_offer));
                            } else {
                                mProductDs.setIsOffer("");
                            }
                            if (jsonProductDetail.getString("vendor_id") != null) {
                                mProductDs.setVendor_id(jsonProductDetail.getString("vendor_id"));
                            } else {
                                mProductDs.setVendor_id("0");
                            }
                            if (jsonProductDetail.getString(DefaultNames.price_on_selection) != null) {
                                mProductDs.setPriceOnselection(jsonProductDetail.getString(DefaultNames.price_on_selection));
                            } else {
                                mProductDs.setPriceOnselection("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.sort_order) != null) {
                                mProductDs.setSortOrder(jsonProductDetail.getString(DefaultNames.sort_order));
                            } else {
                                mProductDs.setSortOrder("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.product_to_menu_id) != null) {
                                mProductDs.setProductToMenuId(jsonProductDetail.getString(DefaultNames.product_to_menu_id));
                            } else {
                                mProductDs.setProductToMenuId("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.new_status) != null) {
                                mProductDs.setNewStatus(jsonProductDetail.getString(DefaultNames.new_status));
                            } else {
                                mProductDs.setNewStatus("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.stock) != null) {
                                mProductDs.setStock(jsonProductDetail.getString(DefaultNames.stock));
                            } else {
                                mProductDs.setStock("");
                            }
                            //Add to cart will be disabled
                            mProductDs.setOptionListEmpty(false);

                            productList.add(mProductDs);
                        }

                        if (productList.size() > 0) {
                            ModelHomeList modelHomeListProductList = new ModelHomeList();
                            modelHomeListProductList.setType(DefaultNames.TYPE_PRODUCT_SPECIAL);
                            modelHomeListProductList.setProductList(productList);
                            modelHomeListProductList.setDetail(featureList);
                            modelHomeLists.add(modelHomeListProductList);
                        }
                    }
                }
            }

            if (!jsonObject.isNull(DefaultNames.trending)) {
                JSONArray jsonRestaurantList = jsonObject.getJSONArray(DefaultNames.trending);
                if (jsonRestaurantList != null) {
                    if (jsonRestaurantList.length() > 0) {
                        ArrayList<ListDataSet> restaurantList = new ArrayList<>();
                        for (int i = 0; i < jsonRestaurantList.length(); i++) {
                            JSONObject jsonObjectRestaurantList = jsonRestaurantList.getJSONObject(i);

                            ListDataSet mRestaurantListDs = new ListDataSet();

                            mRestaurantListDs.setRestaurantsListEmpty(false);

                            if (jsonObjectRestaurantList.getString(DefaultNames.vendor_id) != null) {
                                mRestaurantListDs.setRestaurantId(jsonObjectRestaurantList.getString(DefaultNames.vendor_id));
                            } else {
                                mRestaurantListDs.setRestaurantId("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.name) != null) {
                                mRestaurantListDs.setName(jsonObjectRestaurantList.getString(DefaultNames.name));
                            } else {
                                mRestaurantListDs.setName("");
                            }


                            if (jsonObjectRestaurantList.getString(DefaultNames.image) != null) {
                                mRestaurantListDs.setImage(jsonObjectRestaurantList.getString(DefaultNames.image));
                            } else {
                                mRestaurantListDs.setImage("");
                            }


                            if (jsonObjectRestaurantList.getString(DefaultNames.logo) != null) {
                                mRestaurantListDs.setLogo(jsonObjectRestaurantList.getString(DefaultNames.logo));
                            } else {
                                mRestaurantListDs.setLogo("");
                            }


                            if (jsonObjectRestaurantList.getString(DefaultNames.preparing_time) != null) {
                                mRestaurantListDs.setPreparingTime(jsonObjectRestaurantList.getString(DefaultNames.preparing_time));
                            } else {
                                mRestaurantListDs.setPreparingTime("");
                            }

                            /*if (jsonObjectRestaurantList.getString(DefaultNames.new_status) != null) {
                                mRestaurantListDs.setNewStatus(jsonObjectRestaurantList.getString(DefaultNames.new_status));
                            } else {
                                mRestaurantListDs.setNewStatus("");
                            }*/
                            mRestaurantListDs.setNewStatus("");

                           /* if (jsonObjectRestaurantList.getString(DefaultNames.working_status) != null) {
                                mRestaurantListDs.setWorkingStatus(jsonObjectRestaurantList.getString(DefaultNames.working_status));
                            } else {
                                mRestaurantListDs.setWorkingStatus("");
                            }*/
                            mRestaurantListDs.setWorkingStatus("");

                            if (jsonObjectRestaurantList.getString(DefaultNames.rating) != null) {
                                mRestaurantListDs.setRating(jsonObjectRestaurantList.getString(DefaultNames.rating));
                            } else {
                                mRestaurantListDs.setRating("");
                            }

                            /*if (jsonObjectRestaurantList.getString(DefaultNames.cuisine) != null) {
                                mRestaurantListDs.setCuisine(jsonObjectRestaurantList.getString(DefaultNames.cuisine));
                            } else {
                                mRestaurantListDs.setCuisine("");
                            }*/
                            mRestaurantListDs.setCuisine("");

                            if (jsonObjectRestaurantList.getString(DefaultNames.vendor_status) != null) {
                                mRestaurantListDs.setRestaurantStatus(jsonObjectRestaurantList.getString(DefaultNames.vendor_status));
                            } else {
                                mRestaurantListDs.setRestaurantStatus("");
                            }


                            /*if (jsonObjectRestaurantList.getString(DefaultNames.sort_status) != null) {
                                mRestaurantListDs.setSortStatus(jsonObjectRestaurantList.getString(DefaultNames.sort_status));
                            } else {
                                mRestaurantListDs.setSortStatus("");
                            }*/
                            mRestaurantListDs.setSortStatus("");

                            if (jsonObjectRestaurantList.getString(DefaultNames.min_delivery_charge) != null) {
                                mRestaurantListDs.setDeliveryCharge(jsonObjectRestaurantList.getString(DefaultNames.min_delivery_charge));
                            } else {
                                mRestaurantListDs.setDeliveryCharge("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.minimum_amount) != null) {
                                mRestaurantListDs.setMinimumAmount(jsonObjectRestaurantList.getString(DefaultNames.minimum_amount));
                            } else {
                                mRestaurantListDs.setMinimumAmount("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.delivery_time) != null) {
                                mRestaurantListDs.setDeliveryTime(jsonObjectRestaurantList.getString(DefaultNames.delivery_time));
                            } else {
                                mRestaurantListDs.setDeliveryTime("");
                            }
                            restaurantList.add(mRestaurantListDs);
                        }

                        if (restaurantList.size() > 0) {
                            ModelHomeList modelHomeListRestaurantList = new ModelHomeList();
                            modelHomeListRestaurantList.setType(DefaultNames.TYPE_RESTAURANT_LIST);
                            modelHomeListRestaurantList.setRestaurantList(restaurantList);
                            modelHomeListRestaurantList.setDetail(featureList);
                            modelHomeLists.add(modelHomeListRestaurantList);
                        }
                    }
                }
            }

            if (!jsonObject.isNull("liked_restaurants")) {
                JSONArray jsonRestaurantList = jsonObject.getJSONArray("liked_restaurants");
                if (jsonRestaurantList != null) {
                    if (jsonRestaurantList.length() > 0) {
                        ArrayList<ListDataSet> restaurantList = new ArrayList<>();
                        for (int i = 0; i < jsonRestaurantList.length(); i++) {
                            JSONObject jsonObjectRestaurantList = jsonRestaurantList.getJSONObject(i);

                            ListDataSet mRestaurantListDs = new ListDataSet();

                            mRestaurantListDs.setRestaurantsListEmpty(false);

                            if (jsonObjectRestaurantList.getString(DefaultNames.restaurant_id) != null) {
                                mRestaurantListDs.setRestaurantId(jsonObjectRestaurantList.getString(DefaultNames.restaurant_id));
                            } else {
                                mRestaurantListDs.setRestaurantId("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.name) != null) {
                                mRestaurantListDs.setName(jsonObjectRestaurantList.getString(DefaultNames.name));
                            } else {
                                mRestaurantListDs.setName("");
                            }


                            if (jsonObjectRestaurantList.getString(DefaultNames.image) != null) {
                                mRestaurantListDs.setImage(jsonObjectRestaurantList.getString(DefaultNames.image));
                            } else {
                                mRestaurantListDs.setImage("");
                            }


                            if (jsonObjectRestaurantList.getString(DefaultNames.logo) != null) {
                                mRestaurantListDs.setLogo(jsonObjectRestaurantList.getString(DefaultNames.logo));
                            } else {
                                mRestaurantListDs.setLogo("");
                            }


                            if (jsonObjectRestaurantList.getString(DefaultNames.preparing_time) != null) {
                                mRestaurantListDs.setPreparingTime(jsonObjectRestaurantList.getString(DefaultNames.preparing_time));
                            } else {
                                mRestaurantListDs.setPreparingTime("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.new_status) != null) {
                                mRestaurantListDs.setNewStatus(jsonObjectRestaurantList.getString(DefaultNames.new_status));
                            } else {
                                mRestaurantListDs.setNewStatus("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.working_status) != null) {
                                mRestaurantListDs.setWorkingStatus(jsonObjectRestaurantList.getString(DefaultNames.working_status));
                            } else {
                                mRestaurantListDs.setWorkingStatus("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.rating) != null) {
                                mRestaurantListDs.setRating(jsonObjectRestaurantList.getString(DefaultNames.rating));
                            } else {
                                mRestaurantListDs.setRating("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.cuisine) != null) {
                                mRestaurantListDs.setCuisine(jsonObjectRestaurantList.getString(DefaultNames.cuisine));
                            } else {
                                mRestaurantListDs.setCuisine("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.vendor_status) != null) {
                                mRestaurantListDs.setRestaurantStatus(jsonObjectRestaurantList.getString(DefaultNames.vendor_status));
                            } else {
                                mRestaurantListDs.setRestaurantStatus("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.sort_status) != null) {
                                mRestaurantListDs.setSortStatus(jsonObjectRestaurantList.getString(DefaultNames.sort_status));
                            } else {
                                mRestaurantListDs.setSortStatus("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.delivery_charge) != null) {
                                mRestaurantListDs.setDeliveryCharge(jsonObjectRestaurantList.getString(DefaultNames.delivery_charge));
                            } else {
                                mRestaurantListDs.setDeliveryCharge("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.minimum_amount) != null) {
                                mRestaurantListDs.setMinimumAmount(jsonObjectRestaurantList.getString(DefaultNames.minimum_amount));
                            } else {
                                mRestaurantListDs.setMinimumAmount("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.delivery_time) != null) {
                                mRestaurantListDs.setDeliveryTime(jsonObjectRestaurantList.getString(DefaultNames.delivery_time));
                            } else {
                                mRestaurantListDs.setDeliveryTime("");
                            }
                            restaurantList.add(mRestaurantListDs);
                        }

                        if (restaurantList.size() > 0) {
                            ModelHomeList modelHomeListRestaurantList = new ModelHomeList();
                            modelHomeListRestaurantList.setType(DefaultNames.TYPE_RESTAURANT);
                            modelHomeListRestaurantList.setRestaurantList(restaurantList);
                            modelHomeListRestaurantList.setDetail(featureList);
                            modelHomeLists.add(modelHomeListRestaurantList);
                        }
                    }
                }
            }

            if (!jsonObject.isNull("restaurants")) {
                JSONArray jsonRestaurantList = jsonObject.getJSONArray("restaurants");
                if (jsonRestaurantList != null) {
                    if (jsonRestaurantList.length() > 0) {
                        ArrayList<ListDataSet> restaurantList = new ArrayList<>();
                        for (int i = 0; i < jsonRestaurantList.length(); i++) {
                            JSONObject jsonObjectRestaurantList = jsonRestaurantList.getJSONObject(i);

                            ListDataSet mRestaurantListDs = new ListDataSet();

                            mRestaurantListDs.setRestaurantsListEmpty(false);

                            if (jsonObjectRestaurantList.getString(DefaultNames.restaurant_id) != null) {
                                mRestaurantListDs.setRestaurantId(jsonObjectRestaurantList.getString(DefaultNames.restaurant_id));
                            } else {
                                mRestaurantListDs.setRestaurantId("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.name) != null) {
                                mRestaurantListDs.setName(jsonObjectRestaurantList.getString(DefaultNames.name));
                            } else {
                                mRestaurantListDs.setName("");
                            }


                            if (jsonObjectRestaurantList.getString(DefaultNames.image) != null) {
                                mRestaurantListDs.setImage(jsonObjectRestaurantList.getString(DefaultNames.image));
                            } else {
                                mRestaurantListDs.setImage("");
                            }


                            if (jsonObjectRestaurantList.getString(DefaultNames.logo) != null) {
                                mRestaurantListDs.setLogo(jsonObjectRestaurantList.getString(DefaultNames.logo));
                            } else {
                                mRestaurantListDs.setLogo("");
                            }


                            if (jsonObjectRestaurantList.getString(DefaultNames.preparing_time) != null) {
                                mRestaurantListDs.setPreparingTime(jsonObjectRestaurantList.getString(DefaultNames.preparing_time));
                            } else {
                                mRestaurantListDs.setPreparingTime("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.new_status) != null) {
                                mRestaurantListDs.setNewStatus(jsonObjectRestaurantList.getString(DefaultNames.new_status));
                            } else {
                                mRestaurantListDs.setNewStatus("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.working_status) != null) {
                                mRestaurantListDs.setWorkingStatus(jsonObjectRestaurantList.getString(DefaultNames.working_status));
                            } else {
                                mRestaurantListDs.setWorkingStatus("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.rating) != null) {
                                mRestaurantListDs.setRating(jsonObjectRestaurantList.getString(DefaultNames.rating));
                            } else {
                                mRestaurantListDs.setRating("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.cuisine) != null) {
                                mRestaurantListDs.setCuisine(jsonObjectRestaurantList.getString(DefaultNames.cuisine));
                            } else {
                                mRestaurantListDs.setCuisine("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.vendor_status) != null) {
                                mRestaurantListDs.setRestaurantStatus(jsonObjectRestaurantList.getString(DefaultNames.vendor_status));
                            } else {
                                mRestaurantListDs.setRestaurantStatus("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.sort_status) != null) {
                                mRestaurantListDs.setSortStatus(jsonObjectRestaurantList.getString(DefaultNames.sort_status));
                            } else {
                                mRestaurantListDs.setSortStatus("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.delivery_charge) != null) {
                                mRestaurantListDs.setDeliveryCharge(jsonObjectRestaurantList.getString(DefaultNames.delivery_charge));
                            } else {
                                mRestaurantListDs.setDeliveryCharge("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.minimum_amount) != null) {
                                mRestaurantListDs.setMinimumAmount(jsonObjectRestaurantList.getString(DefaultNames.minimum_amount));
                            } else {
                                mRestaurantListDs.setMinimumAmount("");
                            }

                            if (jsonObjectRestaurantList.getString(DefaultNames.delivery_time) != null) {
                                mRestaurantListDs.setDeliveryTime(jsonObjectRestaurantList.getString(DefaultNames.delivery_time));
                            } else {
                                mRestaurantListDs.setDeliveryTime("");
                            }
                            restaurantList.add(mRestaurantListDs);
                        }

                        if (restaurantList.size() > 0) {
                            ModelHomeList modelHomeListRestaurantList = new ModelHomeList();
                            modelHomeListRestaurantList.setType(DefaultNames.TYPE_RESTAURANT);
                            modelHomeListRestaurantList.setRestaurantList(restaurantList);
                            modelHomeListRestaurantList.setDetail(featureList);
                            modelHomeLists.add(modelHomeListRestaurantList);
                        }
                    }
                }
            }

            if (!jsonObject.isNull("products")) {
                JSONArray jsonArrayProductList = jsonObject.getJSONArray("products");
                if (jsonArrayProductList != null) {
                    if (jsonArrayProductList.length() > 0) {
                        ArrayList<ProductDataSet> productList = new ArrayList<>();
                        for (int i = 0; i < jsonArrayProductList.length(); i++) {
                            JSONObject jsonProductDetail = jsonArrayProductList.getJSONObject(i);
                            ProductDataSet mProductDs = new ProductDataSet();
                            if (jsonProductDetail.getString(DefaultNames.product_id) != null) {
                                mProductDs.setProductId(jsonProductDetail.getString(DefaultNames.product_id));
                            } else {
                                mProductDs.setProductId("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.name) != null) {
                                mProductDs.setName(jsonProductDetail.getString(DefaultNames.name));
                            } else {
                                mProductDs.setName("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.product_description) != null) {
                                mProductDs.setProductDescription(jsonProductDetail.getString(DefaultNames.product_description));
                            } else {
                                mProductDs.setProductDescription("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.delivery_note) != null) {
                                mProductDs.setDeliveryNote(jsonProductDetail.getString(DefaultNames.delivery_note));
                            } else {
                                mProductDs.setDeliveryNote("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.item_note) != null) {
                                mProductDs.setItemNote(jsonProductDetail.getString(DefaultNames.item_note));
                            } else {
                                mProductDs.setItemNote("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.image) != null) {
                                mProductDs.setImage(jsonProductDetail.getString(DefaultNames.image));
                            } else {
                                mProductDs.setImage("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.minimum) != null) {
                                mProductDs.setMinimum(jsonProductDetail.getString(DefaultNames.minimum));
                            } else {
                                mProductDs.setMinimum("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.price) != null) {
                                mProductDs.setPrice(jsonProductDetail.getString(DefaultNames.price));
                            } else {
                                mProductDs.setPrice("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.offer_price) != null) {
                                mProductDs.setOfferPrice(jsonProductDetail.getString(DefaultNames.offer_price));
                            } else {
                                mProductDs.setOfferPrice("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.is_offer) != null) {
                                mProductDs.setIsOffer(jsonProductDetail.getString(DefaultNames.is_offer));
                            } else {
                                mProductDs.setIsOffer("");
                            }
                            if (jsonProductDetail.getString("vendor_id") != null) {
                                mProductDs.setVendor_id(jsonProductDetail.getString("vendor_id"));
                            } else {
                                mProductDs.setVendor_id("0");
                            }
                            if (jsonProductDetail.getString(DefaultNames.price_on_selection) != null) {
                                mProductDs.setPriceOnselection(jsonProductDetail.getString(DefaultNames.price_on_selection));
                            } else {
                                mProductDs.setPriceOnselection("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.sort_order) != null) {
                                mProductDs.setSortOrder(jsonProductDetail.getString(DefaultNames.sort_order));
                            } else {
                                mProductDs.setSortOrder("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.product_to_menu_id) != null) {
                                mProductDs.setProductToMenuId(jsonProductDetail.getString(DefaultNames.product_to_menu_id));
                            } else {
                                mProductDs.setProductToMenuId("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.new_status) != null) {
                                mProductDs.setNewStatus(jsonProductDetail.getString(DefaultNames.new_status));
                            } else {
                                mProductDs.setNewStatus("");
                            }
                            if (jsonProductDetail.getString(DefaultNames.stock) != null) {
                                mProductDs.setStock(jsonProductDetail.getString(DefaultNames.stock));
                            } else {
                                mProductDs.setStock("");
                            }
                            //Add to cart will be disabled
                            mProductDs.setOptionListEmpty(false);

                            productList.add(mProductDs);
                        }

                        if (productList.size() > 0) {
                            ModelHomeList modelHomeListProductList = new ModelHomeList();
                            modelHomeListProductList.setType(DefaultNames.TYPE_PRODUCT);
                            modelHomeListProductList.setProductList(productList);
                            modelHomeListProductList.setDetail(featureList);
                            modelHomeLists.add(modelHomeListProductList);
                        }
                    }
                }
            }

            return modelHomeLists;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static ArrayList<HomeBannerDataSet> homeStoreBannerList(String result) {

        ArrayList<HomeBannerDataSet> mHomeBannerList = new ArrayList<>();
        HomeBannerDataSet mHomeBannerDS;
       /* try {
            JSONObject mBannerObject = new JSONObject(result);
            JSONArray mBannerArray = mBannerObject.getJSONArray("store_banner");

            for (int banner = 0; banner < mBannerArray.length(); banner++) {
                mHomeBannerDS = new HomeBannerDataSet();

                JSONObject mBanner = mBannerArray.getJSONObject(banner);
                if (!mBanner.isNull(DefaultNames.name)) {
                    mHomeBannerDS.setName(mBanner.getString(DefaultNames.name));
                } else {
                    mHomeBannerDS.setName("");
                }
                if (!mBanner.isNull(DefaultNames.store_id)) {
                    mHomeBannerDS.setRestaurantId(mBanner.getString(DefaultNames.store_id));
                } else {
                    mHomeBannerDS.setRestaurantId("");
                }
                if (!mBanner.isNull("store_banner_id")) {
                    mHomeBannerDS.setBannserId(mBanner.getString("store_banner_id"));
                } else {
                    mHomeBannerDS.setBannserId("");
                }
                if (!mBanner.isNull(DefaultNames.image)) {
                    mHomeBannerDS.setImage(mBanner.getString(DefaultNames.image));
                } else {
                    mHomeBannerDS.setImage("");
                }
                mHomeBannerList.add(mHomeBannerDS);
            }*/

        //testing purposes :-
        mHomeBannerDS = new HomeBannerDataSet();
        mHomeBannerDS.setName("Product 1");
        mHomeBannerDS.setRestaurantId("");
        mHomeBannerDS.setBannserId("");
        mHomeBannerDS.setImage("");
        mHomeBannerList.add(mHomeBannerDS);

        mHomeBannerDS = new HomeBannerDataSet();
        mHomeBannerDS.setName("Product 2");
        mHomeBannerDS.setRestaurantId("");
        mHomeBannerDS.setBannserId("");
        mHomeBannerDS.setImage("");
        mHomeBannerList.add(mHomeBannerDS);

        mHomeBannerDS = new HomeBannerDataSet();
        mHomeBannerDS.setName("Product 3");
        mHomeBannerDS.setRestaurantId("");
        mHomeBannerDS.setBannserId("");
        mHomeBannerDS.setImage("");
        mHomeBannerList.add(mHomeBannerDS);


        return mHomeBannerList;
        /*} catch (Exception e) {
             //Log.e("bannerList Execp ", e.toString());
            return null;
        }*/

    }

    public static ArrayList<CategoryTypeModel> categoryTypeModels(String data) {

        ArrayList<CategoryTypeModel> categoryTypeModels = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (!jsonObject.isNull(DefaultNames.business_types)) {
                JSONArray jsonRestaurantList = jsonObject.getJSONArray(DefaultNames.business_types);
                if (jsonRestaurantList != null) {
                    if (jsonRestaurantList.length() > 0) {
                        for (int i = 0; i < jsonRestaurantList.length(); i++) {
                            JSONObject jsonObjectRestaurantList = jsonRestaurantList.getJSONObject(i);

                            CategoryTypeModel mRestaurantListDs = new CategoryTypeModel();

                            if (jsonObjectRestaurantList.getString(DefaultNames.logo) != null) {
                                mRestaurantListDs.setImage(jsonObjectRestaurantList.getString(DefaultNames.logo));
                            } else {
                                mRestaurantListDs.setImage("");
                            }
                            if (jsonObjectRestaurantList.getString(DefaultNames.type_id) != null) {
                                mRestaurantListDs.setType_id(jsonObjectRestaurantList.getString(DefaultNames.type_id));
                            } else {
                                mRestaurantListDs.setType_id("");
                            }
                            if (jsonObjectRestaurantList.getString(DefaultNames.name) != null) {
                                mRestaurantListDs.setTitle(jsonObjectRestaurantList.getString(DefaultNames.name));
                            } else {
                                mRestaurantListDs.setTitle("");
                            }

                            categoryTypeModels.add(mRestaurantListDs);
                        }

                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return categoryTypeModels;
    }

    @Nullable
    public static ArrayList<CategoryListDataSet> topPickList(String result) {
        ArrayList<CategoryListDataSet> mCuisinesList = new ArrayList<>();
        CategoryListDataSet mCuisinesListDS;

        /*try {
            JSONObject mCuisineObject = new JSONObject(result);
            JSONArray mCuisineArray = mCuisineObject.getJSONArray(DefaultNames.cuisines);

            //To Check cuisine list array length :-
            if (mCuisineArray.length() > 0) {

                for (int cuisine = 0; cuisine < mCuisineArray.length(); cuisine++) {
                    mCuisinesListDS = new CategoryListDataSet();
                    JSONObject mCuisine = mCuisineArray.getJSONObject(cuisine);

                    if (mCuisine.getString(DefaultNames.cuisine_id) != null) {
                        mCuisinesListDS.setCategoryId(mCuisine.getString(DefaultNames.cuisine_id));
                    } else {
                        mCuisinesListDS.setCategoryId("");
                    }
                    if (mCuisine.getString(DefaultNames.name) != null) {
                        mCuisinesListDS.setName(mCuisine.getString(DefaultNames.name));
                    } else {
                        mCuisinesListDS.setName("");
                    }
                    if (mCuisine.getString(DefaultNames.image) != null) {
                        mCuisinesListDS.setLogo(mCuisine.getString(DefaultNames.image));
                    } else {
                        mCuisinesListDS.setLogo("");
                    }

                    mCuisinesListDS.setSortOrder("");

                    mCuisinesList.add(mCuisinesListDS);
                }

                return mCuisinesList;
            } else {
                return null;
            }*/

        /* } catch (Exception e) {
            //Log.e("categoryList Execp ", e.toString());
            return null;
        }*/

        mCuisinesListDS = new CategoryListDataSet();
        mCuisinesListDS.setCategoryId("1");
        mCuisinesListDS.setName("Past Orders");
        mCuisinesListDS.setLogo("");
        mCuisinesListDS.setSortOrder("");
        mCuisinesList.add(mCuisinesListDS);

        mCuisinesListDS = new CategoryListDataSet();
        mCuisinesListDS.setCategoryId("2");
        mCuisinesListDS.setName("All Shops");
        mCuisinesListDS.setLogo("");
        mCuisinesListDS.setSortOrder("");
        mCuisinesList.add(mCuisinesListDS);

        mCuisinesListDS = new CategoryListDataSet();
        mCuisinesListDS.setCategoryId("3");
        mCuisinesListDS.setName("Free Delivery");
        mCuisinesListDS.setLogo("");
        mCuisinesListDS.setSortOrder("");
        mCuisinesList.add(mCuisinesListDS);

        mCuisinesListDS = new CategoryListDataSet();
        mCuisinesListDS.setCategoryId("4");
        mCuisinesListDS.setName("Discounts");
        mCuisinesListDS.setLogo("");
        mCuisinesListDS.setSortOrder("");
        mCuisinesList.add(mCuisinesListDS);

        return mCuisinesList;

    }


}
