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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Locale;

// XScale
public class XScale extends View
{
    private static final int HEIGHT_FRACTION = 32;

    protected float step;
    protected float scale;
    protected float start;

    private int width;
    private int height;

    private Paint paint;

    // XScale
    public XScale(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        // Create paint
        paint = new Paint();

        // Set initial values
        start = 0;
        scale = 1;
        step = 10;
    }

    // onMeasure
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get offered dimension
        int w = MeasureSpec.getSize(widthMeasureSpec);

        // Set wanted dimensions
        setMeasuredDimension(w, w / HEIGHT_FRACTION);
    }

    // onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        // Get actual dimensions
        width = w;
        height = h;
    }

    // onDraw
    @Override
    protected void onDraw(Canvas canvas)
    {
        // Set up paint
        paint.setStrokeWidth(2);
        paint.setColor(Color.BLACK);

        // Draw ticks
        for (int i = 0; i < width; i += MainActivity.SIZE)
            canvas.drawLine(i, 0, i, height / 4, paint);

        for (int i = 0; i < width; i += MainActivity.SIZE * 5)
            canvas.drawLine(i, 0, i, height / 3, paint);

        // Set up paint
        paint.setAntiAlias(true);
        paint.setTextSize(height * 2 / 3);
        paint.setTextAlign(Paint.Align.CENTER);

        // Draw scale
        if (scale < 100.0)
        {
            canvas.drawText("ms", 0, height - (height / 6), paint);

            for (int i = MainActivity.SIZE * 10; i < width;
                    i += MainActivity.SIZE * 10)
            {
                String s = String.format(Locale.getDefault(),
                                         "%1.1f", (start + (i * scale)) /
                                         MainActivity.SMALL_SCALE);
                canvas.drawText(s, i, height - (height / 8), paint);
            }
        }

        else
        {
            canvas.drawText("sec", 0, height - (height / 6), paint);

            for (int i = MainActivity.SIZE * 10; i < width;
                    i += MainActivity.SIZE * 10)
            {
                String s = String.format(Locale.getDefault(),
                                         "%1.1f", (start + (i * scale)) /
                                         MainActivity.LARGE_SCALE);
                canvas.drawText(s, i, height - (height / 8), paint);
            }
        }
    }
}
