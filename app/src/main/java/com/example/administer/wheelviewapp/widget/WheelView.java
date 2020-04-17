package com.example.administer.wheelviewapp.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.example.administer.wheelviewapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XHD on 2020/04/09
 */
public class WheelView extends NestedScrollView implements View.OnClickListener {
    private int itemHeight;//每个条目高度
    private int itemVisibleCount;//显示的条目数量
    private int titleTextSize;//标题文本字体大小
    private int titleTextColor;//标题文本字体颜色
    private int wheelViewBackgroundColor;//WheelView背景颜色
    private int wheelViewBackgroundLineColor;//WheelView线的颜色
    private int wheelViewBackgroundLineBorderWidth;//WheelView线的宽度
    private float minScaleBias;//可见最小缩放比例
    private int offset;//头部和尾部偏移的空白数目
    private Context mContext;
    private LinearLayout mLinearLayout;
    private List<String> itemTitles;//条目标题名称

    public WheelView(@NonNull Context context) {
        this(context, null);
        mContext = context;
    }

    public WheelView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
    }

    public WheelView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttributeSet(attrs);//初始化配置
        init();
    }

    private void initAttributeSet(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.WheelView);
        itemHeight = typedArray.getDimensionPixelSize(R.styleable.WheelView_itemHeight, 100);
        itemVisibleCount = typedArray.getInteger(R.styleable.WheelView_itemVisibleCount, 5);
        if (itemVisibleCount % 2 == 0)
            itemVisibleCount++;
        titleTextSize = typedArray.getDimensionPixelSize(R.styleable.WheelView_titleTextSize, 20);
        titleTextColor = typedArray.getColor(R.styleable.WheelView_titleTextColor, Color.parseColor("#666066"));
        wheelViewBackgroundColor = typedArray.getColor(R.styleable.WheelView_wheelViewBackgroundColor, Color.parseColor("#6ffff0"));
        wheelViewBackgroundLineColor = typedArray.getColor(R.styleable.WheelView_wheelViewBackgroundLineColor, Color.parseColor("#83cde6"));
        wheelViewBackgroundLineBorderWidth = typedArray.getDimensionPixelSize(R.styleable.WheelView_wheelViewBackgroundLineBorderWidth, 2);
        minScaleBias = typedArray.getFloat(R.styleable.WheelView_minScaleBias, 0.6f);
        offset = itemVisibleCount / 2;
        typedArray.recycle();
    }


    private Runnable scrollerTask;
    private int currentY;
    private int lastY;

    private void init() {
        setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        mLinearLayout = new LinearLayout(mContext);
        addView(mLinearLayout);//添加容器
        mLinearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setGravity(Gravity.CENTER);

        scrollerTask = new Runnable() {
            @Override
            public void run() {
                currentY = getScrollY();
                if (currentY - lastY == 0) {//-----stop
                    if (currentY % itemHeight > itemHeight / 2) {
                        currentY = currentY + (itemHeight - currentY % itemHeight);//补距离
                        WheelView.this.post(new Runnable() {
                            @Override
                            public void run() {
                                WheelView.this.smoothScrollTo(0, currentY);

                            }
                        });
                    } else if (currentY % itemHeight > 0) {
                        currentY = currentY - currentY % itemHeight;//退距离
                        WheelView.this.post(new Runnable() {
                            @Override
                            public void run() {
                                WheelView.this.smoothScrollTo(0, currentY);
                            }
                        });
                    }
                    onSelectListener.select(getSelectTitle(), itemTitles, getCurrentPosition());

                } else {
                    lastY = getScrollY();
                    WheelView.this.post(scrollerTask);//递归
                }
            }
        };
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, itemHeight * itemVisibleCount);//重写高度

    }


    private int onDrawCount = 0;
    private Paint paint;
    private int startX, startY, endX, endY;

    //设置带线的背景，更新tv字体大小透明度
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (onDrawCount == 0) {
            onDrawCount++;
            paint = new Paint();
            paint.setColor(wheelViewBackgroundLineColor);
            paint.setStrokeWidth(wheelViewBackgroundLineBorderWidth);
            startX = 0;
            startY = itemVisibleCount / 2 * itemHeight;
            endX = getWidth();
            endY = itemVisibleCount / 2 * itemHeight;
            Drawable drawable = new Drawable() {
                @Override
                public void draw(@NonNull Canvas canvas) {
                    canvas.drawColor(wheelViewBackgroundColor);
                    canvas.drawLine(startX, startY, endX, endY, paint);
                    canvas.drawLine(startX, startY + itemHeight, endX, endY + itemHeight, paint);
                }

                @Override
                public void setAlpha(int alpha) {

                }

                @Override
                public void setColorFilter(@Nullable ColorFilter colorFilter) {

                }

                @SuppressLint("WrongConstant")
                @Override
                public int getOpacity() {
                    return 0;
                }
            };
            setBackgroundDrawable(drawable);
        }
        refreshing();//更新tv字体大小透明度

    }

    private int cacheSize = 1;//显示范围外缩放数目

    //更新tv字体大小透明度
    private void refreshing() {
        int visibleTvTitleFirstPostion = getScrollY() / itemHeight;//顶部第一个可见的TvTitle下标
        if (getScrollY() % itemHeight > itemHeight / 2) {
            visibleTvTitleFirstPostion++;//过半，顶部坐标则下移一位
        }
        visibleTvTitleFirstPostion = visibleTvTitleFirstPostion - cacheSize;//上下缓存数目
        for (int i = 0; i < itemVisibleCount + cacheSize * 2; i++) {
            if (visibleTvTitleFirstPostion < 0 || visibleTvTitleFirstPostion > itemTitles.size() - 1) {
                visibleTvTitleFirstPostion++;
                continue;
            }
            int tvTitleTop = mLinearLayout.getChildAt(visibleTvTitleFirstPostion).getTop();
            double tvDistance = Math.abs(tvTitleTop - getCurrentCenterY());//每个tv距中心的的距离
            double centerTop = itemHeight * (itemVisibleCount / 2);//中心区域顶部到显示区域的top
            double scaleBias = minScaleBias + (1 - tvDistance / centerTop) * (1 - minScaleBias);

            if (scaleBias < minScaleBias) {
                scaleBias = minScaleBias;
            }
            getTvTitle(visibleTvTitleFirstPostion).setScaleX((float) scaleBias);//设置缩放比例
            getTvTitle(visibleTvTitleFirstPostion).setScaleY((float) scaleBias);//设置缩放比例
            getTvTitle(visibleTvTitleFirstPostion).setTextColor(titleTextColor);
            ColorStateList textColors = getTvTitle(visibleTvTitleFirstPostion).getTextColors();
            ColorStateList alpha = textColors.withAlpha((int) (255 * scaleBias));
            getTvTitle(visibleTvTitleFirstPostion).setTextColor(alpha);//设置透明度
            visibleTvTitleFirstPostion++;
        }
    }

    private long delayMillis = 50;//延迟时间

    //开启滑动任务
    private void startScrollerTask() {
        lastY = getScrollY();
        this.postDelayed(scrollerTask, delayMillis);
    }


    /**
     * 滑动到指定数据(不含offset)下标的位置
     *
     * @param position 数据下标
     */
    private void smoothScrollToDataPostion(final int position) {
        this.post(new Runnable() {
            @Override
            public void run() {
                currentY = position  * itemHeight;
                WheelView.this.smoothScrollTo(0, currentY);//滚动距离
                onSelectListener.select(getSelectTitle(), itemTitles, getCurrentPosition());
            }
        });
    }

    /**
     * 滑动到指定数据(不含offset)下标的位置
     *
     * @param position 数据下标
     */
    public void smoothScrollToDataPostionWithoutCallBack(final int position) {
        currentY = position * itemHeight;
        this.post(new Runnable() {
            @Override
            public void run() {
                WheelView.this.scrollTo(0, currentY);//滚动距离---直接定位
                WheelView.this.smoothScrollTo(0, currentY);//滚动距离---准确定位(防止scrollTo定位偏离)
//                onSelectListener.select(getSelectTitle(), itemTitles, getCurrentPosition());
            }
        });
    }

    private OnSelectListener onSelectListener = new OnSelectListener() {
        @Override
        public void select(String title, List<String> titleList, int currentPosition) {
            Log.i("---------------", "run:选中 " + title + " titleList.size=" + titleList.size() + " currentPosition=" + currentPosition);
        }
    };


    //重写onSelectListener
    public void setOnSelectListener(OnSelectListener onSelectListener) {
        this.onSelectListener = onSelectListener;
    }


    public interface OnSelectListener {
        /**
         * @param title           当前选中标题
         * @param titleList       标题数据(含offset的空白标题)
         * @param currentPosition 当前选中下标(所有条目，含offset)
         */
        void select(String title, List<String> titleList, int currentPosition);//选中回调
    }


    private TextView createTvTitle(String title) {
        TextView tvTitle = new TextView(mContext);
        tvTitle.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleTextSize);
        tvTitle.setText(title);
        tvTitle.setIncludeFontPadding(false);//消除内边距
        tvTitle.setGravity(Gravity.CENTER);
        return tvTitle;
    }

    private TextView getTvTitle(int position) {
        TextView childAt = (TextView) ((LinearLayout) mLinearLayout.getChildAt(position)).getChildAt(0);
        return childAt;
    }

    public void setItemTitles(List<String> title) {
        if (itemTitles == null)
            this.itemTitles = new ArrayList<>();
        itemTitles.clear();
        itemTitles.addAll(title);
        // 前面和后面补全
        for (int i = 0; i < offset; i++) {
            itemTitles.add(0, "");
            itemTitles.add("");
        }//数据准备完毕
        if (mLinearLayout == null)
            return;
        mLinearLayout.removeAllViews();
        for (int i = 0; i < itemTitles.size(); i++) {//准备控件
            LinearLayout itemLayout = new LinearLayout(mContext);//容器
            itemLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
            itemLayout.setGravity(Gravity.CENTER);
            itemLayout.addView(createTvTitle(itemTitles.get(i)));
            itemLayout.setOnClickListener(this);
            mLinearLayout.addView(itemLayout);
        }
    }

    //获取选中标题
    public String getSelectTitle() {
        return itemTitles.get(getCurrentPosition());
    }

    //当前选中条目控件下标
    private int getCurrentPosition() {
        return getCurrentY() / itemHeight + offset;
    }


    private int getCurrentY() {
        return currentY;
    }

    //当前中心的y顶部坐标
    private int getCurrentCenterY() {
        return getScrollY() + itemHeight * (itemVisibleCount / 2);
    }

    //条目点击事件
    @Override
    public void onClick(View v) {
        int position = ((ViewGroup) v.getParent()).indexOfChild(v);//点击tv下标
        if (position < offset)//防止position越界
            position = offset;
        if (position > itemTitles.size() - 1 - offset)//防止position越界
            position = itemTitles.size() - 1 - offset;
        smoothScrollToDataPostion(position-offset);//滚动至中心
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            startScrollerTask();
        }
        return super.onTouchEvent(ev);
    }
}
