package com.ordenese.CustomClasses;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentTransaction;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;
import com.ordenese.Activities.AppHome;
import com.ordenese.Activities.AppSplashScreen;
import com.ordenese.Fragments.MyOrderInfo;
import com.ordenese.R;

import org.json.JSONObject;

public class MainContext extends MultiDexApplication {

    private static MainContext mInstance;

    private static final String ONESIGNAL_APP_ID = "a85219a8-2886-4a90-95f3-9625d85414a4";

//
    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);

        mInstance=this;

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
        OneSignal.setNotificationOpenedHandler(new ExampleNotificationOpenedHandler(mInstance));


    }

    public static Context getAppContext(){
        return mInstance.getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(AppLanguageSupport.onAttach(base,"en"));
    }

}
