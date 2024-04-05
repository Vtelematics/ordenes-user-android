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
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.FieldSelector;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.DeliveryLocationSearchDataSet;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.DeliveryLocationSearchDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.Databases.RecentSearchedPlacesDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.R;
import com.ordenese.databinding.DeliveryLocationBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryLocation extends Fragment implements View.OnClickListener, /*GoogleApiClient.ConnectionCallbacks,*/
        /*GoogleApiClient.OnConnectionFailedListener,*/ OnMapReadyCallback, GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener/*, View.OnTouchListener*/ {

    private DeliveryLocationBinding mDLBinding;
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
    CartInfo cartInfo;

    private boolean mIsAddressBookProcess = false;


    public DeliveryLocation() {
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
        //return inflater.inflate(R.layout.delivery_location, container, false);
        mDLBinding = DeliveryLocationBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        cartInfo = (CartInfo) getActivity();

        if (getActivity() != null) {
            mDLBinding.tvAddressTitle.setText(getActivity().getResources().getString(R.string.delivering_to));
            mDLBinding.btnDlDeliveryHere.setText(getActivity().getResources().getString(R.string.delivery_here));
        }


        mDLBinding.imgDlBack.setOnClickListener(this);
        mDLBinding.layDlSearchAddsContainer.setOnClickListener(this);
        // mDLBinding.layDlSearchAddsContainer.setOnTouchListener(this);
        mDLBinding.layDlDeliveryHere.setOnClickListener(this);
        mDLBinding.tvDlCurrentAddressData.setOnClickListener(this);

        if (getActivity() != null) {
            // Initialize Places.
            Places.initialize(getActivity(), /*apiKey*/getResources().getString(R.string.google_server_api_key));
            // Retrieve a PlacesClient (previously initialized - see MainActivity)
            placesClient = Places.createClient(getActivity());

        }

        mDLBinding.mapViewDlDeliveryAddress.onCreate(savedInstanceState);
        fieldSelector = new FieldSelector(mDLBinding.useCustomFields1, mDLBinding.customFieldsList1);
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        return mDLBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if (mId == R.id.img_dl_back) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        } else if (mId == R.id.lay_dl_search_adds_container) {

            //Log.e("221","called");

            if (getActivity() != null) {

                //To check is map loader loading while fetching location is under progressing.
                if (!mIsMapLoaderLoading) {
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    DeliveryLocationSearch m_DeliveryLocationSearch = new DeliveryLocationSearch();
                    mFT.replace(R.id.layout_app_home_body, m_DeliveryLocationSearch, "m_DeliveryLocationSearch");
                    mFT.addToBackStack("m_DeliveryLocationSearch");
                    mFT.commit();
                } else {
                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.caa_please_wait));
                }
            }
        }else if (mId == R.id.tv_dl_current_address_data) {

            //Log.e("221","called");

            if (getActivity() != null) {

                //To check is map loader loading while fetching location is under progressing.
                if (!mIsMapLoaderLoading) {
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    DeliveryLocationSearch m_DeliveryLocationSearch = new DeliveryLocationSearch();
                    mFT.replace(R.id.layout_app_home_body, m_DeliveryLocationSearch, "m_DeliveryLocationSearch");
                    mFT.addToBackStack("m_DeliveryLocationSearch");
                    mFT.commit();
                } else {
                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.caa_please_wait));
                }
            }
        } else if (mId == R.id.lay_dl_delivery_here) {
            //***********************************************************************************
            //To check is map loader loading while fetching location is under progressing.
            if (!mIsMapLoaderLoading) {

                //tv_dl_current_address_data
                if (!mDLBinding.tvDlCurrentAddressData.getText().toString().isEmpty()) {


                    //Its delivery location process :-
                    //***********    *********************   **********************  ****************

                    //To call the home modules api to check , Is delivery available for selected latlong
                    //or not.
                    //In this api , to show local msg for error.And if this api success then forward to
                    //ListHome page .

                    //For Currently Selected Address :-
                    AreaGeoCodeDataSet mAreaGeoCodeDS = new AreaGeoCodeDataSet();
                    mAreaGeoCodeDS.setmAddress(current_address_string);
                    mAreaGeoCodeDS.setmLatitude(String.valueOf(Latitude));
                    mAreaGeoCodeDS.setmLongitude(String.valueOf(Longitude));
                    //  mAreaGeoCodeDS.setmLatitude("11.026830147707603");
                    // mAreaGeoCodeDS.setmLongitude("76.90551660954952");
                    mAreaGeoCodeDS.setmAddsNameOnly(current_address_name_only_string);
                    //mAddToCartObject.put(DefaultNames.latitude,"11.026830147707603");
                    //mAddToCartObject.put(DefaultNames.longitude, "76.90551660954952");

                    Log.e("current_address_string", current_address_string);
                    Log.e("Latitude", String.valueOf(Latitude));
                    Log.e("Longitude", String.valueOf(Longitude));
                    Log.e("current_address_name_only_string", current_address_name_only_string);


                    callHomeModulesApiForDeliveryAvailableCheck(mAreaGeoCodeDS);

                    //***********    *********************   **********************  ****************


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
                //Its delivery location process :-
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }

        if (getActivity() != null) {
            cartInfo.cart_info(false, "", "");
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
            if (mDLBinding.mapViewDlDeliveryAddress != null) {

                //   //Log.e("351","mDLBinding.mapViewDlDeliveryAddress != null");

                mDLBinding.mapViewDlDeliveryAddress.onResume(); // needed to get the map to display immediately

                try {
                    //  //Log.e("348","called");
                    MapsInitializer.initialize(getActivity().getApplicationContext());
                } catch (Exception e) {
                    // //Log.e("351",e.toString());
                    e.printStackTrace();
                }


                mDLBinding.mapViewDlDeliveryAddress.getMapAsync(mMap -> {
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
                // //Log.e("391","mDLBinding.mapViewDlDeliveryAddress == null");
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

            //Log.e("589","called");

            //Here , initialCameraPosition method called by actual page process.So here to check AreaGeoCodeDB
            //has entry or not.If AreaGeoCodeDB has entry then process with that details or if empty then
            //load the current location.

            if (DeliveryLocationSearchDB.getInstance(getActivity()).getSizeOfList() > 0) {

                //Log.e("597","called");

                //If DeliveryLocationSearchDB this DB has entry means , the user visited DeliveryLocationSearch page
                //and procced with either current location option or google auto search function.

                DeliveryLocationSearchDataSet mDLSearch = DeliveryLocationSearchDB.getInstance(getActivity()).getData();
                if (mDLSearch.getmIsCurrentLocation().equals(DefaultNames.yes)) {
                    //   //Log.e("452","called");
                    toLoadCurrentLocation(location);
                } else if (mDLSearch.getmIsSearchedAddress().equals(DefaultNames.yes)) {
                    // //Log.e("455","called");
                    if ((mDLSearch.getmSearchedAddressLatitude() != null && mDLSearch.getmSearchedAddressLongitude() != null) &&
                            (!mDLSearch.getmSearchedAddressLatitude().isEmpty() && !mDLSearch.getmSearchedAddressLongitude().isEmpty())) {
                        //   //Log.e("458","called");

                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(mDLSearch.getmSearchedAddressLatitude()), Double.parseDouble(mDLSearch.getmSearchedAddressLongitude())),
                                16.00f));
                    } else {
                        //  //Log.e("463","called");
                        //Just for safe check ! .
                        toLoadCurrentLocation(location);
                    }

                } else {
                    //   //Log.e("469","called");
                    //Just for safe check ! .
                    toLoadCurrentLocation(location);
                }

            } else {

                //Log.e("628","called");


                ////If DeliveryLocationSearchDB this DB has no entry , then its means the user new visited DeliveryLocation
                //page or just visited DeliveryLocationSearch page and just be back to here without any address oriented function.
                AreaGeoCodeDataSet mAreaGeoCodeDS = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                if (AreaGeoCodeDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    // //Log.e("482","called");
                    if ((mAreaGeoCodeDS.getmLatitude() != null && mAreaGeoCodeDS.getmLongitude() != null) &&
                            (!mAreaGeoCodeDS.getmLatitude().isEmpty() && !mAreaGeoCodeDS.getmLongitude().isEmpty())) {
                        //  //Log.e("485","called");
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(mAreaGeoCodeDS.getmLatitude()), Double.parseDouble(mAreaGeoCodeDS.getmLongitude())), 16.00f));
                    } else {
                        //   //Log.e("488","called");
                        toLoadCurrentLocation(location);
                    }
                } else {
                    // //Log.e("490","called");
                    toLoadCurrentLocation(location);
                }

            }


        }


        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.setOnMarkerClickListener(marker -> false);
        mGoogleMap.setOnCameraMoveStartedListener(i -> {
            mDLBinding.pbDlAddressMap.setVisibility(View.VISIBLE);
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


        if (getActivity() != null) {
           /* getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {*/

            try {

                List<Address> addresses;

                addresses = geocoder.getFromLocation(ll.latitude, ll.longitude, 1);

                        /*DecimalFormat df = new DecimalFormat();
                        df.setMaximumFractionDigits(3);
                        double lat = Double.parseDouble(df.format(ll.latitude));
                        double lon = Double.parseDouble(df.format(ll.longitude));
                        addresses =geocoder.getFromLocation(lat,lon,1);*/

                Latitude = ll.latitude;
                Longitude = ll.longitude;
                if (addresses.size() > 0) {

                    String mAdss = addresses.get(0).getAddressLine(0);

                    mDLBinding.tvDlCurrentAddressData.setText(mAdss);
                    //Log.e("587 ", mAdss);
                    current_address_string = mAdss;
                    current_address_name_only_string = addresses.get(0).getFeatureName();






                /*if (current_address_string != null) {


                    if (current_address_string.isEmpty()) {
                        mDLBinding.tvDlCurrentAddressData.setText(mAdss);
                        //Log.e("589 ", mAdss);
                        // Toast.makeText(getContext(), "Current Address 1"+mAdss, Toast.LENGTH_SHORT).show();
                    } else {
                        //  Toast.makeText(getContext(), "Current Address Back String "+Current_address_back_string, Toast.LENGTH_SHORT).show();
                        mDLBinding.tvDlCurrentAddressData.setText(current_address_string);
                        //Log.e("594 ", current_address_string);

                    }
                } else {
                    //This cause accured when , current location selected in select area page :-
                    mDLBinding.tvDlCurrentAddressData.setText(mAdss);
                    //Log.e("600 ", mAdss);
                    current_address_string = mAdss;
                    current_address_name_only_string = addresses.get(0).getFeatureName();
                }*/


                    mDLBinding.pbDlAddressMap.setVisibility(View.GONE);
                    mIsMapLoaderLoading = false;

                } else {
                    //Log.e("610 ", "Address.Size()  Not available");
                    ////Log.e("", "Address.Size()  Not available");


                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("getAddress Excep ", e.toString());
                //  Toast.makeText(getContext(), "Exception "+e.toString(), Toast.LENGTH_SHORT).show();
            }
                /*}
            });*/
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

    private void toStoreTheRecentlySearchedPlace(AreaGeoCodeDataSet mAreaGeoCodeDS) {

        if (!RecentSearchedPlacesDB.getInstance(getActivity()).isSearchedPlaceExists(mAreaGeoCodeDS)) {
            //   ////Log.e("RSP", "Place Not exists.");
            //For that to avoid the repeated entry.
            if (RecentSearchedPlacesDB.getInstance(getActivity()).getSizeOfList() < 5) {

                RecentSearchedPlacesDB.getInstance(getActivity()).addRecentPlace(mAreaGeoCodeDS);
                //  ////Log.e("RSP", "getSizeOfList() < 5");

            } else {
                //   ////Log.e("RSP", "getSizeOfList() >= 5");

                ArrayList<AreaGeoCodeDataSet> mExistingList = RecentSearchedPlacesDB.getInstance(getActivity()).getRecentSearchedPlaces();

                for (int e = 1; e <= mExistingList.size(); e++) {


                    if (e == mExistingList.size()) {


                        AreaGeoCodeDataSet mAreaDS = new AreaGeoCodeDataSet();
                        mAreaDS.setmAddress(mAreaGeoCodeDS.getmAddress());
                        mAreaDS.setmLatitude(mAreaGeoCodeDS.getmLatitude());
                        mAreaDS.setmLongitude(mAreaGeoCodeDS.getmLongitude());

                        RecentSearchedPlacesDB.getInstance(getActivity()).addRecentPlace(mAreaDS);


                    } else {

                        if (e == 1) {

                            //For reset the RecentSearchedPlacesDB database list :-
                            RecentSearchedPlacesDB.getInstance(getActivity()).deleteRecentSearchedPlacesDB();

                        }


                        AreaGeoCodeDataSet mAreaDS = new AreaGeoCodeDataSet();
                        mAreaDS.setmAddress(mExistingList.get(e).getmAddress());
                        mAreaDS.setmLatitude(mExistingList.get(e).getmLatitude());
                        mAreaDS.setmLongitude(mExistingList.get(e).getmLongitude());

                        RecentSearchedPlacesDB.getInstance(getActivity()).addRecentPlace(mAreaDS);


                    }

                }


            }

        } /*else {
            ////Log.e("RSP", "Place Already exists.");
        }*/

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

    private void callHomeModulesApiForDeliveryAvailableCheck(AreaGeoCodeDataSet areaGeoCodeDataSet) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                    jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    if (OrderTypeDB.getInstance(getActivity()).getUserServiceType() != null) {
                        OrderTypeDB.getInstance(getActivity()).updateUserServiceType("1");
                    } else {
                        OrderTypeDB.getInstance(getActivity()).addUserServiceType("1");
                    }
                    jsonObject.put("order_type", OrderTypeDB.getInstance(getActivity()).getUserServiceType());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ApiResponseCheck> Call = retrofitInterface.homeModulesForDeliveryAvailableCheck(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<ApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponseCheck> call, @NonNull Response<ApiResponseCheck> response) {

                            mProgressDialog.cancel();

                            if (response.isSuccessful()) {
                                ApiResponseCheck mApiResponseCheck = response.body();
                                if (mApiResponseCheck != null) {
                                    if (mApiResponseCheck.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            // **********  ****************  ************  ***********

                                            if (AreaGeoCodeDB.getInstance(getActivity()).isAreaGeoCodeSelected()) {
                                                AreaGeoCodeDB.getInstance(getActivity()).updateUserAreaGeoCode(areaGeoCodeDataSet);
                                            } else {
                                                AreaGeoCodeDB.getInstance(getActivity()).addUserAreaGeoCode(areaGeoCodeDataSet);
                                            }

                                            AreaGeoCodeDB.getInstance(getActivity()).print();

                                            //To Add the current address with recent searched place list :-
                                            toStoreTheRecentlySearchedPlace(areaGeoCodeDataSet);

                                            //Here , DeliveryLocationSearchDB DB need is over when this button pressed.
                                            //So delete the DB details if exist.
                                            if (DeliveryLocationSearchDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                                DeliveryLocationSearchDB.getInstance(getActivity()).deleteDB();
                                            }

                                            //layout_home_restaurant_body

                                            if (!mIsAddressBookProcess) {
                                                //Its delivery location process :-
                                                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                                ListHome m_listHome = new ListHome();
                                                mFT.replace(R.id.layout_app_home_body, m_listHome, "m_listHome");
                                                mFT.addToBackStack("m_listHome");
                                                mFT.commit();
                                            }


                                            //*********** *************** ************* ********** ****
                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mApiResponseCheck.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", getResources().getString(R.string.service_not_available_for_this_location));
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
                        public void onFailure(@NonNull Call<ApiResponseCheck> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }
                    });

                } catch (JSONException e) {
                    mProgressDialog.cancel();
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


}