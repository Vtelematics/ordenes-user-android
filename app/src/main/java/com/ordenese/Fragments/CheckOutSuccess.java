package com.ordenese.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.ordenese.DataSets.CheckOutDBDataSet;
import com.ordenese.Databases.CheckOutDetailsDB;
import com.ordenese.Interfaces.CheckOutBackPress;
import com.ordenese.R;
import com.ordenese.databinding.CheckOutSuccessBinding;

public class CheckOutSuccess extends Fragment implements View.OnClickListener{

    CheckOutSuccessBinding mCheckOutSBinding;
    private CheckOutBackPress mCheckOutBackPress;

    public CheckOutSuccess() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mCheckOutSBinding = CheckOutSuccessBinding.inflate(inflater,container,false);
        mCheckOutBackPress = (CheckOutBackPress) getActivity();
        if (mCheckOutBackPress != null) {
            mCheckOutBackPress.checkOutSuccessStatus(true);
        }
        mCheckOutSBinding.layCoSContinue.setOnClickListener(this);

        String thanku_msg = getResources().getString(R.string.str_1) + "<br>" + getResources().getString(R.string.str_2) + "<br>" +
                getResources().getString(R.string.str_3) ;

        mCheckOutSBinding.tvCoSThankU.setText(Html.fromHtml(thanku_msg));

        return mCheckOutSBinding.getRoot();
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();
        if (mId == R.id.img_check_out_back) {
            if (mCheckOutBackPress != null) {
                mCheckOutBackPress.checkOutSuccessBackPressed();
            }
        }else if(mId == R.id.lay_co_s_continue){

            if (mCheckOutBackPress != null) {
                mCheckOutBackPress.checkOutSuccessBackPressed();
            }

        }
    }
}