package com.archer.taskstatusindicator;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.util.Date;

public class MainActivity extends FragmentActivity {

    TSIndicator tsIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tsIndicator = (TSIndicator) findViewById(R.id.ts);
        tsIndicator.setTaskTipStr("哈哈哈哈");
        tsIndicator.setCountDownStr(new Date().getTime() + 1000 * 60 * 150);
        tsIndicator.setStepStrArray(new String[] {
                "发任务", "选择执行人付款", "任务执行中", "任务完成"
        });
        tsIndicator.setCurrentStep(2);
        tsIndicator.setStepCount(4);
        tsIndicator.beginRun();

    }
}
