package com.archer.taskstatusindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.Date;

/**
 * 任务状态指示器
 */
public class TSIndicator extends View implements Runnable {

    Context context;

    /**
     * 步骤上面的提示文字
     */
    private String[] stepStrArray;

    /**
     * 圆环的个数
     */
    private int stepCount;

    /**
     * 当前是第几步
     */
    private int currentStep;

    /**
     * 任务倒计时
     */
    private String countDownStr;

    /**
     * 任务提醒
     */
    private String taskTipStr;

    /**
     * 字体颜色
     */
    private int completeTextColor;
    /**
     * 未完成步骤的字体颜色
     */
    private int pendingTextColor;

    /**
     * 已完成的圆环颜色
     */
    private int completeCircleColor;
    /**
     * 未完成的圆环颜色
     */
    private int pendingCircleColor;

    /**
     * 倒计时Color
     */
    private int countDownColor = Color.WHITE;

    /**
     * 圆圈半径
     */
    private int circleRadius;

    /**
     * 字体的大小
     */
    private float textSize = 12;

    /**
     * 倒计时的大小
     */
    private float countDownSize = 24;

    /**
     * 普通文字的画笔
     */
    private TextPaint mTextPaint;
    private Paint.FontMetrics fontMetrics;

    /**
     * 倒计时的画笔
     */
    private TextPaint mCountDownTextPaint;
    private Paint.FontMetrics countDownFontMetrics;

    /**
     * 画圆圈的画笔
     */
    private Paint mPaint;

    private float mLineLength;

    /**
     * 圆环和顶部提示的距离
     */
    private float circlePadding = 30;
    /**
     * 倒计时和圆环之间的距离
     */
    private float countDownStrPadding = 30;
    /**
     * 任务提示与倒计时之间的距离
     */
    private float tipStrPadding = 30;

    private long mHour, mMin, mSecond;// 天，小:，:钟，
    private float countDownY;
    private boolean run = false; // 是否启动了

    public TSIndicator(Context context) {
        super(context);
        this.context = context;
        init(null, 0);
    }

    public TSIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs, 0);
        circleRadius = ScreenUtils.dip2px(context, 10);
    }

    public TSIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(attrs, defStyle);
        circleRadius = ScreenUtils.dip2px(context, 6);
    }

    private void init(AttributeSet attrs, int defStyle) {

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TSIndicator, defStyle, 0);
        completeTextColor = a.getColor(
                R.styleable.TSIndicator_completeTextColor,
                Color.WHITE);
        pendingTextColor = a.getColor(
                R.styleable.TSIndicator_pendingTextColor,
                getResources().getColor(R.color.t4_color));
        completeCircleColor = a.getColor(
                R.styleable.TSIndicator_completeCircleColor,
                getResources().getColor(R.color.p1_color));
        pendingCircleColor = a.getColor(
                R.styleable.TSIndicator_pendingCircleColor,
                getResources().getColor(R.color.t3_color));
        countDownColor = a.getColor(
                R.styleable.TSIndicator_countDownColor,
                Color.WHITE);
        countDownSize = a.getDimensionPixelSize(R.styleable.TSIndicator_countDownSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, countDownSize, getResources().getDisplayMetrics()));

//        countDownSize = a.getDimension(
//                R.styleable.TSIndicator_countDownSize,
//                ScreenUtils.dip2px(context, countDownSize));

        textSize = a.getDimensionPixelSize(R.styleable.TSIndicator_textSize, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, textSize, getResources().getDisplayMetrics()));

//        textSize = a.getDimension(
//                R.styleable.TSIndicator_textSize,
//                ScreenUtils.dip2px(context, textSize));

        circlePadding = a.getDimensionPixelSize(R.styleable.TSIndicator_circlePadding, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, circlePadding, getResources().getDisplayMetrics()));

//        circlePadding = a.getDimension(R.styleable.TSIndicator_circlePadding, circlePadding);

        countDownStrPadding = a.getDimensionPixelSize(R.styleable.TSIndicator_countDownStrPadding, (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, countDownStrPadding, getResources().getDisplayMetrics()));

//        countDownStrPadding = a.getDimension(R.styleable.TSIndicator_countDownStrPadding, countDownStrPadding);

        tipStrPadding = a.getDimensionPixelSize(R.styleable.TSIndicator_tipStrPadding, (int) TypedValue.applyDimension(

                TypedValue.COMPLEX_UNIT_DIP, tipStrPadding, getResources().getDisplayMetrics()));
//        tipStrPadding = a.getDimension(R.styleable.TSIndicator_tipStrPadding, tipStrPadding);

        a.recycle();

        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setTextSize(textSize);
        fontMetrics = mTextPaint.getFontMetrics();

        mCountDownTextPaint = new TextPaint();
        mCountDownTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mCountDownTextPaint.setTextAlign(Paint.Align.LEFT);
        mCountDownTextPaint.setColor(countDownColor);
        mCountDownTextPaint.setTextSize(countDownSize);
        countDownFontMetrics = mCountDownTextPaint.getFontMetrics();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);
        mPaint.setStyle(Paint.Style.FILL);



    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mWidth, mHeight;
        /**
         * 设置宽度
         */
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            mWidth = specSize;
        } else {
            mWidth = ScreenUtils.getScreenWidth(context);
        }


        /***
         * 设置高度
         */

        specMode = MeasureSpec.getMode(heightMeasureSpec);
        specSize = MeasureSpec.getSize(heightMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            mHeight = specSize;
        } else {
            mHeight = (int) (circlePadding + countDownStrPadding + tipStrPadding
                    + circleRadius * 2
                    + 2 * (- countDownFontMetrics.top - countDownFontMetrics.bottom)
                    + 4 * (- fontMetrics.top - fontMetrics.bottom) + getPaddingTop() + getPaddingBottom());
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);



        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        int contentHeight = getHeight() - paddingTop - paddingBottom;
        // 每个圆圈的起始X
        float circleTotalX = paddingLeft + circleRadius;
        // 圆圈之间连接线的长度
        mLineLength = (contentWidth - ((circleRadius * 2) * stepCount)) / (stepCount - 1);

        float circleCenterY = 0;

        for(int i = 0; i < stepCount; i++) {
            if(!(currentStep < i)) {
                mPaint.setColor(completeCircleColor);
                mTextPaint.setColor(completeTextColor);
            } else {
                mPaint.setColor(pendingCircleColor);
                mTextPaint.setColor(pendingTextColor);
            }
            float mTextWidth = mTextPaint.measureText(stepStrArray[i]);
            float mTextHeight = fontMetrics.bottom;

            // 文字和圆圈居中显示
            float textX = (circleTotalX - (mTextWidth) / 2);

            // 如果超出屏幕，则取最大值
            float extraX = getWidth() - textX - mTextWidth;
            if(extraX < 0) {
                textX += extraX;
            }
            canvas.drawText(stepStrArray[i], textX, paddingTop + getX() - (fontMetrics.top + fontMetrics.bottom) + fontMetrics.descent, mTextPaint);

            // 圆心Y
            circleCenterY = paddingTop + circleRadius - (fontMetrics.top + fontMetrics.bottom) + fontMetrics.descent + circlePadding;

            canvas.drawCircle(circleTotalX, circleCenterY, circleRadius, mPaint);

            // 圆圈中间的index
            String indexStr = String.valueOf(i + 1);
            float mTextWidth1 = mTextPaint.measureText(indexStr);
            canvas.drawText(indexStr, circleTotalX - mTextWidth1 / 2, circleCenterY + mTextHeight / 2 + fontMetrics.descent, mTextPaint);

            circleTotalX += mLineLength + circleRadius * 2;

        }

        // 每个圆圈的起始Y
        float lineTotalX = paddingLeft + circleRadius * 2;
        for(int j = 0; j < stepCount - 1; j++) {
            if(!(currentStep < j + 1)) {
                mPaint.setColor(completeCircleColor);
            } else {
                mPaint.setColor(pendingCircleColor);
            }
            canvas.drawRect(lineTotalX - 1, circleCenterY - 5, lineTotalX + mLineLength + 1, circleCenterY + 5, mPaint);
            lineTotalX += mLineLength + circleRadius * 2;
        }

        circleCenterY = circleCenterY + circleRadius - countDownFontMetrics.top - countDownFontMetrics.bottom + countDownStrPadding;

        float tempX = (getWidth() - mCountDownTextPaint.measureText(countDownStr)) / 2;
        canvas.drawText(countDownStr, tempX, circleCenterY, mCountDownTextPaint); // 小时

        float mTextHeight = countDownFontMetrics.bottom;
        circleCenterY = circleCenterY + mTextHeight + countDownFontMetrics.descent + countDownFontMetrics.bottom + tipStrPadding;
        canvas.drawText(taskTipStr, (getWidth() - mTextPaint.measureText(taskTipStr)) / 2, circleCenterY, mTextPaint);

    }

    public String[] getStepStrArray() {
        return stepStrArray;
    }

    public void setStepStrArray(String[] stepStrArray) {
        this.stepStrArray = stepStrArray;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public String getCountDownStr() {
        return countDownStr;
    }

    public void setCountDownStr(long timeGap) {
        long time = 0;
        try {
            Date d1 = new Date();
            long diff = timeGap - d1.getTime();
            time = diff / 1000;
        } catch (Exception e) {
            e.printStackTrace();
        }

        long[] timeLong = new long[]{0, 0, 0};

        long hour;
        long minute;
        long second;
        if (time > 0) {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeLong[1] = minute;
                timeLong[2] = second;

            } else {
                hour = minute / 60;

                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeLong[0] = hour;
                timeLong[1] = minute;
                timeLong[2] = second;
            }
        }

        mHour = timeLong[0];
        mMin = timeLong[1];
        mSecond = timeLong[2];
    }

    public String getTaskTipStr() {
        return taskTipStr;
    }

    public void setTaskTipStr(String taskTipStr) {
        this.taskTipStr = taskTipStr;
    }

    public int getCompleteTextColor() {
        return completeTextColor;
    }

    public void setCompleteTextColor(int completeTextColor) {
        this.completeTextColor = completeTextColor;
    }

    public int getCompleteCircleColor() {
        return completeCircleColor;
    }

    public void setCompleteCircleColor(int completeCircleColor) {
        this.completeCircleColor = completeCircleColor;
    }

    public int getCountDownColor() {
        return countDownColor;
    }

    public void setCountDownColor(int countDownColor) {
        this.countDownColor = countDownColor;
    }

    public int getCircleRadius() {
        return circleRadius;
    }

    public void setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
    }

    public float getCountDownSize() {
        return countDownSize;
    }

    public void setCountDownSize(float countDownSize) {
        this.countDownSize = countDownSize;
    }

    private void updateCountDownStr(String countDownStr) {
        this.countDownStr = countDownStr;
        postInvalidate();
    }


    /**
     * 倒计:计算
     */
    private void computeTime() {
        mSecond--;
        if (mSecond < 0) {
            mMin--;
            mSecond = 59;
            if (mMin < 0) {
                mHour--;
                if (mHour < 0) {
                    mMin = 0;
                } else {
                    mMin = 59;
                }
            }
        }

    }

    public boolean isRun() {
        return run;
    }

    public void beginRun() {
        this.run = true;
        run();
    }

    public void stopRun() {
        this.run = false;
    }

    @Override
    public void run() {
        // 标示已经启动
        if (run && !(mHour <= 0 && mSecond <= 0 && mMin <= 0)) {
            computeTime();
            String hourStr = mHour + "";
            String minStr = mMin + "";
            String secondStr = mSecond + "";
            if (mHour <= 0) {
                hourStr = "00";
            } else if (mHour > 0 && mHour < 10) {
                hourStr = "0" + mHour;
            }
            if (mMin <= 0) {
                minStr = "00";
            } else if (mMin > 0 && mMin < 10) {
                minStr = "0" + mMin;
            }
            if (mSecond <= 0) {
                secondStr = "00";
            } else if (mSecond > 0 && mSecond < 10) {
                secondStr = "0" + mSecond;
            }
            updateCountDownStr(hourStr + ":" + minStr + ":" + secondStr);
        } else {
            stopRun();
            String hourStr = mHour + "";
            String minStr = mMin + "";
            String secondStr = mSecond + "";
            if (mHour <= 0) {
                hourStr = "00";
            } else if (mHour > 0 && mHour < 10) {
                hourStr = "0" + mHour;
            }
            if (mMin <= 0) {
                minStr = "00";
            } else if (mMin > 0 && mMin < 10) {
                minStr = "0" + mMin;
            }
            if (mSecond <= 0) {
                secondStr = "00";
            } else if (mSecond > 0 && mSecond < 10) {
                secondStr = "0" + mSecond;
            }
            updateCountDownStr(hourStr + ":" + minStr + ":" + secondStr);
            removeCallbacks(this);
        }
        postDelayed(this, 1000);
    }
}
