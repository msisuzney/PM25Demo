package com.msisuzney.pm25phone;


import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SharedPreferences sp;

    private DashboardView pm1_0View;
    private DashboardView pm2_5View;
    private Button selectNumBtn;
    private TextView showCriticalVal;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init() {

        pm1_0View = (DashboardView) findViewById(R.id.pm1_0View);
        pm1_0View.setHeaderText("PM 1.0");
        pm2_5View = (DashboardView) findViewById(R.id.pm2_5View);
        pm2_5View.setHeaderText("PM 2.5");
        showCriticalVal = (TextView) findViewById(R.id.showCriticalVal);
        selectNumBtn = (Button) findViewById(R.id.selectNum);
        selectNumBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNumDialog();
            }
        });
        Button registerBtn, unregisterBtn;
        unregisterBtn = (Button) findViewById(R.id.stop);
        registerBtn = (Button) findViewById(R.id.send);
        registerBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                registerMine();
            }
        });
        unregisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unregisterMine();
            }
        });
        sp = getSharedPreferences(Constants.msisuzney, MODE_PRIVATE);

        //默认是100
        int val = sp.getInt(Constants.criticalVal, -1);
        if (val == -1) {
            sp.edit().putInt(Constants.criticalVal, Constants.default_criticalVal).apply();
            showCriticalVal.setText("默认PM2.5提醒临界值为" + Constants.default_criticalVal);
        } else {
            showCriticalVal.setText("PM2.5提醒临界值为" + val);
        }

        //重置为没用通知过
        sp.edit().putBoolean(Constants.notified, false).apply();
    }

    private void showNumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择PM2.5提醒临界值");

        final NumberPicker np = new NumberPicker(this);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(100, 100);
        np.setLayoutParams(lp);
        np.setMaxValue(150);
        np.setMinValue(20);
        np.setValue(100);
        builder.setView(np);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int x = np.getValue();
                Log.d(TAG, "select num " + x);
                sp.edit().putInt(Constants.criticalVal, x).apply();
                showCriticalVal.setText("PM2.5提醒临界值为" + x);

                //重置为没用通知过
                sp.edit().putBoolean(Constants.notified, false).apply();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }


    private void registerMine() {
        if (!sp.getBoolean(Constants.isRegister, false)) {
            RegisterService.registerMine(MainActivity.this);
            sp.edit().putBoolean(Constants.isRegister, true).apply();
        } else {
            Toast.makeText(MainActivity.this, "已经注册过啦！", Toast.LENGTH_SHORT).show();
        }
    }

    private void unregisterMine() {
        if (sp.getBoolean(Constants.isRegister, true)) {
            RegisterService.unregisterMine(MainActivity.this);
            sp.edit().putBoolean(Constants.isRegister, false).apply();
            pm2_5View.setCreditValue(0);
            pm1_0View.setCreditValue(0);
            Toast.makeText(MainActivity.this, "注销成功！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "已经注销过啦！", Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PMDataMessageEvent event) {
        if (pm1_0View != null && pm2_5View != null) {
            Log.d(TAG, event.getPm1_0() + " , " + event.getPm2_5());
            pm1_0View.setCreditValue(event.getPm1_0());
            pm2_5View.setCreditValue(event.getPm2_5());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (sp.getBoolean(Constants.isRegister, true)) {
            RegisterService.unregisterMine(MainActivity.this);
            sp.edit().putBoolean(Constants.isRegister, false).apply();
        }
    }
}

