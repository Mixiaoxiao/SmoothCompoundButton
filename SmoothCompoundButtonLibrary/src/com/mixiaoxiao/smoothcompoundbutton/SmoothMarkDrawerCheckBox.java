package com.mixiaoxiao.smoothcompoundbutton;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.animation.Interpolator;

public class SmoothMarkDrawerCheckBox extends SmoothMarkDrawer {

	
	private PorterDuffXfermode xfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);// XOR);
	private Interpolator interpolator = new FastOutSlowInInterpolator();
	private RectF mCheckRectF;
	private RectF mCheckClearRectF;
	private Path mCheckHookPath;
	/** 对号的3个关键点坐标 **/
	private float[] HOOK_X = new float[] { 0.31481f, 0.43915f, 0.68783f };
	private float[] HOOK_Y = new float[] { 0.475f, 0.61111f, 0.35450f };
	
	// appcompat颜色secondary_text_default_material_light=8a000000，dark=b3ffffff
	/** CheckBox的边框的（占实际size的百分比）stroke **/
	private final float PERCENT_BORDER_STROKE = 0.064f;
	/** 对钩的边框的（占实际size的百分比）stroke **/
	private final float PERCENT_HOOK_STROKE = 0.068f;
	/** CheckBox的边框的圆角（占实际size的百分比） **/
	private final float PERCENT_BORDER_RADIUS = 0.05f;
	/** CheckBox绘图部分的padding（占实际size的百分比） **/
	private final float PERCENT_CHECK_PADDING = 0.22f;
	/** 切换on/off时候缩小动画的百分比 **/
	private final float PERCENT_SCALE = 0.84f;
//	private int mCheckColorOn = COLOR_CHECK_ON;
//	private int mCheckColorOff = COLOR_CHECK_OFF;

	public SmoothMarkDrawerCheckBox(Context context,int colorOn, int colorOff) {
		super(context, colorOn, colorOff);
		mPaint.setStrokeJoin(Paint.Join.MITER);
		mCheckHookPath = new Path();
		mCheckRectF = new RectF();
		mCheckClearRectF = new RectF();
//		mCheckColorOn = colorOn;
//		mCheckColorOff = colorOff;
//		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.tint });
//		mCheckColorOn = a.getColor(0, COLOR_CHECK_ON);
//		a.recycle();
	}

	@Override
	public void setBounds(RectF rectF) {
		super.setBounds(rectF);
		mPaint.setStrokeWidth(rectF.width() * PERCENT_HOOK_STROKE);

	}

	

	@Override
	protected void drawMark(Canvas canvas, final float fraction1, float left, float top, float size, View view) {
		// 测试mBounds边界位置
		// mPaint.setStyle(Style.FILL);
		// mPaint.setColor(0x440000ff);
		// canvas.drawRect(mBounds, mPaint);
		final float rectSize = size * (1f - PERCENT_CHECK_PADDING * 2f);
		mCheckRectF.left = (size - rectSize) / 2f + left;
		mCheckRectF.right = mCheckRectF.left + rectSize;
		mCheckRectF.top = (size - rectSize) / 2f + top;
		mCheckRectF.bottom = mCheckRectF.top + rectSize;

//		final int viewHeight = view.getHeight();
		updateCheckPaintColorFilter(view);
		final int sc = canvas
				.saveLayer(mCheckRectF, null, Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG
						| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG
						| Canvas.CLIP_TO_LAYER_SAVE_FLAG);
		final float fraction = interpolator.getInterpolation(fraction1);
		if (fraction == 0f) {
			mPaint.setColor(mColorOff);
			mPaint.setStyle(Style.FILL);
			final float roundRadius = size * PERCENT_BORDER_RADIUS;
			canvas.drawRoundRect(mCheckRectF, roundRadius, roundRadius, mPaint);
			mPaint.setXfermode(xfermode);
			mCheckClearRectF.set(mCheckRectF);
			final float borderSize = size * PERCENT_BORDER_STROKE;
			mCheckClearRectF.inset(borderSize, borderSize);
			// final float insetRadiusTemp = mCheckSize * PERCENT_BORDER_STROKE;
			// canvas.drawRoundRect(mCheckTempRectF, 0, 0, mPaint);
			mPaint.setColorFilter(null);// 如果当前是disabled，ColorFilter是半透明的，无法完全clear掉
			mPaint.setColor(Color.WHITE);// 如果这个color是带alpha的话Xfermode不会完全clear掉下层内容
			canvas.drawRect(mCheckClearRectF, mPaint);
			mPaint.setXfermode(null);
		} else if (fraction == 1f) {
			mPaint.setColor(mColorOn);
			mPaint.setStyle(Style.FILL);
			final float roundRadius = size * PERCENT_BORDER_RADIUS;
			canvas.drawRoundRect(mCheckRectF, roundRadius, roundRadius, mPaint);
			mCheckHookPath.reset();
			mCheckHookPath.moveTo(HOOK_X[0] * size, HOOK_Y[0] * size);
			mCheckHookPath.lineTo(HOOK_X[1] * size, HOOK_Y[1] * size);
			mCheckHookPath.lineTo(HOOK_X[2] * size, HOOK_Y[2] * size);
			mCheckHookPath.offset(left, top);// 修正一下位置，文字可能高于checkbox

			mPaint.setXfermode(xfermode);
			mPaint.setStyle(Style.STROKE);
			mPaint.setColorFilter(null);
			mPaint.setColor(Color.WHITE);
			canvas.drawPath(mCheckHookPath, mPaint);
			mPaint.setXfermode(null);
		} else {
			final float cx = mBounds.centerX();
			final float cy = mBounds.centerY();
			final float HALF_FRACTION = 0.5f;
			mPaint.setColor(convertColorFraction(fraction, mColorOff, mColorOn));
			if (fraction <= HALF_FRACTION) {
				final float offFraction = fraction / HALF_FRACTION;
				// 缩小画布
				final float scale = 1f - (1f - PERCENT_SCALE) * offFraction;
//				canvas.scale(scale, scale, mSize / 2f, viewHeight / 2f);
				canvas.scale(scale, scale, cx, cy);
				// 画背景rect
				mPaint.setStyle(Style.FILL);
				final float roundRadius = size * PERCENT_BORDER_RADIUS;
				canvas.drawRoundRect(mCheckRectF, roundRadius, roundRadius, mPaint);
				// 画中间的空白(其实是用PorterDuff.Mode.DST_OUT从背景rect中抠掉)
				mCheckClearRectF.set(mCheckRectF);
				final float borderSize = size * PERCENT_BORDER_STROKE;
				mCheckClearRectF.inset(borderSize, borderSize);// 先减掉边框
				final float insetAnimationSize = (mCheckRectF.width() / 2f - borderSize) * offFraction;// 减掉动画的部分
				mCheckClearRectF.inset(insetAnimationSize, insetAnimationSize);
				mPaint.setXfermode(xfermode);
				mPaint.setColorFilter(null);
				mPaint.setColor(Color.WHITE);
				final float radiusTemp = mCheckClearRectF.width() / 2f * offFraction;
				canvas.drawRoundRect(mCheckClearRectF, radiusTemp, radiusTemp, mPaint);
				mPaint.setXfermode(null);
			} else {
				// 缩小画布
				final float hookFraction = (fraction - HALF_FRACTION) / (1f - HALF_FRACTION);
				final float scale = PERCENT_SCALE + (1f - PERCENT_SCALE) * hookFraction;
				//canvas.scale(scale, scale, mSize / 2f, viewHeight / 2f);
				canvas.scale(scale, scale, cx, cy);
				// 画背景rect
				mPaint.setStyle(Style.FILL);
				final float roundRadius = size * PERCENT_BORDER_RADIUS;
				canvas.drawRoundRect(mCheckRectF, roundRadius, roundRadius, mPaint);
				// 画对号hook(其实是用PorterDuff.Mode.DST_OUT从背景rect中抠掉)
				mCheckHookPath.reset();
				float x0 = HOOK_X[1] + (HOOK_X[0] - HOOK_X[1]) * hookFraction;
				float y0 = HOOK_Y[1] + (HOOK_Y[0] - HOOK_Y[1]) * hookFraction;
				float x2 = HOOK_X[1] + (HOOK_X[2] - HOOK_X[1]) * hookFraction;
				float y2 = HOOK_Y[1] + (HOOK_Y[2] - HOOK_Y[1]) * hookFraction;
				mCheckHookPath.moveTo(x0 * size, y0 * size);
				mCheckHookPath.lineTo(HOOK_X[1] * size, HOOK_Y[1] * size);
				mCheckHookPath.lineTo(x2 * size, y2 * size);
				mCheckHookPath.offset(left, top);// 修正一下位置，文字可能高于checkbox
				mPaint.setXfermode(xfermode);
				mPaint.setStyle(Style.STROKE);
				mPaint.setColorFilter(null);
				mPaint.setColor(Color.WHITE);
				canvas.drawPath(mCheckHookPath, mPaint);
				mPaint.setXfermode(null);
			}

		}
		if (!view.isInEditMode()) {
			canvas.restoreToCount(sc);// eclipse的预览界面这里会报错
		}

	}

	

}
