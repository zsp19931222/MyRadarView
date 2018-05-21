package gcxy.yhkj.com.myradarview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 5018/5/21 0021.
 */

public class MyRadarView extends View {
    //默认的主题颜色
    private int DEFAULT_COLOR = Color.parseColor("#91D7F4");

    // 圆圈和交叉线的颜色
    private int mCircleColor = DEFAULT_COLOR;
    //圆圈的数量 不能小于1
    private int mCircleNum = 3;
    //扫描的颜色 RadarView会对这个颜色做渐变透明处理
    private int mSweepColor = DEFAULT_COLOR;
    //水滴的数量 这里表示的是水滴最多能同时出现的数量。因为水滴是随机产生的，数量是不确定的
    private int mRaindropNum = 4;
    //扫描的转速，表示几秒转一圈
    private float mSpeed = 3.0f;
    //水滴显示和消失的速度
    private float mFlicker = 3.0f;

    private Paint mCirclePaint;// 圆的画笔
    private Paint mSweepPaint; //扫描效果的画笔
    private Paint mRaindropPaint;// 水滴的画笔

    private float mDegrees; //扫描时的扫描旋转角度。
    private boolean isScanning = true;//是否扫描

    private float centerX;//中心X坐标
    private float centerY;//中心Y坐标

    private float w;//屏幕宽
    private float h;//屏幕高

    private float maxR = 400;

    public MyRadarView(Context context) {
        super(context);
    }

    public MyRadarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        // 初始化画笔
        mCirclePaint = new Paint();
        mCirclePaint.setColor(mCircleColor);
        mCirclePaint.setStrokeWidth(1);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setAntiAlias(true);

        mRaindropPaint = new Paint();
        mRaindropPaint.setStyle(Paint.Style.FILL);
        mRaindropPaint.setAntiAlias(true);

        mSweepPaint = new Paint();
        mSweepPaint.setAntiAlias(true);

    }

    /**
     * 获取自定义属性值
     *
     * @param context
     * @param attrs
     */
    private void getAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.MyRadarView);
            mCircleColor = mTypedArray.getColor(R.styleable.MyRadarView_circleColor, DEFAULT_COLOR);
            mCircleNum = mTypedArray.getInt(R.styleable.MyRadarView_circleNum, mCircleNum);
            if (mCircleNum < 1) {
                mCircleNum = 3;
            }
            mSweepColor = mTypedArray.getColor(R.styleable.MyRadarView_sweepColor, DEFAULT_COLOR);
            mRaindropNum = mTypedArray.getInt(R.styleable.MyRadarView_raindropNum, mRaindropNum);
            mSpeed = mTypedArray.getFloat(R.styleable.MyRadarView_speed, mSpeed);
            if (mSpeed <= 0) {
                mSpeed = 3;
            }
            mFlicker = mTypedArray.getFloat(R.styleable.MyRadarView_flicker, mFlicker);
            if (mFlicker <= 0) {
                mFlicker = 3;
            }
            mTypedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //设置宽高,默认500dp
        int defaultSize = dp2px(getContext(), 500);
        setMeasuredDimension(measureWidth(widthMeasureSpec, defaultSize),
                measureHeight(heightMeasureSpec, defaultSize));
        maxR = measureWidth(widthMeasureSpec, defaultSize) / 2;
    }

    /**
     * 测量宽
     *
     * @param measureSpec
     * @param defaultSize
     * @return
     */
    private int measureWidth(int measureSpec, int defaultSize) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        result = Math.max(result, getSuggestedMinimumWidth());
        return result;
    }

    /**
     * 测量高
     *
     * @param measureSpec
     * @param defaultSize
     * @return
     */
    private int measureHeight(int measureSpec, int defaultSize) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else {
            result = defaultSize + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }
        }
        result = Math.max(result, getSuggestedMinimumHeight());
        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
        centerX = 0;
        centerY = 0;
        randomXY = new Random();
        points = new ArrayList<>();
        add();
    }

    private List<Point> points;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isScanning) {
            invalidate();
        }
        canvas.translate(w / 2, h / 2);
        onDrawCoordinateLine(canvas);
        onDrawSmallCircle(canvas);
        onDrawSector(canvas);
        onDrawCircle(canvas);
    }

    /**
     * 画四个圈
     */
    private void onDrawCircle(Canvas canvas) {
        mCirclePaint.setColor(Color.BLUE);
        int r;
        for (int i = 0; i < 4; i++) {
            r = (int) (maxR - maxR / 4 * i);
            canvas.drawCircle(0, 0, r, mCirclePaint);
        }
    }

    /**
     * 画坐标线
     */
    private void onDrawCoordinateLine(Canvas canvas) {
        canvas.drawLine(-maxR, centerY, maxR, centerY, mCirclePaint);
        canvas.drawLine(centerX, -maxR, centerX, maxR, mCirclePaint);
    }

    /**
     * 画渐变扇形
     */
    private void onDrawSector(Canvas canvas) {
        mDegrees = (mDegrees + (360 / mSpeed / 60)) % 360;
        //扇形的透明的渐变效果
        SweepGradient sweepGradient = new SweepGradient(centerX, centerY,
                new int[]{Color.TRANSPARENT, changeAlpha(mSweepColor, 0), changeAlpha(mSweepColor, 168),
                        changeAlpha(mSweepColor, 255), changeAlpha(mSweepColor, 255)
                }, new float[]{0.0f, 0.6f, 0.99f, 0.998f, 1f});
        mSweepPaint.setShader(sweepGradient);
        RectF rect = new RectF((centerX - maxR), centerY - maxR,
                centerX + maxR, centerY + maxR);//扇形区域
        canvas.rotate(-90 + mDegrees, centerX, centerY);
        canvas.drawArc(rect, 0, 360, true, mSweepPaint);
    }

    /**
     * 改变颜色的透明度
     *
     * @param color
     * @param alpha
     * @return
     */
    private static int changeAlpha(int color, int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    /**
     * dp转px
     */
    private static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    private int alpha = 255;
    Random randomXY;
    private float r;


    private void onDrawSmallCircle(Canvas canvas) {
        for (int i = 0; i < points.size(); i++) {
            alpha = points.get(i).getAlpha();
            r = points.get(i).getR();
            mRaindropPaint.setColor(changeAlpha(mSweepColor, points.get(i).getAlpha()));
            canvas.drawCircle(points.get(i).getX(), points.get(i).getY(), points.get(i).getR(), mRaindropPaint);
            r += 1.0f * randomXY.nextInt(100) / 60 / mFlicker;
            alpha -= 1.0f * 255 / 60 / mFlicker;
            points.get(i).setR(r);
            points.get(i).setAlpha(alpha);

        }
        if (alpha < 0) {
            points.removeAll(points);
            add();
        }
    }

    /**
     * 添加小实心圆
     * */
    private void add() {
        for (int i = 0; i < randomXY.nextInt(10); i++) {
            int x = (int) (-maxR + (int) (Math.random() * ((maxR - (-maxR)) + 1)));
            int y = (int) (-maxR + (int) (Math.random() * ((maxR - (-maxR)) + 1)));
            int absX = Math.abs(x);
            int absY = Math.abs(y);
            int distanceZ = (int) Math.sqrt(Math.pow(absX, 2) + Math.pow(absY, 2));
            if (distanceZ < maxR - 50) {
                points.add(new Point(x, y, 0, 255));
            }
        }
    }
}
