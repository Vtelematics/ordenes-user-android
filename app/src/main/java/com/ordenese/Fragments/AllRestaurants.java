package com.ordenese.Fragments;

import static android.os.Looper.getMainLooper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.ordenese.ApiConnection.ApiClient;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.HomeBannerScroller;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.CuisinesApi;
import com.ordenese.DataSets.CuisinesDataSet;
import com.ordenese.DataSets.FilterDataSet;
import com.ordenese.DataSets.FilterDbDataSet;
import com.ordenese.DataSets.FilterListApi;
import com.ordenese.DataSets.FilterListDataSet;
import com.ordenese.DataSets.FilterTypeDataSet;
import com.ordenese.DataSets.VendorBannerDataSet;
import com.ordenese.DataSets.VendorDataSet;
import com.ordenese.DataSets.VendorListingApi;
import com.ordenese.DataSets.VendorListingApiPagination;
import com.ordenese.Databases.ARCuisinesDB;
import com.ordenese.Databases.ARCuisinesDBTemp;
import com.ordenese.Databases.ARFiltersDB;
import com.ordenese.Databases.ARFiltersDBTemp;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.CFCuisinesDB;
import com.ordenese.Databases.CFFiltersDB;
import com.ordenese.Databases.DeliveryLocationSearchDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Databases.WishListDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.Interfaces.CuisinesApplyCall;
import com.ordenese.Interfaces.FiltersApplyCall;
import com.ordenese.Interfaces.RestaurantListLoadListener;
import com.ordenese.R;
import com.ordenese.databinding.AllRestaurantsBinding;
import com.ordenese.databinding.CuisinesListBottomSheetBinding;
import com.ordenese.databinding.FilterListBottomSheetBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllRestaurants extends Fragment implements View.OnClickListener, CuisinesApplyCall, FiltersApplyCall {

    AllRestaurantsBinding mAllRBinding;

    private RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;
    private RecyclerView.LayoutManager mRestaurantARParentLayoutMgr;
    private ARParentListAdapter mArParentListAdapter;
    private CuisinesApi mCuisinesApi;
    private CuisinesApplyCall mCuisinesApplyCall;
    Activity activity;
    private int mPageCount = 1;
    int mListTotals = 0;
    private ArrayList<CuisinesDataSet> mCuisineIdsGlobal;

    String mFreeDelivery = "0", mBusiness_type_id = "";
    CartInfo cartInfo;

    private FilterListApi mFilterListApi;
    private FiltersApplyCall mFiltersApplyCall;


    public AllRestaurants() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFreeDelivery = getArguments().getString("top_pick_id");
            mBusiness_type_id = getArguments().getString("business_type_id");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(AppLanguageSupport.onAttach(context));
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setLayoutDirection(
                    "ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(getActivity())) ?
                            View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.all_restaurants, container, false);

        mAllRBinding = AllRestaurantsBinding.inflate(inflater, container, false);

        activity = getActivity();

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mCuisinesApplyCall = this;
        mFiltersApplyCall = this;

        cartInfo = (CartInfo) getActivity();

        mAllRBinding.tvAllRLocation.setOnClickListener(this);
        mAllRBinding.layAllRFilters.setOnClickListener(this);
        mAllRBinding.layAllRCuisines.setOnClickListener(this);
        mAllRBinding.layAllRSearch.setOnClickListener(this);
        mAllRBinding.layArFilterOrCuisinesTransparentBg.setOnClickListener(this);

        mAllRBinding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        mAllRBinding.cartBtn.setVisibility(View.GONE);
        mAllRBinding.cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                CartList m_cartList = new CartList();
                mFT.replace(R.id.layout_app_home_body, m_cartList, "m_cartList");
                mFT.addToBackStack("m_cartList");
                mFT.commit();
            }
        });

        mAllRBinding.wishlist.setOnClickListener(new View.OnClickListener() {
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

        mAllRBinding.deliveryTv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {
                mAllRBinding.deliveryTv.setBackground(activity.getResources().getDrawable(R.drawable.bg_ar_filter_apply));
                mAllRBinding.deliveryTv.setTextColor(activity.getResources().getColor(R.color.white));
                mAllRBinding.pickupTv.setBackground(null);
                mAllRBinding.pickupTv.setTextColor(activity.getResources().getColor(R.color.black));

                if (!OrderTypeDB.getInstance(getActivity()).getUserServiceType().isEmpty()) {
                    OrderTypeDB.getInstance(getActivity()).updateUserServiceType("1");
                } else {
                    OrderTypeDB.getInstance(getActivity()).addUserServiceType("1");
                }
                callVendorListAPi();
            }
        });
        mAllRBinding.pickupTv.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {

                mAllRBinding.pickupTv.setBackground(activity.getResources().getDrawable(R.drawable.bg_ar_filter_apply));
                mAllRBinding.pickupTv.setTextColor(activity.getResources().getColor(R.color.white));
                mAllRBinding.deliveryTv.setBackground(null);
                mAllRBinding.deliveryTv.setTextColor(activity.getResources().getColor(R.color.black));

                if (!OrderTypeDB.getInstance(getActivity()).getUserServiceType().isEmpty()) {
                    OrderTypeDB.getInstance(getActivity()).updateUserServiceType("2");
                } else {
                    OrderTypeDB.getInstance(getActivity()).addUserServiceType("2");
                }
                callVendorListAPi();
            }
        });

        return mAllRBinding.getRoot();

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onResume() {
        super.onResume();

        if (OrderTypeDB.getInstance(getActivity()).getUserServiceType().equals("2")) {
            mAllRBinding.pickupTv.setBackground(activity.getResources().getDrawable(R.drawable.bg_ar_filter_apply));
            mAllRBinding.pickupTv.setTextColor(activity.getResources().getColor(R.color.white));
            mAllRBinding.deliveryTv.setBackground(null);
            mAllRBinding.deliveryTv.setTextColor(activity.getResources().getColor(R.color.black));
        } else {
            mAllRBinding.deliveryTv.setBackground(activity.getResources().getDrawable(R.drawable.bg_ar_filter_apply));
            mAllRBinding.deliveryTv.setTextColor(activity.getResources().getColor(R.color.white));
            mAllRBinding.pickupTv.setBackground(null);
            mAllRBinding.pickupTv.setTextColor(activity.getResources().getColor(R.color.black));
        }

        if (getActivity() != null) {
            mCuisineIdsGlobal = ARCuisinesDB.getInstance(getActivity()).getCuisineList();
            callVendorListAPi();
            if (AreaGeoCodeDB.getInstance(getActivity()).isAreaGeoCodeSelected()) {
                mAllRBinding.tvAllRLocation.setText(AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode().getmAddress());
                mAllRBinding.tvAllRLocation.setSelected(true);
            } else {
                mAllRBinding.tvAllRLocation.setText(getActivity().getResources().getString(R.string.delivering_to));
            }
        }

        cartInfo.cart_info(false, "", "");
        cart_product_count();
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
                                        mAllRBinding.cartBtn.setVisibility(View.VISIBLE);
                                        mAllRBinding.cartCount.setText(object.getString("qty_count"));
                                    } else {
                                        mAllRBinding.cartBtn.setVisibility(View.GONE);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }
                    });
                } catch (Exception e) {

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


    @Override
    public void onStop() {
        super.onStop();

        if (mArParentListAdapter != null) {
            mArParentListAdapter.stopThread();
        }

    }

    private void callVendorListAPi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                if (mFreeDelivery == null) {
                    mFreeDelivery = "0";
                }
                if (mBusiness_type_id == null) {
                    mBusiness_type_id = "";
                }

                JSONObject jsonObject = new JSONObject();
                try {

                    AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();

                    jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                    jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.free_delivery, mFreeDelivery);
                    jsonObject.put(DefaultNames.business_type, mBusiness_type_id);
                    jsonObject.put(DefaultNames.page, 1);
                    mPageCount = 1;
                    jsonObject.put(DefaultNames.page_per_unit, 10);
                    JSONArray cuisineJsonArray = new JSONArray();

                    if (mCuisineIdsGlobal != null && mCuisineIdsGlobal.size() > 0) {
                        for (int cuisineId = 0; cuisineId < mCuisineIdsGlobal.size(); cuisineId++) {
                            cuisineJsonArray.put(cuisineId, mCuisineIdsGlobal.get(cuisineId).getCuisine_id());
                        }
                    }
                    jsonObject.put(DefaultNames.cuisine, cuisineJsonArray);

                    ArrayList<FilterDbDataSet> mFilterDbList = ARFiltersDB.getInstance(getActivity()).getFiltersList();
                    JSONArray filtersJsonArray = new JSONArray();
                    if (mFilterDbList != null && mFilterDbList.size() > 0) {
                        ArrayList<String> mFilterIdList = getFiltersIdList(mFilterDbList);
                        for (int filter = 0; filter < mFilterIdList.size(); filter++) {

                            JSONObject filterObject = new JSONObject();
                            filterObject.put(DefaultNames.filter_id, mFilterIdList.get(filter));
                            JSONArray filtersValueArray = new JSONArray();

                            for (int filterTId = 0; filterTId < mFilterDbList.size(); filterTId++) {
                                //To compare sorted filterId list with filterDb filterId list to
                                //get Corresponding filter type ids:-
                                if (mFilterDbList.get(filterTId).getmFilterId().equals(mFilterIdList.get(filter))) {
                                    filtersValueArray.put(mFilterDbList.get(filterTId).getmFilterTypeId());
                                }
                            }
                            filterObject.put(DefaultNames.filter_value, filtersValueArray);
                            filtersJsonArray.put(filter, filterObject);
                        }
                    }
                    jsonObject.put(DefaultNames.side_filter, filtersJsonArray);

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<VendorListingApi> Call = retrofitInterface.AllRestaurantsList(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<VendorListingApi>() {
                        @Override
                        public void onResponse(@NonNull Call<VendorListingApi> call, @NonNull Response<VendorListingApi> response) {
                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                VendorListingApi mVendorListingApi = response.body();
                                if (mVendorListingApi != null) {
                                    if (mVendorListingApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            //mListTotals
                                            mRestaurantARParentLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                            mAllRBinding.recyclerAllRestaurantParent.setLayoutManager(mRestaurantARParentLayoutMgr);
                                            mArParentListAdapter = new ARParentListAdapter(mVendorListingApi, mAllRBinding.nestedView);
                                            mAllRBinding.recyclerAllRestaurantParent.setAdapter(mArParentListAdapter);

                                            if (mVendorListingApi.vendorList != null && mVendorListingApi.vendorList.size() == 0) {
                                                mAllRBinding.layoutRestaurantMenuItemListEmptyMessage.setVisibility(View.VISIBLE);
                                            } else {
                                                mAllRBinding.layoutRestaurantMenuItemListEmptyMessage.setVisibility(View.GONE);
                                            }
                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mVendorListingApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mVendorListingApi.error.message);
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
                        public void onFailure(@NonNull Call<VendorListingApi> call, @NonNull Throwable t) {
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

    private ArrayList<String> getFiltersIdList(ArrayList<FilterDbDataSet> filterDbList) {
        ArrayList<String> mFilterIdList = new ArrayList<>();
        ArrayList<String> mFilterIdListTemp = new ArrayList<>();
        //To get all available parent filters ids from list.
        //Becoz the db may have same filter id with different filter type id.

        for (int id = 0; id < filterDbList.size(); id++) {
            mFilterIdListTemp.add(filterDbList.get(id).getmFilterId());
        }

        HashSet<String> mFilterIdHashSet = new HashSet<String>();
        mFilterIdHashSet.addAll(mFilterIdListTemp);
        //mFilterIdListTemp.clear();
        mFilterIdList.addAll(mFilterIdHashSet);

        return mFilterIdList;
    }


    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if (mId == R.id.tv_all_r_location) {
            // AppFunctions.toastShort(getActivity(), "Delivery location");
            if (getActivity() != null) {
                //the DeliveryLocationSearchDB for to save  DeliveryLocationSearch page process.
                //And its used for DeliveryLocation page only.
                //So every time must refresh the DB before going to DeliveryLocation page.
                if (DeliveryLocationSearchDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    DeliveryLocationSearchDB.getInstance(getActivity()).deleteDB();
                }
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                DeliveryLocation m_deliveryLocation = new DeliveryLocation();
                mFT.replace(R.id.layout_app_home_body, m_deliveryLocation, "m_deliveryLocation");
                mFT.addToBackStack("m_deliveryLocation");
                mFT.commit();
            }
        } else if (mId == R.id.lay_all_r_filters) {

            if (ARFiltersDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                ARFiltersDBTemp.getInstance(getActivity()).deleteDB();
            }
            ArrayList<FilterDbDataSet> mExistingFilterList = ARFiltersDB.getInstance(getActivity()).getFiltersList();
            if (mExistingFilterList != null && mExistingFilterList.size() > 0) {
                for (int filter = 0; filter < mExistingFilterList.size(); filter++) {
                    FilterDbDataSet filterDbDataSet = mExistingFilterList.get(filter);
                    ARFiltersDBTemp.getInstance(getActivity()).addFilters(filterDbDataSet);
                }
            }

            callSideFilterListAPi();

        } else if (mId == R.id.lay_all_r_cuisines) {
            // AppFunctions.toastShort(getActivity(), "Cuisines");
            if (ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                ARCuisinesDBTemp.getInstance(getActivity()).deleteCuisinesDB();
            }
            ArrayList<CuisinesDataSet> mExistingCuisinesList = ARCuisinesDB.getInstance(getActivity()).getCuisineList();
            if (mExistingCuisinesList != null && mExistingCuisinesList.size() > 0) {
                for (int cuisine = 0; cuisine < mExistingCuisinesList.size(); cuisine++) {
                    CuisinesDataSet cuisinesDataSet = mExistingCuisinesList.get(cuisine);
                    ARCuisinesDBTemp.getInstance(getActivity()).addCuisines(cuisinesDataSet);
                }
            }

            callCuisinesListAPi();
        } else if (mId == R.id.lay_all_r_search) {
            // AppFunctions.toastShort(getActivity(), "Search");

            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
            SearchRestaurants m_searchRestaurants = new SearchRestaurants();
            mFT.replace(R.id.layout_app_home_body, m_searchRestaurants, "m_searchRestaurants");
            mFT.addToBackStack("m_searchRestaurants");
            mFT.commit();

        }

    }

    public static void hideKeyboard(Activity activity) {

        InputMethodManager mInputMethodMgr = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        if (mInputMethodMgr != null) {

            mInputMethodMgr.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }

    }


    public class ARParentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private VendorListingApi mVendorListingApi;

        private Handler mBannerHandler = new Handler();
        private Runnable mBannerRunnable;

        private int mBannerCurrentPosition, mBannerPosition;
        private Interpolator mHomeInterpolator = new AccelerateInterpolator();

        private Boolean mLoading = false;
        private int lastVisibleItem, totalItemCount;
        private int visibleThreshold = 10;
        private RestaurantListLoadListener mRestaurantListLoadListener;
        NestedScrollView nested_view;

        public ARParentListAdapter(VendorListingApi vendorListingApi, NestedScrollView nested_view) {
            this.mVendorListingApi = vendorListingApi;
            this.nested_view = nested_view;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.all_restaurant_parent_row, parent, false);
            return new ListViewHolder(view);
            //return new ListViewHolder(AllRestaurantParentRowBinding.inflate(getLayoutInflater()));

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            ListViewHolder mListHolder = (ListViewHolder) holder;

            // mListHolder.mSessionPostFixWord.setText(getActivity().getResources().getString(R.string.good_evening));

            if (mVendorListingApi.filterList != null && mVendorListingApi.filterList.size() > 0) {
                mListHolder.recyclerArFilterList.setVisibility(View.VISIBLE);
                mListHolder.mFiltersListLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                mListHolder.recyclerArFilterList.setLayoutManager(mListHolder.mFiltersListLayoutMgr);
                mListHolder.mFilterListAdapter = new FilterListAdapter(mVendorListingApi.filterList);
                mListHolder.recyclerArFilterList.setAdapter(mListHolder.mFilterListAdapter);
            } else {
                mListHolder.recyclerArFilterList.setVisibility(View.GONE);
            }

            if (mVendorListingApi.bannerList != null && mVendorListingApi.bannerList.size() > 0) {
                mListHolder.mPopularOffersTitle.setVisibility(View.VISIBLE);
                mListHolder.mBannerContainerLay.setVisibility(View.VISIBLE);
                aRBanner(mVendorListingApi.bannerList, mListHolder.mBannerContainerLay,
                        mListHolder.mBannerButtonsLay, mListHolder.mBannerViewPager);
            } else {
                mListHolder.mPopularOffersTitle.setVisibility(View.GONE);
                mListHolder.mBannerContainerLay.setVisibility(View.GONE);
            }

            if (mVendorListingApi.vendorList != null && mVendorListingApi.vendorList.size() > 0) {

                mListHolder.mVendorListEmptyMsg.setVisibility(View.GONE);
                mListHolder.recyclerAllRestaurants.setVisibility(View.VISIBLE);
                mListHolder.mAllRestaurantsTitle.setVisibility(View.VISIBLE);

                mListHolder.mVendorListLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                mListHolder.recyclerAllRestaurants.setLayoutManager(mListHolder.mVendorListLayoutMgr);

                mListHolder.mAllRestaurantsListAdapter = new AllRestaurantsListAdapter(mVendorListingApi.vendorList, mListHolder.recyclerAllRestaurants);
                mListHolder.recyclerAllRestaurants.setAdapter(mListHolder.mAllRestaurantsListAdapter);
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
                                        if (mFreeDelivery == null) {
                                            mFreeDelivery = "0";
                                        }
                                        if (mBusiness_type_id == null) {
                                            mBusiness_type_id = "";
                                        }
                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                                            jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                                            jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());
                                            jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                                            jsonObject.put(DefaultNames.page, mPageCount);
                                            jsonObject.put(DefaultNames.page_per_unit, 10);
                                            jsonObject.put(DefaultNames.free_delivery, mFreeDelivery);
                                            jsonObject.put(DefaultNames.business_type, mBusiness_type_id);

                                            JSONArray cuisineJsonArray = new JSONArray();
                                            if (mCuisineIdsGlobal != null && mCuisineIdsGlobal.size() > 0) {
                                                for (int cuisineId = 0; cuisineId < mCuisineIdsGlobal.size(); cuisineId++) {
                                                    cuisineJsonArray.put(cuisineId, mCuisineIdsGlobal.get(cuisineId).getCuisine_id());
                                                }
                                            }
                                            jsonObject.put(DefaultNames.cuisine, cuisineJsonArray);

                                            jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                                            jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                                            jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                                            RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                                            retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                                            Call<VendorListingApiPagination> Call = retrofitInterface.AllRestaurantsListPagination(body);
                                            mProgressDialog.show();
                                            Call.enqueue(new Callback<VendorListingApiPagination>() {
                                                @SuppressLint("NotifyDataSetChanged")
                                                @Override
                                                public void onResponse(@NonNull Call<VendorListingApiPagination> call, @NonNull Response<VendorListingApiPagination> response) {

                                                    mProgressDialog.cancel();

                                                    if (response.isSuccessful()) {
                                                        VendorListingApiPagination m_Vendor_LA_Pagination = response.body();
                                                        if (m_Vendor_LA_Pagination != null) {

                                                            if (m_Vendor_LA_Pagination.success != null) {
                                                                //Api response successDataSet :-
                                                                if (getActivity() != null) {

                                                                    if (m_Vendor_LA_Pagination.vendorList != null) {
                                                                        if (m_Vendor_LA_Pagination.vendorList.size() > 0) {

                                                                            //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                                                                            mVendorListingApi.vendorList.addAll(m_Vendor_LA_Pagination.vendorList);
                                                                            mListHolder.mAllRestaurantsListAdapter.notifyDataSetChanged();
                                                                            setLoaded();
//                                                                        mListHolder.mAllRestaurantsListAdapter.mRestaurantListLoadListener();
                                                                            //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                //Api response failure :-
                                                                if (getActivity() != null) {
                                                                    if (m_Vendor_LA_Pagination.error != null) {
                                                                        AppFunctions.msgDialogOk(getActivity(), "", m_Vendor_LA_Pagination.error.message);
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            //Log.e("m_Vendor_LA_Pagination", "null");
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
                                                public void onFailure(@NonNull Call<VendorListingApiPagination> call, @NonNull Throwable t) {
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

                                        if (getFragmentManager() != null) {
                                            FragmentTransaction mFT = getFragmentManager().beginTransaction();
                                            NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                                            mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                                            mFT.addToBackStack("mNetworkAnalyser");
                                            mFT.commit();
                                        }

                                    }
                                }
                            }
                            //  }
                        }
                    }
                });

                nested_view.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                        if (v.getChildAt(v.getChildCount() - 1) != null) {
                            if (scrollY > oldScrollY) {
                                if (scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) {
                                    //code to fetch more data for endless scrolling
                                    final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mListHolder.recyclerAllRestaurants.getLayoutManager();
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

            } else {
                mListHolder.mVendorListEmptyMsg.setVisibility(View.VISIBLE);
                mListHolder.recyclerAllRestaurants.setVisibility(View.GONE);
                mListHolder.mAllRestaurantsTitle.setVisibility(View.GONE);
            }

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

            return 1;

        }

        public class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, AdapterView.OnItemClickListener {

            private RecyclerView.LayoutManager mFiltersListLayoutMgr,
                    mVendorListLayoutMgr;
            private FilterListAdapter mFilterListAdapter;
            private AllRestaurantsListAdapter mAllRestaurantsListAdapter;
            private RecyclerView recyclerArFilterList, recyclerAllRestaurants;
            private TextView mPopularOffersTitle, mAllRestaurantsTitle;
            private LinearLayout mBannerContainerLay, mBannerButtonsLay;
            private ViewPager mBannerViewPager;
            NestedScrollView nested_view;
            private TextView mVendorListEmptyMsg;

            // private AllRestaurantParentRowBinding mAllRPRBinding;

            //  ListViewHolder(AllRestaurantParentRowBinding allRestaurantParentRowBinding) {
            // super(allRestaurantParentRowBinding.getRoot());
            // mAllRPRBinding = allRestaurantParentRowBinding;
            ListViewHolder(View view) {
                super(view);

                mVendorListEmptyMsg = view.findViewById(R.id.tv_ar_vendor_list_empty);

                recyclerArFilterList = view.findViewById(R.id.recycler_ar_filter_list);
                mPopularOffersTitle = view.findViewById(R.id.tv_ar_popular_offer_title);
                mBannerContainerLay = view.findViewById(R.id.layout_ar_banner_container);
                mBannerViewPager = view.findViewById(R.id.view_pager_ar_banner_slider);
                mBannerButtonsLay = view.findViewById(R.id.layout_ar_banner_slider_buttons);
                mAllRestaurantsTitle = view.findViewById(R.id.tv_ar_all_restaurants_title);
                recyclerAllRestaurants = view.findViewById(R.id.recycler_ar_vendor_list);
                nested_view = view.findViewById(R.id.nested_view);

                mFiltersListLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
                recyclerArFilterList.setLayoutManager(mFiltersListLayoutMgr);

                mVendorListLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                recyclerAllRestaurants.setLayoutManager(mVendorListLayoutMgr);


            }

            @Override
            public void onClick(View v) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }


        }

        private class FilterListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private final ArrayList<FilterDataSet> mFilterList;

            FilterListAdapter(ArrayList<FilterDataSet> filterList) {
                this.mFilterList = filterList;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new FilterListViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.filter_list_row, viewGroup, false));

            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
                FilterListViewHolder mRCListViewHolder = (FilterListViewHolder) viewHolder;


                mRCListViewHolder.tv_Title.setText(mFilterList.get(position).getName());
                AppFunctions.imageLoaderUsingGlide(mFilterList.get(position).getLogo(), mRCListViewHolder.iv_Image, getActivity());
                //  Glide.with(mActivity).load(R.drawable.x_top_pick_sample_1).into(mRCListViewHolder.iv_Image);

                mRCListViewHolder.mTopPicksRowLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String mTopPicksId = mFilterList.get(position).getFilter_id();

                        toPerformAllShops(position);

                    }
                });

                mRCListViewHolder.tv_Title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String mTopPicksId = mFilterList.get(position).getFilter_id();
                        /*if(mTopPicksId.equals("2")){
                            //All shops :-
                            toPerformAllShops(position);
                        }*/
                        toPerformAllShops(position);
                    }
                });

                mRCListViewHolder.iv_Image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String mTopPicksId = mFilterList.get(position).getFilter_id();
                        /* if(mTopPicksId.equals("2")){
                            //All shops :-
                            toPerformAllShops(position);
                        } */
                        toPerformAllShops(position);
                    }
                });
            }

            private void toPerformAllShops(int position) {

                //  AppFunctions.toastShort(getActivity(), mFilterList.get(position).getName());
                if (getActivity() != null) {

                    if (CFCuisinesDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        CFCuisinesDB.getInstance(getActivity()).deleteCuisinesDB();
                    }

                    if (CFFiltersDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        CFFiltersDB.getInstance(getActivity()).deleteDB();
                    }

                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    CategoryFilter categoryFilter = new CategoryFilter();
                    Bundle bundle = new Bundle();
                    bundle.putString(DefaultNames.filter_name, mFilterList.get(position).getName());
                    bundle.putString(DefaultNames.filter_key, mFilterList.get(position).getKey());
                    categoryFilter.setArguments(bundle);
                    mFT.replace(R.id.layout_app_home_body, categoryFilter, "categoryFilter");
                    mFT.addToBackStack("categoryFilter");
                    mFT.commit();

                }

            }

            @Override
            public int getItemCount() {
                return mFilterList.size();
            }


            class FilterListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
                ImageView iv_Image;
                TextView tv_Title;
                LinearLayout mTopPicksRowLay;

                FilterListViewHolder(View view) {
                    super(view);
                    mTopPicksRowLay = view.findViewById(R.id.lay_row_filter_list);
                    iv_Image = view.findViewById(R.id.iv_image_filter);
                    tv_Title = view.findViewById(R.id.tv_title_filter);
                }

                @Override
                public void onClick(View v) {

                }
            }
        }


        private void aRBanner(ArrayList<VendorBannerDataSet> vendorBannerList, LinearLayout mBannerContainer,
                              LinearLayout mBannerSliderButtonContainer, ViewPager mBannerSlider) {

            try {


                mBannerSlider.setAdapter(new ARBannerAdapter(getActivity(), vendorBannerList, getParentFragmentManager()));
                mBannerCurrentPosition = mBannerSlider.getCurrentItem();

                Field mScroller;
                mScroller = ViewPager.class.getDeclaredField("mScroller");
                mScroller.setAccessible(true);
                HomeBannerScroller mHomeBannerScroller = new HomeBannerScroller(getActivity(), mHomeInterpolator);
                mScroller.set(mBannerSlider, mHomeBannerScroller);


            } catch (Exception e) {
                mBannerContainer.setVisibility(View.GONE);
                // //Log.e("NoSuchFieldException", e.toString());
            }


            // Timer for auto sliding
            bannerScrollingProcess(mBannerSlider);


            if (mBannerSliderButtonContainer != null) {
                mBannerSliderButtonContainer.removeAllViews();
            }


            if (getActivity() != null) {
                if (vendorBannerList != null) {
                    for (int i = 0; i < vendorBannerList.size(); i++) {
                        View view = LayoutInflater.from(getActivity()).inflate(R.layout.home_banner_button, null, false);
                        Button button = view.findViewById(R.id.btn_one);
                        button.setPadding(100, 100, 100, 100);
                        int size = (int) getActivity().getResources().getDisplayMetrics().density;
                        if (size == 3) {

                            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(17, 17);
                            button.setLayoutParams(layoutParams);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(17, 17);
                            params.setMargins(5, 0, 5, 0);
                            button.setLayoutParams(params);
                        } else if (size == 2) {

                            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(12, 12);
                            button.setLayoutParams(layoutParams);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(12, 12);
                            params.setMargins(5, 0, 5, 0);
                            button.setLayoutParams(params);
                        } else {

                            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(7, 7);
                            button.setLayoutParams(layoutParams);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(7, 7);
                            params.setMargins(5, 0, 5, 0);
                            button.setLayoutParams(params);
                        }
                        if (mBannerCurrentPosition == i) {
                            button.setBackgroundResource(R.drawable.banner_btn_selected);
                        } else {
                            button.setBackgroundResource(R.drawable.banner_btn_non_selected);
                        }
                        mBannerSliderButtonContainer.addView(button);
                    }

                }

            }


            mBannerSlider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    mBannerSliderButtonContainer.removeAllViews();
                    //if (vendorBannerList != null) {
                    if (vendorBannerList.size() > 0) {
                        for (int i = 0; i < vendorBannerList.size(); i++) {
                            View view = LayoutInflater.from(getActivity()).inflate(R.layout.home_banner_button, null, false);
                            Button button = view.findViewById(R.id.btn_one);


                            int size = (int) getActivity().getResources().getDisplayMetrics().density;
                            if (size == 3) {

                                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(17, 17);
                                button.setLayoutParams(layoutParams);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(17, 17);
                                params.setMargins(5, 0, 5, 0);
                                button.setLayoutParams(params);
                            } else if (size == 2) {

                                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(12, 12);
                                button.setLayoutParams(layoutParams);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(12, 12);
                                params.setMargins(5, 0, 5, 0);
                                button.setLayoutParams(params);
                            } else {

                                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(7, 7);
                                button.setLayoutParams(layoutParams);
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(7, 7);
                                params.setMargins(5, 0, 5, 0);
                                button.setLayoutParams(params);
                            }
                            if (position == i) {
                                button.setBackgroundResource(R.drawable.banner_btn_selected);
                            } else {
                                button.setBackgroundResource(R.drawable.banner_btn_non_selected);
                            }
                            mBannerSliderButtonContainer.addView(button);
                        }
                    }
                    // }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });

        }

        private void bannerScrollingProcess(ViewPager mBannerSlider) {

            stopThread();

            mBannerHandler = new Handler();
            mBannerRunnable = new Runnable() {
                public void run() {

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //   Log.d("Initial Page is ", " " + String.valueOf(mPage));
                                if (mVendorListingApi.bannerList != null && mVendorListingApi.bannerList.size() > 0) {
                                    if (mBannerPosition == mVendorListingApi.bannerList.size()) {
                                        mBannerPosition = 0;
                                        mBannerSlider.setCurrentItem(mBannerPosition, true);
                                        mBannerPosition++;
                                        //    //Log.e("Page->if is ", " " + String.valueOf(mPage));
                                    } else {
                                        // pagePosition = mPage;
                                        mBannerSlider.setCurrentItem(mBannerPosition, true);
                                        mBannerPosition++;
                                        //  //Log.e("Page->else is ", " " + String.valueOf(mPage));
                                    }
                                }
                            }
                        });
                    }
                    mBannerHandler.postDelayed(mBannerRunnable, 3000);
                }
            };
            mBannerHandler.postDelayed(mBannerRunnable, 3000);

        }

        public void stopThread() {
            if (mBannerHandler != null && mBannerRunnable != null) {
                mBannerHandler.removeCallbacks(mBannerRunnable);
            }
        }

        public class ARBannerAdapter extends PagerAdapter {

            private final Context mContext;
            private final ArrayList<VendorBannerDataSet> mARBannerList;
            private final FragmentManager mFragmentManager;

            public ARBannerAdapter(Context context, ArrayList<VendorBannerDataSet> homeBannerList, FragmentManager fragmentManager) {
                this.mContext = context;
                this.mARBannerList = homeBannerList;
                this.mFragmentManager = fragmentManager;
            }

            @Override
            public int getCount() {

                if (mARBannerList != null) {
                    return mARBannerList.size();
                } else {
                    return 0;
                }

            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {

                LayoutInflater mInflater = LayoutInflater.from(mContext);
                ViewGroup mView = (ViewGroup) mInflater.inflate(R.layout.banner_image, container, false);
                ImageView mBannerImage = mView.findViewById(R.id.img_restaurant_banner_image);
                mBannerImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mContext != null) {
                            FragmentTransaction mFT = mFragmentManager.beginTransaction();
                            RestaurantInfo restaurantInfo = new RestaurantInfo();
                            Bundle mBundle = new Bundle();
                            mBundle.putString("vendor_id", mARBannerList.get(position).getVendor_id());
                            mBundle.putString("product_id", "");
                            restaurantInfo.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_home_body, restaurantInfo, "restaurantInfo");
                            mFT.addToBackStack("restaurantInfo");
                            mFT.commit();
                        }

                    }
                });

                AppFunctions.bannerLoaderUsingGlide(mARBannerList.get(position).getBanner(), mBannerImage, getActivity());

                container.addView(mView);

                return mView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        }

        private class AllRestaurantsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            ArrayList<VendorDataSet> mVendorList;
            RecyclerView recyclerViewVendorList;

            AllRestaurantsListAdapter(ArrayList<VendorDataSet> vendorList, RecyclerView recyclerViewVendorList) {
                this.mVendorList = vendorList;
                this.recyclerViewVendorList = recyclerViewVendorList;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RestaurantViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.rc_all_restaurants_row, parent, false));
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

                RestaurantViewHolder restaurantViewHolder = (RestaurantViewHolder) holder;
                restaurantViewHolder.tv_RestaurantTitle.setText(mVendorList.get(position).getName());
                AppFunctions.imageLoaderUsingGlide(mVendorList.get(position).getLogo(), restaurantViewHolder.iv_vendorImage, getActivity());
                restaurantViewHolder.tv_restaurant_sub_content.setText(mVendorList.get(position).getCuisines());
                String mVendorStatus = mVendorList.get(position).getVendor_status();

                if (OrderTypeDB.getInstance(activity).getUserServiceType().equals("2")) {
                    restaurantViewHolder.mDeliveryAmtContainer.setVisibility(View.GONE);
                    restaurantViewHolder.mPickupContainer.setVisibility(View.VISIBLE);
                    String mDTime = mVendorList.get(position).getDelivery_time()
                            + " " + getActivity().getResources().getString(R.string.mins);
                    restaurantViewHolder.mPickupTime.setText(mDTime);
                } else {
                    restaurantViewHolder.mDeliveryAmtContainer.setVisibility(View.VISIBLE);
                    restaurantViewHolder.mPickupContainer.setVisibility(View.GONE);
                    String mDTime = mVendorList.get(position).getDelivery_time()
                            + " " + getActivity().getResources().getString(R.string.mins);
                    restaurantViewHolder.tv_DeliveryTime.setText(mDTime);
                }

                if (mVendorList.get(position).free_delivery != null && mVendorList.get(position).free_delivery.equals("1")) {
                    restaurantViewHolder.tv_delivery_amount.setVisibility(View.GONE);
                    restaurantViewHolder.tv_delivery_amount_title.setVisibility(View.VISIBLE);
                    restaurantViewHolder.tv_delivery_amount_title.setText(activity.getResources().getString(R.string.ar_filter_do_free_delivery));
                } else {
                    restaurantViewHolder.tv_delivery_amount.setVisibility(View.VISIBLE);
                    restaurantViewHolder.tv_delivery_amount.setText(mVendorList.get(position).getDelivery_charge());
                    restaurantViewHolder.tv_delivery_amount_title.setVisibility(View.GONE);
//                    restaurantViewHolder.tv_delivery_amount_title.setText(activity.getResources().getString(R.string.ar_delivery));
                }

                if (WishListDB.getInstance(activity).getSizeOfList() > 0) {
                    if (WishListDB.getInstance(activity).isSelected(mVendorList.get(position).getVendor_id())) {
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
                            if (WishListDB.getInstance(activity).isSelected(mVendorList.get(position).getVendor_id())) {
                                toast_layout(activity.getResources().getString(R.string.removed_from_the_wishlist), mAllRBinding.wishlistLayout);
                                WishListDB.getInstance(activity).removeFromFavouriteList(mVendorList.get(position).getVendor_id());
                                restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_border_primary_color_24dp));
                            } else {
                                toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), mAllRBinding.wishlistLayout);
                                WishListDB.getInstance(activity).add_vendor_id(mVendorList.get(position).getVendor_id());
                                restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                            }
                        } else {
                            toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), mAllRBinding.wishlistLayout);
                            WishListDB.getInstance(activity).add_vendor_id(mVendorList.get(position).getVendor_id());
                            restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                        }
                    }
                });
                restaurantViewHolder.tv_ar_minimum_amount.setText(activity.getResources().getString(R.string.min) + " - " +
                        mVendorList.get(position).minimum_amount);

                //1 -  open
                //2 -  busy
                //0 -  closed
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

                    if (mVendorList.get(position).getVendorRatingDataSet() != null) {
                        String mCRating = mVendorList.get(position).getVendorRatingDataSet().vendor_rating_name;
                        if (mCRating != null && !mCRating.isEmpty()) {
                            restaurantViewHolder.tv_rating_statement.setText(mCRating);
                            restaurantViewHolder.rating_linear.setVisibility(View.VISIBLE);
                            AppFunctions.imageLoaderUsingGlide(mVendorList.get(position).getVendorRatingDataSet().vendor_rating_image, restaurantViewHolder.rating_img, activity);
                        } else {
                            restaurantViewHolder.tv_rating_statement.setText("0");
                            restaurantViewHolder.rating_linear.setVisibility(View.GONE);
                        }
                    } else {
                        restaurantViewHolder.tv_rating_statement.setText("0");
                        restaurantViewHolder.rating_linear.setVisibility(View.GONE);
                    }
                }
//                restaurantViewHolder.tv_delivery_amount.setText(mVendorList.get(position).getDelivery_charge());

                String m_OfferData = mVendorList.get(position).getOffer();
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
                return mVendorList.size();
            }


            class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                ImageView favorite_icon, rating_img;
                ShapeableImageView iv_vendorImage;
                TextView tv_RestaurantTitle, tv_DeliveryTime, tv_restaurant_sub_content;
                TextView tv_rating_statement, mPickupTime,
                        tv_delivery_amount_title, tv_delivery_amount, tv_offerContent, tv_ImageOverStatus, tv_ar_minimum_amount;
                LinearLayout mDeliveryAmtContainer, mDeliveryTimeContainer, mPickupContainer,
                        mLayImageOverStatus, mLayVLOfferContainer, empty, rating_linear;

                RestaurantViewHolder(View itemView) {
                    super(itemView);
                    itemView.setOnClickListener(this);
                    iv_vendorImage = itemView.findViewById(R.id.iv_ar_restaurant_image);
                    tv_RestaurantTitle = itemView.findViewById(R.id.tv_ar_restaurant_title);
                    tv_restaurant_sub_content = itemView.findViewById(R.id.tv_ar_restaurant_sub_title);
                    tv_rating_statement = itemView.findViewById(R.id.tv_ar_rating_msg);
                    rating_linear = itemView.findViewById(R.id.rating_linear);
                    rating_img = itemView.findViewById(R.id.rating_img);
                    mDeliveryAmtContainer = itemView.findViewById(R.id.delivery_container);
                    mPickupContainer = itemView.findViewById(R.id.pickup_container);
                    mPickupTime = itemView.findViewById(R.id.tv_ar_pickup_time);

                    tv_delivery_amount_title = itemView.findViewById(R.id.tv_ar_delivery_amt_title);
                    tv_delivery_amount = itemView.findViewById(R.id.tv_ar_delivery_amt_data);
                    tv_ar_minimum_amount = itemView.findViewById(R.id.tv_ar_minimum_amount);
                    mDeliveryTimeContainer = itemView.findViewById(R.id.lay_ar_restaurant_delivery_time_container);
                    tv_DeliveryTime = itemView.findViewById(R.id.tv_ar_delivery_time);
                    tv_offerContent = itemView.findViewById(R.id.tv_ar_restaurant_offers);
                    mLayImageOverStatus = itemView.findViewById(R.id.lay_ar_restaurant_image);
                    tv_ImageOverStatus = itemView.findViewById(R.id.tv_ar_restaurant_image_over_status);
                    mLayVLOfferContainer = itemView.findViewById(R.id.offer_linear);
                    empty = itemView.findViewById(R.id.empty);
                    favorite_icon = itemView.findViewById(R.id.favorite_icon);
                }

                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() != -1) {
                        if (mVendorList.get(getAdapterPosition()).getVendor_status().equals("0")) {
                            toDialogClosedVendor(mVendorList.get(getAdapterPosition()).getName(), mVendorList.get(getAdapterPosition()).getVendor_id(),
                                    "0");
                        } else if (mVendorList.get(getAdapterPosition()).getVendor_status().equals("2")) {
                            toDialogBusyVendor(mVendorList.get(getAdapterPosition()).getName(), mVendorList.get(getAdapterPosition()).getVendor_id(),
                                    "0");
                        } else {
                            if (mVendorList.get(getAdapterPosition()).getVendor_type_id().equals("2")) {
                                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                GroceryCategoryMainPage m_groceryCategoryMainPage = new GroceryCategoryMainPage();
                                Bundle mBundle = new Bundle();
                                mBundle.putString(DefaultNames.store_id, mVendorList.get(getAdapterPosition()).getVendor_id());
                                mBundle.putString(DefaultNames.store_name, mVendorList.get(getAdapterPosition()).getName());
                                m_groceryCategoryMainPage.setArguments(mBundle);
                                mFT.replace(R.id.layout_app_home_body, m_groceryCategoryMainPage, "m_groceryCategoryMainPage");
                                mFT.addToBackStack("m_groceryCategoryMainPage");
                                mFT.commit();
                            } else {
                                toStoreListing(mVendorList.get(getAdapterPosition()).getVendor_id(), "0");
                            }
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

            public void toStoreListing(String vendor_id, String product_id) {
                if (getActivity() != null) {
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    RestaurantInfo restaurantInfo = new RestaurantInfo();
                    Bundle mBundle = new Bundle();
                    mBundle.putString("vendor_id", vendor_id);
                    mBundle.putString("product_id", product_id);
                    restaurantInfo.setArguments(mBundle);
                    mFT.replace(R.id.layout_app_home_body, restaurantInfo, "restaurantInfo");
                    mFT.addToBackStack("restaurantInfo");
                    mFT.commit();
                }
            }

            public void toDialogClosedVendor(String name, String vendor_id, String product_id) {
                String msg = activity.getResources().getString(R.string.restaurant_closed_msg_1) + " " + name + " " + activity.getResources().getString(R.string.restaurant_closed_msg_2);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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

        }
    }

    private void toast_layout(String string, final LinearLayout wishlistLayout) {

        wishlistLayout.setVisibility(View.VISIBLE);
        mAllRBinding.dataText.setText(string);
        mAllRBinding.viewAll.setOnClickListener(new View.OnClickListener() {
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

    private void callCuisinesListAPi() {


        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    mProgressDialog.show();

                    jsonObject.put(DefaultNames.search, "");
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<CuisinesApi> Call = retrofitInterface.CuisinesList(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<CuisinesApi>() {
                        @Override
                        public void onResponse(@NonNull Call<CuisinesApi> call, @NonNull Response<CuisinesApi> response) {

                            mProgressDialog.cancel();

                            if (response.isSuccessful()) {

                                mCuisinesApi = response.body();
                                if (mCuisinesApi != null) {
                                    if (mCuisinesApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            if (mCuisinesApi.cuisinesList != null && mCuisinesApi.cuisinesList.size() > 0) {


                                                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                                CuisinesListBottomSheet mCuisinesListBottomSheet =
                                                        new CuisinesListBottomSheet(retrofitInterface, mProgressDialog,
                                                                mCuisinesApi.cuisinesList, mCuisinesApplyCall);
                                                mCuisinesListBottomSheet.show(mFT, "mCuisinesListBottomSheet");


                                            }

                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mCuisinesApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mCuisinesApi.error.message);
                                            }
                                        }


                                    }
                                } else {


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
                        public void onFailure(@NonNull Call<CuisinesApi> call, @NonNull Throwable t) {

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

    private void callSideFilterListAPi() {


        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    mProgressDialog.show();

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<FilterListApi> Call = retrofitInterface.filterListApi(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<FilterListApi>() {
                        @Override
                        public void onResponse(@NonNull Call<FilterListApi> call, @NonNull Response<FilterListApi> response) {

                            mProgressDialog.cancel();

                            if (response.isSuccessful()) {

                                mFilterListApi = response.body();
                                if (mFilterListApi != null) {
                                    if (mFilterListApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            if (mFilterListApi.filterList != null && mFilterListApi.filterList.size() > 0) {

                                                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                                FilterListBottomSheet mFilterListBottomSheet =
                                                        new FilterListBottomSheet(retrofitInterface, mProgressDialog,
                                                                mFilterListApi.filterList, mFiltersApplyCall);
                                                mFilterListBottomSheet.show(mFT, "mFilterListBottomSheet");


                                            } else {

                                            }


                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mFilterListApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mFilterListApi.error.message);
                                            }
                                        }


                                    }
                                } else {

                                    //Log.e("mFilterListApi", "null");
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
                        public void onFailure(@NonNull Call<FilterListApi> call, @NonNull Throwable t) {

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

    //**********************************************************************************************

    public static class CuisinesListBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {

        private CuisinesListBottomSheetBinding mBindingCLBS;
        private ArrayList<CuisinesDataSet> m_CuisinesList;
        private RetrofitInterface mRetrofitInterface;
        private ProgressDialog m_ProgressDialog;
        private CuisinesListAdapter mCuisinesListAdapter;
        private RecyclerView.LayoutManager mCuisinesListLayoutMgr;
        private CuisinesApplyCall m__CuisinesApplyCall;


        public CuisinesListBottomSheet(RetrofitInterface retrofitInterface,
                                       ProgressDialog progressDialog,
                                       ArrayList<CuisinesDataSet> cuisinesList,
                                       CuisinesApplyCall cuisinesApplyCall) {

            // Required empty public constructor

            this.mRetrofitInterface = retrofitInterface;
            this.m_ProgressDialog = progressDialog;
            this.m_CuisinesList = cuisinesList;
            this.m__CuisinesApplyCall = cuisinesApplyCall;


        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mBindingCLBS = CuisinesListBottomSheetBinding.inflate(inflater, container, false);


            mBindingCLBS.edtArCuisinesSearch.setText("");
            mBindingCLBS.layHomeCuisinesCloseContainer.setOnClickListener(this);
            mBindingCLBS.layArCuisinesApply.setOnClickListener(this);
            mBindingCLBS.layArCuisinesSearchClear.setOnClickListener(this);
            mBindingCLBS.layArCuisinesSearchClear.setVisibility(View.INVISIBLE);
            mBindingCLBS.tvArCuisinesClearAll.setOnClickListener(this);
            mBindingCLBS.edtArCuisinesSearch.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {

                    if (getActivity() != null) {

                        if (s.length() == 0) {
                            mBindingCLBS.layArCuisinesSearchClear.setVisibility(View.INVISIBLE);
                        } else {
                            mBindingCLBS.layArCuisinesSearchClear.setVisibility(View.VISIBLE);
                        }

                    }


                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {

                }

                public void onTextChanged(CharSequence query, int start,
                                          int before, int count) {

                    //**********************************************************************************

                    query = query.toString().toLowerCase();

                    final ArrayList<CuisinesDataSet> m_Cuisines_List = new ArrayList<>();

                    if (m_CuisinesList != null) {
                        if (getActivity() != null) {

                            if (m_CuisinesList != null && m_CuisinesList.size() > 0) {
                                for (int i = 0; i < m_CuisinesList.size(); i++) {
                                    //Note : getName() will get null when AllCuisines is selected
                                    //            for that to check is it null or not :-
                                    if (m_CuisinesList.get(i) != null) {
                                        String mTempBranchName = m_CuisinesList.get(i).getName();
                                        if (mTempBranchName != null) {
                                            final String mSearchedText = m_CuisinesList.get(i).getName().toLowerCase();
                                            if (mSearchedText.contains(query)) {
                                                m_Cuisines_List.add(m_CuisinesList.get(i));
                                            }
                                        }
                                    }
                                }
                            }

                            if (m_Cuisines_List != null && m_Cuisines_List.size() > 0) {

                                if (ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                    String mSelectedCuisines = ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() + " " +
                                            getActivity().getResources().getString(R.string.ar_cuisines_selected);
                                    mBindingCLBS.btnArSelectedCuisinesApply.setVisibility(View.VISIBLE);
                                    mBindingCLBS.btnArSelectedCuisinesApply.setText(mSelectedCuisines);
                                } else {
                                    mBindingCLBS.btnArSelectedCuisinesApply.setVisibility(View.GONE);
                                    mBindingCLBS.btnArSelectedCuisinesApply.setText("");
                                }

                                mBindingCLBS.tvArCuisinesClearAll.setVisibility(View.VISIBLE);
                                mBindingCLBS.layArCuisinesApply.setVisibility(View.VISIBLE);
                                mBindingCLBS.tvArCuisinesListEmpty.setVisibility(View.GONE);
                                mBindingCLBS.recyclerArCuisinesList.setVisibility(View.VISIBLE);

                                mCuisinesListLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                mBindingCLBS.recyclerArCuisinesList.setLayoutManager(mCuisinesListLayoutMgr);
                                mCuisinesListAdapter = new CuisinesListAdapter(m_Cuisines_List);
                                mBindingCLBS.recyclerArCuisinesList.setAdapter(mCuisinesListAdapter);
                                mCuisinesListAdapter.notifyDataSetChanged();


                            } else {

                                mBindingCLBS.tvArCuisinesClearAll.setVisibility(View.GONE);
                                mBindingCLBS.layArCuisinesApply.setVisibility(View.GONE);
                                mBindingCLBS.tvArCuisinesListEmpty.setVisibility(View.VISIBLE);
                                mBindingCLBS.recyclerArCuisinesList.setVisibility(View.GONE);


                            }


                        }

                    }


                    //**********************************************************************************

                }
            });

            if (m_CuisinesList != null && m_CuisinesList.size() > 0) {

                if (ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                    String mSelectedCuisines = ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() + " " +
                            getActivity().getResources().getString(R.string.ar_cuisines_selected);
                    mBindingCLBS.btnArSelectedCuisinesApply.setVisibility(View.VISIBLE);
                    mBindingCLBS.btnArSelectedCuisinesApply.setText(mSelectedCuisines);
                } else {
                    mBindingCLBS.btnArSelectedCuisinesApply.setVisibility(View.GONE);
                    mBindingCLBS.btnArSelectedCuisinesApply.setText("");
                }

                mBindingCLBS.layArCuisinesSearch.setVisibility(View.VISIBLE);
                mBindingCLBS.tvArCuisinesClearAll.setVisibility(View.VISIBLE);
                mBindingCLBS.layArCuisinesApply.setVisibility(View.VISIBLE);
                mBindingCLBS.tvArCuisinesListEmpty.setVisibility(View.GONE);
                mBindingCLBS.recyclerArCuisinesList.setVisibility(View.VISIBLE);

                mCuisinesListLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                mBindingCLBS.recyclerArCuisinesList.setLayoutManager(mCuisinesListLayoutMgr);
                mCuisinesListAdapter = new CuisinesListAdapter(m_CuisinesList);
                mBindingCLBS.recyclerArCuisinesList.setAdapter(mCuisinesListAdapter);
                mCuisinesListAdapter.notifyDataSetChanged();


            } else {

                mBindingCLBS.layArCuisinesSearch.setVisibility(View.GONE);
                mBindingCLBS.tvArCuisinesClearAll.setVisibility(View.GONE);
                mBindingCLBS.layArCuisinesApply.setVisibility(View.GONE);
                mBindingCLBS.tvArCuisinesListEmpty.setVisibility(View.VISIBLE);
                mBindingCLBS.recyclerArCuisinesList.setVisibility(View.GONE);


            }


            return mBindingCLBS.getRoot();
        }

        private void hideKeyboard() {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            // To get the correct window token, lets first get the currently focused view
            // View v__iew = mAllRestaurantsBinding.edtArCuisinesSearch.getRootView();
            View v__iew = mBindingCLBS.getRoot();
            // To get the window token when there is no currently focused view, we have a to create a view
            if (v__iew == null) {
                v__iew = new View(getActivity());
            }
            // hide the keyboard
            imm.hideSoftInputFromWindow(v__iew.getWindowToken(), 0);

        }

        @Override
        public void onClick(View view) {
            int mId = view.getId();

            if (mId == R.id.tv_ar_cuisines_clear_all) {

                if (ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                    ARCuisinesDBTemp.getInstance(getActivity()).deleteCuisinesDB();
                }
                if (mCuisinesListAdapter != null) {
                    mCuisinesListAdapter.notifyDataSetChanged();
                }

                mBindingCLBS.btnArSelectedCuisinesApply.setVisibility(View.GONE);
                mBindingCLBS.btnArSelectedCuisinesApply.setText("");


            } else if (mId == R.id.lay_ar_cuisines_apply) {

                hideKeyboard();

                if (m__CuisinesApplyCall != null) {
                    m__CuisinesApplyCall.cuisinesApplyCall();
                }

                if (getDialog() != null) {

                    getDialog().dismiss();

                }

            } else if (mId == R.id.lay_home_cuisines_close_container) {

                if (getDialog() != null) {

                    getDialog().dismiss();

                }

            } else if (mId == R.id.lay_ar_cuisines_search_clear) {

                mBindingCLBS.edtArCuisinesSearch.setText("");
                if (getActivity() != null) {
                    if (m_CuisinesList != null) {
                        if (m_CuisinesList != null && m_CuisinesList.size() > 0) {

                            if (ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                String mSelectedCuisines = ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() + " " +
                                        getActivity().getResources().getString(R.string.ar_cuisines_selected);
                                mBindingCLBS.btnArSelectedCuisinesApply.setVisibility(View.VISIBLE);
                                mBindingCLBS.btnArSelectedCuisinesApply.setText(mSelectedCuisines);
                            } else {
                                mBindingCLBS.btnArSelectedCuisinesApply.setVisibility(View.GONE);
                                mBindingCLBS.btnArSelectedCuisinesApply.setText("");
                            }

                            mBindingCLBS.layArCuisinesSearch.setVisibility(View.VISIBLE);
                            mBindingCLBS.tvArCuisinesClearAll.setVisibility(View.VISIBLE);
                            mBindingCLBS.layArCuisinesApply.setVisibility(View.VISIBLE);
                            mBindingCLBS.tvArCuisinesListEmpty.setVisibility(View.GONE);
                            mBindingCLBS.recyclerArCuisinesList.setVisibility(View.VISIBLE);

                            mCuisinesListLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                            mBindingCLBS.recyclerArCuisinesList.setLayoutManager(mCuisinesListLayoutMgr);
                            mCuisinesListAdapter = new CuisinesListAdapter(m_CuisinesList);
                            mBindingCLBS.recyclerArCuisinesList.setAdapter(mCuisinesListAdapter);

                        } else {

                            mBindingCLBS.layArCuisinesSearch.setVisibility(View.GONE);
                            mBindingCLBS.tvArCuisinesClearAll.setVisibility(View.GONE);
                            mBindingCLBS.layArCuisinesApply.setVisibility(View.GONE);
                            mBindingCLBS.tvArCuisinesListEmpty.setVisibility(View.VISIBLE);
                            mBindingCLBS.recyclerArCuisinesList.setVisibility(View.GONE);

                        }


                    }
                }

            }
        }

        private class CuisinesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private ArrayList<CuisinesDataSet> mCuisinesList;
            //private final ArrayList<Object> cuisine__ListUI;

            CuisinesListAdapter(ArrayList<CuisinesDataSet> cuisinesList) {

                this.mCuisinesList = cuisinesList;


            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {


                return new CuisinesListViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.rc_cuisines_list_row, viewGroup, false));

            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {


                Log.e("2039", "called");

                CuisinesListViewHolder mRCListViewHolder = (CuisinesListViewHolder) viewHolder;

                if (mCuisinesList != null) {

                    if (ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                        if (ARCuisinesDBTemp.getInstance(getActivity()).isCuisineSelected(mCuisinesList.get(position))) {
                            mRCListViewHolder.chk_cuisine.setChecked(true);
                        } else {
                            mRCListViewHolder.chk_cuisine.setChecked(false);
                        }
                    } else {
                        mRCListViewHolder.chk_cuisine.setChecked(false);
                    }

                    mRCListViewHolder.tv_cuisine_Title.setText(mCuisinesList.get(position).getName());

                    mRCListViewHolder.mCuisineRowLay.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String mCuisineId = mCuisinesList.get(position).getCuisine_id();

                            if (mRCListViewHolder.chk_cuisine.isChecked()) {
                                mRCListViewHolder.chk_cuisine.setChecked(false);
                            } else {
                                mRCListViewHolder.chk_cuisine.setChecked(true);
                            }


                        }
                    });

                    mRCListViewHolder.tv_cuisine_Title.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String mCuisineId = mCuisinesList.get(position).getCuisine_id();

                            if (mRCListViewHolder.chk_cuisine.isChecked()) {
                                mRCListViewHolder.chk_cuisine.setChecked(false);
                            } else {
                                mRCListViewHolder.chk_cuisine.setChecked(true);
                            }

                        }
                    });

                    mRCListViewHolder.chk_cuisine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                                                 @Override
                                                                                 public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                                                                                     String mCuisineId = mCuisinesList.get(position).getCuisine_id();
                                                                                     String mCuisine = mCuisinesList.get(position).getName();

                                                                                     if (b) {
                                                                                         //cuisine selected :-
                                                                                         //Log.e("if", "cuisine selected + " + position);
                                                                                         CuisinesDataSet cuisinesDataSet = mCuisinesList.get(position);
                                                                                         ARCuisinesDBTemp.getInstance(getActivity()).addCuisines(cuisinesDataSet);
                                                                                     } else {
                                                                                         //cuisine un selected :-
                                                                                         //Log.e("else", "cuisine un selected + " + position);
                                                                                         if (ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                                                                             if (ARCuisinesDBTemp.getInstance(getActivity()).isCuisineSelected(mCuisinesList.get(position))) {
                                                                                                 ARCuisinesDBTemp.getInstance(getActivity()).removeItemFromCuisineList(mCuisinesList.get(position));
                                                                                             }
                                                                                         }
                                                                                     }

                                                                                     //ar_cuisines_selected
                                                                                     //btn_ar_selected_cuisines_apply
                                                                                     if (ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                                                                         String mSelectedCuisines = ARCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() + " " +
                                                                                                 getActivity().getResources().getString(R.string.ar_cuisines_selected);
                                                                                         mBindingCLBS.btnArSelectedCuisinesApply.setVisibility(View.VISIBLE);
                                                                                         mBindingCLBS.btnArSelectedCuisinesApply.setText(mSelectedCuisines);
                                                                                     } else {
                                                                                         mBindingCLBS.btnArSelectedCuisinesApply.setVisibility(View.GONE);
                                                                                         mBindingCLBS.btnArSelectedCuisinesApply.setText("");
                                                                                     }

                                                                                 }
                                                                             }
                    );

                }


            }


            @Override
            public int getItemCount() {
                if (mCuisinesList != null && mCuisinesList.size() > 0) {
                    return mCuisinesList.size();
                } else {
                    return 0;
                }

            }


            class CuisinesListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
                CheckBox chk_cuisine;
                TextView tv_cuisine_Title;
                private LinearLayout mCuisineRowLay;

                CuisinesListViewHolder(View view) {
                    super(view);

                    mCuisineRowLay = view.findViewById(R.id.lay_cl_row);
                    chk_cuisine = view.findViewById(R.id.chk_cl_cuisine);
                    tv_cuisine_Title = view.findViewById(R.id.tv_cl_cuisine_title);

                }

                @Override
                public void onClick(View v) {

                }
            }


        }


    }


    //**********************************************************************************************

    @Override
    public void cuisinesApplyCall() {


        ArrayList<CuisinesDataSet> mTempCuisinesList = ARCuisinesDBTemp.getInstance(getActivity()).getCuisineList();
        if (mTempCuisinesList != null && mTempCuisinesList.size() > 0) {
            if (ARCuisinesDB.getInstance(getActivity()).getSizeOfList() > 0) {
                ARCuisinesDB.getInstance(getActivity()).deleteCuisinesDB();
            }
            for (int cuisine = 0; cuisine < mTempCuisinesList.size(); cuisine++) {
                CuisinesDataSet cuisinesDataSet = mTempCuisinesList.get(cuisine);
                ARCuisinesDB.getInstance(getActivity()).addCuisines(cuisinesDataSet);
            }
        } else {
            //this case will occur , if ARCuisinesDBTemp has some values then press clear all then press
            //apply button now definitely ARCuisinesDBTemp is empty .So delete the ARCuisinesDB and mCuisineIdsGlobal
            //  list to empty.!.
            //Just because user wants clear all cuisines.!
            if (ARCuisinesDB.getInstance(getActivity()).getSizeOfList() > 0) {
                ARCuisinesDB.getInstance(getActivity()).deleteCuisinesDB();
                mCuisineIdsGlobal = new ArrayList<>();
            }
        }
        ARCuisinesDBTemp.getInstance(getActivity()).deleteCuisinesDB();
        if (ARCuisinesDB.getInstance(getActivity()).getSizeOfList() > 0) {
            mCuisineIdsGlobal = ARCuisinesDB.getInstance(getActivity()).getCuisineList();
        }
        callVendorListAPi();


    }

    //********************** filter bottom sheet start *********************************************

    public static class FilterListBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener {

        private FilterListBottomSheetBinding mBindingFLBS;
        private ArrayList<FilterListDataSet> m_FilterList;
        private RetrofitInterface mRetrofitInterface;
        private ProgressDialog m_ProgressDialog;
        private FilterListAdapter mFilterListAdapter;
        private RecyclerView.LayoutManager mCuisinesListLayoutMgr;
        private FiltersApplyCall m__FiltersApplyCall;


        public FilterListBottomSheet(RetrofitInterface retrofitInterface,
                                     ProgressDialog progressDialog,
                                     ArrayList<FilterListDataSet> filtersList,
                                     FiltersApplyCall filtersApplyCall) {

            // Required empty public constructor

            this.mRetrofitInterface = retrofitInterface;
            this.m_ProgressDialog = progressDialog;
            this.m_FilterList = filtersList;
            this.m__FiltersApplyCall = filtersApplyCall;


        }


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            mBindingFLBS = FilterListBottomSheetBinding.inflate(inflater, container, false);

            //lay_home_filter_close_container
            mBindingFLBS.layHomeFilterCloseContainer.setOnClickListener(this);
            mBindingFLBS.tvArFilterClearAll.setOnClickListener(this);
            mBindingFLBS.btnArFilterApply.setOnClickListener(this);
            mBindingFLBS.tvArFilterClearAll.setOnClickListener(this);

            // m_FilterList = null;
            if (m_FilterList != null && m_FilterList.size() > 0) {

                mBindingFLBS.tvArFilterClearAll.setVisibility(View.VISIBLE);
                mBindingFLBS.btnArFilterApply.setVisibility(View.VISIBLE);
                mBindingFLBS.recyclerArFilterList.setVisibility(View.VISIBLE);
                mBindingFLBS.tvArFilterListEmpty.setVisibility(View.GONE);

                mCuisinesListLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                mBindingFLBS.recyclerArFilterList.setLayoutManager(mCuisinesListLayoutMgr);
                mFilterListAdapter = new FilterListAdapter(m_FilterList);
                mBindingFLBS.recyclerArFilterList.setAdapter(mFilterListAdapter);
                mFilterListAdapter.notifyDataSetChanged();


            } else {

                mBindingFLBS.tvArFilterClearAll.setVisibility(View.GONE);
                mBindingFLBS.btnArFilterApply.setVisibility(View.GONE);
                mBindingFLBS.recyclerArFilterList.setVisibility(View.GONE);
                mBindingFLBS.tvArFilterListEmpty.setVisibility(View.VISIBLE);


            }


            return mBindingFLBS.getRoot();
        }

        private void hideKeyboard() {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            // To get the correct window token, lets first get the currently focused view
            // View v__iew = mAllRestaurantsBinding.edtArCuisinesSearch.getRootView();
            View v__iew = mBindingFLBS.getRoot();
            // To get the window token when there is no currently focused view, we have a to create a view
            if (v__iew == null) {
                v__iew = new View(getActivity());
            }
            // hide the keyboard
            imm.hideSoftInputFromWindow(v__iew.getWindowToken(), 0);

        }

        @Override
        public void onClick(View view) {
            int mId = view.getId();

            if (mId == R.id.tv_ar_filter_clear_all) {

                if (ARFiltersDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                    ARFiltersDBTemp.getInstance(getActivity()).deleteDB();
                }

                if (mFilterListAdapter != null) {
                    mFilterListAdapter.notifyDataSetChanged();
                }

            } else if (mId == R.id.lay_home_filter_close_container) {


                if (getDialog() != null) {
                    getDialog().dismiss();
                }

            } else if (mId == R.id.btn_ar_filter_apply) {

                hideKeyboard();

                if (m__FiltersApplyCall != null) {
                    m__FiltersApplyCall.filtersApplyCall();
                }

                if (getDialog() != null) {

                    getDialog().dismiss();

                }

            }
        }

        public class FilterListAdapter extends RecyclerView.Adapter<FilterListAdapter.ViewHolder> {

            private final ArrayList<FilterListDataSet> mFilter__List;
            private boolean mIsChildVisibled = true;

            public FilterListAdapter(ArrayList<FilterListDataSet> filter__List) {
                this.mFilter__List = filter__List;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.ar_filter_list_row, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {


                String mFilterListRowEnableStatus = mFilter__List.get(position).getStatus();
                if (mFilterListRowEnableStatus != null && mFilterListRowEnableStatus.equals("1")) {

                    holder.mFilterTypeList.setVisibility(View.VISIBLE);
                    holder.mFilterTitle.setVisibility(View.VISIBLE);

                    holder.mFilterTitle.setText(mFilter__List.get(position).getName());
                    holder.mFilterTypeListAdapter = new FilterTypeListAdapter(mFilter__List.get(position), position);
                    holder.mFilterTypeList.setLayoutManager(new LinearLayoutManager(getActivity()));
                    holder.mFilterTypeList.setAdapter(holder.mFilterTypeListAdapter);

                } else {

                    holder.mFilterTypeList.setVisibility(View.GONE);
                    holder.mFilterTitle.setVisibility(View.GONE);

                }

                /*if (mFilter__List.get(position).filterTypeList.size() - 1 == position) {
                    holder.mViewLine.setVisibility(View.GONE);
                } else {
                    holder.mViewLine.setVisibility(View.VISIBLE);
                }*/
                holder.mViewLine.setVisibility(View.GONE);

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
                return mFilter__List.size();
            }

            public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                private TextView mFilterTitle;
                private RecyclerView mFilterTypeList;
                private View mViewLine;
                private FilterTypeListAdapter mFilterTypeListAdapter;

                public ViewHolder(View itemView) {
                    super(itemView);

                    mFilterTitle = itemView.findViewById(R.id.tv_ar_filter_dialog_filter_title);
                    mViewLine = itemView.findViewById(R.id.view_ar_filter_dialog_filter_list);
                    mFilterTypeList = itemView.findViewById(R.id.recycler_ar_filter_dialog_filter_list);

                }

                @Override
                public void onClick(View v) {

                }
            }

            public class FilterTypeListAdapter extends RecyclerView.Adapter<FilterTypeListAdapter.ViewHolder> {

                private final ArrayList<FilterTypeDataSet> mFilterTypeList;
                private FilterListDataSet mFilterListDataSet;
                private RadioButton mTempRadioBtnOptionValue;
                private final CheckBox mTempCheckBoxOptionValue;
                int mParentPosition;

                public FilterTypeListAdapter(FilterListDataSet filterListDataSet, int parentPosition) {

                    this.mFilterTypeList = filterListDataSet.filterTypeList;
                    this.mFilterListDataSet = filterListDataSet;
                    this.mParentPosition = parentPosition;

                    mTempRadioBtnOptionValue = null;
                    mTempCheckBoxOptionValue = null;

                }

                @Override
                public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.ar_filter_type_row, parent, false);
                    return new ViewHolder(view);
                }

                @Override
                public void onBindViewHolder(final ViewHolder holder, final int position) {


                    String mTempParenPosition = String.valueOf(mParentPosition);
                    holder.mParentIndex.setText(mTempParenPosition);

                    String mFilterListRowEnableStatus = mFilterTypeList.get(position).getStatus();
                    String mFilterId = mFilterListDataSet.filter_id;
                    String mFilterType = mFilterListDataSet.type;
                    String mFilterTypeId = mFilterTypeList.get(position).filter_type_id;

                    if (mFilterListRowEnableStatus != null && mFilterListRowEnableStatus.equals("1")) {

                        if (mFilterListDataSet.type.equals("1")) {

                            //Radio button selection type :-

                            //To visible top liner for radio button option :-

                            if (ARFiltersDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                FilterDbDataSet filterDbDs = new FilterDbDataSet();
                                filterDbDs.setmFilterId(mFilterId);
                                filterDbDs.setmFilterTypeId(mFilterTypeId);
                                filterDbDs.setmItemType(mFilterType);
                                if (ARFiltersDBTemp.getInstance(getActivity()).isFilterSelected(filterDbDs)) {
                                    holder.mRadioBtnValue.setChecked(true);
                                    mTempRadioBtnOptionValue = holder.mRadioBtnValue;
                                } else {
                                    holder.mRadioBtnValue.setChecked(false);
                                }
                            } else {
                                holder.mRadioBtnValue.setChecked(false);
                            }

                            holder.mCheckBoxRow.setVisibility(View.GONE);
                            holder.mRadioBtnRow.setVisibility(View.VISIBLE);
                            holder.mRadioBtnName.setText(mFilterTypeList.get(position).getName());

                            holder.mRadioBtnValue.setOnClickListener(new View.OnClickListener() {
                                @Override


                                public void onClick(View v) {

                                    if (mTempRadioBtnOptionValue != null) {
                                        mTempRadioBtnOptionValue.setChecked(false);
                                        mTempRadioBtnOptionValue = null;
                                    }


                                    //Here we need to check this parent filter has any child filter type id in db already.
                                    //If exists then we need to remove the existing child type id then insert the current
                                    //child type id into db :-
                                    if (ARFiltersDBTemp.getInstance(getActivity()).isFilterIdExists(mFilterId)) {
                                        FilterDbDataSet filterDbDs = new FilterDbDataSet();
                                        filterDbDs.setmFilterId(mFilterId);
                                        filterDbDs.setmFilterTypeId(mFilterTypeId);
                                        filterDbDs.setmItemType(mFilterType);
                                        ARFiltersDBTemp.getInstance(getActivity()).removeItemFromFilterList(filterDbDs);
                                    }

                                    if (!ARFiltersDBTemp.getInstance(getActivity()).isFilterTypeIdExists(mFilterId, mFilterTypeId)) {

                                        FilterDbDataSet filterDbDs = new FilterDbDataSet();
                                        filterDbDs.setmFilterId(mFilterId);
                                        filterDbDs.setmFilterTypeId(mFilterTypeId);
                                        filterDbDs.setmItemType(mFilterType);
                                        ARFiltersDBTemp.getInstance(getActivity()).addFilters(filterDbDs);

                                    }


                                    holder.mRadioBtnValue.setChecked(true);
                                    mTempRadioBtnOptionValue = holder.mRadioBtnValue;
                                }
                            });

                        } else if (mFilterListDataSet.type.equals("2")) {

                            //Checkbox selection type :-

                            //To visible top liner for check box option :-

                            // Log.e("2376 ", mFilterTypeList.get(position).name+" - "+position);

                            if (ARFiltersDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                FilterDbDataSet filterDbDs = new FilterDbDataSet();
                                filterDbDs.setmFilterId(mFilterListDataSet.filter_id);
                                filterDbDs.setmFilterTypeId(mFilterTypeList.get(position).filter_type_id);
                                filterDbDs.setmItemType(mFilterListDataSet.type);
                                if (ARFiltersDBTemp.getInstance(getActivity()).isFilterSelected(filterDbDs)) {
                                    holder.mCheckBoxValue.setChecked(true);
                                } else {
                                    holder.mCheckBoxValue.setChecked(false);
                                }
                            } else {
                                holder.mCheckBoxValue.setChecked(false);
                            }

                            holder.mCheckBoxRow.setVisibility(View.VISIBLE);
                            holder.mRadioBtnRow.setVisibility(View.GONE);
                            holder.mCheckBoxName.setText(mFilterTypeList.get(position).getName());

                            holder.mCheckBoxValue.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //  Log.e("2399","called");
                                    if (ARFiltersDBTemp.getInstance(getActivity()).isFilterTypeIdExists(mFilterId, mFilterTypeId)) {
                                        holder.mCheckBoxValue.setChecked(false);
                                        ARFiltersDBTemp.getInstance(getActivity()).deleteFilterTypeId(mFilterId, mFilterTypeId);
                                        //   Log.e(" if - 2405","called");
                                    } else {
                                        // Log.e(" else - 2409","called");
                                        holder.mCheckBoxValue.setChecked(true);
                                        FilterDbDataSet filterDbDs = new FilterDbDataSet();
                                        filterDbDs.setmFilterId(mFilterId);
                                        filterDbDs.setmFilterTypeId(mFilterTypeId);
                                        filterDbDs.setmItemType(mFilterType);
                                        ARFiltersDBTemp.getInstance(getActivity()).addFilters(filterDbDs);
                                    }
                                }
                            });
                        } else {
                            holder.mCheckBoxRow.setVisibility(View.GONE);
                            holder.mRadioBtnRow.setVisibility(View.GONE);
                        }
                    } else {
                        holder.mCheckBoxRow.setVisibility(View.GONE);
                        holder.mRadioBtnRow.setVisibility(View.GONE);
                    }

                    if (position == mFilterTypeList.size() - 1) {
                        holder.mBottomLine.setVisibility(View.GONE);
                    } else {
                        holder.mBottomLine.setVisibility(View.VISIBLE);
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
                    return mFilterTypeList.size();
                }

                public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                    private final LinearLayout mCheckBoxRow;
                    private final LinearLayout mRadioBtnRow;

                    private final CheckBox mCheckBoxValue;
                    private final RadioButton mRadioBtnValue;

                    private final TextView mParentIndex;
                    private final TextView mCheckBoxName;
                    private final TextView mRadioBtnName;
                    private View mBottomLine;

                    public ViewHolder(View itemView) {
                        super(itemView);

                        mCheckBoxRow = itemView.findViewById(R.id.layout_checkbox_ar_f_type_dialog_value_holder);
                        mCheckBoxRow.setVisibility(View.GONE);
                        mRadioBtnRow = itemView.findViewById(R.id.lay_radio_btn_ar_f_type_value_holder);
                        mRadioBtnRow.setVisibility(View.GONE);

                        mCheckBoxName = itemView.findViewById(R.id.tv_ar_f_type_value_name);
                        mRadioBtnName = itemView.findViewById(R.id.tv_ar_f_type_value_name_radio);

                        mCheckBoxValue = itemView.findViewById(R.id.chk_box_ar_f_type_value);
                        mRadioBtnValue = itemView.findViewById(R.id.radio_btn_ar_f_type_value);


                        mParentIndex = itemView.findViewById(R.id.tv_ar_f_type_parent_index);
                        mParentIndex.setVisibility(View.GONE);

                        mBottomLine = itemView.findViewById(R.id.mBottomLine);
                        mBottomLine.setVisibility(View.GONE);

                    }

                    @Override
                    public void onClick(View v) {

                    }
                }

            }
        }


    }


    @Override
    public void filtersApplyCall() {


        ArrayList<FilterDbDataSet> mTempFilterList = ARFiltersDBTemp.getInstance(getActivity()).getFiltersList();
        if (mTempFilterList != null && mTempFilterList.size() > 0) {
            if (ARFiltersDB.getInstance(getActivity()).getSizeOfList() > 0) {
                ARFiltersDB.getInstance(getActivity()).deleteDB();
            }
            for (int cuisine = 0; cuisine < mTempFilterList.size(); cuisine++) {
                FilterDbDataSet filterDbDataSet = mTempFilterList.get(cuisine);
                ARFiltersDB.getInstance(getActivity()).addFilters(filterDbDataSet);
            }
        } else {
            //this case will occur , if ARFiltersDBTemp has some values then press clear all then press
            //apply button now definitely ARFiltersDBTemp is empty .So delete the ARFiltersDB
            //  list to empty.!.
            //Just because user wants clear all filters.!
            if (ARFiltersDB.getInstance(getActivity()).getSizeOfList() > 0) {
                ARFiltersDB.getInstance(getActivity()).deleteDB();
            }
        }
        ARFiltersDBTemp.getInstance(getActivity()).deleteDB();


        callVendorListAPi();

    }


    // ******************** filter bottom sheet end ************************************************


}