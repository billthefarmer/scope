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
    }

    // On draw

    @SuppressLint("DefaultLocale")
    @Override
    protected void onDraw(Canvas canvas)
    {
	// Check for data

	if ((audio == null) || (audio.xa == null))
	{
	    canvas.drawColor(Color.BLACK);
	    return;
	}

	// Calculate x scale

	float xscale = (float)Math.log10(audio.xa.length) / width;

	// Create graticule

	if (graticule == null || graticule.getWidth() != width ||
	    graticule.getHeight() != height)
	{
	    // Create a bitmap for the graticule

	    graticule = Bitmap.createBitmap(width, height,
					    Bitmap.Config.ARGB_8888);
	    Canvas c = new Canvas(graticule);

	    // Black background

	    c.drawColor(Color.BLACK);

	    // Set up paint

	    paint.setStrokeWidth(2);
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setColor(Color.argb(255, 0, 63, 0));

	    // Draw graticule

	    float fa[] = {1, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f, 1.7f, 1.8f,
			  1.9f, 2, 2.2f, 2.5f, 3, 3.5f, 4, 4.5f, 5, 6, 7, 8, 9};
	    float ma[] = {1, 10, 100, 1000, 10000};
	    for (float m: ma)
	    {
		for (float f: fa)
		{
		    float x = (float) Math.log10((f * m) / audio.fps) / xscale;
		    c.drawLine(x, 0, x, height, paint);
		}
	    }

	    for (int i = 0; i < height; i += MainActivity.SIZE)
	    {
		c.drawLine(0, i, width, i, paint);
	    }
	}

	canvas.translate(0, height);
	canvas.scale(1, -1);

	// Draw the graticule

	canvas.drawBitmap(graticule, 0, 0, null);

	// Chack max value

	if (max < 1.0f)
	    max = 1.0f;

	// Calculate the scaling

	float yscale = (height / max);

	max = 0.0f;

	// Rewind path

	path.rewind();
	path.moveTo(0, 0);

	// Create trace

	int last = 1;
	for (int x = 0; x < width; x++)
	{
	    float value = 0.0f;

	    int index = (int)Math.round(Math.pow(10, x * xscale));
	    for (int i = last; i < index; i++)
	    {
		// Don't show DC component and don't overflow

		if (i > 0 && i < audio.xa.length)
		{
		    if (value < audio.xa[i])
			value = (float)audio.xa[i];
		}
	    }

	    // Update last index

	    last = index;

	    // Get max value

	    if (max < value)
		max = value;

	    float y = value * yscale;

	    path.lineTo(x, y);
	}

	// Color green

	paint.setColor(Color.GREEN);
	paint.setAntiAlias(true);

	// Draw path

	canvas.drawPath(path, paint);

	if (audio.frequency > 0)
	{
	    // Yellow pen for frequency trace

	    paint.setColor(Color.YELLOW);

	    // Create line for frequency

	    float x = (float)Math.log10(audio.frequency / audio.fps) / xscale;
	    paint.setAntiAlias(false);
	    canvas.drawLine(x, 0, x, height / 4, paint);

	    // Draw frequency value

	    canvas.scale(1, -1);
	    String s = String.format("%1.1fHz", audio.frequency);
	    paint.setTextSize(height / 48);
	    paint.setTextAlign(Paint.Align.CENTER);
	    paint.setAntiAlias(true);
	    canvas.drawText(s, x, 0, paint);
	}
    }
}
