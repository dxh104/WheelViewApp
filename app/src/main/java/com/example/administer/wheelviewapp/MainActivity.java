package com.example.administer.wheelviewapp;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.administer.wheelviewapp.utils.DialogUtil;
import com.example.administer.wheelviewapp.widget.WheelView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements DialogUtil.OnTvFinishClickListener {

    private WheelView wheelView;
    private Button btnPopBornDateDialog;
    private List<String> numTitles;
    private Button btnPopSexDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initDialog();
    }

    private void initDialog() {
        DialogUtil.setOnTvFinishClickListener(this);
        final Dialog bornDateDialog = DialogUtil.createBornDateDialog(this,2000,2,11);//默认年月日
        final WheelView yearWheelView = bornDateDialog.findViewById(R.id.yearWheelView);
        final WheelView monthWheelView = bornDateDialog.findViewById(R.id.monthWheelView);
        final WheelView dayWheelView = bornDateDialog.findViewById(R.id.dayWheelView);
        btnPopBornDateDialog.setOnClickListener(new View.OnClickListener() {//弹出时如果需要选择
            @Override
            public void onClick(View v) {
                bornDateDialog.show();
                dayWheelView.setItemTitles(DialogUtil.createDays(DialogUtil.getDayOnMonth(2000, 2)));//创建日份数据，进行设置
                yearWheelView.smoothScrollToDataPostionWithoutCallBack(2000 - DialogUtil.miniYear);//滚动到指定年
                monthWheelView.smoothScrollToDataPostionWithoutCallBack(2 - DialogUtil.miniMonth);//滚动到指定月
                dayWheelView.smoothScrollToDataPostionWithoutCallBack(29- DialogUtil.miniDay);//滚动到指定日
            }
        });
        final Dialog sexDialog = DialogUtil.createSexDialog(this, "");
        final WheelView sexWheelView = sexDialog.findViewById(R.id.sexWheelView);
        btnPopSexDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sexDialog.show();
                //0 男 1女
                sexWheelView.smoothScrollToDataPostionWithoutCallBack(1);//滚动到指定性别
            }
        });
    }

    private void initView() {
        wheelView = (WheelView) findViewById(R.id.wheelView);
        btnPopBornDateDialog = (Button) findViewById(R.id.btn_popBornDateDialog);
        btnPopSexDialog = (Button) findViewById(R.id.btn_popSexDialog);

        numTitles = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            numTitles.add("" + i);
        }
        wheelView.setItemTitles(numTitles);
    }

    @Override
    public void onclick(int year, int month, int day) {
        int min = Math.min(day, DialogUtil.getDayOnMonth(year, month));//防止年月快速滑动，点击完成，回调出的日份溢出
        Toast.makeText(MainActivity.this, year + "年" + month + "月" + min + "日", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onclick(String sex) {
        Toast.makeText(MainActivity.this, sex, Toast.LENGTH_SHORT).show();

    }
}
