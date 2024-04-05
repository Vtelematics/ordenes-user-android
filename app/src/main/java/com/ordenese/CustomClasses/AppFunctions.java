package com.ordenese.CustomClasses;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.ordenese.Databases.LanguageDetailsDB;
import com.ordenese.Databases.UserDetailsDB;
import com.ordenese.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;

public class AppFunctions {

    private static final String TAG = AppFunctions.class.getSimpleName();
    private static EditText mEditText;

    public static void bannerLoaderUsingGlide(String url, final ImageView imageView, Activity activity) {
        //Glide.with(.getAppContext()).load(url).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
       /* Glide.with(.getAppContext()).load(url).centerCrop().priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.ALL)
                .errorDataSet(R.drawable.ic_image_grey_500_18dp).into(imageView);*/
        Glide.with(activity).load(url).apply(getOption("Default")).into(imageView);
    }

    public static void imageLoaderUsingGlide(String url, final ImageView imageView, Activity activity) {
        //Glide.with(.getAppContext()).load(url).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
       /* Glide.with(.getAppContext()).load(url).asBitmap().priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.ALL).errorDataSet(R.drawable.ic_image_grey_500_18dp).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                imageView.setImageBitmap(resource);
            }
        });*/
        Glide.with(activity).load(url).apply(getOption("Default")).into(imageView);
    }

    public static void glideActualImgLoader(Drawable url, final ImageView imageView, Activity activity) {
        //Glide.with(.getAppContext()).load(url).asBitmap().diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
       /* Glide.with(.getAppContext()).load(url).asBitmap().priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.ALL).errorDataSet(R.drawable.ic_image_grey_500_18dp).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                imageView.setImageBitmap(resource);
            }
        });*/
        Glide.with(activity).load(url).apply(getOption("Default")).into(imageView);
    }

    /*public static void definedSizeLoaderUsingGlide(String url, final ImageView imageView) {
        // Glide.with(.getAppContext()).load(url).asBitmap().errorDataSet(R.drawable.ic_image_grey_500_18dp).override(300, 300).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        // Glide.with(.getAppContext()).load(url).asBitmap().errorDataSet(R.drawable.error1).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
        Glide.with(.getAppContext()).load(url).apply(getOption("fixed")).into(imageView);
    }*/


    private static RequestOptions getOption(String which) {

        RequestOptions options;
        switch (which) {
            case "fixed":
                options = new RequestOptions()
                        .error(R.drawable.no_photo)
                        .override(300, 300)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.IMMEDIATE);
                break;
            default:
                options = new RequestOptions()
                        .error(R.drawable.no_photo)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.IMMEDIATE);
                break;
        }
        return options;
    }

    public static String decimal2Digits(String value, Boolean isPriceWithType) {

        String mValue;
        DecimalFormat mDecimalFormat = new DecimalFormat("#.00");
        if (isPriceWithType) {
            mValue = mDecimalFormat.format(Double.valueOf(getItemPriceAndType(value)[0]));
        } else {
            mValue = mDecimalFormat.format(Double.valueOf(value));
        }
        return mValue;
    }

    public static void toastShort(Context context, String msg) {
        if (context != null) {
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public static void toastShort(Context context, int msg) {
        if (context != null) {
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public static void toastLong(Context context, String msg) {

        if (context != null) {
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public static void toastLong(Context context, int msg) {
        if (context != null) {
            Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            toast.show();
        }

    }

    @SuppressLint("HardwareIds")
    public static String getAndroidId(Activity activity) {
        String mAndroidId = "";

        mAndroidId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        return mAndroidId;
    }

    public static Boolean isUserLoggedIn(Activity activity) {

        int mDbSize;
        mDbSize = UserDetailsDB.getInstance(activity).getSizeOfList();
        if (mDbSize > 0) {
            return true;
        } else {
            return false;
        }

    }

    public static String getDayId() {
        String mDayId = "";

        try {

            //when app in arabic :-
            //E/getDayId() sdf: java.text.SimpleDateFormat@206940
            //E/getDayId() d: Fri Sep 16 11:22:10 GMT+05:30 2022
            //E/getDayId() dayOfTheWeek: Fri
            // E/getDayId() else: called

            //When app in english :-
            //E/getDayId() sdf: java.text.SimpleDateFormat@206940
            // E/getDayId() d: Fri Sep 16 11:24:08 GMT+05:30 2022
            // E/getDayId() dayOfTheWeek: Friday


            SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
            // Log.e("getDayId() sdf",""+sdf);
            Date d = new Date();
            // Log.e("getDayId() d",""+d);
            String dayOfTheWeek = sdf.format(d);
            // Log.e("getDayId() dayOfTheWeek",""+dayOfTheWeek);
            //  //Log.e("dayOfTheWeek",dayOfTheWeek);
            if (dayOfTheWeek.equalsIgnoreCase("sunday") || dayOfTheWeek.equalsIgnoreCase("sun")) {
                mDayId = "1";
            } else if (dayOfTheWeek.equalsIgnoreCase("monday") || dayOfTheWeek.equalsIgnoreCase("mon")) {
                mDayId = "2";
            } else if (dayOfTheWeek.equalsIgnoreCase("tuesday") || dayOfTheWeek.equalsIgnoreCase("tue")) {
                mDayId = "3";
            } else if (dayOfTheWeek.equalsIgnoreCase("wednesday") || dayOfTheWeek.equalsIgnoreCase("wed")) {
                mDayId = "4";
            } else if (dayOfTheWeek.equalsIgnoreCase("thursday") || dayOfTheWeek.equalsIgnoreCase("thu")) {
                mDayId = "5";
            } else if (dayOfTheWeek.equalsIgnoreCase("friday") || dayOfTheWeek.equalsIgnoreCase("fri")) {
                mDayId = "6";
            } else if (dayOfTheWeek.equalsIgnoreCase("saturday") || dayOfTheWeek.equalsIgnoreCase("sat")) {
                mDayId = "7";
            } else {
                // Log.e("getDayId() else","called");
            }

        } catch (Exception e) {
            //Log.e("getDayId() excep",""+e.toString());
        }

        return mDayId;
    }

    public static Boolean mIsArabic(Activity activity) {

        boolean mIsArabic = false;

        try {

            String mCurrentLanguageCode = LanguageDetailsDB.getInstance(activity).get_language_Details().getCode();
            if (mCurrentLanguageCode.equals("ae")) {
                mIsArabic = true;
            }

        } catch (Exception e) {
            //Log.e(" excep mIsArabic ",""+e.toString());
        }

        return mIsArabic;
    }

    public static boolean networkAvailabilityCheck(Context context) {

        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                return info != null && info.isConnected();
            }
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }


    }

    public static void customFont(final Context context, final View root, final String fontPath) {
        try {
            if (root instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) root;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++)
                    customFont(context, viewGroup.getChildAt(i), fontPath);
            } else if (root instanceof TextView)
                ((TextView) root).setTypeface(Typeface.createFromAsset(context.getAssets(), fontPath));
        } catch (Exception e) {
          /*  //Log.e(TAG, String.format(
                    "ErrorDataSet occured when trying to apply %s googleSans for %s view", fontPath, root));*/
            e.printStackTrace();
        }
    }

    public static String inputStreamToStringConversion(BufferedReader bufferedReader) {
        try {
            String line;
            String result = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public static String postDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), DefaultNames.UTF_FORMAT));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), DefaultNames.UTF_FORMAT));
        }

        return result.toString();
    }


    public static boolean emailFormatValidation(String inputEmail) {
        if (inputEmail != null && !inputEmail.isEmpty()) {
            Pattern pattern;
            Matcher matcher;
//        pattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
            pattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)");
            matcher = pattern.matcher(inputEmail);
            return matcher.matches();
        } else {
            return false;
        }

    }

    public static void clearEditTextFocus(View view, final Activity activity) {

        //   mEditText = new EditText(activity);

        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {

                    if (activity.getCurrentFocus() != null) {


                        /*InputMethodManager mInputMethodMgr = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        mInputMethodMgr.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);*/

                        // activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                        //   mEditText.clearFocus();
                        //  //Log.e("Out side edit text ","pressed.");


                    }
                    return false;
                }
            });
        }/*else {
            mEditText = (EditText) view;
        }*/

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View mOtherView = ((ViewGroup) view).getChildAt(i);
                clearEditTextFocus(mOtherView, activity);
            }
        }
    }

    public static void hideDeviceKeyBoard(View view, final Activity activity) {

        if (activity != null) {
             /* InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);*/

            if (!(view instanceof AutoCompleteTextView)) {
                view.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {

                        if (activity.getCurrentFocus() != null) {
                            InputMethodManager mInputMethodMgr = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                            mInputMethodMgr.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                        }
                        return false;
                    }
                });
            }

            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    View mOtherView = ((ViewGroup) view).getChildAt(i);
                    //mHideDeviceKeyBoard(mOtherView, activity);
                }
            }
        }


    }

    public static Bitmap textToBitmapConversion(String text, float textSize, int textColor) {
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.0f); // round
        int height = (int) (baseline + paint.descent() + 0.0f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    public static String[] getItemPriceAndType(String price) {

        String[] mPriceAndType = new String[2]; // s[0] is price.s[1] is price type.

        //For price fetching :-
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(price);
        matcher.find();

        mPriceAndType[0] = matcher.group(); // Price value...

        // For symbol fetching :-
        String mRemoveNumbers = price.replaceAll("[0-9]", "");
        String mRemoveDecimalIfAvailable = mRemoveNumbers.replace(".", "");
        String mRemoveWhiteSpaceIfAvailable = mRemoveDecimalIfAvailable.replace(" ", "");

        mPriceAndType[1] = mRemoveWhiteSpaceIfAvailable; // Price symbol...

        return mPriceAndType;


        /* String mPrice = "";
        // String str="123456 $";
        // String str="$10.656565";
        // Pattern pattern = Pattern.compile("\\w+([0-9]+)\\w+([0-9]+)");
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher matcher = pattern.matcher(price);
        // for(int i = 0 ; i < matcher.groupCount(); i++) {
        matcher.find();
        // System.out.println();
       // Log.d("*************","*****************");
       // //Log.e(" Matcher",matcher.group());
       // Log.d("*************","*****************");



       // For symbol fetching :-
        // String fgfg = "18.000 د.ك";
      //  String mActualPriceWithType = price;
        String mRemoveNumbers = price.replaceAll("[0-9]","");
        String mRemoveDecimalIfAvailable = mRemoveNumbers.replace(".","");
        String mRemoveWhiteSpaceIfAvailable = mRemoveDecimalIfAvailable.replace(" ","");
       // Log.d("*************", "*****************");
       // //Log.e(" CHARACTER", ewyuyue);
       // Log.d("*************", "*****************");


       */

    }

    public static void messageDialog(final Activity mContext, String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

        alertDialogBuilder
                .setMessage(message)
                //.setTitle(mContext.getString(R.string.))
                /*.setItems(new String[]{mContext.getString(R.string.coupon_code_error_1), mContext.getString(R.string.coupon_code_error_2)
                                , mContext.getString(R.string.coupon_code_error_3), mContext.getString(R.string.coupon_code_error_4)},
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })*/
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public static void msgDialogOk(final Activity mContext, String title, String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder
                .setMessage(message)
                //.setTitle(mContext.getString(R.string.))
                .setCancelable(true)
                .setPositiveButton(mContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public static void msgDialogOkWithTitle(final Activity mContext, String title, String message) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);

        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(mContext.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    public static String getPostData(HashMap<String, String> params) {
        try {
            StringBuilder mResult = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first)
                    first = false;
                else
                    mResult.append(DefaultNames.AND_SYMBOL);

                mResult.append(URLEncoder.encode(entry.getKey(), DefaultNames.CONVERT_TYPE));
                mResult.append(DefaultNames.EQUAL_SYMBOL);
                mResult.append(URLEncoder.encode(entry.getValue(), DefaultNames.CONVERT_TYPE));
            }

            return mResult.toString();
        } catch (Exception e) {
            // //Log.e("getPostData() Exc",e.toString());
            return null;
        }
    }

    public static String getCurrentDate() {

        String mDate;

        Calendar mInitialCalendar;
        mInitialCalendar = Calendar.getInstance(TimeZone.getDefault());
        Integer iDay = mInitialCalendar.get(Calendar.DAY_OF_MONTH);
        Integer iMonth = mInitialCalendar.get(Calendar.MONTH);
        Integer iYear = mInitialCalendar.get(Calendar.YEAR);
        String iTempDay, iTempMonth;

        String day = String.valueOf(mInitialCalendar.get(Calendar.DAY_OF_MONTH));
        Integer jMonth = mInitialCalendar.get(Calendar.MONTH) + 1;
        String month = String.valueOf(jMonth);

        mDate = day + "-" + month + "-" + iYear;

        return mDate;
    }

   /* public static String getCurrentTime() {

        String mTime="";

        if (android.text.format.DateFormat.is24HourFormat(.getAppContext())) {

             //  //Log.e("24Hrs Format ","called");

            //Sample output :- 12:59 -> 12:59 AM
            String mCurrentTime = DateFormat.getTimeInstance().format(new Date());
            String[] mSplitTime = mCurrentTime.split(":");
            String m24HrsTime = mSplitTime[0] + ":" + mSplitTime[1];

            mTime = m24HrsTime; // output as 24hrs



        } else {

            // //Log.e("12Hrs Format ","called");

            Calendar c = Calendar.getInstance();
            String mTempHours = "" + c.get(Calendar.HOUR);
            String mTempMinute = "" + c.get(Calendar.MINUTE);
            String mTempMeridian;

            if(c.get(Calendar.AM_PM)==0){
                mTempMeridian = "AM";
            }else {
                mTempMeridian = "PM";
            }
            // //Log.e("12hrs Calender check ",""+c.get(Calendar.HOUR_OF_DAY));
            // //Log.e(TAG, "12Hr Time -> " + mTempHours + ":" + mTempMinute + " " + mMeridian.getText().toString());
            String mStrLHrs, mStrLMin;
            if (mTempHours.length() == 1) {
                mStrLHrs = "0" + mTempHours;
            } else {
                mStrLHrs = mTempHours;
            }
            if (mTempMinute.length() == 1) {
                mStrLMin = "0" + mTempMinute;
            } else {
                mStrLMin = mTempMinute;
            }
            mTime = mStrLHrs + ":" + mStrLMin+" "+mTempMeridian;  // 12hrs format output

            // 24hrs format output :-
            try {
                SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm");
                SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a");
                Date date = parseFormat.parse(mTime);
                // mTime = displayFormat.format(date)+" "+mTempMeridian;
                mTime = displayFormat.format(date);

            }catch (ParseException p){
             //   //Log.e("12hrs Excep ",p.toString());
                mTime = "";
            }
        }

        return mTime;
    }*/


    public static String getCurrentTime(Activity activity) {

        String mTime = "";

        if (android.text.format.DateFormat.is24HourFormat(activity)) {

            // //Log.e("24Hrs Format ", "called");
            //Sample output :- 12:59 -> 12:59 AM
            String mCurrentTime = DateFormat.getTimeInstance().format(new Date());
            String[] mSplitTime = mCurrentTime.split(":");


           /* String mLanguageId = LanguageDetailsDB.getInstance(.getAppContext()).get_language_Details();
            if (mLanguageId != null && !mLanguageId.isEmpty()) {
                if (mLanguageId.equals("2")) {
                    String mArabicHour = mSplitTime[0];
                    String mArabicMinute = mSplitTime[1];
                    String mConvertedHr = arabicToDecimalConvertion(mArabicHour);
                    String mConvertedMin = arabicToDecimalConvertion(mArabicMinute);
                    mTime = mConvertedHr + ":" + mConvertedMin;
                    //  //Log.e("24Hrs mTime ", mTime);
                    // mConfirmOrderObject.put(DefaultNames.delivery_time, mConvertedHr+":"+mConvertedMin+ ":00");
                } else {
                    mTime = mSplitTime[0] + ":" + mSplitTime[1]; // output as 24hrs
                    // //Log.e("24Hrs mTime ", mTime);
                    // mConfirmOrderObject.put(DefaultNames.delivery_time, ApiMethods.getCurrentTimeOnly(getActivity()) + ":00");
                }
            }*/

            mTime = mSplitTime[0] + ":" + mSplitTime[1]; // output as 24hrs
            // //Log.e("24Hrs mTime ", mTime);
            // mConfirmOrderObject.put(DefaultNames.delivery_time, ApiMethods.getCurrentTimeOnly(getActivity()) + ":00");


        } else {

            //  //Log.e("12Hrs Format ", "called");


            /* String mLanguageId = LanguageDetailsDB.getInstance(.getAppContext()).get_language_Details();
            if (mLanguageId != null && !mLanguageId.isEmpty()) {
                if (mLanguageId.equals("2")) {

                    //When device in arabic language :-
                    //  //Log.e("AR 12Hrs Format ", "called");
                    Calendar c = Calendar.getInstance();
                    String mTempHours = "" + c.get(Calendar.HOUR);
                    String mTempMinute = "" + c.get(Calendar.MINUTE);
                    String mTempMeridian;
                    //  //Log.e("mTempHours", mTempHours);
                    //  //Log.e("mTempMinute", mTempMinute);
                    //  //Log.e("12Hrs Meridian ", String.valueOf(c.get(Calendar.AM_PM)));
                    String mTempMerid = arabicToDecimalConvertion(String.valueOf(c.get(Calendar.AM_PM)));
                    if (Integer.valueOf(mTempMerid) == 0) {
                        // if (c.get(Calendar.AM_PM) == 0) {
                        mTempMeridian = "AM";
                    } else {
                        mTempMeridian = "PM";
                    }
                    // //Log.e("12hrs Calender check ",""+c.get(Calendar.HOUR_OF_DAY));
                    // //Log.e(TAG, "12Hr Time -> " + mTempHours + ":" + mTempMinute + " " + mMeridian.getText().toString());
                    String mStrLHrs, mStrLMin;
                    if (arabicToDecimalConvertion(mTempHours).length() == 1) {
                        mStrLHrs = "0" + arabicToDecimalConvertion(mTempHours);
                    } else {
                        mStrLHrs = arabicToDecimalConvertion(mTempHours);
                    }
                    if (arabicToDecimalConvertion(mTempMinute).length() == 1) {
                        mStrLMin = "0" + arabicToDecimalConvertion(mTempMinute);
                    } else {
                        mStrLMin = arabicToDecimalConvertion(mTempMinute);
                    }
                    mTime = mStrLHrs + ":" + mStrLMin + " " + mTempMeridian;  // 12hrs format output
                    // //Log.e("12Hrs mTime ", mTime);
                    // 24hrs format output :-
                    try {
                        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                        Date date = parseFormat.parse(mTime);
                        // mTime = displayFormat.format(date)+" "+mTempMeridian;
                        String[] mSplitTime = displayFormat.format(date).split(":");
                        String mArabicHour = mSplitTime[0];
                        String mArabicMinute = mSplitTime[1];
                        String mConvertedHr = arabicToDecimalConvertion(mArabicHour);
                        String mConvertedMin = arabicToDecimalConvertion(mArabicMinute);
                        mTime = mConvertedHr + ":" + mConvertedMin;
                        // //Log.e("12Hrs as 24hrs mTime ", mTime);
                    } catch (ParseException p) {
                        //  //Log.e("12hrs Excep ", p.toString());
                        mTime = "";
                    }
                } else {
                    //When device in english language :-
                    // //Log.e("EN 12Hrs Format ", "called");
                    Calendar c = Calendar.getInstance();
                    String mTempHours = "" + c.get(Calendar.HOUR);
                    String mTempMinute = "" + c.get(Calendar.MINUTE);
                    String mTempMeridian;
                    //  //Log.e("mTempHours", mTempHours);
                    //  //Log.e("mTempMinute", mTempMinute);
                    //  //Log.e("12Hrs Meridian ", String.valueOf(c.get(Calendar.AM_PM)));
                    if (c.get(Calendar.AM_PM) == 0) {
                        // if (c.get(Calendar.AM_PM) == 0) {
                        mTempMeridian = "AM";
                    } else {
                        mTempMeridian = "PM";
                    }
                    // //Log.e("12hrs Calender check ",""+c.get(Calendar.HOUR_OF_DAY));
                    // //Log.e(TAG, "12Hr Time -> " + mTempHours + ":" + mTempMinute + " " + mMeridian.getText().toString());
                    String mStrLHrs, mStrLMin;
                    if (mTempHours.length() == 1) {
                        mStrLHrs = "0" + mTempHours;
                    } else {
                        mStrLHrs = mTempHours;
                    }
                    if (mTempMinute.length() == 1) {
                        mStrLMin = "0" + mTempMinute;
                    } else {
                        mStrLMin = mTempMinute;
                    }
                    mTime = mStrLHrs + ":" + mStrLMin + " " + mTempMeridian;  // 12hrs format output
                    //  //Log.e("12Hrs mTime ", mTime);
                    // 24hrs format output :-
                    try {
                        SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                        Date date = parseFormat.parse(mTime);
                        // mTime = displayFormat.format(date)+" "+mTempMeridian;
                        mTime = displayFormat.format(date);
                        //   //Log.e("12Hrs as 24hrs mTime ", mTime);
                    } catch (ParseException p) {
                        //   //Log.e("12hrs Excep ", p.toString());
                        mTime = "";
                    }
                }
            }*/


            //When device in english language :-

            // //Log.e("EN 12Hrs Format ", "called");

            Calendar c = Calendar.getInstance();
            String mTempHours = "" + c.get(Calendar.HOUR);
            String mTempMinute = "" + c.get(Calendar.MINUTE);
            String mTempMeridian;

            //  //Log.e("mTempHours", mTempHours);
            //  //Log.e("mTempMinute", mTempMinute);

            //  //Log.e("12Hrs Meridian ", String.valueOf(c.get(Calendar.AM_PM)));


            if (c.get(Calendar.AM_PM) == 0) {
                // if (c.get(Calendar.AM_PM) == 0) {
                mTempMeridian = "AM";
            } else {
                mTempMeridian = "PM";
            }
            // //Log.e("12hrs Calender check ",""+c.get(Calendar.HOUR_OF_DAY));
            // //Log.e(TAG, "12Hr Time -> " + mTempHours + ":" + mTempMinute + " " + mMeridian.getText().toString());
            String mStrLHrs, mStrLMin;
            if (mTempHours.length() == 1) {
                mStrLHrs = "0" + mTempHours;
            } else {
                mStrLHrs = mTempHours;
            }

            if (mTempMinute.length() == 1) {
                mStrLMin = "0" + mTempMinute;
            } else {
                mStrLMin = mTempMinute;
            }

            mTime = mStrLHrs + ":" + mStrLMin + " " + mTempMeridian;  // 12hrs format output

            //  //Log.e("12Hrs mTime ", mTime);

            // 24hrs format output :-
            try {
                SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                Date date = parseFormat.parse(mTime);
                // mTime = displayFormat.format(date)+" "+mTempMeridian;


                mTime = displayFormat.format(date);
                // //Log.e("12Hrs as 24hrs mTime ", mTime);


            } catch (ParseException p) {
                //  //Log.e("12hrs Excep ", p.toString());
                mTime = "";
            }


        }

        return mTime;
    }


    public static int[] hoursDifference(String start_date, String end_date) {

        int[] mHrsMin = new int[2];

        try {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            Date startDate = simpleDateFormat.parse(start_date);
            Date endDate = simpleDateFormat.parse(end_date);

            long difference = endDate.getTime() - startDate.getTime();
            if (difference < 0) {
                Date dateMax = simpleDateFormat.parse("24:00");
                Date dateMin = simpleDateFormat.parse("00:00");
                difference = (dateMax.getTime() - startDate.getTime()) + (endDate.getTime() - dateMin.getTime());
            }
            int days = (int) (difference / (1000 * 60 * 60 * 24));
            int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
            int min = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);

            //  //Log.e("log_tag","Hours: "+hours+", Mins: "+min);
            mHrsMin[0] = hours;
            mHrsMin[1] = min;

        } catch (Exception e) {
            // //Log.e("hoursDifference Excep ",e.toString());
            mHrsMin[0] = 0;
            mHrsMin[1] = 0;
        }

        return mHrsMin;
    }

    public static void logError(String tag, String message) {

        //  //Log.e(tag,message);

    }

    public static String apiResponseErrorMsg(Activity activity, ResponseBody requestBody) {

        String mErrorMsgToShow = "";
        try {
            // ResponseBody requestBody = response.errorBody();
            if (requestBody != null) {
                BufferedReader r = new BufferedReader(new InputStreamReader(requestBody.byteStream()));
                StringBuilder total = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    total.append(line).append('\n');
                }
                JSONObject jObjError = new JSONObject(total.toString());
                if (!jObjError.isNull("error")) {
                    JSONObject jsonErrorObject = jObjError.getJSONObject("error");
                    if (!jsonErrorObject.isNull("message")) {
                        mErrorMsgToShow = jsonErrorObject.getString("message");
                    } else {
                        mErrorMsgToShow = activity.getString(R.string.process_failed_please_try_again);
                    }
                } else {
                    mErrorMsgToShow = activity.getString(R.string.process_failed_please_try_again);
                }
            } else {
                mErrorMsgToShow = activity.getString(R.string.process_failed_please_try_again);
            }

        } catch (Exception e) {
            // e.printStackTrace();
            mErrorMsgToShow = activity.getString(R.string.process_failed_please_try_again);
        }

        return mErrorMsgToShow;

    }

}
