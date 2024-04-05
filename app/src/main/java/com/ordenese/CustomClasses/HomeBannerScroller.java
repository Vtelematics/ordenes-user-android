package com.ordenese.CustomClasses;


import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class HomeBannerScroller extends Scroller {

    public int mDuration = 500;

   /* public HomeBannerScroller(Context context) {
        super(context);
    }*/

    public HomeBannerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

   /* public HomeBannerScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }*/


    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
       // //Log.e("startScroll With Duration","Called");
    }

   /* @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
        //Log.e("startScroll  WithOut Duration", "Called");
    }*/
}