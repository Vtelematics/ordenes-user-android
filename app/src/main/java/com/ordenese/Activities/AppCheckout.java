package com.ordenese.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.DataSets.CheckOutDBDataSet;
import com.ordenese.Databases.CheckOutDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.Fragments.CheckOut;
import com.ordenese.Fragments.OrderConfirmation;
import com.ordenese.Interfaces.CheckOutBackPress;
import com.ordenese.Interfaces.OrderConfirmBackPressUI;
import com.ordenese.R;
import com.ordenese.databinding.AppCheckoutBinding;

public class AppCheckout extends AppCompatActivity implements View.OnClickListener, CheckOutBackPress, OrderConfirmBackPressUI {

    AppCheckoutBinding mAppCheckoutBinding;
    private Boolean mIsCheckOutSuccess = false;
    private boolean mTrackOrderShowing;
    private LinearLayout mConfirmOrderUi;
    private LinearLayout mTrackOderUi;
    private TextView mPageTitle;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(AppLanguageSupport.onAttach(base));
    }

    public AppCheckout() {

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().getDecorView().setLayoutDirection("ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(AppCheckout.this)) ?
                View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.app_checkout);
        mAppCheckoutBinding = AppCheckoutBinding.inflate(getLayoutInflater());
        View view = mAppCheckoutBinding.getRoot();
        setContentView(view);

        if (getIntent().getStringExtra("info") != null) {
            FragmentTransaction mFT = getSupportFragmentManager().beginTransaction();
            OrderConfirmation m_orderConfirmation = new OrderConfirmation();
            Bundle bundle = new Bundle();
            bundle.putString(DefaultNames.from, "order_info");
            bundle.putString(DefaultNames.order_id, getIntent().getStringExtra("info"));
            m_orderConfirmation.setArguments(bundle);
            mFT.replace(R.id.layout_app_check_out_body, m_orderConfirmation, "m_orderConfirmation");
            mFT.addToBackStack("m_orderConfirmation");
            mFT.commit();
        } else {

            if (CheckOutDetailsDB.getInstance(AppCheckout.this).getSizeOfList() > 0) {
                CheckOutDetailsDB.getInstance(AppCheckout.this).deleteDB();
            }

            //For fresh checkout details gathering :-
            CheckOutDBDataSet mCheckOutDBDs = new CheckOutDBDataSet();
            mCheckOutDBDs.setCouponId("");
            mCheckOutDBDs.setCouponCode("");
            mCheckOutDBDs.setAddressId("");
            mCheckOutDBDs.setPaymentListId("");
            mCheckOutDBDs.setContactLessDeliveryChecked("");
            mCheckOutDBDs.setPaymentCode("");
            CheckOutDetailsDB.getInstance(AppCheckout.this).add(mCheckOutDBDs);

            FragmentTransaction mFT = getSupportFragmentManager().beginTransaction();
            CheckOut m_checkOut = new CheckOut();
            Bundle bundle = new Bundle();
            bundle.putString("vendor_id",getIntent().getStringExtra("vendor_id"));
            m_checkOut.setArguments(bundle);
            mFT.replace(R.id.layout_app_check_out_body, m_checkOut, "m_checkOut");
            mFT.addToBackStack("m_checkOut");
            mFT.commit();

        }
    }

    @Override
    public void onClick(View view) {
        int mId = view.getId();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
//        Log.e("91", "called - " + mIsCheckOutSuccess);
        if (mIsCheckOutSuccess) {
            //This case occur, the user press back button when currently in checkout success page.
            //mTrackOrderShowing,mConfirmOrderUi,mTrackOderUi
            if (mTrackOrderShowing) {
                mTrackOrderShowing = false;
                if (mConfirmOrderUi != null && mTrackOderUi != null && mPageTitle != null) {
                    mConfirmOrderUi.setVisibility(View.VISIBLE);
                    mTrackOderUi.setVisibility(View.GONE);
                    mPageTitle.setText(getResources().getString(R.string.oc_order_confirmation));
                }
            } else {
                checkOutSuccessBackPressed();
            }
        } else {
            backwardNavigation();
        }
    }

    private void backwardNavigation() {

        //  //Log.e("92","called - "+mIsCheckOutSuccess);
        FragmentManager mFragmentMgr = getSupportFragmentManager();
        if (mFragmentMgr.getBackStackEntryCount() > 1) {
            mFragmentMgr.popBackStack();
        } else {
            finish();
        }

    }


    @Override
    public void checkOutBackPressed() {

        // //Log.e("105","called - "+mIsCheckOutSuccess);

        if (mIsCheckOutSuccess) {
            //This case occur, the user press back button when currently in checkout success page.
            checkOutSuccessBackPressed();
        } else {
            backwardNavigation();
        }

    }

    @Override
    public void checkOutSuccessBackPressed() {

        //Log.e("132","called - "+mIsCheckOutSuccess);

        Intent intent = new Intent(AppCheckout.this, AppHome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    @Override
    public void checkOutSuccessStatus(Boolean successStatus) {
        mIsCheckOutSuccess = successStatus;
        // Log.e("146","called - "+mIsCheckOutSuccess);
    }


    @Override
    public void orderConfirmBackPressUI(boolean trackOrderShowing, LinearLayout confirmOrderUi,
                                        LinearLayout trackOderUi, TextView pageTitle) {
        //mTrackOrderShowing,mConfirmOrderUi,mTrackOderUi
        mTrackOrderShowing = trackOrderShowing;
        mConfirmOrderUi = confirmOrderUi;
        mTrackOderUi = trackOderUi;
        mPageTitle = pageTitle;

    }
}