package com.example.administer.wheelviewapp.utils;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.administer.wheelviewapp.R;
import com.example.administer.wheelviewapp.widget.WheelView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by XHD on 2020/04/15
 */
public class DialogUtil {

    private static final List<String> yearList = new ArrayList<>();
    private static final List<String> monthList = new ArrayList<>();
    private static final List<String> dayList = new ArrayList<>();
    public static final int miniYear = 1900;//显示最低年份
    public static final int miniMonth = 1;//显示最低月份
    public static final int miniDay = 1;//显示最低日份


    public static Dialog createSexDialog(Context context, String sex) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_sex, null);//填充对话框
        TextView tvFinish = (TextView) view.findViewById(R.id.tv_finish);
        final WheelView sexWheelView = (WheelView) view.findViewById(R.id.sexWheelView);
        List<String> sexList = new ArrayList<>();
        sexList.add("男");
        sexList.add("女");
        sexWheelView.setItemTitles(sexList);
        if (TextUtils.equals(sex, "女"))
            sexWheelView.smoothScrollToDataPostionWithoutCallBack(1);
        else
            sexWheelView.smoothScrollToDataPostionWithoutCallBack(0);
        final Dialog dialog = new Dialog(context, R.style.BlackDialog);//设置样式
        dialog.setContentView(view);//对话框填充布局
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);//对话框进出动画
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        tvFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                onTvFinishClickListener.onclick(sexWheelView.getSelectTitle());
            }
        });
        return dialog;
    }

    public static Dialog createBornDateDialog(final Context context, int defaultYear, int defaultMonth, int defaultDay) {
        createYearMonthData();//创建年月数据
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_date, null);//填充对话框
        TextView tv_finish = (TextView) view.findViewById(R.id.tv_finish);
        final WheelView yearWheelView = (WheelView) view.findViewById(R.id.yearWheelView);
        final WheelView monthWheelView = (WheelView) view.findViewById(R.id.monthWheelView);
        final WheelView dayWheelView = (WheelView) view.findViewById(R.id.dayWheelView);
        yearWheelView.setItemTitles(yearList);//设置年份数据
        monthWheelView.setItemTitles(monthList);//设置月份数据
        dayWheelView.setItemTitles(createDays(getDayOnMonth(defaultYear, defaultMonth)));//创建日份数据，进行设置
        yearWheelView.smoothScrollToDataPostionWithoutCallBack(defaultYear - miniYear);//选中2000年
        monthWheelView.smoothScrollToDataPostionWithoutCallBack(defaultMonth - miniMonth);//选中1月
        dayWheelView.smoothScrollToDataPostionWithoutCallBack(defaultDay - miniDay);//选中1日

        final Dialog dialog = new Dialog(context, R.style.BlackDialog);//设置样式
        dialog.setContentView(view);//对话框填充布局
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);//对话框进出动画
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //年份选中回调
        yearWheelView.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void select(String title, List<String> titleList, int currentPosition) {
                int year = Integer.valueOf(yearWheelView.getSelectTitle().substring(0, yearWheelView.getSelectTitle().length() - 1));
                int month = Integer.valueOf(monthWheelView.getSelectTitle().substring(0, monthWheelView.getSelectTitle().length() - 1));
                int day = Integer.valueOf(dayWheelView.getSelectTitle().substring(0, dayWheelView.getSelectTitle().length() - 1));
                int dayOnMonth = getDayOnMonth(year, month);//获取月日份上限
                dayWheelView.setItemTitles(createDays(dayOnMonth));//重新写入日份数据
                dayWheelView.smoothScrollToDataPostionWithoutCallBack(Math.min(day, dayOnMonth) - miniDay);
            }
        });
        //月份选中回调
        monthWheelView.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void select(String title, List<String> titleList, int currentPosition) {
                int year = Integer.valueOf(yearWheelView.getSelectTitle().substring(0, yearWheelView.getSelectTitle().length() - 1));
                int month = Integer.valueOf(monthWheelView.getSelectTitle().substring(0, monthWheelView.getSelectTitle().length() - 1));
                int day = Integer.valueOf(dayWheelView.getSelectTitle().substring(0, dayWheelView.getSelectTitle().length() - 1));
                int dayOnMonth = getDayOnMonth(year, month);//获取月日份上限
                dayWheelView.setItemTitles(createDays(dayOnMonth));//重新写入日份数据
                dayWheelView.smoothScrollToDataPostionWithoutCallBack(Math.min(day, dayOnMonth) - miniDay);
            }
        });
        //完成点击事件
        tv_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                int year = Integer.valueOf(yearWheelView.getSelectTitle().substring(0, yearWheelView.getSelectTitle().length() - 1));
                int month = Integer.valueOf(monthWheelView.getSelectTitle().substring(0, monthWheelView.getSelectTitle().length() - 1));
                int day = Integer.valueOf(dayWheelView.getSelectTitle().substring(0, dayWheelView.getSelectTitle().length() - 1));
                onTvFinishClickListener.onclick(year, month, day);
            }
        });
        return dialog;
    }

    private static OnTvFinishClickListener onTvFinishClickListener = null;

    public static void setOnTvFinishClickListener(OnTvFinishClickListener onTvFinishClickListener) {
        DialogUtil.onTvFinishClickListener = onTvFinishClickListener;
    }


    public interface OnTvFinishClickListener {
        void onclick(int year, int month, int day);

        void onclick(String sex);
    }

    private static void createYearMonthData() {
        yearList.clear();
        monthList.clear();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);//当前年份
        for (int i = miniYear; i <= year; i++) {
            yearList.add(i + "年");
        }
        for (int i = miniMonth; i <= 12; i++) {
            monthList.add(i + "月");
        }
    }

    public static List<String> createDays(int days) {
        dayList.clear();
        for (int i = 1; i <= days; i++) {
            dayList.add(i + "日");
        }
        return dayList;
    }

    public static int getDayOnMonth(int year, int month) {//获取天数每个月多少天
        int days = 0;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12://7个大月
                days = 31;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                days = 30;//4个小月
                break;
            case 2://二月 闰月29天,平月28天
                if (year % 4 == 0 && year % 100 != 0 || year % 400 == 0)//闰年
                    days = 29;
                else
                    days = 28;
                break;
        }
        return days;
    }
}
