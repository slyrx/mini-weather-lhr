package cn.edu.pku.slyrx.util;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import cn.edu.pku.slyrx.miniweatherlhr.MainActivity;

/**
 * Created by slyrx on 2016/12/6.
 */
public class updateService extends Service {
    public MainActivity main_Activity = null;

    public void setMain_Activity(MainActivity mainActivity){
        this.main_Activity = mainActivity;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

        //创建新线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                //每隔一分钟，更新一次天气信息
                while (true){
                    //sleep 1分钟
                    try {
                        Thread.sleep(6000);
                        //main_Activity.updateBtnRun();

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        return START_STICKY;
    }
}
