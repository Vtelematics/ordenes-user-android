package com.ordenese.Fragments;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ordenese.Activities.AppLogin;
import com.ordenese.ApiConnection.ApiClientGson;
import com.ordenese.ApiConnection.RetrofitInterface;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.DataSets.ApiResponseCheck;
import com.ordenese.DataSets.PagesApi;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.R;
import com.ordenese.databinding.WebLinksBinding;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DialogueWebView extends DialogFragment implements View.OnClickListener {

    private RetrofitInterface retrofitInterface;
    private PagesApi mPagesApi;

    private WebLinksBinding mWebLinksBinding;
    private ProgressDialog mProgressDialog;
    private String from = "";

    private String mThePageCallFrom = "";

    public DialogueWebView() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // viewAboutUs = inflater.inflate(R.layout.web_links, container, false);

        mWebLinksBinding = WebLinksBinding.inflate(inflater, container, false);


        if (getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (getArguments() != null) {
            from = getArguments().getString(DefaultNames.from);
            mThePageCallFrom = getArguments().getString(DefaultNames.thePageCallFrom);
        }

        action();
        return mWebLinksBinding.getRoot();
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

    private void action() {
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setMessage(getResources().getString(R.string.loading_please_wait));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(false);
        mWebLinksBinding.imgWlBackBtn.setOnClickListener(this);

        if (from != null && from.equals(DefaultNames.AboutUs)) {

            // mAboutUsHolder.loadUrl(URL_Class.mAbout_us);

            if (getActivity() != null) {

                mWebLinksBinding.tvWlPageTitle.setText(getActivity().getResources().getString(R.string.about_us));

            }

            /*
                Page_id = 1  => privacy_policy
                Page_id = 2  => about_us
                Page_id = 3  => delivery_info
                Page_id = 4  => terms_conditions
            */

            toCallWebViewApi("2");


        } else if (from != null && from.equals(DefaultNames.PrivacyPolicy)) {

            if (getActivity() != null) {
                mWebLinksBinding.tvWlPageTitle.setText(getActivity().getResources().getString(R.string.privacy_policy));
            }

            toCallWebViewApi("1");

        } else if (from != null && from.equals(DefaultNames.TermsAndConditions)) {

            if (getActivity() != null) {
                mWebLinksBinding.tvWlPageTitle.setText(getActivity().getResources().getString(R.string.terms_and_conditions));
            }

            toCallWebViewApi("4");

        }
    }

    public class WebLinksWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // TODO Auto-generated method stub
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // TODO Auto-generated method stub
            mProgressDialog.show();
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            // TODO Auto-generated method stub
            mProgressDialog.cancel();
            super.onPageFinished(view, url);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null)
            return;
        // int width = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._300sdp);
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        if (getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.img_wl_back_btn:
                dismiss();
                break;
        }

    }

    private void toCallWebViewApi(String pageId) {
        if (getActivity() != null) {
            if (AppFunctions.networkAvailabilityCheck(getActivity())) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(DefaultNames.page_id, pageId);
                    jsonObject.put(DefaultNames.language_id, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId());
                    jsonObject.put(DefaultNames.language_code, LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getCode());
                   // String mCustomerAuthorization = UserDetailsDB.getInstance(getActivity()).getUserDetails().getCustomerKey();
                    RequestBody body = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());
                    retrofitInterface = ApiClientGson.getClient().create(RetrofitInterface.class);
                    Call<PagesApi> Call = retrofitInterface.webViewPagesApi(/*mCustomerAuthorization,*/ body);
                    mProgressDialog.show();
                    Call.enqueue(new Callback<PagesApi>() {
                        @SuppressLint("SetJavaScriptEnabled")
                        @Override
                        public void onResponse(@NonNull Call<PagesApi> call, @NonNull Response<PagesApi> response) {
                            if (response.isSuccessful()) {
                                mPagesApi = response.body();
                                if (mPagesApi != null) {
                                    if (mPagesApi.success != null) {
                                        //Api response successDataSet :-
                                        if (getActivity() != null) {
                                            // WebView URL Content
                                            mWebLinksBinding.webViewWlWebLinks.setWebViewClient(new WebLinksWebClient());
                                            mWebLinksBinding.webViewWlWebLinks.getSettings().setJavaScriptEnabled(true);
                                            String encodedHtml = Base64.encodeToString(mPagesApi.pagesData.content.getBytes(), Base64.NO_PADDING);
                                            mWebLinksBinding.webViewWlWebLinks.loadData(encodedHtml , "text/html", "base64");
//                                            mWebLinksBinding.webViewWlWebLinks.loadData(mPagesApi.pagesData.content, "text/html", "UTF-8");
                                            mWebLinksBinding.tvWlPageTitle.setText(mPagesApi.pagesData.title);
                                        }
                                    } else {
                                        //Api response failure :-
                                        mProgressDialog.cancel();
                                        if (getActivity() != null) {
                                            if (mPagesApi.error != null) {
                                                AppFunctions.msgDialogOk(getActivity(), "", mPagesApi.error.message);
                                            }
                                        }
                                    }
                                }else {
                                    mProgressDialog.cancel();
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
                        public void onFailure(@NonNull Call<PagesApi> call, @NonNull Throwable t) {
                            mProgressDialog.cancel();
                        }
                    });

                } catch (JSONException e) {
                    mProgressDialog.cancel();

                    //Log.e("210 Excep ", e.toString());
                    e.printStackTrace();

                }


            } else {
                if(mThePageCallFrom != null && !mThePageCallFrom.isEmpty()){
                    FragmentTransaction mFT = getParentFragmentManager().beginTransaction();
                    NetworkAnalyser mNetworkAnalyser = new NetworkAnalyser();
                    if(mThePageCallFrom.equals(DefaultNames.thePageCall_ForLoginLay)){
                        //Its for - login activity call :-
                        mFT.replace(R.id.layout_app_login_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    }else if(mThePageCallFrom.equals(DefaultNames.thePageCall_ForHomeLay)){
                        //Its for - home activity call :-
                        mFT.replace(R.id.layout_app_home_body, mNetworkAnalyser, "mNetworkAnalyser");
                        mFT.addToBackStack("mNetworkAnalyser");
                        mFT.commit();
                    }

                }

            }
        }
    }


}
