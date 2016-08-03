package com.mixiaoxiao.smoothcompoundbutton;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.animation.Interpolator;

public class SmoothMarkDrawerRadioButton extends SmoothMarkDrawer {

	private PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);// XOR);
	private Interpolator interpolator = new FastOutSlowInInterpolator();
	
	/** CheckBox绘图部分的padding（占实际size的百分比） **/
	private final float PERCENT_CHECK_PADDING = 0.188f;
	private final float PERCENT_CHECK_ON_RADIUS_INNER = 0.156f;
	private final float PERCENT_CHECK_ON_RADIUS_OUTER = 0.254f;// 0.3f / 2f;
			
	/** 切换on/off时候缩小动画的百分比 **/
	private final float PERCENT_SCALE = 0.84f;

	public SmoothMarkDrawerRadioButton(Context context,int colorOn, int colorOff) {
		super(context, colorOn, colorOff);
		mPaint.setStrokeJoin(Paint.Join.MITER);
	}

	@Override
	public void setBounds(RectF rectF) {
		super.setBounds(rectF);
//		mPaint.setStrokeWidth(mSize * PERCENT_HOOK_STROKE);

	}


	@Override
	protected void drawMark(Canvas canvas, final float fraction1, float left, float top, float size, View view) {
		// 测试mBounds边界位置
//				 mPaint.setStyle(Style.FILL);
//				 mPaint.setColor(0x440000ff);
//				 canvas.drawRect(mCheckRectF, mPaint);
				 
		final float fullRadius = size * (1f - PERCENT_CHECK_PADDING * 2f) / 2f;
		final float cx = mBounds.centerX();
		final float cy = mBounds.centerY();

//		final int viewHeight = view.getHeight();
		updateCheckPaintColorFilter(view);
		final int sc = canvas
				.saveLayer(mBounds, null, Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
						| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
						| Canvas.CLIP_TO_LAYER_SAVE_FLAG);
		final float fraction = interpolator.getInterpolation(fraction1);
		if (fraction == 0f) {
			mPaint.setColor(mColorOff);
			mPaint.setStyle(Style.FILL);
			canvas.drawCircle(cx, cy, fullRadius, mPaint);
			
			//清空掉中间一圈
			mPaint.setXfermode(xfermode);
			mPaint.setColorFilter(null);
			mPaint.setColor(Color.WHITE);
			canvas.drawCircle(cx, cy, size * PERCENT_CHECK_ON_RADIUS_OUTER, mPaint);
			mPaint.setXfermode(null);
		} else if (fraction == 1f) {
			mPaint.setStyle(Style.FILL);
			mPaint.setColor(mColorOn);
			canvas.drawCircle(cx, cy, fullRadius, mPaint);


			//清空掉中间一圈
			mPaint.setXfermode(xfermode);
			mPaint.setColorFilter(null);
			mPaint.setColor(Color.WHITE);
			canvas.drawCircle(cx, cy, size * PERCENT_CHECK_ON_RADIUS_OUTER, mPaint);
			mPaint.setXfermode(null);
			
			//再画中心的一个圆
			mPaint.setColor(mColorOn);
			updateCheckPaintColorFilter(view);
			canvas.drawCircle(cx, cy, size * PERCENT_CHECK_ON_RADIUS_INNER, mPaint);
			
		} else {

			final float HALF_FRACTION = 0.5f;
			mPaint.setColor(convertColorFraction(fraction, mColorOff, mColorOn));
			if (fraction <= HALF_FRACTION) {
				//动画过程->清空的部分逐渐变小
				final float offFraction = fraction / HALF_FRACTION;
				// 缩小画布
				final float scale = 1f - (1f - PERCENT_SCALE) * offFraction;
				canvas.scale(scale, scale, cx, cy);
				// 画背景
				mPaint.setStyle(Style.FILL);
				canvas.drawCircle(cx, cy, fullRadius, mPaint);
				//清空掉中间一圈
				mPaint.setXfermode(xfermode);
				mPaint.setColorFilter(null);
				mPaint.setColor(Color.WHITE);
				canvas.drawCircle(cx, cy, size * PERCENT_CHECK_ON_RADIUS_OUTER * (1f - offFraction), mPaint);
				mPaint.setXfermode(null);
				
			} else {
				//动画过程->清空是完全清空的不用变，再画中间的圆形从最大到正常
				final float onFraction = (fraction - HALF_FRACTION) / (1f - HALF_FRACTION);
				final float scale = PERCENT_SCALE + (1f - PERCENT_SCALE) * onFraction; 
				canvas.scale(scale, scale, cx, cy);
				mPaint.setStyle(Style.FILL);
				canvas.drawCircle(cx, cy, fullRadius, mPaint);
//				//清空掉中间一圈
				mPaint.setXfermode(xfermode);
				mPaint.setColorFilter(null);
				mPaint.setColor(Color.WHITE);
				canvas.drawCircle(cx, cy, size * PERCENT_CHECK_ON_RADIUS_OUTER , mPaint);
				mPaint.setXfermode(null);
				//再画中间的圆形，从最大变正常
				mPaint.setColor(convertColorFraction(fraction, mColorOff, mColorOn));
				updateCheckPaintColorFilter(view);
				final float centerRadius = size * (PERCENT_CHECK_ON_RADIUS_OUTER - 
						(PERCENT_CHECK_ON_RADIUS_OUTER - PERCENT_CHECK_ON_RADIUS_INNER) * onFraction);
				canvas.drawCircle(cx, cy, centerRadius , mPaint);
				
			}

		}
		if (!view.isInEditMode()) {
			canvas.restoreToCount(sc);// eclipse的预览界面这里会报错
		}

	}

}
