package com.r3studio.KGame.gl;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.r3studio.KGame.KGameApplication;
import com.yiqiding.ktvbox.R;

/**
 * Created by double on 16/6/20.
 */
public class GLDaFenView extends Container {
    private class LineData {
        float time;
        float duration;
        float pitch;
        long startMs;
        LineData(float time, float duration, float pitch){
            this.time = time;
            this.duration = duration;
            this.pitch = pitch;
        }
		@Override
		public String toString() {
			return "LineData [time=" + time + ", duration=" + duration
					+ ", pitch=" + pitch + ", startMs=" + startMs + "]";
		}
    }

    private class ShownItem{
        LineData data;
        GLActor actor;
    }
    private Object LK = new Object();
    private Context mContext;
    private BaseGLRender render;
    private int winWidth = 1280;
    private int winHeight = 720;
    private int bgHeight = 300;
    private int lineHeight = 10;
    private int heightOffset = 20;
    private long startTime;
    private long time;
    GLAnimaImage gltime;
    GLAnimaImage ball;
    GLAnimaImage vLine;
    float ballHeight = 0;

    private List<LineData> lineDatas = new ArrayList<LineData>();
    private List<ShownItem> shownList = new ArrayList<ShownItem>();
    private List<LineData> bingoDatas = new ArrayList<LineData>();
    private List<ShownItem> bingoList = new ArrayList<ShownItem>();

    Bitmap bm_line;
    Bitmap bm_line_bingo;
    public GLDaFenView(BaseGLRender render){
    	this.mContext = KGameApplication.getInstance();
        this.render = render;
        bm_line = getLineBitmap(false);
        bm_line_bingo = getLineBitmap(true);

        GLAnimaImage bg = new GLAnimaImage(render, getBgBitmap());
//        heightOffset = (winHeight - bgHeight) / 2 + lineHeight;
        bg.setPosition(0, heightOffset);
        addActor(bg);

        gltime = new GLAnimaImage(render, getTimeBitmap());
        gltime.setPosition(winWidth / 3, 0 + heightOffset + 350);
        gltime.setWidth(200);
        addActor(gltime);

        vLine = new GLAnimaImage(render, getVLineBitmap());
        vLine.setPosition(winWidth / 3, 0 + heightOffset);
        addActor(vLine);

        ball = new GLAnimaImage(render, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ball));
        ballHeight = heightOffset;
        ball.setPosition(winWidth / 3 - ball.getWidth() / 2, ballHeight);
    }

    private Bitmap getBgBitmap(){
        LinearLayout ll_root = new LinearLayout(mContext);
        ll_root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout ll_bg = new LinearLayout(mContext);
        ll_bg.setBackgroundColor(Color.parseColor("#b2000000"));
        ll_bg.setLayoutParams(new LinearLayout.LayoutParams(winWidth, bgHeight));
        ll_root.addView(ll_bg);
        return GLUtil.viewToBmp(ll_root);
    }

    private Bitmap getLineBitmap(boolean bingo){
        LinearLayout ll_root = new LinearLayout(mContext);
        ll_root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout ll_bg = new LinearLayout(mContext);
        ll_bg.setBackgroundColor(Color.parseColor(bingo ? "#ff0000" : "#ffffff"));
        ll_bg.setLayoutParams(new LinearLayout.LayoutParams(1, lineHeight));
        ll_root.addView(ll_bg);

        return GLUtil.viewToBmp(ll_root);
    }

    private Bitmap getVLineBitmap(){
        LinearLayout ll_root = new LinearLayout(mContext);
        ll_root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout ll_bg = new LinearLayout(mContext);
        ll_bg.setBackgroundColor(Color.parseColor("#ff00ff"));
        ll_bg.setLayoutParams(new LinearLayout.LayoutParams(2, bgHeight));
        ll_root.addView(ll_bg);

        return GLUtil.viewToBmp(ll_root);
    }

    private Bitmap getTimeBitmap(){
        TextView tv = new TextView(mContext);
        tv.setTextSize(24);
        tv.setTextColor(Color.RED);
        tv.setText("" + (time == 0 ? 999999999 : time));

        LinearLayout ll_root = new LinearLayout(mContext);
        ll_root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ll_root.addView(tv);

        return GLUtil.viewToBmp(ll_root);
    }

    @Override
    public void paint() {
        super.paint();
        synchronized (LK){
        	if (startTime != 0) {
        		time = System.currentTimeMillis() - startTime;
        		
        		initPaintList();
        		
        		for (ShownItem item:shownList) {
        			item.actor.setPosition(winWidth / 3.0f + (item.data.time - time) * winWidth / 6800.0f + 1, ((280.0f / (maxPitch - minPitch)) * (item.data.pitch - minPitch)) + heightOffset);
        			item.actor.paint();
        		}
        		
        		for (ShownItem item:bingoList) {
        			item.actor.setPosition(winWidth / 3.0f + (item.data.time - time) * winWidth / 6800.0f + 1, ((280.0f / (maxPitch - minPitch)) * (item.data.pitch - minPitch)) + heightOffset);
        			item.actor.paint();
        		}
			}

            vLine.paint();
            ball.setY(ballHeight);
            ball.paint();
//            gltime.setBitmap(getTimeBitmap());
        }
    }

    public void initPaintList(){
        List<LineData> rems = new ArrayList<LineData>();
        for (int i=0;i<lineDatas.size();i++){
            LineData data = lineDatas.get(i);
            if (time > data.time + data.duration + 6800 / 3){
                rems.add(data);
                continue;
            }

            if (time + 6800 * 2 / 3 + 60 <= data.time){
                break;
            }

            if (!isInShownList(data)){
                ShownItem tmp = new ShownItem();
                tmp.data = data;
                tmp.actor = new GLAnimaImage(render, bm_line);
                tmp.actor.setScaleX(tmp.data.duration * winWidth / 6800.0f - 2);
                shownList.add(tmp);
            }
        }
        lineDatas.removeAll(rems);

        List<ShownItem> dels = new ArrayList<ShownItem>();
        for (int i = 0; i < shownList.size(); i++) {
            ShownItem item = shownList.get(i);
            if (time > item.data.time + item.data.duration + 6800 / 3){
                item.actor.dispose();
                dels.add(item);
            }
        }
        shownList.removeAll(dels);


        List<LineData> rems2 = new ArrayList<LineData>();
        for (int i=0;i<bingoDatas.size();i++){
            LineData data = bingoDatas.get(i);
            if (time > data.time + data.duration + 6800 / 3){
                rems2.add(data);
                continue;
            }

            GLActor actor = getActorFromBingoList(data);
            if (actor == null){
                ShownItem tmp = new ShownItem();
                tmp.data = data;
                tmp.actor = new GLAnimaImage(render, bm_line_bingo);
                tmp.actor.setScaleX(tmp.data.duration * winWidth / 6800.0f - 2);
                bingoList.add(tmp);
            }else{
                actor.setScaleX(data.duration * winWidth / 6800.0f - 2);
            }
        }
        bingoDatas.removeAll(rems2);

        List<ShownItem> dels2 = new ArrayList<ShownItem>();
        for (int i = 0; i < bingoList.size(); i++) {
            ShownItem item = bingoList.get(i);
            if (time > item.data.time + item.data.duration + 6800 / 3){
                item.actor.dispose();
                dels2.add(item);
            }
        }
        bingoList.removeAll(dels2);

//        System.out.println("listData size : " + lineDatas.size() + ", showList size : " + shownList.size() + " ---- bingoDatas size : " + bingoDatas.size() + ", bingoList size : " + bingoList.size());
    }

    private boolean isInShownList(LineData data) {
        for (int i = 0; i < shownList.size(); i++) {
            ShownItem item = shownList.get(i);
            if (item.data == data) {
                return true;
            }
        }

        return false;
    }

    private GLActor getActorFromBingoList(LineData data) {
        for (int i = 0; i < bingoList.size(); i++) {
            ShownItem item = bingoList.get(i);
            if (item.data == data) {
                return item.actor;
            }
        }

        return null;
    }

    int minPitch;
    int maxPitch;
    
    public void prepare(float [] dataArr, int minPitch, int maxPitch){
    	if (startTime != 0) {
			return;
		}
    	
    	this.maxPitch = maxPitch;
    	this.minPitch = minPitch;
        
        for (int i=0;i < dataArr.length; i += 3){
        	LineData data = new LineData(dataArr[i], dataArr[i+1], dataArr[i + 2]);
        	System.out.println(data);
        	lineDatas.add(data);
//            lineDatas.add(new LineData(5000 + i * 500, 499, 10 + (i * 20) % 290));
        }
        
        System.out.println(dataArr.length + " ======== " + lineDatas.size());
    }
    
    public void start() {
    	startTime = System.currentTimeMillis();		
	}
    
    long preBingoTime = 0;
    LineData preLineData;
    boolean isConnect;

    public void updataCurPitch(int pitch){
    	if (pitch < minPitch) {
			pitch = minPitch;
		}
    	
    	if (pitch > maxPitch) {
			pitch = maxPitch;
		}
    	
        ballHeight = heightOffset + ((pitch - minPitch) * 280.0f / (maxPitch - minPitch)) - ball.getHeight() / 2  + lineHeight / 2;
        
        long curBingoTime = System.currentTimeMillis();
        LineData data = getCurData(curBingoTime - startTime);
        if (data == null) {
			return;
		}

        if (!(pitch > data.pitch - 2 && pitch < data.pitch + 2)) {
			preBingoTime = 0;
			preLineData = null;
			isConnect = false;
			return;
		}
        
        if (preBingoTime == 0 || preLineData == null || data != preLineData) {
            preBingoTime = curBingoTime;
            preLineData = data;
            isConnect = false;
            return;
        }
        
        LineData ld;
        if (isConnect){
            ld = bingoDatas.get(bingoDatas.size() - 1);
            ld.duration = (int) (curBingoTime - ld.startMs);
        }else{
            ld = new LineData((int) (preBingoTime - startTime), (int)(curBingoTime - preBingoTime), data.pitch);
            ld.startMs = curBingoTime;
            bingoDatas.add(ld);
        }

        preLineData = data;
        preBingoTime = curBingoTime;
        isConnect = true;
    }

    private LineData getCurData(long t){
        LineData ret = null;
        synchronized (LK){
            for (int i=0;i<lineDatas.size();i++){
                LineData data = lineDatas.get(i);
                if (t > data.time && t < data.time + data.duration){
                    ret = data;
                    break;
                }

                if (t + 6800 * 2 / 3 + 60 <= data.time){
                    break;
                }
            }
        }

        return ret;
    }

    @Override
    public void dispose() {
        super.dispose();
        gltime.dispose();
        ball.dispose();
        vLine.dispose();
    }
}
