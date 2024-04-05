package com.ordenese.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.onesignal.OneSignal;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.CountryCodeApi;
import com.ordenese.DataSets.LoginApi;
import com.ordenese.DataSets.LoginCountryCodeDataSet;
import com.ordenese.DataSets.LoginCustomerInfoDataSet;
import com.ordenese.DataSets.LoginDataSet;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.LoginCountryCodeDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Interfaces.CountryCodeSelection;
import com.ordenese.R;
import com.ordenese.databinding.LoginBinding;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends Fragment implements View.OnClickListener, CountryCodeSelection {

    private LoginBinding mLoginBinding;
    private RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;
    private CountryCodeApi mCountryCodeApi;
    private LoginApi mLoginApi;

    public Login() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.login, container, false);
        mLoginBinding = LoginBinding.inflate(inflater,container,false);

        mLoginBinding.imgLoginBack.setOnClickListener(this);
        mLoginBinding.layLoginBtnContainer.setOnClickListener(this);
        mLoginBinding.tvLoginForgotPwd.setOnClickListener(this);
        mLoginBinding.tvLoginCreateAnAccount.setOnClickListener(this);
        mLoginBinding.layCountryCodeContainer.setOnClickListener(this);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        callCountryCodeListAPi();


        return mLoginBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();
        if(mId == R.id.img_login_back){
            if(getActivity() != null){
                getActivity().finish();
            }
        }else if(mId == R.id.lay_login_btn_container){
            if(getActivity() != null){

                String mMobile_Number = mLoginBinding.etLoginMobile.getText().toString();
                String mPass_word = mLoginBinding.etLoginPassword.getText().toString();

                if(!mMobile_Number.isEmpty() && !mPass_word.isEmpty()){
                    //AppFunctions.msgDialogOk(getActivity(),"","successs..");

                    toHideDeviceKeyboard();

                    callLoginAPi();
                }else {
                    if(mMobile_Number.isEmpty()){
                        toHideDeviceKeyboard();
                        AppFunctions.msgDialogOk(getActivity(),"",getActivity().getResources().getString(R.string.please_enter_your_mobile_no));
                    }else {
                        if(mPass_word.isEmpty()){
                            toHideDeviceKeyboard();
                            AppFunctions.msgDialogOk(getActivity(),"",getActivity().getResources().getString(R.string.please_enter_your_pwd));
                        }
                    }
                }

            }
        }else if(mId == R.id.tv_login_forgot_pwd){
            if(getActivity() != null){

                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                ForgotPassword m_forgotPassword = new ForgotPassword();
                mFT.replace(R.id.layout_app_login_body, m_forgotPassword, "m_forgotPassword");
                mFT.addToBackStack("m_forgotPassword");
                mFT.commit();


            }
        }else if(mId == R.id.tv_login_create_an_account){
            if(getActivity() != null){

                FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                RegisterWithOTP m_registerWithOTP = new RegisterWithOTP();
                mFT.replace(R.id.layout_app_login_body, m_registerWithOTP, "m_registerWithOTP");
                mFT.addToBackStack("m_registerWithOTP");
                mFT.commit();

            }
        }else if(mId == R.id.lay_country_code_container){
            if(getActivity() != null){

                if (mCountryCodeApi != null) {
                    //Log.e("mCountryCodeApi", "not null");
                    if (mCountryCodeApi.success != null) {
                        //Api response successDataSet :-
                        if (getActivity() != null) {

                            if (mCountryCodeApi.countryList != null && mCountryCodeApi.countryList.size() > 0) {
                                DialogueCountryCodeList mDCountryCodeList = new DialogueCountryCodeList().newInstance(mCountryCodeApi.countryList);
                                mDCountryCodeList.setTargetFragment(Login.this,2975);
                                mDCountryCodeList.show(getParentFragmentManager(),"mDCountryCodeList");
                            }
                        }
                    }
                }


            }
        }



    }

    private  void toHideDeviceKeyboard(){
        if(getActivity() != null){
            if(mLoginBinding != null){
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                // To get the correct window token, lets first get the currently focused view
                View v__iew = mLoginBinding.getRoot();
                // To get the window token when there is no currently focused view, we have a to create a view
                if (v__iew == null) {
                    v__iew = new View(getActivity());
                }
                // hide the keyboard
                imm.hideSoftInputFromWindow(v__iew.getWindowToken(), 0);
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

                                                if(!LoginCountryCodeDB.getInstance(getActivity()).check_selected()){
                                                    //If there is no previous selection then default selection as first position of list :-

                                                    //default country id india :-
                                                    //country id : 100
                                                    int getCountryPosition = 0;
                                                    for(int country =0;country < mCountryCodeApi.countryList.size(); country++){
                                                        if(mCountryCodeApi.countryList.get(country).getId().equals("100")){
                                                            getCountryPosition = country;
                                                        }
                                                    }

                                                    String mPhoneCode = ""+mCountryCodeApi.countryList.get(getCountryPosition).getCode();
                                                    mLoginBinding.tvLoginCountryCode.setText(mPhoneCode);
                                                    if(getActivity() != null){
                                                        // Glide.with(getActivity()).load(mCountryCodeList.get(getCountryPosition).getImage()).into(flagImg);
                                                    }

                                                }else {

                                                    LoginCountryCodeDataSet loginCCDs = LoginCountryCodeDB.getInstance(getActivity()).get_Details();
                                                    for(int phoneCode = 0;phoneCode < mCountryCodeApi.countryList.size();phoneCode++){
                                                        if(mCountryCodeApi.countryList.get(phoneCode).getId().equals(loginCCDs.getCountryId())) {
                                                            mLoginBinding.tvLoginCountryCode.setText(loginCCDs.getPhoneCode());
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
        mLoginBinding.tvLoginCountryCode.setText(mLoginNCDs.getPhoneCode());
    }

    private void callLoginAPi() {


        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    String mMobile_Number = mLoginBinding.etLoginMobile.getText().toString();
                    String mCountry_code = mLoginBinding.tvLoginCountryCode.getText().toString();
                    String mPass_word = mLoginBinding.etLoginPassword.getText().toString();

                    jsonObject.put(DefaultNames.telephone, mMobile_Number);
                    jsonObject.put(DefaultNames.countryId, mCountry_code);

                    jsonObject.put(DefaultNames.password, mPass_word);
                    if (OneSignal.getDeviceState() != null && OneSignal.getDeviceState().getUserId() != null) {
                        jsonObject.put(DefaultNames.push_id, OneSignal.getDeviceState().getUserId());
                    } else {
                        jsonObject.put(DefaultNames.push_id, "");
                    }
                    jsonObject.put(DefaultNames.device_type, "1"); //1 - android device.

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
                    Call<LoginApi> Call = retrofitInterface.loginApi(body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<LoginApi>() {
                        @Override
                        public void onResponse(@NonNull Call<LoginApi> call, @NonNull Response<LoginApi> response) {

                            mProgressDialog.cancel();
                            if (response.isSuccessful()) {
                                mLoginApi = response.body();
                                if (mLoginApi != null) {
                                    if (mLoginApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {

                                            LoginCustomerInfoDataSet mLoginUserInfo = mLoginApi.loginCustomerInfo;

                                            LoginDataSet mLoginDs = new LoginDataSet();

                                            mLoginDs.setCustomerId(mLoginUserInfo.customer_id);
                                            mLoginDs.setCustomerKey(mLoginUserInfo.secret_key);
                                            mLoginDs.setFirstName(mLoginUserInfo.firstname);
                                            mLoginDs.setLastName(mLoginUserInfo.lastname);
                                            mLoginDs.setEmail(mLoginUserInfo.email);
                                            mLoginDs.setTelephone(mLoginUserInfo.telephone);

                                            //The following two values filled by local app values!.
                                            mLoginDs.setMobileCountryCodeId(LoginCountryCodeDB.getInstance(getActivity()).get_Details().getCountryId());
                                            mLoginDs.setMobileCountryCode(mCountry_code);

                                            String mUserImage = mLoginUserInfo.image;
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
                                            if (mLoginApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mLoginApi.error.message);
                                            }
                                        }
                                    }
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
                        public void onFailure(@NonNull Call<LoginApi> call, @NonNull Throwable t) {

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