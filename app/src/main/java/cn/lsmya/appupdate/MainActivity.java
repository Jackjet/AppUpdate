package cn.lsmya.appupdate;

import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import cn.lsmya.apkupdate.VersionUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE},
                0);
        findViewById(R.id.but1).setOnClickListener(this);
        findViewById(R.id.but2).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.but1:
                break;
            case R.id.but2:
                String url = "http://app-global.pgyer.com/d16f9440fd1793c119d9af454442dad9.apk?attname=%E5%8D%8F%E4%BD%9C%E5%B9%B3%E9%9D%A2_1.1.5.apk&sign=ed63eee34b68ae6c64341dfdae2cd905&t=5b6d4531";
                VersionUtils.with(this).downApk(url,"apk.apk");
                break;
        }
    }
}
