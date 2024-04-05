package com.ordenese.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
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
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.FieldSelector;
import com.ordenese.CustomClasses.MainContext;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.DeliveryLocationSearchDataSet;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.DeliveryLocationSearchDB;
import com.ordenese.Databases.RecentSearchedPlacesDB;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.R;
import com.ordenese.databinding.DeliveryLocationSearchBinding;

import java.util.ArrayList;
import java.util.List;

public class DeliveryLocationSearch extends Fragment implements View.OnClickListener, View.OnTouchListener {

    private DeliveryLocationSearchBinding mDLSearchBinding;
    private RecyclerView.LayoutManager mLayMgrRecentSearchAdds;
    private int USER_LOCATION_PERMISSION_CODE = 41;
    private FieldSelector mFieldSelector;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 23487;
    private Double Latitude, Longitude;
    private PlacesClient mPlacesClient;
    CartInfo cartInfo;


    public DeliveryLocationSearch() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // inflater.inflate(R.layout.delivery_location_search, container, false);

        mDLSearchBinding = DeliveryLocationSearchBinding.inflate(inflater, container, false);

        mDLSearchBinding.imgDlsBack.setOnClickListener(this);
        mDLSearchBinding.imgDlsCurrentLocationTitleIcon.setOnClickListener(this);
        mDLSearchBinding.tvDlsCurrentLocationTitle.setOnClickListener(this);
        mDLSearchBinding.layDlsSearchAddsContainer.setOnTouchListener(this);
        // lay_dls_search_adds_container



        if (getActivity() != null) {
            cartInfo = (CartInfo) getActivity();
        }

        if (getActivity() != null) {

            // Initialize Places.
            Places.initialize(getActivity(), /*apiKey*/getResources().getString(R.string.google_server_api_key));

            // Retrieve a PlacesClient (previously initialized - see MainActivity)
            mPlacesClient = Places.createClient(getActivity());

        }

        mFieldSelector = new FieldSelector(mDLSearchBinding.useCustomFields, mDLSearchBinding.customFieldsList);


        mDLSearchBinding.layDlsRecentlySearchedPlacesContainer.setVisibility(View.GONE);


        return mDLSearchBinding.getRoot();
    }

    @Override
    public void onClick(View view) {

        int mId = view.getId();
        if (mId == R.id.img_dls_back) {
            getParentFragmentManager().popBackStack();
        } else if (mId == R.id.img_dls_current_location_title_icon ||
                mId == R.id.tv_dls_current_location_title) {

            //To reset the previous selection if exists:-
            if (DeliveryLocationSearchDB.getInstance(getActivity()).getSizeOfList() > 0) {
                DeliveryLocationSearchDB.getInstance(getActivity()).deleteDB();
            }

            //Now  to add the current selection :-
            DeliveryLocationSearchDataSet dLSearchDs = new DeliveryLocationSearchDataSet();
            dLSearchDs.setmIsSearchedAddress(DefaultNames.no);
            dLSearchDs.setmIsCurrentLocation(DefaultNames.yes);
            dLSearchDs.setmSearchedAddressNameOnly("");
            dLSearchDs.setmSearchedAddressFull("");
            dLSearchDs.setmSearchedAddressLatitude("");
            dLSearchDs.setmSearchedAddressLongitude("");
            DeliveryLocationSearchDB.getInstance(getActivity()).add(dLSearchDs);

            getParentFragmentManager().popBackStack();

        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int mId = view.getId();
        if (mId == R.id.lay_dls_search_adds_container) {

            //  AppFunctions.toastLong(getActivity(),"search Touch");

            startAutocompleteActivity();

        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                CheckGpsConnection();
                showRecentlySearchedPlace();
            } else {

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

    private void showRecentlySearchedPlace() {

        ArrayList<AreaGeoCodeDataSet> mRecentSPList = RecentSearchedPlacesDB.getInstance(getActivity()).getRecentSearchedPlaces();
        if (mRecentSPList != null && mRecentSPList.size() > 0) {

            mDLSearchBinding.layDlsRecentlySearchedPlacesContainer.setVisibility(View.VISIBLE);

            mLayMgrRecentSearchAdds = new LinearLayoutManager(getActivity());
            mDLSearchBinding.recyclerDlsRecentlySearchedPlacesList.setLayoutManager(mLayMgrRecentSearchAdds);
            RecentSearchedPlacesAdapter mRecentSearchedPlacesAdapter = new RecentSearchedPlacesAdapter(mRecentSPList);
            mDLSearchBinding.recyclerDlsRecentlySearchedPlacesList.setAdapter(mRecentSearchedPlacesAdapter);


        } else {

            mDLSearchBinding.layDlsRecentlySearchedPlacesContainer.setVisibility(View.GONE);

        }

    }

    private void CheckGpsConnection() {
        if (getActivity() != null) {
            final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            assert manager != null;
            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                createLocationServiceError(getActivity());
            } else {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    askPermission();
                }
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
        AlertDialog alert = builder.create();
        alert.show();

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


    public class RecentSearchedPlacesAdapter extends RecyclerView.Adapter<RecentSearchedPlacesAdapter.DataObjectHolder> {

        private ArrayList<AreaGeoCodeDataSet> mRecentSPList;

        public RecentSearchedPlacesAdapter(ArrayList<AreaGeoCodeDataSet> recentSearchedPlaces) {
            this.mRecentSPList = recentSearchedPlaces;
        }

        @Override
        public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_searched_places_row, parent, false);
            return new DataObjectHolder(mView);
        }


        @Override
        public void onBindViewHolder(final DataObjectHolder holder, final int position) {


            holder.mRecentSearchedPlaceData.setText(mRecentSPList.get(position).getmAddress());

            holder.mRecentSearchedPlaceData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // AppFunctions.toastLong(getActivity(),holder.mAddressListData.getText().toString());

                    AreaGeoCodeDataSet mAreaGeoCodeDS = new AreaGeoCodeDataSet();
                    mAreaGeoCodeDS.setmAddress(mRecentSPList.get(position).getmAddress());
                    mAreaGeoCodeDS.setmLatitude(mRecentSPList.get(position).getmLatitude());
                    mAreaGeoCodeDS.setmLongitude(mRecentSPList.get(position).getmLongitude());
                    //   mAreaGeoCodeDS.setmLatitude("11.026830147707603");
                    //  mAreaGeoCodeDS.setmLongitude("76.90551660954952");
                    mAreaGeoCodeDS.setmAddsNameOnly(mRecentSPList.get(position).getmAddsNameOnly());


                    if (AreaGeoCodeDB.getInstance(getActivity()).isAreaGeoCodeSelected()) {
                        AreaGeoCodeDB.getInstance(getActivity()).updateUserAreaGeoCode(mAreaGeoCodeDS);
                    } else {
                        AreaGeoCodeDB.getInstance(getActivity()).addUserAreaGeoCode(mAreaGeoCodeDS);
                    }

                    //Here , DeliveryLocationSearchDB DB need is over when this address pressed.
                    //So delete the DB details if exist.
                    if (DeliveryLocationSearchDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        DeliveryLocationSearchDB.getInstance(getActivity()).deleteDB();
                    }

                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    ListHome m_listHome = new ListHome();
                    mFT.replace(R.id.layout_app_home_body, m_listHome, "m_listHome");
                    mFT.addToBackStack("m_listHome");
                    mFT.commit();



                }
            });


        }


        @Override
        public int getItemCount() {


            return this.mRecentSPList.size();

        }

        public class DataObjectHolder extends RecyclerView.ViewHolder {

            private TextView mRecentSearchedPlaceData;


            public DataObjectHolder(View view) {
                super(view);


                mRecentSearchedPlaceData = view.findViewById(R.id.tv_recent_searched_places_data);


            }
        }


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

    private List<Place.Field> getPlaceFields() {
        return mFieldSelector.getAllFields();
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

                if (Current_address != null && Latitude != null && Longitude != null) {

                    //To reset the previous selection if exists:-
                    if (DeliveryLocationSearchDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        DeliveryLocationSearchDB.getInstance(getActivity()).deleteDB();
                    }

                    //Now  to add the current selection :-
                    DeliveryLocationSearchDataSet dLSearchDs = new DeliveryLocationSearchDataSet();
                    dLSearchDs.setmIsSearchedAddress(DefaultNames.yes);
                    dLSearchDs.setmIsCurrentLocation(DefaultNames.no);
                    dLSearchDs.setmSearchedAddressNameOnly(Current_address_name);
                    dLSearchDs.setmSearchedAddressFull(Current_address);
                    dLSearchDs.setmSearchedAddressLatitude(String.valueOf(Latitude));
                    dLSearchDs.setmSearchedAddressLongitude(String.valueOf(Longitude));
                    DeliveryLocationSearchDB.getInstance(getActivity()).add(dLSearchDs);

                    getParentFragmentManager().popBackStack();

                } else {

                }

            }  else if (resultCode == AutocompleteActivity.RESULT_ERROR) {

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