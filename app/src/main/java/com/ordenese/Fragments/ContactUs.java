package com.ordenese.Fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputLayout;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.R;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactUs extends Fragment implements View.OnClickListener {

    View viewContactUs;

    TextInputLayout mFirstName, mEmailId, mTelephone,
            mEnquiry;
    EditText mFirstNameEditText, mEmailIdEditText, mTelephoneEditText,
            mEnquiryEditText;
    LinearLayout mSubmitBtn;

    private RetrofitInterface retrofitInterface;
    private ImageView mImgBack;
    private ProgressDialog mProgressDialog;


    public ContactUs() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewContactUs = inflater.inflate(R.layout.contact_us, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        action();
        return viewContactUs;
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void action() {
        /*TextInputLayout*/
        mFirstName = viewContactUs.findViewById(R.id.contact_form_first_name_text_input_layout);
        mEmailId = viewContactUs.findViewById(R.id.contact_form_text_input_layout);
        mTelephone = viewContactUs.findViewById(R.id.contact_form_telephone_text_input_layout);
        mEnquiry = viewContactUs.findViewById(R.id.contact_form_message_text_input_layout);
        /*EditText*/
        mFirstNameEditText = viewContactUs.findViewById(R.id.contact_form_first_name_edit_text);
        mEmailIdEditText = viewContactUs.findViewById(R.id.contact_form_email_edit_text);
        mTelephoneEditText = viewContactUs.findViewById(R.id.contact_form_telephone_edit_text);
        mEnquiryEditText = viewContactUs.findViewById(R.id.contact_form_message_edit_text);


        mSubmitBtn = viewContactUs.findViewById(R.id.lay_cu_submit_btn_container);


        mImgBack = viewContactUs.findViewById(R.id.img_cu_back);

        mImgBack.setOnClickListener(this);
        mSubmitBtn.setOnClickListener(this);


    }


    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if (mId == R.id.img_cu_back) {
            if(getActivity() != null){
                hideKeyboard(getActivity());
                getParentFragmentManager().popBackStack();
            }
        } else if (mId == R.id.lay_cu_submit_btn_container) {
            if(getActivity() != null){
                hideKeyboard(getActivity());
                check();
            }

        }

    }

    private void check() {

        if (!mFirstNameEditText.getText().toString().isEmpty()
                && !mEmailIdEditText.getText().toString().isEmpty()
                && !mTelephoneEditText.getText().toString().isEmpty()
                && !mEnquiryEditText.getText().toString().isEmpty()
                && AppFunctions.emailFormatValidation(mEmailIdEditText.getText().toString())
        ) {

            callContactUsAPi();

        }


        if (getActivity() != null) {

            if (mFirstNameEditText.getText().toString().isEmpty()) {

                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_name));

            } else {

                if (mEmailIdEditText.getText().toString().isEmpty()) {

                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_email));

                } else {

                    if (!AppFunctions.emailFormatValidation(mEmailIdEditText.getText().toString())) {
                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_valid_email_address));
                    } else {

                        if (mTelephoneEditText.getText().toString().isEmpty()) {
                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_mobile_number));

                        } else {
                            if (mEnquiryEditText.getText().toString().isEmpty()) {
                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_message));
                            }
                        }
                    }
                }
            }


        }


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

    private void callContactUsAPi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put(DefaultNames.name, mFirstNameEditText.getText().toString());
                    jsonObject.put(DefaultNames.email, mEmailIdEditText.getText().toString());
                    jsonObject.put(DefaultNames.mobile, mTelephoneEditText.getText().toString());
                    jsonObject.put(DefaultNames.message, mEnquiryEditText.getText().toString());
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ApiResponseCheck> Call = retrofitInterface.contactUsApi(body);
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

                                                mFirstNameEditText.setText("");
                                                mEmailIdEditText.setText("");
                                                mTelephoneEditText.setText("");
                                                mEnquiryEditText.setText("");

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
