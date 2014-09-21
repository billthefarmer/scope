////////////////////////////////////////////////////////////////////////////////
//
//  Scope - An Android scope written in Java.
//
//  Copyright (C) 2013	Bill Farmer
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Bill Farmer	 william j farmer [at] yahoo [dot] co [dot] uk.
//
///////////////////////////////////////////////////////////////////////////////

package org.billthefarmer.scope;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Scope extends View
{
    private static final int SIZE = 20;
    private static final float SMALL_SCALE = 200;
    private static final float LARGE_SCALE = 200000;

    private int width;
    private int height;

    private Path path;
    private Canvas cb;
    private Paint paint;
    private Bitmap bitmap;
    private Bitmap graticule;

    protected boolean storage;
    protected boolean clear;

    protected float step;
    protected float scale;
    protected float start;
    protected float index;

    protected MainActivity main;
    protected MainActivity.Audio audio;

    public Scope(Context context, AttributeSet attrs)
    {
	super(context, attrs);

	path = new Path();
	paint = new Paint();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
	super.onSizeChanged(w, h, oldw, oldh);

	width = w;
	height = h;

	bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	cb = new Canvas(bitmap);

	graticule = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
	Canvas canvas = new Canvas(graticule);

	canvas.drawColor(Color.BLACK);

	paint.setStrokeWidth(2);
	paint.setStyle(Paint.Style.STROKE);
	paint.setColor(Color.argb(255, 0, 63, 0));

	// Draw graticule

	for (int i = 0; i < width; i += SIZE)
	    canvas.drawLine(i, 0, i, height, paint);

	canvas.translate(0, height / 2);

	for (int i = 0; i < height / 2; i += SIZE)
	{
	    canvas.drawLine(0, i, width, i, paint);
	    canvas.drawLine(0, -i, width, -i, paint);
	}

	cb.drawBitmap(graticule, 0, 0, null);

	cb.translate(0, height / 2);
    }

    private int max;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas)
    {
	// Check for data

	if ((audio == null) || (audio.data == null))
	{
	    canvas.drawBitmap(graticule, 0, 0, null);
	    return;
	}

	if (!storage || clear)
	{
	    cb.drawBitmap(graticule, 0, -height / 2, null);
	    clear = false;
	}

	// Calculate scale etc

	float xscale = (float)(1.0 / ((audio.sample / 100000.0) * scale));
	int xstart = Math.round(start);
	int xstep = Math.round((float)1.0 / xscale);
	int xstop = Math.round(xstart + ((float)width / xscale));

	if (xstop > audio.length)
	    xstop = (int)audio.length;

	// Calculate scale

	if (max < 4096)
	    max = 4096;

	float yscale = (float)(max / (height / 2.0));

	max = 0;

	// Draw the trace

	path.rewind();
	path.moveTo(0, 0);

	if (xscale < 1.0)
	{
	    for (int i = 0; i <= xstop - xstart; i += xstep)
	    {
		if (max < Math.abs(audio.data[i + xstart]))
		    max = Math.abs(audio.data[i + xstart]);

		float x = (float)i * xscale;
		float y = -(float)audio.data[i + xstart] / yscale;
		path.lineTo(x, y);
	    }
	}

	else
	{
	    for (int i = 0; i <= xstop - xstart; i++)
	    {
		if (max < Math.abs(audio.data[i + xstart]))
		    max = Math.abs(audio.data[i + xstart]);

		float x = (float)i * xscale;
		float y = -(float)audio.data[i + xstart] / yscale;
		path.lineTo(x, y);

		// Draw points at max resolution

		if (main.timebase == 0)
		{
		    path.addRect(x - 2, y - 2, x + 2, y + 2, Path.Direction.CW);
		    path.moveTo(x, y);
		}
	    }
	}

	paint.setColor(Color.GREEN);
	cb.drawPath(path, paint);

	// Draw index

	if (index > 0 && index < width)
	{
	    paint.setColor(Color.YELLOW);
	    paint.setTextAlign(Paint.Align.LEFT);
	    cb.drawLine(index, -height / 2, index, height / 2, paint);

	    int i = Math.round((float)index / xscale);
	    float y = -audio.data[i + xstart] / yscale;

	    String s = String.format("%3.2f", audio.data[i + xstart] / 32768.0);
	    cb.drawText(s, index, y, paint);

	    paint.setTextAlign(Paint.Align.CENTER);

	    if (scale < 100.0)
	    {
		s = String.format((scale < 1.0)? "%3.3f": 
				  (scale < 10.0)? "%3.2f": "%3.1f",
				  (start + (index * scale)) / SMALL_SCALE);
		cb.drawText(s, index, height / 2, paint);
	    }

	    else
	    {
		s = String.format("%3.3f", (start + (index *
						     scale)) / LARGE_SCALE);
		cb.drawText(s, index, height / 2, paint);
	    }
	}

	canvas.drawBitmap(bitmap, 0, 0, null);
    }

    // On touch event

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
	float x = event.getX();
	float y = event.getY();

	switch (event.getAction())
	{
	case MotionEvent.ACTION_DOWN:
	    index = x;
	    break;

	case MotionEvent.ACTION_MOVE:
	    index = x;
	    break;

	case MotionEvent.ACTION_UP:
	    index = x;
	    break;
	}

	return true;
    }
}
