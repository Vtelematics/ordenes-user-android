package com.ordenese.Fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.ordenese.Activities.AppCheckout;
import com.ordenese.Activities.AppLogin;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.AddressGeocodeDataSet;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.CartListApi;
import com.ordenese.DataSets.CartProductsDataSet;
import com.ordenese.DataSets.CartTotalsDataSet;
import com.ordenese.DataSets.CouponListApi;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.RestaurantAddressDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.R;
import com.ordenese.databinding.CartListBinding;
import com.ordenese.databinding.LoginOrGuestBottomSheetBinding;

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

public class CartList extends Fragment implements View.OnClickListener {

    private CartListBinding mCartLBinding;
    private RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;
    private String mVendorName = "", mVendorID = "";

    private RecyclerView.LayoutManager mCLParentLayoutMgr;
    private CartListAdapter mCartListAdapter;
    private ArrayList<Object> mCartWholeList;
    private EditText mEtAddANoteGlobal;

    private CartListAdapter.CartProductsListAdapter mCartProductsListAdapter;
    private CartListAdapter.CartTotalsListAdapter mCartTotalsListAdapter;

    private CartListApi mCartListApi;


    public CartList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.cart_list, container, false);
        mCartLBinding = CartListBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mCartLBinding.imgClBack.setOnClickListener(this);
        mCartLBinding.layClAddItemsBtnContainer.setOnClickListener(this);
        mCartLBinding.layClCheckoutBtnContainer.setOnClickListener(this);
        mCartLBinding.layClEmptyBtn.setOnClickListener(this);

        //initially to hide the bottom button container.
        //then it will show or hide based on cart response :-
        mCartLBinding.cardViewClBottomBtnContainer.setVisibility(View.GONE);

        mCartLBinding.layCartListErrorWarning.setVisibility(View.GONE);

//        if (getArguments() != null) {
//            mVendorName = getArguments().getString(DefaultNames.vendor_name);
//            mVendorID = getArguments().getString(DefaultNames.vendor_id);
//        }


        return mCartLBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        callCartListAPi();

    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if (mId == R.id.img_cl_back || mId == R.id.lay_cl_empty_btn) {
            getParentFragmentManager().popBackStack();
        } else if (mId == R.id.lay_cl_add_items_btn_container) {
            if (getActivity() != null) {

                if (mCartListApi.vendor_type_id != null) {

                    if (mCartListApi.vendor_type_id.equals("2")) {
                        //Its a grocery business type :-
                        if (mVendorID != null && !mVendorID.isEmpty()) {
                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                            GroceryCategoryMainPage m_groceryCategoryMainPage = new GroceryCategoryMainPage();
                            Bundle mBundle = new Bundle();
                            mBundle.putString(DefaultNames.store_id, mVendorID);
                            mBundle.putString(DefaultNames.store_name, mCartListApi.vendor_name);
                            m_groceryCategoryMainPage.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_home_body, m_groceryCategoryMainPage, "m_groceryCategoryMainPage");
                            mFT.addToBackStack("m_groceryCategoryMainPage");
                            mFT.commit();
                        }

                    } else {
                        //Its a food or others Except grocery business type :-
                        if (mVendorID != null && !mVendorID.isEmpty()) {
                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                            RestaurantInfo restaurantInfo = new RestaurantInfo();
                            Bundle mBundle = new Bundle();
                            mBundle.putString("vendor_id", mVendorID);
                            mBundle.putString("product_id", "");
                            restaurantInfo.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_home_body, restaurantInfo, "restaurantInfo");
                            mFT.addToBackStack("restaurantInfo");
                            mFT.commit();
                        }
                    }

                }


            }
        } else if (mId == R.id.lay_cl_checkout_btn_container) {

            if (getActivity() != null) {
                String m_Add_A_Note = mEtAddANoteGlobal.getText().toString();
                if (mCartListApi != null) {

                    if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        AddressGeocodeDataSet addressGeocodeDs = new AddressGeocodeDataSet();
                        Intent mIntent = new Intent(getActivity(), AppCheckout.class);
                        mIntent.putExtra("vendor_id", mVendorID);
                        if ((mVendorID != null && mVendorName != null) && (!mVendorID.isEmpty() && !mVendorName.isEmpty())) {
                            addressGeocodeDs.setRestaurantId(mVendorID);
                            addressGeocodeDs.setRestaurantName(mVendorName);
                            addressGeocodeDs.setAddress(mCartListApi.vendor_address);
                            addressGeocodeDs.setLatitude(mCartListApi.latitude);
                            addressGeocodeDs.setLongitude(mCartListApi.longitude);
                            addressGeocodeDs.setGeocode("");
                            addressGeocodeDs.setPreparingTime("");
                            addressGeocodeDs.setDeliveryTime("");
                            addressGeocodeDs.setCheckOutNote(m_Add_A_Note);
                            if (RestaurantAddressDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                RestaurantAddressDB.getInstance(getActivity()).deleteGeocodeDB();
                                RestaurantAddressDB.getInstance(getActivity()).addRestaurantGeocode(addressGeocodeDs);
                            } else {
                                RestaurantAddressDB.getInstance(getActivity()).addRestaurantGeocode(addressGeocodeDs);
                            }
                            getActivity().startActivity(mIntent);
                        }

                    } else {
                        FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                        LoginOrGuestBottomSheet mLoginOrGuestBottomSheet =
                                new LoginOrGuestBottomSheet(mVendorID, mVendorName, m_Add_A_Note, mCartListApi);
                        mLoginOrGuestBottomSheet.show(mFT, "mLoginOrGuestBottomSheet");


                    }


                }


            }

        }

    }

    private void callCartListAPi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.coupon, "");
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }

                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                    jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                    jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<CartListApi> Call = retrofitInterface.cartListApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<CartListApi>() {
                        @Override
                        public void onResponse(@NonNull Call<CartListApi> call, @NonNull Response<CartListApi> response) {

                            if (response.isSuccessful()) {
                                mCartListApi = response.body();
                                if (mCartListApi != null) {
                                    if (mCartListApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            if (mCartListApi.productsList != null && mCartListApi.productsList.size() > 0) {
                                                mCartLBinding.cardViewClBottomBtnContainer.setVisibility(View.VISIBLE);
                                                mCartLBinding.recyclerCartListParent.setVisibility(View.VISIBLE);
                                                mCartLBinding.layCartListListEmptyParent.setVisibility(View.GONE);
                                                mCartWholeList = new ArrayList<>();
                                                mCartWholeList.add(mCartListApi); // for cart ui.!!
                                                mCartWholeList.add(true); // for add note ui.
                                                mCartWholeList.add(DefaultNames.totals); // for bottom totals ui.
                                                mCLParentLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                                mCartLBinding.recyclerCartListParent.setLayoutManager(mCLParentLayoutMgr);
                                                mCartListAdapter = new CartListAdapter(mCartWholeList);
                                                mCartLBinding.recyclerCartListParent.setAdapter(mCartListAdapter);

                                                if (mCartListApi.error_warning != null && !mCartListApi.error_warning.isEmpty()) {
                                                    //AppFunctions.msgDialogOk(getActivity(), "", mCartListApi.error_warning);
                                                    toDisableCheckoutBtn(true);
                                                } else {
                                                    if (mCartListApi.productsList != null && mCartListApi.productsList.size() > 0) {
                                                        boolean mIsOutOffStock = false;
                                                        for (int p = 0; p < mCartListApi.productsList.size(); p++) {
                                                            if (mCartListApi.productsList.get(p).getStock_status().equals("0")) {
                                                                mIsOutOffStock = true;
                                                                break;
                                                            }
                                                        }
                                                        if (mIsOutOffStock) {
                                                            toDisableCheckoutBtn(false);
                                                        } else {
                                                            toEnableCheckoutBtn();
                                                        }
                                                    } else {
                                                        toDisableCheckoutBtn(false);
                                                    }
                                                }

                                                mCartLBinding.tvClVendorName.setText(mCartListApi.vendor_name);
                                                mVendorID = mCartListApi.vendor_id;
                                                mVendorName = mCartListApi.vendor_name;

                                            } else {

                                                toHideCartList();
                                            }
                                        }
                                    } else {
                                        toHideCartList();
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mCartListApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mCartListApi.error.message);
                                            }
                                        }
                                    }
                                } else {
                                    toHideCartList();
                                }
                            } else {
                                toHideCartList();
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

                            mProgressDialog.cancel();
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartListApi> call, @NonNull Throwable t) {
                            toHideCartList();
                            mProgressDialog.cancel();

                        }
                    });

                } catch (JSONException e) {

                    toHideCartList();

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

    private void toDisableCheckoutBtn(Boolean showErrorWarning) {
        if (getActivity() != null) {
            if (showErrorWarning) {
                mCartLBinding.layCartListErrorWarning.setVisibility(View.VISIBLE);
                mCartLBinding.tvCartListErrorWarning.setText(mCartListApi.error_warning);
            } else {
                mCartLBinding.layCartListErrorWarning.setVisibility(View.GONE);
                mCartLBinding.tvCartListErrorWarning.setText("");
            }
            mCartLBinding.layClCheckoutBtnContainer.setEnabled(false);
            mCartLBinding.layClCheckoutBtnContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_new_req_btn_color_cart_checkout_disable));
        }
    }

    private void toEnableCheckoutBtn() {
        if (getActivity() != null) {
            mCartLBinding.layCartListErrorWarning.setVisibility(View.GONE);
            mCartLBinding.layClCheckoutBtnContainer.setEnabled(true);
            mCartLBinding.layClCheckoutBtnContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_new_req_btn_color_cart_checkout));
        }
    }

    private void toHideCartList() {

        if (getActivity() != null) {
            if (mCartLBinding != null) {

                mCartLBinding.layCartListErrorWarning.setVisibility(View.GONE);

                mCartLBinding.recyclerCartListParent.setVisibility(View.GONE);
                mCartLBinding.layCartListListEmptyParent.setVisibility(View.VISIBLE);
                mCartLBinding.cardViewClBottomBtnContainer.setVisibility(View.GONE);

            }
        }

    }

    private void callCartItemDeleteAPi(String[] productCartId) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {


                JSONObject jsonObject = new JSONObject();
                try {

                    JSONArray productCIdJsonArray = new JSONArray();

                    for (int productId = 0; productId < productCartId.length; productId++) {
                        productCIdJsonArray.put(productId, productCartId[productId]);
                    }

                    jsonObject.put(DefaultNames.product_cart_id, productCIdJsonArray);
                    //Where clear = 0 for delete the requested items from cart list.
                    // If clear = 1 then its delete the all items from cart list:-
                    jsonObject.put(DefaultNames.clear, "0");
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.vendor_id, mVendorID);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ApiResponseCheck> Call = retrofitInterface.cartItemDeleteApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<ApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponseCheck> call, @NonNull Response<ApiResponseCheck> response) {


                            if (response.isSuccessful()) {

                                ApiResponseCheck mApiResponseCheck = response.body();

                                if (mApiResponseCheck != null) {
                                    if (mApiResponseCheck.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            callCartListUpdateAPi();
                                            // callCartListAPi();
                                        }
                                    } else {
                                        //Api response failure :-
                                        mProgressDialog.cancel();
                                        if (getActivity() != null) {
                                            if (mApiResponseCheck.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mApiResponseCheck.error.message);
                                            }
                                        }
                                    }

                                } else {
                                    mProgressDialog.cancel();
                                }


                            } else {
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

                        @Override
                        public void onFailure(@NonNull Call<ApiResponseCheck> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }
                    });

                } catch (JSONException e) {
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

    private void callCartCouponListAPi(String vendorID) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {


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
                    Call<CouponListApi> Call = retrofitInterface.couponListApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<CouponListApi>() {
                        @Override
                        public void onResponse(@NonNull Call<CouponListApi> call, @NonNull Response<CouponListApi> response) {
                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                CouponListApi mCouponListApi = response.body();
                                if (mCouponListApi != null) {
                                    if (mCouponListApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {

                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mCouponListApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mCouponListApi.error.message);
                                            }
                                        }
                                    }
                                }
                            } else {
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

                        @Override
                        public void onFailure(@NonNull Call<CouponListApi> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }
                    });

                } catch (JSONException e) {
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

    //*********************    **********************   ********************************************

    public class CartListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private CartListApi mCartListApi;
        private LinearLayout mListEmptyContainer;
        private RecyclerView mListView;
        private ArrayList<Object> mCartWholeList;
        // private ArrayList<CartTotalsDataSet> mCartTotalsList;

        private RecyclerView.LayoutManager mCartListMgr, mCartListTotalsMgr;


        public CartListAdapter(ArrayList<Object> cartWholeList) {
            this.mCartWholeList = cartWholeList;
            this.mCartListApi = (CartListApi) mCartWholeList.get(0);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == 0) {
                //cart products items ui :-
                return new CartListViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.cart_row, parent, false));
            } else if (viewType == 1) {
                //cart totals ui :-
                return new CartTotalsViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.cart_totals, parent, false));
            } else {
                //add note ui :-
                //its viewType == 2
                return new AddNotesViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.add_notes, parent, false));
            }

        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            if (holder.getItemViewType() == 0) {
                //cart products items ui :-
                CartListViewHolder cartListViewHolder = (CartListViewHolder) holder;

                if (mCartListApi.productsList != null && mCartListApi.productsList.size() > 0) {
                    mCartListMgr = new LinearLayoutManager(getActivity());
                    cartListViewHolder.recyclerViewCartList.setLayoutManager(mCartListMgr);
                    mCartProductsListAdapter = new CartProductsListAdapter(mCartListApi.productsList);
                    cartListViewHolder.recyclerViewCartList.setAdapter(mCartProductsListAdapter);
                }


            } else if (holder.getItemViewType() == 1) {

                //cart totals ui :-
                CartTotalsViewHolder cartTotalsViewHolder = (CartTotalsViewHolder) holder;

                if (mCartListApi.totalsList != null && mCartListApi.totalsList.size() > 0) {
                    mCartListTotalsMgr = new LinearLayoutManager(getActivity());
                    cartTotalsViewHolder.recyclerViewCartTotals.setLayoutManager(mCartListTotalsMgr);
                    mCartTotalsListAdapter = new CartTotalsListAdapter(mCartListApi.totalsList);
                    cartTotalsViewHolder.recyclerViewCartTotals.setAdapter(mCartTotalsListAdapter);
                }


                /*for (int i = 0; i < mTotalsList.size(); i++) {
                    if (mTotalsList.get(i).getTitle().contains(getString(R.string.coupon))) {
                        String text = mTotalsList.get(i).getText().replace("-", " ");
                        String coupon = getString(R.string.you_have_saved) + " " + text +
                                " " + getString(R.string.on_this_bill);
                        cartTotalsViewHolder.textView.setText(coupon);
                    } else {
                        String coupon = getString(R.string.you_have_saved) + " " + 0.0 +
                                " " + getString(R.string.on_this_bill);
                        cartTotalsViewHolder.textView.setText(coupon);
                    }
                }*/


            } else {
                //add note ui :-
                AddNotesViewHolder mCartCouponVH = (AddNotesViewHolder) holder;

                mEtAddANoteGlobal = mCartCouponVH.mEtAddANote;
                mCartCouponVH.mTvAddNoteClear.setVisibility(View.GONE);

                mCartCouponVH.mEtAddANote.addTextChangedListener(new TextWatcher() {

                    public void afterTextChanged(Editable s) {
                        if (getActivity() != null) {
                            if (s.length() > 0) {
                                mCartCouponVH.mTvAddNoteClear.setVisibility(View.VISIBLE);
                            } else {
                                mCartCouponVH.mTvAddNoteClear.setVisibility(View.GONE);
                            }
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }
                });


                mCartCouponVH.mTvAddNoteClear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mCartCouponVH.mEtAddANote.setText("");
                    }
                });

            }

        }


        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }


        @Override
        public int getItemCount() {
            return mCartWholeList.size();
        }

        @Override
        public int getItemViewType(int position) {

            /* mCartWholeList.add(mCartListApi); // for cart ui.!!
               mCartWholeList.add(true); // for coupon ui.
               mCartWholeList.add(DefaultNames.totals); // for bottom totals ui.*/

            if (mCartWholeList.get(position) instanceof String) {
                //cart totals ui :-
                return 1;
            } else if (mCartWholeList.get(position) instanceof Boolean) {
                //cart coupon ui :-
                return 2;
            } else {
                //cart products items ui :-
                return 0;
            }

        }


        public class AddNotesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private EditText mEtAddANote;
            // private ImageView mImgAddNoteDelete;
            private TextView mTvAddNoteClear;


            public AddNotesViewHolder(View itemView) {
                super(itemView);

                mEtAddANote = itemView.findViewById(R.id.et_an_add_a_note);
                //mImgAddNoteDelete = itemView.findViewById(R.id.img_an_add_a_note_delete);
                mTvAddNoteClear = itemView.findViewById(R.id.tv_an_add_a_note_clear);


            }

            @Override
            public void onClick(View v) {

            }
        }


        public class CartListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private RecyclerView recyclerViewCartList;

            public CartListViewHolder(View itemView) {
                super(itemView);

                recyclerViewCartList = itemView.findViewById(R.id.recycler_restaurant_cart_list);
            }

            @Override
            public void onClick(View v) {

            }
        }

        public class CartTotalsViewHolder extends RecyclerView.ViewHolder {

            private LinearLayout cartTotalsContainer;
            private RecyclerView recyclerViewCartTotals;
            TextView textView;

            CartTotalsViewHolder(View itemView) {
                super(itemView);

                cartTotalsContainer = itemView.findViewById(R.id.lay_restaurant_cart_total);
                recyclerViewCartTotals = itemView.findViewById(R.id.recycler_restaurant_cart_totals);
                textView = itemView.findViewById(R.id.coupon_saved_amt);


            }
        }

        public class CartProductsListAdapter extends RecyclerView.Adapter<CartProductsListAdapter.ViewHolder> {

            private ArrayList<CartProductsDataSet> m_Cart_List;
            private LinearLayout mListEmptyContainer;
            private RecyclerView mListView;

            public void toUpdateCartProducts(ArrayList<CartProductsDataSet> cartList) {
                this.m_Cart_List = cartList;
            }

            public CartProductsListAdapter(ArrayList<CartProductsDataSet> cartList) {
                this.m_Cart_List = cartList;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.cart_items_row, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {
                //  ////Log.e("CartProductListAdapter onBindViewHolder","");
                holder.mName.setText(m_Cart_List.get(position).getName());

                if (m_Cart_List.get(position).getOptionsList() != null) {
                    if (m_Cart_List.get(position).getOptionsList().size() > 0) {
                        holder.mOptions.setVisibility(View.VISIBLE);
                        int mListSize = m_Cart_List.get(position).getOptionsList().size();
                        String mCuisinesList = "";
                        for (int options = 0; options < mListSize; options++) {

                            String mOptionNAME = m_Cart_List.get(position).getOptionsList().get(options).getName();

                            if (options == 0) {
                                mCuisinesList = mOptionNAME;
                            } else {
                                mCuisinesList = mCuisinesList + ", " + mOptionNAME;
                            }

                        }
                        holder.mOptions.setText(mCuisinesList);
                    } else {
                        holder.mOptions.setVisibility(View.GONE);
                    }
                } else {
                    holder.mOptions.setVisibility(View.GONE);
                }

                String mStockStatus = m_Cart_List.get(position).getStock_status();
                if (mStockStatus.equals("0")) {
                    holder.mProductOutOfStock.setVisibility(View.VISIBLE);
                } else {
                    holder.mProductOutOfStock.setVisibility(View.GONE);
                }


                AppFunctions.imageLoaderUsingGlide(m_Cart_List.get(position).getImage(), holder.mVendorImg, getActivity());

//                holder.mPrice.setText(m_Cart_List.get(position).getTotal());

                if (!m_Cart_List.get(position).discount_price.isEmpty()) {

                    holder.mDiscountPrice.setVisibility(View.VISIBLE);
                    holder.mDiscountPrice.setText(m_Cart_List.get(position).actual_total);
                    holder.mDiscountPrice.setPaintFlags(holder.mDiscountPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                    holder.mPrice.setText(m_Cart_List.get(position).total);
                } else {
                    holder.mDiscountPrice.setVisibility(View.GONE);
                    holder.mDiscountPrice.setPaintFlags(0);
                    holder.mPrice.setText(m_Cart_List.get(position).total);
                }
                holder.mQuantity.setText(m_Cart_List.get(position).getQuantity());

                holder.mAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (getActivity() != null) {
                            String productCartId = m_Cart_List.get(position).getCart_id();
                            String t = holder.mQuantity.getText().toString();
                            String mStockStatus = m_Cart_List.get(position).getStock_status();
                            if (mStockStatus.equals("0")) {
                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.out_of_stock));
                            } else {
                                // ////Log.e("t",t);
                                int mCount = Integer.valueOf(t);

                                mCount++;

                                //holder.mQuantity.setText(String.valueOf(mCount));
                                //type = 0 (decrement) | type = 1 (increment) :-
                                callCartItemIncrementOrDecrementAPi(productCartId, "1", holder.mQuantity, String.valueOf(mCount));
                            }
                        }


                    }
                });

                holder.mRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String productCartId = m_Cart_List.get(position).getCart_id();

                        String t = holder.mQuantity.getText().toString();
                        //////Log.e("t",t);
                        int mCount = Integer.valueOf(t);

                        if (mCount != 1) {
                            mCount--;
                            // //Log.e("695 ","mCount != 1 : if");
                            //holder.mQuantity.setText(String.valueOf(mCount));
                            //type = 0 (decrement) | type = 1 (increment) :-
                            callCartItemIncrementOrDecrementAPi(productCartId, "0", holder.mQuantity, String.valueOf(mCount));
                        } else {
                            // //Log.e("697 ","mCount != 1 : else");
                            if (getActivity() != null) {
                                // AppFunctions.msgDialogOk(getActivity(),"",getActivity().getString(R.string.otp_sent_to_your_mobile_number));
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder
                                        .setMessage(getActivity().getString(R.string.do_you_want_remove_this_item))
                                        .setCancelable(true)
                                        .setPositiveButton(getActivity().getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                String[] productIds = new String[1];
                                                productIds[0] = m_Cart_List.get(position).getCart_id();
                                                callCartItemDeleteAPi(productIds);
                                                dialog.dismiss();
                                            }
                                        }).setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        }
                                );

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }

                        }


                    }
                });


                if (position == m_Cart_List.size() - 1) {
                    //To visible the horizontal line only when after the last item of cart list :-
                    holder.mViewLine.setVisibility(View.VISIBLE);
                } else {
                    holder.mViewLine.setVisibility(View.GONE);
                }


            }

            private void toDeleteCartItem(int position, String branchId) {

                m_Cart_List.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();

            }


            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

            @Override
            public int getItemCount() {
                return m_Cart_List.size();
            }

            public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                private ImageButton mAdd, mRemove;
                private TextView mName, mOptions, mPrice,mDiscountPrice, mQuantity, mProductErrorMsg, mProductOutOfStock;
                private View mViewLine;
                private LinearLayout mStartInfoContainer;
                private ImageView mVendorImg;

                public ViewHolder(View itemView) {
                    super(itemView);

                    mName = itemView.findViewById(R.id.tv_cl_name);
                    mOptions = itemView.findViewById(R.id.tv_cl_options);
                    mPrice = itemView.findViewById(R.id.tv_cl_item_price);
                    mDiscountPrice = itemView.findViewById(R.id.tv_menu_list_child_amt_when_offer);
                    mQuantity = itemView.findViewById(R.id.tv_cl_item_count);

                    mProductErrorMsg = itemView.findViewById(R.id.tv_cl_item_error_message);

                    mAdd = itemView.findViewById(R.id.img_cl_add);
                    mRemove = itemView.findViewById(R.id.img_cl_remove);

                    mViewLine = itemView.findViewById(R.id.view_cl_horizontal_line);

                    mVendorImg = itemView.findViewById(R.id.iv_cl_restaurant_image);
                    mVendorImg = itemView.findViewById(R.id.iv_cl_restaurant_image);
                    mProductOutOfStock = itemView.findViewById(R.id.tv_cl_item_stock_error);


                }

                @Override
                public void onClick(View v) {

                }
            }
        }

        public class CartTotalsListAdapter extends RecyclerView.Adapter<CartTotalsListAdapter.ViewHolder> {

            private ArrayList<CartTotalsDataSet> mTotalsList;

            public void toUpdateCartTotalsList(ArrayList<CartTotalsDataSet> totalsList) {
                this.mTotalsList = totalsList;
            }

            public CartTotalsListAdapter(ArrayList<CartTotalsDataSet> totalsList) {
                this.mTotalsList = totalsList;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.cart_totals_row, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {
                holder.mCartTotalsTitle.setText(mTotalsList.get(position).getTitle());

                if (LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId().equals("1")) {
                    holder.tv_cl_total_data_linear.setGravity(Gravity.END | Gravity.CENTER);
                    holder.mCartTotalsData.setText(mTotalsList.get(position).getText());
                } else {
                    holder.mCartTotalsData.setText(mTotalsList.get(position).getAmount() + "  " + mTotalsList.get(position).getCurrency());
                    holder.tv_cl_total_data_linear.setGravity(Gravity.START | Gravity.CENTER);
                }

//                if (LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId().equals("1")) {
//                    holder.tv_cl_total_data_linear.setGravity(Gravity.END);
//                    holder.tv_cl_total_data_currency_linear.setGravity(Gravity.START);
//                    holder.mCartTotalsData.setText(mTotalsList.get(position).getAmount());
//                    holder.mCurrency.setText("  " +mTotalsList.get(position).getCurrency());
//                } else {
//                    holder.tv_cl_total_data_linear.setGravity(Gravity.START);
//                    holder.tv_cl_total_data_currency_linear.setGravity(Gravity.START);
//                    holder.mCartTotalsData.setText(mTotalsList.get(position).getAmount());
//                    holder.mCurrency.setText("  "+mTotalsList.get(position).getCurrency());
////                    holder.mCartTotalsData.setText(mTotalsList.get(position).getCurrency());
////                    holder.mCurrency.setText(mTotalsList.get(position).getAmount());
//                }

                //To make Total field only bold and remaining fields are non bold:-
                String mTitleKey = mTotalsList.get(position).getTitle_key();

                if (mTitleKey.equals("total")) {

                    holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.BOLD);
                    holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.BOLD);

                    holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));
                    holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));

                } else if (mTitleKey.equals("offer")) {

                    holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                    holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);

                    holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                    holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));


                } else if (mTitleKey.equals("coupon")) {

                    holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                    holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);

                    holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                    holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));

                } else {

                    holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                    holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);

                    holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));
                    holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));

                }


            }

            @Override
            public long getItemId(int position) {
                return super.getItemId(position);
            }

            @Override
            public int getItemViewType(int position) {
                return super.getItemViewType(position);
            }

            @Override
            public int getItemCount() {
                return mTotalsList.size();
            }

            public class ViewHolder extends RecyclerView.ViewHolder {

                TextView mCartTotalsTitle, mCartTotalsData, mCurrency;
                LinearLayout tv_cl_total_title_linear, tv_cl_total_data_currency_linear, tv_cl_total_data_linear;

                public ViewHolder(View itemView) {
                    super(itemView);

                    tv_cl_total_title_linear = itemView.findViewById(R.id.tv_cl_total_title_linear);
                    tv_cl_total_data_currency_linear = itemView.findViewById(R.id.tv_cl_total_data_currency_linear);
                    tv_cl_total_data_linear = itemView.findViewById(R.id.tv_cl_total_data_linear);
                    mCartTotalsTitle = itemView.findViewById(R.id.tv_cl_total_title);
                    mCartTotalsData = itemView.findViewById(R.id.tv_cl_total_data);
                    mCurrency = itemView.findViewById(R.id.tv_cl_total_data_currency);

                }

            }
        }


    }
    //********************     **********************   ********************************************

    private void callCartItemIncrementOrDecrementAPi(String productCartId, String operationType, TextView tvQuantity, String count) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.cart_id, productCartId);
                    //type = 0 (decrement) | type = 1 (increment) :-
                    jsonObject.put(DefaultNames.type, operationType);
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.vendor_id, mVendorID);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ApiResponseCheck> Call = retrofitInterface.cartItemIncrementOrDecrementApi(mCustomerAuthorization, body);
                    mProgressDialog.show();

                    Call.enqueue(new Callback<ApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponseCheck> call, @NonNull Response<ApiResponseCheck> response) {


                            if (response.isSuccessful()) {

                                ApiResponseCheck mApiResponseCheck = response.body();
                                if (mApiResponseCheck != null) {

                                    if (mApiResponseCheck.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            if (tvQuantity != null) {
                                                tvQuantity.setText(count);
                                            }
                                            callCartListUpdateAPi();
                                            AppFunctions.toastShort(getActivity(), mApiResponseCheck.success.getMessage());
                                            // callCartListAPi();
                                        }
                                    } else {
                                        //Api response failure :-
                                        mProgressDialog.cancel();
                                        if (getActivity() != null) {
                                            if (mApiResponseCheck.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mApiResponseCheck.error.message);
                                            }
                                        }
                                    }

                                } else {
                                    mProgressDialog.cancel();
                                }


                            } else {
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

                        @Override
                        public void onFailure(@NonNull Call<ApiResponseCheck> call, @NonNull Throwable t) {

                            mProgressDialog.cancel();

                        }
                    });

                } catch (JSONException e) {
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

    private void callCartListUpdateAPi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.coupon, "");
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();

                    jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                    jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<CartListApi> Call = retrofitInterface.cartListApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<CartListApi>() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onResponse(@NonNull Call<CartListApi> call, @NonNull Response<CartListApi> response) {
                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                mCartListApi = response.body();
                                if (mCartListApi != null) {
                                    //Log.e("mCartListApi", "not null");
                                    if (mCartListApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            if (mCartListApi.productsList != null && mCartListApi.productsList.size() > 0) {
                                                mCartLBinding.cardViewClBottomBtnContainer.setVisibility(View.VISIBLE);
                                                mCartLBinding.recyclerCartListParent.setVisibility(View.VISIBLE);
                                                mCartLBinding.layCartListListEmptyParent.setVisibility(View.GONE);

                                                if (mCartProductsListAdapter != null) {
                                                    mCartProductsListAdapter.toUpdateCartProducts(mCartListApi.productsList);
                                                    mCartProductsListAdapter.notifyDataSetChanged();

                                                    if (mCartTotalsListAdapter != null && mCartListApi.totalsList != null && mCartListApi.totalsList.size() > 0) {
                                                        mCartTotalsListAdapter.toUpdateCartTotalsList(mCartListApi.totalsList);
                                                        mCartTotalsListAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                               /* if (mCartListApi.error_warning != null && !mCartListApi.error_warning.isEmpty()) {
                                                    //AppFunctions.msgDialogOk(getActivity(), "", mCartListApi.error_warning);
                                                    mCartLBinding.layCartListErrorWarning.setVisibility(View.VISIBLE);
                                                    mCartLBinding.tvCartListErrorWarning.setText(mCartListApi.error_warning);
                                                    mCartLBinding.layClCheckoutBtnContainer.setEnabled(false);
                                                    mCartLBinding.layClCheckoutBtnContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_new_req_btn_color_cart_checkout_disable));
                                                } else {
                                                    mCartLBinding.layCartListErrorWarning.setVisibility(View.GONE);
                                                    mCartLBinding.layClCheckoutBtnContainer.setEnabled(true);
                                                    mCartLBinding.layClCheckoutBtnContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_new_req_btn_color_cart_checkout));
                                                }*/

                                                if (mCartListApi.error_warning != null && !mCartListApi.error_warning.isEmpty()) {
                                                    //AppFunctions.msgDialogOk(getActivity(), "", mCartListApi.error_warning);
                                                    toDisableCheckoutBtn(true);
                                                } else {
                                                    if (mCartListApi.productsList != null && mCartListApi.productsList.size() > 0) {
                                                        boolean mIsOutOffStock = false;
                                                        for (int p = 0; p < mCartListApi.productsList.size(); p++) {
                                                            if (mCartListApi.productsList.get(p).getStock_status().equals("0")) {
                                                                mIsOutOffStock = true;
                                                                break;
                                                            }
                                                        }
                                                        if (mIsOutOffStock) {
                                                            toDisableCheckoutBtn(false);
                                                        } else {
                                                            toEnableCheckoutBtn();
                                                        }
                                                    } else {
                                                        toDisableCheckoutBtn(false);
                                                    }
                                                }
                                            } else {
                                                toHideCartList();
                                            }
                                        }
                                    } else {
                                        toHideCartList();
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mCartListApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mCartListApi.error.message);
                                            }
                                        }
                                    }
                                } else {
                                    toHideCartList();
                                    //Log.e("mCartListApi", "null");
                                }
                            } else {
                                toHideCartList();
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

                        @Override
                        public void onFailure(@NonNull Call<CartListApi> call, @NonNull Throwable t) {
                            toHideCartList();
                            mProgressDialog.cancel();
                        }
                    });
                } catch(JSONException e){
                    toHideCartList();
                    mProgressDialog.cancel();
                    //Log.e("210 Excep ", e.toString());
                    e.printStackTrace();
                }
            }else{
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }
    }

    public static class LoginOrGuestBottomSheet extends BottomSheetDialogFragment {

        LoginOrGuestBottomSheetBinding binding_;
        private String mVendorID = "", mVendorName = "", m_Add_A_Note = "";
        CartListApi m_Cart_List_Api;

        public LoginOrGuestBottomSheet(String vendorID, String vendorName, String add_A_Note,
                                       CartListApi cartListApi) {
            // Required empty public constructor

            this.mVendorID = vendorID;
            this.mVendorName = vendorName;
            this.m_Add_A_Note = add_A_Note;
            this.m_Cart_List_Api = cartListApi;

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            binding_ = LoginOrGuestBottomSheetBinding.inflate(inflater, container, false);

            binding_.layLogBsLoginBtnContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), AppLogin.class);
                        getActivity().startActivity(intent);

                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }
                    }
                }
            });

            binding_.layLogBsGuestBtnContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getActivity() != null) {
                        AddressGeocodeDataSet addressGeocodeDs = new AddressGeocodeDataSet();
                        Intent mIntent = new Intent(getActivity(), AppCheckout.class);
                        mIntent.putExtra("vendor_id", mVendorID);
                        if ((mVendorID != null && mVendorName != null) && (!mVendorID.isEmpty() && !mVendorName.isEmpty())) {
                            addressGeocodeDs.setRestaurantId(mVendorID);
                            addressGeocodeDs.setRestaurantName(mVendorName);
                            addressGeocodeDs.setAddress(m_Cart_List_Api.vendor_address);
                            addressGeocodeDs.setLatitude(m_Cart_List_Api.latitude);
                            addressGeocodeDs.setLongitude(m_Cart_List_Api.longitude);
                            addressGeocodeDs.setGeocode("");
                            addressGeocodeDs.setPreparingTime("");
                            addressGeocodeDs.setDeliveryTime("");
                            addressGeocodeDs.setCheckOutNote(m_Add_A_Note);
                            if (RestaurantAddressDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                RestaurantAddressDB.getInstance(getActivity()).deleteGeocodeDB();
                                RestaurantAddressDB.getInstance(getActivity()).addRestaurantGeocode(addressGeocodeDs);
                            } else {
                                RestaurantAddressDB.getInstance(getActivity()).addRestaurantGeocode(addressGeocodeDs);
                            }
                            getActivity().startActivity(mIntent);
                        }
                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }
                    }
                }
            });

            binding_.layLogBsCancelBtnContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (getActivity() != null) {

                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }
                    }
                }
            });

            return binding_.getRoot();
        }
    }
}