package com.ordenese.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.DirectionsJSONParser;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.OrderTrackProductDataSet;
import com.ordenese.DataSets.OrderTrackProductOptionDataSet;
import com.ordenese.DataSets.TrackOrderApi;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CheckOutBackPress;
import com.ordenese.Interfaces.OrderConfirmBackPressUI;
import com.ordenese.R;
import com.ordenese.databinding.OrderConfirmationBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderConfirmation extends Fragment implements View.OnClickListener {

    private OrderConfirmationBinding mOConfirmBinding;
    private RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;
    private String mThisPageFrom = "", mCheckOutAddsLatitude = "", mCheckOutAddsLongitude = "";

    private CheckOutBackPress mCheckOutBackPress;

    //Google maps for location :-
    private Bundle mSavedInstanceState;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    CameraUpdate cu;
    LatLngBounds bounds;
    private String mLatitude = "", mLongitude = "";
    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};
    private final int curMapTypeIndex = 1;

    String mVendorName = "", mVendorID = "", mOrderID = "", mCheckOutAddNote = "", driver_id = "";
    private TrackOrderApi mTrackOrderApi;

    Boolean mIsTrackOrderUiShowed = false;
    OrderConfirmBackPressUI mOCBackPressUI;
    Handler handler_loc;
    Runnable runnable_loc;
    Activity activity;

    public OrderConfirmation() {
        // Required empty public constructor
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
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.order_confirmation, container, false);

        mOConfirmBinding = OrderConfirmationBinding.inflate(inflater, container, false);
        mCheckOutBackPress = (CheckOutBackPress) getActivity();
        mOCBackPressUI = (OrderConfirmBackPressUI) getActivity();

        //google maps for delivery location :-
        mSavedInstanceState = savedInstanceState;
        mOConfirmBinding.mapViewOrderConfirmLocation.onCreate(mSavedInstanceState);

        if (getArguments() != null) {
            if (getArguments().getString("from").equals("order_info")) {
                mCheckOutBackPress.checkOutSuccessStatus(false);
            } else {
                mCheckOutBackPress.checkOutSuccessStatus(true);
            }
        }

        if (getActivity() != null) {
            if (getArguments() != null) {
                mThisPageFrom = getArguments().getString(DefaultNames.from);
//                mCheckOutAddsLatitude = getArguments().getString(DefaultNames.checkOutAddsLatitude);
//                mCheckOutAddsLongitude = getArguments().getString(DefaultNames.checkOutAddsLongitude);
                mOrderID = getArguments().getString(DefaultNames.order_id);
                if (mThisPageFrom != null) {
                    if (mThisPageFrom.equals(DefaultNames.fromCheckOut)) {
                        mOConfirmBinding.tvOrderConfirmPageTitle.setText(getActivity().getResources().getString(R.string.oc_order_confirmation));
                    }
                }
            }
        }

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mOConfirmBinding.imgOrderConfirmBack.setOnClickListener(this);

        if (AppFunctions.mIsArabic(getActivity())) {
            Glide.with(getActivity()).load(R.drawable.x_track_rider_ar).into(mOConfirmBinding.imgDeliveryBoyInfoImg);
        } else {
            Glide.with(getActivity()).load(R.drawable.x_track_rider_en).into(mOConfirmBinding.imgDeliveryBoyInfoImg);
        }

        String mPrefix_rider = getActivity().getString(R.string.app_name);
        String mPostfix_rider = getActivity().getString(R.string.oc_rider);
        String mFinal_rider = mPrefix_rider + " " + mPostfix_rider;
        mOConfirmBinding.tvDeliveryBoyInfoTitle.setText(getActivity().getString(R.string.oc_finding_available_rider));
        mOConfirmBinding.tvDeliveryBoyInfoDriverStatus.setVisibility(View.GONE);

        handler_loc = new Handler();
        runnable_loc = new Runnable() {
            public void run() {
                if (driver_id.isEmpty() && driver_id.equals("")) {
                    if (getActivity() != null) {
                        callDriverTrackApi();
                    }
                    handler_loc.postDelayed(runnable_loc, 15000);
                } else {
                    if (getActivity() != null) {
                        callDriverTrackApi();
                    }
                    handler_loc.postDelayed(runnable_loc, 120000);
                }
            }
        };
        handler_loc.postDelayed(runnable_loc, 300);

        mOConfirmBinding.tvDeliveryBoyMobNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(activity,
                        Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    phoneCall();
                } else {
                    final String[] PERMISSIONS_STORAGE = {Manifest.permission.CALL_PHONE};
                    //Asking request Permissions
                    ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, 9);
                }
            }
        });

        return mOConfirmBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if (mId == R.id.img_order_confirm_back) {

            if (getArguments() != null) {
                if (getArguments().getString("from").equals("order_info")) {
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                } else {
                    if (mCheckOutBackPress != null) {
                        mCheckOutBackPress.checkOutSuccessBackPressed();
                    }
                }
            }

//            if (mIsTrackOrderUiShowed) {
//                mIsTrackOrderUiShowed = false;
//                mOConfirmBinding.layOrderConfirmUi.setVisibility(View.VISIBLE);
//                mOConfirmBinding.layOrderConfirmTrackOrderUi.setVisibility(View.GONE);
//                if (mThisPageFrom.equals(DefaultNames.fromCheckOut)) {
//                    mOConfirmBinding.tvOrderConfirmPageTitle.setText(getActivity().getResources().getString(R.string.oc_order_confirmation));
//                }
//            } else {
//                if (mCheckOutBackPress != null) {
//                    mCheckOutBackPress.checkOutSuccessBackPressed();
//                }
//            }
        } else if (mId == R.id.tv_delivery_boy_info_driver_status) {
            trackOrderMapLoading();
        } else if (mId == R.id.lay_order_confirm_preparing_time_parent) {
            trackOrderMapLoading();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            callOrderTrackApi();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (handler_loc != null && runnable_loc != null) {
            handler_loc.removeCallbacks(runnable_loc);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler_loc != null && runnable_loc != null) {
            handler_loc.removeCallbacks(runnable_loc);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean permissionGranted = false;
        switch (requestCode) {
            case 9:
                permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (permissionGranted) {
            phoneCall();
        } else {
            Toast.makeText(activity, "You don't assign permission.", Toast.LENGTH_SHORT).show();
        }
    }

    private void phoneCall() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + mTrackOrderApi.orderTrack.driver_mobile));
        activity.startActivity(intent);
    }


    private void loadRestaurantLocationMap() {

        if (getActivity() != null) {

            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .build();

//            // Create the LocationRequest object
//            mLocationRequest = LocationRequest.create()
//                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                    .setInterval(5000)        // 5 seconds, in milliseconds
//                    .setFastestInterval(5000); // 5 second, in milliseconds

        }

    }

    private void restaurantLocationMapInitializerAndConnect(MapView mapView) {


        if (mapView != null) {
            mapView.onResume(); // needed to get the map to display immediately

            try {
                if (getActivity() != null) {
                    MapsInitializer.initialize(getActivity().getApplicationContext());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap mMap) {
                    mGoogleMap = mMap;
                    initialCameraPosition();
                }
            });
        }


        if (mGoogleApiClient != null) {
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }

    }

    private void initialCameraPosition() {

        if (getActivity() != null) {

            if (mLatitude != null && mLongitude != null && !mLatitude.isEmpty() && !mLongitude.isEmpty()) {
                double m__Latitude = Double.parseDouble(mLatitude);
                double m__Longitude = Double.parseDouble(mLongitude);

                double m__CheckoutAddsLatitude = Double.parseDouble(mCheckOutAddsLatitude);
                double m__CheckoutAddsLongitude = Double.parseDouble(mCheckOutAddsLongitude);

                //  //Log.e("initialCameraPosition: ", mLatitude + "/1");
                //  //Log.e("initialCameraPosition: ", mLongitude + "/2");
                //  //Log.e("initialCameraPosition: ", m__Longitude + "/3");
                //  //Log.e("initialCameraPosition: ", m__Latitude + "/4");

                LatLng pickup = new LatLng(m__Latitude, m__Longitude);
                LatLng delivery = new LatLng(m__CheckoutAddsLatitude, m__CheckoutAddsLongitude);

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 16.00f));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(delivery));

                mGoogleMap.setMapType(MAP_TYPES[curMapTypeIndex]);
                mGoogleMap.setTrafficEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.vendor_icon);

                //Vendor location :-
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(m__Latitude, m__Longitude))
                        .icon(icon));
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                //delivery address location :-
                BitmapDescriptor icon_1 = BitmapDescriptorFactory.fromResource(R.drawable.driver_icon);
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(m__CheckoutAddsLatitude, m__CheckoutAddsLongitude))
                        .icon(icon_1));
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

                bounds = new LatLngBounds.Builder()
                        .include(pickup)
                        .include(delivery)
                        .build();
                Point displaySize = new Point();
                getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
                cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);

                DownloadTask downloadTask = new DownloadTask();
                // Getting URL to the Google Directions API
                String Direction_url = getDirectionsUrl(pickup, delivery);
                // Start downloading json data from Google Directions API
                downloadTask.execute(Direction_url);

                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50, 50, 0));

                mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        //  AppFunctions.toastLong(getActivity(),marker.getSnippet());
                        return false;
                    }
                });

                mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {

                    }
                });

                mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {

                    }
                });

                mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {

                    }
                });
            }
        }
    }

    private void callOrderTrackApi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.order_id, mOrderID);
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }

                    mProgressDialog.show();
                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<TrackOrderApi> Call = retrofitInterface.trackOrderApi(mCustomerAuthorization, body);
                    Call.enqueue(new Callback<TrackOrderApi>() {
                        @SuppressLint("UseCompatLoadingForDrawables")
                        @Override
                        public void onResponse(@NonNull Call<TrackOrderApi> call, @NonNull Response<TrackOrderApi> response) {

                            mProgressDialog.cancel();

                            if (getActivity() != null) {

                                if (response.isSuccessful()) {
                                    mTrackOrderApi = response.body();

                                    if (mTrackOrderApi != null) {
                                        //Log.e("mTrackOrderApi", "not null");
                                        if (mTrackOrderApi.success != null) {
                                            //Api response successDataSet :-
                                            String mWithin = getActivity().getResources().getString(R.string.within);
                                            String mMins = getActivity().getResources().getString(R.string.minutes);
                                            if (mTrackOrderApi.orderTrack.schedule_status.equals("1")) {
                                                mOConfirmBinding.tvOrderConfirmPreparingTime.setText(mTrackOrderApi.orderTrack.schedule_date + " " + mTrackOrderApi.orderTrack.schedule_time);
                                            } else {
                                                mOConfirmBinding.tvOrderConfirmPreparingTime.setText(mTrackOrderApi.orderTrack.delivery_time + " " + mMins);
                                            }
                                            mLatitude = mTrackOrderApi.orderTrack.vendor_latitude;
                                            mLongitude = mTrackOrderApi.orderTrack.vendor_longitude;
                                            mVendorName = mTrackOrderApi.orderTrack.vendor_name;
                                            mCheckOutAddNote = mTrackOrderApi.orderTrack.note;

                                            mOConfirmBinding.tvOcDeliveryAddsName.setText(mTrackOrderApi.orderTrack.zone_name);
                                            mOConfirmBinding.tvOcDeliveryAddsMain.setText(mTrackOrderApi.orderTrack.delivery_address);
                                            String mMob = getActivity().getResources().getString(R.string.mobile) + ": " +
                                                    mTrackOrderApi.orderTrack.customer_country_code + "-" + mTrackOrderApi.orderTrack.customer_mobile;
                                            mOConfirmBinding.tvOcDeliveryAddsSubMobile.setText(mMob);
                                            mOConfirmBinding.tvOcDeliveryAddsSub.setText("");

                                            mOConfirmBinding.tvOcOrderId.setText(mTrackOrderApi.orderTrack.order_id);
                                            mOConfirmBinding.tvOcOrderAmt.setText(mTrackOrderApi.orderTrack.total);
                                            mOConfirmBinding.tvOcOrderPayment.setText(mTrackOrderApi.orderTrack.payment_method);

                                            mOConfirmBinding.tvOcOrderVendorName.setText(mTrackOrderApi.orderTrack.vendor_name);

                                            mOConfirmBinding.layOrderConfirmUi.setVisibility(View.VISIBLE);
                                            mOConfirmBinding.layOrderConfirmTrackOrderUi.setVisibility(View.GONE);

                                            mCheckOutAddsLatitude = mTrackOrderApi.orderTrack.customer_latitude;
                                            mCheckOutAddsLongitude = mTrackOrderApi.orderTrack.customer_longitude;

                                            mOConfirmBinding.progressLinear.setVisibility(View.VISIBLE);
                                            switch (mTrackOrderApi.orderTrack.order_status_id) {
                                                case "1": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.oc_sending_your_order_to);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mVendorName);
                                                    break;
                                                }
                                                case "2": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.oc_sending_your_order_to);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mTrackOrderApi.orderTrack.order_status);
                                                    break;
                                                }
                                                case "3": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done2.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.your_order_is);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mTrackOrderApi.orderTrack.order_status);

                                                    break;
                                                }
                                                case "8": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done2.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done3.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.your_order_is);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + activity.getResources().getString(R.string.on_the_way));

                                                    break;
                                                }
                                                case "6": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done2.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done3.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(mTrackOrderApi.orderTrack.order_status);

                                                    break;
                                                }
                                                case "5": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done2.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done3.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.your_order_is);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mTrackOrderApi.orderTrack.order_status);

                                                    break;
                                                }
                                                case "9": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done2.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done3.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done4.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.your_order_is);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mTrackOrderApi.orderTrack.order_status);
                                                    break;
                                                }
                                                default: {
                                                    String MsgPrefix = getActivity().getResources().getString(R.string.oc_sending_your_order_to);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mVendorName);
                                                    mOConfirmBinding.progressLinear.setVisibility(View.GONE);
                                                    break;
                                                }
                                            }

                                            if (mTrackOrderApi.orderTrack.order_status_id.equals("5") || mTrackOrderApi.orderTrack.order_status_id.equals("6")
                                                    || mTrackOrderApi.orderTrack.order_status_id.equals("8")) {
                                                mOConfirmBinding.tvDeliveryBoyInfoDriverStatus.setVisibility(View.VISIBLE);
                                                mOConfirmBinding.tvOrderConfirmPageTitle.setText(activity.getResources().getString(R.string.track_order_));
                                            } else if (mTrackOrderApi.orderTrack.order_status_id.equals("9")) {
                                                mOConfirmBinding.tvDeliveryBoyInfoDriverStatus.setVisibility(View.GONE);
                                                mOConfirmBinding.tvDeliveryBoyInfoDriverStatus.setEnabled(false);
                                                mOConfirmBinding.tvOrderConfirmPageTitle.setText(activity.getResources().getString(R.string.order_information));
                                                mOConfirmBinding.tvTitleDeliveryTime.setVisibility(View.GONE);
                                                mOConfirmBinding.tvOrderConfirmPreparingTime.setVisibility(View.GONE);
                                            } else {
                                                mOConfirmBinding.tvOrderConfirmPageTitle.setText(activity.getResources().getString(R.string.oc_order_confirmation));
                                            }

                                            if (mTrackOrderApi.orderTrack.order_status_id.equals("4") || mTrackOrderApi.orderTrack.order_status_id.equals("7")
                                                    || mTrackOrderApi.orderTrack.order_status_id.equals("13")) {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                                                alertDialogBuilder
                                                        .setMessage(activity.getResources().getString(R.string.your_order_is) + mTrackOrderApi.orderTrack.order_status)
                                                        .setCancelable(true)
                                                        .setPositiveButton(activity.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.dismiss();
                                                                if (getArguments() != null) {
                                                                    if (getArguments().getString("from").equals("order_info")) {
                                                                        if (getActivity() != null) {
                                                                            getActivity().finish();
                                                                        }
                                                                    } else {
                                                                        if (mCheckOutBackPress != null) {
                                                                            mCheckOutBackPress.checkOutSuccessBackPressed();
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });

                                                AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.show();
                                            }

                                            loadRestaurantLocationMap();
                                            //Google map initializer and connection :-
                                            restaurantLocationMapInitializerAndConnect(mOConfirmBinding.mapViewOrderConfirmLocation);

                                            if (!mTrackOrderApi.orderTrack.driver_id.equals("")) {

                                                driver_id = mTrackOrderApi.orderTrack.driver_id;

                                                mOConfirmBinding.animationView.setVisibility(View.GONE);
                                                mOConfirmBinding.tvOrderConfirmPreparingTime.setText(mTrackOrderApi.orderTrack.delivery_time + " " + mMins);
                                                mOConfirmBinding.tvDeliveryBoyInfoTitle.setText(activity.getResources().getString(R.string.name) + " : " + mTrackOrderApi.orderTrack.driver_name);
                                                mOConfirmBinding.tvDeliveryBoyMobNo.setVisibility(View.VISIBLE);
                                                mOConfirmBinding.tvDeliveryBoyMobNo.setText(activity.getResources().getString(R.string.mobile_) + " : " + mTrackOrderApi.orderTrack.driver_mobile);
                                                mOConfirmBinding.tvDeliveryBoyInfoDriverStatus.setText(getActivity().getString(R.string.track_the_rider));
                                                AppFunctions.imageLoaderUsingGlide(mTrackOrderApi.orderTrack.driver_profile, mOConfirmBinding.imgDeliveryBoyInfoImg, activity);

                                                mOConfirmBinding.tvDeliveryBoyInfoDriverStatus.setOnClickListener(OrderConfirmation.this);
                                                mOConfirmBinding.layOrderConfirmPreparingTimeParent.setOnClickListener(OrderConfirmation.this);
                                            } /*else {
                                                mOConfirmBinding.animationView.setAnimation(R.raw.lotti_order_track_vendor);
                                            }*/

                                            if (mTrackOrderApi.orderTrack != null
                                                    && mTrackOrderApi.orderTrack.orderTrackProduct != null
                                                    && mTrackOrderApi.orderTrack.orderTrackProduct.size() > 0) {

                                                LayoutInflater mProductsListInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                                if (mProductsListInflater != null) {

                                                    mOConfirmBinding.layoutOcOrderProductsContainer.removeAllViews();

                                                    ArrayList<OrderTrackProductDataSet> mProductList = mTrackOrderApi.orderTrack.orderTrackProduct;

                                                    for (int products = 0; products < mProductList.size(); products++) {

                                                        View vw = mProductsListInflater.inflate(R.layout.order_confirm_product_row, null);
                                                        int width = LinearLayout.LayoutParams.MATCH_PARENT;
                                                        int height = LinearLayout.LayoutParams.WRAP_CONTENT;

                                                        //  Log.e("**** products=> ",String.valueOf(products));

                                                        TextView mProductName, mProductOptions, mProductQty;

                                                        LinearLayout mProductOptionsContainer = vw.findViewById(R.id.lay_oc_product_options);

                                                        //  ImageView mProductImg = vw.findViewById(R.id.img_oc_product_image);

                                                        mProductName = vw.findViewById(R.id.tv_oc_product_name);
                                                        //mProductOptions = (TextView) vw.findViewById(R.id.tv_restaurant_my_orders_more_details_product_options);
                                                        mProductQty = vw.findViewById(R.id.tv_oc_product_qty);
                                                        mProductOptions = vw.findViewById(R.id.tv_oc_product_options);

                                                        if (mProductList.get(products).orderTrackProductOption != null
                                                                && mProductList.get(products).orderTrackProductOption.size() > 0) {
                                                            mProductOptionsContainer.setVisibility(View.VISIBLE);
                                                            String mOptionData = "";
                                                            ArrayList<OrderTrackProductOptionDataSet> mOptionList = mProductList.get(products).orderTrackProductOption;
                                                            for (int options = 0; options < mOptionList.size(); options++) {

                                                                String mTitle = mOptionList.get(options).option_name;
                                                                String mValue = mOptionList.get(options).option_value;

                                                                String mOption_Data = mTitle + " : " + mValue;
                                                                if (mOptionData.isEmpty()) {


                                                                    mOptionData = "(" + mOption_Data;
                                                                } else {
                                                                    mOptionData = mOptionData + mOption_Data;
                                                                }

                                                            }

                                                            if (!mOptionData.isEmpty()) {
                                                                mOptionData = mOptionData + ")";
                                                                mProductOptions.setText(mOptionData);
                                                            }


                                                        } else {
                                                            mProductOptionsContainer.setVisibility(View.GONE);
                                                        }

                                                        //  mProductImgContainer.setVisibility(View.GONE);

                                                        mProductName.setText(mProductList.get(products).name);
                                                        String mProductCOUNT = mProductList.get(products).quantity + "     " + getActivity().getString(R.string.product_count_x);
                                                        mProductQty.setText(mProductCOUNT);

                                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
                                                        params.setMargins(0, 4, 0, 4);
                                                        vw.setLayoutParams(params);

                                                        // mOrderProductsContainer.addView(vw);
                                                        //layout_moi_order_item_1
                                                        mOConfirmBinding.layoutOcOrderProductsContainer.addView(vw);

                                                    }
                                                }
                                            }

                                        } else {
                                            mProgressDialog.cancel();
                                            //Api response failure :-
                                            if (mTrackOrderApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mTrackOrderApi.error.message);
                                            }
                                        }
                                    } else {
                                        mProgressDialog.cancel();
                                        //Log.e("mTrackOrderApi", "null");
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
                        public void onFailure(@NonNull Call<TrackOrderApi> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }
                    });

                } catch (Exception e) {

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

    private void callDriverTrackApi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.order_id, mOrderID);

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<TrackOrderApi> Call = retrofitInterface.trackOrderApi(mCustomerAuthorization, body);
                    Call.enqueue(new Callback<TrackOrderApi>() {
                        @SuppressLint("UseCompatLoadingForDrawables")
                        @Override
                        public void onResponse(@NonNull Call<TrackOrderApi> call, @NonNull Response<TrackOrderApi> response) {
                            if (getActivity() != null) {
                                if (response.isSuccessful()) {
                                    mTrackOrderApi = response.body();
                                    if (mTrackOrderApi != null) {
                                        //Log.e("mTrackOrderApi", "not null");
                                        if (mTrackOrderApi.success != null) {
                                            //Api response successDataSet :-

                                            String mWithin = getActivity().getResources().getString(R.string.within);
                                            String mMins = getActivity().getResources().getString(R.string.minutes);

                                            if (mTrackOrderApi.orderTrack.schedule_status.equals("1")) {
                                                mOConfirmBinding.tvOrderConfirmPreparingTime.setText(mTrackOrderApi.orderTrack.schedule_date + " " + mTrackOrderApi.orderTrack.schedule_time);
                                            } else {
                                                mOConfirmBinding.tvOrderConfirmPreparingTime.setText(mTrackOrderApi.orderTrack.delivery_time + " " + mMins);
                                            }

                                            if (!mTrackOrderApi.orderTrack.driver_id.equals("")) {
                                                driver_id = mTrackOrderApi.orderTrack.driver_id;
                                                mOConfirmBinding.tvOrderConfirmPreparingTime.setText(mTrackOrderApi.orderTrack.delivery_time + " " + mMins);
                                                mOConfirmBinding.animationView.setVisibility(View.GONE);
                                                mOConfirmBinding.tvDeliveryBoyInfoTitle.setText(activity.getResources().getString(R.string.name) + " : " + mTrackOrderApi.orderTrack.driver_name);
                                                mOConfirmBinding.tvDeliveryBoyMobNo.setVisibility(View.VISIBLE);
                                                mOConfirmBinding.tvDeliveryBoyMobNo.setText(activity.getResources().getString(R.string.mobile_) + " : " + mTrackOrderApi.orderTrack.driver_mobile);
                                                mOConfirmBinding.tvDeliveryBoyInfoDriverStatus.setText(getActivity().getString(R.string.track_the_rider));
                                                AppFunctions.imageLoaderUsingGlide(mTrackOrderApi.orderTrack.driver_profile, mOConfirmBinding.imgDeliveryBoyInfoImg, activity);

                                                mOConfirmBinding.tvDeliveryBoyInfoDriverStatus.setOnClickListener(OrderConfirmation.this);
                                                mOConfirmBinding.layOrderConfirmPreparingTimeParent.setOnClickListener(OrderConfirmation.this);
                                            }

                                            mOConfirmBinding.progressLinear.setVisibility(View.VISIBLE);
                                            switch (mTrackOrderApi.orderTrack.order_status_id) {
                                                case "1": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.oc_sending_your_order_to);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mVendorName);
                                                    break;
                                                }
                                                case "2": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.oc_sending_your_order_to);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mTrackOrderApi.orderTrack.order_status);
                                                    break;
                                                }
                                                case "3": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done2.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.your_order_is);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mTrackOrderApi.orderTrack.order_status);

                                                    break;
                                                }
                                                case "8": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done2.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done3.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.your_order_is);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + activity.getResources().getString(R.string.on_the_way));

                                                    break;
                                                }
                                                case "6": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done2.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done3.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(mTrackOrderApi.orderTrack.order_status);
                                                    break;
                                                }
                                                case "5": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done2.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done3.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done_grey));
                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.grey_200));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.your_order_is);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mTrackOrderApi.orderTrack.order_status);

                                                    break;
                                                }
                                                case "9": {
                                                    mOConfirmBinding.done1.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done1.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done2.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done2.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done3.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done3.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));
                                                    mOConfirmBinding.done4.setBackground(activity.getResources().getDrawable(R.drawable.bg_shape_round_done));
                                                    mOConfirmBinding.done4.setImageDrawable(activity.getResources().getDrawable(R.drawable.baseline_done_white_24dp));

                                                    mOConfirmBinding.doneView1.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView2.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));
                                                    mOConfirmBinding.doneView3.setBackgroundColor(activity.getResources().getColor(R.color.req_new_cart_checkout_btn_color));

                                                    String MsgPrefix = getActivity().getResources().getString(R.string.your_order_is);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mTrackOrderApi.orderTrack.order_status);
                                                    break;
                                                }
                                                default: {
                                                    String MsgPrefix = getActivity().getResources().getString(R.string.oc_sending_your_order_to);
                                                    mOConfirmBinding.tvOrderConfirmPreparingTimeMsg.setText(MsgPrefix + " " + mVendorName);
                                                    mOConfirmBinding.progressLinear.setVisibility(View.GONE);
                                                    break;
                                                }
                                            }

                                            if (mTrackOrderApi.orderTrack.order_status_id.equals("5") || mTrackOrderApi.orderTrack.order_status_id.equals("6")
                                                    || mTrackOrderApi.orderTrack.order_status_id.equals("8")) {
                                                mOConfirmBinding.tvDeliveryBoyInfoDriverStatus.setVisibility(View.VISIBLE);
                                                mOConfirmBinding.tvOrderConfirmPageTitle.setText(activity.getResources().getString(R.string.track_order_));
                                            } else if (mTrackOrderApi.orderTrack.order_status_id.equals("9")) {
                                                mOConfirmBinding.tvDeliveryBoyInfoDriverStatus.setEnabled(false);
                                                mOConfirmBinding.tvDeliveryBoyInfoDriverStatus.setVisibility(View.GONE);
                                                mOConfirmBinding.tvOrderConfirmPageTitle.setText(activity.getResources().getString(R.string.order_information));
                                                mOConfirmBinding.tvTitleDeliveryTime.setVisibility(View.GONE);
                                                mOConfirmBinding.tvOrderConfirmPreparingTime.setVisibility(View.GONE);
                                            } else {
                                                mOConfirmBinding.tvOrderConfirmPageTitle.setText(activity.getResources().getString(R.string.oc_order_confirmation));
                                            }

                                            if (mTrackOrderApi.orderTrack.order_status_id.equals("4") || mTrackOrderApi.orderTrack.order_status_id.equals("7")
                                                    || mTrackOrderApi.orderTrack.order_status_id.equals("13")) {

                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);
                                                alertDialogBuilder
                                                        .setMessage(activity.getResources().getString(R.string.your_order_is) + mTrackOrderApi.orderTrack.order_status)
                                                        .setCancelable(true)
                                                        .setPositiveButton(activity.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.dismiss();
                                                                if (getArguments() != null) {
                                                                    if (getArguments().getString("from").equals("order_info")) {
                                                                        if (getActivity() != null) {
                                                                            getActivity().finish();
                                                                        }
                                                                    } else {
                                                                        if (mCheckOutBackPress != null) {
                                                                            mCheckOutBackPress.checkOutSuccessBackPressed();
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        });

                                                AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.show();
                                            }

                                            if (mTrackOrderApi.orderTrack.order_status_id.equals("9")) {
                                                if (handler_loc != null && runnable_loc != null) {
                                                    handler_loc.removeCallbacks(runnable_loc);
                                                }
                                            }

                                        } else {
                                            //Api response failure :-
                                            if (mTrackOrderApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mTrackOrderApi.error.message);
                                            }
                                        }
                                    }
                                }
                            }
                            mProgressDialog.cancel();
                        }

                        @Override
                        public void onFailure(@NonNull Call<TrackOrderApi> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }
                    });

                } catch (Exception e) {
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

    private void trackOrderMapLoading() {

        FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
        TrackOrder trackOrder = new TrackOrder();
        Bundle bundle = new Bundle();
        bundle.putString(DefaultNames.from, DefaultNames.fromCheckOut);
        bundle.putString(DefaultNames.order_id, mOrderID);
        bundle.putString("mLatitude", mLatitude);
        bundle.putString("mLongitude", mLongitude);
        bundle.putString("mCheckOutAddsLatitude", mCheckOutAddsLatitude);
        bundle.putString("mCheckOutAddsLongitude", mCheckOutAddsLongitude);
        bundle.putString("driver_id", driver_id);
        bundle.putString("delivery_time", mTrackOrderApi.orderTrack.delivery_time);
        bundle.putString(DefaultNames.order_id, mOrderID);
        trackOrder.setArguments(bundle);
        mFT.replace(R.id.layout_app_check_out_body, trackOrder, "trackOrder");
        mFT.addToBackStack("trackOrder");
        mFT.commit();

        /*if (getActivity() != null) {

            if (mThisPageFrom.equals(DefaultNames.fromCheckOut)) {
                mOConfirmBinding.tvOrderConfirmPageTitle.setText("");
            }

            mIsTrackOrderUiShowed = true;

            mOCBackPressUI.orderConfirmBackPressUI(true,
                    mOConfirmBinding.layOrderConfirmUi, mOConfirmBinding.layOrderConfirmTrackOrderUi, mOConfirmBinding.tvOrderConfirmPageTitle);

            mOConfirmBinding.layOrderConfirmUi.setVisibility(View.GONE);
            mOConfirmBinding.layOrderConfirmTrackOrderUi.setVisibility(View.VISIBLE);

            String mWithin = getActivity().getResources().getString(R.string.within);
            String mMins = getActivity().getResources().getString(R.string.mins);
            mOConfirmBinding.tvOcDeliveryTimeMsg.setText(getActivity().getResources().getString(R.string.oc_estimated_delivery_time));
            mOConfirmBinding.tvOcDeliveryTime.setText(mWithin + " 20 " + mMins);

            mOConfirmBinding.mapViewOrderConfirmTrackOrder.onCreate(mSavedInstanceState);

            //Google map initializer and connection :-
            MapView mapView = mOConfirmBinding.mapViewOrderConfirmTrackOrder;

            if (mapView != null) {
                mapView.onResume(); // needed to get the map to display immediately
                try {
                    if (getActivity() != null) {
                        MapsInitializer.initialize(getActivity().getApplicationContext());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap mMap) {
                        mGoogleMap = mMap;
                        if (mLatitude != null && mLongitude != null && !mLatitude.isEmpty() && !mLongitude.isEmpty()) {

                            double m__Latitude = Double.parseDouble(mLatitude);
                            double m__Longitude = Double.parseDouble(mLongitude);

                            double m__CheckoutAddsLatitude = Double.parseDouble(mCheckOutAddsLatitude);
                            double m__CheckoutAddsLongitude = Double.parseDouble(mCheckOutAddsLongitude);

                            //  Log.e("initialCameraPosition: ", mLatitude + "/1");
                            //  Log.e("initialCameraPosition: ", mLongitude + "/2");
                            //  //Log.e("initialCameraPosition: ", m__Longitude + "/3");
                            //  //Log.e("initialCameraPosition: ", m__Latitude + "/4");

                            LatLng pickup = new LatLng(m__Latitude, m__Longitude);
                            LatLng delivery = new LatLng(m__CheckoutAddsLatitude, m__CheckoutAddsLongitude);

                            mGoogleMap.setMapType(MAP_TYPES[curMapTypeIndex]);
                            mGoogleMap.setTrafficEnabled(true);
                            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

                            //Vendor location :-
                            mGoogleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(m__Latitude, m__Longitude))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            );
                            //delivery address location :-
                            mGoogleMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(m__CheckoutAddsLatitude, m__CheckoutAddsLongitude))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            );
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 16.00f));
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(delivery));

                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                            Query myDeliveryPostsQuery = mDatabase.child("drivers").child(driver_id);
                            myDeliveryPostsQuery.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    Log.e("onDataChange: ", snapshot.child("latitude").getValue(String.class) + "");
                                    Log.e("onDataChange:1 ", snapshot.child("longitude").getValue(String.class) + "");
                                    Log.e("onDataChange:2 ", snapshot.child("name").getValue(String.class) + "");

                                    double m__latitude = Double.parseDouble(snapshot.child("latitude").getValue(String.class));
                                    double m__longitude = Double.parseDouble(snapshot.child("longitude").getValue(String.class));

                                    current = new LatLng(m__latitude, m__longitude);

                                    //delivery address location :-
                                    mGoogleMap.addMarker(new MarkerOptions()
                                            .position(current)
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                    );

                                    bounds = new LatLngBounds.Builder()
                                            .include(pickup)
                                            .include(current)
                                            .include(delivery)
                                            .build();
//                                    Point displaySize = new Point();
//                                    getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
                                    cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                                    mGoogleMap.moveCamera(cu);
                                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            DownloadTask downloadTask = new DownloadTask();
                            // Getting URL to the Google Directions API
                            String Direction_url = getDirectionsUrl(pickup, delivery);
                            // Start downloading json data from Google Directions API
                            downloadTask.execute(Direction_url);

                            mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    //  AppFunctions.toastLong(getActivity(),marker.getSnippet());
                                    return false;
                                }
                            });

                            mGoogleMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                                @Override
                                public void onCameraMoveStarted(int i) {

                                }
                            });

                            mGoogleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                                @Override
                                public void onCameraIdle() {

                                }
                            });

                            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {

                                }
                            });
                        }
                    }
                });
            }
            if (mGoogleApiClient != null) {
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }*/
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String key = "key=" + getResources().getString(R.string.google_server_api_key); // Api Key

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + key;

        // Output format
        String output = "json";


        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        HttpURLConnection urlConnection = null;
        URL url = new URL(strUrl);

        // Creating an http connection to communicate with url
        urlConnection = (HttpURLConnection) url.openConnection();

        // Connecting to url
        urlConnection.connect();

        // Reading data from url
        try (InputStream iStream = urlConnection.getInputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            // Log.d("downloading url", e.toString());
        } finally {
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String data = "";
            try {
                data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            ParserTask parserTask = new ParserTask();

            parserTask.execute(s);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(strings[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = lists.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(6);
                lineOptions.color(Color.RED);
            }

            if (lineOptions != null) {
                mGoogleMap.addPolyline(lineOptions);
                mGoogleMap.moveCamera(cu);
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        }
    }


}