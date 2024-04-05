package com.ordenese.CustomClasses;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.ordenese.Interfaces.NetworkAccess;
import com.ordenese.R;


public class NetworkAnalyser extends Fragment implements View.OnClickListener {

    private Button mRetry;
    private NetworkAccess mNetworkAccess;
    private TextView mStatusMsg;
    private static final int STATUS_MESSAGE_TIME_OUT = 2000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View mNetworkValidatorView = inflater.inflate(R.layout.network_analyser, container, false);

        mStatusMsg = mNetworkValidatorView.findViewById(R.id.tv_network_analyser_no_network_status_msg);
        mStatusMsg.setVisibility(View.GONE);

        mRetry = mNetworkValidatorView.findViewById(R.id.btn_network_analyser_retry);
        mRetry.setOnClickListener(this);

        return mNetworkValidatorView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(AppLanguageSupport.onAttach(context));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getActivity().getWindow().getDecorView().setLayoutDirection(
                    "ae".equalsIgnoreCase(AppLanguageSupport.getLanguage(getActivity())) ?
                            View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    @Override
    public void onClick(View v) {
        int mNetworkValId = v.getId();
        if(mNetworkValId == R.id.btn_network_analyser_retry){
            if(AppFunctions.networkAvailabilityCheck(getActivity())) {
               // mNetworkAccess.networkConnectionStatus(true);

                getParentFragmentManager().popBackStack();
            }else{
               // mNetworkAccess.networkConnectionStatus(false);

                mStatusMsg.setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(getActivity() != null){
                            mStatusMsg.setVisibility(View.GONE);
                        }

                    }
                }, STATUS_MESSAGE_TIME_OUT);
            }
        }
    }
}
