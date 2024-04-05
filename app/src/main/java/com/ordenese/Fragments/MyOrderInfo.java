package com.ordenese.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ordenese.Activities.AppCheckout;
import com.ordenese.ApiConnection.ApiClient;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.ApiClass;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.MyOrderInfoApi;
import com.ordenese.DataSets.MyOrderProductDataSet;
import com.ordenese.DataSets.MyOrderProductOptionDataSet;
import com.ordenese.DataSets.MyOrderTotalsDataSet;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.Interfaces.onSetUpdate;
import com.ordenese.R;
import com.ordenese.databinding.FragmentCancelOrderBinding;
import com.ordenese.databinding.MyOrderInfoBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MyOrderInfo extends Fragment implements View.OnClickListener, onSetUpdate {

    private MyOrderInfoBinding mMOInfoBinding;
    private ProgressDialog mProgressDialog;
    RetrofitInterface retrofitInterface;
    private String mOrderID = "";
    private MyOrderInfoApi mMyOrderInfoApi;
    Activity activity;
    private static onSetUpdate reload;
    CartInfo cartInfo;


    public MyOrderInfo() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(AppLanguageSupport.onAttach(context));
        activity = getActivity();
        cartInfo = (CartInfo) getActivity();
        reload = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (getActivity() != null) {

                getActivity().getWindow().getDecorView().setLayoutDirection(
                        "ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(getActivity())) ?
                                View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.my_order_info, container, false);
        mMOInfoBinding = MyOrderInfoBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        if (getArguments() != null) {
            mOrderID = getArguments().getString(DefaultNames.order_id);
        }
        ApiClass.ORDER_ID = ""; // don't delete
        mMOInfoBinding.imgMoiBack.setOnClickListener(this);
        mMOInfoBinding.layMolMyOrdersListParent.setVisibility(View.GONE);

        mMOInfoBinding.tvMoiWriteReview.setVisibility(View.GONE);
        mMOInfoBinding.tvMoiWriteReview.setOnClickListener(this);

        mMOInfoBinding.tvMoiTrackOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mMOInfoBinding.tvMoiTrackOrder.getText().equals(activity.getString(R.string.cancel_order))) {
                    CancelOrder order = new CancelOrder();
                    Bundle bundle = new Bundle();
                    bundle.putString("order_id", mOrderID);
                    order.setArguments(bundle);
                    order.setCancelable(true);
                    order.show(getParentFragmentManager(), "CancelOrder");
                } else {
                    if (mMyOrderInfoApi.order.getOrder_type().equals("1")) {
                        Intent intent = new Intent(getActivity(), AppCheckout.class);
                        intent.putExtra(DefaultNames.from, DefaultNames.fromCheckOut);
                        intent.putExtra("info", mOrderID);
                        startActivity(intent);
                    } else if (mMyOrderInfoApi.order.getOrder_type().equals("2")) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=" + mMyOrderInfoApi.order.getVendor_latitude() + "," + mMyOrderInfoApi.order.getVendor_longitude()));
                            startActivity(intent);
                        } catch (ActivityNotFoundException ane) {
                            Toast.makeText(activity, "", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });

        return mMOInfoBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();
        if (mId == R.id.img_moi_back) {
            if (getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }
        } else if (mId == R.id.tv_moi_write_review) {
            if (getActivity() != null) {
                if (mMyOrderInfoApi != null) {
                    if (mMyOrderInfoApi.order != null) {
                        String mReviewStatus = mMyOrderInfoApi.order.getReview_status();
                        if (mReviewStatus != null && !mReviewStatus.isEmpty() && mReviewStatus.equals("1")) {
                            //If review_status = 1 then show the Write review button and write review page
                            // navigation :-
                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                            MyOrderInfoWriteReview m_myOrderInfoWriteReview = new MyOrderInfoWriteReview();
                            Bundle mBundle = new Bundle();
                            mBundle.putString(DefaultNames.vendor_name, mMyOrderInfoApi.order.getVendor_name());
                            mBundle.putString(DefaultNames.order_id, mMyOrderInfoApi.order.getOrder_id());
                            m_myOrderInfoWriteReview.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_home_body, m_myOrderInfoWriteReview, "m_myOrderInfoWriteReview");
                            mFT.addToBackStack("m_myOrderInfoWriteReview");
                            mFT.commit();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cartInfo.cart_info(false,"","");
        if (getActivity() != null) {
            callOrderInfoApi();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void showOderInfoEmptyMsg() {
        mMOInfoBinding.layMolMyOrdersListParent.setVisibility(View.GONE);
    }

    private void callOrderInfoApi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();

                try {

                    jsonObject.put(DefaultNames.order_id, mOrderID);

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());


                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }

                    mProgressDialog.show();
                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<MyOrderInfoApi> Call = retrofitInterface.orderInfoApi(mCustomerAuthorization, body);
                    Call.enqueue(new Callback<MyOrderInfoApi>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NonNull Call<MyOrderInfoApi> call, @NonNull Response<MyOrderInfoApi> response) {

                            mProgressDialog.cancel();

                            if (getActivity() != null) {

                                if (response.isSuccessful()) {
                                    mMyOrderInfoApi = response.body();

                                    if (mMyOrderInfoApi != null) {
                                        //Log.e("mMyOrderInfoApi", "not null");
                                        if (mMyOrderInfoApi.success != null) {
                                            //Api response successDataSet :-

                                            mMOInfoBinding.layMolMyOrdersListParent.setVisibility(View.VISIBLE);

                                            if (mMyOrderInfoApi.order != null) {
                                                String mOrderStatusId = mMyOrderInfoApi.order.getOrder_status_id();

                                                if (mMyOrderInfoApi.order.schedule_status.equals("1")) {
                                                    mMOInfoBinding.scheduleDateLinear.setVisibility(View.VISIBLE);
                                                    mMOInfoBinding.tvMoiScheduleDateData.setText(mMyOrderInfoApi.order.schedule_date + " " + mMyOrderInfoApi.order.schedule_time);
                                                } else {
                                                    mMOInfoBinding.scheduleDateLinear.setVisibility(View.GONE);
                                                }

//                                                if (mOrderStatusId.equals("8")) {
//                                                    mMOInfoBinding.tvMoiTrackOrder.setVisibility(View.VISIBLE);
//                                                } else {
//                                                    mMOInfoBinding.tvMoiTrackOrder.setVisibility(View.GONE);
//                                                }

                                                if (mOrderStatusId.equals("7") || mOrderStatusId.equals("13") || mOrderStatusId.equals("4")) {
                                                    mMOInfoBinding.tvMoiTrackOrder.setVisibility(View.GONE);
                                                } else {
                                                    mMOInfoBinding.tvMoiTrackOrder.setVisibility(View.VISIBLE);
                                                }

                                                if (mMyOrderInfoApi.order.getOrder_type().equals("1")) {
                                                    mMOInfoBinding.tvMoiOrderTypeData.setText(getActivity().getResources().getString(R.string.delivery));
                                                    mMOInfoBinding.tvMoiOrderShippingAddressTitle.setText(activity.getResources().getString(R.string.shipping_address));
                                                    mMOInfoBinding.tvMoiTrackOrder.setText(activity.getString(R.string.track_order));
                                                } else if (mMyOrderInfoApi.order.getOrder_type().equals("2")) {
                                                    mMOInfoBinding.tvMoiOrderTypeData.setText(getActivity().getResources().getString(R.string.pick_up));
                                                    mMOInfoBinding.tvMoiTrackOrder.setVisibility(View.VISIBLE);
                                                    mMOInfoBinding.tvMoiTrackOrder.setText(activity.getString(R.string.locate_vendor));
                                                    mMOInfoBinding.tvMoiOrderShippingAddressTitle.setText(activity.getResources().getString(R.string.pick_up_address));
                                                }

                                                if (mOrderStatusId.equals("1")) {
                                                    if (mMyOrderInfoApi.order.cancel_status.equals("1")) {
                                                        mMOInfoBinding.tvMoiTrackOrder.setVisibility(View.VISIBLE);
                                                        mMOInfoBinding.tvMoiTrackOrder.setText(activity.getString(R.string.cancel_order));
                                                    }
                                                }

//                                                mMOInfoBinding.tvMoiTrackOrder.setVisibility(View.VISIBLE);
//                                                mMOInfoBinding.tvMoiTrackOrder.setText(activity.getString(R.string.track_order));

                                                if (mOrderStatusId != null && mOrderStatusId.equals("9")) {
                                                    //If order_status_id is 9.Then its refers the current order is completed.
                                                    //show to show the write review button by based on review_status response:-
                                                    String mReviewStatus = mMyOrderInfoApi.order.getReview_status();
                                                    if (mReviewStatus != null && !mReviewStatus.isEmpty() && mReviewStatus.equals("1")) {
                                                        //If review_status = 1 then show the Write review button and write review page
                                                        // navigation.Here user yet to perform rating and  write the review :-
                                                        mMOInfoBinding.tvMoiWriteReview.setVisibility(View.VISIBLE);
                                                    } else {
                                                        //If review_status = 0 then hide the Write review button and dont navigate write review page.
                                                        //Here user done perform rating and write the review.
                                                        mMOInfoBinding.tvMoiWriteReview.setVisibility(View.GONE);
                                                    }
                                                } else {
                                                    mMOInfoBinding.tvMoiWriteReview.setVisibility(View.GONE);
                                                }

                                            } else {
                                                mMOInfoBinding.tvMoiWriteReview.setVisibility(View.GONE);
                                            }


                                            mMOInfoBinding.tvMoiRestaurantNameData.setText(mMyOrderInfoApi.order.getVendor_name());
                                            mMOInfoBinding.tvMoiOrderIdData.setText(mMyOrderInfoApi.order.getOrder_id());
                                            mMOInfoBinding.tvMoiOrderStatusData.setText(mMyOrderInfoApi.order.getOrder_status());

                                            String mOrderDATE = mMyOrderInfoApi.order.getOrdered_date() + " " + mMyOrderInfoApi.order.getOrdered_time();
                                            mMOInfoBinding.tvMoiOrderDateData.setText(mOrderDATE);

                                            mMOInfoBinding.tvMoiOrderShippingAddData.setText(mMyOrderInfoApi.order.getDelivery_address());
                                            mMOInfoBinding.tvMoiPaymentMadeBy.setText(mMyOrderInfoApi.order.getPayment_method());

                                            //Order Products ListHome :-

                                            if (mMyOrderInfoApi.order != null
                                                    && mMyOrderInfoApi.order.getMyOrderProductList() != null
                                                    && mMyOrderInfoApi.order.getMyOrderProductList().size() > 0) {

                                                LayoutInflater mProductsListInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                if (mProductsListInflater != null) {

                                                    mMOInfoBinding.layoutMoiOrderProductsContainer.removeAllViews();

                                                    ArrayList<MyOrderProductDataSet> mProductList = mMyOrderInfoApi.order.getMyOrderProductList();

                                                    for (int products = 0; products < mProductList.size(); products++) {

                                                        View vw = mProductsListInflater.inflate(R.layout.my_orders_more_detals_product_row, null);
                                                        int width = LinearLayout.LayoutParams.MATCH_PARENT;
                                                        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

                                                        //  Log.e("**** products=> ",String.valueOf(products));

                                                        TextView mProductName, mProductOptions, mProductPrice;
                                                        LinearLayout mProductImgContainer = vw.findViewById(R.id.layout_restaurant_more_details_product_image_container);

                                                        LinearLayout mProductOptionsContainer = vw.findViewById(R.id.lay_restaurant_my_orders_more_details_product_options);

                                                        ImageView mProductImg = vw.findViewById(R.id.img_restaurant_more_details_product_image);

                                                        mProductName = vw.findViewById(R.id.tv_restaurant_my_orders_more_details_product_name);
                                                        //mProductOptions = (TextView) vw.findViewById(R.id.tv_restaurant_my_orders_more_details_product_options);
                                                        mProductPrice = vw.findViewById(R.id.tv_restaurant_my_orders_more_details_product_price);


                                                        if (mMyOrderInfoApi.order.getMyOrderProductList().get(products).getOptionList() != null
                                                                && mMyOrderInfoApi.order.getMyOrderProductList().get(products).getOptionList().size() > 0) {

                                                            ArrayList<MyOrderProductOptionDataSet> mOptionList = mProductList.get(products).getOptionList();
                                                            if (mOptionList != null && mOptionList.size() > 0) {
                                                                mProductOptionsContainer.setVisibility(View.VISIBLE);

                                                                LayoutInflater mOptionsListInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                                                                if (mOptionsListInflater != null) {

                                                                    for (int options = 0; options < mOptionList.size(); options++) {

                                                                        View mOptionsView = mOptionsListInflater.inflate(R.layout.order_details_options_row, null);
                                                                        int mOPtionWidth = LinearLayout.LayoutParams.MATCH_PARENT;
                                                                        int mOptionHeight = LinearLayout.LayoutParams.WRAP_CONTENT;

                                                                        TextView mProductOption = mOptionsView.findViewById(R.id.tv_my_orders_more_details_option_data);

                                                                        String mTitle = mOptionList.get(options).getOption_name();
                                                                        String mValue = mOptionList.get(options).getOption_value();

                                                                        String mOptionData = mTitle + " : " + mValue;

                                                                        mProductOption.setText(mOptionData);

                                                                        LinearLayout.LayoutParams mOptionParams = new LinearLayout.LayoutParams(mOPtionWidth, mOptionHeight);
                                                                        mOptionParams.setMargins(0, 1, 0, 1);
                                                                        mOptionsView.setLayoutParams(mOptionParams);

                                                                        mProductOptionsContainer.addView(mOptionsView);

                                                                    }
                                                                }
                                                            } else {
                                                                mProductOptionsContainer.setVisibility(View.VISIBLE);
                                                            }
                                                        }

                                                        mProductImgContainer.setVisibility(View.GONE);

                                                        mProductName.setText(mProductList.get(products).getName());
                                                        mProductPrice.setText(mProductList.get(products).getQuantity() + " x " + mProductList.get(products).getPrice());

                                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                                                        params.setMargins(0, 4, 0, 4);
                                                        vw.setLayoutParams(params);

                                                        // mOrderProductsContainer.addView(vw);
                                                        //layout_moi_order_item_1
                                                        mMOInfoBinding.layoutMoiOrderProductsContainer.addView(vw);
                                                    }
                                                }
                                            }
                                            //Order Totals :-
                                            if (getActivity() != null && mMyOrderInfoApi.order != null
                                                    && mMyOrderInfoApi.order.getMyOrderTotalsList() != null
                                                    && mMyOrderInfoApi.order.getMyOrderTotalsList().size() > 0) {

                                                ArrayList<MyOrderTotalsDataSet> mTotalsList = mMyOrderInfoApi.order.getMyOrderTotalsList();
                                                //lay_moi_order_totals_container
                                                mMOInfoBinding.layMoiOrderTotalsContainer.removeAllViews();
                                                LayoutInflater calInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                for (int orderTotalRow = 0; orderTotalRow < mTotalsList.size(); orderTotalRow++) {

                                                    if (calInflater != null) {

                                                        View shipView = calInflater.inflate(R.layout.totals_row_new, null);
                                                        int width = LinearLayout.LayoutParams.MATCH_PARENT;
                                                        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

                                                        LinearLayout mRowContainer;
                                                        TextView mAmtTitle, mAmtValue;

                                                        mRowContainer = shipView.findViewById(R.id.lay_restaurant_totals_row_new);
                                                        mAmtTitle = shipView.findViewById(R.id.tv_restaurant_totals_row_new_title);
                                                        mAmtValue = shipView.findViewById(R.id.tv_restaurant_totals_row_new_data);

                                                        mAmtTitle.setText(mTotalsList.get(orderTotalRow).getTitle());
                                                        mAmtValue.setText(mTotalsList.get(orderTotalRow).getText());

                                                        if (LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId().equals("1")) {
                                                            mAmtValue.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                                                        } else {
                                                            mAmtValue.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                                                        }

                                                        LinearLayout.LayoutParams shipParams = new LinearLayout.LayoutParams(width, height);
                                                        shipView.setLayoutParams(shipParams);
                                                        mMOInfoBinding.layMoiOrderTotalsContainer.addView(shipView);
                                                    }
                                                }
                                            }
                                        } else {

                                            showOderInfoEmptyMsg();

                                            mProgressDialog.cancel();

                                            //Api response failure :-
                                            if (mMyOrderInfoApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mMyOrderInfoApi.error.message);
                                            }
                                        }
                                    } else {
                                        showOderInfoEmptyMsg();
                                        mProgressDialog.cancel();
                                        //Log.e("mMyOrderInfoApi", "null");
                                    }
                                } else {
                                    showOderInfoEmptyMsg();
                                    mProgressDialog.cancel();
                                    String mErrorMsgToShow = "";
                                    try {
                                        ResponseBody requestBody = response.errorBody();
                                        if (requestBody != null) {
                                            mErrorMsgToShow = AppFunctions.apiResponseErrorMsg(getActivity(), requestBody);
                                        } else {
                                            mErrorMsgToShow = getActivity().getString(R.string.process_failed_please_try_again);
                                        }
                                    } catch (Exception e) {
                                        // e.printStackTrace();
                                        mErrorMsgToShow = getActivity().getString(R.string.process_failed_please_try_again);
                                    }
                                    AppFunctions.msgDialogOk(getActivity(), "", mErrorMsgToShow);
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<MyOrderInfoApi> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                            showOderInfoEmptyMsg();
                        }
                    });

                } catch (Exception e) {
                    showOderInfoEmptyMsg();
                    mProgressDialog.cancel();
                    //Log.e("210 Excep ", e.toString());
                    e.printStackTrace();
                }
            } else {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }
    }

    @Override
    public void reload() {
        callOrderInfoApi();
    }

    public static class CancelOrder extends DialogFragment {

        FragmentCancelOrderBinding binding;
        Activity activity;
        ProgressDialog mProgressDialog;
        RetrofitInterface retrofitInterface;
        String order_id = "";

        public CancelOrder() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                order_id = getArguments().getString("order_id");
            }
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            binding = FragmentCancelOrderBinding.inflate(inflater, container, false);
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);

            binding.submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mProgressDialog.show();
                    cancel_order();
                }
            });

            return binding.getRoot();
        }

        private void cancel_order() {

            if (AppFunctions.networkAvailabilityCheck(activity)) {
                JSONObject object = new JSONObject();
                try {
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        object.put(DefaultNames.guest_status, "0");
                        object.put(DefaultNames.guest_id, "");
                    } else {
                        object.put(DefaultNames.guest_status, "1");
                        object.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    object.put("comment", binding.comment.getText().toString());
                    object.put("order_id", order_id);
                    object.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    object.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());
                    retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }

                    Call<String> call = retrofitInterface.cancel_order(mCustomerAuthorization, body);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful()) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body());
                                    if (!jsonObject.isNull("success")) {
                                        JSONObject object1 = jsonObject.getJSONObject("success");
                                        if (object1.getString("status").equals("111")) {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                                            alertDialogBuilder
                                                    .setMessage(object1.getString("message"))
                                                    .setCancelable(true)
                                                    .setPositiveButton(activity.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.dismiss();
                                                            reload.reload();
                                                        }
                                                    });
                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                            alertDialog.show();
                                        } else {
                                            AppFunctions.toastShort(activity, object1.getString("message"));
                                            reload.reload();
                                        }
                                    } else if (!jsonObject.isNull("error")) {
                                        JSONObject object1 = jsonObject.getJSONObject("error");
                                        AppFunctions.toastShort(activity, object1.getString("message"));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            dismiss();
                            mProgressDialog.cancel();
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            mProgressDialog.cancel();
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    mProgressDialog.cancel();
                }
            } else {
                mProgressDialog.cancel();
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(AppLanguageSupport.onAttach(context));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (getActivity() != null) {
                    getActivity().getWindow().getDecorView().setLayoutDirection(
                            "ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(getActivity())) ?
                                    View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

                }
            }
            activity = getActivity();
        }

        @Override
        public void onStart() {
            super.onStart();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(getDialog().getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setAttributes(lp);
        }

    }

}