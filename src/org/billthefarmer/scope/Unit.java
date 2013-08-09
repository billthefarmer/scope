package org.billthefarmer.scope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class Unit extends View
{
    protected float scale;

    private int width;
    private int height;

    private Paint paint;

    public Unit(Context context, AttributeSet attrs)
    {
	super(context, attrs);

	paint = new Paint();

	scale = 1;
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

	canvas.drawLine(width, 0, width, height / 2, paint);

	paint.setAntiAlias(true);
	paint.setTextSize(height * 2 / 3);
	paint.setTextAlign(Align.CENTER);

	if (scale < 100.0)
	    canvas.drawText("ms", width, height - (height / 8), paint);

	else
	    canvas.drawText("sec", width, height - (height / 8), paint);
    }
}
