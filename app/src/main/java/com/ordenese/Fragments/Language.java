package com.ordenese.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.window.SplashScreen;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ordenese.Activities.AppSplashScreen;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.DataSets.LanguageDataSet;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.R;

import java.util.ArrayList;


public class Language extends DialogFragment implements View.OnClickListener {


    public Language(){

    }

    private View mLanguageView;
    private LinearLayout mClose,mLanguageBody,mProgressBarContainer;
    private ArrayList<String> mLanguageList;
    private Button mChangeLanguage,mCancelLanguage;
    private RecyclerView mLanguageRecycler;
    private RecyclerView.LayoutManager mLanguageRecyclerLayoutMgr;
    private LanguageAdapter mLanguageAdapter;
    private LanguageDataSet mCurrentLanguage;

    private ProgressBar mProgressBar;

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        mLanguageView = inflater.inflate(R.layout.language, container, false);

        mProgressBar = mLanguageView.findViewById(R.id.progress_bar_restaurant_language);
        mProgressBar.setVisibility(View.GONE);

       // mClose = (LinearLayout) mLanguageView.findViewById(R.id.layout_restaurant_language_close);
       // mClose.setOnClickListener(this);
        mLanguageBody = (LinearLayout) mLanguageView.findViewById(R.id.layout_restaurant_language_body);
        mLanguageBody.setVisibility(View.GONE);


        mLanguageRecycler = (RecyclerView) mLanguageView.findViewById(R.id.layout_restaurant_language_list);

        mProgressBarContainer = (LinearLayout) mLanguageView.findViewById(R.id.layout_restaurant_language_progressbar);
        mChangeLanguage =  mLanguageView.findViewById(R.id.btn_restaurant_language_change);
        mChangeLanguage.setVisibility(View.GONE);
        mChangeLanguage.setOnClickListener(this);
        mCancelLanguage =  mLanguageView.findViewById(R.id.btn_restaurant_language_cancel);
        mCancelLanguage.setVisibility(View.GONE);
        mCancelLanguage.setOnClickListener(this);

        ArrayList<String> languageList = new ArrayList<>();
        languageList.add("English");
        languageList.add("Arabic");
        mChangeLanguage.setVisibility(View.VISIBLE);
        mCancelLanguage.setVisibility(View.VISIBLE);
        mLanguageRecycler.setVisibility(View.VISIBLE);
        mLanguageBody.setVisibility(View.VISIBLE);
        languageList(languageList);



        return mLanguageView;
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
    public void onStart() {
        super.onStart();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getDialog().getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        int mCancelId = v.getId();
        /*if (mCancelId == R.id.layout_restaurant_language_close) {
            dismiss();
        }else*/ if (mCancelId == R.id.btn_restaurant_language_cancel){
            dismiss();
        } else if (mCancelId == R.id.btn_restaurant_language_change) {
           //
            if(mCurrentLanguage != null){
                String languageName = LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId();
                if(mCurrentLanguage.getName().equals(languageName)){
                    dismiss();
                }else {

                    //*******************************************************************************

                    LanguageDataSet mLanguageDataSet = new LanguageDataSet();
                    mLanguageDataSet.setLanguageId(mCurrentLanguage.getLanguageId());
                    mLanguageDataSet.setCode(mCurrentLanguage.getCode());

                    if(mCurrentLanguage.getCode().equalsIgnoreCase("ae") ||
                            mCurrentLanguage.getCode().equalsIgnoreCase("ar") ){

                        AppLanguageSupport.setLocale(getActivity(), /*mCurrentLanguage.getCode().toLowerCase()*/"ae");

                        if (LanguageDetailsDB.getInstance(getActivity()).check_language_selected()) {
                            LanguageDetailsDB.getInstance(getActivity()).delete_language_detail();
                            LanguageDetailsDB.getInstance(getActivity()).insert_language_detail(mLanguageDataSet);
                            //mCurrentLanguage.getLanguageId() where arabic language id might be "2" most time.
                        } else {
                            LanguageDetailsDB.getInstance(getActivity()).insert_language_detail(mLanguageDataSet);
                        }

                        applyLanguageForApp();

                    }else {

                        AppLanguageSupport.setLocale(getActivity(), mCurrentLanguage.getCode().toLowerCase());

                        if (LanguageDetailsDB.getInstance(getActivity()).check_language_selected()) {
                            LanguageDetailsDB.getInstance(getActivity()).delete_language_detail();
                            LanguageDetailsDB.getInstance(getActivity()).insert_language_detail(mLanguageDataSet);
                        } else {
                            LanguageDetailsDB.getInstance(getActivity()).insert_language_detail(mLanguageDataSet);
                        }

                        applyLanguageForApp();



                    }


                    //*******************************************************************************




                }
            }

        }
    }

    private void applyLanguageForApp() {

        if(getActivity() != null){
            Intent intent = new Intent(getActivity(), AppSplashScreen.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK |
                    Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        }

    }

    private void changeLanguage(String languageCode){

       // AppFunctions.toastLong(getActivity(),languageCode);
        AppLanguageSupport.setLocale(getActivity().getBaseContext(),languageCode);

        Intent intent = new Intent(getActivity().getApplicationContext(), AppSplashScreen.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
        getActivity().finish();

    }

    private void languageList(ArrayList<String> languageList){

        mLanguageRecyclerLayoutMgr = new LinearLayoutManager(getActivity());
        mLanguageRecycler.setLayoutManager(mLanguageRecyclerLayoutMgr);
        mLanguageAdapter = new LanguageAdapter(languageList);
        mLanguageRecycler.setAdapter(mLanguageAdapter);

    }



   /* @Override
    public void apiResponse(String status, String from, String[] result) {

        //  Log.e("apiResponse Response","Called");

        if (from.equals(DefaultNames.Store_Language)) {

            if (result != null && result.length > 0) {

                //  Log.e("Language Response", result[0]);

                ResponseDataSet mResponse = ApiMethods.apiResponse(result[0]);

                if (mResponse != null) {

                    if (!mResponse.getResponseEmpty()) {
                        if (mResponse.getIsSuccess()) {

                            mLanguageList = ApiMethods.languageList(result[0]);
                            if (mLanguageList != null) {

                                languageList(mLanguageList);
                                mLanguageBody.setVisibility(View.VISIBLE);
                                mChangeLanguage.setVisibility(View.VISIBLE);
                                mCancelLanguage.setVisibility(View.VISIBLE);

                            } else {
                                mLanguageBody.setVisibility(View.GONE);
                                mChangeLanguage.setVisibility(View.GONE);
                                 mCancelLanguage.setVisibility(View.GONE);
                                AppFunctions.toastShort(getActivity(), R.string.process_failed_please_try_again);
                            }


                        } else {
                            mLanguageBody.setVisibility(View.GONE);
                            mChangeLanguage.setVisibility(View.GONE);
                             mCancelLanguage.setVisibility(View.GONE);
                            AppFunctions.toastShort(getActivity(), mResponse.getMessage());
                        }
                    } else {
                        mLanguageBody.setVisibility(View.GONE);
                        mChangeLanguage.setVisibility(View.GONE);
                         mCancelLanguage.setVisibility(View.GONE);
                        AppFunctions.toastShort(getActivity(), R.string.process_failed_please_try_again);
                    }


                } else {
                    mLanguageBody.setVisibility(View.GONE);
                    mChangeLanguage.setVisibility(View.GONE);
                     mCancelLanguage.setVisibility(View.GONE);
                    AppFunctions.toastShort(getActivity(), R.string.process_failed_please_try_again);
                }

                mProgressBarContainer.setVisibility(View.GONE);
            }else {
                mLanguageBody.setVisibility(View.GONE);
                mProgressBarContainer.setVisibility(View.GONE);
                mChangeLanguage.setVisibility(View.GONE);
                 mCancelLanguage.setVisibility(View.GONE);
                AppFunctions.toastShort(getActivity(), R.string.process_failed_please_try_again);
            }
        } else {
            mLanguageBody.setVisibility(View.GONE);
            mProgressBarContainer.setVisibility(View.GONE);
            mChangeLanguage.setVisibility(View.GONE);
             mCancelLanguage.setVisibility(View.GONE);
            AppFunctions.toastShort(getActivity(), R.string.process_failed_please_try_again);
        }


    }*/

    public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder>  {
        private ArrayList<String> mAppLanguageList;
        private TextView mTempSelectedArea,mCurrentSelectedArea;
        private int mCurrentPosition;
        private RadioButton mTempTitle = null;

        public LanguageAdapter(){

        }

        public LanguageAdapter(ArrayList<String> languageList) {
            this.mAppLanguageList = languageList;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.app_language_row, parent, false);
            //  AppFunctions.customFont(getActivity(), view.findViewById(R.id.lay_restaurant_select_area_row), DefaultNames.FONT_ROBOTO_LIGHT);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            if (LanguageDetailsDB.getInstance(getActivity()).check_language_selected()) {
                String languageName = LanguageDetailsDB.getInstance(getActivity()).get_language_Details().getLanguageId();
                if(mAppLanguageList.get(position)./*.getLanguageId().*/equals(languageName)){
                    /* languageList.add("English");
                      languageList.add("Arabic");*/

                    holder.mLanguageTitle.setChecked(true);
                    mTempTitle = holder.mLanguageTitle;

                    String mCurrentLang = mAppLanguageList.get(position);
                    if(mCurrentLang.equals("English")){
                        mCurrentLanguage = new LanguageDataSet();
                        mCurrentLanguage.setLanguageId("1");
                        mCurrentLanguage.setCode("en");
                        mCurrentLanguage.setName("English");
                        mCurrentLanguage.setImage("");
                    }else {
                        mCurrentLanguage = new LanguageDataSet();
                        mCurrentLanguage.setLanguageId("2");
                        mCurrentLanguage.setCode("ae");
                        mCurrentLanguage.setName("Arabic");
                        mCurrentLanguage.setImage("");
                    }



                }else {
                    holder.mLanguageTitle.setChecked(false);
                }
            }else {
                holder.mLanguageTitle.setChecked(false);
            }

            holder.mLanguageTitle.setText(mAppLanguageList.get(position)/*.getName()*/);

            holder.mLanguageTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String mCurrentLang = mAppLanguageList.get(position);
                    if(mCurrentLang.equals("English")){
                        mCurrentLanguage = new LanguageDataSet();
                        mCurrentLanguage.setLanguageId("1");
                        mCurrentLanguage.setCode("en");
                        mCurrentLanguage.setName("English");
                        mCurrentLanguage.setImage("");
                    }else {
                        mCurrentLanguage = new LanguageDataSet();
                        mCurrentLanguage.setLanguageId("2");
                        mCurrentLanguage.setCode("ae");
                        mCurrentLanguage.setName("Arabic");
                        mCurrentLanguage.setImage("");
                    }

                    if(mTempTitle != null){
                        mTempTitle.setChecked(false);
                        holder.mLanguageTitle.setChecked(true);
                        mTempTitle = holder.mLanguageTitle;
                    }else {
                        holder.mLanguageTitle.setChecked(true);
                        mTempTitle = holder.mLanguageTitle;
                    }

                }
            });


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
            return mAppLanguageList.size();
        }



        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

          //  TextView mLanguageTitle;
            private RadioButton mLanguageTitle;
            LinearLayout mLanguageRow;

            public ViewHolder(View itemView) {
                super(itemView);

                mLanguageRow = (LinearLayout) itemView.findViewById(R.id.lay_language_row);
                mLanguageTitle = (RadioButton) itemView.findViewById(R.id.radio_language_title);


            }

            @Override
            public void onClick(View v) {

            }
        }
    }


}
