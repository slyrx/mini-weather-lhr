package cn.edu.pku.slyrx.miniweatherlhr;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import cn.edu.pku.slyrx.bean.TodayWeather;
import cn.edu.pku.slyrx.util.AndroidShare;
import cn.edu.pku.slyrx.util.NetUtil;
import cn.edu.pku.slyrx.util.updateService;

/**
 * Created by slyrx on 16/9/26.
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final String TAG = "MyMainActivity";

    private ImageView mUpdateBtn, mLocationBtn, mShareBtn;
    private ImageView mCitySelect;

    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv, pmQualityTv, temperatureTv, climateTv, windTv, city_name_Tv, temperTodayTv;
    private ImageView weatherImg, pmImg;

    private String mSelectCityNum = null;

    private updateService mUpdateService = null;

    private LocationManager mLocationManager = null;

    private TodayWeather mTodayWeather;

    void updateTodayWeather(TodayWeather todayWeather) {
        city_name_Tv.setText(todayWeather.getCity() + "天气");
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度：" + todayWeather.getShidu());
        temperTodayTv.setText("当下温度" + todayWeather.getWendu() + "度");
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());


        if(todayWeather.getHigh()==null){

            temperatureTv.setText(todayWeather.getLow());

        }else if(todayWeather.getLow()==null){

            temperatureTv.setText(todayWeather.getHigh());

        }else if((todayWeather.getHigh()==null)&&(todayWeather.getLow()==null)){
            temperatureTv.setText("当前无温度数据");
        }else {
            temperatureTv.setText(todayWeather.getHigh() + "~" + todayWeather.getLow());
        }

        climateTv.setText(todayWeather.getType());
        windTv.setText("风力" + todayWeather.getFengli());
        Toast.makeText(MainActivity.this, "更新成功!", Toast.LENGTH_SHORT).show();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };

    void initView() {
        city_name_Tv = (TextView) findViewById(R.id.title_city_name);
        cityTv = (TextView) findViewById(R.id.city);
        timeTv = (TextView) findViewById(R.id.time);
        humidityTv = (TextView) findViewById(R.id.humidity);
        weekTv = (TextView) findViewById(R.id.week_today);
        pmDataTv = (TextView) findViewById(R.id.pm_data);
        pmQualityTv = (TextView) findViewById(R.id.pm2_5_quality);
        pmImg = (ImageView) findViewById(R.id.pm2_5_img);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.climate);
        windTv = (TextView) findViewById(R.id.wind);
        weatherImg = (ImageView) findViewById(R.id.weather_img);
        temperTodayTv = (TextView) findViewById(R.id.temperature_current);

        city_name_Tv.setText("N/A");
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        weekTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        temperTodayTv.setText("N/A");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity->Oncreate");

        setContentView(R.layout.weather_info);

        mUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        mUpdateBtn.setOnClickListener(this);
        mLocationBtn = (ImageView) findViewById(R.id.title_location);
        mLocationBtn.setOnClickListener(this);
        mShareBtn = (ImageView) findViewById(R.id.title_share);
        mShareBtn.setOnClickListener(this);

        //检查网络是否可用
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
            Log.d("myMiniWeather", "网络ok");
            Toast.makeText(MainActivity.this, "网络ok！", Toast.LENGTH_LONG).show();
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            queryWeatherCode(cityCode);

        } else {
            Log.d("myMiniWeather", "网络down");
            Toast.makeText(MainActivity.this, "网络down！", Toast.LENGTH_LONG).show();
        }

        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        mCitySelect.setOnClickListener(this);

        initView();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //开启服务，用以更新天气数据
//        mUpdateService = new updateService();
//        mUpdateService.setMain_Activity(this);
//        startService(new Intent(getBaseContext(), updateService.class));

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.title_city_manager) {


            Intent i = new Intent(this, SelectCity.class);
            startActivityForResult(i, 110);

        }

        switch (view.getId()){
            case R.id.title_update_btn:
                updateBtnRun();
                break;
            case R.id.title_location:
                Location curLoc = getLocation();
                Toast.makeText(MainActivity.this, "经度: "+ curLoc.getLatitude() + "纬度: " + curLoc.getLongitude(), Toast.LENGTH_LONG).show();
                break;
            case R.id.title_share:
                shareToFriend();
                break;
            default:
                break;
        }

//        if (view.getId() == R.id.title_update_btn) {
//            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
//            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
//            if(null != mSelectCityNum) {
//                cityCode = mSelectCityNum;
//            }
//            Log.d("myMiniWeather", cityCode);
//
//            //检查网络是否可用
//            if(NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE){
//                Log.d("myMiniWeather", "网络ok");
//                queryWeatherCode(cityCode);
//            }else {
//                Log.d("myMiniWeather", "网络down");
//                Toast.makeText(MainActivity.this, "网络down！", Toast.LENGTH_LONG).show();
//            }
//            updateBtnRun();
//        }

    }

    private void shareToFriend(){
        String shareWords = mTodayWeather.getCity() + " 今日" + mTodayWeather.getDate() + " 温度" + mTodayWeather.getWendu() + "度";

        AndroidShare as = new AndroidShare(
                MainActivity.this,
                shareWords,
                "文字");
        as.show();
    }

    public void updateBtnRun() {
        SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
        String cityCode = sharedPreferences.getString("main_city_code", "101010100");
        if (null != mSelectCityNum) {
            cityCode = mSelectCityNum;
        }
        Log.d("myMiniWeather", cityCode);

        //检查网络是否可用
        if (NetUtil.getNetworkState(this) != NetUtil.NETWORK_NONE) {
            Log.d("myMiniWeather", "网络ok");
            queryWeatherCode(cityCode);
        } else {
            Log.d("myMiniWeather", "网络down");
            Toast.makeText(MainActivity.this, "网络down！", Toast.LENGTH_LONG).show();
        }
    }

    private void queryWeatherCode(String cityCode) {
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        Log.d("myMiniWeather", address);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection con = null;
                TodayWeather todayWeather = null;

                try {
                    URL url = new URL(address);
                    con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(8000);
                    con.setReadTimeout(8000);

                    InputStream in = con.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        response.append(str);
                        Log.d("myMiniWeather", str);
                    }

                    String responseStr = response.toString();
                    Log.d("myMiniWeather", responseStr);
                    todayWeather = parseXML(responseStr);
                    if (todayWeather != null) {
                        Log.d("myMiniWeather", todayWeather.toString());
                        mTodayWeather = todayWeather;

                        Message msg = new Message();
                        msg.what = UPDATE_TODAY_WEATHER;
                        msg.obj = todayWeather;
                        mHandler.sendMessage(msg);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                }
            }
        }).start();
    }

    private TodayWeather parseXML(String xmldata) {
        TodayWeather todayWeather = null;

        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;

        try {
            XmlPullParserFactory fac = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = fac.newPullParser();
            xmlPullParser.setInput(new StringReader(xmldata));

            int eventType = xmlPullParser.getEventType();
            Log.d("myMiniWeather", "parseXML");

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }

                        if (todayWeather != null) {
                            if (xmlPullParser.getName().equals("city")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setCity(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("updatetime")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setUpdatetime(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setShidu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("wendu")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setWendu(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setPm25(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("quality")) {
                                eventType = xmlPullParser.next();
                                todayWeather.setQuality(xmlPullParser.getText());
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengxiang(xmlPullParser.getText());
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setFengli(xmlPullParser.getText());
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setDate(xmlPullParser.getText());
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && dateCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setHigh(xmlPullParser.getText().substring(2).trim());
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setLow(xmlPullParser.getText().substring(2).trim());
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                eventType = xmlPullParser.next();
                                todayWeather.setType(xmlPullParser.getText());
                                typeCount++;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }

                eventType = xmlPullParser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return todayWeather;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle bundle = data.getBundleExtra("GetSelectCityNumber");
        String str = bundle.getString("City");

        if (110 == requestCode && 101 == resultCode) {
            mSelectCityNum = str;

            //同时更新画面
            if (null != mSelectCityNum) {
                queryWeatherCode(mSelectCityNum);
            }
        }
    }

    private Location getLocation() {

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        Location gpsLocation = null;
        Location netLocation = null;

        //状态监听
        GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
            @Override
            public void onGpsStatusChanged(int event) {
                switch (event) {
                    case GpsStatus.GPS_EVENT_FIRST_FIX:
                        break;
                    case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                        GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                        int maxSatellites = gpsStatus.getMaxSatellites();
                        Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                        int count = 0;
                        while (iters.hasNext() && count <= maxSatellites) {
                            GpsSatellite s = iters.next();
                            count++;
                        }
                        break;
                    case GpsStatus.GPS_EVENT_STARTED:
                        break;
                    case GpsStatus.GPS_EVENT_STOPPED:
                        break;
                }
            }
        };


        mLocationManager.addGpsStatusListener(gpsListener);

        if(netWorksOpen()){
            mLocationManager.requestLocationUpdates("network", 20000, 5, locationListener);
            netLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        if(gpsIsOpen()){
            mLocationManager.requestLocationUpdates("gps", 20000, 5, locationListener);
            gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }


        if (gpsLocation == null && netLocation == null) {
            return null;
        }

        if (gpsLocation != null && netLocation != null) {
            if (gpsLocation.getTime() < netLocation.getTime()) {
                gpsLocation = null;
                return netLocation;
            } else {
                netLocation = null;
                return gpsLocation;
            }
        }

        if (gpsLocation == null) {
            return netLocation;
        } else {
            return gpsLocation;
        }
    }

    private boolean netWorksOpen(){
        boolean netlsOpen = true;

        if(!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            netlsOpen = false;
        }

        return netlsOpen;
    }

    private boolean gpsIsOpen() {
        boolean isOpen = true;
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {//没有开启GPS
            isOpen = false;
        }
        return isOpen;
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(location != null){
                //同步数据
            }else {
                //获取不到数据
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
