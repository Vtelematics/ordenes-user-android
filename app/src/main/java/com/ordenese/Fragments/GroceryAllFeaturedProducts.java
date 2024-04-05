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
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import com.ordenese.DataSets.VendorDataDataSet;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.ProductListLoadListener;
import com.ordenese.R;
import com.ordenese.databinding.GroceryAllFeaturedProductsBinding;

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


public class GroceryAllFeaturedProducts extends Fragment implements View.OnClickListener {

    private ProgressDialog mProgressDialog;
    private GroceryAllFeaturedProductsBinding mGroceryAFPBinding;
    private RecyclerView.LayoutManager mProductListMgr;
    private ProductListAdapter mProductListAdapter;
    private String mIsFrom = "", mStore_ID = "", mStore_NAME = "", mStore_STATUS = "";
    private MenuAndItemsAdapter mMenuAndItemsAdapter;

    private RetrofitInterface retrofitInterface;
    private GroceryProductApi mGroceryProductApi;
    private RecyclerView.LayoutManager mSearchPListLayMgr;

    private int mPageCount = 1;
    private ProductListLoadListener mProductListLoadListener;
    private Boolean mLoading = false;
    private int mListTotals = 0;
    private int totalItemCount, lastVisibleItem;
    private int visibleThreshold = 20;

    private LinearLayout mGlobal_CVInitialAddToCart, mGlobal_CVQtyAddToCart;
    private ImageButton mGlobal_CartCountDelete, mGlobal_CartCountRemove;
    private String mCurrentQty = "", mCurrentProductId = "", mCurrentProductName = "", mCurrentVendorLatitude = "", mCurrentVendorLongitude = "",
            mCurrentCategoryId = "", mCurrentSub_category_id = "";
    private JSONObject mAddToCartObject;

    private String mCurrentSearchText = "";
    private long mSearchDelay = 1000; // 1 seconds after user stops typing
    private long mSearchLastTextEdit = 0;
    private Handler mSearchHandler = new Handler();
    private Runnable mSearchInputFinishChecker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (mSearchLastTextEdit + mSearchDelay - 500)) {
                if (mCurrentSearchText.length() >= 1) {
                    callSearchItemsAPi(mCurrentSearchText);
                }
            }
        }
    };

    private int mGroceryProductPosition = -1;


    public GroceryAllFeaturedProducts() {
        // Required empty public constructor
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //  return inflater.inflate(R.layout.grocery_all_featured_products, container, false);
        mGroceryAFPBinding = GroceryAllFeaturedProductsBinding.inflate(inflater, container, false);


        if (getArguments() != null) {
            mIsFrom = getArguments().getString(DefaultNames.from);
            mStore_ID = getArguments().getString(DefaultNames.store_id);
            // mStore_NAME = getArguments().getString(DefaultNames.store_name);
            //  mStore_STATUS = getArguments().getString(DefaultNames.store_status);

            if (mIsFrom != null) {
                if (mIsFrom.equals(DefaultNames.callFromGroceryCategory)) {
                    mGroceryAFPBinding.recyclerGAfpProductList.setVisibility(View.GONE);
                } else if (mIsFrom.equals(DefaultNames.callFromFeatureProducts)) {
                    mGroceryAFPBinding.recyclerGAfpProductList.setVisibility(View.VISIBLE);
                }

            }

        }

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mGroceryAFPBinding.imgGAfpBack.setOnClickListener(this);

        mGroceryAFPBinding.layGAfpProductSearchClear.setVisibility(View.INVISIBLE);
        mGroceryAFPBinding.layGAfpProductSearchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGroceryAFPBinding.edtGAfpProductSearch.setText("");
            }
        });
        mGroceryAFPBinding.edtGAfpProductSearch.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if (getActivity() != null) {

                    if (s.length() == 0) {
                        mGroceryAFPBinding.layGAfpProductSearchClear.setVisibility(View.INVISIBLE);
                        mGroceryAFPBinding.tvGAfpListEmpty.setVisibility(View.GONE);
                    } else {
                        mGroceryAFPBinding.layGAfpProductSearchClear.setVisibility(View.VISIBLE);
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

        /*ArrayList<String> categoryList = new ArrayList<>();
        categoryList.add("Imported for you");
        categoryList.add("Summer Selection");
        categoryList.add("Promo Packs");
        categoryList.add("Every day Roastery Coffee");

        categoryList.add("Imported for you");
        categoryList.add("Summer Selection");
        categoryList.add("Promo Packs");
        categoryList.add("Every day Roastery Coffee");

        categoryList.add("Imported for you");
        categoryList.add("Summer Selection");
        categoryList.add("Promo Packs");
        categoryList.add("Every day Roastery Coffee");

        categoryList.add("Imported for you");
        categoryList.add("Summer Selection");
        categoryList.add("Promo Packs");
        categoryList.add("Every day Roastery Coffee");

        if (categoryList != null && categoryList.size() > 0) {

            mProductListMgr = new GridLayoutManager(getActivity(), 2);
            mGroceryAFPBinding.recyclerGAfpProductList.setLayoutManager(mProductListMgr);
            mProductListAdapter = new ProductListAdapter(categoryList, false);
            mGroceryAFPBinding.recyclerGAfpProductList.setAdapter(mProductListAdapter);

        }*/

       /* ArrayList<MenuAndItemsDataSet> menuAndItemsList = new ArrayList<>();
        MenuAndItemsDataSet mMenuAndChildItemsList;

        mMenuAndChildItemsList = new MenuAndItemsDataSet();
        mMenuAndChildItemsList.setChildName("Product 1");
        mMenuAndChildItemsList.setPrice_status("1");
        mMenuAndChildItemsList.setChildImage("http://ordenese.foodesoft.com/images/product/picture/1657889559.png");
        mMenuAndChildItemsList.setChildPrice("$ 100");
        mMenuAndChildItemsList.setChildOfferPrice("$ 50");
        menuAndItemsList.add(mMenuAndChildItemsList);

        mMenuAndChildItemsList = new MenuAndItemsDataSet();
        mMenuAndChildItemsList.setChildName("Product 2");
        mMenuAndChildItemsList.setPrice_status("1");
        mMenuAndChildItemsList.setChildImage("http://ordenese.foodesoft.com/images/product/picture/1657889559.png");
        mMenuAndChildItemsList.setChildPrice("$ 100");
        mMenuAndChildItemsList.setChildOfferPrice("$ 50");
        menuAndItemsList.add(mMenuAndChildItemsList);

        mMenuAndChildItemsList = new MenuAndItemsDataSet();
        mMenuAndChildItemsList.setChildName("Product 3");
        mMenuAndChildItemsList.setPrice_status("1");
        mMenuAndChildItemsList.setChildImage("http://ordenese.foodesoft.com/images/product/picture/1657889559.png");
        mMenuAndChildItemsList.setChildPrice("$ 100");
        mMenuAndChildItemsList.setChildOfferPrice("$ 50");
        menuAndItemsList.add(mMenuAndChildItemsList);

        mMenuAndChildItemsList = new MenuAndItemsDataSet();
        mMenuAndChildItemsList.setChildName("Product 4");
        mMenuAndChildItemsList.setPrice_status("1");
        mMenuAndChildItemsList.setChildImage("http://ordenese.foodesoft.com/images/product/picture/1657889559.png");
        mMenuAndChildItemsList.setChildPrice("$ 100");
        mMenuAndChildItemsList.setChildOfferPrice("$ 50");
        menuAndItemsList.add(mMenuAndChildItemsList);

        mMenuAndChildItemsList = new MenuAndItemsDataSet();
        mMenuAndChildItemsList.setChildName("Product 5");
        mMenuAndChildItemsList.setPrice_status("1");
        mMenuAndChildItemsList.setChildImage("http://ordenese.foodesoft.com/images/product/picture/1657889559.png");
        mMenuAndChildItemsList.setChildPrice("$ 100");
        mMenuAndChildItemsList.setChildOfferPrice("$ 50");
        menuAndItemsList.add(mMenuAndChildItemsList);*/


        if (mIsFrom != null) {
            if (mIsFrom.equals(DefaultNames.callFromGroceryCategory)) {
                mGroceryAFPBinding.recyclerGAfpProductList.setVisibility(View.GONE);
                mGroceryAFPBinding.recyclerGAfpSearchProductList.setVisibility(View.VISIBLE);
            } else if (mIsFrom.equals(DefaultNames.callFromFeatureProducts)) {
                mGroceryAFPBinding.recyclerGAfpProductList.setVisibility(View.VISIBLE);
                mGroceryAFPBinding.recyclerGAfpSearchProductList.setVisibility(View.GONE);
            }

        }

        return mGroceryAFPBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();
        if (mId == R.id.img_g_afp_back) {

            if (getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null) {

        }


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void callSearchItemsAPi(String searchText) {

        if (getActivity() != null) {
            toHideDeviceKeyboard();
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {


                    jsonObject.put(DefaultNames.vendor_id, mStore_ID);
                    jsonObject.put(DefaultNames.category_id, "");
                    jsonObject.put(DefaultNames.sub_category_id, "");
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    mPageCount = 1;
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

                    jsonObject.put(DefaultNames.search, searchText);
                    //type -1 => only one vendor product are listed.
                    //type - 2 => All vendor product are listed here.
                    jsonObject.put(DefaultNames.type, "1");
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

                                                    mStore_NAME = mGroceryProductApi.vendorData.vendor_name;
                                                    mStore_STATUS = mGroceryProductApi.vendorData.vendor_status;

                                                    if (mGroceryProductApi.total != null && !mGroceryProductApi.total.isEmpty()) {
                                                        mListTotals = Integer.parseInt(mGroceryProductApi.total);
                                                        //  Log.e("mListTotals", "" + mListTotals);
                                                    } else {
                                                        // Log.e("mListTotals", "empty");
                                                    }

                                                    mGroceryAFPBinding.recyclerGAfpSearchProductList.setVisibility(View.VISIBLE);
                                                    mGroceryAFPBinding.tvGAfpListEmpty.setVisibility(View.GONE);
                                                    mSearchPListLayMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                                    mGroceryAFPBinding.recyclerGAfpSearchProductList.setLayoutManager(mSearchPListLayMgr);
                                                    mMenuAndItemsAdapter = new MenuAndItemsAdapter(getActivity(), mGroceryProductApi.productList);
                                                    mGroceryAFPBinding.recyclerGAfpSearchProductList.setAdapter(mMenuAndItemsAdapter);
                                                    setOnLoadMoreProducts(new ProductListLoadListener() {
                                                        @Override
                                                        public void loadMoreProducts() {

                                                            if (getActivity() != null) {
                                                                // if(mListTotals > 10){
                                                                if (mPageCount <= ((mListTotals / 20) + 1)) {
                                                                    mPageCount++;
                                                                    if (getActivity() != null) {
                                                                        if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                                                                            //**********************************************************

                                                                            JSONObject jsonObject = new JSONObject();
                                                                            try {

                                                                                jsonObject.put(DefaultNames.vendor_id, mStore_ID);
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

                                                                                jsonObject.put(DefaultNames.search, searchText);
                                                                                //type -1 => only one vendor product are listed.
                                                                                //type - 2 => All vendor product are listed here.
                                                                                jsonObject.put(DefaultNames.type, "1");
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

                                                                                                                mStore_NAME = m_Store_P_Pagination.vendorData.vendor_name;
                                                                                                                mStore_STATUS = m_Store_P_Pagination.vendorData.vendor_status;

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


                                                    mGroceryAFPBinding.nestedViewGAfp.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                                                        @Override
                                                        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                                                            if (v.getChildAt(v.getChildCount() - 1) != null) {
                                                                if (scrollY > oldScrollY) {
                                                                    if (scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) {
                                                                        //code to fetch more data for endless scrolling
                                                                        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mGroceryAFPBinding.recyclerGAfpSearchProductList.getLayoutManager();
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

        mGroceryAFPBinding.recyclerGAfpSearchProductList.setVisibility(View.GONE);
        mGroceryAFPBinding.tvGAfpListEmpty.setVisibility(View.VISIBLE);

    }

    private void toHideDeviceKeyboard() {
        if (getActivity() != null) {
            if (mGroceryAFPBinding != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                // To get the correct window token, lets first get the currently focused view
                View v__iew = mGroceryAFPBinding.getRoot();
                // To get the window token when there is no currently focused view, we have a to create a view
                if (v__iew == null) {
                    v__iew = new View(getActivity());
                }
                // hide the keyboard
                imm.hideSoftInputFromWindow(v__iew.getWindowToken(), 0);
            }
        }
    }


    public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

        private ArrayList<String> m_Category_List;
        private LinearLayout mListEmptyContainer;
        private RecyclerView mListView;
        private Boolean mIsGreaterThan_11_items;


        public ProductListAdapter(ArrayList<String> category_List, Boolean isGreaterThan_11_items) {
            this.m_Category_List = category_List;
            this.mIsGreaterThan_11_items = isGreaterThan_11_items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.gafp_product_list_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            //  ////Log.e("CartProductListAdapter onBindViewHolder","");

            holder.mCategoryName.setText(m_Category_List.get(position));
            //AppFunctions.imageLoaderUsingGlide(m_Cart_List.get(position).getImage(), holder.mVendorImg, getActivity());


            if (position == 2 || position == 4) {
                holder.mPrice.setVisibility(View.VISIBLE);
                holder.mSpecialPrice.setText("$ 20");
                holder.mPrice.setText("$ 50");
                holder.mPrice.setPaintFlags(holder.mPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.mSpecialPrice.setText("$ 20");
                holder.mPrice.setVisibility(View.GONE);
            }


            if (getActivity() != null) {
                Glide.with(getActivity()).load(R.drawable.x_fruit_category).into(holder.mCategoryImg);
            }

            holder.mLayRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AppFunctions.toastShort(getActivity(), m_Category_List.get(position));

                }
            });

                /*if (position == m_Cart_List.size() - 1) {
                    //To visible the horizontal line only when after the last item of cart list :-
                    holder.mViewLine.setVisibility(View.VISIBLE);
                } else {
                    holder.mViewLine.setVisibility(View.GONE);
                }*/


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

            return m_Category_List.size();

        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


            private TextView mCategoryName, mSpecialPrice, mPrice;
            private ImageView mCategoryImg;
            private LinearLayout mLayRow;

            public ViewHolder(View itemView) {
                super(itemView);

                mSpecialPrice = itemView.findViewById(R.id.tv_gaf_pl_special_price);
                mPrice = itemView.findViewById(R.id.tv_gaf_pl_price);

                mCategoryName = itemView.findViewById(R.id.tv_gaf_pl_name);
                mCategoryImg = itemView.findViewById(R.id.iv_gaf_pl_image);
                mLayRow = itemView.findViewById(R.id.lay_gaf_pl_row);


            }

            @Override
            public void onClick(View v) {

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

        public void updateMenuAndItem(ArrayList<GroceryProductDataSet> groceryProductList) {
            this.mGroceryProductList = groceryProductList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
           /* if (viewType == 2) {
                return new MenuViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.grocery_search_product_row, parent, false));
            } else {
                return new MenuListEmptyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.store_menu_list_empty, parent, false));
            }*/
            return new MenuViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.grocery_search_product_row, parent, false));
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


            menuViewHolder.mChildMenuItemsContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mContext != null) {


                        FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                        GroceryProductDetails groceryProductDetails = new GroceryProductDetails();
                        Bundle mBundle = new Bundle();
                        ////mStore_NAME,mStore_STATUS,mStore_ID
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

                    mGlobal_CVInitialAddToCart = menuViewHolder.mLayInitialAddToBasket;
                    mGlobal_CVQtyAddToCart = menuViewHolder.mLayCartProductBasket;
                    mGlobal_CartCountRemove = menuViewHolder.mImgBtnQtyRemove;

                    mCurrentQty = "1";
                    mCurrentProductId = mGroceryProductList.get(position).product_item_id;
                    mCurrentProductName = mGroceryProductList.get(position).item_name;

                    if (mStore_STATUS != null && (mStore_STATUS.equals("0") || mStore_STATUS.equals("2"))) {
                        //Currently vendor closed or busy so to show message to user
                        //and perform add to cart :-
                        mGroceryProductPosition = position;
                        toPerformAddToCart();
                    } else {
                        //here restaurant is open or busy :-
                        mGroceryProductPosition = position;
                        add_to_cart();
                    }

                }
            });

            menuViewHolder.mImgBtnQtyAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String productCartId = mGroceryProductList.get(position).cart_id;

                    String t = menuViewHolder.mTvCartProductQty.getText().toString();

                    int mCount = Integer.valueOf(t);
                    mCount++;

                    //type = 0 (decrement) | type = 1 (increment) :-
                    mGroceryProductPosition = position;
                    callCartItemIncrementOrDecrementAPi(productCartId, "1", menuViewHolder.mTvCartProductQty, String.valueOf(mCount));


                }
            });

            menuViewHolder.mImgBtnQtyRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String productCartId = mGroceryProductList.get(position).cart_id;

                    String t = menuViewHolder.mTvCartProductQty.getText().toString();
                    int mCount = Integer.valueOf(t);

                    if (mCount != 1) {
                        mCount--;
                        //type = 0 (decrement) | type = 1 (increment) :-
                        mGroceryProductPosition = position;
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

            public MenuViewHolder(View itemView) {
                super(itemView);

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
                                                // get_productCartQtyUpdate();
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

                                                //  get_productCartQtyUpdate();
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
                                            // get_productCartQtyUpdate();

                                            if (mGroceryProductPosition != -1 && mGroceryProductApi != null
                                                    && mGroceryProductApi.productList != null
                                                    && mGroceryProductApi.productList.size() > 0
                                                    && mMenuAndItemsAdapter != null
                                                    && mGpApiResponseCheck.GroceryProduct != null) {

                                                mGroceryProductApi.productList.set(mGroceryProductPosition, mGpApiResponseCheck.GroceryProduct);
                                                mMenuAndItemsAdapter.notifyDataSetChanged();
                                                setLoaded();

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


}