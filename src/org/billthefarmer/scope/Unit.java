package org.billthefarmer.scope;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.View;

public class Unit extends View
{

	private int width;
    private int height;

    private Paint paint;

	public Unit(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub

		paint = new Paint();
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
    	paint.setTextSize(height / 2);
    	paint.setTextAlign(Align.CENTER);
    	canvas.drawText("ms", width, height - (height / 8), paint);
    }
}
