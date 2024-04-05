package com.ordenese.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.CustomClasses.SimpleCountDownTimer;
import com.ordenese.DataSets.AddressGeocodeDataSet;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.DataSets.ConfirmOrderApi;
import com.ordenese.Databases.AddressBookGuestDB;
import com.ordenese.Databases.CheckOutDetailsDB;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.RestaurantAddressDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.R;
import com.ordenese.databinding.FragmentGuestOTPVerificationBinding;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

public class GuestOTPVerification extends Fragment implements View.OnClickListener, SimpleCountDownTimer.OnCountDownListener {

    FragmentGuestOTPVerificationBinding binding;
    String mCountry_code = "", mMobile = "", mMOBNoWithCCodeWithPlus = "", payment_list_id = "", order_type = "", contactless_delivery = "";
    ProgressDialog mProgressDialog;
    RetrofitInterface retrofitInterface;
    ConfirmOrderApi mConfirmOrderApi;
    FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String mVerificationId = "";
    PhoneAuthProvider.ForceResendingToken mResendToken;
    SimpleCountDownTimer simpleCountDownTimer;
    AddressGeocodeDataSet addressGeocodeDs;

    public GuestOTPVerification() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            order_type = getArguments().getString("order_type");
            payment_list_id = getArguments().getString("payment_list_id");
            contactless_delivery = getArguments().getString("contactless_delivery");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGuestOTPVerificationBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        addressGeocodeDs = RestaurantAddressDB.getInstance(getActivity()).getGeocodeDetails();
        mAuth = FirebaseAuth.getInstance();

        mCountry_code = AddressBookGuestDB.getInstance(getActivity()).getDetails().getCountry_code();
        mMobile = AddressBookGuestDB.getInstance(getActivity()).getDetails().getMobile_number();

        binding.imgCheckOutBack.setOnClickListener(this);
        binding.layCgOtpSubmitBtnContainer.setOnClickListener(this);
        binding.layCgOtpResend.setOnClickListener(this);

        binding.tvCoAppBarVendorName.setText(addressGeocodeDs.getRestaurantName());

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
                                    simpleCountDownTimer = new SimpleCountDownTimer(0, 30, 1, GuestOTPVerification.this);
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

        mMOBNoWithCCodeWithPlus = "+" + mCountry_code + "" + mMobile;
        String mPrefix = getActivity().getString(R.string.enter_4_digit);
        String mMobileNumber = " " + mMOBNoWithCCodeWithPlus;
        String mData = mPrefix + mMobileNumber;
        binding.tvRwoOtpMsgWithMobileNumber.setText(mData);
        startPhoneNumberVerification(mMOBNoWithCCodeWithPlus);


        mProgressDialog.cancel();
        return binding.getRoot();
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
                                // mProgressDialog.cancel();
                                // isUserAlreadyRegistered();
                                if (simpleCountDownTimer != null) {
                                    simpleCountDownTimer.pause();
                                }
                                callConfirmOrderApi();

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

    private void callConfirmOrderApi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.coupon_id, CheckOutDetailsDB.getInstance(getActivity()).getDetails().getCouponId());
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                    jsonObject.put(DefaultNames.delivery_type, "now"); //static content.
                    jsonObject.put(DefaultNames.order_date, getCurrentDate());
                    jsonObject.put(DefaultNames.order_time, getCurrentTime());
                    jsonObject.put(DefaultNames.note, addressGeocodeDs.getCheckOutNote());
                    jsonObject.put(DefaultNames.payment_list_id, payment_list_id);
                    jsonObject.put(DefaultNames.day_id, AppFunctions.getDayId());
                    jsonObject.put("order_type", order_type);
                    jsonObject.put(DefaultNames.address_id, "0");

                    JSONObject object = new JSONObject();
                    object.put("first_name", AddressBookGuestDB.getInstance(getActivity()).getDetails().getF_name());
                    object.put("last_name", AddressBookGuestDB.getInstance(getActivity()).getDetails().getL_name());
                    object.put("email", AddressBookGuestDB.getInstance(getActivity()).getDetails().getEmail_id());
                    object.put("mobile", AddressBookGuestDB.getInstance(getActivity()).getDetails().getMobile_number());
                    object.put("country_code", AddressBookGuestDB.getInstance(getActivity()).getDetails().getCountry_code());
                    jsonObject.put("guest_pickup", object);

                    jsonObject.put(DefaultNames.contactless_delivery, contactless_delivery);

                    if (OneSignal.getDeviceState() != null && OneSignal.getDeviceState().getUserId() != null) {
                        jsonObject.put(DefaultNames.push_id, OneSignal.getDeviceState().getUserId());
                    } else {
                        jsonObject.put(DefaultNames.push_id, "");
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
                            if (response.isSuccessful()) {
                                mConfirmOrderApi = response.body();
                                if (mConfirmOrderApi != null) {
                                    //Log.e("mConfirmOrderApi", "not null");
                                    if (mConfirmOrderApi.success != null) {
                                        //Api response successDataSet :-
                                        String mOrderId = mConfirmOrderApi.getOrder_id();
                                        callPlaceOrderApi(mOrderId);
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
                                            getParentFragmentManager().popBackStack("m_guestAddressAddEdit", 2);
                                            getParentFragmentManager().beginTransaction().remove(GuestOTPVerification.this).commit();

                                            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                                            CheckOutSuccess m_checkOutSuccess = new CheckOutSuccess();
                                            mFT.replace(R.id.layout_app_check_out_body, m_checkOutSuccess, "m_checkOutSuccess");
                                            mFT.addToBackStack("m_checkOutSuccess");
                                            mFT.commit();


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
    public void onClick(View v) {

        int mId = v.getId();

        if (mId == R.id.lay_cg_otp_submit_btn_container) {
            if (getActivity() != null) {
                String mOTP = binding.etCgOtp.getText().toString();

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
                binding.layCgOtpResend.setVisibility(View.GONE);
                binding.tvCgOtpDidNotReceive.setVisibility(View.VISIBLE);
                mProgressDialog.show();
                resendVerificationCode(mMOBNoWithCCodeWithPlus, mResendToken);
            }
        } else if (mId == R.id.img_check_out_back) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
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

    private void toHideDeviceKeyBoard() {

        if (getActivity() != null) {
            if (binding != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                // To get the correct window token, lets first get the currently focused view
                View v__iew = binding.getRoot();
                // To get the window token when there is no currently focused view, we have a to create a view
                if (v__iew == null) {
                    v__iew = new View(getActivity());
                }
                // hide the keyboard
                imm.hideSoftInputFromWindow(v__iew.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onCountDownActive(String time) {
        if (getActivity() != null) {
            String mPrefix = getActivity().getString(R.string.i_did_not_received_code_prefix);
            // String mData = time;
            String mPostfix = getActivity().getString(R.string.i_did_not_received_code_postfix);
            String mFinalData = mPrefix + time + mPostfix;
            binding.tvCgOtpDidNotReceive.setText(mFinalData);
            //tv_rwo_otp_did_not_receive

        }
    }

    @Override
    public void onCountDownFinished() {
        binding.tvCgOtpDidNotReceive.setVisibility(View.GONE);
        binding.layCgOtpResend.setVisibility(View.VISIBLE);
        toHideDeviceKeyBoard();
    }
}