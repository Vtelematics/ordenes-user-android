package com.ordenese.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.ordenese.ApiConnection.ApiClient;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.CuisinesApi;
import com.ordenese.DataSets.CuisinesDataSet;
import com.ordenese.DataSets.FilterDataSet;
import com.ordenese.DataSets.FilterDbDataSet;
import com.ordenese.DataSets.FilterListApi;
import com.ordenese.DataSets.FilterListDataSet;
import com.ordenese.DataSets.FilterTypeDataSet;
import com.ordenese.DataSets.VendorFilterApi;
import com.ordenese.DataSets.VendorFilterDataSet;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.CFCuisinesDB;
import com.ordenese.Databases.CFCuisinesDBTemp;
import com.ordenese.Databases.CFFiltersDB;
import com.ordenese.Databases.CFFiltersDBTemp;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Databases.WishListDB;
import com.ordenese.Interfaces.CuisinesApplyCall;
import com.ordenese.Interfaces.FiltersApplyCall;
import com.ordenese.Interfaces.RestaurantListLoadListener;
import com.ordenese.R;
import com.ordenese.databinding.CuisinesListBottomSheetBinding;
import com.ordenese.databinding.FilterListBottomSheetBinding;
import com.ordenese.databinding.FragmentCategoryFilterBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFilter extends Fragment implements View.OnClickListener, CuisinesApplyCall, FiltersApplyCall {

    private RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;
    private RecyclerView.LayoutManager mRestaurantARParentLayoutMgr;
    private ARParentListAdapter mArParentListAdapter;

    private CuisinesApi mCuisinesApi;
    private int mPageCount = 1;
    private int mListTotals = 0;
    private ArrayList<CuisinesDataSet> mCuisineIdsGlobal;
    FragmentCategoryFilterBinding binding;

    String mFilterName = "", mFilterKey = "";
    private VendorFilterApi mVendorFilterApi;
    private CuisinesApplyCall mCuisinesApplyCall;
    private FilterListApi mFilterListApi;
    private FiltersApplyCall mFiltersApplyCall;
    Activity activity;

    public CategoryFilter() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryFilterBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        activity = getActivity();

        mCuisinesApplyCall = this;
        mFiltersApplyCall = this;

        if (getArguments() != null) {
            mFilterName = getArguments().getString(DefaultNames.filter_name);
            mFilterKey = getArguments().getString(DefaultNames.filter_key);
            binding.filterName.setText(mFilterName);
        }

        binding.cartBtn.setVisibility(View.GONE);
        binding.cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                CartList m_cartList = new CartList();
                mFT.replace(R.id.layout_app_home_body, m_cartList, "m_cartList");
                mFT.addToBackStack("m_cartList");
                mFT.commit();
            }
        });

        binding.wishlist.setOnClickListener(new View.OnClickListener() {
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

        binding.layAllRFilters.setOnClickListener(this);
        binding.layAllRCuisines.setOnClickListener(this);
        binding.layAllRSearch.setOnClickListener(this);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });


        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCuisineIdsGlobal = CFCuisinesDB.getInstance(getActivity()).getCuisineList();
        callVendorFiltersListAPi();
        cart_product_count();
    }

    @Override
    public void onStop() {
        super.onStop();
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
                                        binding.cartBtn.setVisibility(View.VISIBLE);
                                        binding.cartCount.setText(object.getString("qty_count"));
                                    } else {
                                        binding.cartBtn.setVisibility(View.GONE);
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


    private void callVendorFiltersListAPi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();

                    jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                    jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.page, 1);
                    mPageCount = 1;
                    jsonObject.put(DefaultNames.page_per_unit, 10);
                    jsonObject.put(DefaultNames.filter, mFilterKey);
                    JSONArray cuisineJsonArray = new JSONArray();

                    if (mCuisineIdsGlobal != null && mCuisineIdsGlobal.size() > 0) {
                        for (int cuisineId = 0; cuisineId < mCuisineIdsGlobal.size(); cuisineId++) {
                            cuisineJsonArray.put(cuisineId, mCuisineIdsGlobal.get(cuisineId).getCuisine_id());
                        }
                    }
                    jsonObject.put(DefaultNames.cuisine, cuisineJsonArray);


                    ArrayList<FilterDbDataSet> mFilterDbList = CFFiltersDB.getInstance(getActivity()).getFiltersList();
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
                    Call<VendorFilterApi> Call = retrofitInterface.vendorFiltersApi(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<VendorFilterApi>() {
                        @Override
                        public void onResponse(@NonNull Call<VendorFilterApi> call, @NonNull Response<VendorFilterApi> response) {

                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                mVendorFilterApi = response.body();
                                if (mVendorFilterApi != null) {
                                    if (mVendorFilterApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            //mListTotals
                                            mRestaurantARParentLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                            binding.recyclerAllRestaurantParent.setLayoutManager(mRestaurantARParentLayoutMgr);
                                            mArParentListAdapter = new ARParentListAdapter(mVendorFilterApi, binding.nestedView);
                                            binding.recyclerAllRestaurantParent.setAdapter(mArParentListAdapter);

                                            if (mVendorFilterApi.vendorList != null && mVendorFilterApi.vendorList.size() == 0) {
                                                binding.layoutRestaurantMenuItemListEmptyMessage.setVisibility(View.VISIBLE);
                                            } else {
                                                binding.layoutRestaurantMenuItemListEmptyMessage.setVisibility(View.GONE);
                                            }
                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mVendorFilterApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mVendorFilterApi.error.message);
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
                        public void onFailure(@NonNull Call<VendorFilterApi> call, @NonNull Throwable t) {
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
    public void onClick(View v) {

        int mId = v.getId();
        if (mId == R.id.lay_all_r_filters) {

            if (CFFiltersDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                CFFiltersDBTemp.getInstance(getActivity()).deleteDB();
            }
            ArrayList<FilterDbDataSet> mExistingFilterList = CFFiltersDB.getInstance(getActivity()).getFiltersList();
            if (mExistingFilterList != null && mExistingFilterList.size() > 0) {
                for (int filter = 0; filter < mExistingFilterList.size(); filter++) {
                    FilterDbDataSet filterDbDataSet = mExistingFilterList.get(filter);
                    CFFiltersDBTemp.getInstance(getActivity()).addFilters(filterDbDataSet);
                }
            }

            callSideFilterListAPi();

        } else if (mId == R.id.lay_all_r_cuisines) {
            // AppFunctions.toastShort(getActivity(), "Cuisines");
            if (CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                CFCuisinesDBTemp.getInstance(getActivity()).deleteCuisinesDB();
            }
            ArrayList<CuisinesDataSet> mExistingCuisinesList = CFCuisinesDB.getInstance(getActivity()).getCuisineList();
            if (mExistingCuisinesList != null && mExistingCuisinesList.size() > 0) {
                for (int cuisine = 0; cuisine < mExistingCuisinesList.size(); cuisine++) {
                    CuisinesDataSet cuisinesDataSet = mExistingCuisinesList.get(cuisine);
                    CFCuisinesDBTemp.getInstance(getActivity()).addCuisines(cuisinesDataSet);
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

        } else if (mId == R.id.lay_ar_filter_or_cuisines_transparent_bg) {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            // To get the correct window token, lets first get the currently focused view
            // View v__iew = mAllRestaurantsBinding.edtArCuisinesSearch.getRootView();
            View v__iew = binding.getRoot();
            // To get the window token when there is no currently focused view, we have a to create a view
            if (v__iew == null) {
                v__iew = new View(getActivity());
            }
            // hide the keyboard
            imm.hideSoftInputFromWindow(v__iew.getWindowToken(), 0);


        }


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


    public class ARParentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private VendorFilterApi m_Vendor_FilterApi;

        private Handler mBannerHandler = new Handler();
        private Runnable mBannerRunnable;

        private int mBannerCurrentPosition, mBannerPosition;
        private Interpolator mHomeInterpolator = new AccelerateInterpolator();

        private Boolean mLoading = false;
        private int lastVisibleItem, totalItemCount;
        private int visibleThreshold = 10;
        private RestaurantListLoadListener mRestaurantListLoadListener;
        NestedScrollView nested_view;

        public ARParentListAdapter(VendorFilterApi vendorFilterApi, NestedScrollView nested_view) {
            this.m_Vendor_FilterApi = vendorFilterApi;
            this.nested_view = nested_view;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(getActivity()).inflate(R.layout.filter_restaurant, parent, false);
            return new ListViewHolder(view);
            //return new ListViewHolder(AllRestaurantParentRowBinding.inflate(getLayoutInflater()));

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            ListViewHolder mListHolder = (ListViewHolder) holder;

            if (m_Vendor_FilterApi.vendorList != null && m_Vendor_FilterApi.vendorList.size() > 0) {

                mListHolder.recyclerAllRestaurants.setVisibility(View.VISIBLE);
                // mListHolder.mAllRestaurantsTitle.setVisibility(View.VISIBLE);

                mListHolder.mVendorListLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                mListHolder.recyclerAllRestaurants.setLayoutManager(mListHolder.mVendorListLayoutMgr);

                mListHolder.mAllRestaurantsListAdapter = new AllRestaurantsListAdapter(m_Vendor_FilterApi.vendorList, mListHolder.recyclerAllRestaurants);
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
                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                                            jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                                            jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());
                                            jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                                            jsonObject.put(DefaultNames.page, mPageCount);
                                            jsonObject.put(DefaultNames.filter, mFilterKey);
                                            jsonObject.put(DefaultNames.page_per_unit, 10);

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
                                            Call<VendorFilterApi> Call = retrofitInterface.vendorFiltersApi(body);
                                            mProgressDialog.show();
                                            Call.enqueue(new Callback<VendorFilterApi>() {
                                                @SuppressLint("NotifyDataSetChanged")
                                                @Override
                                                public void onResponse(@NonNull Call<VendorFilterApi> call, @NonNull Response<VendorFilterApi> response) {

                                                    mProgressDialog.cancel();

                                                    if (response.isSuccessful()) {

                                                        VendorFilterApi m_Vendor_LA_Pagination = response.body();
                                                        if (m_Vendor_LA_Pagination != null) {

                                                            if (m_Vendor_LA_Pagination.success != null) {
                                                                //Api response successDataSet :-
                                                                if (getActivity() != null) {

                                                                    if (m_Vendor_LA_Pagination.vendorList != null) {
                                                                        if (m_Vendor_LA_Pagination.vendorList.size() > 0) {

                                                                            //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                                                                            m_Vendor_FilterApi.vendorList.addAll(m_Vendor_LA_Pagination.vendorList);
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
                                                public void onFailure(@NonNull Call<VendorFilterApi> call, @NonNull Throwable t) {
                                                    mProgressDialog.cancel();
                                                }
                                            });
                                        } catch (JSONException e) {
                                            mProgressDialog.cancel();
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
                mListHolder.recyclerAllRestaurants.setVisibility(View.GONE);
                // mListHolder.mAllRestaurantsTitle.setVisibility(View.GONE);
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
            private RecyclerView recyclerAllRestaurants;
            //  private TextView mAllRestaurantsTitle;
            NestedScrollView nested_view;

            ListViewHolder(View view) {
                super(view);

                //   mAllRestaurantsTitle = view.findViewById(R.id.tv_ar_all_restaurants_title);
                recyclerAllRestaurants = view.findViewById(R.id.recycler_ar_vendor_list);
                nested_view = view.findViewById(R.id.nested_view);
                mFiltersListLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
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
                        /*if(mTopPicksId.equals("2")){
                            //All shops :-
                            toPerformAllShops(position);
                        }*/
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
                        /*if(mTopPicksId.equals("2")){
                            //All shops :-
                            toPerformAllShops(position);
                        }*/
                        toPerformAllShops(position);
                    }
                });


            }

            private void toPerformAllShops(int position) {

                //   AppFunctions.toastShort(getActivity(), mFilterList.get(position).getName());

                if (getActivity() != null) {
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    CategoryFilter categoryFilter = new CategoryFilter();
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
                private LinearLayout mTopPicksRowLay;

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


        class AllRestaurantsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private ArrayList<VendorFilterDataSet> mVendorList;

            RecyclerView recyclerViewVendorList;

            AllRestaurantsListAdapter(ArrayList<VendorFilterDataSet> vendorList, RecyclerView recyclerViewVendorList) {

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
                //Glide.with(getActivity()).load(R.drawable.x_best_offer_sample_1).into(restaurantViewHolder.iv_BestOfferImage);
                restaurantViewHolder.tv_restaurant_sub_content.setText(mVendorList.get(position).getCuisines());
                String mVendorStatus = mVendorList.get(position).getVendor_status();

                if (OrderTypeDB.getInstance(getActivity()).getUserServiceType().equals("2")) {
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
                                toast_layout(activity.getResources().getString(R.string.removed_from_the_wishlist), binding.wishlistLayout);
                                WishListDB.getInstance(activity).removeFromFavouriteList(mVendorList.get(position).getVendor_id());
                                restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_border_primary_color_24dp));
                            } else {
                                toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), binding.wishlistLayout);
                                WishListDB.getInstance(activity).add_vendor_id(mVendorList.get(position).getVendor_id());
                                restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                            }
                        } else {
                            toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), binding.wishlistLayout);
                            WishListDB.getInstance(activity).add_vendor_id(mVendorList.get(position).getVendor_id());
                            restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                        }
                    }
                });

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

                restaurantViewHolder.tv_ar_minimum_amount.setText(activity.getResources().getString(R.string.min) + " - " +
                        mVendorList.get(position).minimum_amount);

                if (mVendorList.get(position).free_delivery.equals("1")) {
                    restaurantViewHolder.tv_delivery_amount.setVisibility(View.GONE);
                    restaurantViewHolder.tv_delivery_amount_title.setVisibility(View.VISIBLE);
                    restaurantViewHolder.tv_delivery_amount_title.setText(activity.getResources().getString(R.string.ar_filter_do_free_delivery));
                } else {
                    restaurantViewHolder.tv_delivery_amount.setVisibility(View.VISIBLE);
                    restaurantViewHolder.tv_delivery_amount.setText(mVendorList.get(position).getDelivery_charge());
                    restaurantViewHolder.tv_delivery_amount_title.setVisibility(View.GONE);
//                    restaurantViewHolder.tv_delivery_amount_title.setText(activity.getResources().getString(R.string.ar_delivery));
                }

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
        binding.dataText.setText(string);
        binding.viewAll.setOnClickListener(new View.OnClickListener() {
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


/*
    private class CuisinesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final ArrayList<CuisinesDataSet> mCuisinesList;

        CuisinesListAdapter(ArrayList<CuisinesDataSet> cuisinesList) {
            this.mCuisinesList = cuisinesList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new CuisinesListViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.rc_cuisines_list_row, viewGroup, false));

        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            CuisinesListViewHolder mRCListViewHolder = (CuisinesListViewHolder) viewHolder;

            if (CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                if (CFCuisinesDBTemp.getInstance(getActivity()).isCuisineSelected(mCuisinesList.get(position))) {
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
                                                                                 CFCuisinesDBTemp.getInstance(getActivity()).addCuisines(cuisinesDataSet);
                                                                             } else {
                                                                                 //cuisine un selected :-
                                                                                 //Log.e("else", "cuisine un selected + " + position);
                                                                                 if (CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                                                                     if (CFCuisinesDBTemp.getInstance(getActivity()).isCuisineSelected(mCuisinesList.get(position))) {
                                                                                         CFCuisinesDBTemp.getInstance(getActivity()).removeItemFromCuisineList(mCuisinesList.get(position));
                                                                                     }
                                                                                 }
                                                                             }

                                                                             //ar_cuisines_selected
                                                                             //btn_ar_selected_cuisines_apply
                                                                             if (CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                                                                 String mSelectedCuisines = CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() + " " +
                                                                                         getActivity().getResources().getString(R.string.ar_cuisines_selected);
                                                                                 binding.btnArSelectedCuisinesApply.setVisibility(View.VISIBLE);
                                                                                 binding.btnArSelectedCuisinesApply.setText(mSelectedCuisines);
                                                                             } else {
                                                                                 binding.btnArSelectedCuisinesApply.setVisibility(View.GONE);
                                                                                 binding.btnArSelectedCuisinesApply.setText("");
                                                                             }

                                                                         }
                                                                     }
            );


        }


        @Override
        public int getItemCount() {
            return mCuisinesList.size();
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
*/

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

                                if (CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                    String mSelectedCuisines = CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() + " " +
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

                if (CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                    String mSelectedCuisines = CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() + " " +
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

                if (CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                    CFCuisinesDBTemp.getInstance(getActivity()).deleteCuisinesDB();
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

                            if (CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                String mSelectedCuisines = CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() + " " +
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

                    if (CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                        if (CFCuisinesDBTemp.getInstance(getActivity()).isCuisineSelected(mCuisinesList.get(position))) {
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
                                                                                         CFCuisinesDBTemp.getInstance(getActivity()).addCuisines(cuisinesDataSet);
                                                                                     } else {
                                                                                         //cuisine un selected :-
                                                                                         //Log.e("else", "cuisine un selected + " + position);
                                                                                         if (CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                                                                             if (CFCuisinesDBTemp.getInstance(getActivity()).isCuisineSelected(mCuisinesList.get(position))) {
                                                                                                 CFCuisinesDBTemp.getInstance(getActivity()).removeItemFromCuisineList(mCuisinesList.get(position));
                                                                                             }
                                                                                         }
                                                                                     }

                                                                                     //ar_cuisines_selected
                                                                                     //btn_ar_selected_cuisines_apply
                                                                                     if (CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                                                                         String mSelectedCuisines = CFCuisinesDBTemp.getInstance(getActivity()).getSizeOfList() + " " +
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


        ArrayList<CuisinesDataSet> mTempCuisinesList = CFCuisinesDBTemp.getInstance(getActivity()).getCuisineList();
        if (mTempCuisinesList != null && mTempCuisinesList.size() > 0) {
            if (CFCuisinesDB.getInstance(getActivity()).getSizeOfList() > 0) {
                CFCuisinesDB.getInstance(getActivity()).deleteCuisinesDB();
            }
            for (int cuisine = 0; cuisine < mTempCuisinesList.size(); cuisine++) {
                CuisinesDataSet cuisinesDataSet = mTempCuisinesList.get(cuisine);
                CFCuisinesDB.getInstance(getActivity()).addCuisines(cuisinesDataSet);
            }
        } else {
            //this case will occur , if CFCuisinesDBTemp has some values then press clear all then press
            //apply button now definitely CFCuisinesDBTemp is empty .So delete the CFCuisinesDB and mCuisineIdsGlobal
            //  list to empty.!.
            //Just because user wants clear all cuisines.!
            if (CFCuisinesDB.getInstance(getActivity()).getSizeOfList() > 0) {
                CFCuisinesDB.getInstance(getActivity()).deleteCuisinesDB();
                mCuisineIdsGlobal = new ArrayList<>();
            }
        }
        CFCuisinesDBTemp.getInstance(getActivity()).deleteCuisinesDB();
        if (CFCuisinesDB.getInstance(getActivity()).getSizeOfList() > 0) {
            mCuisineIdsGlobal = CFCuisinesDB.getInstance(getActivity()).getCuisineList();
        }
        callVendorFiltersListAPi();


    }


    //  ************************  Filter list bottom sheet  start **********************************
    //**************   @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  **********************

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

                if (CFFiltersDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                    CFFiltersDBTemp.getInstance(getActivity()).deleteDB();
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
                    Log.e("1919", "filtersApplyCall() - if");
                    m__FiltersApplyCall.filtersApplyCall();
                } else {
                    Log.e("1922", "filtersApplyCall() - else");
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
                return new FilterListAdapter.ViewHolder(view);
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

                            if (CFFiltersDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                FilterDbDataSet filterDbDs = new FilterDbDataSet();
                                filterDbDs.setmFilterId(mFilterId);
                                filterDbDs.setmFilterTypeId(mFilterTypeId);
                                filterDbDs.setmItemType(mFilterType);
                                if (CFFiltersDBTemp.getInstance(getActivity()).isFilterSelected(filterDbDs)) {
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
                                    if (CFFiltersDBTemp.getInstance(getActivity()).isFilterIdExists(mFilterId)) {
                                        FilterDbDataSet filterDbDs = new FilterDbDataSet();
                                        filterDbDs.setmFilterId(mFilterId);
                                        filterDbDs.setmFilterTypeId(mFilterTypeId);
                                        filterDbDs.setmItemType(mFilterType);
                                        CFFiltersDBTemp.getInstance(getActivity()).removeItemFromFilterList(filterDbDs);
                                    }

                                    if (!CFFiltersDBTemp.getInstance(getActivity()).isFilterTypeIdExists(mFilterId, mFilterTypeId)) {

                                        FilterDbDataSet filterDbDs = new FilterDbDataSet();
                                        filterDbDs.setmFilterId(mFilterId);
                                        filterDbDs.setmFilterTypeId(mFilterTypeId);
                                        filterDbDs.setmItemType(mFilterType);
                                        CFFiltersDBTemp.getInstance(getActivity()).addFilters(filterDbDs);

                                    }


                                    holder.mRadioBtnValue.setChecked(true);
                                    mTempRadioBtnOptionValue = holder.mRadioBtnValue;
                                }
                            });

                        } else if (mFilterListDataSet.type.equals("2")) {

                            //Checkbox selection type :-

                            //To visible top liner for check box option :-

                            //  Log.e("2376 ", mFilterTypeList.get(position).name+" - "+position);

                            if (CFFiltersDBTemp.getInstance(getActivity()).getSizeOfList() > 0) {
                                FilterDbDataSet filterDbDs = new FilterDbDataSet();
                                filterDbDs.setmFilterId(mFilterListDataSet.filter_id);
                                filterDbDs.setmFilterTypeId(mFilterTypeList.get(position).filter_type_id);
                                filterDbDs.setmItemType(mFilterListDataSet.type);
                                if (CFFiltersDBTemp.getInstance(getActivity()).isFilterSelected(filterDbDs)) {
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

                                    if (CFFiltersDBTemp.getInstance(getActivity()).isFilterTypeIdExists(mFilterId, mFilterTypeId)) {
                                        holder.mCheckBoxValue.setChecked(false);
                                        CFFiltersDBTemp.getInstance(getActivity()).deleteFilterTypeId(mFilterId, mFilterTypeId);

                                        //   Log.e(" if - 2405","called");

                                    } else {

                                        // Log.e(" else - 2409","called");

                                        holder.mCheckBoxValue.setChecked(true);

                                        FilterDbDataSet filterDbDs = new FilterDbDataSet();
                                        filterDbDs.setmFilterId(mFilterId);
                                        filterDbDs.setmFilterTypeId(mFilterTypeId);
                                        filterDbDs.setmItemType(mFilterType);
                                        CFFiltersDBTemp.getInstance(getActivity()).addFilters(filterDbDs);

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

        Log.e("2259", "filtersApplyCall()");

        ArrayList<FilterDbDataSet> mTempFilterList = CFFiltersDBTemp.getInstance(getActivity()).getFiltersList();
        if (mTempFilterList != null && mTempFilterList.size() > 0) {
            if (CFFiltersDB.getInstance(getActivity()).getSizeOfList() > 0) {
                CFFiltersDB.getInstance(getActivity()).deleteDB();
            }
            for (int cuisine = 0; cuisine < mTempFilterList.size(); cuisine++) {
                FilterDbDataSet filterDbDataSet = mTempFilterList.get(cuisine);
                CFFiltersDB.getInstance(getActivity()).addFilters(filterDbDataSet);
            }
        } else {
            //this case will occur , if CFFiltersDBTemp has some values then press clear all then press
            //apply button now definitely CFFiltersDBTemp is empty .So delete the CFFiltersDB
            //  list to empty.!.
            //Just because user wants clear all filters.!
            if (CFFiltersDB.getInstance(getActivity()).getSizeOfList() > 0) {
                CFFiltersDB.getInstance(getActivity()).deleteDB();
            }
        }
        CFFiltersDBTemp.getInstance(getActivity()).deleteDB();


        callVendorFiltersListAPi();

    }


    //**************   @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  **********************
    //************************** Filter list bottom sheet end  *************************************

}