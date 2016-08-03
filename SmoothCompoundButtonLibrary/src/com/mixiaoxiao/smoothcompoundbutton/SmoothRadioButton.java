package com.mixiaoxiao.smoothcompoundbutton;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewParent;

public class SmoothRadioButton extends SmoothCompoundButton{

	public SmoothRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SmoothRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SmoothRadioButton(Context context) {
		super(context);
	}

	@Override
	protected SmoothMarkDrawer makeSmoothMarkDrawer(Context context, int colorOn, int colorOff) {
		return new SmoothMarkDrawerRadioButton(context, colorOn, colorOff);
	}
	
	@Override
    public void toggle() {
		ViewParent viewParent = getParent();
		if(viewParent != null && viewParent instanceof SmoothRadioGroup){
			// we override to prevent toggle when the radio is already
	        // checked (as opposed to check boxes widgets)
	        if (!isChecked()) {
	            super.toggle();
	        }
		}else{
			//如果不在SmoothRadioGroup中则允许正常的toogle
			super.toggle();
		}
    }
	
}
