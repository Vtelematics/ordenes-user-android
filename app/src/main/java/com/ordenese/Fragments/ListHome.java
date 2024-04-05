package com.ordenese.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.onesignal.OneSignal;
import com.ordenese.Activities.AppHome;
import com.ordenese.Activities.AppLogin;
import com.ordenese.Adapters.BannerAdapter;
import com.ordenese.ApiConnection.ApiClient;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.HomeBannerScroller;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.AddressListApi;
import com.ordenese.DataSets.AddressListDataSet;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.BannersDataSet;
import com.ordenese.DataSets.BestOffersDataSet;
import com.ordenese.DataSets.BrandsDataSet;
import com.ordenese.DataSets.BusinessTypesDataSet;
import com.ordenese.DataSets.DrinksDataSet;
import com.ordenese.DataSets.HomeModulesApi;
import com.ordenese.DataSets.ListDataSet;
import com.ordenese.DataSets.TopPickDataSet;
import com.ordenese.Databases.ARCuisinesDB;
import com.ordenese.Databases.ARFiltersDB;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.DeliveryLocationSearchDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.Interfaces.MakeBottomMarginForViewBasket;
import com.ordenese.Interfaces.onSetUpdate;
import com.ordenese.R;
import com.ordenese.databinding.HomeDeliveryLocAndAddsBottomSheetBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListHome extends Fragment implements View.OnClickListener, onSetUpdate {

    CartInfo cartInfo;
    Activity activity;
    View mRestaurantListView;
    TextView mLocationChange, mLocationChangeNowTitle;
    LinearLayout mLayoutSortBy;
    private LinearLayout mRestaurantOptionsBar, mLayMenuLogoutParent;
    private DrawerLayout mMenuListDrawer;
    private AppBarLayout mPageHeader;
    private ProgressDialog mProgressDialog;
    ImageView mHomeSideMenuAppBarImg, mHomeLocationChangeImg, mHomeSearchAppBarAppBarImg;
    private RecyclerView mRecyclerHomeParent;
    private RecyclerView.LayoutManager mRestaurantHomeParentLayoutMgr;
    private HomeParentListAdapter mHomeParentListAdapter;
    private RetrofitInterface retrofitInterface;
    private Boolean mIsAppBarMenuOpened = false;
    ImageView mImgDeliveryLocation;
    private CircleImageView mProfileImg;
    private TextView mLoggedUserName;
    LinearLayout mLayProfileDetails, mLayMyAddresses, mLayMyOrders, mLayChangePwd,
            mLayContactUs, mLayAboutUs, mLayPrivacyPolicy, mLayTermsAndConditions, mLayChangeLanguage, mLayDeleteAccount, my_favouritesLayout;

    private MakeBottomMarginForViewBasket mMakeBottomMarginForViewBasket;

    private AddressListApi mAddressListApi;
    Fragment fragment;
    private static onSetUpdate update;

    public ListHome() {
        // Required empty public constructor
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


    @Override
    public void onAttach(Context context) {
        super.onAttach(AppLanguageSupport.onAttach(context));
        if (getActivity() != null) {
            getActivity().getWindow().getDecorView().setLayoutDirection(
                    "ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(getActivity())) ?
                            View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
        cartInfo = (CartInfo) context;
        activity = getActivity();
        fragment = this;
        update = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRestaurantListView = inflater.inflate(R.layout.list_home, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mMakeBottomMarginForViewBasket = (MakeBottomMarginForViewBasket) getActivity();

        /*lay_restaurant_list_location_change
                    start_search
                    tv_restaurant_list_location_change
                    img_restaurant_list_location_change*/

        /* AreaGeoCodeDataSet mAreaGeoCodeDS = new AreaGeoCodeDataSet();
        mAreaGeoCodeDS.setmAddress("");
        // mAreaGeoCodeDS.setmLatitude(String.valueOf(Latitude));
        //  mAreaGeoCodeDS.setmLongitude(String.valueOf(Longitude));
        mAreaGeoCodeDS.setmLatitude("11.026830147707603");
        mAreaGeoCodeDS.setmLongitude("76.90551660954952");
        // //11.026983233187956, 76.90567428944063
        mAreaGeoCodeDS.setmNewAdds("");
        if (AreaGeoCodeDB.getInstance(getActivity()).isAreaGeoCodeSelected()) {
            AreaGeoCodeDB.getInstance(getActivity()).updateUserAreaGeoCode(mAreaGeoCodeDS);
        } else {
            AreaGeoCodeDB.getInstance(getActivity()).addUserAreaGeoCode(mAreaGeoCodeDS);
        }
*/

        /*if (!LanguageDetailsDB.getInstance(AppSplashScreen.this).check_language_selected()) {
            LanguageDataSet mLanguageDataSet = new LanguageDataSet();
            mLanguageDataSet.setLanguageId("1");
            mLanguageDataSet.setCode("en");
            LanguageDetailsDB.getInstance(AppSplashScreen.this).insert_language_detail(mLanguageDataSet);
        }*/

        /*ImageView imageView = mRestaurantListView.findViewById(R.id.img_app_bar_app_logo);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity() != null) {
                    if (LanguageDetailsDB.getInstance(getActivity()).check_language_selected()) {

                        //******************************************************************************

                        String mCurrentLanguageId = LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId();
                        LanguageDataSet mLanguageDataSet = new LanguageDataSet();

                        if(mCurrentLanguageId.equals("1")){
                            mLanguageDataSet.setLanguageId("3");
                            mLanguageDataSet.setCode("ae");
                            AppLanguageSupport.setLocale(getActivity(), "ae");
                        }else {
                            mLanguageDataSet.setLanguageId("1");
                            mLanguageDataSet.setCode("en");
                            AppLanguageSupport.setLocale(getActivity(), "en");
                        }
                        LanguageDetailsDB.getInstance(getActivity()).delete_language_detail();
                        LanguageDetailsDB.getInstance(getActivity()).insert_language_detail(mLanguageDataSet);

                        Intent intent = new Intent(getActivity(), AppHome.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        getActivity().finish();

                        //******************************************************************************

                    }

                }
            }
        });*/

        mPageHeader = mRestaurantListView.findViewById(R.id.app_bar_restaurant_store_list_layout);

        mRestaurantOptionsBar = mRestaurantListView.findViewById(R.id.layout_restaurant_list_options_bar);

        mLayoutSortBy = mRestaurantListView.findViewById(R.id.layout_restaurant_list_option_sort_by);
        mLayoutSortBy.setOnClickListener(this);

        mImgDeliveryLocation = mRestaurantListView.findViewById(R.id.start_search);
        mImgDeliveryLocation.setOnClickListener(this);
        mLocationChange = mRestaurantListView.findViewById(R.id.tv_restaurant_list_location_change);
        mLocationChange.setOnClickListener(this);
        mLocationChangeNowTitle = mRestaurantListView.findViewById(R.id.tv_restaurant_list_location_change_now);
        mLocationChangeNowTitle.setOnClickListener(this);

        mRecyclerHomeParent = mRestaurantListView.findViewById(R.id.recycler_home_parent);

        mHomeSideMenuAppBarImg = mRestaurantListView.findViewById(R.id.img_restaurant_list_menu);
        mHomeSideMenuAppBarImg.setOnClickListener(this);
        mHomeSearchAppBarAppBarImg = mRestaurantListView.findViewById(R.id.img_restaurant_list_search_top);
        mHomeSearchAppBarAppBarImg.setOnClickListener(this);


        mHomeLocationChangeImg = mRestaurantListView.findViewById(R.id.img_restaurant_list_location_change);
        mHomeLocationChangeImg.setOnClickListener(this);

        mMenuListDrawer = mRestaurantListView.findViewById(R.id.drawer_layout_restaurant_list);

        mLayMenuLogoutParent = mRestaurantListView.findViewById(R.id.layout_lh_menu_logout_parent);
        mLayMenuLogoutParent.setOnClickListener(this);
        mLayMenuLogoutParent.setVisibility(View.GONE);

        mProfileImg = mRestaurantListView.findViewById(R.id.img_View_lh_profile_img);
        mLoggedUserName = mRestaurantListView.findViewById(R.id.tv_lh_user_name);
        mLayProfileDetails = mRestaurantListView.findViewById(R.id.lay_lh_profile_details_container);
        mLayProfileDetails.setOnClickListener(this);

        mLayMyAddresses = mRestaurantListView.findViewById(R.id.layout_lh_menu_my_addresses_parent);
        mLayMyAddresses.setOnClickListener(this);
        mLayMyOrders = mRestaurantListView.findViewById(R.id.layout_lh_menu_my_orders_parent);
        mLayMyOrders.setOnClickListener(this);
        mLayChangePwd = mRestaurantListView.findViewById(R.id.layout_lh_menu_change_pwd_parent);
        mLayChangePwd.setOnClickListener(this);
        mLayContactUs = mRestaurantListView.findViewById(R.id.layout_lh_menu_contact_us_parent);
        mLayContactUs.setOnClickListener(this);
        mLayAboutUs = mRestaurantListView.findViewById(R.id.layout_lh_menu_about_us_parent);
        mLayAboutUs.setOnClickListener(this);
        mLayPrivacyPolicy = mRestaurantListView.findViewById(R.id.layout_lh_menu_privacy_policy_parent);
        mLayPrivacyPolicy.setOnClickListener(this);
        mLayTermsAndConditions = mRestaurantListView.findViewById(R.id.layout_lh_menu_terms_and_conditions_parent);
        mLayTermsAndConditions.setOnClickListener(this);

        mLayChangeLanguage = mRestaurantListView.findViewById(R.id.layout_lh_menu_change_language_parent);
        mLayChangeLanguage.setOnClickListener(this);
        mLayDeleteAccount = mRestaurantListView.findViewById(R.id.layout_lh_menu_delete_account_parent);
        mLayDeleteAccount.setOnClickListener(this);
        mLayDeleteAccount.setVisibility(View.GONE);

        my_favouritesLayout = mRestaurantListView.findViewById(R.id.layout_lh_menu_my_favourites_parent);
        my_favouritesLayout.setOnClickListener(this);

        return mRestaurantListView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void menuDrawer() {
        //  mNavigationDrawerListener.drawerListener(mDrawerLayout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mMenuListDrawer, R.string.drawer_open, R.string.drawer_close) {
            //  ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, mToolbar,) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);

                menuDrawerLock(true);

                boolean mFullyExpanded = (mPageHeader.getHeight() - mPageHeader.getBottom()) == 0;
                if (mFullyExpanded) {
                    mPageHeader.setExpanded(true);
                }

                mIsAppBarMenuOpened = false;


            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank

                super.onDrawerOpened(drawerView);


                //To check restaurant list in empty or not :-

                mPageHeader.setExpanded(false);

            }
        };

        //Setting the actionbarToggle to drawer layout
        mMenuListDrawer.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    private void menuDrawerCloser() {
        if (mMenuListDrawer.isDrawerOpen(GravityCompat.END)) {
            mMenuListDrawer.closeDrawer(GravityCompat.END);
        }
    }

    private void menuDrawerLock(Boolean isRequire) {
        if (isRequire) {
            mMenuListDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            mMenuListDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (!AppFunctions.networkAvailabilityCheck(activity)) {
            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
            NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
            mFT.replace(R.id.layout_home_restaurant_body, mNetworkAnalyser, "mNetworkAnalyser");
            mFT.addToBackStack("mNetworkAnalyser");
            mFT.commit();
        } else {
            cart_product_count();
        }

        if (getActivity() != null) {
            if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                //User currently logged in :-

                mLayDeleteAccount.setVisibility(View.VISIBLE);
                mLayMenuLogoutParent.setVisibility(View.VISIBLE);
                String mImg = UserDetailsDB.getInstance(getActivity()).getUserDetails().getImage();
                if (mImg != null && !mImg.isEmpty()) {
                    //Toast.makeText(getContext(), "Image "+mImg, Toast.LENGTH_SHORT).show();
//                    Log.e("Image path ",mImg);
                    String mFinalPath = mImg.replace("\\", "");
                    //  Log.e("mImg",mImg);
                    //  Log.e("mFinalPath",mFinalPath);
                    Glide.with(getActivity()).load(mFinalPath).into(mProfileImg);
                } else {
                    mProfileImg.setImageResource(R.drawable.svg_account_circle_48dp);
                }
                String mUserName = UserDetailsDB.getInstance(getActivity()).getUserDetails().getFirstName() +
                        " " + UserDetailsDB.getInstance(getActivity()).getUserDetails().getLastName();
                mLoggedUserName.setText(mUserName);
            } else {
                //User currently logged out :-
                mLayDeleteAccount.setVisibility(View.GONE);
                mLayMenuLogoutParent.setVisibility(View.GONE);
                mProfileImg.setImageResource(R.drawable.svg_account_circle_48dp);
                mLoggedUserName.setText(getActivity().getResources().getString(R.string.login_to));
            }
            if (AreaGeoCodeDB.getInstance(getActivity()).isAreaGeoCodeSelected()) {
                mLocationChange.setText(AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode().getmAddress());
                mLocationChange.setSelected(true);
            } else {
                mLocationChange.setText(getActivity().getResources().getString(R.string.set_delivery_location));
            }

            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                menuDrawer();
                callHomeModulesList();
                mRestaurantOptionsBar.setVisibility(View.VISIBLE);
            } else {
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }

    }

    private void cart_product_count() {

        if (AppFunctions.networkAvailabilityCheck(activity)) {

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
                            JSONObject object = new
                                    JSONObject(response.body());
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

    private void delete_cart() {

        if (AppFunctions.networkAvailabilityCheck(activity)) {
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
                object.put(DefaultNames.vendor_id, "");

                RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());
                retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                String mCustomerAuthorization = "";
                if (AppFunctions.isUserLoggedIn(getActivity())) {
                    mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                }

                Call<String> call = retrofitInterface.clear_cart(mCustomerAuthorization, body);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {

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

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onStop() {
        super.onStop();
        cartInfo.cart_info(false, "", "");
        if (mHomeParentListAdapter != null) {
            mHomeParentListAdapter.stopThread();
        }
        mMakeBottomMarginForViewBasket.toMakeBottomMarginForViewBasket(false);

    }

    public void callHomeModulesList() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                    jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                    jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    jsonObject.put("order_type", OrderTypeDB.getInstance(activity).getUserServiceType());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<HomeModulesApi> Call = retrofitInterface.homeModules(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<HomeModulesApi>() {
                        @Override
                        public void onResponse(@NonNull Call<HomeModulesApi> call, @NonNull Response<HomeModulesApi> response) {

                            mProgressDialog.cancel();

                            if (response.isSuccessful()) {
                                HomeModulesApi mHomeModulesApi = response.body();

                                if (mHomeModulesApi != null) {
                                    if (mHomeModulesApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            mRestaurantHomeParentLayoutMgr = new LinearLayoutManager(getActivity());
                                            mRecyclerHomeParent.setLayoutManager(mRestaurantHomeParentLayoutMgr);
                                            mHomeParentListAdapter = new HomeParentListAdapter(
                                                    getParentFragmentManager(), getActivity(),
                                                    getActivity().getSupportFragmentManager(), mHomeModulesApi);
                                            mRecyclerHomeParent.setAdapter(mHomeParentListAdapter);
                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mHomeModulesApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mHomeModulesApi.error.message);
                                            }
                                        }
                                    }
                                } else {
                                    //Log.e("mHomeModulesApi", "null");
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
                        public void onFailure(@NonNull Call<HomeModulesApi> call, @NonNull Throwable t) {

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
                mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }


    }

    private void mLogE(String title, String msg) {

        // //Log.e(title,msg);

    }

    @Override
    public void onClick(View v) {
        int mRestaurantViewId = v.getId();


        if (mRestaurantViewId == R.id.start_search ||
                mRestaurantViewId == R.id.tv_restaurant_list_location_change ||
                mRestaurantViewId == R.id.img_restaurant_list_location_change ||
                mRestaurantViewId == R.id.tv_restaurant_list_location_change_now) {

            //Log.e("&&&&&&&&& 416", "called");

            if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                //User currently logged in :-
                menuDrawerCloser();
                callAddressListApi();
            } else {
                //User not logged-in currently so no need to inflate bottom
                //menu.Because we need to show address list along with
                //delivery location page link.
                deliveryLocationPage();
            }


        } else if (mRestaurantViewId == R.id.img_restaurant_list_menu) {

            if (mIsAppBarMenuOpened) {
                mIsAppBarMenuOpened = false;
                if (mMenuListDrawer.isDrawerOpen(GravityCompat.END)) {
                    mMenuListDrawer.closeDrawer(GravityCompat.END);
                }
            } else {
                mIsAppBarMenuOpened = true;
                mMenuListDrawer.openDrawer(GravityCompat.END);
            }


        } else if (mRestaurantViewId == R.id.img_restaurant_list_search_top) {

            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
            SearchRestaurants m_searchRestaurants = new SearchRestaurants();
            mFT.replace(R.id.layout_app_home_body, m_searchRestaurants, "m_searchRestaurants");
            mFT.addToBackStack("m_searchRestaurants");
            mFT.commit();


        } else if (mRestaurantViewId == R.id.lay_lh_profile_details_container) {

            if (getActivity() != null) {
                menuDrawerCloser();
                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    //User currently logged in :-
                    cartInfo.cart_info(false, "", "");


                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    EditProfile m_editProfile = new EditProfile();
                    mFT.replace(R.id.layout_app_home_body, m_editProfile, "m_editProfile");
                    mFT.addToBackStack("m_editProfile");
                    mFT.commit();


                } else {
                    AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                    Intent intent = new Intent(getActivity(), AppLogin.class);
                    getActivity().startActivity(intent);
                }

            }

        } else if (mRestaurantViewId == R.id.layout_lh_menu_my_orders_parent) {
            if (getActivity() != null) {
                menuDrawerCloser();
                //User currently logged in :-
                cartInfo.cart_info(false, "", "");

                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                MyOrderList m_myOrderList = new MyOrderList();
                mFT.replace(R.id.layout_app_home_body, m_myOrderList, "m_myOrderList");
                mFT.addToBackStack("m_myOrderList");
                mFT.commit();
            }
        } else if (mRestaurantViewId == R.id.layout_lh_menu_my_addresses_parent) {
            if (getActivity() != null) {
                menuDrawerCloser();
                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    //User currently logged in :-
                    cartInfo.cart_info(false, "", "");
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    CheckoutAddressList m_CheckoutAddressList = new CheckoutAddressList();
                    Bundle mBundle = new Bundle();
                    mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_MyAccountAddsBook);
                    m_CheckoutAddressList.setArguments(mBundle);
                    mFT.replace(R.id.layout_app_home_body, m_CheckoutAddressList, "m_CheckoutAddressList");
                    mFT.addToBackStack("m_CheckoutAddressList");
                    mFT.commit();
                } else {
                    AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                    Intent intent = new Intent(getActivity(), AppLogin.class);
                    getActivity().startActivity(intent);
                }
            }
        } else if (mRestaurantViewId == R.id.layout_lh_menu_change_pwd_parent) {
            menuDrawerCloser();
            if (getActivity() != null) {
                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    //User currently logged in :-
                    cartInfo.cart_info(false, "", "");
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    ChangePassword m_changePassword = new ChangePassword();
                    mFT.replace(R.id.layout_app_home_body, m_changePassword, "m_changePassword");
                    mFT.addToBackStack("m_changePassword");
                    mFT.commit();
                } else {
                    AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                    Intent intent = new Intent(getActivity(), AppLogin.class);
                    getActivity().startActivity(intent);
                }
            }
        } else if (mRestaurantViewId == R.id.layout_lh_menu_contact_us_parent) {

            menuDrawerCloser();

            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
            ContactUs m_contactUs = new ContactUs();
            mFT.replace(R.id.layout_app_home_body, m_contactUs, "m_contactUs");
            mFT.addToBackStack("m_contactUs");
            mFT.commit();

        } else if (mRestaurantViewId == R.id.layout_lh_menu_my_favourites_parent) {

            menuDrawerCloser();

            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
            FavouriteListFragment favouriteList = new FavouriteListFragment();
            mFT.replace(R.id.layout_app_home_body, favouriteList, "favouriteList");
            mFT.addToBackStack("favouriteList");
            mFT.commit();

        } else if (mRestaurantViewId == R.id.layout_lh_menu_about_us_parent) {

            menuDrawerCloser();

            DialogueWebView mDialogueWebView = new DialogueWebView();
            Bundle mBundle = new Bundle();
            mBundle.putString(DefaultNames.from, DefaultNames.AboutUs);
            mBundle.putString(DefaultNames.thePageCallFrom, DefaultNames.thePageCall_ForHomeLay);
            mDialogueWebView.setArguments(mBundle);
            mDialogueWebView.show(getParentFragmentManager(), "mDialogueWebView");

        } else if (mRestaurantViewId == R.id.layout_lh_menu_privacy_policy_parent) {

            menuDrawerCloser();

            DialogueWebView mDialogueWebView = new DialogueWebView();
            Bundle mBundle = new Bundle();
            mBundle.putString(DefaultNames.from, DefaultNames.PrivacyPolicy);
            mBundle.putString(DefaultNames.thePageCallFrom, DefaultNames.thePageCall_ForHomeLay);
            mDialogueWebView.setArguments(mBundle);
            mDialogueWebView.show(getParentFragmentManager(), "mDialogueWebView");

        } else if (mRestaurantViewId == R.id.layout_lh_menu_terms_and_conditions_parent) {

            menuDrawerCloser();

            DialogueWebView mDialogueWebView = new DialogueWebView();
            Bundle mBundle = new Bundle();
            mBundle.putString(DefaultNames.from, DefaultNames.TermsAndConditions);
            mBundle.putString(DefaultNames.thePageCallFrom, DefaultNames.thePageCall_ForHomeLay);
            mDialogueWebView.setArguments(mBundle);
            mDialogueWebView.show(getParentFragmentManager(), "mDialogueWebView");

        } else if (mRestaurantViewId == R.id.layout_lh_menu_change_language_parent) {
            // AppFunctions.toastShort(getActivity(),"change_language");


            if (getActivity() != null) {

                menuDrawerCloser();

                Language m_language = new Language();
                m_language.show(getParentFragmentManager(), "m_language");

            }


        } else if (mRestaurantViewId == R.id.layout_lh_menu_delete_account_parent) {
            if (getActivity() != null) {
                menuDrawerCloser();
                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder
                            .setMessage(getActivity().getString(R.string.delete_account_warning_msg))
                            .setCancelable(false)
                            .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                    DeleteAccount m_DeleteAccount = new DeleteAccount();
                                    mFT.replace(R.id.layout_app_home_body, m_DeleteAccount, "m_DeleteAccount");
                                    mFT.addToBackStack("m_DeleteAccount");
                                    mFT.commit();

                                }
                            })
                            .setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                    Intent intent = new Intent(getActivity(), AppLogin.class);
                    getActivity().startActivity(intent);
                }
            }
        } else if (mRestaurantViewId == R.id.layout_lh_menu_logout_parent) {
            log_out();
        }
    }

    private void log_out() {
        mProgressDialog.show();
        if (AppFunctions.networkAvailabilityCheck(activity)) {
            JSONObject object = new JSONObject();
            try {
                if (OneSignal.getDeviceState() != null && OneSignal.getDeviceState().getUserId() != null) {
                    object.put(DefaultNames.push_id, OneSignal.getDeviceState().getUserId());
                } else {
                    object.put(DefaultNames.push_id, "");
                }
                RequestBody body = RequestBody.create(MediaType.parse("application/json"), object.toString());
                retrofitInterface = ApiClient.getClient().create(RetrofitInterface.class);
                String mCustomerAuthorization = "";
                if (AppFunctions.isUserLoggedIn(getActivity())) {
                    mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                }
                Call<String> call = retrofitInterface.log_out(mCustomerAuthorization, body);
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body());
                                if (!jsonObject.isNull("success")) {
                                    if (getActivity() != null) {
                                        cartInfo.cart_info(false, "", "");
                                        if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                            //User currently logged in :-
                                            UserDetailsDB.getInstance(getActivity()).deleteUserDetailsDB();
                                        }
                                        Intent intent = new Intent(getActivity(), AppHome.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        getActivity().finish();
                                    }
                                    JSONObject object1 = jsonObject.getJSONObject("success");
                                    AppFunctions.toastShort(activity, object1.getString("message"));
                                } else if (!jsonObject.isNull("error")) {
                                    JSONObject object1 = jsonObject.getJSONObject("error");
                                    AppFunctions.toastShort(activity, object1.getString("message"));
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

    private void deliveryLocationPage() {

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

    }

    private void cToast(String title, String value) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), title + " - " + value, Toast.LENGTH_SHORT).show();
        }
    }

    //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
    //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&
    //&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

    private void callAddressListApi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();

                try {

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                    mProgressDialog.show();
                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<AddressListApi> Call = retrofitInterface.addressListsApi(mCustomerAuthorization, body);
                    Call.enqueue(new Callback<AddressListApi>() {
                        @Override
                        public void onResponse(@NonNull Call<AddressListApi> call, @NonNull Response<AddressListApi> response) {

                            mProgressDialog.cancel();

                            if (getActivity() != null) {

                                if (response.isSuccessful()) {
                                    mAddressListApi = response.body();

                                    if (mAddressListApi != null) {
                                        //Log.e("mAddressListApi", "not null");
                                        if (mAddressListApi.success != null) {
                                            //Api response successDataSet :-

                                            if (mAddressListApi.addressList != null && mAddressListApi.addressList.size() > 0) {

                                                //RecyclerView.LayoutManager mRestaurantHomeParentLayoutMgr
                                                //RecyclerView mRecyclerHomeParent
                                                //HomeParentListAdapter mHomeParentListAdapter

                                                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                                DeliveryLocationAndAddsListBottomSheet mDLAndALBottomSheet =
                                                        new DeliveryLocationAndAddsListBottomSheet(mMenuListDrawer,
                                                                retrofitInterface, mProgressDialog, mRestaurantHomeParentLayoutMgr, mRecyclerHomeParent,
                                                                mHomeParentListAdapter, mLocationChange);
                                                Bundle bundle = new Bundle();
                                                bundle.putSerializable(DefaultNames.address_list, mAddressListApi.addressList);
                                                mDLAndALBottomSheet.setArguments(bundle);
                                                mDLAndALBottomSheet.show(mFT, "mDLAndALBottomSheet");
                                            } else {
                                                toPerformWhenAddressEmpty();
                                            }
                                        } else {
                                            toPerformWhenAddressEmpty();
                                        }
                                    } else {
                                        toPerformWhenAddressEmpty();
                                    }
                                } else {
                                    toPerformWhenAddressEmpty();
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<AddressListApi> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                            toPerformWhenAddressEmpty();

                        }
                    });
                } catch (Exception e) {
                    toPerformWhenAddressEmpty();
                    mProgressDialog.cancel();
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

    private void toPerformWhenAddressEmpty() {
        deliveryLocationPage();
    }

    @Override
    public void reload() {
        callHomeModulesList();
    }

    public static class DeliveryLocationAndAddsListBottomSheet extends BottomSheetDialogFragment {

        HomeDeliveryLocAndAddsBottomSheetBinding binding_;
        ArrayList<AddressListDataSet> mAddressLIST;
        DrawerLayout m_Menu_List_Drawer;
        RetrofitInterface mRetrofitInterface;
        ProgressDialog m_ProgressDialog;

        RecyclerView.LayoutManager m_RestaurantHomeParentLayoutMgr;
        RecyclerView m_RecyclerHomeParent;
        HomeParentListAdapter m_HomeParentListAdapter;
        TextView m_Location_Change;

        //RecyclerView.LayoutManager mRestaurantHomeParentLayoutMgr
        //RecyclerView mRecyclerHomeParent
        //HomeParentListAdapter mHomeParentListAdapter

        public DeliveryLocationAndAddsListBottomSheet(DrawerLayout menu_List_Drawer, RetrofitInterface retrofitInterface,
                                                      ProgressDialog progressDialog, RecyclerView.LayoutManager restaurantHPLMgr,
                                                      RecyclerView recyclerHomeParent, HomeParentListAdapter homeParentListAdapter,
                                                      TextView mLocationChange) {
            // Required empty public constructor
            this.m_Menu_List_Drawer = menu_List_Drawer;
            this.mRetrofitInterface = retrofitInterface;
            this.m_ProgressDialog = progressDialog;
            this.m_RestaurantHomeParentLayoutMgr = restaurantHPLMgr;
            this.m_RecyclerHomeParent = recyclerHomeParent;
            this.m_HomeParentListAdapter = homeParentListAdapter;
            this.m_Location_Change = mLocationChange;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme);
            if (getArguments() != null) {
                mAddressLIST = (ArrayList<AddressListDataSet>) getArguments().getSerializable(DefaultNames.address_list);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            binding_ = HomeDeliveryLocAndAddsBottomSheetBinding.inflate(inflater, container, false);
            binding_.recyclerHDlAalBs.setLayoutManager(new LinearLayoutManager(getActivity()));
            binding_.recyclerHDlAalBs.setAdapter(new LocationAddressListAdapter(mAddressLIST));

//            binding_.layChooseLocOnMap.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (getActivity() != null) {
//                        //the DeliveryLocationSearchDB for to save  DeliveryLocationSearch page process.
//                        //And its used for DeliveryLocation page only.
//                        //So every time must refresh the DB before going to DeliveryLocation page.
//                        if (DeliveryLocationSearchDB.getInstance(getActivity()).getSizeOfList() > 0) {
//                            DeliveryLocationSearchDB.getInstance(getActivity()).deleteDB();
//                        }
//                        FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
//                        DeliveryLocation m_deliveryLocation = new DeliveryLocation();
//                        mFT.replace(R.id.layout_app_home_body, m_deliveryLocation, "m_deliveryLocation");
//                        mFT.addToBackStack("m_deliveryLocation");
//                        mFT.commit();
//
//                        if (getDialog() != null) {
//                            getDialog().dismiss();
//                        }
//
//                    }
//                }
//            });

            return binding_.getRoot();
        }

        private void menuDrawerCloser() {
            if (m_Menu_List_Drawer.isDrawerOpen(GravityCompat.END)) {
                m_Menu_List_Drawer.closeDrawer(GravityCompat.END);
            }
        }

        public void callHomeModulesApiForDeliveryAvailableCheck(AreaGeoCodeDataSet areaGeoCodeDataSet) {

            if (getActivity() != null) {
                if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                        jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());
                        jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                        jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                        jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                        jsonObject.put("order_type", OrderTypeDB.getInstance(getActivity()).getUserServiceType());

                        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                        mRetrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                        Call<HomeModulesApi> Call = mRetrofitInterface.homeModules(body);
                        m_ProgressDialog.show();
                        Call.enqueue(new Callback<HomeModulesApi>() {
                            @Override
                            public void onResponse(@NonNull Call<HomeModulesApi> call, @NonNull Response<HomeModulesApi> response) {
                                if (getActivity() != null) {
                                    m_ProgressDialog.cancel();
                                    if (response.isSuccessful()) {
                                        HomeModulesApi mHomeModulesApi = response.body();
                                        if (mHomeModulesApi != null) {
                                            if (mHomeModulesApi.success != null) {
                                                //Api response successDataSet :-
                                                if (getActivity() != null) {
                                                    // **********  ****************  ************  ***********

                                                    if (AreaGeoCodeDB.getInstance(getActivity()).isAreaGeoCodeSelected()) {
                                                        AreaGeoCodeDB.getInstance(getActivity()).updateUserAreaGeoCode(areaGeoCodeDataSet);
                                                    } else {
                                                        AreaGeoCodeDB.getInstance(getActivity()).addUserAreaGeoCode(areaGeoCodeDataSet);
                                                    }

                                                    AreaGeoCodeDB.getInstance(getActivity()).print();


                                                    //Here , DeliveryLocationSearchDB DB need is over when this button pressed.
                                                    //So delete the DB details if exist.
                                                    if (DeliveryLocationSearchDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                                        DeliveryLocationSearchDB.getInstance(getActivity()).deleteDB();
                                                    }

                                                    m_Location_Change.setText(areaGeoCodeDataSet.getmAddress());

                                                    //m_RestaurantHomeParentLayoutMgr
                                                    //m_RecyclerHomeParent
                                                    //m_HomeParentListAdapter

                                                    if (getActivity() != null) {
                                                        m_RestaurantHomeParentLayoutMgr = new LinearLayoutManager(getActivity());
                                                        m_RecyclerHomeParent.setLayoutManager(m_RestaurantHomeParentLayoutMgr);
                                                        m_HomeParentListAdapter = new HomeParentListAdapter(
                                                                getParentFragmentManager(), getActivity(),
                                                                getActivity().getSupportFragmentManager(), mHomeModulesApi);
                                                        m_RecyclerHomeParent.setAdapter(m_HomeParentListAdapter);
                                                    }

                                                    if (getDialog() != null) {
                                                        getDialog().dismiss();
                                                    }


                                                    //*********** *************** ************* ********** ****
                                                }
                                            } else {
                                                //Api response failure :-
                                                if (getActivity() != null) {
                                                    if (mHomeModulesApi.error != null) {
                                                        AppFunctions.msgDialogOk(getActivity(), "", getResources().getString(R.string.service_not_available_for_this_location));
                                                    }
                                                }
                                            }
                                        } else {

                                        }
                                    } else {
                                        m_ProgressDialog.cancel();
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
                            public void onFailure(@NonNull Call<HomeModulesApi> call, @NonNull Throwable t) {
                                m_ProgressDialog.cancel();
                            }
                        });

                    } catch (JSONException e) {
                        m_ProgressDialog.cancel();
                        e.printStackTrace();
                    }

                } else {

                    //Its delivery location process :-
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                    mFT.addToBackStack("mNetworkAnalyser");
                    mFT.commit();

                }
            }
        }

        public class LocationAddressListAdapter extends RecyclerView.Adapter<LocationAddressListAdapter.DataObjectHolder> {

            private final ArrayList<AddressListDataSet> mAddressesList;

            public LocationAddressListAdapter(ArrayList<AddressListDataSet> addressesList) {
                this.mAddressesList = addressesList;
            }

            @Override
            public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_adds_list_row, parent, false);
                return new DataObjectHolder(mView);
            }


            @Override
            public void onBindViewHolder(final DataObjectHolder holder, final int position) {

                String mAdds = "";

                if(position == mAddressesList.size()-1){
                    holder.lay_choose_loc_on_map.setVisibility(View.VISIBLE);
                }else {
                    holder.lay_choose_loc_on_map.setVisibility(View.GONE);
                }

                String mBlock = mAddressesList.get(position).getBlock();
                String mStreet = mAddressesList.get(position).getStreet();
                String mBuilding = mAddressesList.get(position).getBuilding_name();
                String mWay = mAddressesList.get(position).getWay();
                String mFloor = mAddressesList.get(position).getFloor();
                String mDoorNo = mAddressesList.get(position).getDoor_no();
                String mArea = mAddressesList.get(position).getArea();


                String mDeliveryAdds = "";

                if (mBlock != null && !mBlock.isEmpty()) {
                    mDeliveryAdds = "" + mBlock;
                }

                if (mStreet != null && !mStreet.isEmpty()) {
                    if (mDeliveryAdds.length() > 0) {
                        mDeliveryAdds = mDeliveryAdds + "," + mStreet;
                    } else {
                        mDeliveryAdds = "" + mStreet;
                    }
                }

                if (mBuilding != null && !mBuilding.isEmpty()) {
                    if (mDeliveryAdds.length() > 0) {
                        mDeliveryAdds = mDeliveryAdds + "," + mBuilding;
                    } else {
                        mDeliveryAdds = "" + mBuilding;
                    }
                }

                if (mWay != null && !mWay.isEmpty()) {
                    if (mDeliveryAdds.length() > 0) {
                        mDeliveryAdds = mDeliveryAdds + "," + mWay;
                    } else {
                        mDeliveryAdds = "" + mWay;
                    }
                }

                String mAddsTYPE = mAddressesList.get(position).getAddress_type();
                //  Log.e("268","mAddsTYPe "+mAddsTYPE);
                if (mAddsTYPE.equals("1")) {
                    //Its a house address type :-
                    mAdds = getActivity().getResources().getString(R.string.caa_house);
                } else if (mAddsTYPE.equals("2")) {
                    //Its a apartment address type :-
                    mAdds = getActivity().getResources().getString(R.string.caa_apartment);
                } else {
                    //Its a office address type :-
                    mAdds = getActivity().getResources().getString(R.string.caa_office);
                }

                if (mAddsTYPE.equals("2") || mAddsTYPE.equals("3")) {
                    //To show floor and door no field datas only when address type is apartment or office.
                    //If address type is house then to hide the floor and door no field data.
                    if (mFloor != null && !mFloor.isEmpty()) {
                        if (mDeliveryAdds.length() > 0) {
                            if (!mFloor.equals("0")) {
                                mDeliveryAdds = mDeliveryAdds + "," + mFloor;
                            }
                        } else {
                            if (!mFloor.equals("0")) {
                                mDeliveryAdds = "" + mFloor;
                            }
                        }
                    }
                    if (mDoorNo != null && !mDoorNo.isEmpty()) {
                        if (mDeliveryAdds.length() > 0) {
                            if (!mFloor.equals("0")) {
                                mDeliveryAdds = mDeliveryAdds + "," + mDoorNo;
                            }
                        } else {
                            if (!mFloor.equals("0")) {
                                mDeliveryAdds = "" + mDoorNo;
                            }
                        }
                    }
                } else {
                    mDeliveryAdds = mDeliveryAdds;
                }

                if (mArea != null && !mArea.isEmpty()) {
                    if (mDeliveryAdds.length() > 0) {
                        mDeliveryAdds = mDeliveryAdds + "," + mArea;
                    } else {
                        mDeliveryAdds = "" + mArea;
                    }
                }

                holder.lay_choose_loc_on_map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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

                            if (getDialog() != null) {
                                getDialog().dismiss();
                            }

                        }
                    }
                });
                holder.mAddress.setText(mAdds);
                holder.mAddressSubDetails.setText(mArea);

                holder.mParentRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                            menuDrawerCloser();
                            AreaGeoCodeDataSet mAreaGeoCodeDS = new AreaGeoCodeDataSet();
                            mAreaGeoCodeDS.setmAddress(mAddressesList.get(position).getArea());
                            mAreaGeoCodeDS.setmLatitude(mAddressesList.get(position).getLatitude());
                            mAreaGeoCodeDS.setmLongitude(mAddressesList.get(position).getLongitude());
                            mAreaGeoCodeDS.setmAddsNameOnly("");
                            callHomeModulesApiForDeliveryAvailableCheck(mAreaGeoCodeDS);
                        } else {
                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                            NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                            mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                            mFT.addToBackStack("mNetworkAnalyser");
                            mFT.commit();
                        }
                    }
                });
            }

            @Override
            public int getItemCount() {
                return this.mAddressesList.size();
            }

            public class DataObjectHolder extends RecyclerView.ViewHolder {
                TextView mAddress, mAddressSubDetails;
                LinearLayout mParentRow,lay_choose_loc_on_map;

                public DataObjectHolder(View view) {
                    super(view);
                    mParentRow = view.findViewById(R.id.lay_location_address_list_row);
                    mAddressSubDetails = view.findViewById(R.id.tv_edit_account_address_sub_details);
                    mAddress = view.findViewById(R.id.tv_edit_account_address);
                    lay_choose_loc_on_map = view.findViewById(R.id.lay_choose_loc_on_map);
                }
            }
        }
    }

    public static class HomeParentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final int VIEW_TYPE_LIST_ITEM = 0;
        private final int VIEW_TYPE_LOADING_BAR = 1;
        ArrayList<ListDataSet> mRestaurantList;
        FragmentManager mFragmentManager, support_FManager;
        Activity mActivity;

        //Banner
        private ViewPager mBannerSlider;
        CardView cardView_banner;
        private LinearLayout mBannerSliderButtonContainer;
        private LinearLayout mBannerContainer;
        private int mBannerCurrentPosition, mBannerPosition;
        Interpolator mHomeInterpolator = new AccelerateInterpolator();
        HomeModulesApi mHomeModulesApi;
        private Handler mBannerHandler = new Handler();
        private Runnable mBannerRunnable;
//        onSetUpdate update;

        public HomeParentListAdapter(FragmentManager fragmentManager,
                                     Activity activity, FragmentManager supportFManager, HomeModulesApi homeModulesApi) {
            this.support_FManager = supportFManager;
            this.mFragmentManager = fragmentManager;
            this.mActivity = activity;
            this.mHomeModulesApi = homeModulesApi;
//            update = (onSetUpdate) activity;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_LIST_ITEM) {
                View view = LayoutInflater.from(mActivity).inflate(R.layout.store_list_row, parent, false);
                //Banner
                mBannerSlider = view.findViewById(R.id.view_pager_list_home_banner_slider);
                cardView_banner = view.findViewById(R.id.banner_card);

//                DisplayMetrics displayMetrics = new DisplayMetrics();
//                mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//                int width = displayMetrics.widthPixels;
////            int height = displayMetrics.heightPixels;
//                int height = width / 16 * 9;
//                cardView_banner.setMinimumWidth(width);
//                cardView_banner.setMinimumHeight(height);

                mBannerSliderButtonContainer = view.findViewById(R.id.layout_list_home_banner_slider_buttons);
                mBannerContainer = view.findViewById(R.id.layout_list_home_banner_container);
                if (mHomeModulesApi.bannersList != null && mHomeModulesApi.bannersList.size() > 0) {
                    mBannerContainer.setVisibility(View.VISIBLE);
                    homeBanner(mHomeModulesApi.bannersList);
                } else {
                    mBannerContainer.setVisibility(View.GONE);
                }
                return new ListViewHolder(view);
            } else if (viewType == VIEW_TYPE_LOADING_BAR) {
                View view = LayoutInflater.from(mActivity).inflate(R.layout.store_list_loading_bar, parent, false);
                return new LoadingBarViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

            if (holder.getItemViewType() == VIEW_TYPE_LIST_ITEM) {
                ListViewHolder mListHolder = (ListViewHolder) holder;
                if (AppFunctions.mIsArabic(mActivity)) {
                    AppFunctions.glideActualImgLoader(ContextCompat.getDrawable(mActivity, R.drawable.x_500_280_12), mListHolder.mLoginImgBg, mActivity);
                } else {
                    AppFunctions.glideActualImgLoader(ContextCompat.getDrawable(mActivity, R.drawable.x_500_280_11), mListHolder.mLoginImgBg, mActivity);
                }
                if (UserDetailsDB.getInstance(mActivity).getSizeOfList() > 0) {
                    mListHolder.mLayLoginParent.setVisibility(View.GONE);
                } else {
                    mListHolder.mLayLoginParent.setVisibility(View.VISIBLE);
                }
                mListHolder.login_title.setText(mHomeModulesApi.content.loginTitle);
                mListHolder.login_description.setText(mHomeModulesApi.content.loginDescription);

                mListHolder.mLayLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (UserDetailsDB.getInstance(mActivity).getSizeOfList() == 0) {
                            Intent intent = new Intent(mActivity, AppLogin.class);
                            mActivity.startActivity(intent);
                        }
                    }
                });

                String mTime = AppFunctions.getCurrentTime(mActivity);
                String[] mTime_split = mTime.split(":");
                if (Integer.valueOf(mTime_split[0]) <= 11) {
                    mListHolder.mSessionPostFixWord.setText(mActivity.getResources().getString(R.string.good_morning));
                } else if (Integer.valueOf(mTime_split[0]) <= 16) {
                    mListHolder.mSessionPostFixWord.setText(mActivity.getResources().getString(R.string.good_afternoon));
                } else {
                    mListHolder.mSessionPostFixWord.setText(mActivity.getResources().getString(R.string.good_evening));
                }

                if (mHomeModulesApi.businessTypesList != null && mHomeModulesApi.businessTypesList.size() > 0) {
                    mListHolder.mRecyclerBusinessTypeList.setVisibility(View.VISIBLE);
                    mListHolder.mHomeBusinessTypeListAdapter = new BusinessTypeListAdapter(mHomeModulesApi.businessTypesList);
                    mListHolder.mRecyclerBusinessTypeList.setAdapter(mListHolder.mHomeBusinessTypeListAdapter);
                } else {
                    mListHolder.mRecyclerBusinessTypeList.setVisibility(View.GONE);
                }

                DisplayMetrics displayMetrics = new DisplayMetrics();
                mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int width = displayMetrics.widthPixels;
                int height = width / 5;
                mListHolder.card_view_pickup.setRadius(20);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.height = height;
                layoutParams.width = width - 30;
                layoutParams.setMargins(10, 8, 10, 8);
                mListHolder.card_view_pickup.setLayoutParams(layoutParams);

                mListHolder.pickup_content.setText(mHomeModulesApi.pick_up_description);
                mListHolder.delivery_content.setText(mHomeModulesApi.delivery_description);

                if (OrderTypeDB.getInstance(mActivity).getUserServiceType().equals("2")) {
                    mListHolder.pickup_tv.setBackground(mActivity.getResources().getDrawable(R.drawable.bg_orange_border));
                    mListHolder.delivery_tv.setBackground(mActivity.getResources().getDrawable(R.drawable.bg_grey_border));
                } else {
                    mListHolder.delivery_tv.setBackground(mActivity.getResources().getDrawable(R.drawable.bg_orange_border));
                    mListHolder.pickup_tv.setBackground(mActivity.getResources().getDrawable(R.drawable.bg_grey_border));
                }

                mListHolder.card_view_pickup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!OrderTypeDB.getInstance(mActivity).getUserServiceType().isEmpty()) {
                            OrderTypeDB.getInstance(mActivity).updateUserServiceType("2");
                        } else {
                            OrderTypeDB.getInstance(mActivity).addUserServiceType("2");
                        }
                        FragmentTransaction mFT = mFragmentManager.beginTransaction();
                        AllRestaurants m_AllRestaurants = new AllRestaurants();
                        Bundle mBundle = new Bundle();
                        mBundle.putString("top_pick_id", "0");
                        mBundle.putString("top_pick_id", "0");
                        m_AllRestaurants.setArguments(mBundle);
                        mFT.replace(R.id.layout_app_home_body, m_AllRestaurants, "m_AllRestaurants");
                        mFT.addToBackStack("m_AllRestaurants");
                        mFT.commit();
                    }
                });

//                AppFunctions.bannerLoaderUsingGlide("", mListHolder.order_type_pickup, mActivity);

                if (mHomeModulesApi.topPicksList != null && mHomeModulesApi.topPicksList.size() > 0) {
                    mListHolder.mRecyclerTopPicksList.setVisibility(View.VISIBLE);
                    mListHolder.mTopPicksListAdapter = new TopPicksListAdapter(mHomeModulesApi.topPicksList);
                    mListHolder.mRecyclerTopPicksList.setAdapter(mListHolder.mTopPicksListAdapter);
                } else {
                    mListHolder.mRecyclerTopPicksList.setVisibility(View.GONE);
                }

                if (mHomeModulesApi.bestOffersList != null && mHomeModulesApi.bestOffersList.size() > 0) {

                    mListHolder.mBestOffersTv.setVisibility(View.VISIBLE);
                    mListHolder.mRecyclerBestOffersList.setVisibility(View.VISIBLE);

                    mListHolder.mBestOffersTv.setText(mActivity.getResources().getString(R.string.best_offer));
                    mListHolder.mRecyclerBestOffersList.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
                    mListHolder.mRecyclerBestOffersList.setAdapter(new BestOfferListAdapter(mHomeModulesApi.bestOffersList, mActivity));
                } else {
                    mListHolder.mBestOffersTv.setVisibility(View.GONE);
                    mListHolder.mRecyclerBestOffersList.setVisibility(View.GONE);
                }

                if (mHomeModulesApi.brandsList != null && mHomeModulesApi.brandsList.size() > 0) {
                    mListHolder.mNotoriousBrandTv.setVisibility(View.VISIBLE);
                    mListHolder.mRecyclerNotoriousBrandsList.setVisibility(View.VISIBLE);
                    mListHolder.mNotoriousBrandTv.setText(mHomeModulesApi.content.brand);
                    mListHolder.mRecyclerNotoriousBrandsList.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
                    mListHolder.mRecyclerNotoriousBrandsList.setAdapter(new NotoriousBrandsListAdapter(mHomeModulesApi.brandsList, mActivity));
                } else {
                    mListHolder.mNotoriousBrandTv.setVisibility(View.GONE);
                    mListHolder.mRecyclerNotoriousBrandsList.setVisibility(View.GONE);
                }

                if (mHomeModulesApi.drinksList != null && mHomeModulesApi.drinksList.size() > 0) {
                    mListHolder.mLikeToDrinkTv.setVisibility(View.VISIBLE);
                    mListHolder.mRecyclerLikeToDrinkList.setVisibility(View.VISIBLE);
                    mListHolder.mLikeToDrinkTv.setText(mHomeModulesApi.content.drinks);
                    mListHolder.mRecyclerLikeToDrinkList.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
                    mListHolder.mRecyclerLikeToDrinkList.setAdapter(new LikeToDrinkListAdapter(mHomeModulesApi.drinksList, mActivity));
                } else {
                    mListHolder.mLikeToDrinkTv.setVisibility(View.GONE);
                    mListHolder.mRecyclerLikeToDrinkList.setVisibility(View.GONE);
                }

                mListHolder.delivery_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mListHolder.delivery_tv.setBackground(mActivity.getResources().getDrawable(R.drawable.bg_orange_border));
                        mListHolder.pickup_tv.setBackground(mActivity.getResources().getDrawable(R.drawable.bg_grey_border));

                        if (!OrderTypeDB.getInstance(mActivity).getUserServiceType().isEmpty()) {
                            OrderTypeDB.getInstance(mActivity).updateUserServiceType("1");
                        } else {
                            OrderTypeDB.getInstance(mActivity).addUserServiceType("1");
                        }
                        if (mActivity != null) {
                            update.reload();
//                            mActivity.recreate(); // for refresh the page
//                            FragmentManager mFragmentMgr = support_FManager;
//                            for (int i = 0; i < mFragmentMgr.getBackStackEntryCount(); i++) {
//                                if (mFragmentMgr.getBackStackEntryAt(i).getName().equals("m_listHome")) {
//                                    mFragmentMgr.popBackStack();
//                                    break;
//                                }
//                            }
                        }
                    }
                });

                mListHolder.pickup_tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListHolder.pickup_tv.setBackground(mActivity.getResources().getDrawable(R.drawable.bg_orange_border));
                        mListHolder.delivery_tv.setBackground(mActivity.getResources().getDrawable(R.drawable.bg_grey_border));

                        if (!OrderTypeDB.getInstance(mActivity).getUserServiceType().isEmpty()) {
                            OrderTypeDB.getInstance(mActivity).updateUserServiceType("2");
                        } else {
                            OrderTypeDB.getInstance(mActivity).addUserServiceType("2");
                        }

                        update.reload();

//                        if (mActivity != null) {
//                            mActivity.recreate(); // for refresh the page
//                            FragmentManager mFragmentMgr = support_FManager;
//                            for (int i = 0; i < mFragmentMgr.getBackStackEntryCount(); i++) {
//                                if (mFragmentMgr.getBackStackEntryAt(i).getName().equals("m_listHome")) {
//                                    mFragmentMgr.popBackStack();
//                                    break;
//                                }
//                            }
//                        }

//                        callHomeModulesList();
                    }
                });

                mListHolder.mSearchContentLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //AppFunctions.toastLong(mActivity, "Search Clicked.");
                        FragmentTransaction mFT = mFragmentManager.beginTransaction();
                        SearchRestaurants m_searchRestaurants = new SearchRestaurants();
                        mFT.replace(R.id.layout_app_home_body, m_searchRestaurants, "m_searchRestaurants");
                        mFT.addToBackStack("m_searchRestaurants");
                        mFT.commit();
                    }
                });

            } else if (holder.getItemViewType() == VIEW_TYPE_LOADING_BAR) {
                LoadingBarViewHolder mLoadingBarHolder = (LoadingBarViewHolder) holder;
                mLoadingBarHolder.mListLoadingBar.setIndeterminate(true);
            }
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }


        @Override
        public int getItemViewType(int position) {
            if (mRestaurantList != null) {
                if (mRestaurantList.get(position) != null) {
                    // //Log.e("getItemViewType() mType",String.valueOf(mType));
                    return VIEW_TYPE_LIST_ITEM;
                } else {
                    return VIEW_TYPE_LOADING_BAR;
                }
            } else {
                return VIEW_TYPE_LIST_ITEM;
            }
        }

        @Override
        public int getItemCount() {
            return 1;
        }

        public class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, AdapterView.OnItemClickListener {

            private final RecyclerView mRecyclerNotoriousBrandsList;
            private final RecyclerView mRecyclerLikeToDrinkList;
            private final RecyclerView mRecyclerBestOffersList;
            private final RecyclerView mRecyclerBusinessTypeList;

            private final RecyclerView mRecyclerTopPicksList;
            final RecyclerView.LayoutManager mBestOffersListLayoutMgr,
                    mLikeToDrinkListLayoutMgr, mNotoriousBrandsListLayoutMgr;
            final RecyclerView.LayoutManager mBusinessTypeListLayoutMgr;

            private BusinessTypeListAdapter mHomeBusinessTypeListAdapter;
            private TopPicksListAdapter mTopPicksListAdapter;
            FrameLayout mLayLoginParent;
            TextView mSessionPostFixWord, mBestOffersTv, mLikeToDrinkTv, mNotoriousBrandTv, delivery_content, pickup_content,login_title,login_description;
            LinearLayout mSearchContentLay, delivery_tv, pickup_tv, mLayLogin;
            ImageView mLoginImgBg, order_type_pickup;
            CardView card_view_pickup;

            ListViewHolder(View itemView) {
                super(itemView);
                mLoginImgBg = itemView.findViewById(R.id.img_login_image_bg);
                order_type_pickup = itemView.findViewById(R.id.order_type_pickup);
                card_view_pickup = itemView.findViewById(R.id.card_view_pickup);

                mLayLogin = itemView.findViewById(R.id.lay_rl_login_btn_container);
                mLayLoginParent = itemView.findViewById(R.id.lay_login_parent);
                login_title = itemView.findViewById(R.id.login_title);
                login_description = itemView.findViewById(R.id.login_description);

                mNotoriousBrandTv = itemView.findViewById(R.id.tv_notorious_brands_title);
                mSearchContentLay = itemView.findViewById(R.id.lay_search_content_home);

                mSessionPostFixWord = itemView.findViewById(R.id.tv_day_session_text_postfix);
                mBestOffersTv = itemView.findViewById(R.id.tv_best_offers_list_title);
                mLikeToDrinkTv = itemView.findViewById(R.id.tv_like_to_drink_title);
                delivery_tv = itemView.findViewById(R.id.delivery_tv);
                pickup_tv = itemView.findViewById(R.id.pickup_tv);
                delivery_content = itemView.findViewById(R.id.delivery_content);
                pickup_content = itemView.findViewById(R.id.pickup_content);

                mRecyclerBestOffersList = itemView.findViewById(R.id.recycler_best_offers_list);
                mBestOffersListLayoutMgr = new LinearLayoutManager(mActivity);
                mRecyclerBestOffersList.setLayoutManager(mBestOffersListLayoutMgr);

                mRecyclerNotoriousBrandsList = itemView.findViewById(R.id.recycler_notorious_brands);
                mNotoriousBrandsListLayoutMgr = new LinearLayoutManager(mActivity);
                mRecyclerNotoriousBrandsList.setLayoutManager(mNotoriousBrandsListLayoutMgr);

                mRecyclerLikeToDrinkList = itemView.findViewById(R.id.recycler_like_to_drink_list);
                mLikeToDrinkListLayoutMgr = new LinearLayoutManager(mActivity);
                mRecyclerLikeToDrinkList.setLayoutManager(mLikeToDrinkListLayoutMgr);

                mRecyclerBusinessTypeList = itemView.findViewById(R.id.recycler_home_business_type_list);
                mBusinessTypeListLayoutMgr = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
                mRecyclerBusinessTypeList.setLayoutManager(mBusinessTypeListLayoutMgr);

                mRecyclerTopPicksList = itemView.findViewById(R.id.recycler_top_picks_list);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(mActivity, 1, LinearLayoutManager.HORIZONTAL, false);
                mRecyclerTopPicksList.setLayoutManager(gridLayoutManager);

            }

            @Override
            public void onClick(View v) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }

        }

        private class BestOfferListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private final ArrayList<BestOffersDataSet> mBestOffersList;
            private final Context mStoresContext;

            BestOfferListAdapter(ArrayList<BestOffersDataSet> bestOffersList, Context context) {
                this.mBestOffersList = bestOffersList;
                this.mStoresContext = context;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RestaurantViewHolder(LayoutInflater.from(mStoresContext).inflate(R.layout.rc_row_best_offers, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                RestaurantViewHolder restaurantViewHolder = (RestaurantViewHolder) holder;

                restaurantViewHolder.tv_RestaurantTitle.setText(mBestOffersList.get(position).getVendor_name());

                String mDTime = mActivity.getResources().getString(R.string.within) + " " + mBestOffersList.get(position).getDelivery_time()
                        + " " + mActivity.getResources().getString(R.string.mins);
                restaurantViewHolder.tv_DeliveryWithin.setText(mDTime);

                restaurantViewHolder.tv_restaurant_sub_content.setText(mBestOffersList.get(position).getCuisines());

                if (mBestOffersList.get(position).getRatingDataSet() != null) {
                    String mCRating = mBestOffersList.get(position).getRatingDataSet().vendor_rating_name;
                    if (mCRating != null && !mCRating.isEmpty()) {
                        restaurantViewHolder.tv_rating_statement.setText(mCRating);
                        restaurantViewHolder.tv_rating_statement.setVisibility(View.VISIBLE);
                        restaurantViewHolder.mLayRatingSplit.setVisibility(View.VISIBLE);
                        restaurantViewHolder.mImgRatingStatement.setVisibility(View.VISIBLE);
                        AppFunctions.imageLoaderUsingGlide(mBestOffersList.get(position).getRatingDataSet().vendor_rating_image, restaurantViewHolder.mImgRatingStatement, mActivity);
                    } else {
                        restaurantViewHolder.tv_rating_statement.setText("0");
                        restaurantViewHolder.tv_rating_statement.setVisibility(View.GONE);
                        restaurantViewHolder.mLayRatingSplit.setVisibility(View.GONE);
                        restaurantViewHolder.mImgRatingStatement.setVisibility(View.GONE);
                    }
                } else {
                    restaurantViewHolder.tv_rating_statement.setText("0");
                    restaurantViewHolder.tv_rating_statement.setVisibility(View.GONE);
                    restaurantViewHolder.mLayRatingSplit.setVisibility(View.GONE);
                    restaurantViewHolder.mImgRatingStatement.setVisibility(View.GONE);
                }


                restaurantViewHolder.tv_delivery_amount.setText(mBestOffersList.get(position).getDelivery_fee());

                String mVendorStatus = mBestOffersList.get(position).getVendor_status();
                if (mStoresContext != null) {
                    if (mVendorStatus.equals("0")) {
                        //closed :-
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.VISIBLE);
                        restaurantViewHolder.tv_ImageOverStatus.setText(mStoresContext.getResources().getString(R.string.closed));
                        //restaurantViewHolder.tv_restaurant_working_status.setText(mStoresContext.getResources().getString(R.string.closed));
                    } else if (mVendorStatus.equals("2")) {
                        //Busy :-
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.VISIBLE);
                        restaurantViewHolder.tv_ImageOverStatus.setText(mStoresContext.getResources().getString(R.string.busy));
                        //restaurantViewHolder.tv_restaurant_working_status.setText(mStoresContext.getResources().getString(R.string.busy));
                    } else {
                        //Open :-
                        //restaurantViewHolder.tv_restaurant_working_status.setText("");
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.GONE);
                        restaurantViewHolder.tv_ImageOverStatus.setText("");
                    }
                }


                String mOfferData = mBestOffersList.get(position).getOffer();
                if (mOfferData != null && !mOfferData.isEmpty()) {
                    restaurantViewHolder.mLayOfferParent.setVisibility(View.VISIBLE);
                    restaurantViewHolder.tv_offerContent.setText(mOfferData);
                } else {
                    restaurantViewHolder.mLayOfferParent.setVisibility(View.INVISIBLE);
                    restaurantViewHolder.tv_offerContent.setText("");
                }

                AppFunctions.imageLoaderUsingGlide(mBestOffersList.get(position).getBanner(), restaurantViewHolder.iv_BestOfferImage, mActivity);
                //Glide.with(MmActivity).load(R.drawable.x_best_offer_sample_1).into(restaurantViewHolder.iv_BestOfferImage);


            }

            @Override
            public int getItemCount() {
                /*if (mBestOffersList.size() > 4) {
                    return 4;
                } else {
                    return mBestOffersList.size();
                }*/
                return mBestOffersList.size();

            }

            class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                ImageView iv_BestOfferImage;
                TextView tv_RestaurantTitle, tv_DeliveryWithin, tv_restaurant_sub_content;
                // View tv_restaurant_close_background;
                TextView /*tv_restaurant_working_status,*/ tv_rating_statement,
                        tv_delivery_amount, tv_offerContent, tv_ImageOverStatus;
                LinearLayout best_offer_linear, mLayOfferParent, mLayImageOverStatus;
                LinearLayout mLayRatingSplit;
                ImageView mImgRatingStatement;

                RestaurantViewHolder(View itemView) {
                    super(itemView);

                    mLayImageOverStatus = itemView.findViewById(R.id.lay_best_offers_vendor_status_over);

                    mLayRatingSplit = itemView.findViewById(R.id.lay_rating_statement_split);
                    mImgRatingStatement = itemView.findViewById(R.id.img_rating_statement);

                    tv_ImageOverStatus = itemView.findViewById(R.id.tv_best_offers_image_over_status);

                    tv_offerContent = itemView.findViewById(R.id.tv_offer_content);
                    tv_delivery_amount = itemView.findViewById(R.id.tv_delivery_charge);
                    tv_rating_statement = itemView.findViewById(R.id.tv_rating_statement);
                    iv_BestOfferImage = itemView.findViewById(R.id.iv_home_restaurant_image);
                    tv_RestaurantTitle = itemView.findViewById(R.id.tv_home_restaurant_title);
                    tv_restaurant_sub_content = itemView.findViewById(R.id.tv_restaurant_sub_content);
                    tv_DeliveryWithin = itemView.findViewById(R.id.tv_home_delivery_time);
                    // tv_restaurant_close_background = itemView.findViewById(R.id.tv_restaurant_close_background);
                    // tv_restaurant_working_status = itemView.findViewById(R.id.tv_restaurant_working_status);
                    best_offer_linear = itemView.findViewById(R.id.best_offer_linear);

                    mLayOfferParent = itemView.findViewById(R.id.lay_offer_content_container);

                    best_offer_linear.setOnClickListener(this);
                    iv_BestOfferImage.setOnClickListener(this);
                    tv_RestaurantTitle.setOnClickListener(this);
                    tv_restaurant_sub_content.setOnClickListener(this);
                    tv_DeliveryWithin.setOnClickListener(this);

                }

                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() != -1) {
                        String mTYPE_Id = mBestOffersList.get(getAdapterPosition()).getVendor_type_id();
                        if (mBestOffersList.get(getAdapterPosition()).getVendor_status().equals("0")) {
                            toDialogClosedVendor(mBestOffersList.get(getAdapterPosition()).getVendor_name(), mBestOffersList.get(getAdapterPosition()).getVendor_id(),
                                    "0");
                        } else if (mBestOffersList.get(getAdapterPosition()).getVendor_status().equals("2")) {
                            toDialogBusyVendor(mBestOffersList.get(getAdapterPosition()).getVendor_name(), mBestOffersList.get(getAdapterPosition()).getVendor_id(),
                                    "0");
                        } else {
                            if (mTYPE_Id.equals("2")) {
                                toGroceryList(mBestOffersList.get(getAdapterPosition()).getVendor_id(), mBestOffersList.get(getAdapterPosition()).getVendor_name());
                            } else {
                                toStoreListing(mBestOffersList.get(getAdapterPosition()).getVendor_id(), "0");
                            }
                        }
                    }
                }
            }
        }

        private class NotoriousBrandsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private final ArrayList<BrandsDataSet> mBrandsList;
            private final Context mStoresContext;

            NotoriousBrandsListAdapter(ArrayList<BrandsDataSet> brandsList, Context context) {
                this.mBrandsList = brandsList;
                this.mStoresContext = context;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RestaurantViewHolder(LayoutInflater.from(mStoresContext).inflate(R.layout.rc_row_notorious_brands, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                RestaurantViewHolder restaurantViewHolder = (RestaurantViewHolder) holder;

                restaurantViewHolder.tv_RestaurantTitle.setText(mBrandsList.get(position).getVendor_name());
                // Glide.with(mActivity).load(R.drawable.x_best_offer_sample_1).into(restaurantViewHolder.iv_Image);
                AppFunctions.imageLoaderUsingGlide(mBrandsList.get(position).getLogo(), restaurantViewHolder.iv_Image, mActivity);

                String mDTime = mBrandsList.get(position).getDelivery_time()
                        + " " + mActivity.getResources().getString(R.string.mins);
                restaurantViewHolder.tv_DeliveryTime.setText(mDTime);

                String mVendorStatus = mBrandsList.get(position).getVendor_status();
                if (mStoresContext != null) {
                    if (mVendorStatus.equals("0")) {
                        //closed :-
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.VISIBLE);
                        restaurantViewHolder.tv_ImageOverStatus.setText(mStoresContext.getResources().getString(R.string.closed));
                    } else if (mVendorStatus.equals("2")) {
                        //Busy :-
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.VISIBLE);
                        restaurantViewHolder.tv_ImageOverStatus.setText(mStoresContext.getResources().getString(R.string.busy));
                    } else {
                        //Open :-
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.GONE);
                        restaurantViewHolder.tv_ImageOverStatus.setText("");
                    }

                }


            }

            @Override
            public int getItemCount() {

               /* if (mBrandsList.size() > 4) {
                    return 4;
                } else {
                    return mBrandsList.size();
                }*/

                return mBrandsList.size();

            }

            class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                ImageView iv_Image;
                TextView tv_RestaurantTitle, tv_DeliveryTime, tv_ImageOverStatus;
                LinearLayout mLayImageOverStatus;


                RestaurantViewHolder(View itemView) {
                    super(itemView);

                    mLayImageOverStatus = itemView.findViewById(R.id.lay_nb_vendor_status_over);
                    tv_ImageOverStatus = itemView.findViewById(R.id.tv_nb_image_over_status);

                    iv_Image = itemView.findViewById(R.id.iv_home_restaurant_image);
                    tv_RestaurantTitle = itemView.findViewById(R.id.tv_home_restaurant_title);
                    tv_DeliveryTime = itemView.findViewById(R.id.tv_delivery_time);

                    iv_Image.setOnClickListener(this);
                    tv_RestaurantTitle.setOnClickListener(this);
                    tv_DeliveryTime.setOnClickListener(this);


                }

                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() != -1) {
                        String mTYPE_Id = mBrandsList.get(getAdapterPosition()).getVendor_type_id();
                        if (mBrandsList.get(getAdapterPosition()).getVendor_status().equals("0")) {
                            toDialogClosedVendor(mBrandsList.get(getAdapterPosition()).getVendor_name(), mBrandsList.get(getAdapterPosition()).getVendor_id(),
                                    "0");
                        } else if (mBrandsList.get(getAdapterPosition()).getVendor_status().equals("2")) {
                            toDialogBusyVendor(mBrandsList.get(getAdapterPosition()).getVendor_name(), mBrandsList.get(getAdapterPosition()).getVendor_id(),
                                    "0");
                        } else {
                            if (mTYPE_Id.equals("2")) {
                                toGroceryList(mBrandsList.get(getAdapterPosition()).getVendor_id(), mBrandsList.get(getAdapterPosition()).getVendor_name());
                            } else {
                                toStoreListing(mBrandsList.get(getAdapterPosition()).getVendor_id(), "0");
                            }
                        }
                    }
                }
            }
        }

        private class LikeToDrinkListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private final ArrayList<DrinksDataSet> mDrinksList;
            private final Context mStoresContext;

            LikeToDrinkListAdapter(ArrayList<DrinksDataSet> drinksList, Context context) {
                this.mDrinksList = drinksList;
                this.mStoresContext = context;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new RestaurantViewHolder(LayoutInflater.from(mStoresContext).inflate(R.layout.rc_row_like_to_drink, parent, false));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                RestaurantViewHolder restaurantViewHolder = (RestaurantViewHolder) holder;

                restaurantViewHolder.tv_RestaurantTitle.setText(mDrinksList.get(position).getVendor_name());

                String mDTime = mActivity.getResources().getString(R.string.within) + " " + mDrinksList.get(position).getDelivery_time()
                        + " " + mActivity.getResources().getString(R.string.mins);
                restaurantViewHolder.tv_DeliveryWithin.setText(mDTime);

                restaurantViewHolder.tv_restaurant_sub_content.setText(mDrinksList.get(position).getCuisines());
                //            restaurantViewHolder.tv_rating_value.setText(mDrinksList.get(position).getRating().get(position).getRating());


                if (mDrinksList.get(position).getRating() != null) {
                    String mCRating = mDrinksList.get(position).getRating().vendor_rating_name;
                    if (mCRating != null && !mCRating.isEmpty()) {
                        restaurantViewHolder.tv_rating_statement.setText(mCRating);
                        restaurantViewHolder.tv_rating_statement.setVisibility(View.VISIBLE);
                        restaurantViewHolder.mLayRatingSplit.setVisibility(View.VISIBLE);
                        restaurantViewHolder.mImgRatingStatement.setVisibility(View.VISIBLE);
                        AppFunctions.imageLoaderUsingGlide(mDrinksList.get(position).getRating().vendor_rating_image, restaurantViewHolder.mImgRatingStatement, mActivity);
                    } else {
                        restaurantViewHolder.tv_rating_statement.setText("0");
                        restaurantViewHolder.tv_rating_statement.setVisibility(View.GONE);
                        restaurantViewHolder.mLayRatingSplit.setVisibility(View.GONE);
                        restaurantViewHolder.mImgRatingStatement.setVisibility(View.GONE);
                    }
                } else {
                    restaurantViewHolder.tv_rating_statement.setText("0");
                    restaurantViewHolder.tv_rating_statement.setVisibility(View.GONE);
                    restaurantViewHolder.mLayRatingSplit.setVisibility(View.GONE);
                    restaurantViewHolder.mImgRatingStatement.setVisibility(View.GONE);
                }

                restaurantViewHolder.tv_delivery_amount.setText(mDrinksList.get(position).getDelivery_fee());

                //mLayOfferParent

                String mOfferData = mDrinksList.get(position).getOffer();
                if (mOfferData != null && !mOfferData.isEmpty()) {
                    restaurantViewHolder.mLayOfferParent.setVisibility(View.VISIBLE);
                    restaurantViewHolder.tv_offerContent.setText(mOfferData);
                } else {
                    restaurantViewHolder.mLayOfferParent.setVisibility(View.INVISIBLE);
                    restaurantViewHolder.tv_offerContent.setText("");
                }


                AppFunctions.imageLoaderUsingGlide(mDrinksList.get(position).getBanner(), restaurantViewHolder.iv_BestOfferImage, mActivity);
                // Glide.with(mActivity).load(R.drawable.x_best_offer_sample_1).into(restaurantViewHolder.iv_BestOfferImage);

                String mVendorStatus = mDrinksList.get(position).getVendor_status();
                if (mStoresContext != null) {
                    if (mVendorStatus.equals("0")) {
                        //closed :-
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.VISIBLE);
                        restaurantViewHolder.tv_ImageOverStatus.setText(mStoresContext.getResources().getString(R.string.closed));
                    } else if (mVendorStatus.equals("2")) {
                        //Busy :-
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.VISIBLE);
                        restaurantViewHolder.tv_ImageOverStatus.setText(mStoresContext.getResources().getString(R.string.busy));
                    } else {
                        //Open :-
                        restaurantViewHolder.mLayImageOverStatus.setVisibility(View.GONE);
                        restaurantViewHolder.tv_ImageOverStatus.setText("");
                    }

                }


            }

            @Override
            public int getItemCount() {

               /* if (mDrinksList.size() > 4) {
                    return 4;
                } else {
                    return mDrinksList.size();
                }*/

                return mDrinksList.size();

            }

            class RestaurantViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                ImageView iv_BestOfferImage;
                TextView tv_RestaurantTitle, tv_DeliveryWithin, tv_restaurant_sub_content;
                TextView tv_rating_statement,
                        tv_delivery_amount, tv_offerContent, tv_ImageOverStatus;


                LinearLayout mLayOfferParent, mLayImageOverStatus;

                LinearLayout mLayRatingSplit;
                ImageView mImgRatingStatement;


                RestaurantViewHolder(View itemView) {
                    super(itemView);

                    mLayRatingSplit = itemView.findViewById(R.id.lay_rating_statement_split_drink);
                    mImgRatingStatement = itemView.findViewById(R.id.img_rating_statement_drink);

                    mLayImageOverStatus = itemView.findViewById(R.id.lay_drink_vendor_status_over);
                    tv_ImageOverStatus = itemView.findViewById(R.id.tv_drink_image_over_status);

                    tv_offerContent = itemView.findViewById(R.id.tv_offer_content);
                    tv_delivery_amount = itemView.findViewById(R.id.tv_delivery_charge);
                    tv_rating_statement = itemView.findViewById(R.id.tv_rating_statement);
                    iv_BestOfferImage = itemView.findViewById(R.id.iv_home_restaurant_image);
                    tv_RestaurantTitle = itemView.findViewById(R.id.tv_home_restaurant_title);
                    tv_restaurant_sub_content = itemView.findViewById(R.id.tv_restaurant_sub_content);
                    tv_DeliveryWithin = itemView.findViewById(R.id.tv_home_delivery_time);
                    mLayOfferParent = itemView.findViewById(R.id.lay_offer_content_container);
                    iv_BestOfferImage.setOnClickListener(this);
                    tv_RestaurantTitle.setOnClickListener(this);
                    tv_restaurant_sub_content.setOnClickListener(this);
                    tv_DeliveryWithin.setOnClickListener(this);

                }

                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() != -1) {
                        String mTYPE_Id = mDrinksList.get(getAdapterPosition()).getVendor_type_id();
                        if (mDrinksList.get(getAdapterPosition()).getVendor_status().equals("0")) {
                            toDialogClosedVendor(mDrinksList.get(getAdapterPosition()).getVendor_name(), mDrinksList.get(getAdapterPosition()).getVendor_id(),
                                    "0");
                        } else if (mDrinksList.get(getAdapterPosition()).getVendor_status().equals("2")) {
                            toDialogBusyVendor(mDrinksList.get(getAdapterPosition()).getVendor_name(), mDrinksList.get(getAdapterPosition()).getVendor_id(),
                                    "0");
                        } else {
                            if (mTYPE_Id.equals("2")) {
                                toGroceryList(mDrinksList.get(getAdapterPosition()).getVendor_id(), mDrinksList.get(getAdapterPosition()).getVendor_name());
                            } else {
                                toStoreListing(mDrinksList.get(getAdapterPosition()).getVendor_id(), "0");
                            }
                        }
                    }
                }
            }
        }

        public class LoadingBarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private final ProgressBar mListLoadingBar;

            public LoadingBarViewHolder(View itemView) {
                super(itemView);
                mListLoadingBar = itemView.findViewById(R.id.progressBar_restaurant_list);
            }

            @Override
            public void onClick(View v) {

            }
        }

        public void toGroceryList(String vendor_id, String vendor_name) {
            FragmentTransaction mFT = support_FManager.beginTransaction();
            GroceryCategoryMainPage m_groceryCategoryMainPage = new GroceryCategoryMainPage();
            Bundle mBundle = new Bundle();
            mBundle.putString(DefaultNames.store_id, vendor_id);
            mBundle.putString(DefaultNames.store_name, vendor_name);
            m_groceryCategoryMainPage.setArguments(mBundle);
            mFT.replace(R.id.layout_app_home_body, m_groceryCategoryMainPage, "m_groceryCategoryMainPage");
            mFT.addToBackStack("m_groceryCategoryMainPage");
            mFT.commit();
        }

        public void toStoreListing(String vendor_id, String product_id) {
            if (mActivity != null) {
                FragmentTransaction mFT = support_FManager.beginTransaction();
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
            String msg = mActivity.getResources().getString(R.string.restaurant_closed_msg_1) + " " + name + " " + mActivity.getResources().getString(R.string.restaurant_closed_msg_2);
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(mActivity.getResources().getString(R.string.restaurant_closed));
            builder.setMessage(msg);
            builder.setPositiveButton(mActivity.getResources().getString(R.string.co_s_continue), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    toStoreListing(vendor_id, product_id);
                }
            });
            builder.setNegativeButton(mActivity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }

        public void toDialogBusyVendor(String name, String vendor_id, String product_id) {
            String msg = mActivity.getResources().getString(R.string.restaurant_closed_msg_1) + " " + name + " " + mActivity.getResources().getString(R.string.restaurant_busy_msg_2);
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setTitle(mActivity.getResources().getString(R.string.restaurant_busy));
            builder.setMessage(msg);
            builder.setPositiveButton(mActivity.getResources().getString(R.string.co_s_continue), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    toStoreListing(vendor_id, product_id);
                }
            });
            builder.setNegativeButton(mActivity.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }

        private class BusinessTypeListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private final ArrayList<BusinessTypesDataSet> businessTypeList;

            BusinessTypeListAdapter(ArrayList<BusinessTypesDataSet> businessTypeList) {
                this.businessTypeList = businessTypeList;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new CuisineViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.rc_row_business_types, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                CuisineViewHolder restaurantViewHolder = (CuisineViewHolder) viewHolder;
                restaurantViewHolder.tv_RestaurantTitle.setText(businessTypeList.get(i).getName());
                AppFunctions.imageLoaderUsingGlide(businessTypeList.get(i).getLogo(), restaurantViewHolder.iv_RestaurantImage, mActivity);
                // Glide.with(mActivity).load(R.drawable.x_top_list_sample_1).into(restaurantViewHolder.iv_RestaurantImage);
            }

            @Override
            public int getItemCount() {
                return businessTypeList.size();
            }

            class CuisineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

                ImageView iv_RestaurantImage;
                TextView tv_RestaurantTitle, tv_RestaurantStatus;

                CuisineViewHolder(View itemView) {
                    super(itemView);
                    iv_RestaurantImage = itemView.findViewById(R.id.iv_home_restaurant_image);
                    tv_RestaurantTitle = itemView.findViewById(R.id.tv_home_restaurant_title);
                    //                tv_RestaurantStatus = itemView.findViewById(R.id.tv_home_restaurant_status);
                    iv_RestaurantImage.setOnClickListener(this);
                    tv_RestaurantTitle.setOnClickListener(this);
                    //                tv_RestaurantStatus.setOnClickListener(this);
                }

                @Override
                public void onClick(View v) {
                    int id = v.getId();
                    if (getAdapterPosition() != -1) {

                        String mTYPE_Id = businessTypeList.get(getAdapterPosition()).getType_id();

                        if (mTYPE_Id.equals("2")) {
                            //Type id 4 for grocery :-
                            // AppFunctions.toastShort(mActivity,businessTypeList.get(getAdapterPosition()).getName()+" - "
                            // +businessTypeList.get(getAdapterPosition()).getType_id());
                            FragmentTransaction mFT = mFragmentManager.beginTransaction();
                            GroceryAllStores m_groceryAllStores = new GroceryAllStores();
                            mFT.replace(R.id.layout_app_home_body, m_groceryAllStores, "m_groceryAllStores");
                            mFT.addToBackStack("m_groceryAllStores");
                            mFT.commit();
                        } else {
                            toPerformAllShops(getAdapterPosition());
                        }


                    }
                }

                public void toPerformAllShops(int position) {

                    if (ARCuisinesDB.getInstance(mActivity).getSizeOfList() > 0) {
                        ARCuisinesDB.getInstance(mActivity).deleteCuisinesDB();
                    }
                    if (ARFiltersDB.getInstance(mActivity).getSizeOfList() > 0) {
                        ARFiltersDB.getInstance(mActivity).deleteDB();
                    }

                    FragmentTransaction mFT = mFragmentManager.beginTransaction();
                    AllRestaurants m_AllRestaurants = new AllRestaurants();
                    Bundle mBundle = new Bundle();
                    mBundle.putString("business_type_id", businessTypeList.get(position).getType_id());
                    mBundle.putString("top_pick_id", "0");
                    m_AllRestaurants.setArguments(mBundle);
                    mFT.replace(R.id.layout_app_home_body, m_AllRestaurants, "m_AllRestaurants");
                    mFT.addToBackStack("m_AllRestaurants");
                    mFT.commit();

                }

            }
        }

        private class TopPicksListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private final ArrayList<TopPickDataSet> mTopPicksList;

            TopPicksListAdapter(ArrayList<TopPickDataSet> topPicksList) {
                this.mTopPicksList = topPicksList;
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                return new RestaurantCategoryListViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.rc_row_top_pick_list, viewGroup, false));
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
                RestaurantCategoryListViewHolder restaurantCategoryListViewHolder = (RestaurantCategoryListViewHolder) viewHolder;
                restaurantCategoryListViewHolder.tv_Title.setText(mTopPicksList.get(position).getName());
                AppFunctions.imageLoaderUsingGlide(mTopPicksList.get(position).getLogo(), restaurantCategoryListViewHolder.iv_Image, mActivity);
                //  Glide.with(mActivity).load(R.drawable.x_top_pick_sample_1).into(restaurantCategoryListViewHolder.iv_Image);

                restaurantCategoryListViewHolder.mTopPicksRowLay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String mTopPicksId = mTopPicksList.get(position).getId();

                        if (mTopPicksId.equals("1")) {
                            // AppFunctions.toastShort(mActivity, mTopPicksList.get(position).getName());
                            toPerformPastOrders();
                        } else {
                            toPerformAllShops(position);
                        }

                        //                    if(mTopPicksId.equals("2")){
                        //                        //All shops :-
                        //                        toPerformAllShops(position);
                        //                    }else {
                        //                        AppFunctions.toastShort(mActivity,mTopPicksList.get(position).getName());
                        //                    }

                    }
                });

                restaurantCategoryListViewHolder.tv_Title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String mTopPicksId = mTopPicksList.get(position).getId();
                        if (mTopPicksId.equals("1")) {
                            // AppFunctions.toastShort(mActivity, mTopPicksList.get(position).getName());
                            toPerformPastOrders();
                        } else {
                            toPerformAllShops(position);
                        }
                    }
                });

                restaurantCategoryListViewHolder.iv_Image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String mTopPicksId = mTopPicksList.get(position).getId();
                        if (mTopPicksId.equals("1")) {
                            // AppFunctions.toastShort(mActivity, mTopPicksList.get(position).getName());
                            toPerformPastOrders();
                        } else {
                            toPerformAllShops(position);
                        }
                    }
                });

            }

            public void toPerformPastOrders() {


                if (mActivity != null) {

                    FragmentTransaction mFT = mFragmentManager.beginTransaction();
                    MyOrderList m_myOrderList = new MyOrderList();
                    mFT.replace(R.id.layout_app_home_body, m_myOrderList, "m_myOrderList");
                    mFT.addToBackStack("m_myOrderList");
                    mFT.commit();

                }

            }

            public void toPerformAllShops(int position) {

                if (ARCuisinesDB.getInstance(mActivity).getSizeOfList() > 0) {
                    ARCuisinesDB.getInstance(mActivity).deleteCuisinesDB();
                }

                if (ARFiltersDB.getInstance(mActivity).getSizeOfList() > 0) {
                    ARFiltersDB.getInstance(mActivity).deleteDB();
                }

                String free_delivery = "";
                if (mTopPicksList.get(position).getId().equals("3")) {
                    free_delivery = "1";
                } else {
                    free_delivery = "0";
                }

                FragmentTransaction mFT = mFragmentManager.beginTransaction();
                AllRestaurants m_AllRestaurants = new AllRestaurants();
                Bundle mBundle = new Bundle();
                mBundle.putString("top_pick_id", free_delivery);
                mBundle.putString("business_type_id", "");
                m_AllRestaurants.setArguments(mBundle);
                mFT.replace(R.id.layout_app_home_body, m_AllRestaurants, "m_AllRestaurants");
                mFT.addToBackStack("m_AllRestaurants");
                mFT.commit();

            }

            @Override
            public int getItemCount() {
                return mTopPicksList.size();
            }

            class RestaurantCategoryListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
                ImageView iv_Image;
                TextView tv_Title;
                private LinearLayout mTopPicksRowLay;

                RestaurantCategoryListViewHolder(View itemView) {
                    super(itemView);
                    iv_Image = itemView.findViewById(R.id.iv_image_top_pick);
                    tv_Title = itemView.findViewById(R.id.tv_title_top_pick);
                    mTopPicksRowLay = itemView.findViewById(R.id.lay_row_top_pick_list);


                    iv_Image.setOnClickListener(this);
                    tv_Title.setOnClickListener(this);
                }

                @Override
                public void onClick(View v) {

                    /*if (!(UserRestaurantCategoryRecentSearchesDB.getInstance(mActivity).isRestaurantExist(RestaurantCategoryList.get(getAdapterPosition()).getName()))) {
                        UserRestaurantCategoryRecentSearchesDB.getInstance(mActivity).addRecentSearchesRestaurant(RestaurantCategoryList.get(getAdapterPosition()).getName());
                    }

                    FragmentTransaction mFT = mFragmentManager.beginTransaction();
                    Restaurant_Category_List mCategory_list = new Restaurant_Category_List();
                    Bundle args = new Bundle();
                    args.putString("RestaurantProductCategoryList", RestaurantCategoryList.get(getAdapterPosition()).getName());
                    args.putString("cuisine_id", RestaurantCategoryList.get(getAdapterPosition()).getCategoryId());
                    mCategory_list.setArguments(args);
                    mFT.replace(R.id.layout_home_restaurant_body, mCategory_list, "mRestaurant_CategoryList");
                    mFT.addToBackStack("mRestaurant_CategoryList");
                    mFT.commit();*/
                }
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

        private void homeBanner(final ArrayList<BannersDataSet> homeBannerList) {
            try {

                mBannerContainer.setVisibility(View.VISIBLE);
                mBannerSlider.setAdapter(new BannerAdapter(mActivity, homeBannerList, mFragmentManager/*, mListContext, mMenuPageMenuShowing*/, mActivity));
                mBannerCurrentPosition = mBannerSlider.getCurrentItem();

                Field mScroller;
                mScroller = ViewPager.class.getDeclaredField("mScroller");
                mScroller.setAccessible(true);
                HomeBannerScroller mHomeBannerScroller = new HomeBannerScroller(mActivity, mHomeInterpolator);
                //  HomeBannerScroller scroller = new HomeBannerScroller(mViewPagerSlider.getContext());
                // scroller.setFixedDuration(5000);
                mScroller.set(mBannerSlider, mHomeBannerScroller);


            } catch (Exception e) {
                mBannerContainer.setVisibility(View.GONE);
                // //Log.e("NoSuchFieldException", e.toString());
            }


            // Timer for auto sliding
            bannerScrollingProcess();


            if (mBannerSliderButtonContainer != null) {
                mBannerSliderButtonContainer.removeAllViews();
            }


            if (homeBannerList != null) {
                for (int i = 0; i < homeBannerList.size(); i++) {
                    View view = LayoutInflater.from(mActivity).inflate(R.layout.home_banner_button, null, false);
                    Button button = view.findViewById(R.id.btn_one);
                    button.setPadding(100, 100, 100, 100);
                    int size = (int) mActivity.getResources().getDisplayMetrics().density;
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


            mBannerSlider.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    mBannerSliderButtonContainer.removeAllViews();
                    //if (homeBannerList != null) {
                    if (homeBannerList.size() > 0) {
                        for (int i = 0; i < homeBannerList.size(); i++) {
                            View view = LayoutInflater.from(mActivity).inflate(R.layout.home_banner_button, null, false);
                            Button button = view.findViewById(R.id.btn_one);


                            int size = (int) mActivity.getResources().getDisplayMetrics().density;
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

        private void bannerScrollingProcess() {

            //        if (mTimer == null) {

               /* mBannerSlider.setCurrentItem(0, true);
                mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mActivity != null) {

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    //   Log.d("Initial Page is ", " " + String.valueOf(mPage));
                                    if (mHomeModulesApi.bannersList != null && mHomeModulesApi.bannersList.size() > 0) {
                                        if (mBannerPosition == mHomeModulesApi.bannersList.size()) {
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
                    }
                }, 500, 3000);*/

            //    }
            stopThread();

            mBannerHandler = new Handler();
            mBannerRunnable = new Runnable() {
                public void run() {

                    if (mActivity != null) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //   Log.d("Initial Page is ", " " + String.valueOf(mPage));
                                if (mHomeModulesApi.bannersList != null && mHomeModulesApi.bannersList.size() > 0) {
                                    if (mBannerPosition == mHomeModulesApi.bannersList.size()) {
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

    }

}
