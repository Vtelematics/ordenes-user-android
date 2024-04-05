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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.R;
import com.ordenese.databinding.ChangePasswordBinding;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassword extends Fragment implements View.OnClickListener {

    private ChangePasswordBinding mCPwdBinding;
    private ImageView mImgBack;
    private ProgressDialog mProgressDialog;
    private RetrofitInterface retrofitInterface;

    public ChangePassword() {
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
        //return inflater.inflate(R.layout.change_password, container, false);
        mCPwdBinding = ChangePasswordBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mCPwdBinding.imgCpBack.setOnClickListener(this);
        mCPwdBinding.layCpSubmitBtnContainer.setOnClickListener(this);

        return mCPwdBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if (mId == R.id.img_cp_back) {
            if (getActivity() != null) {
                hideKeyboard(getActivity());
                getParentFragmentManager().popBackStack();
            }

        } else if (mId == R.id.lay_cp_submit_btn_container) {
            if (getActivity() != null) {
                hideKeyboard(getActivity());
                check();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }


    public static void hideKeyboard(Activity activity) {

        InputMethodManager mInputMethodMgr = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        if (mInputMethodMgr != null) {

            mInputMethodMgr.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }


    }

    private void check() {

        if (getActivity() != null) {
            if (!mCPwdBinding.etCpOldPwd.getText().toString().isEmpty()
                    && !mCPwdBinding.etCpNewPwd.getText().toString().isEmpty()
                    && !mCPwdBinding.etCpConfirmPwd.getText().toString().isEmpty()) {

                if (mCPwdBinding.etCpNewPwd.getText().toString().equals(mCPwdBinding.etCpConfirmPwd.getText().toString())) {

                    callChangePwdAPi();

                } else {
                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.pwd_and_confirm_pwd_not_match));
                }


            } else {
                if (mCPwdBinding.etCpOldPwd.getText().toString().isEmpty()) {

                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_old_pwd));

                } else {

                    if (mCPwdBinding.etCpNewPwd.getText().toString().isEmpty()) {

                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_new_pwd));

                    } else {

                        if (mCPwdBinding.etCpConfirmPwd.getText().toString().isEmpty()) {
                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_the_confirm_pwd));

                        }

                    }
                }
            }


        }


    }


    private void callChangePwdAPi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put(DefaultNames.old_password, mCPwdBinding.etCpOldPwd.getText().toString());
                    jsonObject.put(DefaultNames.password, mCPwdBinding.etCpNewPwd.getText().toString());
                    jsonObject.put(DefaultNames.confirm_password, mCPwdBinding.etCpConfirmPwd.getText().toString());

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }
                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ApiResponseCheck> Call = retrofitInterface.changePasswordApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<ApiResponseCheck>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponseCheck> call, @NonNull Response<ApiResponseCheck> response) {

                            mProgressDialog.cancel();

                            if (getActivity() != null) {
                                if (response.isSuccessful()) {

                                    ApiResponseCheck mApiResponseCheck = response.body();
                                    if (mApiResponseCheck != null) {
                                        //Log.e("mApiResponseCheck", "not null");
                                        if (mApiResponseCheck.success != null) {
                                            //Api response successDataSet :-
                                            if (getActivity() != null) {

                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder
                                                        .setMessage(mApiResponseCheck.success.message)
                                                        .setCancelable(false)
                                                        .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        getParentFragmentManager().popBackStack();
                                                                    }
                                                                }
                                                        );

                                                AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.show();
                                            }

                                        } else {
                                            //Api response failure :-
                                            if (mApiResponseCheck.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mApiResponseCheck.error.message);
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


}