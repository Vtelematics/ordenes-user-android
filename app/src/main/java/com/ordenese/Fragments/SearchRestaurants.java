package com.ordenese.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.imageview.ShapeableImageView;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.SearchVendorDataSet;
import com.ordenese.DataSets.VendorSearchApi;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.WishListDB;
import com.ordenese.Interfaces.RestaurantListLoadListener;
import com.ordenese.R;
import com.ordenese.databinding.SearchRestaurantsBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchRestaurants extends Fragment implements View.OnClickListener {

    private SearchRestaurantsBinding mSRBinding;

    private ProgressDialog mProgressDialog;
    private RetrofitInterface retrofitInterface;
    private int mPageCount = 1;
    int mListTotals = 0;
    private VendorSearchApi mVendorSearchApi;
    private RecyclerView.LayoutManager mSearchVListLayMgr;
    private SearchVendorListAdapter mSearchVendorListAdapter;

    private Boolean mLoading = false;
    private int lastVisibleItem, totalItemCount;
    int visibleThreshold = 10;
    private RestaurantListLoadListener mRestaurantListLoadListener;
    Activity activity;

    private String mCurrentSearchText = "";
    long mSearchDelay = 1000; // 1 seconds after user stops typing
    private long mSearchLastTextEdit = 0;
    Handler mSearchHandler = new Handler();
    Runnable mSearchInputFinishChecker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (mSearchLastTextEdit + mSearchDelay - 500)) {
                if (mCurrentSearchText.length() >= 1) {
                    callSearchVendorListAPi(mCurrentSearchText);
                }
            }
        }
    };

    public SearchRestaurants() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.search_restaurants, container, false);
        mSRBinding = SearchRestaurantsBinding.inflate(inflater, container, false);

        activity = getActivity();

        mSRBinding.imgSrBack.setOnClickListener(this);
        mSRBinding.laySrRestaurantsSearchClear.setOnClickListener(this);
        mSRBinding.tvSrRestaurantsListEmpty.setVisibility(View.GONE);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mSRBinding.laySrRestaurantsSearchClear.setVisibility(View.INVISIBLE);
        mSRBinding.edtSrRestaurantsSearch.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                if (getActivity() != null) {
                    if (s.length() == 0) {
                        mSRBinding.laySrRestaurantsSearchClear.setVisibility(View.INVISIBLE);
                    } else {
                        mSRBinding.laySrRestaurantsSearchClear.setVisibility(View.VISIBLE);
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

        mSRBinding.nestedView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (v.getChildAt(v.getChildCount() - 1) != null) {
                    if (scrollY > oldScrollY) {
                        if (scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) {
                            //code to fetch more data for endless scrolling
                            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mSRBinding.recyclerSrRestaurantsList.getLayoutManager();
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

        return mSRBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();
        if (mId == R.id.img_sr_back) {
            FragmentManager mFragmentManager = getParentFragmentManager();
            mFragmentManager.popBackStack();
        } else if (mId == R.id.lay_sr_restaurants_search_clear) {
            mSRBinding.edtSrRestaurantsSearch.setText("");
            mSRBinding.tvSrRestaurantsListEmpty.setVisibility(View.GONE);
        }
    }

    private void toHideDeviceKeyboard() {
        if (getActivity() != null) {
            if (mSRBinding != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                // To get the correct window token, lets first get the currently focused view
                View v__iew = mSRBinding.getRoot();
                // To get the window token when there is no currently focused view, we have a to create a view
                if (v__iew == null) {
                    v__iew = new View(getActivity());
                }
                // hide the keyboard
                imm.hideSoftInputFromWindow(v__iew.getWindowToken(), 0);
            }
        }
    }

    private void callSearchVendorListAPi(String searchText) {

        if (getActivity() != null) {

            toHideDeviceKeyboard();

            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                    jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                    jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());
                    // jsonObject.put(DefaultNames.order_type, UserServiceTypeDB.getInstance(getActivity()).getUserServiceType());
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.page, 1);
                    mPageCount = 1;
                    jsonObject.put(DefaultNames.page_per_unit, 10);
                    // JSONArray cuisineJsonArray = new JSONArray();
                    //jsonObject.put(DefaultNames.cuisine, cuisineJsonArray);
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));
                    jsonObject.put(DefaultNames.search, searchText);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<VendorSearchApi> Call = retrofitInterface.searchRestaurantsList(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<VendorSearchApi>() {
                        @Override
                        public void onResponse(@NonNull Call<VendorSearchApi> call, @NonNull Response<VendorSearchApi> response) {

                            mProgressDialog.cancel();

                            if (response.isSuccessful()) {
                                mVendorSearchApi = response.body();
                                if (mVendorSearchApi != null) {
                                    if (mVendorSearchApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                       /* mRestaurantARParentLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                        mAllRestaurantsBinding.recyclerAllRestaurantParent.setLayoutManager(mRestaurantARParentLayoutMgr);
                                        mArParentListAdapter = new ARParentListAdapter(mVendorSearchApi);
                                        mAllRestaurantsBinding.recyclerAllRestaurantParent.setAdapter(mArParentListAdapter);*/

                                            if (mVendorSearchApi.searchVendorList != null && mVendorSearchApi.searchVendorList.size() > 0) {

                                                mSRBinding.recyclerSrRestaurantsList.setVisibility(View.VISIBLE);
                                                mSRBinding.tvSrRestaurantsListEmpty.setVisibility(View.GONE);

                                                mSearchVListLayMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                                mSRBinding.recyclerSrRestaurantsList.setLayoutManager(mSearchVListLayMgr);
                                                mSearchVendorListAdapter = new SearchVendorListAdapter(mVendorSearchApi.searchVendorList,
                                                        mSRBinding.recyclerSrRestaurantsList);
                                                mSRBinding.recyclerSrRestaurantsList.setAdapter(mSearchVendorListAdapter);

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
                                                                            // jsonObject.put(DefaultNames.order_type, UserServiceTypeDB.getInstance(getActivity()).getUserServiceType());
                                                                            jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                                                                            jsonObject.put(DefaultNames.page, mPageCount);
                                                                            jsonObject.put(DefaultNames.page_per_unit, 10);
                                                                            // JSONArray cuisineJsonArray = new JSONArray();
                                                                            //jsonObject.put(DefaultNames.cuisine, cuisineJsonArray);
                                                                            jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                                                                            jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                                                                            jsonObject.put(DefaultNames.search, searchText);
                                                                            jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                                                                            RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                                                                            retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                                                                            Call<VendorSearchApi> Call = retrofitInterface.searchRestaurantsList(body);
                                                                            mProgressDialog.show();
                                                                            Call.enqueue(new Callback<VendorSearchApi>() {
                                                                                @SuppressLint("NotifyDataSetChanged")
                                                                                @Override
                                                                                public void onResponse(@NonNull Call<VendorSearchApi> call, @NonNull Response<VendorSearchApi> response) {
                                                                                    mProgressDialog.cancel();
                                                                                    if (response.isSuccessful()) {
                                                                                        VendorSearchApi m_Vendor_LA_Pagination = response.body();
                                                                                        if (m_Vendor_LA_Pagination != null) {
                                                                                            if (m_Vendor_LA_Pagination.success != null) {
                                                                                                //Api response successDataSet :-
                                                                                                if (getActivity() != null) {
                                                                                                    if (m_Vendor_LA_Pagination.searchVendorList != null) {
                                                                                                        if (m_Vendor_LA_Pagination.searchVendorList.size() > 0) {
                                                                                                            //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
                                                                                                            mVendorSearchApi.searchVendorList.addAll(m_Vendor_LA_Pagination.searchVendorList);
                                                                                                            mSearchVendorListAdapter.notifyDataSetChanged();
                                                                                                            setLoaded();
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
                                                                                public void onFailure(@NonNull Call<VendorSearchApi> call, @NonNull Throwable t) {
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

                                            } else {
                                                mSRBinding.recyclerSrRestaurantsList.setVisibility(View.GONE);
                                                mSRBinding.tvSrRestaurantsListEmpty.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mVendorSearchApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mVendorSearchApi.error.message);
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
                        public void onFailure(@NonNull Call<VendorSearchApi> call, @NonNull Throwable t) {
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

    public void setOnLoadMoreRestaurants(RestaurantListLoadListener restaurantListLoadListener) {
        this.mRestaurantListLoadListener = restaurantListLoadListener;

    }

    public void setLoaded() {
        mLoading = false;
    }

    private class SearchVendorListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<SearchVendorDataSet> mVendorList;

        SearchVendorListAdapter(ArrayList<SearchVendorDataSet> vendorList, RecyclerView recyclerViewVendorList) {
            this.mVendorList = vendorList;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new RestaurantViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.rc_search_vendor_list_row, parent, false));
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            RestaurantViewHolder restaurantViewHolder = (RestaurantViewHolder) holder;

            restaurantViewHolder.tv_RestaurantTitle.setText(mVendorList.get(position).getName());
            AppFunctions.imageLoaderUsingGlide(mVendorList.get(position).getLogo(), restaurantViewHolder.iv_vendorImage, getActivity());
            //Glide.with(getActivity()).load(R.drawable.x_best_offer_sample_1).into(restaurantViewHolder.iv_BestOfferImage);
//            restaurantViewHolder.tv_restaurant_sub_content.setText("Breakfast");

            if (OrderTypeDB.getInstance(getActivity()).getUserServiceType().equals("2")) {
                restaurantViewHolder.mDeliveryAmtContainer.setVisibility(View.GONE);
                restaurantViewHolder.mPickupContainer.setVisibility(View.VISIBLE);
                String mDTime = mVendorList.get(position).getDelivery_time()
                        + " " + getActivity().getResources().getString(R.string.mins) ;
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
                            toast_layout(activity.getResources().getString(R.string.removed_from_the_wishlist), mSRBinding.wishlistLayout);
                            WishListDB.getInstance(activity).removeFromFavouriteList(mVendorList.get(position).getVendor_id());
                            restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_border_primary_color_24dp));
                        } else {
                            toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), mSRBinding.wishlistLayout);
                            WishListDB.getInstance(activity).add_vendor_id(mVendorList.get(position).getVendor_id());
                            restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                        }
                    } else {
                        toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), mSRBinding.wishlistLayout);
                        WishListDB.getInstance(activity).add_vendor_id(mVendorList.get(position).getVendor_id());
                        restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                    }
                }
            });

            String mVendorStatus = mVendorList.get(position).getVendor_status();
            //1 -  open
            //2 -  busy
            // 0 -  closed
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

                if (mVendorList.get(position).getsVRatingDataSet() != null) {
                    String mCRating = mVendorList.get(position).getsVRatingDataSet().getRating();
                    if (mCRating != null && !mCRating.isEmpty()) {
                        restaurantViewHolder.tv_rating_statement.setText(mCRating);
                        restaurantViewHolder.rating_linear.setVisibility(View.VISIBLE);
                        AppFunctions.imageLoaderUsingGlide(mVendorList.get(position).getsVRatingDataSet().vendor_rating_image,
                                restaurantViewHolder.rating_img, activity);
                    } else {
                        restaurantViewHolder.tv_rating_statement.setText("0");
                        restaurantViewHolder.rating_linear.setVisibility(View.GONE);
                    }
                } else {
                    restaurantViewHolder.tv_rating_statement.setText("0");
                    restaurantViewHolder.rating_linear.setVisibility(View.GONE);
                }
            }
            restaurantViewHolder.tv_ar_minimum_amount.setText(activity.getResources().getString(R.string.min) + " - " + mVendorList.get(position).minimum_amount);

            if (mVendorList.get(position).free_delivery.equals("1")) {
                restaurantViewHolder.tv_delivery_amount_title.setVisibility(View.VISIBLE);
                restaurantViewHolder.tv_delivery_amount.setVisibility(View.GONE);
                restaurantViewHolder.tv_delivery_amount_title.setText(activity.getResources().getString(R.string.ar_filter_do_free_delivery));
            } else {
                restaurantViewHolder.tv_delivery_amount.setVisibility(View.VISIBLE);
                restaurantViewHolder.tv_delivery_amount_title.setVisibility(View.GONE);
                restaurantViewHolder.tv_delivery_amount.setText(mVendorList.get(position).getDelivery_charge());
            }
            String m_OfferData = mVendorList.get(position).getOffer();
            if (m_OfferData != null && !m_OfferData.isEmpty()) {
                restaurantViewHolder.mLayVLOfferContainer.setVisibility(View.VISIBLE);
                restaurantViewHolder.empty.setVisibility(View.VISIBLE);
//                restaurantViewHolder.iv_Dot.setVisibility(View.VISIBLE);
                restaurantViewHolder.tv_offerContent.setText(m_OfferData);
            } else {
                restaurantViewHolder.mLayVLOfferContainer.setVisibility(View.GONE);
                restaurantViewHolder.empty.setVisibility(View.GONE);
//                restaurantViewHolder.iv_Dot.setVisibility(View.GONE);
                restaurantViewHolder.tv_offerContent.setText("");
            }
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
                iv_vendorImage = itemView.findViewById(R.id.iv_sr_restaurant_image);
                tv_RestaurantTitle = itemView.findViewById(R.id.tv_sr_restaurant_title);
                tv_restaurant_sub_content = itemView.findViewById(R.id.tv_sr_restaurant_sub_title);
                tv_rating_statement = itemView.findViewById(R.id.tv_sr_rating_msg);
                rating_linear = itemView.findViewById(R.id.rating_linear);
                rating_img = itemView.findViewById(R.id.rating_img);
                mDeliveryAmtContainer = itemView.findViewById(R.id.lay_sr_restaurant_delivery_amt_container);
                tv_delivery_amount_title = itemView.findViewById(R.id.tv_sr_delivery_amt_title);
                tv_delivery_amount = itemView.findViewById(R.id.tv_sr_delivery_amt_data);
                tv_ar_minimum_amount = itemView.findViewById(R.id.tv_ar_minimum_amount);
                mDeliveryAmtContainer = itemView.findViewById(R.id.delivery_container);
                mPickupContainer = itemView.findViewById(R.id.pickup_container);
                mPickupTime = itemView.findViewById(R.id.tv_ar_pickup_time);

                tv_DeliveryTime = itemView.findViewById(R.id.tv_sr_delivery_time);
                tv_offerContent = itemView.findViewById(R.id.tv_sr_restaurant_offers);
                mLayImageOverStatus = itemView.findViewById(R.id.lay_sr_restaurant_image);
                tv_ImageOverStatus = itemView.findViewById(R.id.tv_sr_restaurant_image_over_status);
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
                            toGroceryList(mVendorList.get(getAdapterPosition()).getVendor_id(), mVendorList.get(getAdapterPosition()).getName());
                        } else {
                            toStoreListing(mVendorList.get(getAdapterPosition()).getVendor_id(), "0");
                        }
                    }
                }
            }
        }
    }

    private void toast_layout(String string, final LinearLayout wishlistLayout) {

        wishlistLayout.setVisibility(View.VISIBLE);
        mSRBinding.dataText.setText(string);
        mSRBinding.viewAll.setOnClickListener(new View.OnClickListener() {
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

    public void toGroceryList(String vendor_id, String vendor_name) {
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