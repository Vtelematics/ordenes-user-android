package com.ordenese.Fragments;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.bumptech.glide.Glide;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.DataSets.LoginApi;
import com.ordenese.DataSets.LoginCustomerInfoDataSet;
import com.ordenese.DataSets.LoginDataSet;
import com.ordenese.DataSets.ProfilePictureApi;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.LoginCountryCodeDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.R;
import com.ordenese.databinding.EditProfileBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class EditProfile extends Fragment implements View.OnClickListener {

    private EditProfileBinding mEPBinding;
    private RetrofitInterface retrofitInterface;
    private ProgressDialog mProgressDialog;
    Bitmap mSelectedImage;
    private ActivityResultLauncher<Intent> mOpenImgActivityRLauncher;
    private ActivityResultLauncher<String> mImagePermissionResult;

    private ProfilePictureApi mProfilePictureApi;


    public EditProfile() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //  return inflater.inflate(R.layout.edit_profile, container, false);
        mEPBinding = EditProfileBinding.inflate(inflater, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);

        mEPBinding.imgEpBack.setOnClickListener(this);
        mEPBinding.layEpSubmitBtnContainer.setOnClickListener(this);
        mEPBinding.imgEpProfileImage.setOnClickListener(this);

//        if (getActivity() != null) {
//            if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
//
//                String mFirstName = UserDetailsDB.getInstance(getActivity()).getUserDetails().getFirstName();
//                String mLastName = UserDetailsDB.getInstance(getActivity()).getUserDetails().getLastName();
//                String mEmail = UserDetailsDB.getInstance(getActivity()).getUserDetails().getEmail();
//                String mMobile = UserDetailsDB.getInstance(getActivity()).getUserDetails().getTelephone();
//
//                //mEPBinding.etEpFirstNameEditText
//                //mEPBinding.etEpLastNameEditText
//                //mEPBinding.etEpEmailEditText
//                //mEPBinding.etEpTelephoneEditText
//
//                mEPBinding.etEpFirstNameEditText.setText(mFirstName);
//                mEPBinding.etEpLastNameEditText.setText(mLastName);
//                mEPBinding.etEpEmailEditText.setText(mEmail);
//                mEPBinding.etEpTelephoneEditText.setText(mMobile);
//
//                //  Log.e("img", UserDetailsDB.getInstance(getActivity()).getUserDetails().getImage());
//
//                String mProfileIMAGE = UserDetailsDB.getInstance(getActivity()).getUserDetails().getImage();
//                if (mProfileIMAGE != null && !mProfileIMAGE.isEmpty()) {
//                    if (getActivity() != null) {
//                        Glide.with(getActivity()).load(UserDetailsDB.getInstance(getActivity()).getUserDetails().getImage()).into(mEPBinding.imgEpProfileImage);
//                    }
//                } else {
//                    mEPBinding.imgEpProfileImage.setImageResource(R.drawable.svg_account_circle_48dp);
//                }
//            }
//        }

        mImagePermissionResult = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        if (result) {
                            // Log.e(TAG, "onActivityResult: PERMISSION GRANTED");
                            if (getActivity() != null) {
                                // AppFunctions.toastShort(getActivity(), getResources().getString(R.string.permission_accepted));
                                openImageChooser();
                            }

                        } else {
                            // Log.e(TAG, "onActivityResult: PERMISSION DENIED");
                            if (getActivity() != null) {
                                AppFunctions.toastShort(getActivity(), getResources().getString(R.string.permission_denied));
                            }

                        }
                    }
                });

        mOpenImgActivityRLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        try {

                            if (getActivity() != null) {
                                if (result.getData() != null) {
                                    Uri imageUri = result.getData().getData();
                                    InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                                    selectedImage = getResizedBitmap(selectedImage, 400);
                                    String image = BitMapToString(selectedImage);

                                    callChangeProfilePictureApi(image, selectedImage);

                                } else {
                                    AppFunctions.toastShort(getActivity(), getResources().getString(R.string.process_failed_please_try_again));
                                }
                            }


                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            AppFunctions.toastShort(getActivity(), getResources().getString(R.string.process_failed_please_try_again));
                        }

                    }
                });

        return mEPBinding.getRoot();
    }


    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if (mId == R.id.img_ep_back) {
            if (getActivity() != null) {
                getParentFragmentManager().popBackStack();
            }
        } else if (mId == R.id.lay_ep_submit_btn_container) {
            if (getActivity() != null) {
                toHideDeviceKeyboard();
                check();
            }
        }else if (mId == R.id. img_ep_profile_image) {
            if (getActivity() != null) {
                toHideDeviceKeyboard();
                if (checkPermission()) {
                    openImageChooser();
                    //   Log.e("permission", "Permission already granted.");
                } else {
                    requestPermission();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        user_info();

        if (mSelectedImage != null) {
            mEPBinding.imgEpProfileImage.setImageBitmap(mSelectedImage);
        } else {

            String mProfileIMAGE = UserDetailsDB.getInstance(getActivity()).getUserDetails().getImage();
            if (mProfileIMAGE != null && !mProfileIMAGE.isEmpty()) {
                if (getActivity() != null) {
                    Glide.with(getActivity()).load(UserDetailsDB.getInstance(getActivity()).getUserDetails().getImage()).into(mEPBinding.imgEpProfileImage);
                }
            } else {
                mEPBinding.imgEpProfileImage.setImageResource(R.drawable.svg_account_circle_48dp);
            }


        }

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void user_info() {
        mProgressDialog.show();
        if (AppFunctions.networkAvailabilityCheck(getActivity())) {
            retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
            String mCustomerAuthorization = "";
            if (AppFunctions.isUserLoggedIn(getActivity())) {
                mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
            }
            Call<LoginApi> call = retrofitInterface.profile_info(mCustomerAuthorization);
            call.enqueue(new Callback<LoginApi>() {
                @Override
                public void onResponse(Call<LoginApi> call, Response<LoginApi> response) {
                    if (response.isSuccessful()) {
                        LoginApi loginApi = response.body();
                        if (loginApi != null) {
                            LoginCustomerInfoDataSet customerInfoDataSet = loginApi.loginCustomerInfo;
                            mEPBinding.etEpFirstNameEditText.setText(customerInfoDataSet.firstname);
                            mEPBinding.etEpLastNameEditText.setText(customerInfoDataSet.lastname);
                            mEPBinding.etEpEmailEditText.setText(customerInfoDataSet.email);
                            mEPBinding.etEpTelephoneEditText.setText(customerInfoDataSet.telephone);

                            if (customerInfoDataSet.image != null && !customerInfoDataSet.image.isEmpty()) {
                                if (getActivity() != null) {
                                    Glide.with(getActivity()).load(customerInfoDataSet.image).into(mEPBinding.imgEpProfileImage);
                                }
                            } else {
                                mEPBinding.imgEpProfileImage.setImageResource(R.drawable.svg_account_circle_48dp);
                            }

                        }
                    }
                    mProgressDialog.cancel();
                }

                @Override
                public void onFailure(Call<LoginApi> call, Throwable t) {
                    mProgressDialog.cancel();
                }
            });
        } else {
            mProgressDialog.cancel();
            FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
            NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
            mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
            mFT.addToBackStack("mNetworkAnalyser");
            mFT.commit();
        }
    }

    private void toHideDeviceKeyboard() {
        if (getActivity() != null) {
            if (mEPBinding != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                // To get the correct window token, lets first get the currently focused view
                View v__iew = mEPBinding.getRoot();
                // To get the window token when there is no currently focused view, we have a to create a view
                if (v__iew == null) {
                    v__iew = new View(getActivity());
                }
                // hide the keyboard
                imm.hideSoftInputFromWindow(v__iew.getWindowToken(), 0);
            }
        }
    }

    private void check() {

        if (!mEPBinding.etEpFirstNameEditText.getText().toString().isEmpty()
                && !mEPBinding.etEpLastNameEditText.getText().toString().isEmpty()
                && !mEPBinding.etEpEmailEditText.getText().toString().isEmpty()
                //&& !mEPBinding.etEpTelephoneEditText.getText().toString().isEmpty()
                && AppFunctions.emailFormatValidation(mEPBinding.etEpEmailEditText.getText().toString())) {

            callEditProfileAPi();

        }


        if (getActivity() != null) {

            if (mEPBinding.etEpFirstNameEditText.getText().toString().isEmpty()) {

                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_first_name));

            } else {

                if (mEPBinding.etEpLastNameEditText.getText().toString().isEmpty()) {

                    AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_last_name));

                } else {

                    if (mEPBinding.etEpEmailEditText.getText().toString().isEmpty()) {

                        AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_email));

                    } else {

                        if (!AppFunctions.emailFormatValidation(mEPBinding.etEpEmailEditText.getText().toString())) {
                            AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_valid_email_address));
                        } else {

                            /*if (mEPBinding.etEpTelephoneEditText.getText().toString().isEmpty()) {
                                AppFunctions.msgDialogOk(getActivity(), "", getActivity().getResources().getString(R.string.please_enter_your_mobile_number));
                            }*/
                        }
                    }

                }


            }


        }


    }

    private void callEditProfileAPi() {

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {

                    jsonObject.put(DefaultNames.firstname, mEPBinding.etEpFirstNameEditText.getText().toString());
                    jsonObject.put(DefaultNames.lastname, mEPBinding.etEpLastNameEditText.getText().toString());
                    jsonObject.put(DefaultNames.email, mEPBinding.etEpEmailEditText.getText().toString());

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ApiResponseCheck> Call = retrofitInterface.editProfileApi(mCustomerAuthorization, body);
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

                                                LoginDataSet mExistsLoginData = UserDetailsDB.getInstance(getActivity()).getUserDetails();

                                                LoginDataSet mLoginDs = new LoginDataSet();

                                                mLoginDs.setCustomerId(mExistsLoginData.getCustomerId());
                                                mLoginDs.setCustomerKey(mExistsLoginData.getCustomerKey());
                                                mLoginDs.setFirstName(mEPBinding.etEpFirstNameEditText.getText().toString());
                                                mLoginDs.setLastName(mEPBinding.etEpLastNameEditText.getText().toString());
                                                mLoginDs.setEmail(mEPBinding.etEpEmailEditText.getText().toString());
                                                mLoginDs.setTelephone(mExistsLoginData.getTelephone());

                                                //The following two values filled by local app values!.
                                                mLoginDs.setMobileCountryCodeId(LoginCountryCodeDB.getInstance(getActivity()).get_Details().getCountryId());
                                                mLoginDs.setMobileCountryCode(mExistsLoginData.getMobileCountryCode());

                                                String mUserImage = mExistsLoginData.getImage();
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



    //**********************  Profile image process *******************************

    private boolean checkPermission() {
        int result;
        if (getActivity() != null) {
            result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return false;
        }
    }

    void openImageChooser() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        mOpenImgActivityRLauncher.launch(photoPickerIntent);

    }

    public static Bitmap getResizedBitmap(Bitmap image, int maxSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    public static String BitMapToString(Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;

    }

    private void requestPermission() {

        if (getActivity() != null) {
            //  ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            mImagePermissionResult.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    private void callChangeProfilePictureApi(String image, Bitmap m_Selected_Image) {

        /*{"image":"http:\/\/ordenese.foodesoft.com\/\/var\/www\/vhosts\/ordenesefoodesoft\/public\/images\/customer_profile_pic\/62f0ed5d5eb92.png","success":{"message":"success.","status":"200"}}*/

        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {

                JSONObject jsonObject = new JSONObject();
                try {


                   // String mImage = "data:image/jpeg;base64," + image;

                    jsonObject.put(DefaultNames.image, image);

                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());

                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                    String mCustomerAuthorization = "";
                    if (AppFunctions.isUserLoggedIn(getActivity())) {
                        mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    }

                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<ProfilePictureApi> Call = retrofitInterface.changeProfilePictureApi(mCustomerAuthorization, body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<ProfilePictureApi>() {
                        @Override
                        public void onResponse(@NonNull Call<ProfilePictureApi> call, @NonNull Response<ProfilePictureApi> response) {

                            mProgressDialog.cancel();

                            if (getActivity() != null) {
                                if (response.isSuccessful()) {

                                    mProfilePictureApi  = response.body();
                                    if (mProfilePictureApi != null) {
                                        //Log.e("mProfilePictureApi", "not null");
                                        if (mProfilePictureApi.success != null) {
                                            //Api response successDataSet :-
                                            if (getActivity() != null) {

                                                LoginDataSet mExistsLoginData = UserDetailsDB.getInstance(getActivity()).getUserDetails();

                                                LoginDataSet mLoginDs = new LoginDataSet();

                                                mLoginDs.setCustomerId(mExistsLoginData.getCustomerId());
                                                mLoginDs.setCustomerKey(mExistsLoginData.getCustomerKey());
                                                mLoginDs.setFirstName(mExistsLoginData.getFirstName());
                                                mLoginDs.setLastName(mExistsLoginData.getLastName());
                                                mLoginDs.setEmail(mExistsLoginData.getEmail());
                                                mLoginDs.setTelephone(mExistsLoginData.getTelephone());

                                                //The following two values filled by local app values!.
                                                mLoginDs.setMobileCountryCodeId(LoginCountryCodeDB.getInstance(getActivity()).get_Details().getCountryId());
                                                mLoginDs.setMobileCountryCode(mExistsLoginData.getMobileCountryCode());

                                                /*if (mUserImage != null) {
                                                    String mFinalPath = mUserImage.replace("\\", "");
                                                    // ////Log.e("mFinalPath",mFinalPath);
                                                    mLoginDs.setImage(mFinalPath);
                                                } else {
                                                    mLoginDs.setImage("");
                                                }*/


                                                if(mProfilePictureApi.image != null && !mProfilePictureApi.image.isEmpty()){
                                                    mLoginDs.setImage(mProfilePictureApi.image);
                                                    Glide.with(getActivity()).load(mProfilePictureApi.image).into(mEPBinding.imgEpProfileImage);
                                                }else {
                                                    mLoginDs.setImage("");
                                                }


                                                if (UserDetailsDB.getInstance(getActivity()).getSizeOfList() > 0) {
                                                    //safe check :-
                                                    UserDetailsDB.getInstance(getActivity()).deleteUserDetailsDB();
                                                }
                                                UserDetailsDB.getInstance(getActivity()).addUserDetails(mLoginDs);

                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder
                                                        .setMessage(mProfilePictureApi.success.message)
                                                        .setCancelable(false)
                                                        .setPositiveButton(getActivity().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                    }
                                                                }
                                                        );

                                                AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.show();

                                            }

                                        } else {
                                            //Api response failure :-
                                            if (mProfilePictureApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mProfilePictureApi.error.message);
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
                        public void onFailure(@NonNull Call<ProfilePictureApi> call, @NonNull Throwable t) {

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