////////////////////////////////////////////////////////////////////////////////
//
//  Scope - An Android scope written in Java.
//
//  Copyright (C) 2014	Bill Farmer
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

// Spectrum

public class Spectrum extends View
{
    private int width;
    private int height;

    private Path path;
    private Paint paint;

    private Bitmap graticule;

    private float max;

    protected SpectrumActivity.Audio audio;

    // Spectrum

    public Spectrum(Context context, AttributeSet attrs)
    {
	super(context, attrs);

	// Create path and paint

	path = new Path();
	paint = new Paint();
    }

    // On size changed

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
	super.onSizeChanged(w, h, oldw, oldh);

	// Get dimensions

	width = w;
	height = h;

	// Create a bitmap for the graticule

	graticule = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);
	Canvas canvas = new Canvas(graticule);

	// Black background

	canvas.drawColor(Color.BLACK);

	// Set up paint

	paint.setStrokeWidth(2);
	paint.setStyle(Paint.Style.STROKE);
	paint.setColor(Color.argb(255, 0, 63, 0));

	// Draw graticule

	for (int i = 0; i < width; i += MainActivity.SIZE)
	    canvas.drawLine(i, 0, i, height, paint);

	for (int i = 0; i < height; i += MainActivity.SIZE)
	{
	    canvas.drawLine(0, i, width, i, paint);
	}
    }

    // On draw

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas)
    {
	canvas.translate(0, height);
	canvas.scale(1, -1);

	// Draw the graticule

	canvas.drawBitmap(graticule, 0, 0, null);

	// Check for data

	if ((audio == null) || (audio.xa == null))
	    return;

	// Chack max value

	if (max < 1.0f)
	    max = 1.0f;

	// Calculate the scaling

	float yscale = (height / max);

	max = 0.0f;

	// Rewind path

	path.rewind();
	path.moveTo(0, 0);

	// Calculate x scale

	float xscale = (float)audio.xa.length / (float)width;

	// Create trace

	for (int x = 0; x < width; x++)
	{
	    float value = 0.0f;

	    // Don't show DC component

	    if (x > 0)
	    {
		// Find max value for each vertex

		for (int j = 0; j < xscale; j++)
		{
		    int n = (int)(x * xscale) + j;

		    if (value < audio.xa[n])
			value = (float)audio.xa[n];
		}
	    }

	    // Get max value

	    if (max < value)
		max = value;

	    float y = value * yscale;

	    path.lineTo(x, y);
	}

	// Color green

	paint.setColor(Color.GREEN);

	// Draw path

	canvas.drawPath(path, paint);

	if (audio.frequency > 0)
	{
	    // Yellow pen for frequency trace

	    paint.setColor(Color.YELLOW);
	    paint.setTextSize(height / 48);
	    paint.setTextAlign(Paint.Align.CENTER);

	    // Create line for frequency

	    float x = (float)(audio.frequency / audio.fps / xscale);
	    canvas.drawLine(x, 0, x, height / 4, paint);

	    // Draw frequency value

	    canvas.scale(1, -1);
	    String s = String.format("%1.1fHz", audio.frequency);
	    canvas.drawText(s, x, 0, paint);
	}
    }
}
