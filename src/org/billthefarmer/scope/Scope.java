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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class Scope extends View
{
	private static final int SIZE = 20;

    private int width;
    private int height;
    
    Paint paint;

    protected MainActivity.Audio audio;

    public Scope(Context context, AttributeSet attrs)
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
    	canvas.drawColor(Color.BLACK);

    	paint.setStrokeWidth(2);
    	paint.setColor(Color.argb(255, 0, 63, 0));

    	for (int i = 0; i < width; i += SIZE)
    		canvas.drawLine(i, 0, i, height, paint);

    	for (int i = (height % SIZE) / 2; i < height; i += SIZE)
    		canvas.drawLine(0, i, width, i, paint);

    	canvas.translate(0, height / 2);

    	paint.setColor(Color.GREEN);
    	canvas.drawLine(0, 0, width, 0, paint);
    }
}
