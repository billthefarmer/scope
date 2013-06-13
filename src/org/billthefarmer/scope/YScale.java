package org.billthefarmer.scope;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

public class YScale extends View
{
    private int width;
    private int height;

	public YScale(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int h = MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(h / 16, h);
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
	super.onSizeChanged(w, h, oldw, oldh);

	width = w;
	height = h;
    }

}
