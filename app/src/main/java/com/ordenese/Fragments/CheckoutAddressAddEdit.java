package com.ordenese.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
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
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.ordenese.Activities.AppLogin;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.FieldSelector;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.AddressGeocodeDataSet;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.CountryCodeApi;
import com.ordenese.DataSets.LoginCountryCodeDataSet;
import com.ordenese.DataSets.VendorZoneApi;
import com.ordenese.DataSets.VendorZoneDetailsDataSet;
import com.ordenese.Databases.AddressBookChangeLocationDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.RestaurantAddressDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CountryCodeSelection;
import com.ordenese.R;
import com.ordenese.databinding.CheckoutAddressAddEditBinding;

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

public class CheckoutAddressAddEdit extends Fragment implements View.OnClickListener, CountryCodeSelection,
        OnMapReadyCallback, GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener {

    private CheckoutAddressAddEditBinding mCAAddEBinding;
    private String mAddressAddOrEdit = "";

    private String m__firstName = "";
    private String m__lastName = "";
    private String m__Area = "";
    private String m__Area_id_or_zone_id = "";
    private String m__block = "";
    private String m__street = "";
    private String m__building = "";
    private String m__way = "";
    private String m__floor = "";
    private String m__door_no = "";
    private String m__mobile = "";
    private String m__latitude = "";
    private String m__longitude = "";
    private String m__address_id = "";
    private String m__landline = "";
    private String m__address_type = "";
    private String m__additional_direction = "";

    private String mFirst__Name = "", mLast__Name = "", mMobile__No = "",m__email="",
            mCountry__Code = "", mLand_Line = "", mBloc__k = "",
            mStree_t = "", mBuildin_g = "", mFloo__r = "", mApartment_No = "",
            mAdditional_Direction = "", mAre__a = "",mAre__a_id_or_zone_id = "";

    private RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;

    private CountryCodeApi mCountryCodeApi;
    private String[] mAddressTypeList;
    private String mSelectedAddressType_Id = "", mSelectedAddressType_Name = "",mSelectedArea_Name = "", mSelectedArea_Id_ZoneId = "";


    private PlacesClient placesClient;
    private FieldSelector fieldSelector;
    private Geocoder geocoder;
    static AlertDialog alertDialog;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationListener mLocationListener;
    private GoogleMap mGoogleMap;
    private Boolean mIsMapLoaderLoading = true;
    private Double Latitude, Longitude;
    private int USER_LOCATION_PERMISSION_CODE = 41;
    private String Current_address_back_string,
            current_address_string, current_address_name_only_string;

    private VendorZoneApi mVendorZoneApi;

    private String mIsAddsBookCallFrom = "";
    private String mVendor_id = "";

    public CheckoutAddressAddEdit() {
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
        //  return inflater.inflate(R.layout.checkout_address_add_edit, container, false);

        mCAAddEBinding = CheckoutAddressAddEditBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        if (getActivity() != null) {
            // Initialize Places.
            Places.initialize(getActivity(), /*apiKey*/getResources().getString(R.string.google_server_api_key));
            // Retrieve a PlacesClient (previously initialized - see MainActivity)
            placesClient = Places.createClient(getActivity());
            mCAAddEBinding.mapViewCaaAddressLocation.onCreate(savedInstanceState);
            fieldSelector = new FieldSelector(mCAAddEBinding.useCustomFields1, mCAAddEBinding.customFieldsList1);
            geocoder = new Geocoder(getActivity(), Locale.getDefault());
        }

        mCAAddEBinding.imgCaaBack.setOnClickListener(this);
        mCAAddEBinding.layCaaSaveAndContinueBtnContainer.setOnClickListener(this);
        mCAAddEBinding.layCaaCountryCodeContainer.setOnClickListener(this);
        mCAAddEBinding.tvCaaChangeAddress.setOnClickListener(this);
        //


        if (getActivity() != null) {
            if (getArguments() != null) {

                mIsAddsBookCallFrom = getArguments().getString(DefaultNames.addressBook_callFrom);
                mVendor_id = getArguments().getString("Vendor_id");

                mAddressAddOrEdit = getArguments().getString(DefaultNames.address_add_or_edit);
                if (mAddressAddOrEdit.equals(DefaultNames.address_add_process)) {
                    mCAAddEBinding.tvCaaPageTitle.setText(getActivity().getResources().getString(R.string.co_al_new_address));

                    String mFirstName = UserDetailsDB.getInstance(getActivity()).getUserDetails().getFirstName();
                    String mLastName = UserDetailsDB.getInstance(getActivity()).getUserDetails().getLastName();
                    String mEmail = UserDetailsDB.getInstance(getActivity()).getUserDetails().getEmail();
                    String mMobile = UserDetailsDB.getInstance(getActivity()).getUserDetails().getTelephone();
                    String mMobileCode = UserDetailsDB.getInstance(getActivity()).getUserDetails().getMobileCountryCode();

                    if (mFirstName != null && !mFirstName.isEmpty()) {
                        mCAAddEBinding.etCaaFirstName.setText(mFirstName);
                    } else {
                        mCAAddEBinding.etCaaFirstName.setText("");
                    }

                    if (mLastName != null && !mLastName.isEmpty()) {
                        mCAAddEBinding.etCaaLastName.setText(mLastName);
                    } else {
                        mCAAddEBinding.etCaaLastName.setText("");
                    }

                    if (mMobile != null && !mMobile.isEmpty()) {
                        mCAAddEBinding.etCaaMobile.setText(mMobile);
                    } else {
                        mCAAddEBinding.etCaaMobile.setText("");
                    }

                    if (mMobileCode != null && !mMobileCode.isEmpty()) {
                        mCountry__Code = mMobileCode;
                    } else {
                        mCountry__Code = mMobileCode;
                    }

                    if (mEmail != null && !mEmail.isEmpty()) {
                        m__email = mEmail;
                        mCAAddEBinding.etCaaEmail.setText(mEmail);
                    } else {
                        m__email = mEmail;
                        mCAAddEBinding.etCaaEmail.setText("");
                    }




                } else {

                    mCAAddEBinding.tvCaaPageTitle.setText(getActivity().getResources().getString(R.string.co_al_edit_address));

                    m__firstName = getArguments().getString(DefaultNames.firstName);
                    m__lastName = getArguments().getString(DefaultNames.lastName);
                    m__Area = getArguments().getString(DefaultNames.area);
                    m__Area_id_or_zone_id = getArguments().getString(DefaultNames.zone_id);
                    m__block = getArguments().getString(DefaultNames.block);
                    m__street = getArguments().getString(DefaultNames.street);
                    m__building = getArguments().getString(DefaultNames.building);
                    m__way = getArguments().getString(DefaultNames.way);
                    m__floor = getArguments().getString(DefaultNames.floor);
                    m__door_no = getArguments().getString(DefaultNames.door_no);
                    mCountry__Code = getArguments().getString(DefaultNames.country_code);
                    m__mobile = getArguments().getString(DefaultNames.mobile);

                    m__email = getArguments().getString(DefaultNames.email);

                    m__latitude = getArguments().getString(DefaultNames.latitude);
                    m__longitude = getArguments().getString(DefaultNames.longitude);
                    m__address_id = getArguments().getString(DefaultNames.address_id);
                    m__landline = getArguments().getString(DefaultNames.landline);
                    m__address_type = getArguments().getString(DefaultNames.address_type);
                    m__additional_direction = getArguments().getString(DefaultNames.additional_direction);

                    // != null && Longitude

                    Log.e("208 m__address_id", "" + m__address_id);

                    if (m__latitude != null && m__longitude != null && !m__latitude.isEmpty() && !m__longitude.isEmpty()) {
                        Latitude = Double.parseDouble(m__latitude);
                        Longitude = Double.parseDouble(m__longitude);
                        Log.e("211 Latitude", "" + Latitude);
                        Log.e("212 Longitude", "" + Longitude);
                    }
                    Log.e("216 m__address_type", "" + m__address_type);


                    if (m__firstName != null && !m__firstName.isEmpty()) {
                        mCAAddEBinding.etCaaFirstName.setText(m__firstName);
                    } else {
                        mCAAddEBinding.etCaaFirstName.setText("");
                    }

                    if (m__lastName != null && !m__lastName.isEmpty()) {
                        mCAAddEBinding.etCaaLastName.setText(m__lastName);
                    } else {
                        mCAAddEBinding.etCaaLastName.setText("");
                    }

                    if (m__mobile != null && !m__mobile.isEmpty()) {
                        mCAAddEBinding.etCaaMobile.setText(m__mobile);
                    } else {
                        mCAAddEBinding.etCaaMobile.setText("");
                    }

                    if (m__email != null && !m__email.isEmpty()) {
                        mCAAddEBinding.etCaaEmail.setText(m__email);
                    } else {
                        mCAAddEBinding.etCaaEmail.setText("");
                    }

                    if (m__landline != null && !m__landline.isEmpty()) {
                        mCAAddEBinding.etCaaLandlineNo.setText(m__landline);
                    } else {
                        mCAAddEBinding.etCaaLandlineNo.setText("");
                    }

                    if (m__block != null && !m__block.isEmpty()) {
                        mCAAddEBinding.etCaaBlock.setText(m__block);
                    } else {
                        mCAAddEBinding.etCaaBlock.setText("");
                    }

                    if (m__street != null && !m__street.isEmpty()) {
                        mCAAddEBinding.etCaaStreet.setText(m__street);
                    } else {
                        mCAAddEBinding.etCaaStreet.setText("");
                    }

                    if (m__building != null && !m__building.isEmpty()) {
                        mCAAddEBinding.etCaaBuilding.setText(m__building);
                    } else {
                        mCAAddEBinding.etCaaBuilding.setText("");
                    }

                    if (m__floor != null && !m__floor.isEmpty() && !m__floor.equals("0")) {
                        mCAAddEBinding.etCaaFloor.setText(m__floor);
                    } else {
                        mCAAddEBinding.etCaaFloor.setText("");
                    }

                    if (m__door_no != null && !m__door_no.isEmpty() && !m__door_no.equals("0")) {
                        mCAAddEBinding.etCaaApartmentNo.setText(m__door_no);
                    } else {
                        mCAAddEBinding.etCaaApartmentNo.setText("");
                    }

                    if (m__additional_direction != null && !m__additional_direction.isEmpty()) {
                        mCAAddEBinding.etCaaAdditionalDirection.setText(m__additional_direction);
                    } else {
                        mCAAddEBinding.etCaaAdditionalDirection.setText("");
                    }
                    if (m__Area != null && !m__Area.isEmpty()) {
                        mCAAddEBinding.etCaaAddress.setText(m__Area);
                    } else {
                        mCAAddEBinding.etCaaAddress.setText("");
                    }

                    //m__Area - to load map first then it will fill area.


                }
            }
        }

        callCountryCodeListAPi();
        callAddressTypeSpinner();


        return mCAAddEBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();
        if (mId == R.id.img_caa_back) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        } else if (mId == R.id.lay_caa_save_and_continue_btn_container) {

            Log.e("1","");
            //To check is map loader loading while fetching location is under progressing.
            if (!mIsMapLoaderLoading) {

                Log.e("2","");
                mFirst__Name = mCAAddEBinding.etCaaFirstName.getText().toString();
                mLast__Name = mCAAddEBinding.etCaaLastName.getText().toString();
                mMobile__No = mCAAddEBinding.etCaaMobile.getText().toString();

                m__email = mCAAddEBinding.etCaaEmail.getText().toString();

                mCountry__Code = mCAAddEBinding.tvCaaCountryCode.getText().toString();
                mLand_Line = mCAAddEBinding.etCaaLandlineNo.getText().toString();
                mBloc__k = mCAAddEBinding.etCaaBlock.getText().toString();
                mStree_t = mCAAddEBinding.etCaaStreet.getText().toString();
                mBuildin_g = mCAAddEBinding.etCaaBuilding.getText().toString();
                mFloo__r = mCAAddEBinding.etCaaFloor.getText().toString();
                //where mApartment_No is a door_no in api.
                mApartment_No = mCAAddEBinding.etCaaApartmentNo.getText().toString();
                mAdditional_Direction = mCAAddEBinding.etCaaAdditionalDirection.getText().toString();
                mAre__a = mSelectedArea_Name;
                mAre__a_id_or_zone_id = mSelectedArea_Id_ZoneId;

               /* if (!mFirst__Name.isEmpty() && !mLast__Name.isEmpty() && !mCountry__Code.isEmpty() && !mMobile__No.isEmpty() &&
                        !mAre__a.isEmpty() && (mSelectedAddressType_Id != null && !mSelectedAddressType_Id.isEmpty()) &&
                        !mAre__a_id_or_zone_id.isEmpty() &&
                        !mStree_t.isEmpty() && !mBuildin_g.isEmpty() && !mFloo__r.isEmpty() && !mApartment_No.isEmpty()) {

                }*/

                //mSelectedAddressType_Id = 1 : house, mSelectedAddressType_Id = 2 : Apartment,
                //mSelectedAddressType_Id = 3 : Office


                if (mSelectedAddressType_Id.equals("1")) {
                    //Its a house address type selection :-
                    Log.e("3","");
                    if (!mFirst__Name.isEmpty() && !mLast__Name.isEmpty() && !mCountry__Code.isEmpty() && !mMobile__No.isEmpty() &&
                            !m__email.isEmpty() && AppFunctions.emailFormatValidation(m__email) &&
                             /*!mAre__a.isEmpty() &&!mAre__a_id_or_zone_id.isEmpty()&& */ (mSelectedAddressType_Id != null && !mSelectedAddressType_Id.isEmpty()) &&
                            !mStree_t.isEmpty() && !mBuildin_g.isEmpty()) {
                        Log.e("4","");
                        toCallAddEditApi();

                    } else {
                        if (getActivity().getResources() != null) {
                            if (mFirst__Name.isEmpty()) {
                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_first_name));
                            } else {
                                if (mLast__Name.isEmpty()) {
                                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_last_name));
                                } else {
                                    if (mCountry__Code.isEmpty()) {
                                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_select_country_code));
                                    } else {
                                        if (mMobile__No.isEmpty()) {
                                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_mobile_no));
                                        } else {
                                            if (m__email.isEmpty()) {
                                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_email));
                                            } else {
                                                if (!AppFunctions.emailFormatValidation(m__email)) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.enter_valid_email_id));
                                                } else {
//                                                    if (mAre__a.isEmpty() && mAre__a_id_or_zone_id.isEmpty()) {
//                                                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_select_the_area));
//                                                    } else {
                                                        if ((mSelectedAddressType_Id == null || mSelectedAddressType_Id.isEmpty())) {
                                                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_select_the_address_type));
                                                        } else {
                                                            if (mStree_t.isEmpty()) {
                                                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_street));
                                                            } else {

                                                                if (mBuildin_g.isEmpty()) {
                                                                    //For "House" address type, the building is renamed as house.
                                                                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_house));
                                                                }

                                                            }
                                                        }

//                                                    }


                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                } else if (mSelectedAddressType_Id.equals("2")) {
                    //Its a apartment address type selection :-

                    if (!mFirst__Name.isEmpty() && !mLast__Name.isEmpty() && !mCountry__Code.isEmpty() && !mMobile__No.isEmpty() &&
                            !m__email.isEmpty() && AppFunctions.emailFormatValidation(m__email) &&
                            /*!mAre__a.isEmpty() &&!mAre__a_id_or_zone_id.isEmpty()&& */  (mSelectedAddressType_Id != null && !mSelectedAddressType_Id.isEmpty()) &&
                            !mStree_t.isEmpty() && !mBuildin_g.isEmpty() && !mFloo__r.isEmpty() && !mApartment_No.isEmpty()) {

                        Log.e("5","");
                        toCallAddEditApi();

                    } else {

                        if (getActivity().getResources() != null) {
                            if (mFirst__Name.isEmpty()) {
                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_first_name));
                            } else {
                                if (mLast__Name.isEmpty()) {
                                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_last_name));
                                } else {
                                    if (mCountry__Code.isEmpty()) {
                                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_select_country_code));
                                    } else {
                                        if (mMobile__No.isEmpty()) {
                                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_mobile_no));
                                        } else {

                                            if (m__email.isEmpty()) {
                                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_email));
                                            } else {
                                                if (!AppFunctions.emailFormatValidation(m__email)) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.enter_valid_email_id));
                                                } else {
//                                                    if (mAre__a.isEmpty() && mAre__a_id_or_zone_id.isEmpty()) {
//                                                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_select_the_area));
//                                                    } else {
                                                        if ((mSelectedAddressType_Id == null || mSelectedAddressType_Id.isEmpty())) {
                                                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_select_the_address_type));
                                                        } else {
                                                            if (mStree_t.isEmpty()) {
                                                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_street));
                                                            } else {
                                                                if (mBuildin_g.isEmpty()) {
                                                                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_building));
                                                                } else {
                                                                    if (mFloo__r.isEmpty()) {
                                                                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_floor));
                                                                    } else {
                                                                        if (mApartment_No.isEmpty()) {
                                                                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_apartment_no));
                                                                        }

                                                                    }

                                                                }

                                                            }
                                                        }

//                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }

                } else {
                    //Its a office address type selection :-

                    if (!mFirst__Name.isEmpty() && !mLast__Name.isEmpty() && !mCountry__Code.isEmpty() && !mMobile__No.isEmpty() &&
                            !m__email.isEmpty() && AppFunctions.emailFormatValidation(m__email) &&
//                            !mAre__a.isEmpty() && !mAre__a_id_or_zone_id.isEmpty() && (mSelectedAddressType_Id != null && !mSelectedAddressType_Id.isEmpty()) &&
                            !mStree_t.isEmpty() && !mBuildin_g.isEmpty() && !mFloo__r.isEmpty() && !mApartment_No.isEmpty()) {
                        Log.e("6","");
                        toCallAddEditApi();

                    } else {

                        if (getActivity().getResources() != null) {
                            if (mFirst__Name.isEmpty()) {
                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_first_name));
                            } else {
                                if (mLast__Name.isEmpty()) {
                                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_last_name));
                                } else {
                                    if (mCountry__Code.isEmpty()) {
                                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_select_country_code));
                                    } else {
                                        if (mMobile__No.isEmpty()) {
                                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_mobile_no));
                                        } else {

                                            if (m__email.isEmpty()) {
                                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_email));
                                            } else {
                                                if (!AppFunctions.emailFormatValidation(m__email)) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.enter_valid_email_id));
                                                } else {
//                                                    if (mAre__a.isEmpty() && mAre__a_id_or_zone_id.isEmpty()) {
//                                                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_select_the_area));
//                                                    } else {
                                                        if ((mSelectedAddressType_Id == null || mSelectedAddressType_Id.isEmpty())) {
                                                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_select_the_address_type));
                                                        } else {
                                                            if (mStree_t.isEmpty()) {
                                                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_street));
                                                            } else {
                                                                if (mBuildin_g.isEmpty()) {
                                                                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_building));
                                                                } else {
                                                                    if (mFloo__r.isEmpty()) {
                                                                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_floor));
                                                                    } else {
                                                                        if (mApartment_No.isEmpty()) {
                                                                            //For "Office" address type, the apartment no is renamed as office.
                                                                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_office));
                                                                        }

                                                                    }

                                                                }

                                                            }
                                                        }

//                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        }

                    }

                }


            } else {
                if (getActivity() != null) {

                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.caa_please_wait));
                }
            }


        } else if (mId == R.id.tv_caa_change_address) {
            // AppFunctions.msgDialogOk(getActivity(),"","change address ");

            //To check is map loader loading while fetching location is under progressing.
            if (!mIsMapLoaderLoading) {

                if (getActivity() != null) {
                    if (mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()) {
                        AreaGeoCodeDataSet mAreaDS = new AreaGeoCodeDataSet();
                        mAreaDS.setmAddress(current_address_string);
                        mAreaDS.setmLatitude(String.valueOf(Latitude));
                        mAreaDS.setmLongitude(String.valueOf(Longitude));
                        mAreaDS.setmAddsNameOnly(current_address_name_only_string);
                        if (AddressBookChangeLocationDB.getInstance(getActivity()).getSizeOfList() > 0) {
                            AddressBookChangeLocationDB.getInstance(getActivity()).deleteDB();
                        }
                        AddressBookChangeLocationDB.getInstance(getActivity()).add(mAreaDS);
                        FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                        AddressChangeLocation m_addressChangeLocation = new AddressChangeLocation();
                        Bundle mBundle = new Bundle();
                        if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                            //Its for - checkout page address selection
                            mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_CheckOutAddsBook);
                            m_addressChangeLocation.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_check_out_body, m_addressChangeLocation, "m_addressChangeLocation");
                            mFT.addToBackStack("m_addressChangeLocation");
                            mFT.commit();
                        } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
                            //Its - my account page address book call.
                            mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_MyAccountAddsBook);
                            m_addressChangeLocation.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_home_body, m_addressChangeLocation, "m_addressChangeLocation");
                            mFT.addToBackStack("m_addressChangeLocation");
                            mFT.commit();
                        }
                    }
                }

            } else {
                if (getActivity() != null) {
                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.caa_please_wait));
                }
            }


        } else if (mId == R.id.lay_caa_country_code_container) {

            if (getActivity() != null) {

                if (mCountryCodeApi != null) {
                    //Log.e("mCountryCodeApi", "not null");
                    if (mCountryCodeApi.success != null) {
                        //Api response successDataSet :-
                        if (getActivity() != null) {

                            if (mCountryCodeApi.countryList != null && mCountryCodeApi.countryList.size() > 0) {
                                DialogueCountryCodeList mDCountryCodeList = new DialogueCountryCodeList().newInstance(mCountryCodeApi.countryList);
                                mDCountryCodeList.setTargetFragment(CheckoutAddressAddEdit.this, 7842);
                                mDCountryCodeList.show(getParentFragmentManager(), "mDCountryCodeList");
                            }
                        }
                    }
                }

            }


        }


    }

    private void toCallAddEditApi() {
        if (mAddressAddOrEdit.equals(DefaultNames.address_add_process)) {
            //add address process :-

            Log.e("mAddressAddOrEdit,"+mAddressAddOrEdit,"");
            callAddAddressAPi();
        } else {
            //edit address process :-
            Log.e("2mAddressAddOrEdit,"+mAddressAddOrEdit,"");
            callEditAddressAPi(m__address_id);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                CheckGpsConnection();
            } else {

                if(mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()){
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                        //Its for - checkout page address selection
                        mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
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

    private void callAddAddressAPi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {

                    JSONObject jsonObject = new JSONObject();
                    try {

                        jsonObject.put(DefaultNames.first_name, mFirst__Name);
                        jsonObject.put(DefaultNames.last_name, mLast__Name);
                        jsonObject.put(DefaultNames.country_code, mCountry__Code);
                        jsonObject.put(DefaultNames.mobile, mMobile__No);
                        jsonObject.put(DefaultNames.email, m__email);

                        jsonObject.put(DefaultNames.landline, mLand_Line);
                        jsonObject.put(DefaultNames.area, mAre__a);
                        jsonObject.put(DefaultNames.zone_id, mSelectedArea_Id_ZoneId);
                        jsonObject.put(DefaultNames.address_type, mSelectedAddressType_Id);
                        jsonObject.put(DefaultNames.block, mBloc__k);
                        jsonObject.put(DefaultNames.street, mStree_t);
                        jsonObject.put(DefaultNames.way, "");
                        jsonObject.put(DefaultNames.building_name, mBuildin_g);
                        jsonObject.put(DefaultNames.latitude, String.valueOf(Latitude));
                        jsonObject.put(DefaultNames.longitude, String.valueOf(Longitude));
                        jsonObject.put(DefaultNames.additional_direction, mAdditional_Direction);
                        jsonObject.put("address",mCAAddEBinding.etCaaAddress.getText().toString());
                        jsonObject.put("Vendor_id", mVendor_id);


                        if (mSelectedAddressType_Id.equals("1")) {
                            //Its a house address type selection :-
                            //To send floor and door_no as zero :-
                            jsonObject.put(DefaultNames.floor, "0");
                            jsonObject.put(DefaultNames.door_no, "0");
                        } else if (mSelectedAddressType_Id.equals("2")) {
                            //Its a apartment address type selection :-
                            jsonObject.put(DefaultNames.floor, mFloo__r);
                            jsonObject.put(DefaultNames.door_no, mApartment_No);
                        } else {
                            //Its a office address type selection :-
                            jsonObject.put(DefaultNames.floor, mFloo__r);
                            jsonObject.put(DefaultNames.door_no, mApartment_No);
                        }
                        jsonObject.put(DefaultNames.guest_status, "0"); //default guest_status = 0 for logged in user.
                        jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                        jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                        String mCustomerAuthorization = "";
                        if (AppFunctions.isUserLoggedIn(getActivity())) {
                            mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                        }
                        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                        retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                        Call<ApiResponseCheck> Call = retrofitInterface.addAddressApi(mCustomerAuthorization, body);
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
                                                // AppFunctions.msgDialogOk(getActivity(),"",getActivity().getString(R.string.otp_sent_to_your_mobile_number));
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder
                                                        .setMessage(mApiResponseCheck.success.message)
                                                        .setCancelable(false)
                                                        .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        getParentFragmentManager().popBackStack();
                                                                        dialog.dismiss();
                                                                    }
                                                                }
                                                        );

                                                AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.show();
                                            }


                                        } else {
                                            //Api response failure :-
                                            if (getActivity() != null) {
                                                if (mApiResponseCheck.error != null) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", mApiResponseCheck.error.message);
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
                            public void onFailure(@NonNull Call<ApiResponseCheck> call, @NonNull Throwable t) {
                                mProgressDialog.cancel();
                            }
                        });


                    } catch (JSONException e) {
                        mProgressDialog.cancel();

                        //Log.e("210 Excep ", e.toString());
                        e.printStackTrace();

                    }

                } else {

                    AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                    Intent intent = new Intent(getActivity(), AppLogin.class);
                    getActivity().startActivity(intent);

                }

            } else {
                if(mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()){
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                        //Its for - checkout page address selection
                        mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
                        //Its - my account page address book call.
                        mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    }

                }
            }
        }


    }

    private void callEditAddressAPi(String mAddressId) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {

                    JSONObject jsonObject = new JSONObject();
                    try {

                        jsonObject.put(DefaultNames.address_id, mAddressId);
                        jsonObject.put(DefaultNames.first_name, mFirst__Name);
                        jsonObject.put(DefaultNames.first_name, mFirst__Name);
                        jsonObject.put(DefaultNames.last_name, mLast__Name);
                        jsonObject.put(DefaultNames.country_code, mCountry__Code);
                        jsonObject.put(DefaultNames.mobile, mMobile__No);
                        jsonObject.put(DefaultNames.email, m__email);
                        jsonObject.put(DefaultNames.landline, mLand_Line);
                        jsonObject.put(DefaultNames.area, mAre__a);
                        jsonObject.put(DefaultNames.zone_id, mSelectedArea_Id_ZoneId);
                        jsonObject.put(DefaultNames.address_type, mSelectedAddressType_Id);
                        jsonObject.put(DefaultNames.block, mBloc__k);
                        jsonObject.put(DefaultNames.street, mStree_t);
                        jsonObject.put(DefaultNames.way, "");
                        jsonObject.put(DefaultNames.building_name, mBuildin_g);
                        jsonObject.put(DefaultNames.latitude, String.valueOf(Latitude));
                        jsonObject.put(DefaultNames.longitude, String.valueOf(Longitude));
                        jsonObject.put(DefaultNames.additional_direction, mAdditional_Direction);
                        jsonObject.put("address",mCAAddEBinding.etCaaAddress.getText().toString());
                        jsonObject.put("Vendor_id",mVendor_id);

                        if (mSelectedAddressType_Id.equals("1")) {
                            //Its a house address type selection :-
                            //To send floor and door_no as zero :-
                            jsonObject.put(DefaultNames.floor, "0");
                            jsonObject.put(DefaultNames.door_no, "0");
                        } else if (mSelectedAddressType_Id.equals("2")) {
                            //Its a apartment address type selection :-
                            jsonObject.put(DefaultNames.floor, mFloo__r);
                            jsonObject.put(DefaultNames.door_no, mApartment_No);
                        } else {
                            //Its a office address type selection :-
                            jsonObject.put(DefaultNames.floor, mFloo__r);
                            jsonObject.put(DefaultNames.door_no, mApartment_No);
                        }
                        jsonObject.put(DefaultNames.guest_status, "0"); //default guest_status = 0 for logged in user.
                        jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                        jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                        String mCustomerAuthorization = "";
                        if (AppFunctions.isUserLoggedIn(getActivity())) {
                            mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                        }
                        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                        retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                        Call<ApiResponseCheck> Call = retrofitInterface.editAddressApi(mCustomerAuthorization, body);
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
                                                // AppFunctions.msgDialogOk(getActivity(),"",getActivity().getString(R.string.otp_sent_to_your_mobile_number));
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder
                                                        .setMessage(mApiResponseCheck.success.message)
                                                        .setCancelable(false)
                                                        .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        getParentFragmentManager().popBackStack();
                                                                        dialog.dismiss();
                                                                    }
                                                                }
                                                        );

                                                AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.show();
                                            }


                                        } else {
                                            //Api response failure :-

                                            if (getActivity() != null) {
                                                if (mApiResponseCheck.error != null) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", mApiResponseCheck.error.message);
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
                            public void onFailure(@NonNull Call<ApiResponseCheck> call, @NonNull Throwable t) {
                                mProgressDialog.cancel();
                            }
                        });

                    } catch (JSONException e) {
                        mProgressDialog.cancel();

                        //Log.e("210 Excep ", e.toString());
                        e.printStackTrace();

                    }

                } else {

                    AppFunctions.toastShort(getActivity(), getActivity().getString(R.string.please_login_to_proceed_further));
                    Intent intent = new Intent(getActivity(), AppLogin.class);
                    getActivity().startActivity(intent);

                }

            } else {
                if(mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()){
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                        //Its for - checkout page address selection
                        mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
                        //Its - my account page address book call.
                        mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    }

                }
            }
        }


    }

    private void callCountryCodeListAPi() {


        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());


                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<CountryCodeApi> Call = retrofitInterface.countryCodeList(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<CountryCodeApi>() {
                        @Override
                        public void onResponse(@NonNull Call<CountryCodeApi> call, @NonNull Response<CountryCodeApi> response) {

                            mProgressDialog.cancel();

                            if (response.isSuccessful()) {
                                mCountryCodeApi = response.body();
                                if (mCountryCodeApi != null) {
                                    //Log.e("mCountryCodeApi", "not null");
                                    if (mCountryCodeApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {

                                            if (mCountryCodeApi.countryList != null && mCountryCodeApi.countryList.size() > 0) {


                                                int getCountryPosition = 0;
                                                //If its new address then load logged in user
                                                //mobile number country code as default selection.
                                                //If its edit address then load the current address country code as
                                                //default selection.
                                                Log.e("mCountry__Code","--"+mCountry__Code);
                                                for (int country = 0; country < mCountryCodeApi.countryList.size(); country++) {
                                                    if (mCountryCodeApi.countryList.get(country).getCode().equals(mCountry__Code)) {
                                                        getCountryPosition = country;
                                                        Log.e("getCountryPosition",""+getCountryPosition);
                                                        Log.e("mCountry__Code",""+mCountry__Code);
                                                    }
                                                }
                                                String mPhoneCode = "" + mCountryCodeApi.countryList.get(getCountryPosition).getCode();
                                                Log.e("getCountryPosition","--"+getCountryPosition);
                                                Log.e("mPhoneCode",""+mPhoneCode);
                                                mCAAddEBinding.tvCaaCountryCode.setText(mPhoneCode);


                                                /*int getCountryPosition = 0;
                                                if (mAddressAddOrEdit.equals(DefaultNames.address_add_process)) {
                                                        //its a new address.If its new address then load logged in user
                                                        //mobile number country code as default selection :-
                                                    for (int country = 0; country < mCountryCodeApi.countryList.size(); country++) {
                                                        if (mCountryCodeApi.countryList.get(country).getId().equals(mCountry__Code)) {
                                                            getCountryPosition = country;
                                                        }
                                                    }
                                                }else {
                                                    //its a edit address :-
                                                    //If its edit address then load the current address country code as
                                                    //default selection :-
                                                    for (int country = 0; country < mCountryCodeApi.countryList.size(); country++) {
                                                        if (mCountryCodeApi.countryList.get(country).getId().equals(mCountry__Code)) {
                                                            getCountryPosition = country;
                                                        }
                                                    }
                                                }
                                                String mPhoneCode = "" + mCountryCodeApi.countryList.get(getCountryPosition).getCode();
                                                mCAAddEBinding.tvCaaCountryCode.setText(mPhoneCode);*/


                                                // ********************  *********************** *****************************
                                                // ********************  *********************** *****************************
                                                /*if (!LoginCountryCodeDB.getInstance(getActivity()).check_selected()) {
                                                    //If there is no previous selection then default selection as first position of list :-
                                                    Log.e("1024","if");
                                                    //default country id india :-
                                                    //country id : 100
                                                    int getCountryPosition = 0;
                                                    for (int country = 0; country < mCountryCodeApi.countryList.size(); country++) {
                                                        if (mCountryCodeApi.countryList.get(country).getId().equals("100")) {
                                                            getCountryPosition = country;
                                                        }
                                                    }
                                                    String mPhoneCode = "" + mCountryCodeApi.countryList.get(getCountryPosition).getCode();
                                                    mCAAddEBinding.tvCaaCountryCode.setText(mPhoneCode);
                                                    if (getActivity() != null) {
                                                        // Glide.with(getActivity()).load(mCountryCodeList.get(getCountryPosition).getImage()).into(flagImg);
                                                    }
                                                } else {
                                                    Log.e("1024","else");
                                                    LoginCountryCodeDataSet loginCCDs = LoginCountryCodeDB.getInstance(getActivity()).get_Details();
                                                    for (int phoneCode = 0; phoneCode < mCountryCodeApi.countryList.size(); phoneCode++) {
                                                        if (mCountryCodeApi.countryList.get(phoneCode).getId().equals(loginCCDs.getCountryId())) {
                                                            mCAAddEBinding.tvCaaCountryCode.setText(loginCCDs.getPhoneCode());
                                                            if (getActivity() != null) {
                                                                // Glide.with(getActivity()).load(mCountryCodeApi.countryList.get(phoneCode).getImage()).into(flagImg);
                                                            }
                                                            break;
                                                        }
                                                    }
                                                }*/
                                                // ********************  *********************** *****************************
                                                // ********************  *********************** *****************************


                                            } else {


                                            }

                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mCountryCodeApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mCountryCodeApi.error.message);
                                            }
                                        }
                                    }
                                } else {
                                    //Log.e("mCountryCodeApi", "null");
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
                        public void onFailure(@NonNull Call<CountryCodeApi> call, @NonNull Throwable t) {

                            mProgressDialog.cancel();

                        }
                    });

                } catch (JSONException e) {
                    mProgressDialog.cancel();

                    //Log.e("210 Excep ", e.toString());
                    e.printStackTrace();

                }

            } else {
                if(mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()){
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                        //Its for - checkout page address selection
                        mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
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
    public void selectedCountryCode(LoginCountryCodeDataSet mLoginNCDs) {
        mCAAddEBinding.tvCaaCountryCode.setText(mLoginNCDs.getPhoneCode());
    }

    public void callAddressTypeSpinner() {

        mAddressTypeList = new String[]{getResources().getString(R.string.caa_house), getResources().getString(R.string.caa_apartment)
                , getResources().getString(R.string.caa_office)};

        SpinnerStatusAdapter spinnerStatusAdapter = new SpinnerStatusAdapter(mAddressTypeList);
        mCAAddEBinding.spinnerCaaAddressType.setAdapter(spinnerStatusAdapter);

        if (mAddressAddOrEdit.equals(DefaultNames.address_edit_process)) {
            //edit address process :-
            if (m__address_type != null && !m__address_type.isEmpty()) {
                if (m__address_type.equals("1")) {
                    //Its house :-
                    // Log.e("990 m__address_type house",m__address_type);
                    mCAAddEBinding.spinnerCaaAddressType.setSelection(0);
                } else if (m__address_type.equals("2")) {
                    //Its apartment :-
                    // Log.e("994 m__address_type apartment",m__address_type);
                    mCAAddEBinding.spinnerCaaAddressType.setSelection(1);
                } else if (m__address_type.equals("3")) {
                    //Its office :-
                    // Log.e("998 m__address_type office",m__address_type);
                    mCAAddEBinding.spinnerCaaAddressType.setSelection(2);
                }
            }
        } else {
            //Don't do any process for add address.because in default positions 0
            //item will be selected.
        }

        mCAAddEBinding.spinnerCaaAddressType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedAddressType_Name = mAddressTypeList[position];

                if (mSelectedAddressType_Name.equals(getResources().getString(R.string.caa_house))) {
                    mSelectedAddressType_Id = "1";
                    m__address_type = "1";
                    // Log.e("1015 m__address_type house",m__address_type);
                    mCAAddEBinding.layCaaFloor.setVisibility(View.GONE);
                    mCAAddEBinding.layCaaApartmentNo.setVisibility(View.GONE);
                    mCAAddEBinding.txtInpLayCaaBuilding.setHint(getActivity().getResources().getString(R.string.caa_house));
                    mCAAddEBinding.txtInpLayCaaApartmentNo.setHint(getActivity().getResources().getString(R.string.caa_apartment_no));
                } else if (mSelectedAddressType_Name.equals(getResources().getString(R.string.caa_apartment))) {
                    mSelectedAddressType_Id = "2";
                    m__address_type = "2";
                    mCAAddEBinding.layCaaFloor.setVisibility(View.VISIBLE);
                    mCAAddEBinding.layCaaApartmentNo.setVisibility(View.VISIBLE);
                    // Log.e("1025 m__address_type apartment",m__address_type);
                    mCAAddEBinding.txtInpLayCaaBuilding.setHint(getActivity().getResources().getString(R.string.caa_building));
                    mCAAddEBinding.txtInpLayCaaApartmentNo.setHint(getActivity().getResources().getString(R.string.caa_apartment_no));
                } else if (mSelectedAddressType_Name.equals(getResources().getString(R.string.caa_office))) {
                    mSelectedAddressType_Id = "3";
                    m__address_type = "3";
                    // Log.e("1031 m__address_type office",m__address_type);
                    mCAAddEBinding.layCaaFloor.setVisibility(View.VISIBLE);
                    mCAAddEBinding.layCaaApartmentNo.setVisibility(View.VISIBLE);
                    mCAAddEBinding.txtInpLayCaaBuilding.setHint(getActivity().getResources().getString(R.string.caa_building));
                    mCAAddEBinding.txtInpLayCaaApartmentNo.setHint(getActivity().getResources().getString(R.string.caa_office));
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private class SpinnerStatusAdapter extends BaseAdapter implements SpinnerAdapter {
        private final String[] mAddressType__List;

        SpinnerStatusAdapter(String[] status) {
            this.mAddressType__List = status;
        }

        @Override
        public int getCount() {
            return mAddressType__List.length;
        }

        @Override
        public Object getItem(int position) {
            return mAddressType__List[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView txt = new TextView(getActivity());
            //txt.setPadding(5, 5, 5, 5);
            txt.setTextSize(14);
            txt.setGravity(Gravity.START | Gravity.CENTER);
            txt.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            if(AppFunctions.mIsArabic(getActivity())){
                txt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.svg_keyboard_arrow_left_24dp, 0, 0, 0);
            }else {
                txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.svg_keyboard_arrow_right_24dp, 0);
            }



            txt.setText(mAddressType__List[position]);
            Typeface mTypeface = ResourcesCompat.getFont(getActivity(), R.font.poppins_regular);
            txt.setTypeface(mTypeface);
            txt.setTextColor(getResources().getColor(R.color.ar_popular_offers_text_color));
            return txt;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView txt = new TextView(getActivity());
            txt.setPadding(10, 10, 10, 10);
            txt.setTextSize(14);
            txt.setGravity(Gravity.START | Gravity.CENTER);
            txt.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            Typeface mTypeface = ResourcesCompat.getFont(getActivity(), R.font.poppins_regular);
            txt.setTypeface(mTypeface);
            txt.setText(mAddressType__List[position]);
            return txt;
        }
    }


    //**********************************************************************************************
    //*********************    ***********************   *******************************************

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
            if (mCAAddEBinding.mapViewCaaAddressLocation != null) {

                //   //Log.e("351","mCAAddEBinding.mapViewCaaAddressLocation != null");

                mCAAddEBinding.mapViewCaaAddressLocation.onResume(); // needed to get the map to display immediately

                try {
                    //  //Log.e("348","called");
                    MapsInitializer.initialize(getActivity().getApplicationContext());
                } catch (Exception e) {
                    // //Log.e("351",e.toString());
                    e.printStackTrace();
                }


                mCAAddEBinding.mapViewCaaAddressLocation.getMapAsync(mMap -> {
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
                // //Log.e("391","mCAAddEBinding.mapViewCaaAddressLocation == null");
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

        //Latitude != null && Longitude
        //Log.e("1082 Latitude",""+Latitude);
        //Log.e("1082 Longitude",""+Longitude);


        if (isFromMyLocationBtnClickOnMap) {
            //Here , initialCameraPosition method called by My location button clicked on google maps.
            //So to load current location only:-
            //Or else new address process is requested now :-
            toLoadCurrentLocation(location);
            //Log.e("1088","called");
        } else {


            if (AddressBookChangeLocationDB.getInstance(getActivity()).getSizeOfList() > 0) {

                AreaGeoCodeDataSet mAddressBkDs = AddressBookChangeLocationDB.getInstance(getActivity()).getDetails();
                String mTEMP_LAT = mAddressBkDs.getmLatitude();
                String mTEMP_LNG = mAddressBkDs.getmLongitude();
                String mTEMP_ADDS = mAddressBkDs.getmAddress();

                if (mTEMP_LAT != null && mTEMP_LNG != null && !mTEMP_LAT.isEmpty() && !mTEMP_LNG.isEmpty()) {
                    Latitude = Double.parseDouble(mTEMP_LAT);
                    Longitude = Double.parseDouble(mTEMP_LNG);


                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Latitude, Longitude),
                            16.00f));
                } else {
                    toLoadCurrentLocation(location);
                }

            } else {

                toLoadCurrentLocation(location);

            }

        }


        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        mGoogleMap.setOnMarkerClickListener(marker -> false);
        mGoogleMap.setOnCameraMoveStartedListener(i -> {
            mCAAddEBinding.pbCaaAddressMap.setVisibility(View.VISIBLE);
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

                Log.e("mAdds"+mAdss,"");

                if (mAdss != null && !mAdss.equals("")) {
                    mCAAddEBinding.etCaaAddress.setText(mAdss);
                } else {
                    //  Log.e("getFeatureName", "***");
                    mCAAddEBinding.etCaaAddress.setText("");
                }

                //Log.e("587 ", mAdss);
                current_address_string = mAdss;
                current_address_name_only_string = addresses.get(0).getFeatureName();

                // String sdfsdf = addresses.get(0).

                /*if (mAdss != null && !mAdss.isEmpty()) {
                    Log.e("mAdss", mAdss);
                } else {
                    Log.e("mAdss", "***");
                }*/

                /*if (addresses.get(0) != null && !addresses.get(0).toString().isEmpty()) {
                    String mAddres0 = addresses.get(0).toString();
                    Log.e("mAddres0", mAddres0);
                } else {
                    Log.e("mAddres0", "***");
                }*/


                /*if (addresses.get(0) != null && !addresses.get(0).toString().isEmpty()) {
                    String mAddres0 = addresses.get(0).toString();
                    Log.e("mAddres0", mAddres0);
                } else {
                    Log.e("mAddres0", "***");
                }*/


                /*if (addresses.get(0).getAdminArea() != null && !addresses.get(0).getAdminArea().isEmpty()) {
                    String getAdminArea = addresses.get(0).getAdminArea();
                    Log.e("getAdminArea", getAdminArea);
                } else {
                    Log.e("getAdminArea", "***");
                }


                if (addresses.get(0).getSubAdminArea() != null && !addresses.get(0).getSubAdminArea().isEmpty()) {
                    String getSubAdminArea = addresses.get(0).getSubAdminArea();
                    Log.e("getSubAdminArea", getSubAdminArea);
                } else {
                    Log.e("getSubAdminArea", "***");
                }*/


                /*if (addresses.get(0).getCountryCode() != null && !addresses.get(0).getCountryCode().isEmpty()) {
                    String getCountryCode = addresses.get(0).getCountryCode();
                    Log.e("getCountryCode", getCountryCode);
                } else {
                    Log.e("getCountryCode", "***");
                }


                if (addresses.get(0).getLocality() != null && !addresses.get(0).getLocality().isEmpty()) {
                    String getLocality = addresses.get(0).getLocality();
                    Log.e("getLocality", getLocality);
                } else {
                    Log.e("getLocality", "***");
                }


                if (addresses.get(0).getCountryName() != null && !addresses.get(0).getCountryName().isEmpty()) {
                    String getCountryName =  addresses.get(0).getCountryName();
                    Log.e("getCountryName", getCountryName);
                } else {
                    Log.e("getCountryName", "***");
                }*/


                if (addresses.get(0).getFeatureName() != null && !addresses.get(0).getFeatureName().isEmpty()) {
                    String getFeatureName = addresses.get(0).getFeatureName();
                    // Log.e("getFeatureName", getFeatureName);
                    mCAAddEBinding.etCaaStreet.setText(getFeatureName);
                } else {
                    //  Log.e("getFeatureName", "***");
                    mCAAddEBinding.etCaaStreet.setText("");
                }



                /*if (addresses.get(0).getPhone() != null && !addresses.get(0).getPhone().isEmpty()) {
                    String getPhone = addresses.get(0).getPhone();
                    Log.e("getPhone", getPhone);
                } else {
                    Log.e("getPhone", "***");
                }


                if (addresses.get(0).getPostalCode() != null && !addresses.get(0).getPostalCode().isEmpty()) {
                    String getPostalCode = addresses.get(0).getPostalCode();
                    Log.e("getPostalCode", getPostalCode);
                } else {
                    Log.e("getPostalCode", "***");
                }


                if (addresses.get(0).getPremises() != null && !addresses.get(0).getPremises().isEmpty()) {
                    String getPremises = addresses.get(0).getPremises();
                    Log.e("getPremises", getPremises);
                } else {
                    Log.e("getPremises", "***");
                }*/


                /*if (addresses.get(0).getSubLocality() != null && !addresses.get(0).getSubLocality().isEmpty()) {
                    String getSubLocality = addresses.get(0).getSubLocality();
                  //  Log.e("getSubLocality", getSubLocality);
                    mCAAddEBinding..setText(getSubLocality);
                } else {
                  //  Log.e("getSubLocality", "***");
                    mCAAddEBinding..setText("");
                }*/

//                if (RestaurantAddressDB.getInstance(getActivity()).getSizeOfList() > 0) {
//                    AddressGeocodeDataSet addressGeocodeDs = RestaurantAddressDB.getInstance(getActivity()).getGeocodeDetails();
//                    callVendorZoneAPi(addressGeocodeDs.getRestaurantId(), String.valueOf(Latitude), String.valueOf(Longitude));
//                } else {
//                    callVendorZoneAPi("", String.valueOf(Latitude), String.valueOf(Longitude));
//                }


                /*


                if (addresses.get(0).getSubThoroughfare() != null && !addresses.get(0).getSubThoroughfare().isEmpty()) {
                    String getSubThoroughfare = addresses.get(0).getSubThoroughfare();
                    Log.e("getSubThoroughfare", getSubThoroughfare);
                } else {
                    Log.e("getSubThoroughfare", "***");
                }*/


                if (addresses.get(0).getThoroughfare() != null && !addresses.get(0).getThoroughfare().isEmpty()) {
                    String getThoroughfare = addresses.get(0).getThoroughfare();
                    //  Log.e("getThoroughfare", getThoroughfare);
                } else {
                    // Log.e("getThoroughfare", "***");
                }


                /*if (addresses.get(0).getUrl() != null && !addresses.get(0).getUrl().isEmpty()) {
                    String getUrl = addresses.get(0).getUrl();
                    Log.e("getUrl", getUrl);
                } else {
                    Log.e("getUrl", "***");
                }

                String getMaxAddressLineIndex = "" +addresses.get(0).getMaxAddressLineIndex();
                if (getMaxAddressLineIndex != null && !getMaxAddressLineIndex.isEmpty()) {
                    Log.e("getMaxAddressLineIndex", getMaxAddressLineIndex);
                } else {
                    Log.e("getMaxAddressLineIndex", "***");
                }


                if (addresses.get(0).getExtras() != null && !addresses.get(0).getExtras().toString().isEmpty()) {
                    String getExtras = addresses.get(0).getExtras().toString();
                    Log.e("getExtras", getExtras);
                } else {
                    Log.e("getExtras", "***");
                }

                String getLatitude = "" + addresses.get(0).getLatitude();
                if (getLatitude != null && !getLatitude.isEmpty()) {
                    Log.e("getLatitude", getLatitude);
                } else {
                    Log.e("getLatitude", "***");
                }

                String getLongitude = "" + addresses.get(0).getLongitude();
                if (getLongitude != null && !getLongitude.isEmpty()) {
                    Log.e("getLongitude", getLongitude);
                } else {
                    Log.e("getLongitude", "***");
                }


                if (addresses.get(0).getLocale() != null && !addresses.get(0).getLocale().toString().isEmpty()) {
                    String getLocale = addresses.get(0).getLocale().toString();
                    Log.e("getLocale", getLocale);
                } else {
                    Log.e("getLocale", "***");
                }


                String describeContents = ""+addresses.get(0).describeContents();
                if (describeContents != null && !describeContents.isEmpty()) {
                    Log.e("describeContents", describeContents);
                } else {
                    Log.e("describeContents", "***");
                }*/




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

                mCAAddEBinding.pbCaaAddressMap.setVisibility(View.GONE);
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

    //*********************    ***********************   *******************************************
    //**********************************************************************************************

    private void callVendorZoneAPi(String vendorID, String searchedLat, String searchedLng) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put(DefaultNames.vendor_id, vendorID);
                    jsonObject.put(DefaultNames.latitude, searchedLat);
                    jsonObject.put(DefaultNames.longitude, searchedLng);

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<VendorZoneApi> Call = retrofitInterface.vendorZoneApi(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<VendorZoneApi>() {
                        @Override
                        public void onResponse(@NonNull Call<VendorZoneApi> call, @NonNull Response<VendorZoneApi> response) {

                            mProgressDialog.cancel();

                            if (getActivity() != null) {
                                if (response.isSuccessful()) {

                                    mVendorZoneApi = response.body();
                                    if (mVendorZoneApi != null) {
                                        //Log.e("mVendorZoneApi", "not null");
                                        if (mVendorZoneApi.success != null) {
                                            //Api response successDataSet :-
                                            if (getActivity() != null) {


                                                if(mVendorZoneApi.vendorZoneDetails != null && mVendorZoneApi.vendorZoneDetails.size() > 0){
                                                    callAreaSpinner();
                                                }else {
                                                    serviceNotAvailMsgForZone();
                                                }

                                            }

                                        } else {
                                            //Api response failure :-
                                            if (mVendorZoneApi.error != null) {
                                               // AppFunctions.msgDialogOk(getActivity(), "", mVendorZoneApi.error.message);
                                              //  AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.service_not_available_for_current_area));
                                                serviceNotAvailMsgForZone();
                                            }

                                        }
                                    }

                                } else {

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
                        public void onFailure(@NonNull Call<VendorZoneApi> call, @NonNull Throwable t) {

                            mProgressDialog.cancel();

                        }
                    });

                } catch (JSONException e) {
                    mProgressDialog.cancel();

                    //Log.e("210 Excep ", e.toString());
                    e.printStackTrace();

                }


            } else {
                if(mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()){
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                        //Its for - checkout page address selection
                        mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
                        //Its - my account page address book call.
                        mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    }

                }
            }
        }


    }


    private void serviceNotAvailMsgForZone(){
        if (getActivity() != null) {
            // AppFunctions.msgDialogOk(getActivity(),"",getActivity().getString(R.string.otp_sent_to_your_mobile_number));
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder
                    .setMessage(getActivity().getResources().getString(R.string.service_not_available_for_current_area))
                    .setCancelable(false)
                    .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    if (getActivity() != null) {
                                        if (mIsAddsBookCallFrom != null && !mIsAddsBookCallFrom.isEmpty()) {
                                            AreaGeoCodeDataSet mAreaDS = new AreaGeoCodeDataSet();
                                            mAreaDS.setmAddress(current_address_string);
                                            mAreaDS.setmLatitude(String.valueOf(Latitude));
                                            mAreaDS.setmLongitude(String.valueOf(Longitude));
                                            mAreaDS.setmAddsNameOnly(current_address_name_only_string);
                                            if (AddressBookChangeLocationDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                                AddressBookChangeLocationDB.getInstance(getActivity()).deleteDB();
                                            }
                                            AddressBookChangeLocationDB.getInstance(getActivity()).add(mAreaDS);
                                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                            AddressChangeLocation m_addressChangeLocation = new AddressChangeLocation();
                                            Bundle mBundle = new Bundle();
                                            if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_CheckOutAddsBook)) {
                                                //Its for - checkout page address selection
                                                mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_CheckOutAddsBook);
                                                m_addressChangeLocation.setArguments(mBundle);
                                                mFT.replace(R.id.layout_app_check_out_body, m_addressChangeLocation, "m_addressChangeLocation");
                                                mFT.addToBackStack("m_addressChangeLocation");
                                                mFT.commit();
                                            } else if (mIsAddsBookCallFrom.equals(DefaultNames.callFor_MyAccountAddsBook)) {
                                                //Its - my account page address book call.
                                                mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_MyAccountAddsBook);
                                                m_addressChangeLocation.setArguments(mBundle);
                                                mFT.replace(R.id.layout_app_home_body, m_addressChangeLocation, "m_addressChangeLocation");
                                                mFT.addToBackStack("m_addressChangeLocation");
                                                mFT.commit();
                                            }
                                        }
                                    }
                                    dialog.dismiss();

                                }
                            }
                    ).setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getParentFragmentManager().popBackStack();
                            dialog.dismiss();
                        }
                    }
            );

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

    }


    public void callAreaSpinner() {


        SpinnerZonesAdapter spinnerZonesAdapter = new SpinnerZonesAdapter(mVendorZoneApi.vendorZoneDetails);
        mCAAddEBinding.spinnerCaaArea.setAdapter(spinnerZonesAdapter);

        if (mAddressAddOrEdit.equals(DefaultNames.address_edit_process)) {
            //edit address process :-
            ArrayList<VendorZoneDetailsDataSet> mZoneIdList = mVendorZoneApi.vendorZoneDetails;
            if (m__Area_id_or_zone_id != null && !m__Area_id_or_zone_id.isEmpty()) {
                for(int zoneID = 0;zoneID < mZoneIdList.size();zoneID++){
                    if(mZoneIdList.get(zoneID).getZone_id().equals(m__Area_id_or_zone_id)){
                        mCAAddEBinding.spinnerCaaArea.setSelection(zoneID);
                    }
                }

            }
        } else {
            //Don't do any process for add address.
        }

        mCAAddEBinding.spinnerCaaArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedArea_Name = mVendorZoneApi.vendorZoneDetails.get(position).getZone_name();
                mSelectedArea_Id_ZoneId = mVendorZoneApi.vendorZoneDetails.get(position).getZone_id();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private class SpinnerZonesAdapter extends BaseAdapter implements SpinnerAdapter {
        private ArrayList<VendorZoneDetailsDataSet>  mVendorZone__List;

        SpinnerZonesAdapter(ArrayList<VendorZoneDetailsDataSet>  vendorZone__List) {
            this.mVendorZone__List = vendorZone__List;
        }

        @Override
        public int getCount() {
            return mVendorZone__List.size();
        }

        @Override
        public Object getItem(int position) {
            return mVendorZone__List.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView txt = new TextView(getActivity());
            //txt.setPadding(5, 5, 5, 5);
            txt.setTextSize(14);
            txt.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            txt.setGravity(Gravity.START | Gravity.CENTER);
            if(AppFunctions.mIsArabic(getActivity())){
                txt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.svg_keyboard_arrow_left_24dp, 0, 0, 0);
            }else {
                txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.svg_keyboard_arrow_right_24dp, 0);
            }
            txt.setText(mVendorZone__List.get(position).getZone_name());
            Typeface mTypeface = ResourcesCompat.getFont(getActivity(), R.font.poppins_regular);
            txt.setTypeface(mTypeface);
            txt.setTextColor(getResources().getColor(R.color.ar_popular_offers_text_color));
            return txt;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView txt = new TextView(getActivity());
            txt.setPadding(10, 10, 10, 10);
            txt.setTextSize(14);
            txt.setGravity(Gravity.START | Gravity.CENTER);
            txt.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            Typeface mTypeface = ResourcesCompat.getFont(getActivity(), R.font.poppins_regular);
            txt.setTypeface(mTypeface);
            txt.setText(mVendorZone__List.get(position).getZone_name());
            return txt;
        }
    }



}