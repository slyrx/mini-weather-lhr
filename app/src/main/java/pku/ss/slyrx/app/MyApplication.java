package pku.ss.slyrx.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cn.edu.pku.slyrx.bean.City;
import pku.ss.slyrx.db.CityDB;

/**
 * Created by slyrx on 16/11/1.
 */
public class MyApplication extends Application {
    private static final String TAG = "MyAPP";

    private static Application mApplication;
    private static List<City> mCityList;
    private static CityDB mCityDB;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "MyApplication->Oncreate");
        mApplication = this;

        mCityDB = openCityDB();
        initCityList();
    }

    public static Application getInstance(){
        return mApplication;
    }

    private CityDB openCityDB(){

        String dirpath = Environment.getExternalStorageDirectory()//.getDataDirectory().getAbsolutePath()
                + File.separator + getPackageName()
                + File.separator + "databases"
                + File.separator;
        String path = dirpath + CityDB.CITY_DB_NAME;

        File db = new File(path);
        Log.d(TAG, path);

        File dir = new File(dirpath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        if(!db.exists()){
            Log.i("MyApp", "db is not exists");
            writeDB2Card(db);
        }else {
            writeDB2Card(db);
        }
        return new CityDB(this, path);
    }

    private void writeDB2Card(File db){
        try{
            InputStream is = getAssets().open("city.db");
            FileOutputStream fos = new FileOutputStream(db);
            int len = -1;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
                fos.flush();
            }
            fos.close();
            is.close();

        }catch (IOException e){
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void initCityList(){
        mCityList = new ArrayList<City>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }

    private boolean prepareCityList(){
        mCityList = mCityDB.getAllCity();
        for(City city : mCityList) {
            String cityName = city.getCity();
            Log.d(TAG, cityName);
        }
        return true;
    }

}
