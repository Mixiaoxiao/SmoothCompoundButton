package com.mixiaoxiao.smoothcompoundbutton;

import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

public class SmoothViewCompat {

	private static final AtomicInteger sNextGeneratedId = (Build.VERSION.SDK_INT < 17) ?  new AtomicInteger(1) : null;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1) 
	public static int generateViewId() {
		//http://stackoverflow.com/questions/6790623/programmatic-views-how-to-set-unique-ids
		if (Build.VERSION.SDK_INT < 17) {
			for (;;) {
				final int result = sNextGeneratedId.get();
				// aapt-generated IDs have the high byte nonzero; clamp to the
				// range under that.
				int newValue = result + 1;
				if (newValue > 0x00FFFFFF)
					newValue = 1; // Roll over to 1, not 0.
				if (sNextGeneratedId.compareAndSet(result, newValue)) {
					return result;
				}
			}
		} else {
			return View.generateViewId();
		}
	}
}
