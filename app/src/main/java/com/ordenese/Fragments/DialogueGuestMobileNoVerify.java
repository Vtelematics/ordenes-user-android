package com.ordenese.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.Interfaces.GuestMobileNoOTPUI;
import com.ordenese.R;

public class DialogueGuestMobileNoVerify extends DialogFragment implements View.OnClickListener {

    View mGuestMNVView;
    Button mBtnCancel, mBtnGetCode;
    private GuestMobileNoOTPUI mGuestMobileNoOTPUI;
    private String mMobileNo = "", mCountryCode = "";
    TextView mTvMobileNumber, mTvChangeMobileNoBtn;

    public DialogueGuestMobileNoVerify() {
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
    public void onStart() {
        super.onStart();
        // safety check
        if (getDialog() == null)
            return;

        int width = getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._299sdp);
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;

        if (getDialog().getWindow() != null)
            getDialog().getWindow().setLayout(width, height);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mGuestMNVView = inflater.inflate(R.layout.dialogue_guest_mobile_no_verify, container, false);

        if (getDialog() != null) {
            if (getDialog().getWindow() != null) {
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
        }

        if (getArguments() != null) {
            mMobileNo = getArguments().getString(DefaultNames.mobile);
            mCountryCode = getArguments().getString(DefaultNames.country_code);

        }

        mGuestMobileNoOTPUI = (GuestMobileNoOTPUI) getTargetFragment();

        mBtnCancel = mGuestMNVView.findViewById(R.id.btn_gm_nv_cancel);
        mBtnCancel.setOnClickListener(this);
        mBtnGetCode = mGuestMNVView.findViewById(R.id.btn_gm_nv_get_code);
        mBtnGetCode.setOnClickListener(this);

        mTvMobileNumber = mGuestMNVView.findViewById(R.id.tv_gm_nv_mobile_no);
        mTvChangeMobileNoBtn = mGuestMNVView.findViewById(R.id.tv_gm_nv_mobile_no_change);
        mTvChangeMobileNoBtn.setOnClickListener(this);

        String mMOBILE_NO = "+"+mCountryCode+mMobileNo;
        mTvMobileNumber.setText(mMOBILE_NO);


        return mGuestMNVView;
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();
        if (mId == R.id.btn_gm_nv_cancel) {
            dismiss();
        } else if (mId == R.id.btn_gm_nv_get_code) {
            if (mGuestMobileNoOTPUI != null) {
                mGuestMobileNoOTPUI.toShowMobileNoOTPUI(true);
            }
            dismiss();
        } else if (mId == R.id.tv_gm_nv_mobile_no_change) {
            if (mGuestMobileNoOTPUI != null) {
                mGuestMobileNoOTPUI.toChangeMobileNo(true);
            }
            dismiss();
        }


    }
}