package org.billthefarmer.scope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class YScale extends View
{
	private static final int SIZE = 20;

    private int width;
    private int height;

    private Paint paint;

    public YScale(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub

		paint = new Paint();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int h = MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(h / 24, h);
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
	super.onSizeChanged(w, h, oldw, oldh);

	width = w;
	height = h;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
    	paint.setStrokeWidth(2);

    	for (int i = (height % SIZE) / 2; i < height; i += SIZE)
    		canvas.drawLine(width * 2 / 3, i, width, i, paint);

    	for (int i = (height % SIZE) / 2; i < height; i += SIZE * 5)
    		canvas.drawLine(width / 3, i, width, i, paint);
    }
}
