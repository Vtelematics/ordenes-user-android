package com.ordenese.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.ordenese.ApiConnection.ApiClient;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.CustomClasses.SnappingLinearLayoutManager;
import com.ordenese.CustomClasses.TabSync;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.Category;
import com.ordenese.DataSets.MenuAndItemsDataSet;
import com.ordenese.DataSets.MenuCategorySelectionDataSet;
import com.ordenese.DataSets.Product;
import com.ordenese.DataSets.Vendor_Info;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Databases.WishListDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.Interfaces.MakeBottomMarginForViewBasket;
import com.ordenese.R;
import com.ordenese.databinding.FragmentCategoriesListBottomBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RestaurantInfo extends Fragment {

    View mView;
    RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;
    String vendor_id = "", product_id = "";
    ImageView img_restaurant_menu_restaurant_logo, menu_item, back_btn, search_btn, wishlist_btn;
    TextView tv_restaurant_menu_restaurant_title, tv_restaurant_menu_title_cuisines, delivery_time, delivery_fee, mRating_txt,
            vendor_info_txt, delivery_info, pickup_info, data_text;
    //    private static RecyclerView category_title_list;
    public static RecyclerView recycler_restaurant_menu_list;
    public static TabSync tabSync;
    public static LinearSmoothScroller smoothScroller;
    LinearLayoutManager linearLayoutManager;
    public static TabLayout tabLayout;
    boolean userSelect = false;
    MenuAndItemsAdapter mMenuAndItemsAdapter;
    public static TitleListAdapter mTitleListAdapter;
    public static ArrayList<MenuAndItemsDataSet> mMenuAndChildItemsList = new ArrayList<>();
    LinearLayout product_empty_linear, vendor_delivery_details, delivery_tv, pickup_tv;
    public static Vendor_Info vendor_info;
    String vendor_info_str = "";
    private int productPosition = -1;
    public static Activity activity;
    CartInfo cartInfo;
    Boolean misFirst = false;

    private LinearLayout mLayVendorClosed, order_type_linear, view_all, wishlist_layout;
    private TextView mLayVendorStatus, vendor_reviews_btn, mRatingCount;

    private RatingBar mVendorRatingBar;

    private MakeBottomMarginForViewBasket mMakeBottomMarginForViewBasket;
    private NestedScrollView mNestedScrollView;
    private SnappingLinearLayoutManager snappingLinearLayMgr;

    private AppBarLayout mAppBarLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vendor_id = getArguments().getString("vendor_id");
            product_id = getArguments().getString("product_id");
        }
        misFirst = true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(AppLanguageSupport.onAttach(context));
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setLayoutDirection(
                    "ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(getActivity())) ?
                            View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
        activity = getActivity();
        cartInfo = (CartInfo) context;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.store_menu, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mMakeBottomMarginForViewBasket = (MakeBottomMarginForViewBasket) getActivity();

        mAppBarLayout = mView.findViewById(R.id.app_bar_restaurant_menu_layout);

        mLayVendorClosed = mView.findViewById(R.id.lay_sm_restaurant_image);
        mLayVendorClosed.setVisibility(View.GONE);
        mLayVendorStatus = mView.findViewById(R.id.tv_sm_restaurant_image_over_status);
        mLayVendorStatus.setVisibility(View.GONE);

//        mNestedScrollView = mView.findViewById(R.id.nested_view_rml);
        delivery_tv = mView.findViewById(R.id.delivery_tv);
        pickup_tv = mView.findViewById(R.id.pickup_tv);
        pickup_info = mView.findViewById(R.id.pickup_info);
        delivery_info = mView.findViewById(R.id.delivery_info);
        wishlist_btn = mView.findViewById(R.id.wishlist_btn);
        wishlist_layout = mView.findViewById(R.id.wishlist_layout);
        view_all = mView.findViewById(R.id.view_all);
        data_text = mView.findViewById(R.id.data_text);

        delivery_tv.setVisibility(View.GONE);
        pickup_tv.setVisibility(View.GONE);

        order_type_linear = mView.findViewById(R.id.order_type_linear);
        mRating_txt = mView.findViewById(R.id.tv_restaurant_menu_rating);
        mRatingCount = mView.findViewById(R.id.tv_restaurant_menu_rating_count);
        mRatingCount.setVisibility(View.GONE);
        mVendorRatingBar = mView.findViewById(R.id.rating_image_rating);
        tabLayout = mView.findViewById(R.id.tabLayout);

        tv_restaurant_menu_restaurant_title = mView.findViewById(R.id.tv_restaurant_menu_restaurant_title);
        tv_restaurant_menu_title_cuisines = mView.findViewById(R.id.tv_restaurant_menu_title_cuisines);
        img_restaurant_menu_restaurant_logo = mView.findViewById(R.id.img_restaurant_menu_restaurant_logo);
        recycler_restaurant_menu_list = mView.findViewById(R.id.recycler_restaurant_menu_list);
//        category_title_list = mView.findViewById(R.id.recycler_restaurant_menu_title_list);
        delivery_time = mView.findViewById(R.id.delivery_time);
        delivery_fee = mView.findViewById(R.id.delivery_fee);
        menu_item = mView.findViewById(R.id.menu_item);
        menu_item.setVisibility(View.GONE);
        search_btn = mView.findViewById(R.id.search_btn);
        search_btn.setVisibility(View.GONE);
        back_btn = mView.findViewById(R.id.back_btn);
        vendor_info_txt = mView.findViewById(R.id.vendor_info_txt);
        vendor_info_txt.setVisibility(View.GONE);

        vendor_reviews_btn = mView.findViewById(R.id.tv_restaurant_reviews_btn);
        vendor_reviews_btn.setVisibility(View.GONE);
        vendor_reviews_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activity != null) {
                    if (vendor_info != null) {
                        if (vendor_info.getVendor() != null) {
                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                            RatingList ratingList = new RatingList();
                            Bundle mBundle = new Bundle();
                            mBundle.putString(DefaultNames.vendor_id, vendor_info.getVendor().getVendorId());
                            mBundle.putString(DefaultNames.vendor_image_url_path, vendor_info.getVendor().getLogo());
                            ratingList.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_home_body, ratingList, "ratingList");
                            mFT.addToBackStack("ratingList");
                            mFT.commit();
                        }
                    }

                }
            }
        });

        if (WishListDB.getInstance(activity).getSizeOfList() > 0) {
            if (WishListDB.getInstance(activity).isSelected(vendor_id)) {
                wishlist_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
            } else {
                wishlist_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_border_primary_color_24dp));
            }
        } else {
            wishlist_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_border_primary_color_24dp));
        }

        wishlist_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (WishListDB.getInstance(activity).getSizeOfList() > 0) {
                    if (WishListDB.getInstance(activity).isSelected(vendor_id)) {
                        toast_layout(activity.getResources().getString(R.string.removed_from_the_wishlist), wishlist_layout);
                        WishListDB.getInstance(activity).removeFromFavouriteList(vendor_id);
                        wishlist_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_border_primary_color_24dp));
                    } else {
                        toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), wishlist_layout);
                        WishListDB.getInstance(activity).add_vendor_id(vendor_id);
                        wishlist_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                    }
                } else {
                    toast_layout(activity.getResources().getString(R.string.added_to_the_wishlist), wishlist_layout);
                    WishListDB.getInstance(activity).add_vendor_id(vendor_id);
                    wishlist_btn.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_favorite_primary_color_24dp));
                }
            }
        });

        vendor_delivery_details = mView.findViewById(R.id.lay_sm_delivery_details);
        vendor_delivery_details.setVisibility(View.GONE);

        product_empty_linear = mView.findViewById(R.id.layout_restaurant_menu_item_not_found_message);

        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        menu_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                CategoriesListBottom categoriesListBottom = new CategoriesListBottom(/*mNestedScrollView*/snappingLinearLayMgr, mAppBarLayout);
                categoriesListBottom.show(getParentFragmentManager(), "categoriesListBottom");
                mFT.addToBackStack("categoriesListBottom");
            }
        });

        vendor_info_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null) {
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    RestaurantDetails restaurantDetails = new RestaurantDetails();
                    Bundle mBundle = new Bundle();
                    mBundle.putString("vendor_id", vendor_id);
                    restaurantDetails.setArguments(mBundle);
                    mFT.replace(R.id.layout_app_home_body, restaurantDetails, "restaurantDetails");
                    mFT.addToBackStack("restaurantDetails");
                    mFT.commit();
                }
            }
        });

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null) {
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    SearchProduct searchProduct = new SearchProduct();
                    Bundle mBundle = new Bundle();
                    mBundle.putString("vendor_id", vendor_id);
                    mBundle.putSerializable("vendor_info", vendor_info);
                    mBundle.putSerializable("product_list", mMenuAndChildItemsList);
                    searchProduct.setArguments(mBundle);
                    mFT.replace(R.id.layout_app_home_body, searchProduct, "searchProduct");
                    mFT.addToBackStack("searchProduct");
                    mFT.commit();
                }
            }
        });

        delivery_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delivery_tv.setBackground(activity.getResources().getDrawable(R.drawable.bg_white_curve));
                pickup_tv.setBackground(null);

                if (!OrderTypeDB.getInstance(activity).getUserServiceType().isEmpty()) {
                    OrderTypeDB.getInstance(activity).updateUserServiceType("1");
                } else {
                    OrderTypeDB.getInstance(activity).addUserServiceType("1");
                }
            }
        });

        pickup_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickup_tv.setBackground(activity.getResources().getDrawable(R.drawable.bg_white_curve));
                delivery_tv.setBackground(null);

                if (!OrderTypeDB.getInstance(activity).getUserServiceType().isEmpty()) {
                    OrderTypeDB.getInstance(activity).updateUserServiceType("2");
                } else {
                    OrderTypeDB.getInstance(activity).addUserServiceType("2");
                }
            }
        });


        getInfo();
        return mView;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (OrderTypeDB.getInstance(activity).getUserServiceType().equals("2")) {
            pickup_tv.setBackground(activity.getResources().getDrawable(R.drawable.bg_white_curve));
            delivery_tv.setBackground(null);
        } else {
            delivery_tv.setBackground(activity.getResources().getDrawable(R.drawable.bg_white_curve));
            pickup_tv.setBackground(null);
        }

        if (!misFirst) {
            getInfo();
        }
        cart_product_count();
    }

    private void toast_layout(String string, final LinearLayout wishlistLayout) {

        wishlistLayout.setVisibility(View.VISIBLE);
        data_text.setText(string);
        view_all.setOnClickListener(new View.OnClickListener() {
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

    private void getInfo() {
        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();

                String latitude = "", longitude = "";

                if (AreaGeoCodeDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    AreaGeoCodeDataSet mAreaGeoCodeDS = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                    latitude = mAreaGeoCodeDS.getmLatitude();
                    longitude = mAreaGeoCodeDS.getmLongitude();
                }

                try {
                    jsonObject.put(DefaultNames.vendor_id, vendor_id);
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.vendor_type_id, "");
                    jsonObject.put(DefaultNames.latitude, latitude);
                    jsonObject.put(DefaultNames.longitude, longitude);

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    jsonObject.put("order_type", String.valueOf(OrderTypeDB.getInstance(getActivity()).getUserServiceType()));

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<Vendor_Info> Call = retrofitInterface.vendorInfo(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<Vendor_Info>() {
                        @Override
                        public void onResponse(@NonNull Call<Vendor_Info> call, @NonNull Response<Vendor_Info> response) {
                            mProgressDialog.cancel();
                            if (getActivity() != null) {

                                if (response.isSuccessful()) {
                                    vendor_info = response.body();
                                    vendor_info_str = response.toString();

                                    if (vendor_info.getSuccess() != null) {
                                        //Api response successDataSet :-

                                        search_btn.setVisibility(View.VISIBLE);
                                        vendor_info_txt.setVisibility(View.VISIBLE);

                                        mRating_txt.setVisibility(View.VISIBLE);
                                        mRatingCount.setVisibility(View.VISIBLE);

                                        if (vendor_info != null && vendor_info.getVendor() != null) {

                                            String ab = activity.getResources().getString(R.string.in) + " " + vendor_info.getVendor().getDeliveryTime() + " " + activity.getResources().getString(R.string.mins_) +
                                                    " - " + vendor_info.getVendor().getDeliveryCharge();
                                            delivery_info.setText(ab);
                                            String cd = activity.getResources().getString(R.string.in) + " " + vendor_info.getVendor().preparing_time + " " + activity.getResources().getString(R.string.mins_) +
                                                    " - " + vendor_info.getVendor().vendor_distance + " " + activity.getResources().getString(R.string.km);
                                            pickup_info.setText(cd);
                                            order_type_linear.setVisibility(View.VISIBLE);
                                            if (vendor_info.getVendor().delivery.equals("1") && vendor_info.getVendor().pick_up.equals("1")) {
                                                delivery_tv.setVisibility(View.VISIBLE);
                                                pickup_tv.setVisibility(View.VISIBLE);
                                            } else {
                                                if (vendor_info.getVendor().pick_up.equals("0")) {
                                                    delivery_tv.setVisibility(View.VISIBLE);
                                                    pickup_tv.setVisibility(View.GONE);
                                                }
                                                if (vendor_info.getVendor().delivery.equals("0")) {
                                                    delivery_tv.setVisibility(View.GONE);
                                                    pickup_tv.setVisibility(View.VISIBLE);
                                                }
                                            }

                                            if (vendor_info.getVendor() != null && vendor_info.getVendor().getVendorStatus() != null) {
                                                if (vendor_info.getVendor().getVendorStatus().equals("0")) {
                                                    //where 1 - open , 0 - closed, 2 - busy
                                                    mLayVendorClosed.setVisibility(View.VISIBLE);
                                                    mLayVendorStatus.setVisibility(View.VISIBLE);
                                                    mLayVendorStatus.setText(getActivity().getResources().getString(R.string.closed));
                                                } else if (vendor_info.getVendor().getVendorStatus().equals("2")) {
                                                    //where 1 - open , 0 - closed, 2 - busy
                                                    mLayVendorClosed.setVisibility(View.VISIBLE);
                                                    mLayVendorStatus.setVisibility(View.VISIBLE);
                                                    mLayVendorStatus.setText(getActivity().getResources().getString(R.string.busy));
                                                } else {
                                                    //where 1 - open , 0 - closed, 2 - busy
                                                    mLayVendorClosed.setVisibility(View.GONE);
                                                    mLayVendorStatus.setVisibility(View.GONE);
                                                }
                                            } else {
                                                mLayVendorClosed.setVisibility(View.GONE);
                                                mLayVendorStatus.setVisibility(View.GONE);
                                            }

                                            tv_restaurant_menu_restaurant_title.setText(vendor_info.getVendor().getName());
                                            delivery_time.setText(activity.getResources().getString(R.string.in) + " " + vendor_info.getVendor().getDeliveryTime() + " " + activity.getResources().getString(R.string.mins_));
                                            delivery_fee.setText("( " + activity.getResources().getString(R.string.delivery_fee) + " : " + vendor_info.getVendor().getDeliveryCharge() + " )");
                                            AppFunctions.bannerLoaderUsingGlide(vendor_info.getVendor().getImage(), img_restaurant_menu_restaurant_logo, getActivity());

                                            mVendorRatingBar.setVisibility(View.VISIBLE);
                                            mRating_txt.setVisibility(View.VISIBLE);

                                            if (vendor_info.getVendor().getRating() != null) {
                                                String mRATING = vendor_info.getVendor().getRating().getRating();
                                                String mCOUNT = vendor_info.getVendor().getRating().getCount();
                                                if (mCOUNT != null && !mCOUNT.isEmpty() && !mCOUNT.equals("0")) {
                                                    if (mRATING != null && !mRATING.isEmpty()) {
                                                        mVendorRatingBar.setRating(Float.parseFloat(mRATING));
                                                        mRating_txt.setText(mRATING);
                                                        String m_RATING = "(" + mCOUNT + " " + activity.getResources().getString(R.string.ratings) + ")";
                                                        mRatingCount.setText(m_RATING);
                                                        mRatingCount.setVisibility(View.VISIBLE);
                                                        vendor_reviews_btn.setVisibility(View.VISIBLE);
                                                    } else {
                                                        noRatingUI();
                                                    }
                                                } else {
                                                    noRatingUI();
                                                }
                                            } else {
                                                noRatingUI();
                                            }

                                            //mRatingCount.setText(activity.getResources().getString(R.string.no_rating));

                                            StringBuilder mCuisine = new StringBuilder();
                                            if (vendor_info.getVendor().getCuisine() != null) {
                                                for (int i = 0; i < vendor_info.getVendor().getCuisine().size(); i++) {
                                                    if (mCuisine.toString().isEmpty()) {
                                                        mCuisine.append(vendor_info.getVendor().getCuisine().get(i).getName());
                                                    } else {
                                                        mCuisine.append(", ").append(vendor_info.getVendor().getCuisine().get(i).getName());
                                                    }
                                                }
                                            }

                                            tv_restaurant_menu_title_cuisines.setText(mCuisine.toString());
                                            if (vendor_info.getVendor().getCategory().size() != 0) {
                                                menu_item.setVisibility(View.VISIBLE);
                                                product_empty_linear.setVisibility(View.GONE);
                                                restaurantMenuListDetails(vendor_info.getVendor().getCategory());
                                            } else {
                                                mMenuAndChildItemsList = new ArrayList<>();
                                                product_empty_linear.setVisibility(View.VISIBLE);
                                                menu_item.setVisibility(View.GONE);
                                            }
                                        }

                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (vendor_info.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", vendor_info.error.message);
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


                        }

                        @Override
                        public void onFailure(@NonNull Call<Vendor_Info> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }

                    });

                } catch (JSONException e) {
                    mProgressDialog.cancel();
                    //Log.e("415 Excep ", e.toString());
                    e.printStackTrace();
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

    private void noRatingUI() {
        mRating_txt.setText(activity.getResources().getString(R.string.no_rating));
        mRatingCount.setVisibility(View.GONE);
        vendor_reviews_btn.setVisibility(View.GONE);
    }

/*
    private void cart_product_count() {

        if (AppFunctions.networkAvailabilityCheck(activity)) {
            if (UserDetailsDB.getInstance(getActivity()).isUserLoggedIn()) {


                try {
                    retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                    String mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                    Call<String> Call = retrofitInterface.cart_product_count(mCustomerAuthorization, body);
                    Call.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {

                            try {
                                JSONObject object = new JSONObject(response.body());
                                if (!object.isNull("qty_count")) {
                                    if (!object.getString("qty_count").equals("0")) {
                                        if (!object.isNull("total")) {
                                            cartInfo.cart_info(true, object.getString("qty_count"), object.getString("total"));
                                        }
                                    } else {
                                        cartInfo.cart_info(false, "", "");
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                        }
                    });
                } catch (Exception e) {
                }


            } else {
                cartInfo.cart_info(false, "", "");
            }
        } else {
            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
            NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
            mFT.replace(R.id.layout_home_restaurant_body, mNetworkAnalyser, "mNetworkAnalyser");
            mFT.addToBackStack("mNetworkAnalyser");
            mFT.commit();
        }

    }
*/

    private void cart_product_count() {

        if (AppFunctions.networkAvailabilityCheck(activity)) {

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


    @Override
    public void onStop() {
        super.onStop();
        cartInfo.cart_info(false, "", "");
        mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);
    }

    private void restaurantMenuListDetails(ArrayList<Category> restaurantDetailsList) {

        //For StoreMenu titles :-
        if (restaurantDetailsList.size() > 0) {

            menu_item.setVisibility(View.VISIBLE);
            product_empty_linear.setVisibility(View.GONE);

            mMenuAndChildItemsList = new ArrayList<>();

            int mMenuListSize = restaurantDetailsList.size();
            for (int menu = 0; menu < mMenuListSize; menu++) {

                MenuAndItemsDataSet mParentMenuAndItemsDs = new MenuAndItemsDataSet();
                mParentMenuAndItemsDs.setParentMenu(true);
                mParentMenuAndItemsDs.setParentSectionId(restaurantDetailsList.get(menu).getProductCategoryId());
                mParentMenuAndItemsDs.setParentName(restaurantDetailsList.get(menu).getName());
                mParentMenuAndItemsDs.setParentChildItemsCount(String.valueOf(restaurantDetailsList.get(menu).getCount()));
                // mMenuDs.setProductsList(restaurantDetailsList.get(0).getmSectionsList().getmMenuList().get(menu).getProductsList());
                if (restaurantDetailsList.get(menu).getProduct().size() != 0) {
                    mMenuAndChildItemsList.add(mParentMenuAndItemsDs);
                }

                //  String mParentName = mParentMenuAndItemsDs.getParentName();
                //   Log.e(" List "+mParentName,""+menu);

                //For StoreMenu Items list :-
                int mMenuItemListSize = restaurantDetailsList.get(menu).getProduct().size();
                //  ArrayList<ProductDataSet> mMenuItemList = new ArrayList<>();
                for (int menuItem = 0; menuItem < mMenuItemListSize; menuItem++) {
                    MenuAndItemsDataSet mChildMenuAndItemsDs = new MenuAndItemsDataSet();
                    mChildMenuAndItemsDs.setParentMenu(false);
                    Product mTempMenuItemsDs = restaurantDetailsList.get(menu).getProduct().get(menuItem);
                    mChildMenuAndItemsDs.setChildProductId(mTempMenuItemsDs.getProductItemId());
                    mChildMenuAndItemsDs.setChildName(mTempMenuItemsDs.getItemName());
                    mChildMenuAndItemsDs.setChildProductDescription(mTempMenuItemsDs.getDescription());
                    mChildMenuAndItemsDs.setChildImage(mTempMenuItemsDs.getImage());
                    mChildMenuAndItemsDs.setChildLogo(mTempMenuItemsDs.getLogo());
                    mChildMenuAndItemsDs.setChildPrice(mTempMenuItemsDs.getPrice());
                    mChildMenuAndItemsDs.setPrice_status(mTempMenuItemsDs.getPrice_status());
                    mChildMenuAndItemsDs.setChildOfferPrice(String.valueOf(mTempMenuItemsDs.getDiscount()));
                    mChildMenuAndItemsDs.setChildHasOption(String.valueOf(mTempMenuItemsDs.getHasOptions()));
                    mChildMenuAndItemsDs.setProductOptionList(mTempMenuItemsDs.getOptions());

                    mChildMenuAndItemsDs.setChildProductMinQty(mTempMenuItemsDs.getStock());

                    mChildMenuAndItemsDs.setParentSectionId(restaurantDetailsList.get(menu).getProductCategoryId());

                    mMenuAndChildItemsList.add(mChildMenuAndItemsDs);

                    if (product_id.equals(mTempMenuItemsDs.getProductItemId())) {
                        // productPosition = menuItem;
                        if (mMenuAndChildItemsList != null && mMenuAndChildItemsList.size() > 0) {
                            productPosition = mMenuAndChildItemsList.size() - 1;
                        }
                    }
                }
            }
            if (productPosition > -1) {
                // AppFunctions.toastLong(getActivity(),String.valueOf(productPosition));
                recycler_restaurant_menu_list.smoothScrollToPosition(productPosition);
            }
        }


//        mMenuAndItemsAdapter = new MenuAndItemsAdapter(getActivity(), mMenuAndChildItemsList);
        mMenuAndItemsAdapter = new MenuAndItemsAdapter(getActivity(), restaurantDetailsList);
//        snappingLinearLayMgr = new SnappingLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recycler_restaurant_menu_list.setLayoutManager(linearLayoutManager);
        recycler_restaurant_menu_list.setAdapter(mMenuAndItemsAdapter);

        for (int i = 0; i < restaurantDetailsList.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(restaurantDetailsList.get(i).getName()));
        }

        tabSync = new TabSync(tabLayout, recycler_restaurant_menu_list, linearLayoutManager, restaurantDetailsList);
        tabLayout.addOnTabSelectedListener(tabSync);
        recycler_restaurant_menu_list.addOnScrollListener(tabSync);

//        smoothScroller = new LinearSmoothScroller(activity) {
//            @Override
//            protected int getVerticalSnapPreference() {
//                return LinearSmoothScroller.SNAP_TO_START;
//            }
//        };

//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                userSelect = true;
//                int position = tab.getPosition();
//                smoothScroller.setTargetPosition(position);
//                Objects.requireNonNull(recycler_restaurant_menu_list.getLayoutManager()).startSmoothScroll(smoothScroller);
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//        });
//
//        recycler_restaurant_menu_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    userSelect = false;
//                }
//            }
//
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (!userSelect) {
//                    int firstVisiblePosition = ((LinearLayoutManager) recycler_restaurant_menu_list.getLayoutManager()).findFirstVisibleItemPosition();
//                    if (tabLayout.getSelectedTabPosition() != firstVisiblePosition) {
//                        tabLayout.getTabAt(firstVisiblePosition).select();
//                    }
//                }
//            }
//        });

        recycler_restaurant_menu_list.setVisibility(View.VISIBLE);

        if (mMenuAndChildItemsList != null && mMenuAndChildItemsList.size() > 0) {

            recycler_restaurant_menu_list.setVisibility(View.VISIBLE);
//            category_title_list.setVisibility(View.VISIBLE);

            ArrayList<MenuCategorySelectionDataSet> mMenuCategorySelectionList = new ArrayList<>();
            for (int menu = 0; menu < mMenuAndChildItemsList.size(); menu++) {
                if (mMenuAndChildItemsList.get(menu).getParentMenu()) {
                    MenuCategorySelectionDataSet mMenuCategorySelectionDs = new MenuCategorySelectionDataSet();
                    Category mMenuDs = new Category();
                    mMenuDs.setName(mMenuAndChildItemsList.get(menu).getParentName());
                    mMenuDs.setProductCategoryId(mMenuAndChildItemsList.get(menu).getParentSectionId());
                    mMenuDs.setCount(mMenuAndChildItemsList.get(menu).getParentChildItemsCount());
                    mMenuCategorySelectionDs.setSectionDataSet(mMenuDs);
                    mMenuCategorySelectionDs.setPosition(menu);
                    mMenuCategorySelectionList.add(mMenuCategorySelectionDs);
                }
            }

//            category_title_list.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
//            mTitleListAdapter = new TitleListAdapter(mMenuCategorySelectionList, activity, 0, false, mMenuAndChildItemsList
//                    /*,mNestedScrollView*/, snappingLinearLayMgr, mAppBarLayout);
//            category_title_list.setAdapter(mTitleListAdapter);

        } else {

            recycler_restaurant_menu_list.setVisibility(View.GONE);
//            category_title_list.setVisibility(View.GONE);
            menu_item.setVisibility(View.GONE);
            product_empty_linear.setVisibility(View.VISIBLE);

        }

    }

    public static class TitleListAdapter extends RecyclerView.Adapter<TitleListAdapter.DataObjectHolder> {

        private final ArrayList<MenuCategorySelectionDataSet> mTitleList;
        private TextView mTempMenuTitleName;
        private Boolean mIsInitial = true;
        private final int mPositionToMove;
        private Boolean mIsFromItemTouch;
        Activity activity;
        private ArrayList<MenuAndItemsDataSet> m__MenuAndChildItemsList;
        // private NestedScrollView m__NestedScrollView;
        SnappingLinearLayoutManager mSnappingLinearLayMgr;
        AppBarLayout m_AppBarLayout;


        public TitleListAdapter(ArrayList<MenuCategorySelectionDataSet> titleList, Activity activity,
                                int positionToMove, Boolean isFromItemTouch,
                                ArrayList<MenuAndItemsDataSet> menuAndChildItemsList
                /*, NestedScrollView nestedScrollView*/, SnappingLinearLayoutManager snappingLinearLayMgr, AppBarLayout appBarLayout) {

            this.mTitleList = titleList;
            this.activity = activity;
            this.mPositionToMove = positionToMove;
            this.mIsFromItemTouch = isFromItemTouch;
            this.m__MenuAndChildItemsList = menuAndChildItemsList;
            // this.m__NestedScrollView = nestedScrollView;
            this.mSnappingLinearLayMgr = snappingLinearLayMgr;
            this.m_AppBarLayout = appBarLayout;

        }

        @Override
        public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.store_menu_title_list_row, parent, false);
            return new DataObjectHolder(mView);
        }


        @Override
        public void onBindViewHolder(final DataObjectHolder holder, final int position) {

            holder.mMenuTitleName.setText(mTitleList.get(position).getSectionDataSet().getName());

            if (mIsFromItemTouch) {
                if (mPositionToMove == position) {
                    if (mTempMenuTitleName != null) {
                        if (activity != null) {
                            mTempMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                            mTempMenuTitleName.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                        }
                    }
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.white));
                        holder.mMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_square_primary_color));
                    }
                    mTempMenuTitleName = holder.mMenuTitleName;
                } else {
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                        holder.mMenuTitleName.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                    }
                }
            } else {
                if (mIsInitial) {
                    //safe check :-
                    if (mTempMenuTitleName != null) {
                        if (activity != null) {
                            mTempMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                            mTempMenuTitleName.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                        }
                    }
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.white));
                        holder.mMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_square_primary_color));
                    }
                    mTempMenuTitleName = holder.mMenuTitleName;
                    mIsInitial = false;
                } else {
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                        holder.mMenuTitleName.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                    }
                }
            }

            holder.mRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mIsFromItemTouch = false;
                    if (mTempMenuTitleName != null) {
                        if (activity != null) {
                            mTempMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
                            mTempMenuTitleName.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
                        }
                    }
                    if (activity != null) {
                        holder.mMenuTitleName.setTextColor(ContextCompat.getColor(activity, R.color.white));
                        holder.mMenuTitleName.setBackground(activity.getResources().getDrawable(R.drawable.bg_square_primary_color));
                    }

                    int mPositionShow = -1;
                    for (int catPosition = 0; catPosition < mMenuAndChildItemsList.size(); catPosition++) {
                        if (mMenuAndChildItemsList.get(catPosition).getParentMenu()) {
                            String mParentSecId = mMenuAndChildItemsList.get(catPosition).getParentSectionId();
                            String mCurrentSecId = mTitleList.get(position).getSectionDataSet().getProductCategoryId();
                            if (mParentSecId.equals(mCurrentSecId)) {
                                mPositionShow = catPosition;
                                break;
                            }
                        }
                    }

                    if (mPositionShow != -1) {
                        mTempMenuTitleName = holder.mMenuTitleName;
                        mSnappingLinearLayMgr.scrollToPositionWithOffset(mPositionShow, 0);
                        if (m_AppBarLayout != null) {
                            m_AppBarLayout.setExpanded(false);
                        }
                    }
                }
            });

            if (position == mTitleList.size() - 1) {
                holder.mTitleView.setVisibility(View.GONE);
            } else {
                holder.mTitleView.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return mTitleList.size();
        }

        public class DataObjectHolder extends RecyclerView.ViewHolder {

            private final TextView mMenuTitleName;
            //            private final View mBarView;
            private final View mTitleView;
            private final LinearLayout mRow;

            public DataObjectHolder(View view) {
                super(view);
                mMenuTitleName = view.findViewById(R.id.tv_store_menu_title);
//                mBarView = view.findViewById(R.id.view_store_menu_title_bar);
                mTitleView = view.findViewById(R.id.view_store_menu_title);
                mRow = view.findViewById(R.id.lay_store_menu_title_list_row);
            }
        }

    }

    public class MenuAndItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        //        private final ArrayList<MenuAndItemsDataSet> mAdapterMenuAndChildItemsList;
        ArrayList<Category> mAdapterMenuAndChildItemsList;
        private final Context mContext;

        //        public MenuAndItemsAdapter(Context context, ArrayList<MenuAndItemsDataSet> menuAndChildItemsList) {
//            this.mContext = context;
//            this.mAdapterMenuAndChildItemsList = menuAndChildItemsList;
//        }
        public MenuAndItemsAdapter(Context context, ArrayList<Category> menuAndChildItemsList) {
            this.mContext = context;
            this.mAdapterMenuAndChildItemsList = menuAndChildItemsList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 2) {
                return new MenuViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.menu_and_items, parent, false));
            } else {
                return new MenuListEmptyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.store_menu_list_empty, parent, false));
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder.getItemViewType() == 2) {
                MenuViewHolder menuViewHolder = (MenuViewHolder) holder;
                //***********************************************************************************

                menuViewHolder.mParentItemName.setText(mAdapterMenuAndChildItemsList.get(position).getName());
                ItemsAdapter itemsAdapter = new ItemsAdapter(mContext, mAdapterMenuAndChildItemsList.get(position).getProduct());
                menuViewHolder.items_list_view.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false));
                menuViewHolder.items_list_view.setAdapter(itemsAdapter);


//                if (mAdapterMenuAndChildItemsList.get(position).getParentMenu()) {
//
//                    //To show the parent menu list :-
//                    menuViewHolder.mParentMenuContainer.setVisibility(View.VISIBLE);
//                    menuViewHolder.mChildMenuItemsContainer.setVisibility(View.GONE);
//                    menuViewHolder.mParentItemName.setText(mAdapterMenuAndChildItemsList.get(position).getParentName());
//
//                    //  String mParentName = mAdapterMenuAndChildItemsList.get(position).getParentName();
//                    //  Log.e(mParentName,""+position);
//
//                } else {
//
//                    //To show the child menu items list :-
//                    menuViewHolder.mParentMenuContainer.setVisibility(View.GONE);
//                    menuViewHolder.mChildMenuItemsContainer.setVisibility(View.VISIBLE);
//
//                    menuViewHolder.mChildItemName.setText(mAdapterMenuAndChildItemsList.get(position).getChildName());
//                    menuViewHolder.mChildItemDescription.setText(mAdapterMenuAndChildItemsList.get(position).getChildProductDescription());
//
//                    if (mAdapterMenuAndChildItemsList.get(position).getPrice_status().equals("1")) {
//                        menuViewHolder.price_txt.setVisibility(View.GONE);
//                        menuViewHolder.mChildItemPrice.setVisibility(View.VISIBLE);
//
//                        //To check offer price available or not :-
//                        if (!mAdapterMenuAndChildItemsList.get(position).getChildOfferPrice().isEmpty()) {
//
//                            menuViewHolder.mChildItemPriceWhenOffer.setVisibility(View.VISIBLE);
//                            menuViewHolder.mChildItemPriceWhenOffer.setText(mAdapterMenuAndChildItemsList.get(position).getChildPrice());
//                            menuViewHolder.mChildItemPriceWhenOffer.setPaintFlags(menuViewHolder.mChildItemPriceWhenOffer.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//
//                            menuViewHolder.mChildItemPrice.setText(mAdapterMenuAndChildItemsList.get(position).getChildOfferPrice());
//                        } else {
//                            menuViewHolder.mChildItemPriceWhenOffer.setVisibility(View.GONE);
//                            menuViewHolder.mChildItemPriceWhenOffer.setPaintFlags(0);
//                            menuViewHolder.mChildItemPrice.setText(mAdapterMenuAndChildItemsList.get(position).getChildPrice());
//                        }
//
//                    } else {
//                        menuViewHolder.price_txt.setVisibility(View.VISIBLE);
//                        menuViewHolder.mChildItemPrice.setVisibility(View.GONE);
//                    }
//
//                    AppFunctions.imageLoaderUsingGlide(mAdapterMenuAndChildItemsList.get(position).getChildLogo(), menuViewHolder.mProductItemImage, getActivity());
//
//                }
//                menuViewHolder.mChildMenuItemsContainer.setOnTouchListener(new View.OnTouchListener() {
//                    @Override
//                    public boolean onTouch(View view, MotionEvent motionEvent) {
//
//                        String mSection__Id = mAdapterMenuAndChildItemsList.get(position).getProductCategoryId();
//                        int mTitlePosition = 0;
//
//                        ArrayList<Category> sectionList = new ArrayList<>();
//                        for (int menu = 0; menu < mMenuAndChildItemsList.size(); menu++) {
//                            if (mMenuAndChildItemsList.get(menu).getParentMenu()) {
//                                Category mMenuDs = new Category();
//                                mMenuDs.setName(mMenuAndChildItemsList.get(menu).getParentName());
//                                mMenuDs.setProductCategoryId(mMenuAndChildItemsList.get(menu).getParentSectionId());
//                                mMenuDs.setCount(mMenuAndChildItemsList.get(menu).getParentChildItemsCount());
//                                sectionList.add(mMenuDs);
//                            }
//                        }
//
//                        // Boolean mIsFinished = false;
//                        for (int menu = 0; menu < sectionList.size(); menu++) {
//                            if (sectionList.get(menu).getProductCategoryId().equals(mSection__Id)) {
//                                mTitlePosition = menu;
//                            }
//                        }
//
//                        category_title_list.smoothScrollToPosition(mTitlePosition);
//
//                        ArrayList<MenuCategorySelectionDataSet> mMenuCategorySelectionList = new ArrayList<>();
//                        for (int menu = 0; menu < mMenuAndChildItemsList.size(); menu++) {
//                            if (mMenuAndChildItemsList.get(menu).getParentMenu()) {
//                                MenuCategorySelectionDataSet mMenuCategorySelectionDs = new MenuCategorySelectionDataSet();
//                                Category mMenuDs = new Category();
//                                mMenuDs.setName(mMenuAndChildItemsList.get(menu).getParentName());
//                                mMenuDs.setProductCategoryId(mMenuAndChildItemsList.get(menu).getParentSectionId());
//                                mMenuDs.setCount(mMenuAndChildItemsList.get(menu).getParentChildItemsCount());
//                                mMenuCategorySelectionDs.setSectionDataSet(mMenuDs);
//                                mMenuCategorySelectionDs.setPosition(menu);
//                                mMenuCategorySelectionList.add(mMenuCategorySelectionDs);
//                            }
//                        }
//                        category_title_list.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false));
//                        mTitleListAdapter = new TitleListAdapter(mMenuCategorySelectionList, activity, mTitlePosition,
//                                true, mMenuAndChildItemsList,/*mNestedScrollView,*/ snappingLinearLayMgr, mAppBarLayout);
//                        category_title_list.setAdapter(mTitleListAdapter);
//
//                        return false;
//                    }
//                });
//
//                menuViewHolder.mChildMenuItemsContainer.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (activity != null) {
//                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
//                            ProductDetails productDetails = new ProductDetails();
//                            Bundle mBundle = new Bundle();
//                            mBundle.putString("vendor_id", vendor_info.getVendor().getVendorId());
//                            mBundle.putString("vendor_name", vendor_info.getVendor().getName());
//
//                            mBundle.putString("vendor_status", vendor_info.getVendor().getVendorStatus());
//
//                            mBundle.putString("product_id", mAdapterMenuAndChildItemsList.get(position).getChildProductId());
//                            mBundle.putString("product_name", mAdapterMenuAndChildItemsList.get(position).getChildName());
//                            mBundle.putString("category_id", mAdapterMenuAndChildItemsList.get(position).getParentSectionId());
//                            mBundle.putString("latitude", vendor_info.getVendor().getLatitude());
//                            mBundle.putString("longitude", vendor_info.getVendor().getLongitude());
//                            mBundle.putString("product_desc", mAdapterMenuAndChildItemsList.get(position).getChildProductDescription());
//                            mBundle.putString("product_image", mAdapterMenuAndChildItemsList.get(position).getChildImage());
//                            mBundle.putString("product_minimum", mAdapterMenuAndChildItemsList.get(position).getChildProductMinQty());
//                            mBundle.putString("product_price", mAdapterMenuAndChildItemsList.get(position).getChildPrice());
//                            mBundle.putString("price_status", mAdapterMenuAndChildItemsList.get(position).getPrice_status());
//                            mBundle.putSerializable("product_details", mAdapterMenuAndChildItemsList.get(position).getProductOptionList());
//                            productDetails.setArguments(mBundle);
//                            mFT.replace(R.id.layout_app_home_body, productDetails, "productDetails");
//                            mFT.addToBackStack("productDetails");
//                            mFT.commit();
//                        }
//                    }
//                });

            }

        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public int getItemCount() {
            if (mAdapterMenuAndChildItemsList != null) {
                if (mAdapterMenuAndChildItemsList.size() > 0) {
                    return mAdapterMenuAndChildItemsList.size();
                } else {
                    return 1;
                }
            } else {
                return 1;
            }
        }

        @Override
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

        }

        public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            final TextView mParentItemName;
            final ImageView mProductItemImage;
            final ImageView mParentItemList;
            final ImageView item_type;
            View mChildBottomLine;
            RecyclerView items_list_view;

            TextView mChildItemName, price_txt;
            TextView mChildItemDescription;
            TextView mChildItemPrice;
            TextView mChildItemPriceWhenOffer;
//            private final TextView ; //,mChildOption,

            private final LinearLayout mParentMenuContainer;
            private final LinearLayout mChildMenuItemsContainer;
            private final LinearLayout mChildPriceContainer;

            // private ImageView mPriceBtn;

            public MenuViewHolder(View itemView) {
                super(itemView);

                mParentItemName = itemView.findViewById(R.id.tv_menu_list_parent_item_name);
                //mParentItemImage = (ImageView) itemView.findViewById(R.id.img_menu_list_parent_image);
                mProductItemImage = itemView.findViewById(R.id.img_menu_list_product_image);
                mParentItemList = itemView.findViewById(R.id.img_menu_list_parent_all_menu);
                items_list_view = itemView.findViewById(R.id.items_list_view);

                mChildItemName = itemView.findViewById(R.id.tv_menu_list_child_item_name);
                mChildItemDescription = itemView.findViewById(R.id.tv_menu_list_child_item_description);
                mChildItemPrice = itemView.findViewById(R.id.tv_menu_list_child_amt);

                mChildBottomLine = itemView.findViewById(R.id.mChildBottomLine);
                price_txt = itemView.findViewById(R.id.price_txt);
                item_type = itemView.findViewById(R.id.item_type);
                item_type.setVisibility(View.GONE);

                mChildItemPriceWhenOffer = itemView.findViewById(R.id.tv_menu_list_child_amt_when_offer);
                mChildItemPriceWhenOffer.setVisibility(View.GONE);

                mParentMenuContainer = itemView.findViewById(R.id.layout_restaurant_menu_parent_container);
                mChildMenuItemsContainer = itemView.findViewById(R.id.layout_restaurant_menu_child_container);
                mChildPriceContainer = itemView.findViewById(R.id.layout_menu_list_child_add_item);
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

    public class ItemsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<Product> mAdapterMenuAndChildItemsList;

        private final Context mContext;

        public ItemsAdapter(Context context, List<Product> product) {
            this.mContext = context;
            this.mAdapterMenuAndChildItemsList = product;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 2) {
                return new MenuViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.items_list, parent, false));
            } else {
                return new MenuListEmptyViewHolder(LayoutInflater.from(getActivity()).inflate(R.layout.store_menu_list_empty, parent, false));
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            if (holder.getItemViewType() == 2) {
                MenuViewHolder menuViewHolder = (MenuViewHolder) holder;
                //***********************************************************************************

//                if (!mAdapterMenuAndChildItemsList.get(position).getParentMenu()) {

                //To show the child menu items list :-
                menuViewHolder.mChildMenuItemsContainer.setVisibility(View.VISIBLE);

                menuViewHolder.mChildItemName.setText(mAdapterMenuAndChildItemsList.get(position).getItemName());
                menuViewHolder.mChildItemDescription.setText(mAdapterMenuAndChildItemsList.get(position).getDescription());

                if (mAdapterMenuAndChildItemsList.get(position).getPrice_status().equals("1")) {
                    menuViewHolder.price_txt.setVisibility(View.GONE);
                    menuViewHolder.mChildItemPrice.setVisibility(View.VISIBLE);

                    //To check offer price available or not :-
                    if (!mAdapterMenuAndChildItemsList.get(position).getDiscount().isEmpty()) {

                        menuViewHolder.mChildItemPriceWhenOffer.setVisibility(View.VISIBLE);
                        menuViewHolder.mChildItemPriceWhenOffer.setText(mAdapterMenuAndChildItemsList.get(position).getPrice());
                        menuViewHolder.mChildItemPriceWhenOffer.setPaintFlags(menuViewHolder.mChildItemPriceWhenOffer.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                        menuViewHolder.mChildItemPrice.setText(mAdapterMenuAndChildItemsList.get(position).getDiscount());
                    } else {
                        menuViewHolder.mChildItemPriceWhenOffer.setVisibility(View.GONE);
                        menuViewHolder.mChildItemPriceWhenOffer.setPaintFlags(0);
                        menuViewHolder.mChildItemPrice.setText(mAdapterMenuAndChildItemsList.get(position).getPrice());
                    }

                } else {
                    menuViewHolder.price_txt.setVisibility(View.VISIBLE);
                    menuViewHolder.mChildItemPrice.setVisibility(View.GONE);
                }

                AppFunctions.imageLoaderUsingGlide(mAdapterMenuAndChildItemsList.get(position).getLogo(), menuViewHolder.mProductItemImage, getActivity());

                menuViewHolder.mChildMenuItemsContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (activity != null) {

                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                            ProductDetails productDetails = new ProductDetails();
                            Bundle mBundle = new Bundle();
                            mBundle.putString("vendor_id", vendor_info.getVendor().getVendorId());
                            mBundle.putString("vendor_name", vendor_info.getVendor().getName());
                            mBundle.putString("vendor_status", vendor_info.getVendor().getVendorStatus());
                            mBundle.putString("product_id", mAdapterMenuAndChildItemsList.get(position).getProductItemId());
                            mBundle.putString("product_name", mAdapterMenuAndChildItemsList.get(position).getItemName());
                            mBundle.putString("category_id", mAdapterMenuAndChildItemsList.get(position).getProductItemId());
                            mBundle.putString("latitude", vendor_info.getVendor().getLatitude());
                            mBundle.putString("longitude", vendor_info.getVendor().getLongitude());
                            mBundle.putString("product_desc", mAdapterMenuAndChildItemsList.get(position).getDescription());
                            mBundle.putString("product_image", mAdapterMenuAndChildItemsList.get(position).getImage());
                            mBundle.putString("product_minimum", mAdapterMenuAndChildItemsList.get(position).getPrice_status());
                            mBundle.putString("product_price", mAdapterMenuAndChildItemsList.get(position).getPrice());
                            mBundle.putString("product_discount_price",mAdapterMenuAndChildItemsList.get(position).getDiscount());
                            mBundle.putString("price_status", mAdapterMenuAndChildItemsList.get(position).getPrice_status());
                            mBundle.putSerializable("product_details", mAdapterMenuAndChildItemsList.get(position).getOptions());
                            productDetails.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_home_body, productDetails, "productDetails");
                            mFT.addToBackStack("productDetails");
                            mFT.commit();
                        }
                    }
                });
//                }
            }

        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public int getItemCount() {
            if (mAdapterMenuAndChildItemsList != null) {
                if (mAdapterMenuAndChildItemsList.size() > 0) {
                    return mAdapterMenuAndChildItemsList.size();
                } else {
                    return 1;
                }
            } else {
                return 1;
            }
        }

        @Override
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

        }

        public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            final TextView mParentItemName;
            final ImageView mProductItemImage;
            final ImageView mParentItemList;
            final ImageView item_type;
            View mChildBottomLine;

            TextView mChildItemName, price_txt;
            TextView mChildItemDescription;
            TextView mChildItemPrice;
            TextView mChildItemPriceWhenOffer;
//            private final TextView ; //,mChildOption,

            private final LinearLayout mChildMenuItemsContainer;
            private final LinearLayout mChildPriceContainer;

            // private ImageView mPriceBtn;

            public MenuViewHolder(View itemView) {
                super(itemView);

                mParentItemName = itemView.findViewById(R.id.tv_menu_list_parent_item_name);
                //mParentItemImage = (ImageView) itemView.findViewById(R.id.img_menu_list_parent_image);
                mProductItemImage = itemView.findViewById(R.id.img_menu_list_product_image);
                mParentItemList = itemView.findViewById(R.id.img_menu_list_parent_all_menu);

                mChildItemName = itemView.findViewById(R.id.tv_menu_list_child_item_name);
                mChildItemDescription = itemView.findViewById(R.id.tv_menu_list_child_item_description);
                mChildItemPrice = itemView.findViewById(R.id.tv_menu_list_child_amt);

                mChildBottomLine = itemView.findViewById(R.id.mChildBottomLine);
                price_txt = itemView.findViewById(R.id.price_txt);
                item_type = itemView.findViewById(R.id.item_type);
                item_type.setVisibility(View.GONE);

                mChildItemPriceWhenOffer = itemView.findViewById(R.id.tv_menu_list_child_amt_when_offer);
                mChildItemPriceWhenOffer.setVisibility(View.GONE);

                mChildMenuItemsContainer = itemView.findViewById(R.id.layout_restaurant_menu_child_container);
                mChildPriceContainer = itemView.findViewById(R.id.layout_menu_list_child_add_item);
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

    public static class CategoriesListBottom extends BottomSheetDialogFragment {

        FragmentCategoriesListBottomBinding binding;
        private NestedScrollView m__NestedScrollVIEW;
        SnappingLinearLayoutManager mSnappingLinearLayMgr;
        AppBarLayout m_AppBarLayout;

        public CategoriesListBottom(/*NestedScrollView nestedScrollVIEW*/SnappingLinearLayoutManager snappingLinearLayMgr, AppBarLayout appBarLayout) {
            // Required empty public constructor
            // this.m__NestedScrollVIEW = nestedScrollVIEW;
            this.mSnappingLinearLayMgr = snappingLinearLayMgr;
            this.m_AppBarLayout = appBarLayout;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
            if (getArguments() != null) {

            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
//            mView = inflater.inflate(R.layout.fragment_categories_list_bottom, container, false);
            binding = FragmentCategoriesListBottomBinding.inflate(inflater, container, false);

            binding.categoriesRecList.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
            binding.categoriesRecList.setAdapter(new CategoryListAdapter(vendor_info.getVendor().getCategory(), getDialog(), activity
                    /*,m__NestedScrollVIEW*/, mSnappingLinearLayMgr, m_AppBarLayout));

            binding.resName.setText(vendor_info.getVendor().getName());

            binding.closeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getDialog() != null) {
                        getDialog().dismiss();
                    }
                }
            });

            binding.progressBar.setVisibility(View.GONE);

            return binding.getRoot();
        }
    }

    public static class CategoryListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        ArrayList<Category> categoryArrayList;
        Dialog dialog;
        Activity activity;
        //NestedScrollView m_Nested_Scroll_VIEW;
        SnappingLinearLayoutManager mSnappingLinearLayMgr;
        AppBarLayout m_AppBarLayout;

        public CategoryListAdapter(ArrayList<Category> categoryArrayList, Dialog dialog, Activity activity/*,NestedScrollView nestedScrollVIEW*/,
                                   SnappingLinearLayoutManager snappingLinearLayMgr, AppBarLayout appBarLayout) {

            this.categoryArrayList = categoryArrayList;
            this.dialog = dialog;
            this.activity = activity;
            //this.m_Nested_Scroll_VIEW = nestedScrollVIEW;
            this.mSnappingLinearLayMgr = snappingLinearLayMgr;
            this.m_AppBarLayout = appBarLayout;

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_list, parent, false);
            return new DataObjectHolder(mView);
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            DataObjectHolder holder1 = (DataObjectHolder) holder;
            holder1.category_name.setText(categoryArrayList.get(position).getName());
            holder1.product_count.setText(categoryArrayList.get(position).getCount());
        }

        @Override
        public int getItemCount() {
            return categoryArrayList.size();
        }

        public class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            TextView category_name, product_count;
            FrameLayout frameLayout;

            public DataObjectHolder(View view) {
                super(view);
                category_name = view.findViewById(R.id.category_name);
                product_count = view.findViewById(R.id.product_count);
                frameLayout = view.findViewById(R.id.frame_layout_category);
                frameLayout.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.frame_layout_category) {
                    if (!categoryArrayList.get(getAdapterPosition()).getCount().equals("0")) {

                        if (mMenuAndChildItemsList != null && mSnappingLinearLayMgr != null) {
                            int mPositionShow = -1;
                            for (int catPosition = 0; catPosition < mMenuAndChildItemsList.size(); catPosition++) {
                                if (mMenuAndChildItemsList.get(catPosition).getParentMenu()) {
                                    String mParentSecId = mMenuAndChildItemsList.get(catPosition).getParentSectionId();
                                    String mCurrentSecId = categoryArrayList.get(getAdapterPosition()).getProductCategoryId();
                                    if (mParentSecId.equals(mCurrentSecId)) {
                                        mPositionShow = catPosition;
                                        break;
                                    }
                                }
                            }
                            if (mPositionShow != -1) {
                                //mTempMenuTitleName = holder.mMenuTitleName;
                                mSnappingLinearLayMgr.scrollToPositionWithOffset(mPositionShow, 0);
                            }
                        }

                        ArrayList<MenuCategorySelectionDataSet> mMenuCategorySelectionList = new ArrayList<>();
                        for (int menu = 0; menu < mMenuAndChildItemsList.size(); menu++) {
                            if (mMenuAndChildItemsList.get(menu).getParentMenu()) {
                                MenuCategorySelectionDataSet mMenuCategorySelectionDs = new MenuCategorySelectionDataSet();
                                Category mMenuDs = new Category();
                                mMenuDs.setName(mMenuAndChildItemsList.get(menu).getParentName());
                                mMenuDs.setProductCategoryId(mMenuAndChildItemsList.get(menu).getParentSectionId());
                                mMenuDs.setCount(mMenuAndChildItemsList.get(menu).getParentChildItemsCount());
                                mMenuCategorySelectionDs.setSectionDataSet(mMenuDs);
                                mMenuCategorySelectionDs.setPosition(menu);
                                mMenuCategorySelectionList.add(mMenuCategorySelectionDs);
                            }
                        }

                        Objects.requireNonNull(tabLayout.getTabAt(getAdapterPosition())).select();

//                        Objects.requireNonNull(tabLayout.getTabAt(getAdapterPosition())).select();
//                        smoothScroller.setTargetPosition(getAdapterPosition());
//                        Objects.requireNonNull(recycler_restaurant_menu_list.getLayoutManager()).startSmoothScroll(smoothScroller);

//                        mTitleListAdapter = new TitleListAdapter(mMenuCategorySelectionList, activity, getAdapterPosition(),
//                                true, mMenuAndChildItemsList/*,m_Nested_Scroll_VIEW*/, mSnappingLinearLayMgr, m_AppBarLayout);
//                        category_title_list.setAdapter(mTitleListAdapter);
//                        category_title_list.smoothScrollToPosition(getAdapterPosition());

                    }
                    dialog.dismiss();
                }
            }
        }

    }

}
