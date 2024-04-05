package com.ordenese.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ordenese.CustomClasses.AppLanguageSupport;
import com.ordenese.Fragments.ListHome;
import com.ordenese.Fragments.Login;
import com.ordenese.R;
import com.ordenese.databinding.AppLoginBinding;

public class AppLogin extends AppCompatActivity implements View.OnClickListener{

    AppLoginBinding mAppLoginBinding;

    public AppLogin(){

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(AppLanguageSupport.onAttach(base));
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getWindow().getDecorView().setLayoutDirection("ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(AppLogin.this)) ?
                View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.app_login);
        mAppLoginBinding = AppLoginBinding.inflate(getLayoutInflater());
        View view = mAppLoginBinding.getRoot();
        setContentView(view);

        FragmentTransaction mFT = getSupportFragmentManager().beginTransaction();
        Login m_login = new Login();
        mFT.replace(R.id.layout_app_login_body, m_login, "m_login");
        mFT.addToBackStack("m_login");
        mFT.commit();

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
    public void onBackPressed() {
        //  super.onBackPressed();
        backwardNavigation();
    }

    private void backwardNavigation() {

        FragmentManager mFragmentMgr = getSupportFragmentManager();
        if (mFragmentMgr.getBackStackEntryCount() > 1) {
            mFragmentMgr.popBackStack();
        } else {
            finish();
        }

    }


}