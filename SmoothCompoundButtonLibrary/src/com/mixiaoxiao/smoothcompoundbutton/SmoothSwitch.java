package com.mixiaoxiao.smoothcompoundbutton;

import android.content.Context;
import android.util.AttributeSet;

public class SmoothSwitch extends SmoothCompoundButton{

	public SmoothSwitch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SmoothSwitch(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SmoothSwitch(Context context) {
		super(context);
	}
	@Override
	protected SmoothMarkDrawer makeSmoothMarkDrawer(Context context, int colorOn, int colorOff) {
		//setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		return new SmoothMarkDrawerSwitch(context, colorOn, colorOff);
	}
}
