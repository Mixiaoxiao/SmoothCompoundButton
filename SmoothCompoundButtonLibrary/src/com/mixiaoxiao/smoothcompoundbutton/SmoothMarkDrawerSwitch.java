package com.mixiaoxiao.smoothcompoundbutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

public class SmoothMarkDrawerSwitch extends SmoothMarkDrawer {

//	private static final int DEFAULT_COLOR_THUMB_OFF = 0xffececec;
//	private static final int DEFAULT_COLOR_THUMB_ON = MaterialColor.DefaultLight.colorPrimary;
//	private static final int DEFAULT_COLOR_TRACK_OFF = 0x60000000;
//	private static final int DEFAULT_COLOR_TRACK_ON = 0x60bdbdbd;

	private static final float PERCENT_TRACK_INSET_X = 0.33f;
	private static final float PERCENT_TRACK_INSET_Y = 0.23f;
	private static final float PERCENT_THUMB_INSET = 0.0f;// 0.12f;

	private RectF mTrackRectF;
	private RectF mThumbRectF;
	private int mTrackColorOn, mTrackColorOff;
	
	//以下内容用以处理滑动事件，参考自appcompat里面的SwitchCompat
	private static final int TOUCH_MODE_IDLE = 0;
	private static final int TOUCH_MODE_DOWN = 1;
	private static final int TOUCH_MODE_DRAGGING = 2;
	
	private int mTouchMode = TOUCH_MODE_IDLE;
	private int mTouchSlop;
	private float mTouchX;
	private float mTouchY;
	@SuppressLint("Recycle") 
	private VelocityTracker mVelocityTracker = VelocityTracker.obtain();
	private int mMinFlingVelocity;

	public SmoothMarkDrawerSwitch(Context context, int colorOn, int colorOff) {
		super(context, colorOn, colorOff);
		mTrackRectF = new RectF();
		mThumbRectF = new RectF();
		this.mColorOn = convertColorAlpha(1f, colorOn);
		this.mColorOff = 0xffececec;//convertColorAlpha(1f, colorOff);
		this.mTrackColorOn =  convertColorAlpha(0.3f, mColorOn);
		this.mTrackColorOff = convertColorAlpha(0.3f, 0xff141414);
		
		final ViewConfiguration config = ViewConfiguration.get(context);
		mTouchSlop = config.getScaledTouchSlop();
		mMinFlingVelocity = config.getScaledMinimumFlingVelocity();
	}
	private void log(String msg){
		Log.d("SwitchSmoothMarkDrawer", msg);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev, SmoothCompoundButton smoothCompoundButton) {
		mVelocityTracker.addMovement(ev);
		final int action = MotionEventCompat.getActionMasked(ev);
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			final float x = ev.getX();
			final float y = ev.getY();
			//isEnabled()已经在SmoothCompoundButton中判断了
			if ( hitThumb(x, y, smoothCompoundButton.isChecked())) {
				log("hitThumb true-> x=" + x + " y=" + y);
				smoothCompoundButton.getParent().requestDisallowInterceptTouchEvent(true);
				mTouchMode = TOUCH_MODE_DOWN;
				mTouchX = x;
				mTouchY = y;
			}else{
				log("hitThumb false-> x=" + x + " y=" + y);
			}
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			switch (mTouchMode) {
			case TOUCH_MODE_IDLE:
				// Didn't target the thumb, treat normally.
				break;

			case TOUCH_MODE_DOWN: {
				final float x = ev.getX();
				final float y = ev.getY();
				if (Math.abs(x - mTouchX) > mTouchSlop || Math.abs(y - mTouchY) > mTouchSlop) {
					mTouchMode = TOUCH_MODE_DRAGGING;
					log("TOUCH_MODE_DOWN -> TOUCH_MODE_DRAGGING");
					smoothCompoundButton.getParent().requestDisallowInterceptTouchEvent(true);
					mTouchX = x;
					mTouchY = y;
					return true;
				}
				break;
			}

			case TOUCH_MODE_DRAGGING: {
				
				final float x = ev.getX();
				final int thumbScrollRange = getThumbScrollRange();
				final float thumbScrollOffset = x - mTouchX;
				float dPos;
				log("thumbScrollRange->" + thumbScrollRange + " thumbScrollOffset->" + thumbScrollOffset);
				if (thumbScrollRange != 0) {
					dPos = thumbScrollOffset / thumbScrollRange;
				} else {
					// If the thumb scroll range is empty, just use the
					// movement direction to snap on or off.
					dPos = thumbScrollOffset > 0 ? 1 : -1;
				}
				//if (SmoothCompoundButton.isLayoutRtl(smoothCompoundButton)) {
					//Oh, fuck 
					//dPos = -dPos;
				//}
				float mThumbPosition = smoothCompoundButton.getFractionInternal();
				final float newPos = constrain(mThumbPosition + dPos, 0, 1);
				if (newPos != mThumbPosition) { 
					mTouchX = x;
//					setThumbPosition(newPos);
					smoothCompoundButton.setFractionInternal(newPos);
				}
				return true;
			}
			}
			break;
		}

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL: {
			if (mTouchMode == TOUCH_MODE_DRAGGING) {
				stopDrag(ev, smoothCompoundButton);
				// Allow super class to handle pressed state, etc.
//				super.onTouchEvent(ev);
				return true;
			}
			mTouchMode = TOUCH_MODE_IDLE;
			mVelocityTracker.clear();
			break;
		}
		}
		return false;
	}
	
	private void stopDrag(MotionEvent ev, SmoothCompoundButton smoothCompoundButton) {
		mTouchMode = TOUCH_MODE_IDLE;

		// Commit the change if the event is up and not canceled and the switch
		// has not been disabled during the drag.
		final boolean commitChange = ev.getAction() == MotionEvent.ACTION_UP;// && isEnabled();
		final boolean newState;
		if (commitChange) {
			mVelocityTracker.computeCurrentVelocity(1000);
			final float xvel = mVelocityTracker.getXVelocity();
			if (Math.abs(xvel) > mMinFlingVelocity) {
//				newState = SmoothCompoundButton.isLayoutRtl(smoothCompoundButton) ? (xvel < 0) : (xvel > 0);
				//Oh, fuck
				newState = xvel > 0;
			} 
			else {
				newState = smoothCompoundButton.getFractionInternal() > 0.5f ? true : false; // getTargetCheckedState();
				log("newState byFraction->" + newState);
			}
		} 
		else {
			newState = smoothCompoundButton.isChecked();
		}
		smoothCompoundButton.setChecked(newState);
		//cancelSuperTouch(ev);
		MotionEvent cancel = MotionEvent.obtain(ev);
		cancel.setAction(MotionEvent.ACTION_CANCEL);
		smoothCompoundButton.superOnTouchEvent(cancel);
		cancel.recycle();
	}
	
	
	@Override
	public boolean isUpdatingFractionBySelf() {
		return mTouchMode != TOUCH_MODE_IDLE;
	}

	@Override
	public void setBounds(RectF rectF) {
		// super.setBounds(rectF);
		mBounds.set(rectF);
		mTrackRectF.set(mBounds);
		mTrackRectF.inset(PERCENT_TRACK_INSET_X * mBounds.height(),
				PERCENT_TRACK_INSET_Y * mBounds.height());
		// 需要知道fraction后设置mCompatBackgroundDrawable的位置
	}
	
	@Override
	public int getDefaultWidth() {
		return dp2px(46); 
	}
	@Override
	public int getDefaultHeight() {
		return dp2px(26);
	}
	/**thumb总共可以移动的宽度**/
	private int getThumbScrollRange(){
		float thumbSize = mBounds.height();
		return (int) (mBounds.width() - thumbSize);
	}
	private boolean hitThumb(float x, float y, boolean isChecked){
		if(mBounds.isEmpty()){
			return false;
		}
		final float thumbSize = mBounds.height();
		final float thumbLeft = isChecked ? (mBounds.right - thumbSize) : mBounds.left;
		final float thumbTop = mBounds.top;
		return (x >= thumbLeft) && (x <= (thumbLeft + thumbSize)) &&  (y >= thumbTop) && (y <= (thumbTop + thumbSize));
	}
	
	@Override
	public void draw(Canvas canvas, float fraction, View view) {
			//super.draw(canvas, fraction, view);//重写这个，自身处理mCompatBackgroundDrawable
		drawMark(canvas, fraction, mBounds.left, mBounds.top, mBounds.width(), view);
	}
	
	private LayerDrawable mThumbLayerDrawable;//带阴影的thumb 
	
	@Override
	protected void drawMark(Canvas canvas, float fraction, float left, float top, float size, View view) {
		// 测试mBounds边界位置
//		mPaint.setStyle(Style.FILL);
//		mPaint.setColor(0x440000ff);
//		canvas.drawRect(mBounds, mPaint);
		final float width = mBounds.width();
		final float height = mBounds.height();
		
		updateCheckPaintColorFilter(view);
		
		//draw track
		mPaint.setColor(convertColorFraction(fraction, mTrackColorOff, mTrackColorOn));
		canvas.drawRoundRect(mTrackRectF, mTrackRectF.height() / 2f, mTrackRectF.height() / 2f, mPaint);
		
		//draw thumb
		final float thumbSize = height;
		final float thumbFullOffset = width - thumbSize;//总共可以移动的距离
		mThumbRectF.left = left +  thumbFullOffset * fraction;
		mThumbRectF.right = mThumbRectF.left + thumbSize;
		mThumbRectF.top = mBounds.top;
		mThumbRectF.bottom = mBounds.bottom;
		
		if(REAL_RIPPLE){
			mCompatBackgroundDrawable.setBounds((int) (mThumbRectF.left), (int) (mThumbRectF.top),
					(int) (mThumbRectF.right),(int) (mThumbRectF.bottom));
		}else{
			final int moreSize = (int) ((mThumbRectF.width() * (1.414f - 1f)) / 2f);// 边界多出来的大小
			mCompatBackgroundDrawable.setBounds((int) (mThumbRectF.left - moreSize), (int) (mThumbRectF.top - moreSize),
					(int) (mThumbRectF.right + moreSize), (int) (mThumbRectF.bottom + moreSize));
		}
		mCompatBackgroundDrawable.draw(canvas);
		
		final float thumbInset = height * PERCENT_THUMB_INSET;
		mThumbRectF.inset(thumbInset, thumbInset);//这里是正确的Thumb范围
		
		makeThumbBitmapIfNeeded();
		if(mThumbLayerDrawable!=null){
			if(view.isEnabled()){
				mThumbLayerDrawable.setColorFilter(null);
			}else{
				mThumbLayerDrawable.setColorFilter(mCheckDisableColorFilter);
			}
			mThumbLayerDrawable.setBounds((int)mThumbRectF.left, (int)mThumbRectF.top, (int)mThumbRectF.right, (int)mThumbRectF.bottom);
			//PorterDuff.Mode.SRC_IN;效果不佳
			//而且如果是整体setColorFilter的话，半透明黑色阴影也会被着色，用LayerDrawable只修改上层的圆形drawable的颜色
			((ShapeDrawable)mThumbLayerDrawable.getDrawable(1)).getPaint().setColor(convertColorFraction(fraction, mColorOff, mColorOn));
			mThumbLayerDrawable.draw(canvas);
		}
	}
	
//	private void fuckHardwareAcceleratedAndShadowLayer(Canvas canvas, float fraction){
//		final float thumbRealRadius = mThumbRectF.width() / 2f;
//		//draw thumb shadow
//		final float thumbShadowOffsetY = thumbRealRadius * 0.15f;
//		mThumbRectF.offset(0, thumbShadowOffsetY);
//		mPaint.setColor(0x2a000000);
//		canvas.drawCircle(mThumbRectF.centerX(), mThumbRectF.centerY(), thumbRealRadius , mPaint);
//		mThumbRectF.offset(0, -thumbShadowOffsetY);//恢复回来
//		//draw thumb button
//		//mPaint.setShadowLayer(thumbRealRadius * 0.22f, 0, thumbShadowOffsetY, 0x2a000000);
//		//setShadowLayer不支持硬件加速？关闭硬件加速后ripple无法超出View范围
//		mPaint.setColor(convertColorFraction(fraction, mColorOff, mColorOn));
//		//setMaskFilter也不支持硬件加速
//		//mPaint.setMaskFilter(new BlurMaskFilter(thumbShadowOffsetY, BlurMaskFilter.Blur.NORMAL));
//		canvas.drawCircle(mThumbRectF.centerX(), mThumbRectF.centerY(), thumbRealRadius, mPaint);
//		//mPaint.setShadowLayer(0, 0, 0, 0);
//	}
	
	
	private void makeThumbBitmapIfNeeded(){
		final int width = Math.round(mThumbRectF.width());
		final int height = Math.round(mThumbRectF.height());
		if(mThumbLayerDrawable != null){
			final Bitmap b = ((BitmapDrawable)mThumbLayerDrawable.getDrawable(0)).getBitmap();
			if(b.getWidth() == width
					&& b.getHeight() == height){
				return;
			}
		}
		try {
			Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			//经测试是isHardwareAccelerated是false，自己new的Canvas是不带硬件加速的
			//可用setShadowLayer
			log("canvas.isHardwareAccelerated()->" + canvas.isHardwareAccelerated());
			
			RectF tmpRectF = new RectF(mThumbRectF);
			tmpRectF.offsetTo(0, 0);
			Paint tmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			final int thumbShadowOffset = Math.round(tmpRectF.width() * 0.11f);
			tmpRectF.inset(thumbShadowOffset, thumbShadowOffset);
			final float thumbRealRadius = tmpRectF.width() / 2f; 
			tmpPaint.setColor(Color.TRANSPARENT);
			tmpPaint.setShadowLayer(thumbShadowOffset / 2f, 0, thumbShadowOffset /2f, 0x44000000);
			//tmpPaint.setShadowLayer(0, 0, 0, 0);//取消掉layer
			canvas.drawCircle(tmpRectF.centerX(), tmpRectF.centerY(), thumbRealRadius, tmpPaint);
			
			BitmapDrawable shadowDrawable = new BitmapDrawable(mContext.getResources(), bitmap);
			ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
			shapeDrawable.getPaint().setColor(Color.WHITE);
			//下层是已经画好阴影的BitmapDrawable，上层是带Inset的ShapeDrawable（用于着色）
			LayerDrawable thumbDrawable = new LayerDrawable(new Drawable[]{shadowDrawable, shapeDrawable});
			//把上层的shapeDrawable缩小thumbShadowOffset
			thumbDrawable.setLayerInset(1, thumbShadowOffset, thumbShadowOffset, thumbShadowOffset,thumbShadowOffset);
			mThumbLayerDrawable = thumbDrawable;
		} catch (Exception e) {
			e.printStackTrace();
		} catch (OutOfMemoryError er) {
			er.printStackTrace();
			log("Oh, FUCK!");
		}
		
		
		
	}
	
	
	@Override
	protected boolean isMarkInRight() {
		return true;
	}
	
	/**
	 * Taken from android.util.MathUtils
	 */
	private static float constrain(float amount, float low, float high) {
		return amount < low ? low : (amount > high ? high : amount);
	}

}
