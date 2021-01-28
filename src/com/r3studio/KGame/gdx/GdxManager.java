package com.r3studio.KGame.gdx;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidGraphics;
import com.badlogic.gdx.backends.android.GdxViewManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/**
 * Created by double on 15/11/9.
 */
public class GdxManager {
    private static GdxManager manager = null;
    private DisplayMetrics metrics;
    private GdxViewManager gdxViewManager;
    public GdxViewListener gdxViewListener;
    private View gdxView;
    private Context mContext;

    private GdxManager(){

    }

    public static GdxManager getInstance(){
        if (manager == null){
            manager = new GdxManager();
        }

        return manager;
    }

    public View initForGdxView(Context context){
        if (gdxViewManager != null) {
            return gdxView;
        }
        
        mContext = context;
        
        metrics = context.getResources().getDisplayMetrics();

        gdxViewListener = new GdxViewListener(metrics.widthPixels, metrics.heightPixels);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.a = 8;
        config.r = 8;
        config.g = 8;
        config.b = 8;

        gdxViewManager = new GdxViewManager(context.getApplicationContext(), new Handler());
        gdxView = gdxViewManager.initializeForView(gdxViewListener, config);

        SurfaceView sf = (SurfaceView) ((AndroidGraphics)gdxViewManager.getGraphics()).getView();
        sf.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        sf.setZOrderMediaOverlay(true);
        sf.setZOrderOnTop(true);

        return gdxView;
    }

    public Bitmap ViewToBmp(View v){
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
    
    public Texture bmToTexture(Bitmap bitmap) {
        Texture texture = new Texture(bitmap.getWidth(), bitmap.getHeight(), Pixmap.Format.RGBA8888);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture.getTextureObjectHandle());
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        return texture;
    }
}
