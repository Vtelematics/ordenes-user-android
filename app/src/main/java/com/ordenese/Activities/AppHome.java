package com.ordenese.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;
import com.ordenese.CustomClasses.ApiClass;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.CustomClasses.NetworkAnalyser;
import com.ordenese.Databases.AreaGeoCodeDB;
import com.ordenese.Databases.DeliveryLocationSearchDB;
import com.ordenese.Fragments.CartList;
import com.ordenese.Fragments.DeliveryLocation;
import com.ordenese.Fragments.ListHome;
import com.ordenese.Fragments.MyOrderInfo;
import com.ordenese.Interfaces.CartInfo;
import com.ordenese.Interfaces.MakeBottomMarginForViewBasket;
import com.ordenese.R;
import com.ordenese.databinding.AppHomeBinding;

import org.json.JSONException;
import org.json.JSONObject;

public class AppHome extends AppCompatActivity implements View.OnClickListener, CartInfo,
        MakeBottomMarginForViewBasket/*, onSetUpdate */ {

    private AppHomeBinding mAppHomeBinding;
    Activity activity;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(AppLanguageSupport.onAttach(base));
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().getDecorView().setLayoutDirection("ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(AppHome.this)) ?
                View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.app_home);
        mAppHomeBinding = AppHomeBinding.inflate(getLayoutInflater());
        View view = mAppHomeBinding.getRoot();
        setContentView(view);

        /*FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();
                        if(FCMTokenDB.getInstance(AppHome.this).getSizeOfList() > 0){
                            FCMTokenDB.getInstance(AppHome.this).deleteDB();
                        }
                        FCMTokenDB.getInstance(AppHome.this).add(token);
                        if(AppHome.this != null){
                            TestFragment testFragment = new TestFragment();
                            testFragment.show(getSupportFragmentManager(),"testFragment");
                        }
                        AppFunctions.toastShort(AppHome.this,"onComplete called.");
                        // Log and toast
                        String msg = "Token : "+token;
                        Log.e(TAG, msg);
                        //Toast.makeText(AppHome.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        if(CheckDB.getInstance(AppHome.this).getSizeOfList() > 0){
            Log.e("CheckDB" ,"Has list.");
            CheckDB.getInstance(AppHome.this).toPrint();
        }else {
            Log.e("CheckDB" ,"empty list.");
        }*/
        
        /*E/current_address_string: 122-1-3-4, Tauta Nagar, Coimbatore, Tamil Nadu 641041, India
        E/Latitude: 11.022743527292898
        E/Longitude: 76.90503548830748
        E/current_address_name_only_string: 122-1-3-4*/

       /* AreaGeoCodeDataSet mAreaGeoCodeDS = new AreaGeoCodeDataSet();
        mAreaGeoCodeDS.setmAddress("122-1-3-4, Tauta Nagar, Coimbatore, Tamil Nadu 641041, India");
        mAreaGeoCodeDS.setmLatitude("11.022743527292898");
        mAreaGeoCodeDS.setmLongitude("76.90503548830748");
        mAreaGeoCodeDS.setmAddsNameOnly("122-1-3-4");
        if (AreaGeoCodeDB.getInstance(AppHome.this).isAreaGeoCodeSelected()) {
            AreaGeoCodeDB.getInstance(AppHome.this).updateUserAreaGeoCode(mAreaGeoCodeDS);
        } else {
            AreaGeoCodeDB.getInstance(AppHome.this).addUserAreaGeoCode(mAreaGeoCodeDS);
        }*/

        //   mAndroid_id = Settings.Secure.getString(getContentResolver(),
        //    Settings.Secure.ANDROID_ID);
        //Log.e("mAndroid_id",mAndroid_id);
        // AppFunctions.msgDialogOk(this,"Android_id",mAndroid_id);

        activity = this;

        if (!ApiClass.ORDER_ID.isEmpty()) {

            FragmentTransaction mFT = getSupportFragmentManager().beginTransaction();
            ListHome m_listHome = new ListHome();
            Bundle mBundle = new Bundle();
            mBundle.putString("FromHome", "FromHome");
            m_listHome.setArguments(mBundle);
            mFT.replace(R.id.layout_app_home_body, m_listHome, "m_listHome");
            mFT.addToBackStack("m_listHome");
            mFT.commit();


            FragmentTransaction mFT1 = getSupportFragmentManager().beginTransaction();
            Bundle mBundle1 = new Bundle();
            MyOrderInfo m_myOrderInfo = new MyOrderInfo();
            mBundle1.putString(DefaultNames.order_id, ApiClass.ORDER_ID);
            m_myOrderInfo.setArguments(mBundle1);
            mFT1.replace(R.id.layout_app_home_body, m_myOrderInfo, "m_myOrderInfo");
            mFT1.addToBackStack("m_myOrderInfo");
            mFT1.commit();

        } else {
            if (AreaGeoCodeDB.getInstance(AppHome.this).getSizeOfList() > 0) {
                FragmentTransaction mFT = getSupportFragmentManager().beginTransaction();
                ListHome m_listHome = new ListHome();
                Bundle mBundle = new Bundle();
                mBundle.putString("FromHome", "FromHome");
                m_listHome.setArguments(mBundle);
                mFT.replace(R.id.layout_app_home_body, m_listHome, "m_listHome");
                mFT.addToBackStack("m_listHome");
                mFT.commit();
            } else {
                //the DeliveryLocationSearchDB for to save  DeliveryLocationSearch page process.
                //And its used for DeliveryLocation page only.
                //So every time must refresh the DB before going to DeliveryLocation page.
                if (DeliveryLocationSearchDB.getInstance(AppHome.this).getSizeOfList() > 0) {
                    DeliveryLocationSearchDB.getInstance(AppHome.this).deleteDB();
                }
                FragmentTransaction mFT = getSupportFragmentManager().beginTransaction();
                DeliveryLocation m_deliveryLocation = new DeliveryLocation();
                mFT.replace(R.id.layout_app_home_body, m_deliveryLocation, "m_deliveryLocation");
                mFT.addToBackStack("m_deliveryLocation");
                mFT.commit();
            }
        }

    }

    @Override
    public void onBackPressed() {
        //  super.onBackPressed();
        backwardNavigation();
    }

    private void backwardNavigation() {

        FragmentManager mFragmentMgr = getSupportFragmentManager();

        for (int i = 0; i < mFragmentMgr.getBackStackEntryCount(); i++) {
            Log.e("" + i, "" + mFragmentMgr.getBackStackEntryAt(i));
            Log.e("" + i, "" + mFragmentMgr.getBackStackEntryAt(i).getName());
            /*if(mFragmentMgr.getBackStackEntryAt(i).getName() != null){
                if(mFragmentMgr.getBackStackEntryAt(i).getName().equals("mDLAndALBottomSheet")){
                    mFragmentMgr.popBackStack();
                    break;
                }
            }*/

        }

        if (mFragmentMgr.getBackStackEntryCount() > 1) {
            mFragmentMgr.popBackStack();
        } else {
            finish();
        }

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
    public void onClick(View view) {
        int mId = view.getId();

    }

    @Override
    public void cart_info(Boolean data, String count, String total) {

        if (data) {
            try {
                JSONObject object = new JSONObject(total);
                if (!object.isNull("total")) {
                    mAppHomeBinding.viewBasketLinear.setVisibility(View.VISIBLE);
                    mAppHomeBinding.cartCount.setText(count);
                    mAppHomeBinding.cartPrice.setText(object.getString("total"));
                }
                if (!object.isNull("min_cart_value")) {
                    if (!object.getString("min_cart_value").isEmpty()) {
                        mAppHomeBinding.viewBasket.setVisibility(View.VISIBLE);
                        String min_value = activity.getResources().getString(R.string.min_cart_value_is) + " " + object.getString("min_cart_value");
                        mAppHomeBinding.viewBasket.setText(min_value);
                    } else {
                        mAppHomeBinding.viewBasket.setVisibility(View.GONE);
                    }
                }

                mAppHomeBinding.viewBasketLinear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (object.getString("min_cart_value").isEmpty()) {
                                FragmentTransaction mFT = getSupportFragmentManager().beginTransaction();
                                CartList m_cartList = new CartList();
                                mFT.replace(R.id.layout_app_home_body, m_cartList, "m_cartList");
                                mFT.addToBackStack("m_cartList");
                                mFT.commit();
                            } else {
                                String min_value = activity.getResources().getString(R.string.min_cart_value_is) + " " + object.getString("min_cart_value");
                                AppFunctions.msgDialogOk(activity, "", min_value);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } catch (JSONException e) {
                mAppHomeBinding.viewBasketLinear.setVisibility(View.GONE);
                e.printStackTrace();
            }
        } else {
            mAppHomeBinding.viewBasketLinear.setVisibility(View.GONE);
        }
    }

    private void toMakeBottomMarginForViewBasketUI(Boolean isShow) {

        if (isShow) {

            //To create bottom margin :-
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0, 0, 0, getResources().getDimensionPixelSize(com.intuit.sdp.R.dimen._65sdp));
            mAppHomeBinding.layoutAppHomeBody.setLayoutParams(layoutParams);

        } else {

            //To remove bottom margin
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(0, 0, 0, 0);
            mAppHomeBinding.layoutAppHomeBody.setLayoutParams(layoutParams);

        }
    }

    @Override
    public void toMakeBottomMarginForViewBasket(Boolean toMakeMargin) {
        toMakeBottomMarginForViewBasketUI(toMakeMargin);
    }

//    @Override
//    public void reload() {
//        FragmentTransaction mFT = getSupportFragmentManager().beginTransaction();
//        ListHome m_listHome = new ListHome();
//        Bundle mBundle = new Bundle();
//        mBundle.putString("FromHome", "FromHome");
//        m_listHome.setArguments(mBundle);
//        mFT.replace(R.id.layout_app_home_body, m_listHome, "m_listHome");
//        mFT.addToBackStack("m_listHome");
//        mFT.detach(m_listHome);
//        mFT.attach(m_listHome);
//        mFT.commit();
//    }
}