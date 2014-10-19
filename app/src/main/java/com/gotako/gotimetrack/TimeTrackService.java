/**
 *
 */
package com.gotako.gotimetrack;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author Nam
 */
public class TimeTrackService extends Service {
    private static final String TIME_STATUS = "STATUS";

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

}
