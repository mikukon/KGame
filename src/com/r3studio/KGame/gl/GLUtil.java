package com.r3studio.KGame.gl;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;
import android.widget.LinearLayout;

public class GLUtil {
	public static Bitmap viewToBmp(View v){
        Bitmap bm = null;
        if(v==null)
            return bm;

        LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams) v.getLayoutParams();

        if(lp==null){
            lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            v.setLayoutParams(lp);
        }

        int swd= View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int sht= View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        v.measure(swd, sht);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());

        if(v.getMeasuredWidth() > 0 && v.getMeasuredHeight() > 0){
            bm = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas can=new Canvas(bm);
            v.draw(can);
        }

        return bm;
    }
}
