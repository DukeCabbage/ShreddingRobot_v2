package com.gloomy.ShreddingRobot.Widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.gloomy.ShreddingRobot.R;

/**
 * Created by Leo on 26/01/2015.
 */
public class MeinTextView extends TextView {

    private String fontPath;

    public MeinTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.MeinTextView, 0, 0);
        fontPath = a.getString(R.styleable.MeinTextView_fontPath);
        setFont();
    }

    public void setFont() {
        if (fontPath==null){
            fontPath="fonts/RionaSansMedium.otf";
        }
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), fontPath);
        this.setTypeface(tf);
    }
}