package com.example.esmidth.hellobaidumap;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Great Esmidth on 2016/1/23.
 * Stay Foolish
 */
public class GpsService extends Service {
    ArrayList<CellInfo> cellIds;
    private Gps gps = null;
    private boolean threadDisabled = false;

    @Override
    public void onCreate() {
        super.onCreate();

        gps = new Gps(GpsService.this);
        cellIds = UtilTool.init(GpsService.this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!threadDisabled) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (gps != null) {
                        Location location = gps.getLocation();
                        if (location == null) {
                            Log.v(TAG, "gps location null");
                            try {
                                location = UtilTool.callGear(GpsService.this, cellIds);
                            } catch (Exception e) {
                                location = null;
                                e.printStackTrace();
                            }
                            if (location == null) {
                                Log.v(TAG, "cell location null");
                            }
                        }

                        Intent intent = new Intent();
                        intent.putExtra("lat", location == null ? "" : location.getLatitude() + "");
                        intent.putExtra("lon", location == null ? "" : location.getLongitude() + "");
                        intent.setAction("Great Esmidth");
                        sendBroadcast(intent);
                    }
                }
            }
        }).start();


    }

    @Override
    public void onDestroy() {
        threadDisabled = true;
        if (cellIds != null && cellIds.size() > 0) {
            cellIds = null;
        }
        if (gps != null) {
            gps.closeLocation();
            gps = null;
        }
        super.onDestroy();
    }

    private final static String TAG = GpsService.class.getSimpleName();

    public IBinder onBind(Intent intent) {
        return null;
    }
}
