package com.ordenese.Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.DirectionsJSONParser;
import com.ordenese.Interfaces.CheckOutBackPress;
import com.ordenese.R;
import com.ordenese.databinding.FragmentTrackOrderBinding;

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
import java.util.Objects;

public class TrackOrder extends Fragment {

    FragmentTrackOrderBinding binding;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    CameraUpdate cu;
    LatLngBounds bounds;
    Bundle mSavedInstanceState;
    private String mLatitude = "", mLongitude = "", mCheckOutAddsLatitude = "", mCheckOutAddsLongitude = "", driver_id = "",
            delivery_time = "", mWithin = "", mMins = "";
    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};
    LatLng current;
    private Marker mMarker;
    final int curMapTypeIndex = 1;
    Activity activity;
    CheckOutBackPress mCheckOutBackPress;

    Handler handler_loc;
    Runnable runnable_loc;

    public TrackOrder() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLatitude = getArguments().getString("mLatitude");
            mLongitude = getArguments().getString("mLongitude");
            mCheckOutAddsLatitude = getArguments().getString("mCheckOutAddsLatitude");
            mCheckOutAddsLongitude = getArguments().getString("mCheckOutAddsLongitude");
            driver_id = getArguments().getString("driver_id");
            delivery_time = getArguments().getString("delivery_time");
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
        this.activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTrackOrderBinding.inflate(inflater, container, false);

        mCheckOutBackPress = (CheckOutBackPress) getActivity();

        //google maps for delivery location :-
        mSavedInstanceState = savedInstanceState;
        binding.mapViewOrderConfirmTrackOrder.onCreate(mSavedInstanceState);

        binding.imgOrderConfirmBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        String mWithin = activity.getResources().getString(R.string.within);
        String mMins = activity.getResources().getString(R.string.mins);
        binding.tvOcDeliveryTimeMsg.setText(activity.getResources().getString(R.string.oc_estimated_delivery_time));
        binding.tvOcDeliveryTime.setText(mWithin + " " + delivery_time + " " + mMins);

        loadRestaurantLocationMap();
        //Google map initializer and connection :-
        restaurantLocationMapInitializerAndConnect(binding.mapViewOrderConfirmTrackOrder);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCheckOutBackPress.checkOutSuccessStatus(false);
    }

    private void loadRestaurantLocationMap() {
        mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .build();

//            // Create the LocationRequest object
//            mLocationRequest = LocationRequest.create()
//                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                    .setInterval(5000)        // 5 seconds, in milliseconds
//                    .setFastestInterval(5000); // 5 second, in milliseconds

    }

    private void restaurantLocationMapInitializerAndConnect(MapView mapView) {

        if (mapView != null) {
            mapView.onResume(); // needed to get the map to display immediately
            try {
                MapsInitializer.initialize(activity.getApplicationContext());
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

                LatLng pickup = new LatLng(m__Latitude, m__Longitude);
                LatLng delivery = new LatLng(m__CheckoutAddsLatitude, m__CheckoutAddsLongitude);

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickup, 16.00f));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(delivery));

                mGoogleMap.setMapType(MAP_TYPES[curMapTypeIndex]);
                mGoogleMap.setTrafficEnabled(true);
                mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

                //Vendor location :-
                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.vendor_icon);
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(m__Latitude, m__Longitude)).icon(icon));
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                //delivery address location :-
                BitmapDescriptor icon_1 = BitmapDescriptorFactory.fromResource(R.drawable.driver_icon);
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(m__CheckoutAddsLatitude, m__CheckoutAddsLongitude)).icon(icon_1));
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                Query myDeliveryPostsQuery = mDatabase.child("drivers").child(driver_id);
                myDeliveryPostsQuery.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        double m__latitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("latitude").getValue(String.class)));
                        double m__longitude = Double.parseDouble(Objects.requireNonNull(snapshot.child("longitude").getValue(String.class)));
                        current = new LatLng(m__latitude, m__longitude);

                        if (current != null) {
                            if (mMarker != null)
                                mMarker.remove();
                            //driver address location :-
                            mMarker = mGoogleMap.addMarker(new MarkerOptions()
                                    .position(current)
                                    .icon(bitmapDescriptorFromVector(activity, R.drawable.driver_green))
                            );

                            if (mMarker != null) {
                                mMarker.setPosition(current);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                bounds = new LatLngBounds.Builder()
                        .include(pickup)
                        .include(delivery)
                        .build();
                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                mGoogleMap.moveCamera(cu);
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

                DownloadTask downloadTask = new DownloadTask();
                // Getting URL to the Google Directions API
                String Direction_url = getDirectionsUrl(pickup, delivery);
                // Start downloading json data from Google Directions API
                downloadTask.execute(Direction_url);

                handler_loc = new Handler();
                runnable_loc = new Runnable() {
                    public void run() {
                        GET_ETA_Task downloadTask1 = new GET_ETA_Task();
                        // Getting URL to the Google Directions API
                        String Direction_url1 = getDirectionsUrl(pickup, delivery);
                        // Start downloading json data from Google Directions API
                        downloadTask1.execute(Direction_url1);
                        handler_loc.postDelayed(runnable_loc, 20000);
                    }
                };
                handler_loc.postDelayed(runnable_loc, 1000);

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

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        assert vectorDrawable != null;
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
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

    private class GET_ETA_Task extends AsyncTask<String, Void, String> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
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
            try {
                JSONObject mResponseObject = new JSONObject(s);
                String distance = mResponseObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getString("text");
                String duration = mResponseObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getString("text");

                binding.tvOcDeliveryTime.setText(mWithin + " " + duration + " " + mMins);

                //                Log.e("onPostExecute: ", duration + "");
            } catch (Exception e) {
                Log.e("onPostExecute: ", e.getMessage() + "");
                e.printStackTrace();
            }
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

}