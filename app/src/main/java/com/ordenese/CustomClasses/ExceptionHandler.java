package com.ordenese.CustomClasses;


import android.app.Activity;
import android.content.Intent;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultUEH;
    private String mLocalPath;
    private Activity mActivity;

    public ExceptionHandler() {

    }

    public ExceptionHandler(String localPath, Activity activity) {
        // //Log.e(TAG,"ExceptionHandler Called.");
        this.mLocalPath = localPath;
        this.mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        //  //Log.e("ExceptionHandler "," "+Thread.getDefaultUncaughtExceptionHandler());
        this.mActivity = activity;
    }

    public void uncaughtException(Thread t, Throwable e) {
        // //Log.e(TAG,"uncaughtException Called.");
        final Writer mResult = new StringWriter();
        final PrintWriter mPrintWriter = new PrintWriter(mResult);
        e.printStackTrace(mPrintWriter);
        String mStacktrace = mResult.toString();
        mPrintWriter.close();
        String filename = "Stacktrace";

        writeToFile(mStacktrace, filename);
        sendToServer(mStacktrace, filename);

        mDefaultUEH.uncaughtException(t, e);
    }

    private void writeToFile(String stacktrace, String filename) {
        // //Log.e(TAG,"writeToFile Called.");
        try {
            BufferedWriter mBufferedWriter = new BufferedWriter(new FileWriter(
                    mLocalPath + "/" + filename));
            mBufferedWriter.write(stacktrace);
            mBufferedWriter.flush();
            mBufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToServer(String stacktrace, String filename) {

        // //Log.e(TAG,"sendToServer Called.");
        String mPostData = mLocalPath + " : " + stacktrace;
        final Intent mEmailIntent = new Intent(Intent.ACTION_SEND);

        mEmailIntent.setType("text/plain");
        mEmailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{
                "abc@mail.com"
        });
        mEmailIntent.putExtra(Intent.EXTRA_SUBJECT, "App ErrorDataSet Report !");
        mEmailIntent.putExtra(Intent.EXTRA_TEXT, mPostData);
        mEmailIntent.setType("message/rfc822");

        try {
            mActivity.startActivity(Intent.createChooser(mEmailIntent,
                    "Send email using..."));
        } catch (Exception ex) {
          //  //Log.e("ExceptionHandler ", "sendToServer Exception " + ex.toString());

        }
    }
}