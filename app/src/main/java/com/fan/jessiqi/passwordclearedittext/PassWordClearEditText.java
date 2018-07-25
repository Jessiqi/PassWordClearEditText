package com.fan.jessiqi.passwordclearedittext;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by jessiqifan
 * 2018/7/25 0025 下午 2:18
 */
public class PassWordClearEditText extends AppCompatEditText {
    //按钮资源
    private final int CLEAR = R.mipmap.ic_cancel;
    //按钮左右间隔,单位DP
    private final int INTERVAL = 20;
    //清除按钮宽度,单位DP
    private final int WIDTH_OF_CLEAR = 18;
    //动画时长
    private final int ANIMATOR_TIME = 200;
    //图标间隔
    private int padding;
    //清除按钮宽度记录
    private int mWidth_clear;
    //清除按钮的bitmap
    private Bitmap mBitmap_clear;
    //清除按钮出现动画
    private ValueAnimator mAnimator_visible;
    //消失动画
    private ValueAnimator mAnimator_gone;
    //是否显示的记录
    private boolean isVisible = false;
    //右边添加其他按钮时使用
    private int mRight = 0;

    private int mWidthPassWorld;
    private TextInputLayout mTextInputLayout;

    public PassWordClearEditText(final Context context) {
        super(context);
        init(context);
    }

    public PassWordClearEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PassWordClearEditText(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mBitmap_clear = createBitmap(CLEAR, context);
        mTextInputLayout = new TextInputLayout(context);

        padding = dp2px(INTERVAL);
        mWidth_clear = dp2px(WIDTH_OF_CLEAR);
        //获取TextInputLayout PasswordVisibilityToggleDrawable的原始尺寸
        mWidthPassWorld = mTextInputLayout.getPasswordVisibilityToggleDrawable().getIntrinsicWidth();
        mAnimator_gone = ValueAnimator.ofFloat(1f, 0f).setDuration(ANIMATOR_TIME);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));//抗锯齿
        if (isVisible) {
            drawClear(canvas);
            invalidate();
        }

        if (mAnimator_gone.isRunning()) {
            float scale = (float) mAnimator_gone.getAnimatedValue();
            drawClearGone(scale, canvas);
            invalidate();
        }
    }

    /**
     * 绘制清除按钮出现的图案
     *
     * @param canvas
     */
    protected void drawClear(Canvas canvas) {
        int right = getWidth()  - mWidthPassWorld - padding - mRight;
        int left = right - mWidth_clear;
        int top = (getHeight() - mWidth_clear) / 2;
        int bottom = top + mWidth_clear;
        Rect rect = new Rect(left, top, right, bottom);
        canvas.drawBitmap(mBitmap_clear, null, rect, null);
    }

    /**
     * 绘制清除按钮消失的图案
     *
     * @param scale  缩放比例
     * @param canvas
     */
    protected void drawClearGone(float scale, Canvas canvas) {
        int right = (int) (getWidth() + getScrollX() - padding - mRight - mWidthPassWorld - mWidth_clear * (1f - scale) / 2f);
        int left = (int) (right - mWidth_clear * (scale + (1f - scale) / 2f));
        int top = (int) ((getHeight() - mWidth_clear * scale) / 2);
        int bottom = (int) (top + mWidth_clear * scale);
        Rect rect = new Rect(left, top, right, bottom);
        canvas.drawBitmap(mBitmap_clear, null, rect, null);
    }

    /**
     * 开始清除按钮的消失动画
     */
    private void startGoneAnimator() {
        endAnaimator();
        mAnimator_gone.start();
        invalidate();
    }

    /**
     * 结束动画
     */
    private void endAnaimator() {
        mAnimator_gone.end();
    }

    /**
     * Edittext内容变化的监听
     *
     * @param text
     * @param start
     * @param lengthBefore
     * @param lengthAfter
     */
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        if (text.length() > 0) {
            if (!isVisible) {
                isVisible = true;
            }
        } else {
            if (isVisible) {
                isVisible = false;
                startGoneAnimator();
            }
        }
    }

    /**
     * 触控执行的监听
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            boolean touchable = (getWidth() - mWidth_clear - mWidthPassWorld - padding < event.getX()) && (event.getX() < getWidth() - padding - mWidthPassWorld);
            if (touchable) {
                setError(null);
                this.setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 给图标染上当前提示文本的颜色并且转出Bitmap
     *
     * @param resources
     * @param context
     * @return
     */
    public Bitmap createBitmap(int resources, Context context) {
        final Drawable drawable = ContextCompat.getDrawable(context, resources);
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, getCurrentHintTextColor());
        return drawableToBitamp(wrappedDrawable);
    }

    /**
     * drawable转换成bitmap
     *
     * @param drawable
     * @return
     */
    private Bitmap drawableToBitamp(Drawable drawable) {
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public int dp2px(float dipValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
