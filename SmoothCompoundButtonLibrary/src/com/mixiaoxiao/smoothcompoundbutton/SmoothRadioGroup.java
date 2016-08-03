package com.mixiaoxiao.smoothcompoundbutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;

/**
 * Code is copied from RadioGroup mostly
 * @author Mixiaoxiao
 *
 */
public class SmoothRadioGroup extends LinearLayout{
	
	// holds the checked id; the selection is empty by default
    private int mCheckedId = -1;
    // tracks children radio buttons checked state
    private SmoothCompoundButton. OnCheckedChangeListener mChildOnCheckedChangeListener;
    // when true, mOnCheckedChangeListener discards events
    private boolean mProtectFromCheckedChange = false;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private PassThroughHierarchyChangeListener mPassThroughListener;
	
	public SmoothRadioGroup(Context context) {
		super(context);
		setOrientation(VERTICAL);
		init();
	}
	public SmoothRadioGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		// retrieve selected radio button as requested by the user in the
        // XML layout file
        TypedArray attributes = context.obtainStyledAttributes(
                attrs,new int[]{android.R.attr.orientation, //16842948 
                		android.R.attr.checkedButton,//16843080 
                		});

        int value =  attributes.getResourceId(1, View.NO_ID);
        if (value != View.NO_ID) {
            mCheckedId = value;
        }
        final int index = attributes.getInt(0, VERTICAL);
        setOrientation(index);

        attributes.recycle();
        init();
	}
	 private void init() {
	        mChildOnCheckedChangeListener = new CheckedStateTracker();
	        mPassThroughListener = new PassThroughHierarchyChangeListener();
	        super.setOnHierarchyChangeListener(mPassThroughListener);
	    }
	 
	    @Override
	    public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
	        // the user listener is delegated to our pass-through listener
	        mPassThroughListener.mOnHierarchyChangeListener = listener;
	    }
	    
	    @Override
	    protected void onFinishInflate() {
	        super.onFinishInflate();

	        // checks the appropriate radio button as requested in the XML file
	        if (mCheckedId != -1) {
	            mProtectFromCheckedChange = true;
	            setCheckedStateForView(mCheckedId, true);
	            mProtectFromCheckedChange = false;
	            setCheckedId(mCheckedId);
	        }
	    }
	    @Override
	    public void addView(View child, int index, ViewGroup.LayoutParams params) {
	        if (child instanceof SmoothRadioButton) {
	            final SmoothRadioButton button = (SmoothRadioButton) child;
	            if (button.isChecked()) {
	                mProtectFromCheckedChange = true;
	                if (mCheckedId != -1) {
	                    setCheckedStateForView(mCheckedId, false);
	                }
	                mProtectFromCheckedChange = false;
	                setCheckedId(button.getId());
	            }
	        }

	        super.addView(child, index, params);
	    }
	    
	    public void check(int id) {
	        // don't even bother
	        if (id != -1 && (id == mCheckedId)) {
	            return;
	        }

	        if (mCheckedId != -1) {
	            setCheckedStateForView(mCheckedId, false);
	        }

	        if (id != -1) {
	            setCheckedStateForView(id, true);
	        }

	        setCheckedId(id);
	    }
	    
	    private void setCheckedId(int id) {
	        mCheckedId = id;
	        if (mOnCheckedChangeListener != null) {
	            mOnCheckedChangeListener.onCheckedChanged(this, mCheckedId);
	        }
	    }

	    private void setCheckedStateForView(int viewId, boolean checked) {
	        View checkedView = findViewById(viewId);
	        if (checkedView != null && checkedView instanceof SmoothRadioButton) {
	            ((SmoothRadioButton) checkedView).setChecked(checked);
	        }
	    }
	    
	    public int getCheckedRadioButtonId() {
	        return mCheckedId;
	    }
	    
	    public void clearCheck() {
	        check(-1);
	    }
	    
	    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
	        mOnCheckedChangeListener = listener;
	    }
	    
	    @Override
	    public LayoutParams generateLayoutParams(AttributeSet attrs) {
	        return new SmoothRadioGroup.LayoutParams(getContext(), attrs);
	    }
	    
	    
	    @Override
	    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
	        return p instanceof SmoothRadioGroup.LayoutParams;
	    }
	    
	    @Override
	    protected LinearLayout.LayoutParams generateDefaultLayoutParams() {
	        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    }
	    
	    @Override
	    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
	        super.onInitializeAccessibilityEvent(event);
	        event.setClassName(SmoothRadioGroup.class.getName());
	    }

	    @Override
	    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
	        super.onInitializeAccessibilityNodeInfo(info);
	        info.setClassName(SmoothRadioGroup.class.getName());
	    }
	    
	    public static class LayoutParams extends LinearLayout.LayoutParams {
	        public LayoutParams(Context c, AttributeSet attrs) {
	            super(c, attrs);
	        }

	        public LayoutParams(int w, int h) {
	            super(w, h);
	        }

	        public LayoutParams(int w, int h, float initWeight) {
	            super(w, h, initWeight);
	        }

	        public LayoutParams(ViewGroup.LayoutParams p) {
	            super(p);
	        }

	        public LayoutParams(MarginLayoutParams source) {
	            super(source);
	        }

	        /**
	         * <p>Fixes the child's width to
	         * {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT} and the child's
	         * height to  {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT}
	         * when not specified in the XML file.</p>
	         *
	         * @param a the styled attributes set
	         * @param widthAttr the width attribute to fetch
	         * @param heightAttr the height attribute to fetch
	         */
	        @Override
	        protected void setBaseAttributes(TypedArray a,
	                int widthAttr, int heightAttr) {

	            if (a.hasValue(widthAttr)) {
	                width = a.getLayoutDimension(widthAttr, "layout_width");
	            } else {
	                width = WRAP_CONTENT;
	            }
	            
	            if (a.hasValue(heightAttr)) {
	                height = a.getLayoutDimension(heightAttr, "layout_height");
	            } else {
	                height = WRAP_CONTENT;
	            }
	        }
	    }
	    
	    public interface OnCheckedChangeListener {
	        /**
	         * <p>Called when the checked radio button has changed. When the
	         * selection is cleared, checkedId is -1.</p>
	         *
	         * @param group the group in which the checked radio button has changed
	         * @param checkedId the unique identifier of the newly checked radio button
	         */
	        public void onCheckedChanged(SmoothRadioGroup group, int checkedId);
	    }
	 
	 private class CheckedStateTracker implements SmoothCompoundButton.OnCheckedChangeListener {
	        public void onCheckedChanged(SmoothCompoundButton buttonView, boolean isChecked) {
	            // prevents from infinite recursion
	            if (mProtectFromCheckedChange) {
	                return;
	            }

	            mProtectFromCheckedChange = true;
	            if (mCheckedId != -1) {
	                setCheckedStateForView(mCheckedId, false);
	            }
	            mProtectFromCheckedChange = false;

	            int id = buttonView.getId();
	            setCheckedId(id);
	        }
	    }
	 
	 /**
	     * <p>A pass-through listener acts upon the events and dispatches them
	     * to another listener. This allows the table layout to set its own internal
	     * hierarchy change listener without preventing the user to setup his.</p>
	     */
	    private class PassThroughHierarchyChangeListener implements
	            ViewGroup.OnHierarchyChangeListener {
	        private ViewGroup.OnHierarchyChangeListener mOnHierarchyChangeListener;

	        /**
	         * {@inheritDoc}
	         */
	        public void onChildViewAdded(View parent, View child) {
	            if (parent == SmoothRadioGroup.this && child instanceof SmoothRadioButton) {
	                int id = child.getId();
	                // generates an id if it's missing
	                if (id == View.NO_ID) {
	                    id = SmoothViewCompat.generateViewId();
	                    child.setId(id);
	                }
	                ((SmoothRadioButton) child).setOnCheckedChangeWidgetListener(
	                        mChildOnCheckedChangeListener);
	            }

	            if (mOnHierarchyChangeListener != null) {
	                mOnHierarchyChangeListener.onChildViewAdded(parent, child);
	            }
	        }

	        /**
	         * {@inheritDoc}
	         */
	        public void onChildViewRemoved(View parent, View child) {
	            if (parent == SmoothRadioGroup.this && child instanceof SmoothRadioButton) {
	                ((SmoothRadioButton) child).setOnCheckedChangeWidgetListener(null);
	            }

	            if (mOnHierarchyChangeListener != null) {
	                mOnHierarchyChangeListener.onChildViewRemoved(parent, child);
	            }
	        }
	    }
	 
	   
	 
}
