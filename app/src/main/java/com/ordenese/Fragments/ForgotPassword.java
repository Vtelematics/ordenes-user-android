package com.ordenese.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

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
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.CustomClasses.SimpleCountDownTimer;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.DataSets.CommonFunctionOtpDataSet;
import com.ordenese.DataSets.CountryCodeApi;
import com.ordenese.DataSets.LoginApi;
import com.ordenese.DataSets.LoginCountryCodeDataSet;
import com.ordenese.DataSets.LoginCustomerInfoDataSet;
import com.ordenese.DataSets.LoginDataSet;
import com.ordenese.DataSets.OtpSuccess;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.LoginCountryCodeDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CountryCodeSelection;
import com.ordenese.R;
import com.ordenese.databinding.ForgotPasswordBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.PrimitiveIterator;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ForgotPassword extends Fragment implements View.OnClickListener, CountryCodeSelection,
        SimpleCountDownTimer.OnCountDownListener {

    private ForgotPasswordBinding mForgotPwdBinding;
    private RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;
    private CountryCodeApi mCountryCodeApi;
    private String mMOBNoWithCCodeWithPlus = "";

    //OTP :-
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId = "";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private SimpleCountDownTimer simpleCountDownTimer;
    String mMobileNumber,mCountryCode = "";

    String mMobile_Number = "";
    String mCountry_code = "";

    boolean isFirebase = false;
    public ForgotPassword() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(AppLanguageSupport.onAttach(context));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (getActivity() != null) {

                getActivity().getWindow().getDecorView().setLayoutDirection(
                        "ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(getActivity())) ?
                                View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.forgot_password, container, false);
        mForgotPwdBinding = ForgotPasswordBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mForgotPwdBinding.imgFpBack.setOnClickListener(this);

        mForgotPwdBinding.layFpMobileNoParent.setVisibility(View.VISIBLE);
        mForgotPwdBinding.layFpOtpParent.setVisibility(View.GONE);
        mForgotPwdBinding.tvFpOtpDidNotReceive.setVisibility(View.GONE);
        mForgotPwdBinding.layFpNewPwdParent.setVisibility(View.GONE);


        mForgotPwdBinding.layFpMobNoSubmitBtnContainer.setOnClickListener(this);
        mForgotPwdBinding.layFpOtpSubmitBtnContainer.setOnClickListener(this);
        mForgotPwdBinding.layFpOtpResend.setOnClickListener(this);
        mForgotPwdBinding.layFpPwdSubmitBtnContainer.setOnClickListener(this);
        mForgotPwdBinding.layFpCountryCodeContainer.setOnClickListener(this);
        //
        //
        callCountryCodeListAPi();

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

//                 signInWithPhoneAuthCredential(credential);

                mProgressDialog.cancel();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                //Log.w("onVerificationFailed", e);

                // m_Toast("onVerificationFailed ", "Excep : " + e.toString());
                Log.e("onVerificationFailed", "onVerificationFailed: "+e.toString());

                if (getActivity() != null) {
                    // AppFunctions.msgDialogOk(getActivity(),"",getActivity().getString(R.string.otp_sent_to_your_mobile_number));
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder
                            .setMessage(e.getMessage())
                            //.setTitle(mContext.getString(R.string.))
                            .setCancelable(true)
                            .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.dismiss();
                                }
                            });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

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
                                    simpleCountDownTimer = new SimpleCountDownTimer(0, 30, 1, ForgotPassword.this);
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
        // [END phone_auth_callbacks]


        return mForgotPwdBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (simpleCountDownTimer != null) {
            simpleCountDownTimer.start(true);
        }

    }

    @Override
    public void onStop() {
        super.onStop();

        if (simpleCountDownTimer != null) {
            simpleCountDownTimer.pause();
        }

    }

    private void toHideDeviceKeyBoard() {

        if (getActivity() != null) {
            if(mForgotPwdBinding != null){
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                // To get the correct window token, lets first get the currently focused view
                View v__iew = mForgotPwdBinding.getRoot();
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
    public void onClick(View view) {
        int mId = view.getId();
        if (mId == R.id.img_fp_back) {
            if (getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }
        } else if (mId == R.id.lay_fp_mob_no_submit_btn_container) {
            if (getActivity() != null) {
                String mMobile_Number = mForgotPwdBinding.etFpMobile.getText().toString();

                if (!mMobile_Number.isEmpty()) {
                    //AppFunctions.msgDialogOk(getActivity(),"","successs..");

                    toHideDeviceKeyBoard();

                    callMobileNoExistsCheckAPi();
                    /*mForgotPwdBinding.layFpMobileNoParent.setVisibility(View.GONE);
                    mForgotPwdBinding.layFpOtpParent.setVisibility(View.VISIBLE);
                    mForgotPwdBinding.tvFpOtpDidNotReceive.setVisibility(View.VISIBLE);
                    mForgotPwdBinding.layFpOtpResend.setVisibility(View.GONE);
                    simpleCountDownTimer = new SimpleCountDownTimer(0, 30, 1, ForgotPassword.this);
                    simpleCountDownTimer.start(false);*/

                } else {

                    toHideDeviceKeyBoard();
                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_mobile_no));
                }
            }
        } else if (mId == R.id.lay_fp_otp_submit_btn_container) {
            if (getActivity() != null) {
                String mOTP = mForgotPwdBinding.etFpOtp.getText().toString();

                if (!mOTP.isEmpty()) {
                    //AppFunctions.msgDialogOk(getActivity(),"","successs..");

                    toHideDeviceKeyBoard();
                    mProgressDialog.show();
                    if(isFirebase){
                        verifyPhoneNumberWithCode(mVerificationId, mOTP);
                    }else {
                        verifyOTP(mForgotPwdBinding.tvFpCountryCode.getText().toString(),mForgotPwdBinding.etFpMobile.getText().toString(),mOTP);
                    }
                    /*mForgotPwdBinding.layFpOtpParent.setVisibility(View.GONE);
                    mForgotPwdBinding.layFpNewPwdParent.setVisibility(View.VISIBLE);*/

                } else {
                    toHideDeviceKeyBoard();
                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_otp));
                }
            }
        } else if (mId == R.id.lay_fp_otp_resend) {
            if (getActivity() != null) {
                mForgotPwdBinding.layFpOtpResend.setVisibility(View.GONE);
                mForgotPwdBinding.tvFpOtpDidNotReceive.setVisibility(View.VISIBLE);
                mProgressDialog.show();
                if(isFirebase){
                    resendVerificationCode(mMOBNoWithCCodeWithPlus, mResendToken);
                }else{
                    CommonFunctionOtp(mMobile_Number,mCountry_code);
                }
            }
        } else if (mId == R.id.lay_fp_pwd_submit_btn_container) {

            if (getActivity() != null) {
                String mPwd = mForgotPwdBinding.etFpPwd.getText().toString();
                String mConfirmPwd = mForgotPwdBinding.etFpConfirmPwd.getText().toString();

                if (!mPwd.isEmpty() && !mConfirmPwd.isEmpty()) {
                    if (mPwd.equals(mConfirmPwd)) {
                        //AppFunctions.msgDialogOk(getActivity(),"","successs..");
                        toHideDeviceKeyBoard();
                        callPwdUpdateAPi();
                    } else {
                        toHideDeviceKeyBoard();
                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.pwd_and_confirm_pwd_not_match));
                    }
                } else {
                    if (mPwd.isEmpty()) {
                        toHideDeviceKeyBoard();
                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_pwd));
                    } else {
                        if (mConfirmPwd.isEmpty()) {
                            toHideDeviceKeyBoard();
                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_the_confirm_pwd));
                        }
                    }
                }
            }
        }else if(mId == R.id.lay_fp_country_code_container){
            if(getActivity() != null){
                if (mCountryCodeApi != null) {
                    //Log.e("mCountryCodeApi", "not null");
                    if (mCountryCodeApi.success != null) {
                        //Api response successDataSet :-
                        if (getActivity() != null) {

                            if (mCountryCodeApi.countryList != null && mCountryCodeApi.countryList.size() > 0) {
                                DialogueCountryCodeList mDCountryCodeList = new DialogueCountryCodeList().newInstance(mCountryCodeApi.countryList);
                                mDCountryCodeList.setTargetFragment(ForgotPassword.this,2975);
                                mDCountryCodeList.show(getParentFragmentManager(),"mDCountryCodeList");
                            }
                        }
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

                                                if (!LoginCountryCodeDB.getInstance(getActivity()).check_selected()) {
                                                    //If there is no previous selection then default selection as first position of list :-

                                                    //default country id india :-
                                                    //country id : 100
                                                    int getCountryPosition = 0;
                                                    for (int country = 0; country < mCountryCodeApi.countryList.size(); country++) {
                                                        if (mCountryCodeApi.countryList.get(country).getId().equals("100")) {
                                                            getCountryPosition = country;
                                                        }
                                                    }

                                                    String mPhoneCode = "" + mCountryCodeApi.countryList.get(getCountryPosition).getCode();
                                                    mForgotPwdBinding.tvFpCountryCode.setText(mPhoneCode);
                                                    if (getActivity() != null) {
                                                        // Glide.with(getActivity()).load(mCountryCodeList.get(getCountryPosition).getImage()).into(flagImg);
                                                    }

                                                } else {

                                                    LoginCountryCodeDataSet loginCCDs = LoginCountryCodeDB.getInstance(getActivity()).get_Details();
                                                    for (int phoneCode = 0; phoneCode < mCountryCodeApi.countryList.size(); phoneCode++) {
                                                        if (mCountryCodeApi.countryList.get(phoneCode).getId().equals(loginCCDs.getCountryId())) {
                                                            mForgotPwdBinding.tvFpCountryCode.setText(loginCCDs.getPhoneCode());
                                                            if (getActivity() != null) {
                                                                // Glide.with(getActivity()).load(mCountryCodeApi.countryList.get(phoneCode).getImage()).into(flagImg);
                                                            }
                                                            break;
                                                        }
                                                    }


                                                }


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
                            }else {
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
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_login_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }

    }

    @Override
    public void selectedCountryCode(LoginCountryCodeDataSet mLoginNCDs) {
        mForgotPwdBinding.tvFpCountryCode.setText(mLoginNCDs.getPhoneCode());
    }

    private void callMobileNoExistsCheckAPi() {


        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    mMobile_Number = mForgotPwdBinding.etFpMobile.getText().toString();
                    mCountry_code = mForgotPwdBinding.tvFpCountryCode.getText().toString();

                    jsonObject.put(DefaultNames.telephone, mMobile_Number);
                    jsonObject.put(DefaultNames.countryId, mCountry_code);

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ApiResponseCheck> Call = retrofitInterface.checkCustomerApi(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<ApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponseCheck> call, @NonNull Response<ApiResponseCheck> response) {


                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                ApiResponseCheck mApiResponseCheck = response.body();
                                if (mApiResponseCheck != null) {
                                    //Log.e("mApiResponseCheck", "not null");
                                    if (mApiResponseCheck.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            //If response is success.Then its refers a new customer.The forgot password process
                                            //not suit for this customer.So show error msg to customer.
                                            AppFunctions.msgDialogOk(getActivity(), "", getResources().getString(R.string.You_not_a_exists_user));
                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mApiResponseCheck.error != null) {
                                                //If response has error.Then its refers customer exists. So proceed forgot pwd otp :-
                                                mForgotPwdBinding.layFpMobileNoParent.setVisibility(View.GONE);
                                                mForgotPwdBinding.layFpOtpParent.setVisibility(View.VISIBLE);
                                                mForgotPwdBinding.tvFpOtpDidNotReceive.setVisibility(View.VISIBLE);
                                                mForgotPwdBinding.layFpOtpResend.setVisibility(View.GONE);

                                                mProgressDialog.show();
                                                mMOBNoWithCCodeWithPlus = "+" + mCountry_code + "" + mMobile_Number;
                                                String mPrefix = getActivity().getString(R.string.enter_4_digit);
                                                String mMobileNumber = " " + mMOBNoWithCCodeWithPlus;
                                                String mData = mPrefix + mMobileNumber;
                                                mForgotPwdBinding.tvFpOtpMsgWithMobileNumber.setText(mData);
//                                                startPhoneNumberVerification(mMOBNoWithCCodeWithPlus);
                                                CommonFunctionOtp(mMobile_Number,mCountry_code);
                                            }
                                        }
                                    }
                                } else {
                                    //Log.e("mApiResponseCheck", "null");
                                }
                            }else {
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
                mFT.replace(R.id.layout_app_login_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
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
            mForgotPwdBinding.tvFpOtpDidNotReceive.setText(mFinalData);
            //tv_fp_otp_did_not_receive

        }
    }

    @Override
    public void onCountDownFinished() {
        mForgotPwdBinding.tvFpOtpDidNotReceive.setVisibility(View.GONE);
        mForgotPwdBinding.layFpOtpResend.setVisibility(View.VISIBLE);
        toHideDeviceKeyBoard();
    }

    private void CommonFunctionOtp(String mMobile_Number,String mCountry_code){
        mProgressDialog.show();
        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.telephone, mMobile_Number);
                    jsonObject.put(DefaultNames.countryId, mCountry_code);

//                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
//                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<CommonFunctionOtpDataSet> Call = retrofitInterface.commonFunctionOtp(body);
                    Call.enqueue(new Callback<CommonFunctionOtpDataSet>(){
                        @Override
                        public void onResponse(@NonNull Call<CommonFunctionOtpDataSet> call, @NonNull Response<CommonFunctionOtpDataSet> response) {
                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                CommonFunctionOtpDataSet commonFunctionOtpDataSet = response.body();
                                if (commonFunctionOtpDataSet.otpType.equals("firebase")) {
                                    isFirebase = true;
                                    startPhoneNumberVerification(mMOBNoWithCCodeWithPlus);
                                } else {
                                    isFirebase = false;
                                    sendOTP(mMobile_Number, mCountry_code, commonFunctionOtpDataSet.encript.encriptCode, commonFunctionOtpDataSet.encript.now);
                                }
                            }
//                            else {
//                                mProgressDialog.cancel();
//                                String mErrorMsgToShow = "";
//                                try {
//                                    ResponseBody requestBody = response.errorBody();
//                                    if (requestBody != null) {
//                                        mErrorMsgToShow = AppFunctions.apiResponseErrorMsg(getActivity(), requestBody);
//                                    } else {
//                                        mErrorMsgToShow = getActivity().getString(R.string.process_failed_please_try_again);
//                                    }
//                                } catch (Exception e) {
//                                    // e.printStackTrace();
//                                    mErrorMsgToShow = getActivity().getString(R.string.process_failed_please_try_again);
//                                }
//                                AppFunctions.msgDialogOk(getActivity(), "", mErrorMsgToShow);
//
                        }
                        @Override
                        public void onFailure(@NonNull Call<CommonFunctionOtpDataSet> call, @NonNull Throwable t){
                            mProgressDialog.cancel();
                        }
                    });
                } catch (JSONException e){
                    mProgressDialog.cancel();
                    //  //Log.e("210 Excep ", e.toString());
                    e.printStackTrace();
                }
            }else{
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_login_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }
    }
    private void sendOTP(String mMobile_Number,String mCountry_code,String encriptCode,String now){
        mProgressDialog.show();
        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.telephone, mMobile_Number);
                    jsonObject.put(DefaultNames.countryId, mCountry_code);
                    jsonObject.put(DefaultNames.encriptCode,encriptCode);
                    jsonObject.put(DefaultNames.now,now);

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<OtpSuccess> Call = retrofitInterface.sendOTP(body);

                    Call.enqueue(new Callback<OtpSuccess>(){
                        @Override
                        public void onResponse(@NonNull Call<OtpSuccess> call, @NonNull Response<OtpSuccess> response) {
                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                OtpSuccess otpSuccess = response.body();
                                if(otpSuccess.success != null){
                                    mMobileNumber = mMobile_Number;
                                    mCountryCode = mCountry_code;
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder
                                            .setMessage(otpSuccess.success.getMessage())
                                            //.setTitle(mContext.getString(R.string.))
                                            .setCancelable(true)
                                            .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    simpleCountDownTimer = new SimpleCountDownTimer(0, 30, 1, ForgotPassword.this);
                                                    simpleCountDownTimer.start(false);
                                                    dialog.dismiss();
                                                }
                                            });

                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();

                                    AppFunctions.msgDialogOk(getActivity(), "", otpSuccess.success.getMessage());
                                }else if(otpSuccess.error != null){
                                    mProgressDialog.cancel();
                                    AppFunctions.msgDialogOk(getActivity(), "", otpSuccess.error.message);
                                }
//                            else {
//                                    mProgressDialog.cancel();

//                                    String mErrorMsgToShow = "";
//                                    try {
//
//                                        if (requestBody != null) {
//                                            mErrorMsgToShow = AppFunctions.apiResponseErrorMsg(getActivity(), requestBody);
//                                        } else {
//                                            mErrorMsgToShow = getActivity().getString(R.string.process_failed_please_try_again);
//                                        }
//                                    } catch (Exception e) {
//                                        // e.printStackTrace();
//                                        mErrorMsgToShow = getActivity().getString(R.string.process_failed_please_try_again);
//                                    }
//                                    AppFunctions.msgDialogOk(getActivity(), "", mErrorMsgToShow);
                                }
                            }
                        @Override
                        public void onFailure(@NonNull Call<OtpSuccess> call, @NonNull Throwable t){
                            mProgressDialog.cancel();
                        }
                    });
                } catch (JSONException e){
                    mProgressDialog.cancel();
                    //  //Log.e("210 Excep ", e.toString());
                    e.printStackTrace();
                }
            }else{
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_login_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }
    }

    private void verifyOTP(String mMobile_Number,String mCountry_code,String mVerificationCode){
        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.telephone, mMobile_Number);
                    jsonObject.put(DefaultNames.countryId, mCountry_code);
                    jsonObject.put(DefaultNames.otp,mVerificationCode);

//                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
//                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<OtpSuccess> Call = retrofitInterface.verifyOTP(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<OtpSuccess>(){
                        @Override
                        public void onResponse(@NonNull Call<OtpSuccess> call, @NonNull Response<OtpSuccess> response) {
                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                OtpSuccess otpSuccess = response.body();
                                if(otpSuccess.success != null){
                                    mForgotPwdBinding.layFpOtpParent.setVisibility(View.GONE);
                                    mForgotPwdBinding.layFpNewPwdParent.setVisibility(View.VISIBLE);
                                    mProgressDialog.cancel();

                                }else {
                                    if(otpSuccess.error != null) {
                                        AppFunctions.msgDialogOk(getActivity(), "", otpSuccess.error.message);
                                    }
                                }
                            }
//                            else {
//                                mProgressDialog.cancel();
//                                String mErrorMsgToShow = "";
//                                try {
//                                    ResponseBody requestBody = response.errorBody();
//                                    if (requestBody != null) {
//                                        mErrorMsgToShow = AppFunctions.apiResponseErrorMsg(getActivity(), requestBody);
//                                    } else {
//                                        mErrorMsgToShow = getActivity().getString(R.string.process_failed_please_try_again);
//                                    }
//                                } catch (Exception e) {
//                                    // e.printStackTrace();
//                                    mErrorMsgToShow = getActivity().getString(R.string.process_failed_please_try_again);
//                                }
//                                AppFunctions.msgDialogOk(getActivity(), "", mErrorMsgToShow);
//                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<OtpSuccess> call, @NonNull Throwable t){
                            mProgressDialog.cancel();
                        }
                    });
                } catch (JSONException e){
                    mProgressDialog.cancel();
                    //  //Log.e("210 Excep ", e.toString());
                    e.printStackTrace();
                }
            }else{
                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                mFT.replace(R.id.layout_app_login_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }
    }
    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        Log.e("test phonenumber"+phoneNumber,"");
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

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        // [START verify_with_code]
        Log.e("verifyPhoneNumberWithCode","");
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
                                Log.d("signInWithCredential:", "success");

                                FirebaseUser user = task.getResult().getUser();
                                // Update UI

                                // m_Toast("signInWithCredential", "success");

                                // mProgressDialog.cancel();
                                // isUserAlreadyRegistered();

                                if (simpleCountDownTimer != null) {
                                    simpleCountDownTimer.pause();
                                }


                                mForgotPwdBinding.layFpOtpParent.setVisibility(View.GONE);
                                mForgotPwdBinding.layFpNewPwdParent.setVisibility(View.VISIBLE);

                                mProgressDialog.cancel();

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

    private void callPwdUpdateAPi() {


        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    String mMobile_Number = mForgotPwdBinding.etFpMobile.getText().toString();
                    String mCountry_code = mForgotPwdBinding.tvFpCountryCode.getText().toString();
                    String mConfirmPWD = mForgotPwdBinding.etFpConfirmPwd.getText().toString();

                    jsonObject.put(DefaultNames.telephone, mMobile_Number);
                    jsonObject.put(DefaultNames.countryId, mCountry_code);
                    jsonObject.put(DefaultNames.password, mConfirmPWD);

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ApiResponseCheck> Call = retrofitInterface.forgotPwdApi(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<ApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponseCheck> call, @NonNull Response<ApiResponseCheck> response) {

                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                ApiResponseCheck mApiResponseCheck = response.body();
                                if (mApiResponseCheck != null) {
                                    //Log.e("mApiResponseCheck", "not null");
                                    if (mApiResponseCheck.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            //AppFunctions.msgDialogOk(getActivity(), "", getResources().getString(R.string.You_not_a_exists_user));

                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder
                                                    .setMessage(getActivity().getString(R.string.pwd_updated_success_fully))
                                                    //.setTitle(mContext.getString(R.string.))
                                                    .setCancelable(false)
                                                    .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            dialog.dismiss();
                                                            getParentFragmentManager().popBackStack();
                                                        }
                                                    });
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
                                } else {
                                    //Log.e("mApiResponseCheck", "null");
                                }
                            }else {
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
                mFT.replace(R.id.layout_app_login_body, mNetworkAnalyser, "mNetworkAnalyser");
                mFT.addToBackStack("mNetworkAnalyser");
                mFT.commit();
            }
        }

    }


}