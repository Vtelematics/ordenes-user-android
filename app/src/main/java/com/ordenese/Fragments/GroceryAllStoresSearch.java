package com.ordenese.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.ordenese.ApiConnection.ApiClient;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.GPApiResponseCheck;
import com.ordenese.DataSets.GroceryProductApi;
import com.ordenese.DataSets.GroceryProductDataSet;
import com.ordenese.DataSets.GroceryStoresSearchApi;
import com.ordenese.DataSets.StoresDataSet;
import com.ordenese.DataSets.VendorDataDataSet;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Databases.WishListDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.Interfaces.MakeBottomMarginForViewBasket;
import com.ordenese.Interfaces.ProductListLoadListener;
import com.ordenese.Interfaces.RestaurantListLoadListener;
import com.ordenese.R;
import com.ordenese.databinding.GroceryAllStoresSearchBinding;

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

public class GroceryAllStoresSearch extends Fragment implements View.OnClickListener {

    private GroceryAllStoresSearchBinding mGroceryASSBinding;
    private ProgressDialog mProgressDialog;

    private String mStore_ID = "", mStore_NAME = "", mStore_STATUS = "";
    private MenuAndItemsAdapter mMenuAndItemsAdapter;

    private RetrofitInterface retrofitInterface;
    private GroceryProductApi mGroceryProductApi;
    private RecyclerView.LayoutManager mSearchPListLayMgr;

    private int mPageCount = 1;
    private ProductListLoadListener mProductListLoadListener;
    private Boolean mLoading = false;
    private int mListTotals = 0;
    private int totalItemCount, lastVisibleItem;
    int visibleThreshold = 20;

    String mCurrentQty = "", mCurrentProductId = "", mCurrentProductName = "";
    private JSONObject mAddToCartObject;

    private String mCurrentSearchText = "";
    long mSearchDelay = 1000; // 1 seconds after user stops typing
    private long mSearchLastTextEdit = 0;
    Handler mSearchHandler = new Handler();
    Runnable mSearchInputFinishChecker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (mSearchLastTextEdit + mSearchDelay - 500)) {
                if (mCurrentSearchText.length() >= 1) {

                    if (mIsBtnGroceryClicked) {
                        toHideDeviceKeyboard();
                        callStoreListAPi();
                    }

                    if (mIsBtnProductsClicked) {
                        toHideDeviceKeyboard();
                        callSearchItemsAPi();
                    }

                }
            }
        }
    };

    private CartInfo cartInfo;

    private MakeBottomMarginForViewBasket mMakeBottomMarginForViewBasket;

    private Boolean mIsBtnGroceryClicked = false, mIsBtnProductsClicked = false;
    private GroceryStoresSearchApi mGStoresListApi;
    private GASPAdapter mGaspAdapter;
    private RecyclerView.LayoutManager mLayoutMgrGCMPParent;
    private ArrayList<Object> mGASParentList;
    private int mGroceryProductPosition = -1;
    Activity activity;

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


    public GroceryAllStoresSearch() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // mParam1 = getArguments().getString(ARG_PARAM1);
            //  mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //  return inflater.inflate(R.layout.grocery_all_stores_search, container, false);
        mGroceryASSBinding = GroceryAllStoresSearchBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        cartInfo = (CartInfo) getActivity();

        mMakeBottomMarginForViewBasket = (MakeBottomMarginForViewBasket) getActivity();


        mGroceryASSBinding.imgGAssBack.setOnClickListener(this);
        mGroceryASSBinding.tvGAssBtnGrocery.setOnClickListener(this);
        mGroceryASSBinding.tvGAssBtnProducts.setOnClickListener(this);

        mGroceryASSBinding.layGAssProductSearchClear.setVisibility(View.INVISIBLE);
        mGroceryASSBinding.layGAssProductSearchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGroceryASSBinding.edtGAssProductSearch.setText("");
            }
        });
        mGroceryASSBinding.edtGAssProductSearch.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if (getActivity() != null) {

                    if (s.length() == 0) {
                        mGroceryASSBinding.layGAssProductSearchClear.setVisibility(View.INVISIBLE);
                        mGroceryASSBinding.tvGAssListEmpty.setVisibility(View.GONE);
                    } else {
                        mGroceryASSBinding.layGAssProductSearchClear.setVisibility(View.VISIBLE);
                    }

                    //avoid triggering event when text is empty
                    if (s.length() > 0) {
                        mSearchLastTextEdit = System.currentTimeMillis();
                        if (mSearchHandler != null && mSearchInputFinishChecker != null) {
                            mSearchHandler.postDelayed(mSearchInputFinishChecker, mSearchDelay);
                        }
                    }


                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {

            }

            public void onTextChanged(CharSequence query, int start,
                                      int before, int count) {

                query = query.toString().toLowerCase();
                if (query.length() >= 1) {
                    mCurrentSearchText = query.toString();
                }
                //You need to remove this to run only once
                if (mSearchHandler != null && mSearchInputFinishChecker != null) {
                    mSearchHandler.removeCallbacks(mSearchInputFinishChecker);
                }
            }
        });


        //*************************************************
        //Initial search type :-
        if (getActivity() != null) {
            mIsBtnGroceryClicked = true;
            mIsBtnProductsClicked = false;
            mGroceryASSBinding.tvGAssBtnGrocery.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_g_ass_type_selected));
            mGroceryASSBinding.tvGAssBtnGrocery.setTextColor(getActivity().getResources().getColor(R.color.white));
            mGroceryASSBinding.tvGAssBtnProducts.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_g_ass_type_un_selected));
            mGroceryASSBinding.tvGAssBtnProducts.setTextColor(getActivity().getResources().getColor(R.color.text_color));
            mGroceryASSBinding.edtGAssProductSearch.setHint(getActivity().getResources().getString(R.string.g_ass_search_grocery));
        }
        //*************************************************


        return mGroceryASSBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {

        }
        cartInfo.cart_info(false, "", "");
        cart_product_count();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
        cartInfo.cart_info(false, "", "");
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if (mId == R.id.img_g_ass_back) {

            if (getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }

        } else if (mId == R.id.tv_g_ass_btn_grocery) {
            //mIsBtnGroceryClicked = true,mIsBtnProductsClicked;

            if (getActivity() != null) {
                if (!mIsBtnGroceryClicked) {

                    mIsBtnGroceryClicked = true;
                    mIsBtnProductsClicked = false;

                    mGroceryASSBinding.tvGAssBtnGrocery.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_g_ass_type_selected));
                    mGroceryASSBinding.tvGAssBtnGrocery.setTextColor(getActivity().getResources().getColor(R.color.white));
                    mGroceryASSBinding.tvGAssBtnProducts.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_g_ass_type_un_selected));
                    mGroceryASSBinding.tvGAssBtnProducts.setTextColor(getActivity().getResources().getColor(R.color.text_color));
                    mGroceryASSBinding.edtGAssProductSearch.setHint(getActivity().getResources().getString(R.string.g_ass_search_grocery));
                    //tv_g_ass_list_empty

                    mGroceryASSBinding.recyclerGAssSearchGroceryList.setVisibility(View.VISIBLE);
                    mGroceryASSBinding.recyclerGAssSearchProductList.setVisibility(View.GONE);

                    if (mCurrentSearchText != null && !mCurrentSearchText.isEmpty()) {
                        toHideDeviceKeyboard();
                        callStoreListAPi();
                    }


                }
            }


        } else if (mId == R.id.tv_g_ass_btn_products) {

            if (getActivity() != null) {
                if (!mIsBtnProductsClicked) {
                    mIsBtnProductsClicked = true;
                    mIsBtnGroceryClicked = false;


                    mGroceryASSBinding.tvGAssBtnGrocery.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_g_ass_type_un_selected));
                    mGroceryASSBinding.tvGAssBtnGrocery.setTextColor(getActivity().getResources().getColor(R.color.text_color));
                    mGroceryASSBinding.tvGAssBtnProducts.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_g_ass_type_selected));
                    mGroceryASSBinding.tvGAssBtnProducts.setTextColor(getActivity().getResources().getColor(R.color.white));
                    mGroceryASSBinding.edtGAssProductSearch.setHint(getActivity().getResources().getString(R.string.g_ass_search_products));

                    mGroceryASSBinding.recyclerGAssSearchGroceryList.setVisibility(View.GONE);
                    mGroceryASSBinding.recyclerGAssSearchProductList.setVisibility(View.VISIBLE);

                    if (mCurrentSearchText != null && !mCurrentSearchText.isEmpty()) {
                        toHideDeviceKeyboard();
                        callSearchItemsAPi();
                    }

                }
            }

        }
    }

    private void cart_product_count() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                mProgressDialog.show();

                try {
                    retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());


                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                    Call<String> Call = retrofitInterface.cart_product_count(mCustomerAuthorization, body);
                    Call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            mProgressDialog.cancel();
                            try {
                                JSONObject object = new JSONObject(response.body());
                                if (!object.isNull("qty_count")) {
                                    if (!object.getString("qty_count").equals("0")) {
                                        if (!object.isNull("total")) {
                                            mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(true);
                                            cartInfo.cart_info(true, object.getString("qty_count"), object.toString());
                                        } else {
                                            mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                                        }
                                    } else {
                                        mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                                        cartInfo.cart_info(false, "", "");
                                    }
                                } else {
                                    mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                                }
                            } catch (JSONException e) {
                                mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                            mProgressDialog.cancel();
                        }
                    });
                } catch (Exception e) {
                    mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
                }


            } else {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_home_restaurant_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }

    }

    private void callSearchItemsAPi() {

        if (getActivity() != null) {
            toHideDeviceKeyboard();
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {


                    jsonObject.put(DefaultNames.category_id, "");
                    jsonObject.put(DefaultNames.sub_category_id, "");
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.page, 1);
                    mPageCount = 1;
                    jsonObject.put(DefaultNames.page_per_unit, DefaultNames.pageCountGrocery);
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    jsonObject.put(DefaultNames.search, mCurrentSearchText);

                    //type -1 => only one vendor product are listed.
                    //type - 2 => All vendor product are listed here.
                    jsonObject.put(DefaultNames.type, "2");
                    jsonObject.put(DefaultNames.vendor_id, "");
                    jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                    String mCustomerAuthorization = "";
                    if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<GroceryProductApi> Call = retrofitInterface.groceryProductList(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<GroceryProductApi>() {
                        @Override
                        public void onResponse(@NonNull Call<GroceryProductApi> call, @NonNull Response<GroceryProductApi> response) {

                            if (getActivity() != null) {
                                mProgressDialog.cancel();

                                if (response.isSuccessful()) {
                                    mGroceryProductApi = response.body();
                                    if (mGroceryProductApi != null) {

                                        if (mGroceryProductApi.success != null) {
                                            //Api response successDataSet :-
                                            if (getActivity() != null) {

                                                if (mGroceryProductApi.productList != null && mGroceryProductApi.productList.size() > 0) {

                                                    if (mGroceryProductApi.total != null && !mGroceryProductApi.total.isEmpty()) {
                                                        mListTotals = Integer.parseInt(mGroceryProductApi.total);
                                                        //  Log.e("mListTotals", "" + mListTotals);
                                                    } else {
                                                        // Log.e("mListTotals", "empty");
                                                    }

                                                    mGroceryASSBinding.recyclerGAssSearchProductList.setVisibility(View.VISIBLE);
                                                    mGroceryASSBinding.tvGAssListEmpty.setVisibility(View.GONE);
                                                    mSearchPListLayMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                                    mGroceryASSBinding.recyclerGAssSearchProductList.setLayoutManager(mSearchPListLayMgr);
                                                    mMenuAndItemsAdapter = new MenuAndItemsAdapter(getActivity(), mGroceryProductApi.productList);
                                                    mGroceryASSBinding.recyclerGAssSearchProductList.setAdapter(mMenuAndItemsAdapter);
                                                    setOnLoadMoreProducts(new ProductListLoadListener() {
                                                        @Override
                                                        public void loadMoreProducts() {

                                                            if (getActivity() != null) {
                                                                // if(mListTotals > 10){
                                                                if (mPageCount <= ((mListTotals / 20) + 1)) {
                                                                    mPageCount++;
                                                                    //  Log.e("mPageCount", "" + mPageCount);
                                                                    if (getActivity() != null) {
                                                                        if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                                                                            //**********************************************************

                                                                            JSONObject jsonObject = new JSONObject();
                                                                            try {

                                                                                jsonObject.put(DefaultNames.category_id, "");
                                                                                jsonObject.put(DefaultNames.sub_category_id, "");
                                                                                jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                                                                                jsonObject.put(DefaultNames.page, mPageCount);

                                                                                jsonObject.put(DefaultNames.page_per_unit, DefaultNames.pageCountGrocery);
                                                                                if (AppFunctions.isUserLoggedIn(getActivity())) {
                                                                                    jsonObject.put(DefaultNames.guest_status, "0");
                                                                                    jsonObject.put(DefaultNames.guest_id, "");
                                                                                } else {
                                                                                    jsonObject.put(DefaultNames.guest_status, "1");
                                                                                    jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                                                                                }
                                                                                jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                                                                                jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());


                                                                                jsonObject.put(DefaultNames.search, mCurrentSearchText);

                                                                                //type -1 => only one vendor product are listed.
                                                                                //type - 2 => All vendor product are listed here.
                                                                                jsonObject.put(DefaultNames.type, "2");
                                                                                jsonObject.put(DefaultNames.vendor_id, "");
                                                                                jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                                                                                RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                                                                                String mCustomerAuthorization = "";
                                                                                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                                                                    mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                                                                                }

                                                                                retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                                                                                Call<GroceryProductApi> Call = retrofitInterface.groceryProductList(mCustomerAuthorization, body);
                                                                                mProgressDialog.show();
                                                                                Call.enqueue(new Callback<GroceryProductApi>() {
                                                                                    @SuppressLint("NotifyDataSetChanged")
                                                                                    @Override
                                                                                    public void onResponse(@NonNull Call<GroceryProductApi> call, @NonNull Response<GroceryProductApi> response) {

                                                                                        mProgressDialog.cancel();

                                                                                        if (response.isSuccessful()) {
                                                                                            GroceryProductApi m_Store_P_Pagination = response.body();
                                                                                            if (m_Store_P_Pagination != null) {

                                                                                                if (m_Store_P_Pagination.success != null) {
                                                                                                    //Api response successDataSet :-
                                                                                                    if (getActivity() != null) {

                                                                                                        if (m_Store_P_Pagination.productList != null) {
                                                                                                            if (m_Store_P_Pagination.productList.size() > 0) {

                                                                                                                //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                                                                                                                mGroceryProductApi.productList.addAll(m_Store_P_Pagination.productList);
                                                                                                                mMenuAndItemsAdapter.notifyDataSetChanged();
                                                                                                                setLoaded();
//
                                                                                                                //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                } else {
                                                                                                    //Api response failure :-
                                                                                                    if (getActivity() != null) {
                                                                                                        if (m_Store_P_Pagination.error != null) {
                                                                                                            AppFunctions.msgDialogOk(getActivity(), "", m_Store_P_Pagination.error.message);
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            } else {
                                                                                                //Log.e("m_Store_LA_Pagination", "null");
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
                                                                                    public void onFailure(@NonNull Call<GroceryProductApi> call, @NonNull Throwable t) {
                                                                                        mProgressDialog.cancel();
                                                                                    }
                                                                                });
                                                                            } catch (JSONException e) {
                                                                                mProgressDialog.cancel();
                                                                                //Log.e("385 Excep ", e.toString());
                                                                                e.printStackTrace();
                                                                            }


                                                                            //**********************************************************


                                                                        } else {


                                                                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                                                            NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                                                                            mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                                                                            mFT.addToBackStack("mNetworkAnalyser");
                                                                            mFT.commit();


                                                                        }
                                                                    }
                                                                }

                                                                //  }


                                                            }


                                                        }
                                                    });


                                                    mGroceryASSBinding.nestedViewGAss.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                                        @Override
                                                        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                                            if (v.getChildAt(v.getChildCount() - 1) != null) {
                                                                if (scrollY > oldScrollY) {
                                                                    if (scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) {
                                                                        //code to fetch more data for endless scrolling
                                                                        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mGroceryASSBinding.recyclerGAssSearchProductList.getLayoutManager();
                                                                        assert linearLayoutManager != null;
                                                                        totalItemCount = linearLayoutManager.getItemCount();
                                                                        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                                                                        if (!mLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                                                            if (mProductListLoadListener != null) {
                                                                                mProductListLoadListener.loadMoreProducts();
                                                                            }
                                                                            mLoading = true;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    });


                                                } else {
                                                    searchProductListEmpty();
                                                }
                                            }
                                        } else {
                                            //Api response failure :-
                                            searchProductListEmpty();
                                            if (mGroceryProductApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mGroceryProductApi.error.message);

                                            }
                                        }
                                    }
                                } else {
                                    searchProductListEmpty();
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
                        public void onFailure(@NonNull Call<GroceryProductApi> call, @NonNull Throwable t) {
                            searchProductListEmpty();
                            mProgressDialog.cancel();
                        }
                    });

                } catch (JSONException e) {
                    searchProductListEmpty();
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

    public void setOnLoadMoreProducts(ProductListLoadListener productListLoadListener) {
        this.mProductListLoadListener = productListLoadListener;
    }

    public void setLoaded() {
        mLoading = false;
    }

    private void searchProductListEmpty() {

        if (getActivity() != null) {

            mGroceryASSBinding.recyclerGAssSearchProductList.setVisibility(View.GONE);
            mGroceryASSBinding.tvGAssListEmpty.setVisibility(View.VISIBLE);
            mGroceryASSBinding.tvGAssListEmpty.setText(getActivity().getResources().getString(R.string.g_afp_search_products_lst_empty_msg));

        }


    }

    private void searchGroceryListEmpty() {

        if (getActivity() != null) {

            mGroceryASSBinding.recyclerGAssSearchProductList.setVisibility(View.GONE);
            mGroceryASSBinding.recyclerGAssSearchGroceryList.setVisibility(View.GONE);
            mGroceryASSBinding.tvGAssListEmpty.setVisibility(View.VISIBLE);
            mGroceryASSBinding.tvGAssListEmpty.setText(getActivity().getResources().getString(R.string.g_ass_search_grocery_lst_empty_msg));

        }


    }

    private void toHideDeviceKeyboard() {
        if (getActivity() != null) {
            if (mGroceryASSBinding != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                // To get the correct window token, lets first get the currently focused view
                View v__iew = mGroceryASSBinding.getRoot();
                // To get the window token when there is no currently focused view, we have a to create a view
                if (v__iew == null) {
                    v__iew = new View(getActivity());
                }
                // hide the keyboard
                imm.hideSoftInputFromWindow(v__iew.getWindowToken(), 0);
            }
        }
    }

    public class MenuAndItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<GroceryProductDataSet> mGroceryProductList;
        private final Context mContext;

        public MenuAndItemsAdapter(Context context, ArrayList<GroceryProductDataSet> groceryProductList) {
            this.mContext = context;
            this.mGroceryProductList = groceryProductList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
           /* if (viewType == 2) {
                return new MenuViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.grocery_search_product_row, parent, false));
            } else {
                return new MenuListEmptyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.store_menu_list_empty, parent, false));
            }*/
            return new MenuViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.grocery_g_ass_product_row
                    , parent, false));
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            //   if (holder.getItemViewType() == 2) {


            MenuViewHolder menuViewHolder = (MenuViewHolder) holder;
            menuViewHolder.mChildMenuItemsContainer.setVisibility(View.VISIBLE);
            menuViewHolder.mChildItemName.setText(mGroceryProductList.get(position).item_name);
            // menuViewHolder.mChildItemDescription.setText(mAdapterMenuAndChildItemsList.get(position).getChildProductDescription());

            String mProductDiscount = mGroceryProductList.get(position).discount;
            String mProductPrice = mGroceryProductList.get(position).price;
            if (mProductDiscount != null && !mProductDiscount.isEmpty()) {

                menuViewHolder.mChildItemPrice.setVisibility(View.VISIBLE);
                menuViewHolder.temp_price.setVisibility(View.VISIBLE);
                menuViewHolder.temp_price.setText(mProductPrice);
                menuViewHolder.temp_price.setPaintFlags(menuViewHolder.temp_price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                menuViewHolder.mChildItemPrice.setText(mProductDiscount);

            } else {

                menuViewHolder.mChildItemPrice.setText(mProductPrice);
                menuViewHolder.mChildItemPrice.setVisibility(View.VISIBLE);
                menuViewHolder.temp_price.setPaintFlags(0);
                menuViewHolder.temp_price.setVisibility(View.GONE);

            }


            AppFunctions.imageLoaderUsingGlide(mGroceryProductList.get(position).logo, menuViewHolder.mProductImage, getActivity());
            String mVendorStatus = mGroceryProductList.get(position).vendorData.vendor_status;
            //1 -  open
            //2 -  busy
            //0 -  closed
            if (getActivity() != null) {
                if (mVendorStatus.equals("0")) {
                    //closed :-
                    menuViewHolder.mLayImgOver.setVisibility(View.VISIBLE);
                    menuViewHolder.mtvImgOverVendorStatus.setText(getActivity().getResources().getString(R.string.closed));
                } else if (mVendorStatus.equals("2")) {
                    //Busy :-
                    menuViewHolder.mLayImgOver.setVisibility(View.VISIBLE);
                    menuViewHolder.mtvImgOverVendorStatus.setText(getActivity().getResources().getString(R.string.busy));
                } else {
                    //Open :-
                    menuViewHolder.mLayImgOver.setVisibility(View.GONE);
                    menuViewHolder.mtvImgOverVendorStatus.setText("");
                }
            }
            // //mLayImgOver,mtvImgOverVendorStatus


            menuViewHolder.mChildMenuItemsContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mContext != null) {


                        FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                        GroceryProductDetails groceryProductDetails = new GroceryProductDetails();
                        Bundle mBundle = new Bundle();

                        mStore_ID = mGroceryProductList.get(position).vendorData.vendor_id;
                        mStore_NAME = mGroceryProductList.get(position).vendorData.vendor_name;
                        mStore_STATUS = mGroceryProductList.get(position).vendorData.vendor_status;

                        mBundle.putString("vendor_id", mStore_ID);
                        mBundle.putString("vendor_name", mStore_NAME);
                        mBundle.putString("vendor_status", mStore_STATUS);
                        mBundle.putString("product_id", mGroceryProductList.get(position).product_item_id);
                        mBundle.putString("latitude", AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode().getmLatitude());
                        mBundle.putString("longitude", AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode().getmLongitude());
                        groceryProductDetails.setArguments(mBundle);
                        mFT.replace(R.id.layout_app_home_body, groceryProductDetails, "groceryProductDetails");
                        mFT.addToBackStack("groceryProductDetails");
                        mFT.commit();

                    }
                }
            });
            //  }

            //mLayInitialAddToBasket,mLayCartProductBasket
            //mImgBtnQtyAdd,mImgBtnQtyRemove,mTvCartProductQty

            String mCartQTY = mGroceryProductList.get(position).cart_qty;
            if (mCartQTY != null && !mCartQTY.isEmpty()) {
                menuViewHolder.mLayInitialAddToBasket.setVisibility(View.GONE);
                menuViewHolder.mLayCartProductBasket.setVisibility(View.VISIBLE);
                menuViewHolder.mTvCartProductQty.setText(mCartQTY);
            } else {
                menuViewHolder.mLayInitialAddToBasket.setVisibility(View.VISIBLE);
                menuViewHolder.mLayCartProductBasket.setVisibility(View.GONE);
            }

            menuViewHolder.mLayInitialAddToBasket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //  mGlobal_CVInitialAddToCart = menuViewHolder.mLayInitialAddToBasket;
                    //  mGlobal_CVQtyAddToCart = menuViewHolder.mLayCartProductBasket;
                    //  mGlobal_CartCountRemove = menuViewHolder.mImgBtnQtyRemove;


                    mCurrentQty = "1";
                    mCurrentProductId = mGroceryProductList.get(position).product_item_id;
                    mCurrentProductName = mGroceryProductList.get(position).item_name;

                    mStore_ID = mGroceryProductList.get(position).vendorData.vendor_id;
                    mStore_NAME = mGroceryProductList.get(position).vendorData.vendor_name;
                    mStore_STATUS = mGroceryProductList.get(position).vendorData.vendor_status;

                    if (mStore_STATUS != null && (mStore_STATUS.equals("0") || mStore_STATUS.equals("2"))) {
                        //Currently vendor closed or busy so to show message to user
                        //and perform add to cart :-
                        mGroceryProductPosition = position;
                        // Log.e(mGroceryProductList.get(position).item_name + "--mGroceryProductPosition", "" + mGroceryProductPosition);
                        toPerformAddToCart();
                    } else {
                        //here restaurant is open or busy :-
                        mGroceryProductPosition = position;
                        // Log.e(mGroceryProductList.get(position).item_name + "--mGroceryProductPosition", "" + mGroceryProductPosition);
                        add_to_cart();
                    }


                }
            });

            menuViewHolder.mImgBtnQtyAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    mStore_ID = mGroceryProductList.get(position).vendorData.vendor_id;
                    mStore_NAME = mGroceryProductList.get(position).vendorData.vendor_name;
                    mStore_STATUS = mGroceryProductList.get(position).vendorData.vendor_status;

                    String productCartId = mGroceryProductList.get(position).cart_id;

                    String t = menuViewHolder.mTvCartProductQty.getText().toString();

                    int mCount = Integer.valueOf(t);
                    mCount++;

                    //type = 0 (decrement) | type = 1 (increment) :-
                    mGroceryProductPosition = position;
                    //  Log.e(mGroceryProductList.get(position).item_name + "--mGroceryProductPosition", "" + mGroceryProductPosition);
                    callCartItemIncrementOrDecrementAPi(productCartId, "1", menuViewHolder.mTvCartProductQty, String.valueOf(mCount));


                }
            });

            menuViewHolder.mImgBtnQtyRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mStore_ID = mGroceryProductList.get(position).vendorData.vendor_id;
                    mStore_NAME = mGroceryProductList.get(position).vendorData.vendor_name;
                    mStore_STATUS = mGroceryProductList.get(position).vendorData.vendor_status;

                    String productCartId = mGroceryProductList.get(position).cart_id;

                    String t = menuViewHolder.mTvCartProductQty.getText().toString();
                    int mCount = Integer.valueOf(t);

                    if (mCount != 1) {
                        mCount--;
                        //type = 0 (decrement) | type = 1 (increment) :-
                        mGroceryProductPosition = position;
                        //   Log.e(mGroceryProductList.get(position).item_name + "--mGroceryProductPosition", "" + mGroceryProductPosition);
                        callCartItemIncrementOrDecrementAPi(productCartId, "0", menuViewHolder.mTvCartProductQty, String.valueOf(mCount));
                    } else {

                        if (getActivity() != null) {
                            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
                            alertDialogBuilder
                                    .setMessage(getActivity().getString(R.string.do_you_want_remove_this_item))
                                    .setCancelable(true)
                                    .setPositiveButton(getActivity().getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            mGroceryProductPosition = position;
                                            //  Log.e(mGroceryProductList.get(position).item_name + "--mGroceryProductPosition", "" + mGroceryProductPosition);
                                            callCartItemDeleteAPi(productCartId, menuViewHolder.mLayInitialAddToBasket,
                                                    menuViewHolder.mLayCartProductBasket);

                                            dialog.dismiss();
                                        }
                                    }).setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    }
                            );

                            androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        }

                    }


                }
            });


        }


        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public int getItemCount() {

            return mGroceryProductList.size();

        }

       /* @Override
        public int getItemViewType(int position) {
            if (mAdapterMenuAndChildItemsList != null) {
                if (mAdapterMenuAndChildItemsList.size() > 0) {
                    return 2;
                } else {
                    return 1;
                }
            } else {
                return 1;
            }

        }*/

        public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            View mChildBottomLine;

            TextView mChildItemName;
            // TextView mChildItemDescription;
            TextView mChildItemPrice, temp_price;
            private ImageView mProductImage;
            LinearLayout mChildMenuItemsContainer;

            private LinearLayout mLayInitialAddToBasket, mLayCartProductBasket;
            private ImageButton mImgBtnQtyAdd, mImgBtnQtyRemove;
            private TextView mTvCartProductQty;

            private LinearLayout mLayImgOver;
            private TextView mtvImgOverVendorStatus;

            public MenuViewHolder(View itemView) {
                super(itemView);


                mLayImgOver = itemView.findViewById(R.id.lay_gsp_image_over);
                mtvImgOverVendorStatus = itemView.findViewById(R.id.tv_gsp_image_over_status);

                mChildItemName = itemView.findViewById(R.id.product_name);
                // mChildItemDescription = itemView.findViewById(R.id.product_desc);
                mChildItemPrice = itemView.findViewById(R.id.product_price);
                temp_price = itemView.findViewById(R.id.temp_price);
                mChildBottomLine = itemView.findViewById(R.id.view_bottom);
                mProductImage = itemView.findViewById(R.id.mProductImage);
                mChildMenuItemsContainer = itemView.findViewById(R.id.product_container_linear);

                mLayInitialAddToBasket = itemView.findViewById(R.id.lay_gsp_initial_add_to_basket);
                mLayCartProductBasket = itemView.findViewById(R.id.lay_gsp_cart_product_basket);

                mImgBtnQtyAdd = itemView.findViewById(R.id.img_gsp_add);
                mImgBtnQtyRemove = itemView.findViewById(R.id.img_gsp_remove);
                mTvCartProductQty = itemView.findViewById(R.id.tv_gsp_item_count);

                //mLayInitialAddToBasket,mLayCartProductBasket
                //mImgBtnQtyAdd,mImgBtnQtyRemove,mTvCartProductQty


            }

            @Override
            public void onClick(View v) {

            }
        }

        public class MenuListEmptyViewHolder extends RecyclerView.ViewHolder {
            LinearLayout menuListEmptyUI;

            MenuListEmptyViewHolder(View itemView) {
                super(itemView);
                menuListEmptyUI = itemView.findViewById(R.id.layout_restaurant_menu_item_list_empty_message);
            }
        }

    }

    private void toPerformAddToCart() {

        if (getActivity() != null) {
            String mFINALMsg = mStore_NAME + " " + getActivity().getResources().getString(R.string.pd_is_not_available) + " " +
                    mCurrentProductName + " " + getActivity().getResources().getString(R.string.pd_at_this_time);

            // AppFunctions.msgDialogOk(getActivity(),"",getActivity().getString(R.string.otp_sent_to_your_mobile_number));
            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
            alertDialogBuilder
                    .setMessage(mFINALMsg)
                    .setCancelable(false)
                    .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            add_to_cart();
                            dialog.dismiss();
                        }
                    }).setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }


    }

    private void add_to_cart() {

        if (getActivity() != null) {

            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                mAddToCartObject = new JSONObject();
                JSONObject product_obj = new JSONObject();
                JSONArray product_array = new JSONArray();
                JSONObject option_object = new JSONObject();
                try {
                    product_obj.put(DefaultNames.product_id, mCurrentProductId);
                    product_obj.put(DefaultNames.quantity, mCurrentQty);
                    product_obj.put(DefaultNames.option, option_object);
                    product_array.put(product_obj);
                    mAddToCartObject.put(DefaultNames.products, product_array);
                    mAddToCartObject.put(DefaultNames.latitude, "0.0");
                    mAddToCartObject.put(DefaultNames.longitude, "0.0");
                    mAddToCartObject.put(DefaultNames.vendor_id, mStore_ID);

                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mAddToCartObject.put(DefaultNames.guest_status, "0");
                        mAddToCartObject.put(DefaultNames.guest_id, "");
                    } else {
                        mAddToCartObject.put(DefaultNames.guest_status, "1");
                        mAddToCartObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    mAddToCartObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    mAddToCartObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    mAddToCartObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    mAddToCartObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), mAddToCartObject.toString());


                    mProgressDialog.show();

                    retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                    String mCustomerAuthorization = "";
                    if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    Call<String> call = retrofitInterface.add_to_cart(mCustomerAuthorization, body);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            mAddToCartObject = null;

                            if (response.isSuccessful()) {
                                try {
                                    JSONObject obj = new JSONObject(response.body());
                                    if (!obj.isNull("error")) {
                                        JSONObject jsonObject = obj.getJSONObject("error");
                                        if (!jsonObject.isNull("status") && jsonObject.getString("status").equals("007")) {
                                            if (!obj.isNull("error_warning")) {
                                                if (!obj.getString("error_warning").isEmpty()) {
                                                    delete_cart_dialog(obj.getString("error_warning"));
                                                }
                                            }
                                        } else {
                                            if (!obj.isNull("error_warning")) {
                                                if (!obj.getString("error_warning").isEmpty()) {
                                                    getDialog(obj.getString("error_warning"));
                                                }
                                            }
                                        }
                                    }
                                    if (!obj.isNull("success")) {
                                        JSONObject jsonObject = obj.getJSONObject("success");
                                        if (!jsonObject.isNull("message")) {
                                            if (!jsonObject.getString("message").isEmpty()) {

                                            }
                                        }
                                        if (!obj.isNull("error_warning")) {
                                            if (!obj.getString("error_warning").isEmpty()) {
                                                getDialog(obj.getString("error_warning"));
                                            } else {

                                                GroceryProductDataSet mGroceryPDS = new GroceryProductDataSet();
                                                if (!obj.isNull("product_info")) {
                                                    JSONObject objectProductInfo = obj.getJSONObject("product_info");
                                                    if (!objectProductInfo.isNull("product_item_id") && !objectProductInfo.getString("product_item_id").isEmpty()) {
                                                        mGroceryPDS.setProduct_item_id(objectProductInfo.getString("product_item_id"));
                                                    } else {
                                                        mGroceryPDS.setProduct_item_id("");
                                                    }
                                                    if (!objectProductInfo.isNull("item_name") && !objectProductInfo.getString("item_name").isEmpty()) {
                                                        mGroceryPDS.setItem_name(objectProductInfo.getString("item_name"));
                                                    } else {
                                                        mGroceryPDS.setItem_name("");
                                                    }
                                                    if (!objectProductInfo.isNull("description") && !objectProductInfo.getString("description").isEmpty()) {
                                                        mGroceryPDS.setDescription(objectProductInfo.getString("description"));
                                                    } else {
                                                        mGroceryPDS.setDescription("");
                                                    }
                                                    if (!objectProductInfo.isNull("price") && !objectProductInfo.getString("price").isEmpty()) {
                                                        mGroceryPDS.setPrice(objectProductInfo.getString("price"));
                                                    } else {
                                                        mGroceryPDS.setPrice("");
                                                    }
                                                    if (!objectProductInfo.isNull("qty") && !objectProductInfo.getString("qty").isEmpty()) {
                                                        mGroceryPDS.setQty(objectProductInfo.getString("qty"));
                                                    } else {
                                                        mGroceryPDS.setQty("");
                                                    }
                                                    if (!objectProductInfo.isNull("cart_id") && !objectProductInfo.getString("cart_id").isEmpty()) {
                                                        mGroceryPDS.setCart_id(objectProductInfo.getString("cart_id"));
                                                    } else {
                                                        mGroceryPDS.setCart_id("");
                                                    }
                                                    if (!objectProductInfo.isNull("cart_qty") && !objectProductInfo.getString("cart_qty").isEmpty()) {
                                                        mGroceryPDS.setCart_qty(objectProductInfo.getString("cart_qty"));
                                                    } else {
                                                        mGroceryPDS.setCart_qty("");
                                                    }
                                                    if (!objectProductInfo.isNull("discount") && !objectProductInfo.getString("discount").isEmpty()) {
                                                        mGroceryPDS.setDiscount(objectProductInfo.getString("discount"));
                                                    } else {
                                                        mGroceryPDS.setDiscount("");
                                                    }
                                                    if (!objectProductInfo.isNull("logo") && !objectProductInfo.getString("logo").isEmpty()) {
                                                        mGroceryPDS.setLogo(objectProductInfo.getString("logo"));
                                                    } else {
                                                        mGroceryPDS.setLogo("");
                                                    }
                                                    if (!objectProductInfo.isNull("picture") && !objectProductInfo.getString("picture").isEmpty()) {
                                                        mGroceryPDS.setPicture(objectProductInfo.getString("picture"));
                                                    } else {
                                                        mGroceryPDS.setPicture("");
                                                    }
                                                    VendorDataDataSet vendorData = new VendorDataDataSet();
                                                    if (!objectProductInfo.isNull("vendorData")) {
                                                        JSONObject objectVendorInfo = objectProductInfo.getJSONObject("vendorData");
                                                        if (!objectVendorInfo.isNull("vendor_id") && !objectVendorInfo.getString("vendor_id").isEmpty()) {
                                                            vendorData.setVendor_id(objectVendorInfo.getString("vendor_id"));
                                                        } else {
                                                            vendorData.setVendor_id("");
                                                        }
                                                        if (!objectVendorInfo.isNull("vendor_name") && !objectVendorInfo.getString("vendor_name").isEmpty()) {
                                                            vendorData.setVendor_name(objectVendorInfo.getString("vendor_name"));
                                                        } else {
                                                            vendorData.setVendor_name("");
                                                        }
                                                        if (!objectVendorInfo.isNull("vendor_status") && !objectVendorInfo.getString("vendor_status").isEmpty()) {
                                                            vendorData.setVendor_status(objectVendorInfo.getString("vendor_status"));
                                                        } else {
                                                            vendorData.setVendor_status("");
                                                        }
                                                    }
                                                    mGroceryPDS.setVendorData(vendorData);

                                                    if (mGroceryProductPosition != -1 && mGroceryProductApi != null
                                                            && mGroceryProductApi.productList != null
                                                            && mGroceryProductApi.productList.size() > 0
                                                            && mMenuAndItemsAdapter != null) {

                                                        mGroceryProductApi.productList.set(mGroceryProductPosition, mGroceryPDS);
                                                        mMenuAndItemsAdapter.notifyDataSetChanged();
                                                        setLoaded();

                                                    }
                                                }


                                            }
                                        }

                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            mProgressDialog.cancel();
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            mProgressDialog.cancel();
                            mAddToCartObject = null;


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

    }

    private void getDialog(String data) {


        if (getActivity() != null) {
            android.app.AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(data);
            builder.setCancelable(true);
            builder.setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();

                }
            });
            builder.create();
            builder.show();
        }


    }

    private void delete_cart_dialog(String data) {

        if (getActivity() != null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(data);
            builder.setTitle(getActivity().getResources().getString(R.string.start_a_new_basket));
            builder.setPositiveButton(getActivity().getResources().getString(R.string.agree), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    delete_cart();
                }
            });
            builder.setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create();
            builder.show();

        }


    }

    private void delete_cart() {

        if (getActivity() != null) {
            mProgressDialog.show();
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject object = new JSONObject();
                try {

                    object.put(DefaultNames.product_cart_id, "");
                    object.put(DefaultNames.clear, "1");
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        object.put(DefaultNames.guest_status, "0");
                        object.put(DefaultNames.guest_id, "");
                    } else {
                        object.put(DefaultNames.guest_status, "1");
                        object.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    object.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    object.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    object.put(DefaultNames.day_id, AppFunctions.getDayId());
                    object.put(DefaultNames.vendor_id, mStore_ID);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());
                    retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                    String mCustomerAuthorization = "";
                    if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }

                    Call<String> call = retrofitInterface.clear_cart(mCustomerAuthorization, body);
                    call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.isSuccessful()) {
                                add_to_cart();
//                            AppFunctions.toastShort(activity,response.message());
                            }
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


    }


    private void callCartItemIncrementOrDecrementAPi(String productCartId, String operationType, TextView tvQuantity,
                                                     String count) {

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
                    if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
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
                    jsonObject.put(DefaultNames.vendor_id, mStore_ID);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<GPApiResponseCheck> Call = retrofitInterface.groceryCartItemIncrementOrDecrementApi(mCustomerAuthorization, body);
                    mProgressDialog.show();

                    Call.enqueue(new Callback<GPApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<GPApiResponseCheck> call, @NonNull Response<GPApiResponseCheck> response) {


                            if (response.isSuccessful()) {

                                GPApiResponseCheck mGpApiResponseCheck = response.body();
                                if (mGpApiResponseCheck != null) {

                                    if (mGpApiResponseCheck.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            if (tvQuantity != null) {
                                                tvQuantity.setText(count);

                                                if (mGroceryProductPosition != -1 && mGroceryProductApi != null
                                                        && mGroceryProductApi.productList != null
                                                        && mGroceryProductApi.productList.size() > 0
                                                        && mMenuAndItemsAdapter != null
                                                        && mGpApiResponseCheck.GroceryProduct != null) {

                                                    mGroceryProductApi.productList.set(mGroceryProductPosition, mGpApiResponseCheck.GroceryProduct);
                                                    mMenuAndItemsAdapter.notifyDataSetChanged();
                                                    setLoaded();

                                                }

                                            }

                                            mProgressDialog.cancel();
                                        }
                                    } else {
                                        //Api response failure :-
                                        mProgressDialog.cancel();
                                        if (getActivity() != null) {
                                            if (mGpApiResponseCheck.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mGpApiResponseCheck.error.message);
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
                        public void onFailure(@NonNull Call<GPApiResponseCheck> call, @NonNull Throwable t) {

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

    private void callCartItemDeleteAPi(String productCartId, LinearLayout layInitialAddToBasket, LinearLayout layCartProductBasket) {


        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {


                JSONObject jsonObject = new JSONObject();
                try {

                    JSONArray productCIdJsonArray = new JSONArray();

                    productCIdJsonArray.put(0, productCartId);

                    jsonObject.put(DefaultNames.product_cart_id, productCIdJsonArray);
                    //Where clear = 0 for delete the requested items from cart list.
                    // If clear = 1 then its delete the all items from cart list:-
                    jsonObject.put(DefaultNames.clear, "0");
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    String mCustomerAuthorization = "";
                    if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
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
                    jsonObject.put(DefaultNames.vendor_id, mStore_ID);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<GPApiResponseCheck> Call = retrofitInterface.groceryCartItemDeleteApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<GPApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<GPApiResponseCheck> call, @NonNull Response<GPApiResponseCheck> response) {

                            if (response.isSuccessful()) {
                                GPApiResponseCheck mGpApiResponseCheck = response.body();

                                if (mGpApiResponseCheck != null) {
                                    if (mGpApiResponseCheck.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            layInitialAddToBasket.setVisibility(View.VISIBLE);
                                            layCartProductBasket.setVisibility(View.GONE);

                                            if (mGroceryProductPosition != -1 && mGroceryProductApi != null
                                                    && mGroceryProductApi.productList != null
                                                    && mGroceryProductApi.productList.size() > 0
                                                    && mMenuAndItemsAdapter != null
                                                    && mGpApiResponseCheck.GroceryProduct != null) {

                                                mGroceryProductApi.productList.set(mGroceryProductPosition, mGpApiResponseCheck.GroceryProduct);
                                                mMenuAndItemsAdapter.notifyDataSetChanged();
                                                setLoaded();

                                            }

                                            //  Log.e("1786","called");
                                            mProgressDialog.cancel();
                                        }

                                    } else {
                                        //Api response failure :-
                                        mProgressDialog.cancel();
                                        if (getActivity() != null) {
                                            if (mGpApiResponseCheck.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mGpApiResponseCheck.error.message);
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
                        public void onFailure(@NonNull Call<GPApiResponseCheck> call, @NonNull Throwable t) {
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

    private void callStoreListAPi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {


                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put(DefaultNames.search, mCurrentSearchText);
                    jsonObject.put(DefaultNames.page, 1);
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    mPageCount = 1;
                    jsonObject.put(DefaultNames.page_per_unit, 10);
                    //Type 1 :- grocery search.
                    jsonObject.put(DefaultNames.type, "1");
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<GroceryStoresSearchApi> Call = retrofitInterface.grocerySearchApi(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<GroceryStoresSearchApi>() {
                        @Override
                        public void onResponse(@NonNull Call<GroceryStoresSearchApi> call, @NonNull Response<GroceryStoresSearchApi> response) {
                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                mGStoresListApi = response.body();
                                if (mGStoresListApi != null) {
                                    if (mGStoresListApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {

                                            if (mGStoresListApi.storeList != null && mGStoresListApi.storeList.size() > 0) {

                                                mGroceryASSBinding.recyclerGAssSearchGroceryList.setVisibility(View.VISIBLE);
                                                mGroceryASSBinding.tvGAssListEmpty.setVisibility(View.GONE);

                                                mGASParentList = new ArrayList<>();
                                                mGASParentList.add(DefaultNames.product_list); // for stores list ui.
                                                mLayoutMgrGCMPParent = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                                mGroceryASSBinding.recyclerGAssSearchGroceryList.setLayoutManager(mLayoutMgrGCMPParent);
                                                mGaspAdapter = new GASPAdapter(mGASParentList);
                                                mGroceryASSBinding.recyclerGAssSearchGroceryList.setAdapter(mGaspAdapter);

                                            } else {
                                                searchGroceryListEmpty();
                                            }
                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            searchGroceryListEmpty();
                                            if (mGStoresListApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mGStoresListApi.error.message);
                                            }
                                        }
                                    }
                                }
                            } else {
                                searchGroceryListEmpty();
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
                        public void onFailure(@NonNull Call<GroceryStoresSearchApi> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                            searchGroceryListEmpty();
                        }
                    });

                } catch (JSONException e) {
                    mProgressDialog.cancel();
                    searchGroceryListEmpty();

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

    public class GASPAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private LinearLayout mListEmptyContainer;
        private RecyclerView mListView;
        private ArrayList<Object> mGCMP_Parent_List;
        private RecyclerView.LayoutManager mCartListMgr, mCartListTotalsMgr;

        private RestaurantListLoadListener mRestaurantListLoadListener;
        private Boolean mLoading = false;
        private int totalItemCount, lastVisibleItem;
        private int visibleThreshold = 10;

        public GASPAdapter(ArrayList<Object> gCMP_Parent_List) {
            this.mGCMP_Parent_List = gCMP_Parent_List;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BottomViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.grocery_g_ass_bottom_ui, parent, false));

        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {


            BottomViewHolder mBottomVHolder = (BottomViewHolder) holder;

            if (mGStoresListApi != null && mGStoresListApi.storeList != null && mGStoresListApi.storeList.size() > 0) {

                if (mGStoresListApi.total != null && !mGStoresListApi.total.isEmpty()) {
                    mListTotals = Integer.parseInt(mGStoresListApi.total);
                    //  Log.e("mListTotals", "" + mListTotals);
                } else {
                    // Log.e("mListTotals", "empty");
                }

                mBottomVHolder.mProductListMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                mBottomVHolder.recyclerProductList.setLayoutManager(mBottomVHolder.mProductListMgr);
                mBottomVHolder.mGroceryStoresListAdapter = new GroceryStoresListAdapter(mGStoresListApi.storeList);
                mBottomVHolder.recyclerProductList.setAdapter(mBottomVHolder.mGroceryStoresListAdapter);
                setOnLoadMoreRestaurants(new RestaurantListLoadListener() {
                    @Override
                    public void loadMoreRestaurants() {

                        if (getActivity() != null) {
                            // if(mListTotals > 10){
                            if (mPageCount <= ((mListTotals / 10) + 1)) {
                                mPageCount++;
                                if (getActivity() != null) {
                                    if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                                        //**********************************************************

                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put(DefaultNames.search, mCurrentSearchText);
                                            jsonObject.put(DefaultNames.page, mPageCount);
                                            jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                                            jsonObject.put(DefaultNames.page_per_unit, 10);
                                            //Type 1 :- grocery search.
                                            jsonObject.put(DefaultNames.type, "1");
                                            if (AppFunctions.isUserLoggedIn(getActivity())) {
                                                jsonObject.put(DefaultNames.guest_status, "0");
                                                jsonObject.put(DefaultNames.guest_id, "");
                                            } else {
                                                jsonObject.put(DefaultNames.guest_status, "1");
                                                jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                                            }
                                            jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                                            jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                                            jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                                            RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                                            retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                                            Call<GroceryStoresSearchApi> Call = retrofitInterface.grocerySearchApi(body);
                                            mProgressDialog.show();
                                            Call.enqueue(new Callback<GroceryStoresSearchApi>() {
                                                @SuppressLint("NotifyDataSetChanged")
                                                @Override
                                                public void onResponse(@NonNull Call<GroceryStoresSearchApi> call, @NonNull Response<GroceryStoresSearchApi> response) {

                                                    mProgressDialog.cancel();

                                                    if (response.isSuccessful()) {
                                                        GroceryStoresSearchApi m_Store_LA_Pagination = response.body();
                                                        if (m_Store_LA_Pagination != null) {

                                                            if (m_Store_LA_Pagination.success != null) {
                                                                //Api response successDataSet :-
                                                                if (getActivity() != null) {

                                                                    if (m_Store_LA_Pagination.storeList != null) {
                                                                        if (m_Store_LA_Pagination.storeList.size() > 0) {

                                                                            //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                                                                            mGStoresListApi.storeList.addAll(m_Store_LA_Pagination.storeList);
                                                                            mBottomVHolder.mGroceryStoresListAdapter.notifyDataSetChanged();
                                                                            setLoaded();
//
                                                                            //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                //Api response failure :-
                                                                if (getActivity() != null) {
                                                                    if (m_Store_LA_Pagination.error != null) {
                                                                        AppFunctions.msgDialogOk(getActivity(), "", m_Store_LA_Pagination.error.message);
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            //Log.e("m_Store_LA_Pagination", "null");
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
                                                public void onFailure(@NonNull Call<GroceryStoresSearchApi> call, @NonNull Throwable t) {
                                                    mProgressDialog.cancel();
                                                }
                                            });
                                        } catch (JSONException e) {
                                            mProgressDialog.cancel();
                                            //Log.e("385 Excep ", e.toString());
                                            e.printStackTrace();
                                        }


                                        //**********************************************************


                                    } else {


                                        FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                        NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                                        mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                                        mFT.addToBackStack("mNetworkAnalyser");
                                        mFT.commit();


                                    }
                                }
                            }

                            //  }


                        }


                    }
                });

            }

            mBottomVHolder.mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                @Override
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    if (v.getChildAt(v.getChildCount() - 1) != null) {
                        if (scrollY > oldScrollY) {
                            if (scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) {
                                //code to fetch more data for endless scrolling
                                final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mBottomVHolder.recyclerProductList.getLayoutManager();
                                assert linearLayoutManager != null;
                                totalItemCount = linearLayoutManager.getItemCount();
                                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                                if (!mLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                    if (mRestaurantListLoadListener != null) {
                                        mRestaurantListLoadListener.loadMoreRestaurants();
                                    }
                                    mLoading = true;
                                }
                            }
                        }
                    }
                }
            });


        }

        public void setOnLoadMoreRestaurants(RestaurantListLoadListener restaurantListLoadListener) {
            this.mRestaurantListLoadListener = restaurantListLoadListener;
        }

        public void setLoaded() {
            mLoading = false;
        }


        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }


        @Override
        public int getItemCount() {
            return mGCMP_Parent_List.size();
        }


        public class BottomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView mTvTitle;
            private RecyclerView recyclerProductList;
            private RecyclerView.LayoutManager mProductListMgr;
            private GroceryStoresListAdapter mGroceryStoresListAdapter;
            private NestedScrollView mNestedScrollView;


            public BottomViewHolder(View itemView) {
                super(itemView);

                mTvTitle = itemView.findViewById(R.id.tv_gas_grocery_stores_title);
                recyclerProductList = itemView.findViewById(R.id.recycler_gas_grocery_stores_list);
                mNestedScrollView = itemView.findViewById(R.id.nested_view_gas_grocery_stores_list);


            }

            @Override
            public void onClick(View v) {

            }
        }

        private class GroceryStoresListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private ArrayList<StoresDataSet> mStoreList;

            // RecyclerView recyclerViewVendorList;


            GroceryStoresListAdapter(ArrayList<StoresDataSet> storeList/*, RecyclerView recyclerViewVendorList*/) {

                this.mStoreList = storeList;
                // this.recyclerViewVendorList = recyclerViewVendorList;

            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RestaurantViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.rc_all_store_row, parent, false));
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                RestaurantViewHolder restaurantViewHolder = (RestaurantViewHolder) holder;
                restaurantViewHolder.tv_RestaurantTitle.setText(mStoreList.get(position).name);
                AppFunctions.imageLoaderUsingGlide(mStoreList.get(position).logo, restaurantViewHolder.iv_vendorImage, getActivity());
                //Glide.with(getActivity()).load(R.drawable.x_best_offer_sample_1).into(restaurantViewHolder.iv_BestOfferImage);
                restaurantViewHolder.tv_restaurant_sub_content.setText(mStoreList.get(position).store_types);
                String mVendorStatus = mStoreList.get(position).vendor_status;
                //1 -  open
                //2 -  busy
                //0 -  closed
                if (WishListDB.getInstance(activity).getSizeOfList() > 0) {
                    if (WishListDB.getInstance(activity).isSelected(mStoreList.get(position).vendor_id)) {
                        restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                    } else {
                        restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_border_primary_color_24dp));
                    }
                } else {
                    restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_border_primary_color_24dp));
                }

                restaurantViewHolder.favorite_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (WishListDB.getInstance(activity).getSizeOfList() > 0) {
                            if (WishListDB.getInstance(activity).isSelected(mStoreList.get(position).vendor_id)) {
                                toast_layout(activity.getResources().getString(R.string.removed_from_the_wishlist), mGroceryASSBinding.wishlistLayout);
                                WishListDB.getInstance(activity).removeFromFavouriteList(mStoreList.get(position).vendor_id);
                                restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_border_primary_color_24dp));
                            } else {
                                toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), mGroceryASSBinding.wishlistLayout);
                                WishListDB.getInstance(activity).add_vendor_id(mStoreList.get(position).vendor_id);
                                restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                            }
                        } else {
                            toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), mGroceryASSBinding.wishlistLayout);
                            WishListDB.getInstance(activity).add_vendor_id(mStoreList.get(position).vendor_id);
                            restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                        }
                    }
                });

                if (mStoreList.get(position).vendorRatingDataSet != null) {
                    String mCRating = mStoreList.get(position).vendorRatingDataSet.vendor_rating_name;
                    if (mCRating != null && !mCRating.isEmpty()) {
                        restaurantViewHolder.tv_rating_statement.setText(mCRating);
                        restaurantViewHolder.rating_linear.setVisibility(View.VISIBLE);
                        AppFunctions.imageLoaderUsingGlide(mStoreList.get(position).vendorRatingDataSet.vendor_rating_image,
                                restaurantViewHolder.rating_img, activity);
                    } else {
                        restaurantViewHolder.tv_rating_statement.setText("0");
                        restaurantViewHolder.rating_linear.setVisibility(View.GONE);
                    }
                } else {
                    restaurantViewHolder.tv_rating_statement.setText("0");
                    restaurantViewHolder.rating_linear.setVisibility(View.GONE);
                }

                if (OrderTypeDB.getInstance(activity).getUserServiceType().equals("2")) {
                    restaurantViewHolder.mDeliveryAmtContainer.setVisibility(View.GONE);
                    restaurantViewHolder.mPickupContainer.setVisibility(View.VISIBLE);
                    String mDTime = mStoreList.get(position).delivery_time
                            + " " + activity.getResources().getString(R.string.mins);
                    restaurantViewHolder.mPickupTime.setText(mDTime);
                } else {
                    restaurantViewHolder.mDeliveryAmtContainer.setVisibility(View.VISIBLE);
                    restaurantViewHolder.mPickupContainer.setVisibility(View.GONE);
                    String mDTime = mStoreList.get(position).delivery_time
                            + " " + activity.getResources().getString(R.string.mins);
                    restaurantViewHolder.tv_DeliveryTime.setText(mDTime);
                }

                if (getActivity() != null) {
                    if (mVendorStatus.equals("0")) {
                        //closed :-
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.VISIBLE);
                        restaurantViewHolder.tv_ImageOverStatus.setText(getActivity().getResources().getString(R.string.closed));
                    } else if (mVendorStatus.equals("2")) {
                        //Busy :-
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.VISIBLE);
                        restaurantViewHolder.tv_ImageOverStatus.setText(getActivity().getResources().getString(R.string.busy));
                    } else {
                        //Open :-
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.GONE);
                        restaurantViewHolder.tv_ImageOverStatus.setText("");
                    }
                }

                if (mStoreList.get(position).free_delivery.equals("1")) {
                    restaurantViewHolder.tv_delivery_amount_title.setVisibility(View.VISIBLE);
                    restaurantViewHolder.tv_delivery_amount_title.setText(activity.getResources().getString(R.string.ar_filter_do_free_delivery));
                    restaurantViewHolder.tv_delivery_amount.setVisibility(View.GONE);
                } else {
                    restaurantViewHolder.tv_delivery_amount.setVisibility(View.VISIBLE);
                    restaurantViewHolder.tv_delivery_amount.setText(mStoreList.get(position).delivery_charge);
                    restaurantViewHolder.tv_delivery_amount_title.setVisibility(View.GONE);
                }

                restaurantViewHolder.tv_ar_minimum_amount.setText(activity.getResources().getString(R.string.min) + " - " +
                        mStoreList.get(position).minimum_amount);

                String m_OfferData = mStoreList.get(position).offer;
                if (m_OfferData != null && !m_OfferData.isEmpty()) {
                    restaurantViewHolder.mLayVLOfferContainer.setVisibility(View.VISIBLE);
                    restaurantViewHolder.empty.setVisibility(View.VISIBLE);
//                    restaurantViewHolder.iv_Dot.setVisibility(View.VISIBLE);
                    restaurantViewHolder.tv_offerContent.setText(m_OfferData);
                } else {
                    restaurantViewHolder.mLayVLOfferContainer.setVisibility(View.GONE);
                    restaurantViewHolder.empty.setVisibility(View.GONE);
//                    restaurantViewHolder.iv_Dot.setVisibility(View.GONE);
                    restaurantViewHolder.tv_offerContent.setText("");
                }
                // restaurantViewHolder.mRestaurantStatusContainer.setVisibility(View.GONE);

            }

            @Override
            public int getItemCount() {
                return mStoreList.size();
            }


            class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                ImageView favorite_icon, rating_img;
                ShapeableImageView iv_vendorImage;
                TextView tv_RestaurantTitle, tv_DeliveryTime, tv_restaurant_sub_content, mPickupTime;
                TextView tv_rating_statement,
                        tv_delivery_amount_title, tv_delivery_amount, tv_offerContent, tv_ImageOverStatus,tv_ar_minimum_amount;
                LinearLayout mDeliveryAmtContainer, mDeliveryTimeContainer, mPickupContainer,
                        mLayImageOverStatus, mLayVLOfferContainer, rating_linear, empty;

                RestaurantViewHolder(View itemView) {
                    super(itemView);
                    itemView.setOnClickListener(this);
                    iv_vendorImage = itemView.findViewById(R.id.iv_ar_restaurant_image);
                    tv_RestaurantTitle = itemView.findViewById(R.id.tv_ar_restaurant_title);
                    tv_restaurant_sub_content = itemView.findViewById(R.id.tv_ar_restaurant_sub_title);
                    tv_rating_statement = itemView.findViewById(R.id.tv_ar_rating_msg);
                    rating_linear = itemView.findViewById(R.id.rating_linear);
                    rating_img = itemView.findViewById(R.id.rating_img);
                    mPickupContainer = itemView.findViewById(R.id.pickup_container);
                    mPickupTime = itemView.findViewById(R.id.tv_ar_pickup_time);
                    mDeliveryAmtContainer = itemView.findViewById(R.id.delivery_container);
                    tv_delivery_amount_title = itemView.findViewById(R.id.tv_ar_delivery_amt_title);
                    tv_delivery_amount = itemView.findViewById(R.id.tv_ar_delivery_amt_data);
                    tv_ar_minimum_amount = itemView.findViewById(R.id.tv_ar_minimum_amount);
                    mDeliveryTimeContainer = itemView.findViewById(R.id.lay_ar_restaurant_delivery_time_container);
                    tv_DeliveryTime = itemView.findViewById(R.id.tv_ar_delivery_time);
                    tv_offerContent = itemView.findViewById(R.id.tv_ar_restaurant_offers);
                    mLayImageOverStatus = itemView.findViewById(R.id.lay_ar_restaurant_image);
                    tv_ImageOverStatus = itemView.findViewById(R.id.tv_ar_restaurant_image_over_status);
                    mLayVLOfferContainer = itemView.findViewById(R.id.offer_linear);
                    favorite_icon = itemView.findViewById(R.id.favorite_icon);
                    empty = itemView.findViewById(R.id.empty);

                }

                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() != -1) {
                        if (mStoreList.get(getAdapterPosition()).vendor_status.equals("0")) {
                            toDialogClosedVendor(mStoreList.get(getAdapterPosition()).name, mStoreList.get(getAdapterPosition()).vendor_id,
                                    "0");
                        } else if (mStoreList.get(getAdapterPosition()).vendor_status.equals("2")) {
                            toDialogBusyVendor(mStoreList.get(getAdapterPosition()).name, mStoreList.get(getAdapterPosition()).vendor_id,
                                    "0");
                        } else {
                            toStoreListing(mStoreList.get(getAdapterPosition()).vendor_id, mStoreList.get(getAdapterPosition()).name);
                        }
                    }
                }
            }

            public class MenuListEmptyViewHolder extends RecyclerView.ViewHolder {
                LinearLayout menuListEmptyUI;

                MenuListEmptyViewHolder(View itemView) {
                    super(itemView);
                    menuListEmptyUI = itemView.findViewById(R.id.layout_restaurant_menu_item_list_empty_message);
                }
            }

            public void toDialogClosedVendor(String name, String vendor_id, String product_id) {
                String msg = activity.getResources().getString(R.string.restaurant_closed_msg_1) + " " + name + " " + activity.getResources().getString(R.string.restaurant_closed_msg_2);
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
                builder.setTitle(activity.getResources().getString(R.string.restaurant_closed));
                builder.setMessage(msg);
                builder.setPositiveButton(activity.getResources().getString(R.string.co_s_continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toStoreListing(vendor_id, product_id);
                    }
                });
                builder.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }

            public void toDialogBusyVendor(String name, String vendor_id, String product_id) {
                String msg = activity.getResources().getString(R.string.restaurant_closed_msg_1) + " " + name + " " + activity.getResources().getString(R.string.restaurant_busy_msg_2);
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(activity);
                builder.setTitle(activity.getResources().getString(R.string.restaurant_busy));
                builder.setMessage(msg);
                builder.setPositiveButton(activity.getResources().getString(R.string.co_s_continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toStoreListing(vendor_id, product_id);
                    }
                });
                builder.setNegativeButton(activity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }

            public void toStoreListing(String vendor_id, String vendor_name) {
                if (getActivity() != null) {
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    GroceryCategoryMainPage m_groceryCategoryMainPage = new GroceryCategoryMainPage();
                    Bundle mBundle = new Bundle();
                    mBundle.putString(DefaultNames.store_id, vendor_id);
                    mBundle.putString(DefaultNames.store_name, vendor_name);
                    m_groceryCategoryMainPage.setArguments(mBundle);
                    mFT.replace(R.id.layout_app_home_body, m_groceryCategoryMainPage, "m_groceryCategoryMainPage");
                    mFT.addToBackStack("m_groceryCategoryMainPage");
                    mFT.commit();
                }
            }
        }


    }

    private void toast_layout(String string, final LinearLayout wishlistLayout) {

        wishlistLayout.setVisibility(View.VISIBLE);
        mGroceryASSBinding.dataText.setText(string);
        mGroceryASSBinding.viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                FavouriteListFragment favouriteListFragment = new FavouriteListFragment();
                Bundle mBundle = new Bundle();
                favouriteListFragment.setArguments(mBundle);
                mFT.replace(R.id.layout_app_home_body, favouriteListFragment, "favouriteListFragment");
                mFT.addToBackStack("m_AllRestaurants");
                mFT.commit();
            }
        });

        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            public void run() {
                wishlistLayout.setVisibility(View.GONE);
            }
        };
        handler.postDelayed(runnable, 3000);
    }

}

