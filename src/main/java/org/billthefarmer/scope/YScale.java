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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

// YScale
public class YScale extends View
{
    private static final int WIDTH_FRACTION = 24;

    private int width;
    private int height;
    private int textColour;

    protected float index;

    private Matrix matrix;
    private Paint paint;
    private Path thumb;

    // YScale
    @SuppressWarnings("deprecation")
    public YScale(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        Resources resources = getResources();

        final TypedArray typedArray =
            context.obtainStyledAttributes(attrs, R.styleable.Scope);

        textColour =
            typedArray.getColor(R.styleable.Scope_TextColour,
                                resources.getColor(android.R.color.black));
        typedArray.recycle();

        // Create paint
        matrix = new Matrix();
        paint = new Paint();
    }

    // onMeasure
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Get offered dimension
        int d = Math.min(MeasureSpec.getSize(heightMeasureSpec),
                         MeasureSpec.getSize(widthMeasureSpec));

        // Set wanted dimensions
        setMeasuredDimension(d / WIDTH_FRACTION, d);
    }

    // onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        // Get actual dimensions
        width = w;
        height = h;

        // Create a path for the thumb
        thumb = new Path();

        thumb.moveTo(-1, -1);
        thumb.lineTo(-1, 1);
        thumb.lineTo(1, 1);
        thumb.lineTo(2, 0);
        thumb.lineTo(1, -1);
        thumb.close();

        // Create a matrix to scale the thumb
        matrix.setScale(width / 4, width / 4);

        // Scale the thumb
        thumb.transform(matrix);
    }

    // onDraw
    @Override
    protected void onDraw(Canvas canvas)
    {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(textColour);

        canvas.translate(0, height / 2);

        // Draw scale ticks
        for (int i = 0; i < height / 2; i += Main.SIZE)
        {
            canvas.drawLine(width * 2 / 3, i, width, i, paint);
            canvas.drawLine(width * 2 / 3, -i, width, -i, paint);
        }

        for (int i = 0; i < height / 2; i += Main.SIZE * 5)
        {
            canvas.drawLine(width / 3, i, width, i, paint);
            canvas.drawLine(width / 3, -i, width, -i, paint);
        }

        // Draw sync level thumb if not zero
        if (index != 0)
        {
            canvas.translate(width / 3, index);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(thumb, paint);
        }
    }

    // On touch event
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        // Set the index from the touch dimension
        switch (event.getAction())
        {
        case MotionEvent.ACTION_DOWN:
            index = y - (height / 2);
            break;

        case MotionEvent.ACTION_MOVE:
            index = y - (height / 2);
            break;

        case MotionEvent.ACTION_UP:
            index = y - (height / 2);
            break;
        }

        invalidate();
        return true;
    }
}
