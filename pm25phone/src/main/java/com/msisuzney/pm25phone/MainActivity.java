package com.msisuzney.pm25phone;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init() {
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
            Toast.makeText(MainActivity.this, "注销成功！", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "已经注销过啦！", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sp.getBoolean(Constants.isRegister, true)) {
            RegisterService.unregisterMine(MainActivity.this);
            sp.edit().putBoolean(Constants.isRegister, false).apply();
        }
    }
}

