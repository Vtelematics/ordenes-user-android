
package com.ordenese.Fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;
import com.ordenese.Activities.AppLogin;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.CouponDataSet;
import com.ordenese.DataSets.CouponListApi;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CartApplyCall;
import com.ordenese.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DialogueCouponCode extends DialogFragment {

    private View mCouponCodeHandler;
    private EditText mCouponData;
    private String mCoupon = "";
    private CartApplyCall mCartApplyCall;
    private TextInputLayout mCartCouponCodeHolder;
    private RelativeLayout mParentView;

    private RetrofitInterface retrofitInterface;
   // private ProgressDialog mProgressDialog;

    private String mVendorID = "";
    private RecyclerView.LayoutManager mCouponListLayoutMgr;
    private RecyclerView mRecycler_apply_coupon_list;
    private LinearLayout mProgressBarLay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public DialogueCouponCode(){

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mCouponCodeHandler = inflater.inflate(R.layout.cart_coupon_code, container, false);

        if(getDialog() != null){
            if (getDialog().getWindow() != null){
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }


        setting();
        mCartApplyCall = (CartApplyCall) getTargetFragment();

        mProgressBarLay = mCouponCodeHandler.findViewById(R.id.lay_ccc_progress_bar_container);
        mProgressBarLay.setVisibility(View.GONE);


       /* mProgressDialog = new ProgressDialog(DialogueCouponCode.this.getContext());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);*/

        if(getArguments() != null){
            mVendorID = getArguments().getString(DefaultNames.vendor_id);
            callCartCouponListAPi(mVendorID);
        }

        mRecycler_apply_coupon_list= mCouponCodeHandler.findViewById(R.id.recycler_apply_coupon_list);



        return mCouponCodeHandler;
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
    }

    private void setting() {

        mParentView = mCouponCodeHandler.findViewById(R.id.lay_cart_coupon);
        Button mCouponBtn = mCouponCodeHandler.findViewById(R.id.btn_coupon_apply);
        Button mCouponCancelBtn = mCouponCodeHandler.findViewById(R.id.btn_coupon_cancel);
        mCouponData = mCouponCodeHandler.findViewById(R.id.et_ccc_coupon_data);
        mCartCouponCodeHolder = mCouponCodeHandler.findViewById(R.id.coupon_code_holder);

        mCouponBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppFunctions.hideDeviceKeyBoard(mParentView, getActivity());
                if (!mCouponData.getText().toString().isEmpty()) {
                    mCartCouponCodeHolder.setErrorEnabled(false);

                    mCartApplyCall.applyCoupon(mCouponData.getText().toString(), 1);
                    dismiss();
                } else {
                    mCartCouponCodeHolder.setErrorEnabled(true);
                    mCartCouponCodeHolder.setError(getActivity().getResources().getString(R.string.please_enter_the_coupon_code));
                }
            }
        });

        mCouponCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // safety check
        if (getDialog() == null)
            return;

        int width = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._299sdp);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        if (getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(width, height);
    }

    private void callCartCouponListAPi(String vendorID) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        JSONArray vendorsIdJsonArray = new JSONArray();
                        vendorsIdJsonArray.put(0, vendorID);
                        jsonObject.put(DefaultNames.vendor_id, vendorsIdJsonArray);
                        jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                        jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                        AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                        jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                        jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());

                        String mCustomerAuthorization = "";
                        if (AppFunctions.isUserLoggedIn(getActivity())) {
                            mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                        }
                        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                        retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                        Call<CouponListApi> Call = retrofitInterface.couponListApi(mCustomerAuthorization,body);
                        mProgressBarLay.setVisibility(View.VISIBLE);
                        Call.enqueue(new Callback<CouponListApi>() {
                            @Override
                            public void onResponse(@NonNull Call<CouponListApi> call, @NonNull Response<CouponListApi> response) {
                                mProgressBarLay.setVisibility(View.GONE);
                                if (getActivity() != null) {
                                    if (response.isSuccessful()) {
                                        CouponListApi mCouponListApi = response.body();
                                        if (mCouponListApi != null) {
                                            //Log.e("mCouponListApi", "not null");
                                            if (mCouponListApi.success != null) {
                                                //Api response successDataSet :-
                                                mCouponListLayoutMgr = new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false);
                                                mRecycler_apply_coupon_list.setLayoutManager(mCouponListLayoutMgr);
                                                CouponListAdapter couponlistAdapter = new CouponListAdapter(mCouponListApi.couponList);
                                                mRecycler_apply_coupon_list.setAdapter(couponlistAdapter);
                                            } else {
                                                //Api response failure :-
                                                if (mCouponListApi.error != null) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", mCouponListApi.error.message);
                                                }
                                            }
                                        }
                                    } else {
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
                            public void onFailure(@NonNull Call<CouponListApi> call, @NonNull Throwable t) {
                                mProgressBarLay.setVisibility(View.GONE);
                            }
                        });

                    } catch (JSONException e) {
                        mProgressBarLay.setVisibility(View.GONE);
                        //Log.e("210 Excep ", e.toString());
                        e.printStackTrace();
                    }
                } else {
                    AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                    Intent intent = new Intent(getActivity(), AppLogin.class);
                    getActivity().startActivity(intent);
                }
            } else {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }


    }

    public class CouponListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<CouponDataSet> mCouponList;

        public CouponListAdapter(ArrayList<CouponDataSet> couponlist) {
            this.mCouponList=couponlist;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CouponListViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.rc_row_apply_coupon, parent, false));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            CouponListViewHolder couponViewHolder = (CouponListViewHolder) holder;
            couponViewHolder.Tv_coupon_name.setText(mCouponList.get(position).getName());
            couponViewHolder.Tv_coupon_code.setText(mCouponList.get(position).getCode());
            couponViewHolder.Tv_coupon_apply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!mCouponList.get(position).getCode().equals("")) {
                        mCartApplyCall.applyCoupon(mCouponList.get(position).getCode(), 1);
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), getActivity().getResources().getString(R.string.please_enter_the_coupon_code), Toast.LENGTH_SHORT).show();
                    }

                }
            });

        }

        @Override
        public int getItemCount() {
            if(mCouponList==null || mCouponList.size() == 0){
                return 0;
            }else {
                return mCouponList.size();
            }
        }

        /*@Override
        public int getItemViewType(int position) {

            if (mCouponList==null || mCouponList.size() == 0) {
                return 0;
            }else{
                return 1;

            }

        }*/

        public class CouponListViewHolder extends RecyclerView.ViewHolder {

            private TextView Tv_coupon_name,Tv_coupon_code,Tv_coupon_apply/*,Tv_coupon_title,Tv_coupon_valid,Tv_coupon_not_valid,Tv_readdetails*/;
            private LinearLayout layout_coupon_valid;

            public CouponListViewHolder(View itemView) {
                super(itemView);

                Tv_coupon_name=itemView.findViewById(R.id.tv_coupon_name);
                Tv_coupon_code=itemView.findViewById(R.id.tv_coupon_code);
                Tv_coupon_apply=itemView.findViewById(R.id.tv_coupon_apply);
               //Tv_coupon_title=itemView.findViewById(R.id.tv_coupon_title);
                //Tv_coupon_valid=itemView.findViewById(R.id.tv_coupon_valid);
               // Tv_readdetails=itemView.findViewById(R.id.tv_readdetails);
                //Tv_coupon_not_valid=itemView.findViewById(R.id.tv_coupon_not_valid);

                //layout_coupon_valid=itemView.findViewById(R.id.layout_coupon_valid);

            }
        }

        class EmptyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layout_restaurant_list_empty_message;
            ImageView img_empty_image;
            TextView tv_empty_message;

            EmptyViewHolder(View itemView) {
                super(itemView);
                img_empty_image=itemView.findViewById(R.id.img_restaurant_list_empty_image);
                layout_restaurant_list_empty_message=itemView.findViewById(R.id.layout_restaurant_list_empty_message);
                tv_empty_message=itemView.findViewById(R.id.tv_restaurant_list_empty_message);
//                tv_RestaurantStatus.setOnClickListener(this);
            }
        }

    }




}

