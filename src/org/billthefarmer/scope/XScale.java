package org.billthefarmer.scope;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class XScale extends View
{
    private static final int SIZE = 20;
    private static final float SMALL_SCALE = 200;
    private static final float LARGE_SCALE = 200000;

    protected float step;
    protected float scale;
    protected float start;

    private int width;
    private int height;

    private Paint paint;

    public XScale(Context context, AttributeSet attrs)
    {
	super(context, attrs);

	paint = new Paint();

	start = 0;
	scale = 1;
	step = 10;
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

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas)
    {
	paint.setStrokeWidth(2);
	paint.setColor(Color.BLACK);

	for (int i = 0; i < width; i += SIZE)
	    canvas.drawLine(i, 0, i, height / 4, paint);

	for (int i = 0; i < width; i += SIZE * 5)
	    canvas.drawLine(i, 0, i, height / 3, paint);

	paint.setAntiAlias(true);
	paint.setTextSize(height * 2 / 3);
	paint.setTextAlign(Paint.Align.CENTER);

	if (scale < 100.0)
	{
	    canvas.drawText("ms", 0, height - (height / 8), paint);

	    for (int i = SIZE * 10; i < width; i += SIZE * 10)
	    {
		String s = String.format("%1.1f", (start + (i * scale)) /
					 SMALL_SCALE);
		canvas.drawText(s, i, height - (height / 8), paint);
	    }
	}

	else
	{
	    canvas.drawText("sec", 0, height - (height / 8), paint);
 
	    for (int i = SIZE * 10; i < width; i += SIZE * 10)
	    {
		String s = String.format("%1.1f", (start + (i * scale)) /
					 LARGE_SCALE);
		canvas.drawText(s, i, height - (height / 8), paint);
	    }
	}
    }
}
