package com.mixiaoxiao.smoothcompoundbutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;

public abstract class SmoothCompoundButton extends Button implements Checkable {

	final String TAG = "SmoothCompoundButton";
	private static final int ANIMATION_DURATION = 240;

	private boolean mChecked;
	private boolean mBroadcasting;
	private OnCheckedChangeListener mOnCheckedChangeListener;
	private OnCheckedChangeListener mOnCheckedChangeWidgetListener;

	/** ON状态的颜色，appcompat中是?attr/colorAccent **/
	private final int DEFAULT_COLOR_ON = 0xff009688;
	/** OFF状态的颜色，appcompat是colorControlNormal=?android:attr/textColorSecondary **/
	private final int DEFAULT_COLOR_OFF = 0x8a000000;
	private SmoothMarkDrawer mMarkDrawer;
	private RectF mMarkBounds;
	/** checked = false时mCheckFraction是0，true是1，动画过程由0到1之间 **/
	private float mCheckFraction = 0f;
	/** 用于动画，ANIMATION_STEP_PER_MS， 每一毫秒mCheckFraction需要改变的量 **/
	protected final float ANIMATION_STEP_PER_MS = 1f / ANIMATION_DURATION;
	/** 用于动画，上次draw的系统时间 */
	private long mLastDrawTime = SystemClock.uptimeMillis();
	// private final float mCheckFractionAnimationStep = 0.075f;// 0.075f;
	/** 如果为true，则仅可以点击mark部分来toogle，点击文字部分无效 **/
	private boolean mClickMarkOnly;
	/** 是否反转SmoothMarkDrawer默认的mark位置**/
	private boolean mReverseMarkPosition;  

	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };
	private boolean mIsAttachedToWindow = false;
	// 这个ATTRS的项必须是从小到大排列的
	private final int[] ATTRS = new int[] {
			// android.R.attr.enabled, //16842766:0 //
			// android.R.attr.textSize,// 16842901：1
			// android.R.attr.textColor,// 16842904：2

			android.R.attr.clickable, // 16842981
			android.R.attr.gravity,// 16842927
			android.R.attr.checked,// 16843014
			android.R.attr.adjustViewBounds,// 16843038 借来的attr，用于设置mClickMarkOnly
			android.R.attr.tint, // 16843041 借来的attr，用于设置mark的颜色
			android.R.attr.cropToPadding // 16843043 借来的attr，用于设置mark的左右位置是否反转 

	// android.R.attr.text// 16843087:6

	};

	public SmoothCompoundButton(Context context) {
		this(context, null);
	}

	public SmoothCompoundButton(Context context, AttributeSet attrs) {
		// defStyle只能取0 （取textViewStyle、checkBoxStyle、buttonStyle都会有额外的蛋疼样式）
		this(context, attrs, 0);
	}

	public SmoothCompoundButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {

		int colorOn = DEFAULT_COLOR_ON;
		int colorOff = DEFAULT_COLOR_OFF;
		//CheckedTextView
		if (attrs != null) {
			final TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
			if (!a.hasValue(0)) {
				setClickable(true);
			}

			// 如果没有在xml中设置gravity，那么设置一个center_vertical
			if (!a.hasValue(1)) {
				setGravity(Gravity.CENTER_VERTICAL);
				log("NOT has gravity value, so setGravity(Gravity.CENTER_VERTICAL)");
			}
			mChecked = a.getBoolean(2, false);
			mCheckFraction = mChecked ? 1f : 0f;
			mClickMarkOnly = a.getBoolean(3, false);
			ColorStateList colors = a.getColorStateList(4);
			if (colors != null) {
				if (colors.isStateful()) {
					colorOn = colors.getColorForState(new int[] { android.R.attr.state_checked }, DEFAULT_COLOR_ON);
					colorOff = colors.getColorForState(new int[] { -android.R.attr.state_checked }, DEFAULT_COLOR_ON);
				} else {
					colorOn = colors.getDefaultColor();
				}
			}
			mReverseMarkPosition = a.getBoolean(5, false);
			a.recycle();
		}
		mMarkDrawer = makeSmoothMarkDrawer(context, colorOn, colorOff);
		if (mMarkDrawer == null) {
			throw new RuntimeException("makeSmoothMarkDrawer must NOT be NULL!");
		}
		mCheckFraction = mChecked ? 1f : 0f;
		mMarkBounds = new RectF();
	}

	protected abstract SmoothMarkDrawer makeSmoothMarkDrawer(Context context, int colorOn, int colorOff);

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mMarkDrawer.draw(canvas, mCheckFraction, this);
		// log("onDraw->" + mMarkBounds.toShortString());
		if (updateAnimationFraction()) {
			invalidate();
		}
	}

	/** 计算Fraction，返回true则动画还未结束，需要invalidate **/
	private boolean updateAnimationFraction() {
		final long currTime = SystemClock.uptimeMillis();
		// 限制到16ms到48ms之间，也就是1帧到3帧
		final long duration = Math.min(48L, Math.max(16L, currTime - mLastDrawTime));
		mLastDrawTime = currTime;
		final float animationStep = ANIMATION_STEP_PER_MS * duration;
		log("updateAnimationFraction duration->" + duration + " step->" + animationStep);
		if (mMarkDrawer != null) {
			if (mMarkDrawer.isUpdatingFractionBySelf()) {
				log("mMarkDrawer.isUpdatingFractionBySelf");
				return false;
			}
		}
		if (mChecked) {
			if (mCheckFraction < 1f) {
				mCheckFraction += animationStep;
				if (mCheckFraction > 1f) {
					mCheckFraction = 1f;
				}
				// log("need draw next frame");
				return true;
			}
		} else {
			if (mCheckFraction > 0f) {
				mCheckFraction -= animationStep;
				if (mCheckFraction < 0f) {
					mCheckFraction = 0f;
				}
				// log("need draw next frame");
				return true;
			}
		}
		// log("NOT need draw next frame");
		return false;
	}

	void setFractionInternal(float fraction) {
		log("setFractionInternal->" + fraction);
		if (fraction < 0) {
			fraction = 0;
		} else if (fraction > 1) {
			fraction = 1;
		}
		if (mCheckFraction != fraction) {
			mCheckFraction = fraction;
			invalidate();
		}
	}

	void setCheckedStateByFraction() {
		setChecked(mCheckFraction >= 0.5f);
	}

	float getFractionInternal() {
		return mCheckFraction;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mClickMarkOnly) {
			if (!mMarkBounds.isEmpty()) {
				final int action = MotionEventCompat.getActionMasked(event);
				if (action == MotionEvent.ACTION_DOWN) {
					final float x = event.getX();
					final float y = event.getY();
					if (isEnabled() && mMarkBounds.contains(x, y)) {
						// OK，要处理
						log("hit the MarkBounds !");
					} else {
						// 忽略
						log("NOT hit the MarkBounds, so ignore");
						return false;
					}
				}
			}
		}
		if (mMarkDrawer != null && isEnabled()) {
			if (mMarkDrawer.onTouchEvent(event, this)) {
				return true;
			}
		}
		return super.onTouchEvent(event);
	}

	void superOnTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		// if (changed) {
		final int viewWidth = right - left;
		final int viewHeight = bottom - top;
		updateMarkBounds(viewWidth, viewHeight);
		// }
	}

	private void updateMarkBounds(int viewWidth, int viewHeight) {
		final float markWidth = mMarkDrawer.getWidth();
		final float markHeight = mMarkDrawer.getHeight();
		mMarkBounds.top = viewHeight / 2f - markHeight / 2f;
		mMarkBounds.bottom = mMarkBounds.top + markHeight;
		mMarkBounds.left = 0;
		mMarkBounds.right = mMarkBounds.left + markWidth;
		if (!isMarkInRight()) {
			mMarkBounds.offset(getPaddingLeft(), 0);
		}else{
			final float offset = viewWidth - markWidth - getPaddingRight();
			mMarkBounds.left += offset;
			mMarkBounds.right += offset;
		}
		mMarkDrawer.setBounds(mMarkBounds);
	}

	@Override
	public int getCompoundPaddingLeft() {
		int padding = super.getCompoundPaddingLeft();
		if (mMarkDrawer != null) {
			if (!isMarkInRight()) {
				padding += mMarkDrawer.getWidth();
			}
		}
		return padding;
	}

	@Override
	public int getCompoundPaddingRight() {
		int padding = super.getCompoundPaddingRight();
		if (mMarkDrawer != null) {
			if (isMarkInRight()) {
				padding += mMarkDrawer.getWidth();
			}
		}

		return padding;
	}

	private boolean isMarkInRight() {
		if (mMarkDrawer == null) {
			return !isLayoutRtl(this);
		}
		boolean drawerIsMarkInRight = mMarkDrawer.isMarkInRight();
		if(mReverseMarkPosition){
			drawerIsMarkInRight = !drawerIsMarkInRight;
		}
		if (isLayoutRtl(this)) {
			return  !drawerIsMarkInRight;
		} else {
			return drawerIsMarkInRight;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//先调用一下super.onMeasure让TextView干他该干的事情
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//之后再修正一下
		final int defaultWidth = mMarkDrawer.getDefaultWidth();
		final int defaultHeight = mMarkDrawer.getDefaultHeight();
		if (TextUtils.isEmpty(getText())) {
			// 如果没有文字，那么用SmoothMark完全充满大小
			final float defaultFactor = defaultWidth * 1f / defaultHeight;
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = MeasureSpec.getSize(heightMeasureSpec);
			final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
			final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
			if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY) {
				if ((width * 1f / defaultWidth) >= (height * 1f / defaultHeight)) {
					// height较小，由height约束
					if (height < defaultHeight) {
						height = defaultHeight;
					}
					width = Math.round(height * defaultFactor);
				} else {
					if (width < defaultWidth) {
						width = defaultWidth;
					}
					height = Math.round(width / defaultFactor);
				}
			} else {
				if (widthMode == MeasureSpec.EXACTLY) {
					if (width < defaultWidth) {
						width = defaultWidth;
					}
					height = Math.round(width / defaultFactor);
				} else {
					if (heightMode == MeasureSpec.EXACTLY) {
						if (height < defaultHeight) {
							height = defaultHeight;
						}
						width = Math.round(height * defaultFactor);
					} else {
						width = defaultWidth;
						height = defaultHeight;
					}
				}
			}

			mMarkDrawer.setWidth(width);
			mMarkDrawer.setHeight(height);
			setMeasuredDimension(width, height);
		} else {
			//有文字的话，使得整体高度必须可容纳SmoothMark
			final int measuredHeight = getMeasuredHeight();
			mMarkDrawer.setWidth(defaultWidth);
			mMarkDrawer.setHeight(defaultHeight);
			if (measuredHeight < defaultHeight) {
				setMeasuredDimension(getMeasuredWidth(), defaultHeight);
			}
		}
		updateMarkBounds(getMeasuredWidth(), getMeasuredHeight());

	}

	// Code for drawable state//
	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		return drawableState;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mMarkDrawer != null) {
			mMarkDrawer.drawableStateChanged(this);
		}

	}

	@SuppressLint("NewApi")
	@Override
	public void drawableHotspotChanged(float x, float y) {
		super.drawableHotspotChanged(x, y);
		if (mMarkDrawer != null) {
			mMarkDrawer.drawableHotspotChanged(x, y);
		}

	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		if (mMarkDrawer == null) {// fuck这里居然会有null?
			return super.verifyDrawable(who);
		} else {
			return super.verifyDrawable(who) || mMarkDrawer.verifyDrawable(who);
		}

	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		if (mMarkDrawer != null) {
			mMarkDrawer.jumpDrawablesToCurrentState();
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mIsAttachedToWindow = true;
		if (mMarkDrawer != null) {
			mMarkDrawer.onAttachedToWindow(this);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mIsAttachedToWindow = false;
		if (mMarkDrawer != null) {
			mMarkDrawer.onDetachedFromWindow(this);
		}
	}

	// Code for Checkable//

	@Override
	public void setChecked(boolean checked) {
		log("setChecked->" + checked);
		setChecked(checked, true, true);
	}

	/**
	 * 
	 * @param checked
	 * @param withAnimaation
	 *            是否使用动画
	 * @param notifyOnCheckedChangeListener
	 *            是否回调OnCheckedChangeListener
	 */
	public void setChecked(boolean checked, boolean withAnimation, boolean notifyOnCheckedChangeListener) {
		if (mChecked != checked) {
			mChecked = checked;
			if (!mIsAttachedToWindow) {
				log("NOT AttachedToWindow, so no animation");
				// 如果此时还没有AttachedToWindow比如在onCreate的时候设置
				// 则不用动画效果
				mCheckFraction = mChecked ? 1f : 0f;
			} else {
				if (!withAnimation) {
					mCheckFraction = mChecked ? 1f : 0f;
				}
			}
			refreshDrawableState();
			// Avoid infinite recursions if setChecked() is called from a
			// listener
			if (mBroadcasting) {
				return;
			}
			mBroadcasting = true;
			if (notifyOnCheckedChangeListener) {
				if (mOnCheckedChangeListener != null) {
					mOnCheckedChangeListener.onCheckedChanged(this, mChecked);
				}
			}
			if (mOnCheckedChangeWidgetListener != null) {
				mOnCheckedChangeWidgetListener.onCheckedChanged(this, mChecked);
			}
			mBroadcasting = false;
		}
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void toggle() {
		setChecked(!mChecked);
	}

	@Override
	public boolean performClick() {
		/*
		 * XXX: These are tiny, need some surrounding 'expanded touch area',
		 * which will need to be implemented in Button if we only override
		 * performClick()
		 */

		/* When clicked, toggle the state */
		toggle();
		return super.performClick();
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeListener = listener;
	}

	void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeWidgetListener = listener;
	}

	public static interface OnCheckedChangeListener {
		void onCheckedChanged(SmoothCompoundButton buttonView, boolean isChecked);
	}

	// copy from support
	public static boolean isLayoutRtl(View view) {
		return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
	}

	private void log(String msg) {
		Log.d(TAG, msg);
	}

}
