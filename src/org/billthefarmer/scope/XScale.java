package org.billthefarmer.scope;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

public class XScale extends View
{
    private int width;
    private int height;

	public XScale(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int w = MeasureSpec.getSize(widthMeasureSpec);

		setMeasuredDimension(w, w / 32);
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
	super.onSizeChanged(w, h, oldw, oldh);

	width = w;
	height = h;
    }
	
}
