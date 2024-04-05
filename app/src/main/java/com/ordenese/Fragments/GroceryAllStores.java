package com.ordenese.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.GroceryStoresListApi;
import com.ordenese.DataSets.StoresDataSet;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Databases.WishListDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.Interfaces.MakeBottomMarginForViewBasket;
import com.ordenese.Interfaces.RestaurantListLoadListener;
import com.ordenese.R;
import com.ordenese.databinding.GroceryAllStoresBinding;

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


public class GroceryAllStores extends Fragment implements View.OnClickListener {

    private GroceryAllStoresBinding mGroceryASBinding;
    private ProgressDialog mProgressDialog;
    private RetrofitInterface retrofitInterface;
    private CartInfo cartInfo;
    private ArrayList<Object> mGASParentList;

    private RecyclerView.LayoutManager mLayoutMgrGCMPParent;
    private GASPAdapter mGaspAdapter;
    private GroceryStoresListApi mGStoresListApi;
    Activity activity;

    private int mPageCount = 1;
    private int mListTotals = 0;
    private MakeBottomMarginForViewBasket mMakeBottomMarginForViewBasket;


    public GroceryAllStores() {
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
        activity = getActivity();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.grocery_all_stores, container, false);
        mGroceryASBinding = GroceryAllStoresBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        cartInfo = (CartInfo) getActivity();

        mMakeBottomMarginForViewBasket = (MakeBottomMarginForViewBasket) getActivity();

        mGroceryASBinding.imgGasBackBtn.setOnClickListener(this);
        mGroceryASBinding.tvGasLocation.setOnClickListener(this);

        mGroceryASBinding.wishlist.setOnClickListener(new View.OnClickListener() {
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

        mGroceryASBinding.layGcMpTuSearchGrocery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //gas_search_products_or_stores
                if (getActivity() != null) {

                    //  AppFunctions.toastShort(getActivity(), getActivity().getResources().getString(R.string.gas_search_products_or_stores));
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    GroceryAllStoresSearch m_groceryAllStoresSearch = new GroceryAllStoresSearch();
                    Bundle bundle = new Bundle();
                    //bundle.putString(DefaultNames.store_id,mStore_ID);
                    // bundle.putString(DefaultNames.store_name,mStore_ID);
                    // bundle.putString(DefaultNames.store_status,mStore_ID);
                    m_groceryAllStoresSearch.setArguments(bundle);
                    mFT.replace(R.id.layout_app_home_body, m_groceryAllStoresSearch, "m_groceryAllStoresSearch");
                    mFT.addToBackStack("m_groceryAllStoresSearch");
                    mFT.commit();

                }


            }
        });

        mGroceryASBinding.recyclerGasParent.setVisibility(View.GONE);
        mGroceryASBinding.layGasEmpty.setVisibility(View.GONE);

        return mGroceryASBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if (mId == R.id.img_gas_back_btn) {

            if (getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }

        } else if (mId == R.id.tv_gas_location) {
            // AppFunctions.toastShort(getActivity(), "Delivery location");
            /*if (getActivity() != null) {
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
            }*/
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null) {
            callStoreListAPi();

            if (AreaGeoCodeDB.getInstance(getActivity()).isAreaGeoCodeSelected()) {
                mGroceryASBinding.tvGasLocation.setText(AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode().getmAddress());
                mGroceryASBinding.tvGasLocation.setSelected(true);
            } else {
                mGroceryASBinding.tvGasLocation.setText(getActivity().getResources().getString(R.string.delivering_to));
            }
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

           /* if (viewType == 0) {
                // for search ui.
                return new TopViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.gc_mp_top_ui, parent, false));
            }  else {
                // for stores list ui.
                //its viewType == 1
                return new BottomViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.gc_mp_bottom_ui, parent, false));
            }*/

            return new BottomViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.gas_bottom_ui, parent, false));

        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            BottomViewHolder mBottomVHolder = (BottomViewHolder) holder;

            if (mGStoresListApi != null && mGStoresListApi.storeList != null && mGStoresListApi.storeList.size() > 0) {

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
                                            AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                                            jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                                            jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());
                                            jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                                            jsonObject.put(DefaultNames.page, mPageCount);
                                            jsonObject.put(DefaultNames.page_per_unit, 10);
                                            jsonObject.put(DefaultNames.free_delivery, "");
                                            jsonObject.put(DefaultNames.business_type, "2");// 2 - Grocery business type
                                            JSONArray cuisineJsonArray = new JSONArray();
                                            jsonObject.put(DefaultNames.cuisine, cuisineJsonArray);

                                            jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                                            jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                                            jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                                            RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                                            retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                                            Call<GroceryStoresListApi> Call = retrofitInterface.groceryStoresList(body);
                                            mProgressDialog.show();
                                            Call.enqueue(new Callback<GroceryStoresListApi>() {
                                                @SuppressLint("NotifyDataSetChanged")
                                                @Override
                                                public void onResponse(@NonNull Call<GroceryStoresListApi> call, @NonNull Response<GroceryStoresListApi> response) {

                                                    mProgressDialog.cancel();

                                                    if (response.isSuccessful()) {
                                                        GroceryStoresListApi m_Store_LA_Pagination = response.body();
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
                                                public void onFailure(@NonNull Call<GroceryStoresListApi> call, @NonNull Throwable t) {
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
            ArrayList<StoresDataSet> mStoreList;
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

                restaurantViewHolder.tv_ar_minimum_amount.setText(activity.getResources().getString(R.string.min) + " - " +
                        mStoreList.get(position).minimum_amount);

                restaurantViewHolder.favorite_icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (WishListDB.getInstance(activity).getSizeOfList() > 0) {
                            if (WishListDB.getInstance(activity).isSelected(mStoreList.get(position).vendor_id)) {
                                toast_layout(activity.getResources().getString(R.string.removed_from_the_wishlist), mGroceryASBinding.wishlistLayout);
                                WishListDB.getInstance(activity).removeFromFavouriteList(mStoreList.get(position).vendor_id);
                                restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_border_primary_color_24dp));
                            } else {
                                toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), mGroceryASBinding.wishlistLayout);
                                WishListDB.getInstance(activity).add_vendor_id(mStoreList.get(position).vendor_id);
                                restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                            }
                        } else {
                            toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), mGroceryASBinding.wishlistLayout);
                            WishListDB.getInstance(activity).add_vendor_id(mStoreList.get(position).vendor_id);
                            restaurantViewHolder.favorite_icon.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                        }
                    }
                });

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

                if (mStoreList.get(position).free_delivery.equals("1")) {
                    restaurantViewHolder.tv_delivery_amount_title.setVisibility(View.VISIBLE);
                    restaurantViewHolder.tv_delivery_amount_title.setText(activity.getResources().getString(R.string.ar_filter_do_free_delivery));
                    restaurantViewHolder.tv_delivery_amount.setVisibility(View.GONE);
                } else {
                    restaurantViewHolder.tv_delivery_amount.setVisibility(View.VISIBLE);
                    restaurantViewHolder.tv_delivery_amount.setText(mStoreList.get(position).delivery_charge);
                    restaurantViewHolder.tv_delivery_amount_title.setVisibility(View.GONE);
                }

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
                TextView tv_rating_statement, tv_delivery_amount_title, tv_delivery_amount, tv_offerContent, tv_ImageOverStatus,tv_ar_minimum_amount;
                LinearLayout mDeliveryAmtContainer, mDeliveryTimeContainer,
                        mLayImageOverStatus, mLayVLOfferContainer, rating_linear, empty, mPickupContainer;

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
                    // iv_Dot = itemView.findViewById(R.id.img_ar_restaurant_offers_dot);
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
                String msg = activity.getResources().getString(R.string.restaurant_closed_msg_1) + " " + name + " " + activity.getResources().getString(R.string.store_closed_msg_2);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(activity.getResources().getString(R.string.store_closed));
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
                String msg = activity.getResources().getString(R.string.restaurant_closed_msg_1) + " " + name + " " + activity.getResources().getString(R.string.store_busy_msg_2);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(activity.getResources().getString(R.string.store_busy));
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
        mGroceryASBinding.dataText.setText(string);
        mGroceryASBinding.viewAll.setOnClickListener(new View.OnClickListener() {
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

    private void callStoreListAPi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {


                JSONObject jsonObject = new JSONObject();
                try {

                    AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();

                    jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                    jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.free_delivery, "");
                    jsonObject.put(DefaultNames.business_type, "2");// 2 - Grocery business type
                    jsonObject.put(DefaultNames.page, 1);
                    mPageCount = 1;
                    jsonObject.put(DefaultNames.page_per_unit, 10);
                    JSONArray cuisineJsonArray = new JSONArray();
                    jsonObject.put(DefaultNames.cuisine, cuisineJsonArray);
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<GroceryStoresListApi> Call = retrofitInterface.groceryStoresList(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<GroceryStoresListApi>() {
                        @Override
                        public void onResponse(@NonNull Call<GroceryStoresListApi> call, @NonNull Response<GroceryStoresListApi> response) {
                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                mGStoresListApi = response.body();
                                if (mGStoresListApi != null) {
                                    if (mGStoresListApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            //mListTotals

                                            if (mGStoresListApi.storeList != null && mGStoresListApi.storeList.size() > 0) {
                                                mGroceryASBinding.recyclerGasParent.setVisibility(View.VISIBLE);
                                                mGroceryASBinding.layGasEmpty.setVisibility(View.GONE);

                                                mGASParentList = new ArrayList<>();
                                                mGASParentList.add(DefaultNames.product_list); // for stores list ui.
                                                mLayoutMgrGCMPParent = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                                mGroceryASBinding.recyclerGasParent.setLayoutManager(mLayoutMgrGCMPParent);
                                                mGaspAdapter = new GASPAdapter(mGASParentList);
                                                mGroceryASBinding.recyclerGasParent.setAdapter(mGaspAdapter);

                                            } else {
                                                toShowGroceryStoresEmpty();
                                            }
                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            toShowGroceryStoresEmpty();
                                            if (mGStoresListApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mGStoresListApi.error.message);
                                            }
                                        }
                                    }
                                }
                            } else {
                                toShowGroceryStoresEmpty();
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
                        public void onFailure(@NonNull Call<GroceryStoresListApi> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                            toShowGroceryStoresEmpty();
                        }
                    });

                } catch (JSONException e) {
                    mProgressDialog.cancel();
                    toShowGroceryStoresEmpty();

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

    private void toShowGroceryStoresEmpty() {
        mGroceryASBinding.recyclerGasParent.setVisibility(View.GONE);
        mGroceryASBinding.layGasEmpty.setVisibility(View.VISIBLE);
    }


}