package com.ordenese.Adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;


import com.bumptech.glide.Glide;
import com.ordenese.CustomClasses.AppFunctions;
import com.ordenese.CustomClasses.DefaultNames;
import com.ordenese.DataSets.BannersDataSet;
import com.ordenese.DataSets.HomeBannerDataSet;
import com.ordenese.Fragments.GroceryCategoryMainPage;
import com.ordenese.Fragments.RestaurantInfo;
import com.ordenese.R;

import java.util.ArrayList;


public class BannerAdapter extends PagerAdapter {

    private final Context mContext;
    private final ArrayList<BannersDataSet> mHomeBannerList;
    private final FragmentManager mFragmentManager;
    private Activity mActivity;

    // private final ListContext mListContext;
    // private final MenuPageMenuShowing mMenuPageMenuShowing;


    public BannerAdapter(Context context, ArrayList<BannersDataSet> homeBannerList, FragmentManager fragmentManager
            /*,ListContext listContext,*//* MenuPageMenuShowing menuPageMenuShowing*/, Activity activity) {
        this.mContext = context;
        this.mHomeBannerList = homeBannerList;
        this.mFragmentManager = fragmentManager;
        this.mActivity = activity;
    }

    @Override
    public int getCount() {

        if (mHomeBannerList != null) {
            return mHomeBannerList.size();
        } else {
            return 0;
        }

        //return 3;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        LayoutInflater mInflater = LayoutInflater.from(mContext);
        ViewGroup mView = (ViewGroup) mInflater.inflate(R.layout.banner_image, container, false);
        ImageView mBannerImage = mView.findViewById(R.id.img_restaurant_banner_image);
        mBannerImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext != null) {

                    if (mHomeBannerList.get(position).getVendor_type_id().equals("2")) {
                        FragmentTransaction mFT = mFragmentManager.beginTransaction();
                        GroceryCategoryMainPage m_groceryCategoryMainPage = new GroceryCategoryMainPage();
                        Bundle mBundle = new Bundle();
                        mBundle.putString(DefaultNames.store_id, mHomeBannerList.get(position).getVendor_id());
                        mBundle.putString(DefaultNames.store_name, "");
                        m_groceryCategoryMainPage.setArguments(mBundle);
                        mFT.replace(R.id.layout_app_home_body, m_groceryCategoryMainPage, "m_groceryCategoryMainPage");
                        mFT.addToBackStack("m_groceryCategoryMainPage");
                        mFT.commit();
                    } else {
                        FragmentTransaction mFT = mFragmentManager.beginTransaction();
                        RestaurantInfo restaurantInfo = new RestaurantInfo();
                        Bundle mBundle = new Bundle();
                        mBundle.putString("vendor_id", mHomeBannerList.get(position).getVendor_id());
                        mBundle.putString("product_id", "");
                        restaurantInfo.setArguments(mBundle);
                        mFT.replace(R.id.layout_app_home_body, restaurantInfo, "restaurantInfo");
                        mFT.addToBackStack("restaurantInfo");
                        mFT.commit();
                    }
                }
            }
        });

        AppFunctions.bannerLoaderUsingGlide(mHomeBannerList.get(position).getBanner(), mBannerImage, mActivity);

         /*if (position == 0) {
            //mBannerImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.banner_image_1));
            Glide.with(mContext).load(R.drawable.x_banner_01).into(mBannerImage);
         } else if (position == 1) {
           // mBannerImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.banner_image_2));
            Glide.with(mContext).load(R.drawable.x_banner_02).into(mBannerImage);
        } else if (position == 2) {
           // mBannerImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.banner_image_3));
            Glide.with(mContext).load(R.drawable.x_banner_03).into(mBannerImage);
        }*/

        // CustomFunctions.glideBannerLoader(mValue[position], image);
       /* if(position==0){
            mBannerImage.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.restaurant_banner_1));

        }else if(position==1){
            mBannerImage.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.restaurant_list_banner));
        }else if(position==2){
            mBannerImage.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.restaurant_banner_3));
        }*/

        container.addView(mView);
        return mView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
