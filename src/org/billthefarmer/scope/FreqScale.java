////////////////////////////////////////////////////////////////////////////////
//
//  Scope - An Android scope written in Java.
//
//  Copyright (C) 2014	Bill Farmer
//
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 3 of the License, or
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class FreqScale extends View
{
    private static final int HEIGHT_FRACTION = 32;
    private int width;
    private int height;

    private Paint paint;

    protected SpectrumActivity.Audio audio;

    public FreqScale(Context context, AttributeSet attrs)
    {
	super(context, attrs);

	// Create paint

	paint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
	super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	// Get offered dimension

	int w = MeasureSpec.getSize(widthMeasureSpec);

	// Set wanted dimensions

	setMeasuredDimension(w, w / HEIGHT_FRACTION);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
	super.onSizeChanged(w, h, oldw, oldh);

	// Get actual dimensions

	width = w;
	height = h;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas)
    {
	// Check for data

	if ((audio != null) && (audio.xa != null))
	{
	    // Calculate scale

	    float scale = (float) Math.log10(audio.xa.length) / (float) width;

	    // Set up paint

	    paint.setStrokeWidth(2);
	    paint.setColor(Color.BLACK);
	    paint.setAntiAlias(true);
	    paint.setTextSize(height * 2 / 3);
	    paint.setTextAlign(Paint.Align.CENTER);

	    // Draw ticks

	    canvas.drawLine(0, 0, 0, height / 3, paint);

	    float fa[] = {1, 2, 5};
	    float sa[] = {1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f, 1.7f, 1.8f,
			  1.9f, 2.2f, 2.5f, 3, 3.5f, 4, 4.5f, 6, 7, 8, 9};
	    float ma[] = {1, 10, 100, 1000, 10000};
	    for (float m: ma)
	    {
		for (float f: fa)
		{
		    float x = (float) Math.log10((f * m) / audio.fps) / scale;
		    canvas.drawLine(x, 0, x, height / 3, paint);

		    String s;
		    if (m >= 1000)
			s = String.format("%1.0fK", f * m / 1000);

		    else
			s = String.format("%1.0f", f * m);

		    canvas.drawText(s, x, height - (height / 6), paint);
		}

		for (float s: sa)
		{
		    float x = (float) Math.log10((s * m) / audio.fps) / scale;
		    canvas.drawLine(x, 0, x, height / 4, paint);
		}
	    }
	}

	canvas.drawText("freq", 0, height - (height / 6), paint);
    }
}
