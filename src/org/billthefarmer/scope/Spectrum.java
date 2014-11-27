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

// Scope

public class Spectrum extends View
{
    private int width;
    private int height;

    private Path path;
    private Canvas cb;
    private Paint paint;

    private Bitmap graticule;

    protected MainActivity main;
    protected MainActivity.Audio audio;

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

	canvas.translate(0, height / 2);

	for (int i = 0; i < height / 2; i += MainActivity.SIZE)
	{
	    canvas.drawLine(0, i, width, i, paint);
	    canvas.drawLine(0, -i, width, -i, paint);
	}
    }

    private int max;

    // On draw

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

	// Draw the graticule on the bitmap

	canvas.drawBitmap(graticule, 0, -height / 2, null);
    }
}
