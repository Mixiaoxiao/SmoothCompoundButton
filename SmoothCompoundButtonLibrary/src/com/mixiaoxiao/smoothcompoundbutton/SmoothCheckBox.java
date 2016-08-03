package com.mixiaoxiao.smoothcompoundbutton;

import android.content.Context;
import android.util.AttributeSet;

public class SmoothCheckBox extends SmoothCompoundButton{

	public SmoothCheckBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SmoothCheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SmoothCheckBox(Context context) {
		super(context);
	}

	@Override
	protected SmoothMarkDrawer makeSmoothMarkDrawer(Context context, int colorOn, int colorOff) {
		return new SmoothMarkDrawerCheckBox(context, colorOn, colorOff);
	}
	
}
