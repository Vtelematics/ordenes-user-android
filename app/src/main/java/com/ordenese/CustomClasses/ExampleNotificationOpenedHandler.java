
package com.ordenese.CustomClasses;

import android.content.Intent;
import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;
import com.ordenese.Activities.AppHome;

import org.json.JSONException;
import org.json.JSONObject;

public class ExampleNotificationOpenedHandler implements OneSignal.OSNotificationOpenedHandler {

    MainContext mInstance;

    public ExampleNotificationOpenedHandler(MainContext mInstance) {
        this.mInstance = mInstance;
    }

    @Override
    public void notificationOpened(OSNotificationOpenedResult osNotificationOpenedResult) {

        OSNotification notification = osNotificationOpenedResult.getNotification();
        OSNotificationAction.ActionType actionType = osNotificationOpenedResult.getAction().getType();
        JSONObject data = notification.getAdditionalData();

        //  Log.i("OSNotificationPayload", "result.notification.payload.toJSONObject().toString(): " + result.notification.payload.toJSONObject().toString());
        try {
            if (data != null) {
                if (!data.isNull("order_id")) {
                    ApiClass.ORDER_ID = data.getString("order_id");
                } else {
                    ApiClass.ORDER_ID = "";
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        if (actionType == OSNotificationAction.ActionType.ActionTaken)
//            Log.i("OneSignalExample", "Button pressed with id: " + osNotificationOpenedResult.getAction().getActionId());

        Intent intent = new Intent(mInstance, AppHome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        mInstance.startActivity(intent);


    }
}

