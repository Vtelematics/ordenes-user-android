package com.ordenese.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.onesignal.OneSignal;
import com.ordenese.Activities.AppHome;
import com.ordenese.Activities.AppLogin;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.CustomClasses.SimpleCountDownTimer;
import com.ordenese.DataSets.AddressGeocodeDataSet;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.DataSets.AreaGeoCodeDataSet;
import com.ordenese.DataSets.CartTotalsDataSet;
import com.ordenese.DataSets.CheckOutAddsDataSet;
import com.ordenese.DataSets.CheckOutDBDataSet;
import com.ordenese.DataSets.CheckoutCartListApi;
import com.ordenese.DataSets.ConfirmOrderApi;
import com.ordenese.DataSets.CouponApplyApi;
import com.ordenese.DataSets.PaymentDataSet;
import com.ordenese.DataSets.PaymentMethodsApi;
import com.ordenese.DataSets.ScheduleDeliveryDataSet;
import com.ordenese.Databases.AddressBookChangeLocationDB;
import com.ordenese.Databases.AddressBookGuestDB;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.CheckOutDetailsDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.RestaurantAddressDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CartApplyCall;
import com.ordenese.Interfaces.CheckOutBackPress;
import com.ordenese.Interfaces.GuestMobileNoOTPUI;
import com.ordenese.Interfaces.onSetUpdate;
import com.ordenese.R;
import com.ordenese.databinding.CheckOutBinding;
import com.ordenese.databinding.FragmentScheduleDialogBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckOut extends Fragment implements View.OnClickListener, CartApplyCall,
        GuestMobileNoOTPUI, SimpleCountDownTimer.OnCountDownListener, onSetUpdate {

    private CheckOutBinding mCheckOutBinding;
    private CheckOutBackPress mCheckOutBackPress;
    //google maps :-
    private Bundle mSavedInstanceState;
    private GoogleMap mGoogleMap;
    private GoogleApiClient mGoogleApiClient;
    private final int[] MAP_TYPES = {GoogleMap.MAP_TYPE_SATELLITE,
            GoogleMap.MAP_TYPE_NORMAL,
            GoogleMap.MAP_TYPE_HYBRID,
            GoogleMap.MAP_TYPE_TERRAIN,
            GoogleMap.MAP_TYPE_NONE};
    private final int curMapTypeIndex = 1;
    private LocationRequest mLocationRequest;

    private RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;
    private CheckoutCartListApi mCCartListApi;
    private RecyclerView.LayoutManager mCLTotalsLayoutMgr;
    private CheckOutTotalsListAdapter mCheckOutTotalsListAdapter;

    public PaymentMethodsApi mPaymentMethodsApi;
    private PaymentMethodsListAdapter mPaymentMethodsListAdapter;
    private RecyclerView.LayoutManager mPMLayoutMgr;
    private String mSelectedPMCode = "", mSelectedPMCode_ID = "", mSubTotalAmt = "",
            mCouponCODE_Id = "", mCouponCODE = "", mSelectedDeliveryAddsId = "", mSelectedAddsLatitude = "", mSelectedAddsLongitude = "",
            mSelectedAddsZoneId = "", mVendorName = "", mVendorID = "", mCheckOutAddNote, order_type = "";
    public static String delivery_time = "";
    private RadioButton mSelectedPMRadioBtn;
    TextView mSelectedPMTvTitle;
    LinearLayout mSelectedPMLayBorder;
    private static ScheduleDeliveryDataSet scheduleDeliveryDataSet;
    ConfirmOrderApi mConfirmOrderApi;
    private Boolean mIsCheckoutUiHide = false;

    //Guest OTP :-
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId = "";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private SimpleCountDownTimer simpleCountDownTimer;
    private String mMOBNoWithCCodeWithPlus = "";
    private Boolean mIsGuestOtpVerifiedSuccessfully = false, mIsThereIsNoPaymentMethodsAvailable = false;
    private static onSetUpdate setUpdate;
    Activity activity;
    String selected_payment = " ";

    private int selectedPosition = -1;
    private int selectedPosition1 = -1;
    private boolean mFirstTime = true;
    private boolean mFirstTime1= true;

    String payment_mobile = "";
    boolean contactlessDelivery;
    public CheckOut() {
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
        setUpdate = (onSetUpdate) this;
        activity = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mCheckOutBinding = CheckOutBinding.inflate(inflater, container, false);

        if (AddressBookGuestDB.getInstance(getActivity()).getSizeOfList() != 0)
            AddressBookGuestDB.getInstance(getActivity()).deleteDB();

        mCheckOutBinding.layCheckoutUi.setVisibility(View.VISIBLE);
        mCheckOutBinding.layCheckoutGuestOtpUi.setVisibility(View.GONE);
        mCheckOutBinding.layCoPaymentListEmptyMsg.setVisibility(View.GONE);

        mCheckOutBinding.imgCheckOutBack.setOnClickListener(this);
        mCheckOutBinding.tvCoCouponAddCoupon.setOnClickListener(this);
        mCheckOutBinding.layCoPlaceOrderBtnContainer.setOnClickListener(this);
        mCheckOutBinding.tvCoDeliveryAddsEdit.setOnClickListener(this);

        mCheckOutBinding.tvCoDeliveryAddsNotAvailableAddAdds.setOnClickListener(this);

        mCheckOutBinding.layCgOtpSubmitBtnContainer.setOnClickListener(this);
        mCheckOutBinding.layCgOtpResend.setOnClickListener(this);

        mCheckOutBinding.layCoDeliveryAddsAvailableHolder.setVisibility(View.GONE);
        mCheckOutBinding.layCoDeliveryAddsNotAvailableHolder.setVisibility(View.INVISIBLE);

        mCheckOutBinding.layCoUnCheckCldForCodEnableParent.setVisibility(View.GONE);

        mCheckOutBackPress = (CheckOutBackPress) getActivity();

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mCheckOutBinding.layCoContactlessDeliveryParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckOutBinding.checkBoxCoContactlessDelivery.setChecked(!mCheckOutBinding.checkBoxCoContactlessDelivery.isChecked());
            }
        });

        mCheckOutBinding.checkBoxCoContactlessDelivery.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                           @SuppressLint("NotifyDataSetChanged")
                           @Override
                           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                               if (isChecked) {
                                   //    Log.e("235","called.");
                                   CheckOutDetailsDB.getInstance(getActivity()).updateContactLessDelivery(DefaultNames.checked);
                                   if (mSelectedPMCode.equalsIgnoreCase("cod")) {
                                       //  Log.e("241","called.");
                                       if (mSelectedPMRadioBtn != null) {
                                           //  Log.e("245","called.");
                                           mSelectedPMRadioBtn.setChecked(false);
                                           mSelectedPMCode = "";
                                           mSelectedPMCode_ID = "";
                                           CheckOutDetailsDB.getInstance(getActivity()).updatePaymentListId("");
                                           CheckOutDetailsDB.getInstance(getActivity()).updatePaymentCode("");
                                           mCheckOutBinding.layCoUnCheckCldForCodEnableParent.setVisibility(View.VISIBLE);
                                       }
                                   }
                               } else {
                                   if (mPaymentMethodsListAdapter != null) {
                                       //   Log.e("271","called.");
                                       mPaymentMethodsListAdapter.notifyDataSetChanged();
                                   }
                                    //  Log.e("264","called.");
                                    CheckOutDetailsDB.getInstance(getActivity()).updateContactLessDelivery(DefaultNames.unChecked);
                                    mCheckOutBinding.layCoUnCheckCldForCodEnableParent.setVisibility(View.GONE);
                                }

                                if (mPaymentMethodsListAdapter != null) {
                                    //   Log.e("271","called.");
                                    mPaymentMethodsListAdapter.notifyDataSetChanged();
                                }
                                CheckOutDetailsDB.getInstance(getActivity()).print();
                            }
                        }
        );

        //google maps for delivery location :-
        mSavedInstanceState = savedInstanceState;
        mCheckOutBinding.mapViewCoDeliveryLocation.onCreate(mSavedInstanceState);

        if (RestaurantAddressDB.getInstance(getActivity()).getSizeOfList() > 0) {
            AddressGeocodeDataSet addressGeocodeDs = RestaurantAddressDB.getInstance(getActivity()).getGeocodeDetails();
            mCheckOutAddNote = addressGeocodeDs.getCheckOutNote();
            mCheckOutBinding.tvCoAppBarVendorName.setText(mVendorName);
        }

        if (getArguments() != null && getArguments().getString("vendor_id") != null) {
            mVendorID = getArguments().getString("vendor_id");
        }

//        if (AreaGeoCodeDB.getInstance(getActivity()).getSizeOfList() > 0) {
//            AreaGeoCodeDataSet mAreaGeoCodeDS = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
//            mLatitude = mAreaGeoCodeDS.getmLatitude();
//            mLongitude = mAreaGeoCodeDS.getmLongitude();
//        }

        if (!AppFunctions.isUserLoggedIn(getActivity())) {
            //Its a guest user :-
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();

            // Initialize phone auth callbacks
            // [START phone_auth_callbacks]
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    // This callback will be invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
                    //Log.d("onVerificationCompleted:", "" + credential);
                    // m_Toast("onVerificationCompleted ", "credential : " + credential.toString());

                    // signInWithPhoneAuthCredential(credential);

                    mProgressDialog.cancel();
                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    // This callback is invoked in an invalid request for verification is made,
                    // for instance if the the phone number format is not valid.
                    //Log.w("onVerificationFailed", e);

                    // m_Toast("onVerificationFailed ", "Excep : " + e.toString());

                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                    } else if (e instanceof FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                    }

                    mProgressDialog.cancel();

                    // Show a message and update the UI
                }

                @Override
                public void onCodeSent(@NonNull String verificationId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    //Log.d("onCodeSent:", verificationId);

                    // m_Toast("onCodeSent ", "verificationId : " + verificationId + ",Token : " + token);
                    if (getActivity() != null) {
                        // AppFunctions.msgDialogOk(getActivity(),"",getActivity().getString(R.string.otp_sent_to_your_mobile_number));
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder
                                .setMessage(getActivity().getString(R.string.otp_sent_to_your_mobile_number))
                                //.setTitle(mContext.getString(R.string.))
                                .setCancelable(true)
                                .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        simpleCountDownTimer = new SimpleCountDownTimer(0, 30, 1, CheckOut.this);
                                        simpleCountDownTimer.start(false);
                                        dialog.dismiss();
                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.show();
                    }

                    // Save verification ID and resending token so we can use them later
                    mVerificationId = verificationId;
                    mResendToken = token;

                    mProgressDialog.cancel();

                }
            };
        }

        callPaymentMethodsListApi();

        mCheckOutBinding.scheduleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScheduleDialog scheduleDialog = new ScheduleDialog();
                scheduleDialog.setCancelable(true);
                scheduleDialog.show(getParentFragmentManager(), "mDialogueCouponCode");

            }
        });

        return mCheckOutBinding.getRoot();
    }


    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if (mId == R.id.img_check_out_back) {

            if (mIsCheckoutUiHide) {
                mIsCheckoutUiHide = false;
                mCheckOutBinding.layCheckoutUi.setVisibility(View.VISIBLE);
                mCheckOutBinding.layCheckoutGuestOtpUi.setVisibility(View.GONE);
                mCheckOutBinding.layCgOtpResend.setVisibility(View.GONE);
                mCheckOutBinding.tvCoAppBarVendorName.setText(mVendorName);
            } else {
                if (mCheckOutBackPress != null) {
                    mCheckOutBackPress.checkOutBackPressed();
                }
            }

        } else if (mId == R.id.tv_co_coupon_add_coupon) {
            if (getActivity() != null) {

                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    //logged in user :-
                    if (CheckOutDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        //Now remove coupon showed :-
                        //So to process for delete :-
                        CheckOutDBDataSet mCheckOutDBDs = CheckOutDetailsDB.getInstance(getActivity()).getDetails();
                        if (mCheckOutDBDs.getCouponCode() != null && !mCheckOutDBDs.getCouponCode().isEmpty()) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                            alertDialogBuilder
                                    .setTitle("")
                                    .setMessage(getActivity().getResources().getString(R.string.remove_coupon_confirmation_msg))
                                    .setCancelable(true)
                                    .setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setPositiveButton(getActivity().getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            if (getActivity() != null) {
                                                //CheckOutDetailsDB.getInstance(getActivity()).deleteDB();
                                                // mCheckOutDBDs.setCouponCode(""); //just be empty when coupon removed.
                                                // mCheckOutDBDs.setCouponId(""); //just be empty when coupon removed.
                                                CheckOutDetailsDB.getInstance(getActivity()).updateCouponCode("");
                                                CheckOutDetailsDB.getInstance(getActivity()).updateCouponId("");
                                                mCheckOutBinding.tvCoCouponAddCoupon.setText(getActivity().getResources().getString(R.string.add_coupon));
                                                mCouponCODE_Id = "";
                                                mCouponCODE = "";
                                                callCartListAPi(false);
                                                dialog.dismiss();
                                            }
                                        }
                                    });
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.show();
                        } else {
                            toCallCouponUI();
                        }
                    } else {
                        toCallCouponUI();
                    }
                } else {
                    //guest user :-
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder
                            .setTitle("")
                            .setMessage(getActivity().getResources().getString(R.string.coupon_error_msg_for_guest))
                            .setCancelable(true)
                            .setNegativeButton(getActivity().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(getActivity().getResources().getString(R.string.login), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (getActivity() != null) {
                                        Intent intent = new Intent(getActivity(), AppLogin.class);
                                        getActivity().startActivity(intent);
                                        dialog.dismiss();
                                    }
                                }
                            });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        } else if (mId == R.id.lay_co_place_order_btn_container) {
            //AppFunctions.msgDialogOk(getActivity(), "", "Place order - " + mSelectedPMCode);
            if (getActivity() != null) {

                if(!mCheckOutBinding.edtPaymentMobile.getText().toString().equals("")){
                    payment_mobile = mCheckOutBinding.edtPaymentMobile.getText().toString();
                }

                if (!mSelectedPMCode_ID.isEmpty()) {

                    if (order_type.equals("2")) {
                        if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                            callConfirmOrderApi();
//                        if (mSelectedAddsLongitude != null && !mSelectedAddsLongitude.isEmpty() &&
//                                mSelectedAddsLatitude != null && !mSelectedAddsLatitude.isEmpty() &&
//                                mSelectedDeliveryAddsId != null && !mSelectedDeliveryAddsId.isEmpty() &&
//                                mSelectedAddsZoneId != null && !mSelectedAddsZoneId.isEmpty()) {
//                            //logged in user :-
//                            callIsDeliveryAPi(mSelectedAddsLatitude, mSelectedAddsLongitude, mSelectedDeliveryAddsId, mSelectedAddsZoneId);
//                        } else {
//                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.co_please_select_delivery_address));
//                        }
                        } else {
                            //guest user :-
                            if (AddressBookChangeLocationDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                AddressBookChangeLocationDB.getInstance(getActivity()).deleteDB();
                            }
                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                            GuestAddressAddEdit m_guestAddressAddEdit = new GuestAddressAddEdit();
                            Bundle mBundle = new Bundle();
                            mBundle.putString(DefaultNames.address_add_or_edit, DefaultNames.address_add_process);
                            mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_CheckOutAddsBook);
                            mBundle.putString(DefaultNames.vendor_id, mVendorID);
                            mBundle.putString("order_type", order_type);
                            mBundle.putString("payment_list_id", mSelectedPMCode_ID);

                            String contactless_delivery;
                            if (mCheckOutBinding.checkBoxCoContactlessDelivery.isChecked()) {
                                contactless_delivery = "1";
                            } else {
                                contactless_delivery = "0";
                            }
                            mBundle.putString("contactless_delivery", contactless_delivery);
                            m_guestAddressAddEdit.setArguments(mBundle);
                            mFT.replace(R.id.layout_app_check_out_body, m_guestAddressAddEdit, "m_guestAddressAddEdit");
                            mFT.addToBackStack("m_guestAddressAddEdit");
                            mFT.commit();

//                        if (mIsGuestOtpVerifiedSuccessfully) {
//                            callIsDeliveryAPi(mSelectedAddsLatitude, mSelectedAddsLongitude, mSelectedDeliveryAddsId, mSelectedAddsZoneId);
//                        } else {
//                            //For guest users to verify their current delivery address mobile number through OTP.
//                            //If mobile number is to change while processing otp then guest must go back to
//                            //edit address page to perform change number and save for checkout.
//                            // AppFunctions.toastShort(getActivity(),"Guest place order.");
//                            DialogueGuestMobileNoVerify m_dialogueGuestMobileNoVerify = new DialogueGuestMobileNoVerify();
//                            Bundle bundle = new Bundle();
//                            if (mPaymentMethodsApi != null) {
//                                if (mPaymentMethodsApi.checkOutAdds != null) {
//                                    CheckOutAddsDataSet mCOAddsDs = mPaymentMethodsApi.checkOutAdds;
//                                    if (mCOAddsDs.getAddress_id() != null && !mCOAddsDs.getAddress_id().isEmpty()) {
//                                        String mCountry_code = mCOAddsDs.getCountry_code();
//                                        String mMobile = mCOAddsDs.getMobile();
//                                        bundle.putString(DefaultNames.country_code, mCountry_code);
//                                        bundle.putString(DefaultNames.mobile, mMobile);
//                                    }
//                                }
//                            }
//                            m_dialogueGuestMobileNoVerify.setArguments(bundle);
//                            m_dialogueGuestMobileNoVerify.setTargetFragment(CheckOut.this, 7546);
//                            m_dialogueGuestMobileNoVerify.show(getParentFragmentManager(), "m_dialogueGuestMobileNoVerify");
//                        }
                        }
                    } else {
                        if (mSelectedAddsLongitude != null && !mSelectedAddsLongitude.isEmpty() &&
                                mSelectedAddsLatitude != null && !mSelectedAddsLatitude.isEmpty() &&
                                mSelectedDeliveryAddsId != null && !mSelectedDeliveryAddsId.isEmpty() /*&&
                            mSelectedAddsZoneId != null && !mSelectedAddsZoneId.isEmpty()*/) {

                            if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                //logged in user :-
                                callIsDeliveryAPi(mSelectedAddsLatitude, mSelectedAddsLongitude, mSelectedDeliveryAddsId, mSelectedAddsZoneId);
                            } else {
                                //guest user :-
                                if (mIsGuestOtpVerifiedSuccessfully) {
                                    callIsDeliveryAPi(mSelectedAddsLatitude, mSelectedAddsLongitude, mSelectedDeliveryAddsId, mSelectedAddsZoneId);
                                } else {
                                    //For guest users to verify their current delivery address mobile number through OTP.
                                    //If mobile number is to change while processing otp then guest must go back to
                                    //edit address page to perform change number and save for checkout.
                                    // AppFunctions.toastShort(getActivity(),"Guest place order.");
                                    DialogueGuestMobileNoVerify m_dialogueGuestMobileNoVerify = new DialogueGuestMobileNoVerify();
                                    Bundle bundle = new Bundle();
                                    if (mPaymentMethodsApi != null) {
                                        if (mPaymentMethodsApi.checkOutAdds != null) {
                                            CheckOutAddsDataSet mCOAddsDs = mPaymentMethodsApi.checkOutAdds;
                                            if (mCOAddsDs.getAddress_id() != null && !mCOAddsDs.getAddress_id().isEmpty()) {
                                                String mCountry_code = mCOAddsDs.getCountry_code();
                                                String mMobile = mCOAddsDs.getMobile();
                                                bundle.putString(DefaultNames.country_code, mCountry_code);
                                                bundle.putString(DefaultNames.mobile, mMobile);
                                                m_dialogueGuestMobileNoVerify.setArguments(bundle);
                                            }
                                        }
                                    }
                                    m_dialogueGuestMobileNoVerify.setTargetFragment(CheckOut.this, 7546);
                                    m_dialogueGuestMobileNoVerify.show(getParentFragmentManager(), "m_dialogueGuestMobileNoVerify");
                                }
                            }
                        } else {
                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.co_please_select_delivery_address));
                        }
                    }
                } else {
                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_select_payment_address));
                }
            }

        } else if (mId == R.id.tv_co_delivery_adds_edit) {

            selectedPosition = -1;
            mSelectedPMCode = "";
            mSelectedPMCode_ID="";

            CheckOutDetailsDB.getInstance(getActivity()).updatePaymentListId("");
            CheckOutDetailsDB.getInstance(getActivity()).updatePaymentCode("");

            Log.e("mSelectedPM", "onClick: "+mSelectedPMCode_ID);
            if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                //logged in user :-
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                CheckoutAddressList m_CheckoutAddressList = new CheckoutAddressList();
                Bundle mBundle = new Bundle();
                mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_CheckOutAddsBook);
                mBundle.putString("Vendor_id",mVendorID);
                m_CheckoutAddressList.setArguments(mBundle);
                mFT.replace(R.id.layout_app_check_out_body, m_CheckoutAddressList, "m_CheckoutAddressList");
                mFT.addToBackStack("m_CheckoutAddressList");
                mFT.commit();
            } else {
                toEditGuestAdds();
            }


        } else if (mId == R.id.tv_co_delivery_adds_not_available_add_adds) {

            if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                //logged in user:-
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                CheckoutAddressList m_CheckoutAddressList = new CheckoutAddressList();
                Bundle mBundle = new Bundle();
                mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_CheckOutAddsBook);
                mBundle.putString("Vendor_id",mVendorID);
                m_CheckoutAddressList.setArguments(mBundle);
                mFT.replace(R.id.layout_app_check_out_body, m_CheckoutAddressList, "m_CheckoutAddressList");
                mFT.addToBackStack("m_CheckoutAddressList");
                mFT.commit();
            } else {
                //guest user :-
                //*********************************************************************************
                //AppFunctions.msgDialogOk(getActivity(), "", "add address");
                //The AddressBookChangeLocationDB is used for temporary map page lat long and its address storage
                //purposes.So whenever we go for CheckoutAddressAddEdit page.then reset the AddressBookChangeLocationDB
                // details.
                if (AddressBookChangeLocationDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    AddressBookChangeLocationDB.getInstance(getActivity()).deleteDB();
                }
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                GuestAddressAddEdit m_guestAddressAddEdit = new GuestAddressAddEdit();
                Bundle mBundle = new Bundle();
                mBundle.putString(DefaultNames.address_add_or_edit, DefaultNames.address_add_process);
                mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_CheckOutAddsBook);
                mBundle.putString(DefaultNames.vendor_id, mVendorID);
                mBundle.putString("order_type", order_type);
                mBundle.putString("payment_list_id", mSelectedPMCode_ID);
                String contactless_delivery;
                if (mCheckOutBinding.checkBoxCoContactlessDelivery.isChecked()) {
                    contactless_delivery = "1";
                } else {
                    contactless_delivery = "0";
                }
                mBundle.putString("contactless_delivery", contactless_delivery);
                m_guestAddressAddEdit.setArguments(mBundle);
                mFT.replace(R.id.layout_app_check_out_body, m_guestAddressAddEdit, "m_guestAddressAddEdit");
                mFT.addToBackStack("m_guestAddressAddEdit");
                mFT.commit();
                //*********************************************************************************
            }
        } else if (mId == R.id.lay_cg_otp_submit_btn_container) {
            if (getActivity() != null) {
                String mOTP = mCheckOutBinding.etCgOtp.getText().toString();

                if (!mOTP.isEmpty()) {
                    toHideDeviceKeyBoard();
                    mProgressDialog.show();
                    verifyPhoneNumberWithCode(mVerificationId, mOTP);
                } else {
                    toHideDeviceKeyBoard();
                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_otp));
                }

            }
        } else if (mId == R.id.lay_cg_otp_resend) {
            if (getActivity() != null) {
                mCheckOutBinding.layCgOtpResend.setVisibility(View.GONE);
                mCheckOutBinding.tvCgOtpDidNotReceive.setVisibility(View.VISIBLE);
                mProgressDialog.show();
                resendVerificationCode(mMOBNoWithCCodeWithPlus, mResendToken);
            }
        }


    }

    private void toHideDeviceKeyBoard() {

        if (getActivity() != null) {
            if (mCheckOutBinding != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                // To get the correct window token, lets first get the currently focused view
                View v__iew = mCheckOutBinding.getRoot();
                // To get the window token when there is no currently focused view, we have a to create a view
                if (v__iew == null) {
                    v__iew = new View(getActivity());
                }
                // hide the keyboard
                imm.hideSoftInputFromWindow(v__iew.getWindowToken(), 0);
            }
        }
    }

    private void toEditGuestAdds() {
        if (mPaymentMethodsApi != null) {
            if (mPaymentMethodsApi.checkOutAdds != null) {
                CheckOutAddsDataSet mCOAddsDs = mPaymentMethodsApi.checkOutAdds;

                //If address id present then there is delivery address present.
                //If address id is empty then consider there is no delivery address selected before.
                if (mCOAddsDs.getAddress_id() != null && !mCOAddsDs.getAddress_id().isEmpty()) {

                    //guest user :-
                    //*********************************************************************************
                    //AppFunctions.msgDialogOk(getActivity(), "", "add address");
                    //The AddressBookChangeLocationDB is used for temporary map page lat long and its address storage
                    //purposes.So whenever we go for CheckoutAddressAddEdit page.then reset the AddressBookChangeLocationDB
                    // details.

                    if (AddressBookChangeLocationDB.getInstance(getActivity()).getSizeOfList() > 0) {
                        AddressBookChangeLocationDB.getInstance(getActivity()).deleteDB();
                    }

                    //*********************************************************************************

                    String mFName = mCOAddsDs.getFirst_name();
                    String mLName = mCOAddsDs.getLast_name();
                    String mName = mFName + " " + mLName;

                    String mArea = mCOAddsDs.getArea();
                    String mArea_id_or_zone_id = mCOAddsDs.getZone_id();

                    String mBlock = mCOAddsDs.getBlock();
                    String mStreet = mCOAddsDs.getStreet();
                    String mBuilding = mCOAddsDs.getBuilding_name();
                    String mWay = mCOAddsDs.getWay();
                    String mFloor = mCOAddsDs.getFloor();
                    String mDoorNo = mCOAddsDs.getDoor_no();
                    String mMob = getActivity().getResources().getString(R.string.mobile) + ": " + mCOAddsDs.getCountry_code() + "-" +
                            mCOAddsDs.getMobile();

                    String mCountry_code = mCOAddsDs.getCountry_code();
                    String mMobile = mCOAddsDs.getMobile();

                    String mEMAIL = mCOAddsDs.getEmail();

                    String mLatitude = mCOAddsDs.getLatitude();
                    String mLongitude = mCOAddsDs.getLongitude();
                    String mAddressId = mCOAddsDs.getAddress_id();

                    String landline = mCOAddsDs.getLandline();
                    String address_type = mCOAddsDs.getAddress_type();
                    String additional_direction = mCOAddsDs.getAdditional_direction();

                    //   Log.e("345 mAddressId", "" + mAddressId);
                    //   Log.e("346 Latitude", "" + mLatitude);
                    //  Log.e("347 Longitude", "" + mLongitude);
                    //  Log.e("348 address_type", "" + address_type);

                    AreaGeoCodeDataSet mAreaDS = new AreaGeoCodeDataSet();
                    mAreaDS.setmAddress("");
                    mAreaDS.setmLatitude(String.valueOf(mLatitude));
                    mAreaDS.setmLongitude(String.valueOf(mLongitude));
                    mAreaDS.setmAddsNameOnly("");
                    AddressBookChangeLocationDB.getInstance(getActivity()).add(mAreaDS);

                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    GuestAddressAddEdit m_guestAddressAddEdit = new GuestAddressAddEdit();
                    Bundle mBundle = new Bundle();
                    mBundle.putString(DefaultNames.address_add_or_edit, DefaultNames.address_edit_process);
                    mBundle.putString(DefaultNames.addressBook_callFrom, DefaultNames.callFor_CheckOutAddsBook);
                    mBundle.putString(DefaultNames.vendor_id, mVendorID);

                    mBundle.putString(DefaultNames.firstName, mFName);
                    mBundle.putString(DefaultNames.lastName, mLName);
                    mBundle.putString(DefaultNames.area, mArea);
                    mBundle.putString(DefaultNames.zone_id, mArea_id_or_zone_id);
                    mBundle.putString(DefaultNames.block, mBlock);
                    mBundle.putString(DefaultNames.street, mStreet);
                    mBundle.putString(DefaultNames.building, mBuilding);
                    mBundle.putString(DefaultNames.way, mWay);
                    mBundle.putString(DefaultNames.floor, mFloor);
                    mBundle.putString(DefaultNames.door_no, mDoorNo);
                    mBundle.putString(DefaultNames.country_code, mCountry_code);
                    mBundle.putString(DefaultNames.mobile, mMobile);
                    mBundle.putString(DefaultNames.email, mEMAIL);
                    mBundle.putString(DefaultNames.latitude, mLatitude);
                    mBundle.putString(DefaultNames.longitude, mLongitude);
                    mBundle.putString(DefaultNames.address_id, mAddressId);
                    mBundle.putString(DefaultNames.landline, landline);
                    mBundle.putString(DefaultNames.address_type, address_type);
                    mBundle.putString(DefaultNames.additional_direction, additional_direction);
                    m_guestAddressAddEdit.setArguments(mBundle);
                    mFT.replace(R.id.layout_app_check_out_body, m_guestAddressAddEdit, "m_guestAddressAddEdit");
                    mFT.addToBackStack("m_guestAddressAddEdit");
                    mFT.commit();

                    //***********************************************************************************
                }
            }
        }
    }

    private void toCallCouponUI() {

        //coupon not applied.So to show apply ui :-
        //AppFunctions.msgDialogOk(getActivity(), "", "Add coupon");
        DialogueCouponCode mDialogueCouponCode = new DialogueCouponCode();
        mDialogueCouponCode.setTargetFragment(CheckOut.this, 19845);
        Bundle mBundle = new Bundle();
        mBundle.putString(DefaultNames.vendor_id, mVendorID);
        mDialogueCouponCode.setArguments(mBundle);
        mDialogueCouponCode.show(getParentFragmentManager(), "mDialogueCouponCode");

    }

    @Override
    public void onResume() {
        super.onResume();
        callPaymentMethodsListApi();
        if (getActivity() != null) {
            if (CheckOutDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                CheckOutDBDataSet mCheckOutDBDs = CheckOutDetailsDB.getInstance(getActivity()).getDetails();
                if (mCheckOutDBDs.getCouponCode() != null && !mCheckOutDBDs.getCouponCode().isEmpty()) {
                    mCheckOutBinding.tvCoCouponAddCoupon.setText(getActivity().getResources().getString(R.string.remove_coupon));
                    mCouponCODE_Id = mCheckOutDBDs.getCouponId();
                    mCouponCODE = mCheckOutDBDs.getCouponCode();
                }
            }
            if (!AppFunctions.isUserLoggedIn(getActivity())) {
                //Its a guest user :-
                if (simpleCountDownTimer != null) {
                    simpleCountDownTimer.start(true);
                }
            }
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        if (!AppFunctions.isUserLoggedIn(getActivity())) {
            //Its a guest user :-
            if (simpleCountDownTimer != null) {
                simpleCountDownTimer.pause();
            }
        }
    }

    private void loadRestaurantLocationMap() {

        if (getActivity() != null) {

            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    //.addConnectionCallbacks(Checkout.this)
                    //.addOnConnectionFailedListener(CheckoutNew.this)
                    .addApi(LocationServices.API)
                    .build();

            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(5000)        // 5 seconds, in milliseconds
                    .setFastestInterval(5000); // 5 second, in milliseconds

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

        if (mSelectedAddsLatitude != null && mSelectedAddsLongitude != null && !mSelectedAddsLatitude.isEmpty() && !mSelectedAddsLongitude.isEmpty()) {
            double m__Latitude = Double.parseDouble(mSelectedAddsLatitude);
            double m__Longitude = Double.parseDouble(mSelectedAddsLongitude);

            //  //Log.e("initialCameraPosition: ", mLatitude + "/1");
            //  //Log.e("initialCameraPosition: ", mLongitude + "/2");
            //  //Log.e("initialCameraPosition: ", m__Longitude + "/3");
            //  //Log.e("initialCameraPosition: ", m__Latitude + "/4");

            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(m__Latitude, m__Longitude), 16.00f));

            mGoogleMap.setMapType(MAP_TYPES[curMapTypeIndex]);
            mGoogleMap.setTrafficEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(m__Latitude, m__Longitude))
                    //.title(mAddressGeocodeDs.getRestaurantName())
                    //.snippet(mAddressGeocodeDs.getAddress())
                    // .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_24dp))
            );

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

    private void callCartListAPi(Boolean isForCouponApply) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put(DefaultNames.coupon_id, mCouponCODE_Id);
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                    jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                    jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }

                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<CheckoutCartListApi> Call = retrofitInterface.checkoutCartListApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<CheckoutCartListApi>() {
                        @Override
                        public void onResponse(@NonNull Call<CheckoutCartListApi> call, @NonNull Response<CheckoutCartListApi> response) {

                            if (getActivity() != null) {
                                mProgressDialog.cancel();
                                if (response.isSuccessful()) {
                                    mCCartListApi = response.body();
                                    if (mCCartListApi != null) {
                                        //Log.e("mCCartListApi", "not null");
                                        if (mCCartListApi.success != null) {
                                            //Api response successDataSet :-

                                            mVendorID = mCCartListApi.vendor_id;
                                            mVendorName = mCCartListApi.vendor_name;
                                            mCheckOutBinding.tvCoAppBarVendorName.setText(mVendorName);

                                            if (order_type.equals("2")) {
                                                mCheckOutBinding.tvCoDeliveryAddsName.setText(mCCartListApi.vendor_address);
                                                mCheckOutBinding.tvCoDeliveryAddsMain.setVisibility(View.GONE);
                                                mCheckOutBinding.tvCoDeliveryAddsSub.setVisibility(View.GONE);
                                                mCheckOutBinding.tvCoDeliveryAddsSubMobile.setText(mCCartListApi.vendor_mobile);

                                                mSelectedAddsLatitude = mCCartListApi.restaurant_latitude;
                                                mSelectedAddsLongitude = mCCartListApi.restaurant_longitude;

                                                loadRestaurantLocationMap();
                                                //Google map initializer and connection :-
                                                restaurantLocationMapInitializerAndConnect(mCheckOutBinding.mapViewCoDeliveryLocation);
                                            }

                                            //The following call for coupon apply process.To show coupon apply success only
                                            //if cart api success after coupon apply success.
                                            if (isForCouponApply) {
                                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.coupon_applied_successfully));
                                                // CheckOutDBDataSet checkOutDBDs = new CheckOutDBDataSet();
                                                // checkOutDBDs.setCouponId(mCouponCODE_Id);
                                                // checkOutDBDs.setCouponCode(mCouponCODE);
                                                CheckOutDetailsDB.getInstance(getActivity()).updateCouponId(mCouponCODE_Id);
                                                CheckOutDetailsDB.getInstance(getActivity()).updateCouponCode(mCouponCODE);
                                                mCheckOutBinding.tvCoCouponAddCoupon.setText(getActivity().getResources().getString(R.string.remove_coupon));
                                            }
                                            if (mCCartListApi.totalsList != null && mCCartListApi.totalsList.size() > 0) {

                                                mCLTotalsLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                                mCheckOutBinding.recyclerCheckoutTotalList.setLayoutManager(mCLTotalsLayoutMgr);
                                                mCheckOutTotalsListAdapter = new CheckOutTotalsListAdapter(mCCartListApi.totalsList);
                                                mCheckOutBinding.recyclerCheckoutTotalList.setAdapter(mCheckOutTotalsListAdapter);
                                                if (mCCartListApi.error_warning != null && !mCCartListApi.error_warning.isEmpty()) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", mCCartListApi.error_warning);
                                                    mCheckOutBinding.layCoPlaceOrderBtnContainer.setEnabled(false);
                                                    mCheckOutBinding.layCoPlaceOrderBtnContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_new_req_btn_color_cart_checkout_disable));
                                                    // Log.e("1009", "called");
                                                } else {
                                                    // Log.e("1011", "called");
                                                    //Here we must confirm is payment methods available or not.
                                                    //If available then enable checkout button or else disable checkout button.
                                                    if (mIsThereIsNoPaymentMethodsAvailable) {
                                                        mCheckOutBinding.layCoPaymentListEmptyMsg.setVisibility(View.VISIBLE);
                                                        mCheckOutBinding.layCoPlaceOrderBtnContainer.setEnabled(false);
                                                        mCheckOutBinding.layCoPlaceOrderBtnContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_new_req_btn_color_cart_checkout_disable));
                                                    } else {
                                                        mCheckOutBinding.layCoPaymentListEmptyMsg.setVisibility(View.GONE);
                                                        mCheckOutBinding.layCoPlaceOrderBtnContainer.setEnabled(true);
                                                        mCheckOutBinding.layCoPlaceOrderBtnContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_new_req_btn_color_cart_checkout));

                                                    }
                                                }
                                            }
                                        } else {
                                            //Api response failure :-
                                            if (mCCartListApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mCCartListApi.error.message);
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
                        public void onFailure(@NonNull Call<CheckoutCartListApi> call, @NonNull Throwable t) {

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
                mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();

            }
        }


    }

    @Override
    public void reload() {
        mCheckOutBinding.deliveryTimeTxt.setText(delivery_time);
        mCheckOutBinding.scheduleBtn.setText(activity.getResources().getString(R.string.change));
    }

    public class CheckOutTotalsListAdapter extends RecyclerView.Adapter<CheckOutTotalsListAdapter.ViewHolder> {

        private ArrayList<CartTotalsDataSet> mTotalsList;

        public void toUpdateCartTotalsList(ArrayList<CartTotalsDataSet> totalsList) {
            this.mTotalsList = totalsList;
        }

        public CheckOutTotalsListAdapter(ArrayList<CartTotalsDataSet> totalsList) {
            this.mTotalsList = totalsList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.checkout_totals_row, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mCartTotalsTitle.setText(mTotalsList.get(position).getTitle());
//            holder.mCartTotalsData.setText(mTotalsList.get(position).getText());

            if (LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId().equals("1")) {
                holder.mCartTotalsData.setText(mTotalsList.get(position).getText());
                holder.mCartTotalsData.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            } else {
                holder.mCartTotalsData.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                holder.mCartTotalsData.setText(mTotalsList.get(position).getAmount() + "  " + mTotalsList.get(position).getCurrency());
            }

            //To make Total field only bold and remaining fields are non bold:-
            String mTitleKey = mTotalsList.get(position).getTitle_key();
            if (mTitleKey.equals("total")) {
                holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.BOLD);
                holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.BOLD);
                holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));
                holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));
            } else if (mTitleKey.equals("offer")) {
                holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
            } else if (mTitleKey.equals("coupon")) {
                holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
                holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.colorPrimary));
            } else {
                holder.mCartTotalsTitle.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                holder.mCartTotalsData.setTypeface(holder.mCartTotalsTitle.getTypeface(), Typeface.NORMAL);
                holder.mCartTotalsTitle.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));
                holder.mCartTotalsData.setTextColor(getActivity().getResources().getColor(R.color.ar_filter_title_text_color));
            }

            if (mTitleKey.equals("sub_total")) {
                mSubTotalAmt = mTotalsList.get(position).getText_amount();
            }
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return mTotalsList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView mCartTotalsTitle, mCartTotalsData;
            LinearLayout tv_cl_total_data_linear;

            public ViewHolder(View itemView) {
                super(itemView);
                mCartTotalsTitle = itemView.findViewById(R.id.tv_ct_total_title);
                tv_cl_total_data_linear = itemView.findViewById(R.id.tv_cl_total_data_linear);
                mCartTotalsData = itemView.findViewById(R.id.tv_ct_total_data);
            }
        }
    }

    private void callPaymentMethodsListApi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                try {
                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);

                    JSONObject jsonObject = new JSONObject();
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();

                    jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                    jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    jsonObject.put("vendor_id", mVendorID);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                    Call<PaymentMethodsApi> Call = retrofitInterface.paymentMethodsApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<PaymentMethodsApi>() {
                        @Override
                        public void onResponse(@NonNull Call<PaymentMethodsApi> call, @NonNull Response<PaymentMethodsApi> response) {

                            if (getActivity() != null) {
                                if (response.isSuccessful()) {
                                    mPaymentMethodsApi = response.body();

                                    if (mPaymentMethodsApi != null) {
                                        //Log.e("mPaymentMethodsApi", "not null");
                                        if (mPaymentMethodsApi.success != null) {

                                            Log.e("paymentMehod ","");

                                            order_type = mPaymentMethodsApi.order_type;

                                            if (!mPaymentMethodsApi.error_warning.isEmpty()) {
                                                CheckOutDetailsDB.getInstance(getActivity()).updateCouponCode("");
                                                CheckOutDetailsDB.getInstance(getActivity()).updateCouponId("");
                                                mCheckOutBinding.tvCoCouponAddCoupon.setText(getActivity().getResources().getString(R.string.add_coupon));
                                                mCouponCODE_Id = "";
                                                mCouponCODE = "";
                                            }

                                            if (order_type.equals("2")) {
                                                mCheckOutBinding.confirmDeliveryAddressTxt.setVisibility(View.GONE);
                                                mCheckOutBinding.approximatelyTxt.setText(activity.getResources().getString(R.string.approximately_pickup_time_is));
                                                mCheckOutBinding.layCoContactlessDeliveryParent.setVisibility(View.GONE);
                                            } else {
                                                mCheckOutBinding.confirmDeliveryAddressTxt.setVisibility(View.VISIBLE);
                                                mCheckOutBinding.approximatelyTxt.setText(activity.getResources().getString(R.string.approximately_delivery_time_is));
                                                mCheckOutBinding.layCoContactlessDeliveryParent.setVisibility(View.VISIBLE);
                                            }
                                            mCheckOutBinding.deliveryTimeTxt.setText(mPaymentMethodsApi.delivery_time + " " + activity.getResources().getString(R.string.minutes));

                                            if (mPaymentMethodsApi.schedule_status.equals("1")) {
                                                if (mPaymentMethodsApi.schedule != null) {
                                                    scheduleDeliveryDataSet = mPaymentMethodsApi.schedule;
                                                    if (scheduleDeliveryDataSet.date != null && scheduleDeliveryDataSet.date.size() > 0) {
                                                        mCheckOutBinding.scheduleBtn.setVisibility(View.VISIBLE);
                                                    } else {
                                                        mCheckOutBinding.scheduleBtn.setVisibility(View.GONE);
                                                    }
                                                }
                                                mCheckOutBinding.scheduleBtn.setVisibility(View.VISIBLE);
                                            } else {
                                                mCheckOutBinding.scheduleBtn.setVisibility(View.GONE);
                                            }

                                            if (order_type.equals("1")) {
                                                //Api response successDataSet :-
                                                if (mPaymentMethodsApi.checkOutAdds != null) {
                                                    CheckOutAddsDataSet mCOAddsDs = mPaymentMethodsApi.checkOutAdds;
                                                    //If address id present then there is delivery address present.
                                                    //If address id is empty then consider there is no delivery address selected before.
                                                    if (mCOAddsDs.getAddress_id() != null && !mCOAddsDs.getAddress_id().isEmpty()) {

                                                        mCheckOutBinding.tvCoDeliveryAddsEdit.setVisibility(View.VISIBLE);

                                                        mSelectedDeliveryAddsId = mCOAddsDs.getAddress_id();

                                                        mSelectedAddsLatitude = mCOAddsDs.getLatitude();
                                                        mSelectedAddsLongitude = mCOAddsDs.getLongitude();
                                                        mSelectedAddsZoneId = mCOAddsDs.getZone_id();

                                                        loadRestaurantLocationMap();
                                                        //Google map initializer and connection :-
                                                        restaurantLocationMapInitializerAndConnect(mCheckOutBinding.mapViewCoDeliveryLocation);

                                                        mCheckOutBinding.layCoDeliveryAddsAvailableHolder.setVisibility(View.VISIBLE);
                                                        mCheckOutBinding.layCoDeliveryAddsNotAvailableHolder.setVisibility(View.GONE);

                                                        String mFName = mCOAddsDs.getFirst_name();
                                                        String mLName = mCOAddsDs.getLast_name();
                                                        String mName = mFName + " " + mLName;
                                                        String mAdds = "";

                                                        String mBlock = mCOAddsDs.getBlock();
                                                        String mStreet = mCOAddsDs.getStreet();
                                                        String mBuilding = mCOAddsDs.getBuilding_name();
                                                        String mWay = mCOAddsDs.getWay();
                                                        String mFloor = mCOAddsDs.getFloor();
                                                        String mDoorNo = mCOAddsDs.getDoor_no();
                                                        String mMob = getActivity().getResources().getString(R.string.mobile) + ": " + mCOAddsDs.getCountry_code() + "-" + mCOAddsDs.getMobile();

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

                                                        //  Log.e("885","mCOAddsDs.getAddress_type() "+mCOAddsDs.getAddress_type());
                                                        if (mCOAddsDs.getAddress_type().equals("1")) {
                                                            //Its a house address type :-
//                                                            mAdds = getActivity().getResources().getString(R.string.caa_house) + " (" + mCOAddsDs.getArea() + ")";
                                                            mAdds =  mCOAddsDs.getAddress() ;
                                                        } else if (mCOAddsDs.getAddress_type().equals("2")) {
                                                            //Its a apartment address type :-
//                                                            mAdds = getActivity().getResources().getString(R.string.caa_apartment) + " (" + mCOAddsDs.getArea() + ")";
                                                            mAdds =  mCOAddsDs.getAddress() ;
                                                        } else {
                                                            //Its a office address type :-
//                                                            mAdds = getActivity().getResources().getString(R.string.caa_office) + " (" + mCOAddsDs.getArea() + ")";
                                                            mAdds =  mCOAddsDs.getAddress() ;
                                                        }

                                                        if (mCOAddsDs.getAddress_type().equals("2") || mCOAddsDs.getAddress_type().equals("3")) {
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
                                                        }

                                                        mCheckOutBinding.tvCoDeliveryAddsName.setText(mName);
                                                        mCheckOutBinding.tvCoDeliveryAddsMain.setText(mAdds);
                                                        mCheckOutBinding.tvCoDeliveryAddsSub.setText(mDeliveryAdds);
                                                        mCheckOutBinding.tvCoDeliveryAddsSubMobile.setText(mMob);

                                                    } else {
                                                        mCheckOutBinding.layCoDeliveryAddsAvailableHolder.setVisibility(View.GONE);
                                                        mCheckOutBinding.layCoDeliveryAddsNotAvailableHolder.setVisibility(View.VISIBLE);
                                                        disableCheckoutBtn();
                                                    }
                                                } else {
                                                    mCheckOutBinding.layCoDeliveryAddsAvailableHolder.setVisibility(View.GONE);
                                                    mCheckOutBinding.layCoDeliveryAddsNotAvailableHolder.setVisibility(View.VISIBLE);
                                                    disableCheckoutBtn();
                                                }
                                            } else {
                                                mCheckOutBinding.tvCoDeliveryAddsEdit.setVisibility(View.GONE);
                                                mCheckOutBinding.layCoDeliveryAddsAvailableHolder.setVisibility(View.VISIBLE);
                                                mCheckOutBinding.layCoDeliveryAddsNotAvailableHolder.setVisibility(View.GONE);
                                            }

                                            if (mPaymentMethodsApi.paymentMethodsList != null && mPaymentMethodsApi.paymentMethodsList.size() > 0) {
                                                mCheckOutBinding.layCoPlaceOrderBtnContainer.setEnabled(true);
                                                mCheckOutBinding.layCoPlaceOrderBtnContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_new_req_btn_color_cart_checkout));
                                                mPMLayoutMgr = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                                                mCheckOutBinding.recyclerCoPaymentMethods.setLayoutManager(mPMLayoutMgr);
                                                mPaymentMethodsListAdapter = new PaymentMethodsListAdapter(mPaymentMethodsApi.paymentMethodsList);
                                                mCheckOutBinding.recyclerCoPaymentMethods.setAdapter(mPaymentMethodsListAdapter);
                                                mCheckOutBinding.layCoPaymentListEmptyMsg.setVisibility(View.GONE);
                                            } else {
                                                mProgressDialog.cancel();
                                                disableCheckoutBtn();
                                                mCheckOutBinding.layCoPaymentListEmptyMsg.setVisibility(View.VISIBLE);
                                                mIsThereIsNoPaymentMethodsAvailable = true;
                                            }
                                            //just for UI showing purposes :-
                                            Log.e("paymentMehod ","");
                                            callCartListAPi(false);
                                        } else {

                                            mProgressDialog.cancel();

                                            mCheckOutBinding.layCoPaymentListEmptyMsg.setVisibility(View.VISIBLE);
                                            disableCheckoutBtn();
                                            //Api response failure :-
                                            if (mPaymentMethodsApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mPaymentMethodsApi.error.message);
                                            }
                                        }
                                    } else {

                                        mProgressDialog.cancel();
                                        mCheckOutBinding.layCoPaymentListEmptyMsg.setVisibility(View.VISIBLE);
                                        disableCheckoutBtn();
                                        //Log.e("mPaymentMethodsApi", "null");
                                    }
                                } else {
                                    mProgressDialog.cancel();
                                    mCheckOutBinding.layCoPaymentListEmptyMsg.setVisibility(View.VISIBLE);
                                    disableCheckoutBtn();
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
                        public void onFailure(@NonNull Call<PaymentMethodsApi> call, @NonNull Throwable t) {
                            disableCheckoutBtn();
                            mProgressDialog.cancel();
                            mCheckOutBinding.layCoPaymentListEmptyMsg.setVisibility(View.VISIBLE);

                        }
                    });

                } catch (Exception e) {

                    disableCheckoutBtn();
                    mProgressDialog.cancel();

                    //Log.e("210 Excep ", e.toString());
                    e.printStackTrace();

                }


            } else {

                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();

            }
        }


    }

    private void disableCheckoutBtn() {

        if (getActivity() != null) {

            //Log.e("1458", "called");

            mCheckOutBinding.layCoPlaceOrderBtnContainer.setEnabled(false);
            mCheckOutBinding.layCoPlaceOrderBtnContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_new_req_btn_color_cart_checkout_disable));
        }

    }

    public class PaymentMethodsListAdapter extends RecyclerView.Adapter<PaymentMethodsListAdapter.ViewHolder> {

        private ArrayList<PaymentDataSet> mPMList;

        public void toUpdatePaymentMethodsList(ArrayList<PaymentDataSet> pmList) {
            this.mPMList = pmList;
        }

        public PaymentMethodsListAdapter(ArrayList<PaymentDataSet> totalsList) {
            this.mPMList = totalsList;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.payment_methods_row, parent, false);
            return new ViewHolder(view);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            Log.e("mPmlist"+mPMList.size(),"");
            Log.e("mSelectedPM", "2onClick: "+mSelectedPMCode_ID+position);
            holder.mTitle.setText(mPMList.get(position).getPayment_name());
            holder.mRadioButton.setChecked(false);

            if (mPMList.get(position).getPayment_code().equals("cod")){
                holder.mImage1.setVisibility(View.GONE);
                holder.mImage.setVisibility(View.VISIBLE);
            }else {
                holder.mImage1.setVisibility(View.VISIBLE);
                holder.mImage.setVisibility(View.GONE);
            }
            CheckOutDetailsDB.getInstance(getActivity()).print();

            String mCurrentPaymentListId = mPMList.get(position).getPayment_list_id();
            String mCurrentPaymentCode = mPMList.get(position).getPayment_list_id();

            String mAlreadySelectedPM = CheckOutDetailsDB.getInstance(getActivity()).getDetails().getPaymentListId();
            if (mAlreadySelectedPM != null && !mAlreadySelectedPM.isEmpty() && mAlreadySelectedPM.equalsIgnoreCase(mCurrentPaymentListId)) {

                //Log.e("mAlreadySelectedPM = "+mAlreadySelectedPM,"mCurrentPaymentListId = "+mCurrentPaymentListId);
                if (mCurrentPaymentCode.equals("cod")) {
                    // Log.e("1478 ","cod : Pos-"+position);
                    //If Contactless delivery is checked, then cod must be disabled.
                    if (!mCheckOutBinding.checkBoxCoContactlessDelivery.isChecked()) {
                        // Log.e("1483 ","called : Pos-"+position);
                        if (holder.mRadioButton.isChecked()) {
                            //  holder.mRadioButton.setChecked(false);
                            //   Log.e("1487 ","called : Pos-"+position);
                        } else {
                            //  Log.e("1490 ","called : Pos-"+position);
                            mSelectedPMCode = mPMList.get(position).getPayment_code();
                            mSelectedPMCode_ID = mPMList.get(position).getPayment_list_id();
                            Log.e("mSelectedPM", "3onClick: "+mSelectedPMCode_ID);
                            holder.mRadioButton.setChecked(true);
                            if (mSelectedPMRadioBtn == null) {
                                mSelectedPMRadioBtn = holder.mRadioButton;
                                mSelectedPMTvTitle = holder.mTitle;
                                mSelectedPMLayBorder = holder.mLayBorder;
                            } else {
                                mSelectedPMRadioBtn.setChecked(false);
                                mSelectedPMRadioBtn = holder.mRadioButton;
                                mSelectedPMTvTitle = holder.mTitle;
                                mSelectedPMLayBorder = holder.mLayBorder;
                            }
                        }
                    } else {
                        //  Log.e("1509 ","called : Pos-"+position);
                        holder.mRadioButton.setChecked(false);
                    }
                } else {
                    //  Log.e("1514 ","other : Pos-"+position);
                    if (holder.mRadioButton.isChecked()) {
                        //  holder.mRadioButton.setChecked(false);
                        //   Log.e("1518 ","called : Pos-"+position);
                    } else {
                        //  Log.e("1521 ","called : Pos-"+position);
                        mSelectedPMCode = mPMList.get(position).getPayment_code();
                        mSelectedPMCode_ID = mPMList.get(position).getPayment_list_id();
                        Log.e("mSelectedPM", "4onClick: "+mSelectedPMCode_ID + position);
                        holder.mRadioButton.setChecked(true);

                        if (mSelectedPMRadioBtn == null) {
                            mSelectedPMRadioBtn = holder.mRadioButton;
                            mSelectedPMTvTitle = holder.mTitle;
                            mSelectedPMLayBorder = holder.mLayBorder;
                        } else {
                            mSelectedPMRadioBtn.setChecked(false);
                            mSelectedPMRadioBtn = holder.mRadioButton;
                            mSelectedPMTvTitle = holder.mTitle;
                            mSelectedPMLayBorder = holder.mLayBorder;
                        }

                    }
                }
            } else {
                // Log.e("1542 current pm id ","not match : Pos-"+position);
                holder.mRadioButton.setChecked(false);
            }

            String mCurrentPMCode = mPMList.get(position).getPayment_code();
            if (mCurrentPMCode != null && !mCurrentPMCode.isEmpty() && mCurrentPMCode.equalsIgnoreCase("cod")) {
                // Log.e("1548", "called");
                if (getActivity() != null) {
                    if (getActivity().getResources() != null) {
                        if (!mCheckOutBinding.checkBoxCoContactlessDelivery.isChecked()) {
//                            holder.mLayBorder.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_co_ui_split_box));
                            holder.mRadioButton.setEnabled(true);
                            // holder.mRadioButton.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.colorPrimary)));
                            // holder.mRadioButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorPrimary));
                            holder.mRadioButton.setButtonTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorPrimary));
                            holder.mTitle.setTextColor(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.text_color)));
                            //   Log.e("1559", "called");
//                            holder.radio_btn_uncheck.setVisibility(View.VISIBLE);
//                            holder.radio_btn_check.setVisibility(View.GONE);
//                            holder.radio_btn_disable.setVisibility(View.GONE);
                            mCheckOutBinding.layCoUnCheckCldForCodEnableParent.setVisibility(View.GONE);
                        } else {
                            //  Log.e("1562", "called");
//                            holder.mLayBorder.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.bg_co_ui_split_box_disable));
                            // holder.mRadioButton.setBackgroundTintList(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.co_pm_text_disable_color)));
                            // holder.mRadioButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.co_pm_text_disable_color));
                            holder.mRadioButton.setButtonTintList(ContextCompat.getColorStateList(getActivity(), R.color.co_pm_text_disable_color));
                            holder.mTitle.setTextColor(ColorStateList.valueOf(getActivity().getResources().getColor(R.color.co_pm_text_disable_color)));
//                            holder.radio_btn_uncheck.setVisibility(View.GONE);
//                            holder.radio_btn_check.setVisibility(View.GONE);
//                            holder.radio_btn_disable.setVisibility(View.VISIBLE);
                            holder.mRadioButton.setEnabled(false);
                            contactlessDelivery = true;
                            mCheckOutBinding.layCoUnCheckCldForCodEnableParent.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }


//            holder.mRadioButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    int position = holder.getAdapterPosition();
//                    if (position != RecyclerView.NO_POSITION) {
//                        selectedPosition = position;
//                        notifyDataSetChanged();
//                    }
//
//                    String m_Selected_PMCode = mPMList.get(position).getPayment_code();
//
//                    selected_payment = mPMList.get(position).getPayment_code();
//
//                    if (m_Selected_PMCode.equalsIgnoreCase("cod")) {
//                        mCheckOutBinding.mobilePayment.setVisibility(View.GONE);
//                        //If Contactless delivery is checked, then cod must be disabled.
//                        if (!mCheckOutBinding.checkBoxCoContactlessDelivery.isChecked()) {
//
//                            if (holder.mRadioButton.isChecked()) {
////                                  holder.mRadioButton.setChecked(false);
//                                //    Log.e("1591", "called");
//                            } else {
//                                //   Log.e("1593", "called");
//                                mSelectedPMCode = mPMList.get(position).getPayment_code();
//                                mSelectedPMCode_ID = mPMList.get(position).getPayment_list_id();
//                                Log.e("mSelectedPM", "5onClick: "+mSelectedPMCode_ID);
//                                holder.mRadioButton.setChecked(true);
//
//                                CheckOutDetailsDB.getInstance(getActivity()).updatePaymentListId(mSelectedPMCode_ID);
//                                CheckOutDetailsDB.getInstance(getActivity()).updatePaymentCode(mSelectedPMCode);
//
//                                if (mSelectedPMRadioBtn == null) {
//                                    //    Log.e("1602", "called");
//                                    mSelectedPMRadioBtn = holder.mRadioButton;
//                                    mSelectedPMTvTitle = holder.mTitle;
//                                    mSelectedPMLayBorder = holder.mLayBorder;
//                                } else {
//                                    //  Log.e("1607", "called");
//                                    mSelectedPMRadioBtn.setChecked(false);
//                                    mSelectedPMRadioBtn = holder.mRadioButton;
//                                    mSelectedPMTvTitle = holder.mTitle;
//                                    mSelectedPMLayBorder = holder.mLayBorder;
//                                }
//
//                                if (mSelectedPMCode != null && !mSelectedPMCode.isEmpty() && mSelectedPMCode.equalsIgnoreCase("cod")) {
//                                    mCheckOutBinding.checkBoxCoContactlessDelivery.setChecked(false);
//                                    //    Log.e("1616", "called");
//                                } else {
//                                    //   Log.e("1618", "called");
//                                }
//                            }
//                        }
//                    } else {
//                        mCheckOutBinding.mobilePayment.setVisibility(View.VISIBLE);
//                        if (holder.mRadioButton.isChecked()) {
//                            //  holder.mRadioButton.setChecked(false);
//                            // Log.e("1628", "called");
//                        } else {
//                            //  Log.e("1630", "called");
//                            mSelectedPMCode = mPMList.get(position).getPayment_code();
//                            mSelectedPMCode_ID = mPMList.get(position).getPayment_list_id();
//                            Log.e("mSelectedPM", "6onClick: "+mSelectedPMCode_ID);
//                            holder.mRadioButton.setChecked(true);
//
//                            CheckOutDetailsDB.getInstance(getActivity()).updatePaymentListId(mSelectedPMCode_ID);
//                            CheckOutDetailsDB.getInstance(getActivity()).updatePaymentCode(mSelectedPMCode);
//
//                            if (mSelectedPMRadioBtn == null) {
//                                //   Log.e("1693", "called");
//                                mSelectedPMRadioBtn = holder.mRadioButton;
//                                mSelectedPMTvTitle = holder.mTitle;
//                                mSelectedPMLayBorder = holder.mLayBorder;
//                            } else {
//                                //   Log.e("1644", "called");
//                                mSelectedPMRadioBtn.setChecked(false);
//                                mSelectedPMRadioBtn = holder.mRadioButton;
//                                mSelectedPMTvTitle = holder.mTitle;
//                                mSelectedPMLayBorder = holder.mLayBorder;
//                            }
//                            if (mSelectedPMCode != null && !mSelectedPMCode.isEmpty() && mSelectedPMCode.equalsIgnoreCase("cod")) {
//                                mCheckOutBinding.checkBoxCoContactlessDelivery.setChecked(false);
//                                //    Log.e("1652", "called");
//                            } else {
//                                //    Log.e("1654", "called");
//                            }
//                        }
//                    }
//                    CheckOutDetailsDB.getInstance(getActivity()).print();
//                }
//            });
            holder.mLayRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        selectedPosition = position;
                        notifyDataSetChanged();
                    }
//                        holder.radio_btn_uncheck.setVisibility(View.GONE);
//                        holder.radio_btn_check.setVisibility(View.VISIBLE);


                    selected_payment = mPMList.get(position).getPayment_code();
                    String m_Selected_PMCode = mPMList.get(position).getPayment_code();
                    if (m_Selected_PMCode.equalsIgnoreCase("cod")) {
                        mCheckOutBinding.mobilePayment.setVisibility(View.GONE);
                        //If Contactless delivery is checked, then cod must be disabled.
                        if (!mCheckOutBinding.checkBoxCoContactlessDelivery.isChecked()) {

                            if (holder.mRadioButton.isChecked()) {
//                                  holder.mRadioButton.setChecked(false);
                                //    Log.e("1591", "called");
                            }else {
                                //   Log.e("1593", "called");
                                mSelectedPMCode = mPMList.get(position).getPayment_code();
                                mSelectedPMCode_ID = mPMList.get(position).getPayment_list_id();
                                Log.e("mSelectedPM", "5onClick: "+mSelectedPMCode_ID);
                                holder.mRadioButton.setChecked(true);

                                CheckOutDetailsDB.getInstance(getActivity()).updatePaymentListId(mSelectedPMCode_ID);
                                CheckOutDetailsDB.getInstance(getActivity()).updatePaymentCode(mSelectedPMCode);

                                if (mSelectedPMRadioBtn == null) {
                                    //    Log.e("1602", "called");
                                    mSelectedPMRadioBtn = holder.mRadioButton;
                                    mSelectedPMTvTitle = holder.mTitle;
                                    mSelectedPMLayBorder = holder.mLayBorder;
                                } else {
                                    //  Log.e("1607", "called");
                                    mSelectedPMRadioBtn.setChecked(false);
                                    mSelectedPMRadioBtn = holder.mRadioButton;
                                    mSelectedPMTvTitle = holder.mTitle;
                                    mSelectedPMLayBorder = holder.mLayBorder;
                                }

                                if (mSelectedPMCode != null && !mSelectedPMCode.isEmpty() && mSelectedPMCode.equalsIgnoreCase("cod")) {
                                    mCheckOutBinding.checkBoxCoContactlessDelivery.setChecked(false);
                                    //    Log.e("1616", "called");
                                } else {
                                    //   Log.e("1618", "called");
                                }
                            }
                        }
                    } else {
                        mCheckOutBinding.mobilePayment.setVisibility(View.VISIBLE);
                        if (holder.mRadioButton.isChecked()) {
                            //  holder.mRadioButton.setChecked(false);
                            // Log.e("1628", "called");
                        } else {
                            //  Log.e("1630", "called");
                            mSelectedPMCode = mPMList.get(position).getPayment_code();
                            mSelectedPMCode_ID = mPMList.get(position).getPayment_list_id();
                            Log.e("mSelectedPM", "6onClick: "+mSelectedPMCode_ID);
                            holder.mRadioButton.setChecked(true);

                            CheckOutDetailsDB.getInstance(getActivity()).updatePaymentListId(mSelectedPMCode_ID);
                            CheckOutDetailsDB.getInstance(getActivity()).updatePaymentCode(mSelectedPMCode);

                            if (mSelectedPMRadioBtn == null) {
                                //   Log.e("1693", "called");
                                mSelectedPMRadioBtn = holder.mRadioButton;
                                mSelectedPMTvTitle = holder.mTitle;
                                mSelectedPMLayBorder = holder.mLayBorder;
                            } else {
                                //   Log.e("1644", "called");
                                mSelectedPMRadioBtn.setChecked(false);
                                mSelectedPMRadioBtn = holder.mRadioButton;
                                mSelectedPMTvTitle = holder.mTitle;
                                mSelectedPMLayBorder = holder.mLayBorder;
                            }
                            if (mSelectedPMCode != null && !mSelectedPMCode.isEmpty() && mSelectedPMCode.equalsIgnoreCase("cod")) {
                                mCheckOutBinding.checkBoxCoContactlessDelivery.setChecked(false);
                                //    Log.e("1652", "called");
                            } else {
                                //    Log.e("1654", "called");
                            }
                        }
                    }
                    CheckOutDetailsDB.getInstance(getActivity()).print();

                }
            });
            holder.mRadioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        selectedPosition = position;
                        notifyDataSetChanged();
                    }
                    selected_payment = mPMList.get(position).getPayment_code();
                    String m_Selected_PMCode = mPMList.get(position).getPayment_code();
                    if (m_Selected_PMCode.equalsIgnoreCase("cod")) {
                        Log.e("mSelectedPM", "test1: ");
                        mCheckOutBinding.mobilePayment.setVisibility(View.GONE);
                        //If Contactless delivery is checked, then cod must be disabled.
                        if (!mCheckOutBinding.checkBoxCoContactlessDelivery.isChecked()) {

//                            if (holder.mRadioButton.isChecked()) {
////                                  holder.mRadioButton.setChecked(false);
//                                Log.e("mSelectedPM", "test2: ");
//                                //    Log.e("1591", "called");
//                            }else{
                                Log.e("mSelectedPM", "test3: ");
                                //   Log.e("1593", "called");
                                mSelectedPMCode = mPMList.get(position).getPayment_code();
                                mSelectedPMCode_ID = mPMList.get(position).getPayment_list_id();
                                Log.e("mSelectedPM", "5onClick: "+mSelectedPMCode_ID);
                                holder.mRadioButton.setChecked(true);

                                CheckOutDetailsDB.getInstance(getActivity()).updatePaymentListId(mSelectedPMCode_ID);
                                CheckOutDetailsDB.getInstance(getActivity()).updatePaymentCode(mSelectedPMCode);

                                if (mSelectedPMRadioBtn == null) {
                                    //    Log.e("1602", "called");
                                    mSelectedPMRadioBtn = holder.mRadioButton;
                                    mSelectedPMTvTitle = holder.mTitle;
                                    mSelectedPMLayBorder = holder.mLayBorder;
                                } else {
                                    //  Log.e("1607", "called");
                                    mSelectedPMRadioBtn.setChecked(false);
                                    mSelectedPMRadioBtn = holder.mRadioButton;
                                    mSelectedPMTvTitle = holder.mTitle;
                                    mSelectedPMLayBorder = holder.mLayBorder;
                                }
                                if (mSelectedPMCode != null && !mSelectedPMCode.isEmpty() && mSelectedPMCode.equalsIgnoreCase("cod")) {
                                    mCheckOutBinding.checkBoxCoContactlessDelivery.setChecked(false);
                                    //    Log.e("1616", "called");
                                } else {
                                    //   Log.e("1618", "called");
                                }
                            }
//                        }
                    } else {
                        Log.e("mSelectedPM", "test4: ");
                        mCheckOutBinding.mobilePayment.setVisibility(View.VISIBLE);
//                        if (holder.mRadioButton.isChecked()) {
//                            //  holder.mRadioButton.setChecked(false);
//                            Log.e("mSelectedPM", "test5: ");
//                            // Log.e("1628", "called");
//                        } else {
                            //  Log.e("1630", "called");
                            Log.e("mSelectedPM", "test6: ");
                            mSelectedPMCode = mPMList.get(position).getPayment_code();
                            mSelectedPMCode_ID = mPMList.get(position).getPayment_list_id();
                            Log.e("mSelectedPM", "6onClick: "+mSelectedPMCode_ID+position);
                            holder.mRadioButton.setChecked(true);

                            CheckOutDetailsDB.getInstance(getActivity()).updatePaymentListId(mSelectedPMCode_ID);
                            CheckOutDetailsDB.getInstance(getActivity()).updatePaymentCode(mSelectedPMCode);

                            if (mSelectedPMRadioBtn == null) {
                                //   Log.e("1693", "called");
                                mSelectedPMRadioBtn = holder.mRadioButton;
                                mSelectedPMTvTitle = holder.mTitle;
                                mSelectedPMLayBorder = holder.mLayBorder;
                            } else {
                                //   Log.e("1644", "called");
                                mSelectedPMRadioBtn.setChecked(false);
                                mSelectedPMRadioBtn = holder.mRadioButton;
                                mSelectedPMTvTitle = holder.mTitle;
                                mSelectedPMLayBorder = holder.mLayBorder;
                            }
                            if (mSelectedPMCode != null && !mSelectedPMCode.isEmpty() && mSelectedPMCode.equalsIgnoreCase("cod")) {
                                mCheckOutBinding.checkBoxCoContactlessDelivery.setChecked(false);
                                //    Log.e("1652", "called");
                            } else {
                                //    Log.e("1654", "called");
                            }
                        }
//                    }
                    CheckOutDetailsDB.getInstance(getActivity()).print();
                }
            });

            if (!mFirstTime){
                if (position == selectedPosition) {
                   holder.mRadioButton.setChecked(true);
                } else {
                    holder.mRadioButton.setChecked(false);
                }
            } else{
                mFirstTime = false;
            }
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public int getItemCount() {
            return mPMList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mTitle;
            private ImageView mImage,mImage1,radio_btn_uncheck,radio_btn_check,radio_btn_disable;
            private LinearLayout mLayRow;
            private RadioButton mRadioButton;
            private LinearLayout mLayBorder;

            public ViewHolder(View itemView) {
                super(itemView);

                mImage = itemView.findViewById(R.id.img_pm_image);
                mImage1 = itemView.findViewById(R.id.img_pm_image1);
                mLayRow = itemView.findViewById(R.id.lay_pm_row);
                mTitle = itemView.findViewById(R.id.tv_pm_title);
                mRadioButton = itemView.findViewById(R.id.radio_btn_pm);
                mLayBorder = itemView.findViewById(R.id.lay_pm_box);
                radio_btn_check = itemView.findViewById(R.id.radio_btn_check);
                radio_btn_uncheck = itemView.findViewById(R.id.radio_btn_uncheck);
                radio_btn_disable = itemView.findViewById(R.id.radio_btn_disable);

            }
        }
    }
    @Override
    public void applyCoupon(String couponCode, int type) {
        mCouponCODE = couponCode;
        callCouponApplyAPi(mVendorID, couponCode, mSubTotalAmt);
    }
    private void callCouponApplyAPi(String vendorID, String couponCode, String subTotalAmt) {
        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        JSONArray vendorsIdJsonArray = new JSONArray();

                        vendorsIdJsonArray.put(0, vendorID);
                        jsonObject.put(DefaultNames.coupon_code, couponCode);
                        jsonObject.put(DefaultNames.vendor_id, vendorsIdJsonArray);
                        jsonObject.put(DefaultNames.sub_total, subTotalAmt);
                        jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                        jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                        AreaGeoCodeDataSet areaGeoCodeDataSet = AreaGeoCodeDB.getInstance(getActivity()).getUserAreaGeoCode();
                        jsonObject.put(DefaultNames.latitude, areaGeoCodeDataSet.getmLatitude());
                        jsonObject.put(DefaultNames.longitude, areaGeoCodeDataSet.getmLongitude());

                        RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                        String mCustomerAuthorization = "";
                        if (AppFunctions.isUserLoggedIn(getActivity())) {
                            mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                        }

                        retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                        Call<CouponApplyApi> Call = retrofitInterface.applyCouponApi(mCustomerAuthorization, body);
                        mProgressDialog.show();
                        Call.enqueue(new Callback<CouponApplyApi>() {
                            @Override
                            public void onResponse(@NonNull Call<CouponApplyApi> call, @NonNull Response<CouponApplyApi> response) {

                                if (getActivity() != null) {
                                    if (response.isSuccessful()) {

                                        CouponApplyApi mCouponApplyApi = response.body();
                                        if (mCouponApplyApi != null) {
                                            //Log.e("mCouponApplyApi", "not null");
                                            if (mCouponApplyApi.success != null) {
                                                //Api response successDataSet :-

                                                mCouponCODE_Id = mCouponApplyApi.getCoupon_id();

                                                callCartListAPi(true);

                                            } else {
                                                //Api response failure :-
                                                mProgressDialog.cancel();
                                                if (mCouponApplyApi.error != null) {
                                                    AppFunctions.msgDialogOk(getActivity(), "", mCouponApplyApi.error.message);
                                                }
                                            }
                                        } else {
                                            mProgressDialog.cancel();
                                            //Log.e("mCouponApplyApi", "null");
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
                            public void onFailure(@NonNull Call<CouponApplyApi> call, @NonNull Throwable t) {

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
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }


    }

    private String getCurrentDate() {

        String mDate;
        Calendar mInitialCalendar;
        mInitialCalendar = Calendar.getInstance(TimeZone.getDefault());
        Integer iDay = mInitialCalendar.get(Calendar.DAY_OF_MONTH);
        Integer iMonth = mInitialCalendar.get(Calendar.MONTH) + 1;
        Integer iYear = mInitialCalendar.get(Calendar.YEAR);
        String iTempDay, iTempMonth;

        String day = String.valueOf(mInitialCalendar.get(Calendar.DAY_OF_MONTH));
        Integer jMonth = mInitialCalendar.get(Calendar.MONTH) + 1;
        String month = String.valueOf(jMonth);

        if (iDay < 10) {
            iTempDay = "0" + day;
        } else {
            iTempDay = day;
        }
        if (iMonth < 10) {
            iTempMonth = "0" + month;
        } else {
            iTempMonth = month;
        }

        mDate = iTempMonth + "-" + iTempDay + "-" + iYear;

        return mDate;
    }

    private String getCurrentTime() {

        String mTime;

        if (android.text.format.DateFormat.is24HourFormat(getActivity())) {

            //Sample output :- 12:59 -> 12:59 AM
            String mCurrentTime = DateFormat.getTimeInstance().format(new Date());
            String[] mSplitTime = mCurrentTime.split(":");
            String m24HrsTime = mSplitTime[0] + ":" + mSplitTime[1];
            //Type 2 :-
            try {
                mTime = m24HrsTime; // output as 24hrs without meridian
            } catch (Exception e) {
                mTime = "";
            }
        } else {

            Calendar c = Calendar.getInstance();
            String mTempHours = "" + c.get(Calendar.HOUR);
            String mTempMinute = "" + c.get(Calendar.MINUTE);
            String mTempMeridian;

            if (c.get(Calendar.AM_PM) == 0) {
                mTempMeridian = "AM";
            } else {
                mTempMeridian = "PM";
            }

            String mStrLHrs, mStrLMin;
            if (mTempHours.length() == 1) {
                mStrLHrs = "0" + mTempHours;
            } else {
                mStrLHrs = mTempHours;
            }
            if (mTempMinute.length() == 1) {
                mStrLMin = "0" + mTempMinute;
            } else {
                mStrLMin = mTempMinute;
            }
            mTime = mStrLHrs + ":" + mStrLMin + " " + mTempMeridian;  // 12hrs format output
            // 24hrs format output :-
            try {
                SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
                SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
                Date date = parseFormat.parse(mTime);
                mTime = displayFormat.format(date); // output as 24hrs without meridian
            } catch (ParseException p) {
                mTime = "";
            }
        }
        return mTime;
    }

    private void callConfirmOrderApi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put(DefaultNames.coupon_id, mCouponCODE_Id);
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    if(!payment_mobile.equals("")){
                        jsonObject.put("airtel_mobile", payment_mobile);
                    }
                    jsonObject.put(DefaultNames.delivery_type, "now"); //static content.
                    jsonObject.put(DefaultNames.order_time, getCurrentTime());
                    jsonObject.put(DefaultNames.order_date, getCurrentDate());
                    jsonObject.put(DefaultNames.note, mCheckOutAddNote);
                    jsonObject.put(DefaultNames.payment_list_id, mSelectedPMCode_ID);
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put("order_type", order_type);

                    if (delivery_time != null && !delivery_time.isEmpty()) {
                        jsonObject.put("schedule_status", "1");
                        jsonObject.put("schedule_date", delivery_time);
                    } else {
                        jsonObject.put("schedule_status", "0");
                        jsonObject.put("schedule_date", "");
                    }

                    if (order_type.equals("2")) {
                        jsonObject.put(DefaultNames.address_id, "0");
                        if (!AppFunctions.isUserLoggedIn(getActivity())) {
                            JSONObject object = new JSONObject();
                            object.put("first_name", AddressBookGuestDB.getInstance(getActivity()).getDetails().getF_name());
                            object.put("last_name", AddressBookGuestDB.getInstance(getActivity()).getDetails().getL_name());
                            object.put("email", AddressBookGuestDB.getInstance(getActivity()).getDetails().getEmail_id());
                            object.put("mobile", AddressBookGuestDB.getInstance(getActivity()).getDetails().getMobile_number());
                            object.put("country_code", AddressBookGuestDB.getInstance(getActivity()).getDetails().getCountry_code());
                            jsonObject.put("guest_pickup", object);
                        } else {
                            jsonObject.put("guest_pickup", "0");
                        }
                    } else {
                        jsonObject.put(DefaultNames.address_id, mSelectedDeliveryAddsId);
                        jsonObject.put("guest_pickup", "0");
                    }

                    if (OneSignal.getDeviceState() != null && OneSignal.getDeviceState().getUserId() != null) {
                        jsonObject.put(DefaultNames.push_id, OneSignal.getDeviceState().getUserId());
                    } else {
                        jsonObject.put(DefaultNames.push_id, "");
                    }
                    if (mCheckOutBinding.checkBoxCoContactlessDelivery.isChecked()) {
                        //Contactless delivery selected :-
                        jsonObject.put(DefaultNames.contactless_delivery, "1");
                    } else {
                        //Contactless delivery NOT selected :-
                        jsonObject.put(DefaultNames.contactless_delivery, "0");
                    }

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
                    Call<ConfirmOrderApi> Call = retrofitInterface.confirmOrderApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<ConfirmOrderApi>() {
                        @Override
                        public void onResponse(@NonNull Call<ConfirmOrderApi> call, @NonNull Response<ConfirmOrderApi> response) {

                            if (getActivity() != null) {

                                if (response.isSuccessful()) {
                                    mConfirmOrderApi = response.body();

                                    if (mConfirmOrderApi != null) {
                                        //Log.e("mConfirmOrderApi", "not null");
                                        if (mConfirmOrderApi.success != null) {
                                            //Api response successDataSet :-
                                            String mOrderId = mConfirmOrderApi.getOrder_id();
                                            if(!selected_payment.equals("")&& selected_payment.equals("airtel")){

//                                            web = true;
//                                            pageViewedStatus.stripePGView(true);

                                                mCheckOutBinding.payByCard.setVisibility(View.VISIBLE);
                                                mCheckOutBinding.layCoPlaceOrderBtnContainer.setVisibility(View.GONE);

                                                Log.e("orderid" + mOrderId, "");

//
                                                String urlT = "https://www.ordenesdelivery.com/mobile/checkout/airtel/" + mOrderId;
//                                                Log.e("url"+urlT,"");
                                                mCheckOutBinding.payByCard.getSettings().setJavaScriptEnabled(true);
                                                mCheckOutBinding.payByCard.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
                                                mCheckOutBinding.payByCard.setWebViewClient(new WebViewClient() {
                                                    @Override
                                                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                                        super.onPageStarted(view, url, favicon);
                                                        //  progressBar.setVisibility(View.VISIBLE);
                                                        Log.e("onPageStarted: ", url + "");
                                                    }

                                                    @Override
                                                    public void onPageFinished(WebView view, String url) {
                                                        super.onPageFinished(view, url);
                                                        if (url.contains("https://www.ordenesdelivery.com/mobile/checkout/payment_status/0")) {

                                                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                                                            alert.setMessage(getActivity().getResources().getString(R.string.transaction_failed))
                                                                    .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialog, int which) {
                                                                            if (getActivity() != null) {
                                                                                Intent intent = new Intent(getActivity().getApplicationContext(), AppHome.class);
                                                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                                                                        Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                                getActivity().startActivity(intent);
                                                                                getActivity().finish();
                                                                            }
                                                                        }
                                                                    }).create().show();

                                                        } else if (url.contains("https://www.ordenesdelivery.com/mobile/checkout/payment_status/1")) {

                                                            mProgressDialog.cancel();
                                                            mCheckOutBinding.layCoPlaceOrderBtnContainer.setEnabled(true);
                                                            callPlaceOrderApi(mOrderId);
//                                                            if (getParentFragmentManager() != null) {
//                                                                if (getParentFragmentManager().beginTransaction() != null) {
//                                                                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
//                                                                    if (order_type.equals("2") ) {
////                                                                        || cleaning.equals("1")
//                                                                        CheckOutSuccess m_checkOutSuccess = new CheckOutSuccess();
//                                                                        mFT.replace(R.id.layout_app_check_out_body, m_checkOutSuccess, "m_checkOutSuccess");
//                                                                        mFT.addToBackStack("m_checkOutSuccess");
//                                                                    } else {
//                                                                        OrderConfirmation m_orderConfirmation = new OrderConfirmation();
//                                                                        Bundle bundle = new Bundle();
//                                                                        bundle.putString(DefaultNames.from, DefaultNames.fromCheckOut);
//                                                                        bundle.putString(DefaultNames.order_id, mOrderId);
//                                                                        m_orderConfirmation.setArguments(bundle);
//                                                                        mFT.replace(R.id.layout_app_check_out_body, m_orderConfirmation, "m_orderConfirmation");
//                                                                        mFT.addToBackStack("m_orderConfirmation");
//                                                                    }
//                                                                    mFT.commit();
//                                                                }
//                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                                                        super.onReceivedError(view, errorCode, description, failingUrl);
                                                        Log.e("Error Description: " + description, "");
                                                        Log.e("failingUrl: " + failingUrl, "");
                                                    }
                                                });

                                                mCheckOutBinding.payByCard.setWebChromeClient(new WebChromeClient());
                                                mCheckOutBinding.payByCard.loadUrl(urlT);
                                                mProgressDialog.cancel();
                                                mCheckOutBinding.layCoPlaceOrderBtnContainer.setEnabled(true);
                                            }else {
                                                callPlaceOrderApi(mOrderId);
                                            }


                                        } else {

                                            mProgressDialog.cancel();

                                            //Api response failure :-
                                            if (mConfirmOrderApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mConfirmOrderApi.error.message);
                                            }
                                        }
                                    } else {

                                        mProgressDialog.cancel();

                                        //Log.e("mConfirmOrderApi", "null");
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
                        public void onFailure(@NonNull Call<ConfirmOrderApi> call, @NonNull Throwable t) {

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
                mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();

            }
        }


    }

    private void callPlaceOrderApi(String orderID) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();

                try {

                    jsonObject.put(DefaultNames.order_id, orderID);

                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {

                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    jsonObject.put("order_type", order_type);
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ApiResponseCheck> Call = retrofitInterface.placeOrderApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<ApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponseCheck> call, @NonNull Response<ApiResponseCheck> response) {

                            if (getActivity() != null) {

                                if (response.isSuccessful()) {
                                    ApiResponseCheck mApiResponseCheck = response.body();

                                    if (mApiResponseCheck != null) {
                                        //Log.e("mApiResponseCheck", "not null");
                                        if (mApiResponseCheck.success != null) {
                                            //Api response successDataSet :-
                                            mProgressDialog.cancel();

                                            getParentFragmentManager().popBackStack("m_checkOut", 1);
                                            getParentFragmentManager().beginTransaction().remove(CheckOut.this).commit();

                                            if (order_type.equals("2")) {
                                                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                                CheckOutSuccess m_checkOutSuccess = new CheckOutSuccess();
                                                mFT.replace(R.id.layout_app_check_out_body, m_checkOutSuccess, "m_checkOutSuccess");
                                                mFT.addToBackStack("m_checkOutSuccess");
                                                mFT.commit();
                                            } else {
                                                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                                OrderConfirmation m_orderConfirmation = new OrderConfirmation();
                                                Bundle bundle = new Bundle();
                                                bundle.putString(DefaultNames.from, DefaultNames.fromCheckOut);
                                                bundle.putString(DefaultNames.order_id, orderID);
                                                m_orderConfirmation.setArguments(bundle);
                                                mFT.replace(R.id.layout_app_check_out_body, m_orderConfirmation, "m_orderConfirmation");
                                                mFT.addToBackStack("m_orderConfirmation");
                                                mFT.commit();
                                            }

                                        } else {

                                            mProgressDialog.cancel();

                                            //Api response failure :-
                                            if (mApiResponseCheck.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mApiResponseCheck.error.message);
                                            }
                                        }
                                    } else {

                                        mProgressDialog.cancel();

                                        //Log.e("mApiResponseCheck", "null");
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
                        public void onFailure(@NonNull Call<ApiResponseCheck> call, @NonNull Throwable t) {
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
                mFT.replace(R.id.layout_app_check_out_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();

            }
        }


    }

    private void callIsDeliveryAPi(String latitude, String longitude, String addressID, String zoneID) {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();
                try {
                    AddressGeocodeDataSet mAddressGDs = RestaurantAddressDB.getInstance(getActivity()).getGeocodeDetails();

                    jsonObject.put(DefaultNames.vendor_id, mAddressGDs.getRestaurantId());
                    jsonObject.put(DefaultNames.latitude, latitude);
                    jsonObject.put(DefaultNames.longitude, longitude);
                    jsonObject.put(DefaultNames.address_id, addressID);
                    jsonObject.put(DefaultNames.zone_id, zoneID);

                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        jsonObject.put(DefaultNames.guest_status, "0");
                        jsonObject.put(DefaultNames.guest_id, "");
                    } else {
                        jsonObject.put(DefaultNames.guest_status, "1");
                        jsonObject.put(DefaultNames.guest_id, AppFunctions.getAndroidId(getActivity()));
                    }
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ApiResponseCheck> Call = retrofitInterface.isDeliveryApi(mCustomerAuthorization, body);
                    mProgressDialog.show();

                    Call.enqueue(new Callback<ApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponseCheck> call, @NonNull Response<ApiResponseCheck> response) {


                            if (response.isSuccessful()) {

                                ApiResponseCheck mApiResponseCheck = response.body();

                                if (mApiResponseCheck != null) {
                                    if (mApiResponseCheck.success != null) {
                                        //Api response successDataSet :-

                                        callConfirmOrderApi();
                                    } else {
                                        //Api response failure :-
                                        mProgressDialog.cancel();
                                        if (getActivity() != null) {
                                            if (mApiResponseCheck.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mApiResponseCheck.error.message);
                                            }
                                        }
                                    }

                                } else {
                                    mProgressDialog.cancel();
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
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }


    }

    @Override
    public void toShowMobileNoOTPUI(Boolean toShow) {

        if (toShow) {
            mCheckOutBinding.layCheckoutUi.setVisibility(View.GONE);
            mCheckOutBinding.layCheckoutGuestOtpUi.setVisibility(View.VISIBLE);
            mCheckOutBinding.layCgOtpResend.setVisibility(View.GONE);
            mIsCheckoutUiHide = true;
            mCheckOutBinding.tvCoAppBarVendorName.setText("");

            mProgressDialog.show();

            if (mPaymentMethodsApi != null) {
                if (mPaymentMethodsApi.checkOutAdds != null) {
                    CheckOutAddsDataSet mCOAddsDs = mPaymentMethodsApi.checkOutAdds;
                    if (mCOAddsDs.getAddress_id() != null && !mCOAddsDs.getAddress_id().isEmpty()) {
                        String mCountry_code = mCOAddsDs.getCountry_code();
                        String mMobile = mCOAddsDs.getMobile();
                        mMOBNoWithCCodeWithPlus = "+" + mCountry_code + "" + mMobile;
                        String mPrefix = getActivity().getString(R.string.enter_4_digit);
                        String mMobileNumber = " " + mMOBNoWithCCodeWithPlus;
                        String mData = mPrefix + mMobileNumber;
                        mCheckOutBinding.tvRwoOtpMsgWithMobileNumber.setText(mData);
                        startPhoneNumberVerification(mMOBNoWithCCodeWithPlus);
                    }
                }
            }


        }

    }

    @Override
    public void toChangeMobileNo(Boolean toShow) {
        if (toShow) {
            toEditGuestAdds();
        }
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        if (getActivity() != null) {
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phoneNumber)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(getActivity())                 // Activity (for callback binding)
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }
        // [END start_phone_auth]
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        if (getActivity() != null) {
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                //Log.d("signInWithCredential:", "success");

                                FirebaseUser user = task.getResult().getUser();
                                // Update UI

                                // m_Toast("signInWithCredential", "success");

                                // mProgressDialog.cancel();
                                // isUserAlreadyRegistered();

                                if (simpleCountDownTimer != null) {
                                    simpleCountDownTimer.pause();
                                }

                                mIsGuestOtpVerifiedSuccessfully = true;
                                mCheckOutBinding.layCheckoutUi.setVisibility(View.VISIBLE);
                                mCheckOutBinding.layCheckoutGuestOtpUi.setVisibility(View.GONE);
                                mIsCheckoutUiHide = false;
                                mCheckOutBinding.tvCoAppBarVendorName.setText(mVendorName);
                                callIsDeliveryAPi(mSelectedAddsLatitude, mSelectedAddsLongitude, mSelectedDeliveryAddsId, mSelectedAddsZoneId);


                            } else {

                                mProgressDialog.cancel();

                                // m_Toast("signInWithCredential","failed");


                                if (getActivity() != null) {
                                    AppFunctions.msgDialogOk(getActivity(), "",
                                            getActivity().getString(R.string.please_enter_correct_otp));
                                }

                                // Sign in failed, display a message and update the UI
                                // //Log.w(TAG, "signInWithCredential:failure", task.getException());
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    // The verification code entered was invalid
                                }
                            }
                        }
                    });

        }

    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        if (getActivity() != null) {
            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(mAuth)
                            .setPhoneNumber(phoneNumber)       // Phone number to verify
                            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                            .setActivity(getActivity())                 // Activity (for callback binding)
                            .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                            .setForceResendingToken(token)     // ForceResendingToken from callbacks
                            .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        }
    }

    @Override
    public void onCountDownActive(String time) {
        if (getActivity() != null) {

            String mPrefix = getActivity().getString(R.string.i_did_not_received_code_prefix);
            // String mData = time;
            String mPostfix = getActivity().getString(R.string.i_did_not_received_code_postfix);
            String mFinalData = mPrefix + time + mPostfix;
            mCheckOutBinding.tvCgOtpDidNotReceive.setText(mFinalData);
            //tv_rwo_otp_did_not_receive

        }
    }

    @Override
    public void onCountDownFinished() {
        mCheckOutBinding.tvCgOtpDidNotReceive.setVisibility(View.GONE);
        mCheckOutBinding.layCgOtpResend.setVisibility(View.VISIBLE);
        toHideDeviceKeyBoard();
    }

    public static class ScheduleDialog extends DialogFragment {

        FragmentScheduleDialogBinding binding;

        public ScheduleDialog() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {

            }
        }

        @Override
        public void onStart() {
            super.onStart();
            // safety check
            if (getDialog() == null)
                return;
            int width = ViewGroup.LayoutParams.WRAP_CONTENT;
            int height = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._450sdp);

//            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            if (getDialog().getWindow() != null)
                getDialog().getWindow().setLayout(width, height);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            binding = FragmentScheduleDialogBinding.inflate(inflater, container, false);

            if (getDialog() != null) {
                if (getDialog().getWindow() != null) {
                    getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                }
            }

            binding.scheduleRecView.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
            binding.scheduleRecView.setAdapter(new DeliveryDateAdapter(scheduleDeliveryDataSet.date));

            return binding.getRoot();
        }

        class DeliveryDateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            ArrayList<String> arrayList;

            public DeliveryDateAdapter(ArrayList<String> date) {
                this.arrayList = date;
            }


            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.schedule_rec_list, parent, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ViewHolder viewHolder = (ViewHolder) holder;
                viewHolder.date.setText(arrayList.get(position));

                viewHolder.date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        delivery_time = arrayList.get(position);
                        setUpdate.reload();
                        dismiss();
                    }
                });
            }

            @Override
            public int getItemCount() {
                return arrayList.size();
            }

            class ViewHolder extends RecyclerView.ViewHolder {

                TextView date;

                public ViewHolder(@NonNull View itemView) {
                    super(itemView);
                    date = itemView.findViewById(R.id.delivery_date);
                }
            }

        }

    }
}