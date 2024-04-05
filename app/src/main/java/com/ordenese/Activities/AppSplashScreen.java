package com.ordenese.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.DataSets.LanguageDataSet;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.OrderTypeDB;
import com.ordenese.R;
import com.ordenese.databinding.AppSplashScreenBinding;

import java.util.Timer;
import java.util.TimerTask;

public class AppSplashScreen extends AppCompatActivity {

    private Timer mTimer;
    AppSplashScreenBinding mAppSplashScreenBinding;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(AppLanguageSupport.onAttach(base));
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().getDecorView().setLayoutDirection("ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(AppSplashScreen.this)) ?
                View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_splash_screen);
        mAppSplashScreenBinding = AppSplashScreenBinding.inflate(getLayoutInflater());
        View view = mAppSplashScreenBinding.getRoot();
        setContentView(view);


        //If language not selected when app installed at the time the default language
        // for this app is ENGLISH and ID is 1.
        if (!LanguageDetailsDB.getInstance(AppSplashScreen.this).check_language_selected()) {
            LanguageDataSet mLanguageDataSet = new LanguageDataSet();
            mLanguageDataSet.setLanguageId("1");
            mLanguageDataSet.setName("English");
            mLanguageDataSet.setCode("en");
            LanguageDetailsDB.getInstance(AppSplashScreen.this).insert_language_detail(mLanguageDataSet);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        startSplashScreenLoadingTimer();
        homeScreenLoading();
    }

    private void startSplashScreenLoadingTimer() {

        if (mTimer == null) {
            mTimer = new Timer();
        }
    }

    private void stopSplashScreenLoadingTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    private void homeScreenLoading() {

        if (OrderTypeDB.getInstance(AppSplashScreen.this).getUserServiceType() != null && !OrderTypeDB.getInstance(AppSplashScreen.this).getUserServiceType().isEmpty()) {
            OrderTypeDB.getInstance(AppSplashScreen.this).updateUserServiceType("1");
        } else {
            OrderTypeDB.getInstance(AppSplashScreen.this).addUserServiceType("1");
        }

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getApplicationContext() != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadAppHome();
                            stopSplashScreenLoadingTimer();
                            // //Log.e("mTimer", "Running." + String.valueOf(count++));
                        }
                    });
                }
            }
        }, 3000, 3000);

    }

    private void loadAppHome() {
        Intent mIntent = new Intent(AppSplashScreen.this, AppHome.class);
        startActivity(mIntent);
        finish();
    }


}