package com.ordenese.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.DataSets.CountryCodeDataSet;
import com.ordenese.DataSets.LoginCountryCodeDataSet;
import com.ordenese.Databases.LoginCountryCodeDB;
import com.ordenese.Interfaces.CountryCodeSelection;
import com.ordenese.R;
import com.ordenese.databinding.DialogueCountryCodeListBinding;

import java.util.ArrayList;


public class DialogueCountryCodeList extends DialogFragment implements View.OnClickListener{

    private DialogueCountryCodeListBinding mDCCListBinding;
    private static final String m_CountryCode_List = "m_CountryCode_List";
    private ArrayList<CountryCodeDataSet> mCountryCodeList;
    private RecyclerView.LayoutManager mCCListLayoutMgr;
    private RecyclerView.Adapter mCCListAdapter;
    private CountryCodeSelection mCountryCodeSelection;

    public DialogueCountryCodeList() {
        // Required empty public constructor
    }

    public DialogueCountryCodeList newInstance(ArrayList<CountryCodeDataSet> countryCodeList) {
        DialogueCountryCodeList fragment = new DialogueCountryCodeList();
        Bundle args = new Bundle();
        args.putSerializable(m_CountryCode_List, countryCodeList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCountryCodeList = (ArrayList<CountryCodeDataSet>)getArguments().getSerializable(m_CountryCode_List);
        }
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
        // safety check
        if (getDialog() == null)
            return;

        int width = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._300sdp);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        if (getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.dialogue_country_code_list, container, false);
        mDCCListBinding = DialogueCountryCodeListBinding.inflate(inflater,container,false);

        if(getDialog() != null){
            if (getDialog().getWindow() != null) {
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }

        mCountryCodeSelection = (CountryCodeSelection) getTargetFragment();

       // //Log.e("mCountryCodeList size",""+mCountryCodeList.size());

        mDCCListBinding.imgDCclClose.setOnClickListener(this);

        mCCListLayoutMgr = new LinearLayoutManager(getActivity());
        mDCCListBinding.recyclerLoginCountryCodeList.setLayoutManager(mCCListLayoutMgr);
        mCCListAdapter = new CCListAdapter(mCountryCodeList);
        mDCCListBinding.recyclerLoginCountryCodeList.setAdapter(mCCListAdapter);



        return mDCCListBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();

        if(mId == R.id.img_d_ccl_close){
            dismiss();
        }

    }

    public class CCListAdapter extends RecyclerView.Adapter<CCListAdapter.DataObjectHolder> {

        private ArrayList<CountryCodeDataSet> mACCList;

        public CCListAdapter(ArrayList<CountryCodeDataSet> ccList) {
            this.mACCList = ccList;

        }

        @Override
        public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_name, parent, false);
            return new DataObjectHolder(mView);
        }


        @Override
        public void onBindViewHolder(final DataObjectHolder holder, final int position) {

            //Toast.makeText(getContext(), "Name:"+mAFavouriteList.get(position).getRestaurantName()+" Address:"+mAFavouriteList.get(position).getRestaurantAddress(), Toast.LENGTH_SHORT).show();

            holder.mCountryName.setText(mACCList.get(position).getName());

            holder.mCountryName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String mPhoneCode = ""+mACCList.get(position).getCode();
                   // mCountryCode.setText(mPhoneCode);
                    if(LoginCountryCodeDB.getInstance(getActivity()).check_selected()){
                        LoginCountryCodeDB.getInstance(getActivity()).delete_detail();
                    }
                    LoginCountryCodeDataSet mLoginNCDs = new LoginCountryCodeDataSet();
                    mLoginNCDs.setCountryId(mACCList.get(position).getId());
                    mLoginNCDs.setPhoneCode(mPhoneCode);
                    LoginCountryCodeDB.getInstance(getActivity()).insert(mLoginNCDs);
                    if(getActivity() != null){
                       // Glide.with(getActivity()).load(mACCList.get(position).getImage()).into(flagImg);
                    }

                    if(mCountryCodeSelection != null){
                        mCountryCodeSelection.selectedCountryCode(mLoginNCDs);
                    }
                    dismiss();

                }
            });

        }

        @Override
        public int getItemCount() {
            return this.mACCList.size();
        }

        public class DataObjectHolder extends RecyclerView.ViewHolder {

            TextView mCountryName;

            public DataObjectHolder(View view) {
                super(view);

                mCountryName =  view.findViewById(R.id.tv_login_nc_country_code_name);

            }
        }


    }



}