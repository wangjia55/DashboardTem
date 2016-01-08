package com.wqdata.dashboard.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


/**
 * Created by wangjia on 2016/1/7.
 */
public class MeasureDashboardView extends View {
    /**
     * 不同温度区间对应的颜色
     */
    private static final int COLOR_TEM_LOW = Color.parseColor("#FF64e5e3");
    private static final int COLOR_TEM_NORMAL = Color.parseColor("#ff78d4f8");
    private static final int COLOR_TEM_LOW_HOT = Color.parseColor("#ffc3c3e4");
    private static final int COLOR_TEM_MID_HOT = Color.parseColor("#fff755e2");
    private static final int COLOR_TEM_HOT = Color.parseColor("#ffec8fbf");
    private static final int COLOR_TEM_HIGH_HOT = Color.parseColor("#fffe8e8d");
    /**
     * 进度条阴影背景的颜色
     */
    private static final int COLOR_SHADOW = Color.parseColor("#E4D4CFD1");
    //屏幕中心文字的大小和颜色
    private int COLOR_TEXT_DATA = Color.parseColor("#333333");
    private int COLOR_TEXT_TIPS = Color.parseColor("#f46f33");

    /**
     * 仪表盘的背景
     */
    private Bitmap mBitmapDashboard;
    /**
     * 仪表盘的尺寸
     */
    private int mBitDashWidth;
    private int mBitDashHeight;

    /**
     * 进度条的画笔
     */
    private Paint mPaintProgress;

    /**
     * 进度条的阴影的画笔
     */
    private Paint mPaintShadow;
    private float mProgressStroke = 25;

    /**
     * 环形进度条的路径
     */
    private Path mProgressDashPath;
    private Path mProgressShadowPath;

    /**
     * 圆盘的起始角度，和覆盖的角度
     */
    private static final float START_ANGLE = 57;
    private static final float SWEEP_ANGLE = 246;

    /**
     * 进度条的边距
     */
    private int PROGRESS_PADDING = dpToPx(23);

    /**
     * 中心点的坐标
     */
    private float mCenterX;
    private float mCenterY;

    /**
     * 体温数据的字体大小
     */
    private float TEXT_SIZE_DATA = spToPx(32);

    /**
     * 提示文字的字体大小
     */
    private float TEXT_SIZE_TIPS = spToPx(15);

    private String mTextTips = "当前体温";

    /**
     * 表盘中心显示的内容
     */
    private String mTextValue;

    /**
     * 具体对应的表盘数据
     */
    private float mValue = 0.0f;

    /**
     * 当前旋转的角度
     */
    private float mNewAngle = 0;

    /**
     * 上次位置的角度
     */
    private float mOldAngle = 0;

    /**
     * 角度的变化范围
     */
    private int mAngleRange = 3;

    /**
     * 表盘数据的最大最小值
     */
    private static final float MIN_VALUE = 35.4f;
    private static final float MAX_VALUE = 41.6f;

    private Handler mHandler = new Handler();

    private static final int DELAY = 80;

    public MeasureDashboardView(Context context) {
        this(context, null);
    }

    public MeasureDashboardView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public MeasureDashboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init() {
        mBitmapDashboard = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_dashboard);
        mBitDashWidth = mBitmapDashboard.getWidth();
        mBitDashHeight = mBitmapDashboard.getHeight();
        mCenterX = mBitDashWidth / 2;
        mCenterY = mBitDashHeight / 2;

        mValue = 0.0f;
        mTextValue = getResources().getString(R.string.measure_value, mValue);
        mNewAngle = START_ANGLE;
        mOldAngle = mNewAngle;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBitDashWidth, mBitDashHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        //canvas.drawColor(Color.GRAY);

        initProgressShadow();
        initProgress();
        //绘制圆盘
        canvas.save();
        canvas.rotate(90, mCenterX, mCenterY);
        canvas.drawPath(mProgressShadowPath, mPaintShadow);
        canvas.drawPath(mProgressDashPath, mPaintProgress);
        canvas.restore();
        //绘制仪表盘的图片
        drawDashBoard(canvas);

        //绘制具体的数据文字
        drawDataText(canvas);

        //绘制提示的文字
        drawTipsText(canvas);
    }

    /**
     * 绘制提示的文案
     */
    private void drawTipsText(Canvas canvas) {
        Rect rect = new Rect();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(TEXT_SIZE_TIPS);
        paint.setColor(COLOR_TEXT_TIPS);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(20);
        paint.getTextBounds(mTextTips, 0, mTextTips.length(), rect);
        //绘制控件中心的数据
        canvas.drawText(mTextTips, mCenterX - rect.width() / 2,
                mCenterY + mBitDashHeight / 6, paint);
    }

    /**
     * 绘制中心位置数据的文案
     */
    private void drawDataText(Canvas canvas) {
        Rect rect = new Rect();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(TEXT_SIZE_DATA);
        paint.setColor(COLOR_TEXT_DATA);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(20);
        paint.getTextBounds(mTextValue, 0, mTextValue.length(), rect);
        //绘制控件中心的数据
        canvas.drawText(mTextValue, mCenterX - rect.width() / 2,
                mCenterY, paint);
    }

    /**
     * 初始化进度条
     */
    private void initProgress() {
        mPaintProgress = new Paint();
        mPaintProgress.setColor(COLOR_TEM_LOW);
        mPaintProgress.setAntiAlias(true);
        mPaintProgress.setDither(true);
        mPaintProgress.setStrokeWidth(mProgressStroke);
        mPaintProgress.setStrokeJoin(Paint.Join.BEVEL);
        mPaintProgress.setStyle(Paint.Style.STROKE);
        mProgressDashPath = new Path();
        RectF rectF = new RectF(PROGRESS_PADDING,
                PROGRESS_PADDING,
                mBitDashWidth - PROGRESS_PADDING,
                mBitDashHeight - PROGRESS_PADDING);
        mProgressDashPath.addArc(rectF, START_ANGLE, mOldAngle - START_ANGLE);
        PathEffect effects = new DashPathEffect(new float[]{6, 7}, 2);
        PathEffect effect1 = new CornerPathEffect(5);
        ComposePathEffect composePathEffect = new ComposePathEffect(effects, effect1);
        mPaintProgress.setPathEffect(composePathEffect);
        Shader shader = new SweepGradient(mCenterX, mCenterY,
                new int[]{COLOR_TEM_LOW, COLOR_TEM_NORMAL, COLOR_TEM_LOW_HOT, COLOR_TEM_MID_HOT, COLOR_TEM_HOT, COLOR_TEM_HIGH_HOT},
                new float[]{0.26f, 0.31f, 0.39f, 0.52f, 0.62f, 0.86f});
        mPaintProgress.setShader(shader);
    }


    /**
     * 初始化进度条背景
     */
    private void initProgressShadow() {
        mPaintShadow = new Paint();
        mPaintShadow.setColor(COLOR_SHADOW);
        mPaintShadow.setAntiAlias(true);
        mPaintShadow.setDither(true);
        mPaintShadow.setStrokeWidth(mProgressStroke);
        mPaintShadow.setStrokeJoin(Paint.Join.BEVEL);
        mPaintShadow.setStyle(Paint.Style.STROKE);
        mProgressShadowPath = new Path();
        RectF rectF = new RectF(PROGRESS_PADDING,
                PROGRESS_PADDING,
                mBitDashWidth - PROGRESS_PADDING,
                mBitDashHeight - PROGRESS_PADDING);
        mProgressShadowPath.addArc(rectF, START_ANGLE, SWEEP_ANGLE);
        PathEffect effects = new DashPathEffect(new float[]{6, 7}, 1);
        PathEffect effect1 = new CornerPathEffect(5);
        ComposePathEffect composePathEffect = new ComposePathEffect(effects, effect1);
        mPaintShadow.setPathEffect(composePathEffect);
        Shader shader = new SweepGradient(mCenterX, mCenterY,
                new int[]{COLOR_SHADOW, COLOR_SHADOW},
                new float[]{0, 1});
        mPaintShadow.setShader(shader);
    }


    /**
     * 绘制仪表盘
     */
    private void drawDashBoard(Canvas canvas) {
        canvas.drawBitmap(mBitmapDashboard, 0, 0, null);
    }


    /**
     * 设置和传入体温数据
     */
    public void setValue(float value) {
        mValue = value;
        if (value <= 0.0f) {
            mValue = 0.0f;
        }
        if (value > MAX_VALUE) {
            mValue = MAX_VALUE;
        }
        mTextValue = getResources().getString(R.string.measure_value, mValue);
        mNewAngle = getAngleFromValue(mValue);
        mAngleRange = getAngleRange(mNewAngle, mOldAngle);
        mHandler.removeCallbacks(mRunnableCrease);
        mHandler.removeCallbacks(mRunnableReduce);
        invalidate();
        if (mNewAngle > mOldAngle) {
            mHandler.post(mRunnableCrease);
        } else if (mNewAngle < mOldAngle) {
            mHandler.post(mRunnableReduce);
        }
    }

    /**
     * 获取当前的体温数据
     */
    public float getValue() {
        return mValue;
    }


    private Runnable mRunnableCrease = new Runnable() {
        @Override
        public void run() {
            mOldAngle += mAngleRange;
            if (mOldAngle >= mNewAngle) {
                mOldAngle = mNewAngle;
                mHandler.removeCallbacks(mRunnableCrease);
            } else {
                mHandler.postDelayed(mRunnableCrease, DELAY);
            }
            invalidate();
        }
    };
    private Runnable mRunnableReduce = new Runnable() {
        @Override
        public void run() {
            mOldAngle -= mAngleRange;
            if (mOldAngle <= mNewAngle) {
                mOldAngle = mNewAngle;
                mHandler.removeCallbacks(mRunnableReduce);
            } else {
                mHandler.postDelayed(mRunnableReduce, DELAY);
            }
            invalidate();
        }
    };


    /**
     * 获取到角度变化的幅度，差距越大，幅度会提升
     */
    private int getAngleRange(float newAngle, float oldAngle) {
        int allRange = (int) Math.abs(newAngle - oldAngle);
        if (allRange > 200) {
            return 10;
        } else if (allRange > 100) {
            return 8;
        } else if (allRange > 50) {
            return 5;
        } else {
            return 3;
        }
    }


    /**
     * 根据传入的数据计算出对应的角度
     */
    private float getAngleFromValue(float value) {
        if (value <= MIN_VALUE) {
            return START_ANGLE;
        } else if (value >= MAX_VALUE) {
            return SWEEP_ANGLE;
        } else {
            //45的来源是因为整个圆盘被分成了9份，每份占的角度是40度
            return (START_ANGLE + (value - MIN_VALUE) * 40);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private int spToPx(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

}
