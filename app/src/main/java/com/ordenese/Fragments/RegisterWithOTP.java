package com.ordenese.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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
import com.onesignal.OneSignal;
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
import com.ordenese.DataSets.LoginCountryCodeDataSet;
import com.ordenese.DataSets.LoginDataSet;
import com.ordenese.DataSets.OtpSuccess;
import com.ordenese.DataSets.RegisterApi;
import com.ordenese.DataSets.RegisterCustomerInfoDataSet;
import com.ordenese.DataSets.Success;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.LoginCountryCodeDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CountryCodeSelection;
import com.ordenese.R;
import com.ordenese.databinding.RegisterWithOtpBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterWithOTP extends Fragment implements View.OnClickListener, CountryCodeSelection,
        SimpleCountDownTimer.OnCountDownListener {

    private RegisterWithOtpBinding mRWOtpBinding;

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
    boolean isFirebase = false;
    String mMobile_Number = "";
            String mCountry_code ="";
    public RegisterWithOTP() {
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
        //return inflater.inflate(R.layout.register_with_otp, container, false);

        mRWOtpBinding = RegisterWithOtpBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mRWOtpBinding.imgRwoBack.setOnClickListener(this);

        mRWOtpBinding.layRwoMobileNoParent.setVisibility(View.VISIBLE);
        mRWOtpBinding.layRwoOtpParent.setVisibility(View.GONE);
        mRWOtpBinding.tvRwoOtpDidNotReceive.setVisibility(View.GONE);
        mRWOtpBinding.layRwoNewPwdParent.setVisibility(View.GONE);

        mRWOtpBinding.layRwoMobNoSubmitBtnContainer.setOnClickListener(this);
        mRWOtpBinding.layRwoOtpSubmitBtnContainer.setOnClickListener(this);
        mRWOtpBinding.layRwoOtpResend.setOnClickListener(this);
        mRWOtpBinding.layRwoPwdSubmitBtnContainer.setOnClickListener(this);
        mRWOtpBinding.layRwoCountryCodeContainer.setOnClickListener(this);
        mRWOtpBinding.layRwoPwdSubmitBtnContainer.setOnClickListener(this);

        //Because , manually accept the terms and conditions:-
        mRWOtpBinding.chkBoxTermsAndConditionsAgree.setChecked(false);

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

                 signInWithPhoneAuthCredential(credential);

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
                                    simpleCountDownTimer = new SimpleCountDownTimer(0, 30, 1, RegisterWithOTP.this);
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


        String mData1 = getResources().getString(R.string.register_agreement);
        SpannableString ss1 = new SpannableString(Html.fromHtml(mData1));
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                DialogueWebView mDialogueWebView = new DialogueWebView();
                Bundle mBundle = new Bundle();
                mBundle.putString(DefaultNames.from, DefaultNames.TermsAndConditions);
                mBundle.putString(DefaultNames.thePageCallFrom,DefaultNames.thePageCall_ForLoginLay);
                mDialogueWebView.setArguments(mBundle);
                mDialogueWebView.show(getParentFragmentManager(), "mDialogueWebView");
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                //ds.setColor(ds.linkColor);    // you can use custom color
                if (getActivity() != null) {
                    ds.setColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));    // you can use custom color
                }
                ds.setUnderlineText(false);    // this remove the underline
            }
        };

        //Log.e("Length txt",""+ss1.length());

        if(AppFunctions.mIsArabic(getActivity())){
            //Its a arabic language :-
            ss1.setSpan(clickableSpan1, 19, 35, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else {
            //Its english or other language :-
            ss1.setSpan(clickableSpan1, 29, 49, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        mRWOtpBinding.tvRwoRegisterAgreement.setText(ss1);
        mRWOtpBinding.tvRwoRegisterAgreement.setMovementMethod(LinkMovementMethod.getInstance());


        return mRWOtpBinding.getRoot();
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
            if (mRWOtpBinding != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                // To get the correct window token, lets first get the currently focused view
                View v__iew = mRWOtpBinding.getRoot();
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
        if (mId == R.id.img_rwo_back) {
            if (getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }
        } else if (mId == R.id.lay_rwo_mob_no_submit_btn_container) {
            if (getActivity() != null) {
                String mMobile_Number = mRWOtpBinding.etRwoMobile.getText().toString();

                if (!mMobile_Number.isEmpty()) {
                    //AppFunctions.msgDialogOk(getActivity(),"","successs..");
                    toHideDeviceKeyBoard();
                    callMobileNoExistsCheckAPi();
                    /*mRWOtpBinding.layRwoMobileNoParent.setVisibility(View.GONE);
                    mRWOtpBinding.layRwoOtpParent.setVisibility(View.VISIBLE);
                    mRWOtpBinding.tvRwoOtpDidNotReceive.setVisibility(View.VISIBLE);
                    mRWOtpBinding.layRwoOtpResend.setVisibility(View.GONE);
                    simpleCountDownTimer = new SimpleCountDownTimer(0, 30, 1, RegisterWithOTP.this);
                    simpleCountDownTimer.start(false);*/
                } else {
                    toHideDeviceKeyBoard();
                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_mobile_no));
                }
            }
        } else if (mId == R.id.lay_rwo_otp_submit_btn_container) {
            if (getActivity() != null) {
                String mOTP = mRWOtpBinding.etRwoOtp.getText().toString();

                if (!mOTP.isEmpty()) {
                    //AppFunctions.msgDialogOk(getActivity(),"","successs..");

                    toHideDeviceKeyBoard();
                    mProgressDialog.show();

                    if(isFirebase){
                        verifyPhoneNumberWithCode(mVerificationId, mOTP);
                    }else {
                        verifyOTP( mRWOtpBinding.etRwoMobile.getText().toString(),mRWOtpBinding.tvRwoCountryCode.getText().toString(),mOTP);
                    }
                    /*mRWOtpBinding.layRwoOtpParent.setVisibility(View.GONE);
                    mRWOtpBinding.layRwoNewPwdParent.setVisibility(View.VISIBLE);*/

                } else {
                    toHideDeviceKeyBoard();
                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_otp));
                }

            }
        } else if (mId == R.id.lay_rwo_otp_resend) {
            if (getActivity() != null) {

                mRWOtpBinding.layRwoOtpResend.setVisibility(View.GONE);
                mRWOtpBinding.tvRwoOtpDidNotReceive.setVisibility(View.VISIBLE);
                mProgressDialog.show();

                if(isFirebase){
                    resendVerificationCode(mMOBNoWithCCodeWithPlus, mResendToken);
                }else{
                    CommonFunctionOtp(mMobile_Number,mCountry_code);
                }

            }
        } else if (mId == R.id.lay_rwo_pwd_submit_btn_container) {

            if (getActivity() != null) {

                toHideDeviceKeyBoard();

                String mFName = mRWOtpBinding.etRwoFirstName.getText().toString();
                String mLName = mRWOtpBinding.etRwoLastName.getText().toString();
                String mEmail = mRWOtpBinding.etRwoEmail.getText().toString();

                String mPwd = mRWOtpBinding.etRwoPwd.getText().toString();
                String mConfirmPwd = mRWOtpBinding.etRwoConfirmPwd.getText().toString();

                if (!mFName.isEmpty() &&
                        !mLName.isEmpty() &&
                        !mEmail.isEmpty() &&
                        !mPwd.isEmpty() &&
                        !mConfirmPwd.isEmpty()) {

                    if (AppFunctions.emailFormatValidation(mEmail)) {
                        if (mPwd.equals(mConfirmPwd)) {
                            //AppFunctions.msgDialogOk(getActivity(),"","successs..");

                            if (mRWOtpBinding.chkBoxTermsAndConditionsAgree.isChecked()) {
                                //chk_box_terms_and_conditions_agree
                                callRegisterAPi();
                            } else {
                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_agree_to_terms_and_conditions));
                            }


                        } else {

                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.pwd_and_confirm_pwd_not_match));
                        }
                    } else {

                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.enter_valid_email_id));
                    }

                } else {

                    if (mFName.isEmpty()) {

                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_first_name));
                    } else {
                        if (mLName.isEmpty()) {

                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_last_name));
                        } else {
                            if (mEmail.isEmpty()) {

                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_email));
                            } else {
                                if (mPwd.isEmpty()) {

                                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_pwd));
                                } else {
                                    if (mConfirmPwd.isEmpty()) {
                                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_the_confirm_pwd));
                                    }
                                }
                            }
                        }


                    }


                }
            }
        } else if (mId == R.id.lay_rwo_country_code_container) {
            if (getActivity() != null) {

                if (mCountryCodeApi != null) {
                    //Log.e("mCountryCodeApi", "not null");
                    if (mCountryCodeApi.success != null) {
                        //Api response successDataSet :-
                        if (getActivity() != null) {

                            if (mCountryCodeApi.countryList != null && mCountryCodeApi.countryList.size() > 0) {
                                DialogueCountryCodeList mDCountryCodeList = new DialogueCountryCodeList().newInstance(mCountryCodeApi.countryList);
                                mDCountryCodeList.setTargetFragment(RegisterWithOTP.this, 2975);
                                mDCountryCodeList.show(getParentFragmentManager(), "mDCountryCodeList");
                            }
                        }
                    }
                }


            }
        }

        //lay_rwo_pwd_submit_btn_container

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
                                                    mRWOtpBinding.tvRwoCountryCode.setText(mPhoneCode);
                                                    if (getActivity() != null) {
                                                        // Glide.with(getActivity()).load(mCountryCodeList.get(getCountryPosition).getImage()).into(flagImg);
                                                    }

                                                } else {

                                                    LoginCountryCodeDataSet loginCCDs = LoginCountryCodeDB.getInstance(getActivity()).get_Details();
                                                    for (int phoneCode = 0; phoneCode < mCountryCodeApi.countryList.size(); phoneCode++) {
                                                        if (mCountryCodeApi.countryList.get(phoneCode).getId().equals(loginCCDs.getCountryId())) {
                                                            mRWOtpBinding.tvRwoCountryCode.setText(loginCCDs.getPhoneCode());
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
        mRWOtpBinding.tvRwoCountryCode.setText(mLoginNCDs.getPhoneCode());
    }

    private void callMobileNoExistsCheckAPi() {
        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    mMobile_Number = mRWOtpBinding.etRwoMobile.getText().toString();
                    mCountry_code = mRWOtpBinding.tvRwoCountryCode.getText().toString();

                    jsonObject.put(DefaultNames.telephone, mMobile_Number);
                    jsonObject.put(DefaultNames.countryId, mCountry_code);

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ApiResponseCheck> Call = retrofitInterface.checkCustomerApi(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<ApiResponseCheck>(){
                        @Override
                        public void onResponse(@NonNull Call<ApiResponseCheck> call, @NonNull Response<ApiResponseCheck> response) {
                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                ApiResponseCheck mApiResponseCheck = response.body();
                                if (mApiResponseCheck != null) {
                                    //  //Log.e("mApiResponseCheck", "not null");
                                    if (mApiResponseCheck.success != null) {
                                        Log.e("1","");
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            Log.e("2","");
                                            //If response is success.Then its refers a new customer.
                                            mRWOtpBinding.layRwoMobileNoParent.setVisibility(View.GONE);
                                            mRWOtpBinding.layRwoOtpParent.setVisibility(View.VISIBLE);
                                            mRWOtpBinding.tvRwoOtpDidNotReceive.setVisibility(View.VISIBLE);
                                            mRWOtpBinding.layRwoOtpResend.setVisibility(View.GONE);

                                            mProgressDialog.show();
                                            mMOBNoWithCCodeWithPlus = "+" + mCountry_code + "" + mMobile_Number;
                                            String mPrefix = getActivity().getString(R.string.enter_4_digit);
                                            String mMobileNumber = " " + mMOBNoWithCCodeWithPlus;
                                            String mData = mPrefix + mMobileNumber;
                                            mRWOtpBinding.tvRwoOtpMsgWithMobileNumber.setText(mData);
//                                            startPhoneNumberVerification(mMOBNoWithCCodeWithPlus);
                                            CommonFunctionOtp(mMobile_Number,mCountry_code);
                                        }
                                    } else {
                                        //If response is failure.Then its refers a exists customer.
                                        if (getActivity() != null) {
                                            if (mApiResponseCheck.error != null) {
                                                //If response has error.Then its refers customer exists. So proceed forgot pwd otp :-
                                                AppFunctions.msgDialogOk(getActivity(), "", getResources().getString(R.string.You_already_exists_user));
                                            }
                                        }
                                    }
                                } else {
                                    //  //Log.e("mApiResponseCheck", "null");
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

                    //  //Log.e("210 Excep ", e.toString());
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
            mRWOtpBinding.tvRwoOtpDidNotReceive.setText(mFinalData);
            //tv_rwo_otp_did_not_receive

        }
    }

    @Override
    public void onCountDownFinished() {
        mRWOtpBinding.tvRwoOtpDidNotReceive.setVisibility(View.GONE);
        mRWOtpBinding.layRwoOtpResend.setVisibility(View.VISIBLE);
        toHideDeviceKeyBoard();
    }

    private void CommonFunctionOtp(String mMobile_Number,String mCountry_code){
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
                    mProgressDialog.show();
                    Call.enqueue(new Callback<CommonFunctionOtpDataSet>(){
                        @Override
                        public void onResponse(@NonNull Call<CommonFunctionOtpDataSet> call, @NonNull Response<CommonFunctionOtpDataSet> response) {
                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                CommonFunctionOtpDataSet commonFunctionOtpDataSet = response.body();
                                if(commonFunctionOtpDataSet.otpType.equals("firebase")){
                                    isFirebase = true;
                                    startPhoneNumberVerification(mMOBNoWithCCodeWithPlus);
                                }else {
                                    isFirebase = false;
                                    sendOTP(mMobile_Number,mCountry_code,commonFunctionOtpDataSet.encript.encriptCode,commonFunctionOtpDataSet.encript.now);
                                }
                            } else {
                                mProgressDialog.cancel();
                                String mErrorMsgToShow = "";
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
                            }
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
        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.telephone, mMobile_Number);
                    jsonObject.put(DefaultNames.countryId, mCountry_code);
                    jsonObject.put(DefaultNames.encriptCode,encriptCode);
                    jsonObject.put(DefaultNames.now,now);

//                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
//                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<OtpSuccess> Call = retrofitInterface.sendOTP(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<OtpSuccess>(){
                        @Override
                        public void onResponse(@NonNull Call<OtpSuccess> call, @NonNull Response<OtpSuccess> response) {
                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                OtpSuccess otpSuccess = response.body();

                                if(otpSuccess.success != null){
                                    mMobileNumber = mMobile_Number;
                                    mCountryCode = mCountry_code;

                                    if (getActivity() != null) {
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                        alertDialogBuilder
                                                .setMessage(otpSuccess.success.getMessage())
                                                //.setTitle(mContext.getString(R.string.))
                                                .setCancelable(true)
                                                .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        simpleCountDownTimer = new SimpleCountDownTimer(0, 30, 1, RegisterWithOTP.this);
                                                        simpleCountDownTimer.start(false);
                                                        dialog.dismiss();
                                                    }
                                                });

                                        AlertDialog alertDialog = alertDialogBuilder.create();
                                        alertDialog.show();
                                    }
                                }else if(otpSuccess.error != null){
                                    mProgressDialog.cancel();
                                    AppFunctions.msgDialogOk(getActivity(), "", otpSuccess.error.message);
                                }
//                                else {
//                                    mProgressDialog.cancel();
//                                    String mErrorMsgToShow = "";
//                                    try {
//                                        ResponseBody requestBody = response.errorBody();
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
//                                }
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

                                    mRWOtpBinding.layRwoOtpParent.setVisibility(View.GONE);
                                    mRWOtpBinding.layRwoNewPwdParent.setVisibility(View.VISIBLE);

                                    mProgressDialog.cancel();
                                }else {
                                    if(otpSuccess.error != null) {
                                        AppFunctions.msgDialogOk(getActivity(), "", otpSuccess.error.message);
                                    }
                                }
                            }
//                            else {
//                                    mProgressDialog.cancel();
//                                    String mErrorMsgToShow = "";
//                                    try {
//                                        ResponseBody requestBody = response.errorBody();
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
//                                }
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
        if (getActivity() != null) {
            mAuth.getFirebaseAuthSettings().setAppVerificationDisabledForTesting(false);
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

        Log.e("2verifyPhoneNumberWithCode","");
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
                                Log.d("2signInWithCredential:", "success");

                                FirebaseUser user = task.getResult().getUser();
                                // Update UI

                                // m_Toast("signInWithCredential", "success");

                                // mProgressDialog.cancel();
                                // isUserAlreadyRegistered();

                                if (simpleCountDownTimer != null) {
                                    simpleCountDownTimer.pause();
                                }

                                mRWOtpBinding.layRwoOtpParent.setVisibility(View.GONE);
                                mRWOtpBinding.layRwoNewPwdParent.setVisibility(View.VISIBLE);

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

    private void callRegisterAPi() {


        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    String mF_Name = mRWOtpBinding.etRwoFirstName.getText().toString();
                    String mL_Name = mRWOtpBinding.etRwoLastName.getText().toString();
                    String mEmail = mRWOtpBinding.etRwoEmail.getText().toString();
                    String mMobile_Number = mRWOtpBinding.etRwoMobile.getText().toString();
                    String mCountry_code = mRWOtpBinding.tvRwoCountryCode.getText().toString();
                    String mConfirmPWD = mRWOtpBinding.etRwoConfirmPwd.getText().toString();

                    jsonObject.put(DefaultNames.firstName, mF_Name);
                    jsonObject.put(DefaultNames.lastName, mL_Name);
                    jsonObject.put(DefaultNames.email, mEmail);
                    jsonObject.put(DefaultNames.telephone, mMobile_Number);
                    jsonObject.put(DefaultNames.countryId, mCountry_code);
                    jsonObject.put(DefaultNames.password, mConfirmPWD);

                    if (OneSignal.getDeviceState() != null && OneSignal.getDeviceState().getUserId() != null) {
                        jsonObject.put(DefaultNames.push_id, OneSignal.getDeviceState().getUserId());
                    } else {
                        jsonObject.put(DefaultNames.push_id, "");
                    }
                    jsonObject.put(DefaultNames.device_type, "1"); // 1 - android.
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

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<RegisterApi> Call = retrofitInterface.registerApi(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<RegisterApi>() {
                        @Override
                        public void onResponse(@NonNull Call<RegisterApi> call, @NonNull Response<RegisterApi> response) {

                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                RegisterApi mRegisterApi = response.body();
                                if (mRegisterApi != null) {
                                    //Log.e("mRegisterApi", "not null");
                                    if (mRegisterApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            //AppFunctions.msgDialogOk(getActivity(), "", getResources().getString(R.string.You_not_a_exists_user));

                                            RegisterCustomerInfoDataSet mRegisterCustomerInfo = mRegisterApi.customerInfo;

                                            LoginDataSet mLoginDs = new LoginDataSet();

                                            mLoginDs.setCustomerId(mRegisterCustomerInfo.customer_id);
                                            mLoginDs.setCustomerKey(mRegisterCustomerInfo.secret_key);
                                            mLoginDs.setFirstName(mRegisterCustomerInfo.firstname);
                                            mLoginDs.setLastName(mRegisterCustomerInfo.lastname);
                                            mLoginDs.setEmail(mRegisterCustomerInfo.email);
                                            mLoginDs.setTelephone(mRegisterCustomerInfo.telephone);

                                            //The following two values filled by local app values!.
                                            mLoginDs.setMobileCountryCodeId(LoginCountryCodeDB.getInstance(getActivity()).get_Details().getCountryId());
                                            mLoginDs.setMobileCountryCode(mCountry_code);

                                            String mUserImage = mRegisterCustomerInfo.image;
                                            if (mUserImage != null) {
                                                String mFinalPath = mUserImage.replace("\\", "");
                                                // ////Log.e("mFinalPath",mFinalPath);
                                                mLoginDs.setImage(mFinalPath);
                                            } else {
                                                mLoginDs.setImage("");
                                            }

                                            if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                                //safe check :-
                                                UserDetailsDB.getInstance(getActivity()).deleteUserDetailsDB();
                                            }
                                            UserDetailsDB.getInstance(getActivity()).addUserDetails(mLoginDs);

                                            getActivity().finish();

                                        }
                                    } else {
                                        //Api response failure :-
                                        if (getActivity() != null) {
                                            if (mRegisterApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mRegisterApi.error.message);
                                            }
                                        }
                                    }
                                } else {
                                    //Log.e("mRegisterApi", "null");
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
                        public void onFailure(@NonNull Call<RegisterApi> call, @NonNull Throwable t) {

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