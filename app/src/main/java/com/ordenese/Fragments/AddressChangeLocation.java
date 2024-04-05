package com.ordenese.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.FieldSelector;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.Databases.AddressBookChangeLocationDB;

import com.ordenese.R;
import com.ordenese.databinding.AddressChangeLocationBinding;

import java.util.List;
import java.util.Locale;

public class AddressChangeLocation extends Fragment implements View.OnClickListener, /*GoogleApiClient.ConnectionCallbacks,*/
        /*GoogleApiClient.OnConnectionFailedListener,*/ OnMapReadyCallback, GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener/*, View.OnTouchListener*/{

    private AddressChangeLocationBinding mACLBinding;

    static AlertDialog alertDialog;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;
    private GoogleMap mGoogleMap;
    private int USER_LOCATION_PERMISSION_CODE = 41;
    private Boolean mIsMapLoaderLoading = true;
    private Geocoder geocoder;
    private Double Latitude, Longitude;
    private String Current_address_back_string,
            current_address_string, current_address_name_only_string;
    private PlacesClient placesClient;
    private FieldSelector fieldSelector;
    private RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;

    private boolean mIsAddressBookProcess = false;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 23487;

    private Boolean mIsAutoSearchProcessing = false;

    private String mIsAddsBookCallFrom = "";


    public AddressChangeLocation() {
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
       // return inflater.inflate(R.layout.address_change_location, container, false);
        mACLBinding = AddressChangeLocationBinding.inflate(inflater,container,false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        if (getActivity() != null) {
            if (getArguments() != null) {

                mIsAddsBookCallFrom = getArguments().getString(DefaultNames.addressBook_callFrom);
            }
        }

        mACLBinding.tvAddressTitle.setText(getActivity().getResources().getString(R.string.address));
        mACLBinding.btnAclDeliveryHere.setText(getActivity().getResources().getString(R.string.caa_confirm_location));


        mACLBinding.imgAclBack.setOnClickListener(this);
        mACLBinding.layAclSearchAddsContainer.setOnClickListener(this);
        // mACLBinding.layAclSearchAddsContainer.setOnTouchListener(this);
        mACLBinding.layAclDeliveryHere.setOnClickListener(this);
        mACLBinding.layCurrentAddressData.setOnClickListener(this);

        if (getActivity() != null) {
            // Initialize Places.
            Places.initialize(getActivity(), /*apiKey*/getResources().getString(R.string.google_server_api_key));
            // Retrieve a PlacesClient (previously initialized - see MainActivity)
            placesClient = Places.createClient(getActivity());

        }

        mACLBinding.mapViewAclDeliveryAddress.onCreate(savedInstanceState);
        fieldSelector = new FieldSelector(mACLBinding.useCustomFields1, mACLBinding.customFieldsList1);
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        return mACLBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if (mId == R.id.img_acl_back) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        } else if (mId == R.id.lay_acl_search_adds_container) {

            //Log.e("221","called");

            if (getActivity() != null) {

                //To check is map loader loading while fetching location is under progressing.
                if (!mIsMapLoaderLoading) {

                    // its a address book location process :-
                    //To call the google auto search in this page itself :-

                    //Log.e("241","called");

                    startAutocompleteActivity();

                }else {
                    AppFunctions.msgDialogOk(getActivity(),"",getActivity().getResources().getString(R.string.caa_please_wait));
                }





            }


        }
        else if (mId == R.id.lay_current_address_data) {

//            Log.e("lay_current_address_data","called");

            if (getActivity() != null) {

                //To check is map loader loading while fetching location is under progressing.
                if (!mIsMapLoaderLoading) {

                    // its a address book location process :-
                    //To call the google auto search in this page itself :-

                    //Log.e("241","called");

                    startAutocompleteActivity();

                }else {
                    AppFunctions.msgDialogOk(getActivity(),"",getActivity().getResources().getString(R.string.caa_please_wait));
                }
            }


        }else if (mId == R.id.lay_acl_delivery_here) {
            //***********************************************************************************

            //To check is map loader loading while fetching location is under progressing.
            if (!mIsMapLoaderLoading) {

                //tv_acl_current_address_data
                if (!mACLBinding.tvAclCurrentAddressData.getText().toString().isEmpty()) {

                    // its a address book location process :-

                    AreaGeoCodeDataSet mAreaDS = new AreaGeoCodeDataSet();
                    mAreaDS.setmAddress(current_address_string);
                    mAreaDS.setmLatitude(String.valueOf(Latitude));
                    mAreaDS.setmLongitude(String.valueOf(Longitude));
                    mAreaDS.setmAddsNameOnly(current_address_name_only_string);

                    if(AddressBookChangeLocationDB.getInstance(getActivity()).getSizeOfList() > 0){
                        AddressBookChangeLocationDB.getInstance(getActivity()).deleteDB();
                    }
                    AddressBookChangeLocationDB.getInstance(getActivity()).add(mAreaDS);

                    getParentFragmentManager().popBackStack();

                } else {
                    AppFunctions.toastLong(getActivity(), R.string.please_select_your_address);
                }
            }
            //**************************************************************************************
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                CheckGpsConnection();
            } else {
                // its a address book location process :-

                if(mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()){
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    if(mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)){
                        //Its for - checkout page address selection
                        mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    }else if(mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)){
                        //Its - my account page address book call.
                        mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    }
                }
            }
        }
    }
    @Override
    public void onStop() {
        super.onStop();
    }

    private void CheckGpsConnection() {

        if (getActivity() != null) {
            final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            assert manager != null;
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                createLocationServiceError(getActivity());
            } else {
                loadRestaurantLocationMap();

                new Handler().postDelayed(() -> {
                    if (getActivity() != null) {
                        loadRestaurantLocationMap();
                    }
                }, 3000);

            }
        }


    }

    public void createLocationServiceError(final Activity activityObj) {

        // show alert dialog if Internet is not connected
        AlertDialog.Builder builder = new AlertDialog.Builder(activityObj);

        builder.setMessage(getResources().getString(R.string.gps_enable_msg))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.setting),
                        (dialog, id) -> {
                            Intent intent = new Intent(
                                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            activityObj.startActivity(intent);
                            dialog.dismiss();
                        })
                .setNegativeButton(getResources().getString(R.string.cancel),
                        (dialog, id) -> dialog.dismiss());
        alertDialog = builder.create();
        alertDialog.show();
    }


    private void loadRestaurantLocationMap() {

        if (getActivity() != null) {


            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    // .addConnectionCallbacks(this)
                    //.addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(5000)        // 5 seconds, in milliseconds
                    .setFastestInterval(5000); // 5 second, in milliseconds


            mLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (mGoogleApiClient != null)
                        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
                            mGoogleApiClient.disconnect();
                            mGoogleApiClient.connect();
                        } else if (!mGoogleApiClient.isConnected()) {
                            mGoogleApiClient.connect();
                        }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };


            // initialCameraPosition();
            restaurantLocationMapInitializerAndConnect();

        }
    }


    private void restaurantLocationMapInitializerAndConnect() {

        //  ////Log.e("restaurantLocationMapInitializerAndConnect","Called");


        if (getActivity() != null) {
            if (mACLBinding.mapViewAclDeliveryAddress != null) {

                //   //Log.e("351","mACLBinding.mapViewDlDeliveryAddress != null");

                mACLBinding.mapViewAclDeliveryAddress.onResume(); // needed to get the map to display immediately

                try {
                    //  //Log.e("348","called");
                    MapsInitializer.initialize(getActivity().getApplicationContext());
                } catch (Exception e) {
                    // //Log.e("351",e.toString());
                    e.printStackTrace();
                }


                mACLBinding.mapViewAclDeliveryAddress.getMapAsync(mMap -> {
                    mGoogleMap = mMap;
                    // initialCameraPosition(mGoogleMap.getCameraPosition().target);

                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        // //Log.e("367","called");
                        askPermission();
                    } else {
                        // //Log.e("369","called");
                        mMap.setMyLocationEnabled(true);
                        loadLocationManagement();

                    }

                });


                if (mGoogleApiClient != null) {
                    if (!mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.connect();
                    }
                }

            } else {
                // //Log.e("391","mACLBinding.mapViewDlDeliveryAddress == null");
            }

        }


    }

    private void getDeviceLocation() {
        // //Log.e("392","getDeviceLocation()");
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        initialCameraPosition(task.getResult(), false);
                    } else {
                        //  //Log.e("405","task.isSuccessful() else");
                    }
                }
            });

        } catch (SecurityException e) {
            // //Log.e("411 excep ",e.toString());
            e.printStackTrace();
        } catch (Exception e) {
            // //Log.e("414 excep ",e.toString());
            e.printStackTrace();
        }


    }

    private void loadLocationManagement() {
        mGoogleMap.setOnMyLocationClickListener(this);
        mGoogleMap.setOnMyLocationButtonClickListener(this);
        getDeviceLocation();
    }

    private void toLoadCurrentLocation(Location location) {
        if (location != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16.00f));
        }
    }

    private void initialCameraPosition(Location location, Boolean isFromMyLocationBtnClickOnMap) {

        //Log.e("580","initialCameraPosition called");

        if (isFromMyLocationBtnClickOnMap) {

            //Here , initialCameraPosition method called by My location button clicked on google maps.
            //So to load current location only:-
            toLoadCurrentLocation(location);
            //Log.e("586","called");

        } else {


            if(AddressBookChangeLocationDB.getInstance(getActivity()).getSizeOfList() > 0){

                AreaGeoCodeDataSet mAddressBkDs = AddressBookChangeLocationDB.getInstance(getActivity()).getDetails();
                String mTEMP_LAT = mAddressBkDs.getmLatitude();
                String mTEMP_LNG = mAddressBkDs.getmLongitude();
                String mTEMP_ADDS = mAddressBkDs.getmAddress();

                if(mTEMP_LAT != null && mTEMP_LNG != null && !mTEMP_LAT.isEmpty() && !mTEMP_LNG.isEmpty()){
                    Latitude = Double.parseDouble(mTEMP_LAT);
                    Longitude = Double.parseDouble(mTEMP_LNG);

                    if(mTEMP_ADDS != null && !mTEMP_ADDS.isEmpty()){
                        mACLBinding.tvAclCurrentAddressData.setText(mTEMP_ADDS);
                    }
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Latitude,Longitude),
                            16.00f));
                }else {
                    toLoadCurrentLocation(location);
                }

            }else {

                toLoadCurrentLocation(location);

            }




        }


        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.setOnMarkerClickListener(marker -> false);
        mGoogleMap.setOnCameraMoveStartedListener(i -> {
            mACLBinding.pbAclAddressMap.setVisibility(View.VISIBLE);
            mIsMapLoaderLoading = true;
            // Clear all markers :-
            if (mGoogleMap != null) {
                mGoogleMap.clear();
            }

            mGoogleMap.setOnCameraIdleListener(() -> getAddress(mGoogleMap.getCameraPosition().target));

        });

        mGoogleMap.setOnMapClickListener(latLng -> {

        });

        // }

    }


    /*@Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }*/

    /*@Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }*/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        /*googleMap.setOnMapClickListener(latLng -> {
        });*/

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Thread(new Runnable() {
                public void run() {
                    googleMap.setOnMapClickListener(latLng -> {
                    });
                }
            }));
        }


    }

    private void getAddress(LatLng ll) {
        try {

            List<Address> addresses;
            addresses = geocoder.getFromLocation(ll.latitude, ll.longitude, 1);

            Latitude = ll.latitude;
            Longitude = ll.longitude;
            if (addresses.size() > 0) {

                String mAdss = addresses.get(0).getAddressLine(0);

                mACLBinding.tvAclCurrentAddressData.setText(mAdss);
                //Log.e("587 ", mAdss);
                current_address_string = mAdss;
                current_address_name_only_string = addresses.get(0).getFeatureName();




                mACLBinding.pbAclAddressMap.setVisibility(View.GONE);
                mIsMapLoaderLoading = false;

            } else {
                //Log.e("610 ", "Address.Size()  Not available");
                ////Log.e("", "Address.Size()  Not available");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.e("getAddress Excep ", e.toString());
            //  Toast.makeText(getContext(), "Exception "+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void askPermission() {


        if (getActivity() != null) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.please_allow_the_device_location_access)
                            .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        USER_LOCATION_PERMISSION_CODE);

                            })
                            .create()
                            .show();


                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            USER_LOCATION_PERMISSION_CODE);
                }
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        switch (requestCode) {
            case 41:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadRestaurantLocationMap();

                } else {
                    askPermission();
                }
                break;
        }


    }


    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        initialCameraPosition(location, true);
    }

    private List<Place.Field> getPlaceFields() {
        return fieldSelector.getAllFields();
    }


    private void startAutocompleteActivity() {

        if (getActivity() != null) {
            Intent autocompleteIntent =
                    new Autocomplete.IntentBuilder(getMode(), getPlaceFields())
                            .setInitialQuery(getQuery())
                            .setCountry(getCountry())
                            .setLocationBias(getLocationBias())
                            .setLocationRestriction(getLocationRestriction())
                            .setTypeFilter(getTypeFilter())
                            .build(getActivity());
            startActivityForResult(autocompleteIntent, AUTOCOMPLETE_REQUEST_CODE);

        }

    }


    private AutocompleteActivityMode getMode() {
        return AutocompleteActivityMode.FULLSCREEN;
    }

    private String getQuery() {
        return "";
    }

    private String getCountry() {
        return "";
    }

    @Nullable
    private LocationBias getLocationBias() {
        return getBounds(R.id.autocomplete_location_bias_south_west, R.id.autocomplete_location_bias_north_east);
    }

    @Nullable
    private RectangularBounds getBounds(int resIdSouthWest, int resIdNorthEast) {
        return null;
    }

    @Nullable
    private LocationRestriction getLocationRestriction() {
        return getBounds(R.id.autocomplete_location_restriction_south_west, R.id.autocomplete_location_restriction_north_east);
    }

    @Nullable
    private TypeFilter getTypeFilter() {
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {

            if (resultCode == AutocompleteActivity.RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                // Status status = Autocomplete.getStatusFromIntent(data);
                //Log.e("RESULT_OK", "called");

                // mAutoCompletePlaces.setText(place.getAddress());

                String[] mTempLatLngPlaces1 = place.getLatLng().toString().split(":");
                String mTempLatLngPlaces2 = mTempLatLngPlaces1[1].replace("(", "");
                String mTempLatLngPlaces3 = mTempLatLngPlaces2.replace(")", "");
                String[] mTempLatLngPlaces4 = mTempLatLngPlaces3.split(",");

                String Current_address = place.getAddress();
                String Current_address_name = place.getName();

                String mLatitude = mTempLatLngPlaces4[0];
                String mLongitude = mTempLatLngPlaces4[1];

                Latitude = Double.valueOf(mTempLatLngPlaces4[0]);
                Longitude = Double.valueOf(mTempLatLngPlaces4[1]);

                //Log.e("RESULT_OK Latitude", ""+Latitude);
                //Log.e("RESULT_OK Longitude", ""+Longitude);

                if (Current_address != null && Current_address_name != null && Latitude != null && Longitude != null) {

                    //Log.e("1072","called");

                    AreaGeoCodeDataSet mAreaDS = new AreaGeoCodeDataSet();
                    mAreaDS.setmAddress(Current_address);
                    mAreaDS.setmLatitude(String.valueOf(Latitude));
                    mAreaDS.setmLongitude(String.valueOf(Longitude));
                    mAreaDS.setmAddsNameOnly(Current_address_name);

                    if(AddressBookChangeLocationDB.getInstance(getActivity()).getSizeOfList() > 0){
                        AddressBookChangeLocationDB.getInstance(getActivity()).deleteDB();
                    }
                    AddressBookChangeLocationDB.getInstance(getActivity()).add(mAreaDS);

                    //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Latitude, Longitude), 16.00f));

                } else {

                    //Log.e("1078","called");

                }

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

                if(data != null){
                    Status status = Autocomplete.getStatusFromIntent(data);
                    //   responseView.setText(status.getStatusMessage());
                    if(status != null && status.getStatusMessage() != null){
                        //Log.e("RESULT_ERROR", status.getStatusMessage());
                        AppFunctions.toastShort(getActivity(), status.getStatusMessage());
                    }
                }


            } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
                Status status = Autocomplete.getStatusFromIntent(data);
                if(status != null && status.getStatusMessage() != null){
                    //Log.e("RESULT_CANCELED", status.getStatusMessage());
                    // The user canceled the operation.
                    AppFunctions.toastShort(getActivity(), status.getStatusMessage());
                }

            }


        }
    }


}