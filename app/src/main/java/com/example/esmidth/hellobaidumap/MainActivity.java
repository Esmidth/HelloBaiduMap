package com.example.esmidth.hellobaidumap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;


public class MainActivity extends AppCompatActivity {
    //Loc
    String TAG = "123";
    public LocationClient locationClient = null;
    public MyLocationListener mylistener = new MyLocationListener();
    BitmapDescriptor mCurrentMarker;
    BDLocation bdLocation = null;
    TextView textView = null;
    MyLocationConfiguration.LocationMode mCurrentMode;
    private MyReceiver receiver = null;

    BaiduMap baiduMap;
    MapView mapView = null;


    //UI
    boolean isFirstLoc = true;
    Button requestLocButton;
    RadioGroup.OnCheckedChangeListener radioButtonListener;

    /* TODO: Add Location Function to the App*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        Log.i(TAG, UtilTool.isGpsEnabled((LocationManager) getSystemService(Context.LOCATION_SERVICE)) + "");
        if (!UtilTool.isGpsEnabled((LocationManager) getSystemService(Context.LOCATION_SERVICE))) {
            Toast.makeText(this, "GSP当前无法使用", Toast.LENGTH_LONG).show();
            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(callGPSSettingIntent);
            return;
        }
        startService(new Intent(this, GpsService.class));
        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("Great Esmidth");
        registerReceiver(receiver, filter);


        requestLocButton = (Button) findViewById(R.id.button1);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        requestLocButton.setText("NORMAL");

        final View.OnClickListener btnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mCurrentMode) {
                    case NORMAL:
                        requestLocButton.setText("跟随");
                        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                        baiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        textView = (TextView) findViewById(R.id.Latitude);
                        textView.setText("Latitude Test");
                        textView = null;

                        break;
                    case COMPASS:
                        requestLocButton.setText("普通");
                        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
                        baiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        textView = (TextView) findViewById(R.id.Latitude);
                        textView.setText("FDFDFD");
                        textView = null;

                        break;
                    case FOLLOWING:
                        requestLocButton.setText("罗盘");
                        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
                        baiduMap
                                .setMyLocationConfigeration(new MyLocationConfiguration(
                                        mCurrentMode, true, mCurrentMarker));
                        break;
                    default:
                        break;
                }
            }
        };
        requestLocButton.setOnClickListener(btnClickListener);

        RadioGroup group = (RadioGroup) this.findViewById(R.id.radioGroup);
        radioButtonListener = new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.defaulticon) {
                    mCurrentMarker = null;
                    baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, null));
                }
                if (checkedId == R.id.customicon) {
                    mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
                    baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker));
                }
            }
        };
        group.setOnCheckedChangeListener(radioButtonListener);

        //Initalize the MapView
        mapView = (MapView) findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        locationClient = new LocationClient(this);
        locationClient.registerLocationListener(mylistener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        locationClient.setLocOption(option);
        locationClient.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        stopService(new Intent(this, GpsService.class));
        locationClient.stop();
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;
        super.onDestroy();
    }


    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius()).direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            textView = (TextView) findViewById(R.id.Latitude);
            textView.setText(Double.toString(locData.latitude));
            textView = (TextView) findViewById(R.id.Longitude);
            textView.setText(Double.toString(location.getLongitude()));
            textView = null;
            baiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                baiduMap.animateMapStatus(u);
            }
        }

        public void onReceivePoi(BDLocation poiLocatioin) {
            if (poiLocatioin == null || mapView == null)
                return;
            textView = (TextView) findViewById(R.id.Longitude);
            textView.setText(Double.toString(poiLocatioin.getLongitude()));
            textView = (TextView) findViewById(R.id.Latitude);
            textView.setText(Double.toString(poiLocatioin.getLatitude()));
            textView = null;
        }
    }

    private class MyReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String lon = bundle.getString("lon");
            String lat = bundle.getString("lat");
            if (lon != null && !"".equals(lon) && lat != null && !"".equals(lat)) {
                textView = (TextView) findViewById(R.id.Latitude);
                textView.setText("Lat: " + lat);
                textView = (TextView) findViewById(R.id.Longitude);
                textView.setText("Lon: " + lon);

            }
        }
    }
}
