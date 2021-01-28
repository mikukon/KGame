package com.r3studio.KGame.gdx;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.LinearLayout;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.r3studio.KGame.KGameApplication;
import com.yiqiding.ktvbox.R;

import css.engine.MEngine;
import css.engine.MEngineManager;

/**
 * Created by double on 16/6/20.
 */
public class GdxDaFenView extends Table implements Disposable{
    private class LineData {
        int time;
        int duration;
        int pitch;
        boolean isGold;
        LineData(int time, int duration, int pitch){
            this.time = time;
            this.duration = duration;
            this.pitch = pitch;
        }
        @Override
        public String toString() {
            return "LineData [time=" + time + ", duration=" + duration
                    + ", pitch=" + pitch + "]";
        }
    }

    private class GoldData{
        float time;
        float duration;
        float pitch;
        int status; // 0 默认 1 吃掉 2 移动 3 消失
        GoldData(float time, float duration, float pitch){
            this.time = time;
            this.duration = duration;
            this.pitch = pitch;
        }

        @Override
        public String toString() {
            return "GoldData [time=" + time + ", duration=" + duration
                    + ", pitch=" + pitch + "]";
        }
    }

    private class ShownItem{
        LineData data;
        Image actor;
    }

    private class ShownGoldItem{
        GoldData data;
        GoldImage actor;
        boolean isAdded;
    }

    private Object LK = new Object();
    private Context mContext;
    private MEngineManager mEngineManager;

    private Texture tt_bg;
    private Texture tt_vLine;
    private Texture tt_vLine_pre;
    private Texture tt_ball;
    private Texture tt_ball_pre;
    private Texture tt_line;
    private Texture tt_line_bingo;
    private ParticleEffect effect_star;
    private Texture tt_perfect;
    private Texture tt_good;
    private Texture tt_miss;
//    private Texture tt_gold;
//    private Texture tt_gold_box;
//    private Texture tt_gold_box_pre;
//    private Texture tt_gold_count;

    private Table container;
    private Image ball;
    private Image vLine;
    private Image vLine_pre;
    private Image im_perfect;
    private Image im_good;
    private Image im_miss;
//    private Image tv_gold_count;
//    private Image im_gold_box;
//    private Image im_gold_box_pre;

    private float dur = 6800.0f;
    private float winWidth = 1920.0f;
    private float winHeight = 1080.0f;
    private float bgHeight = 300;
    private float rangeOffset;
    private float lineRange;
    private float lineHeight;
    private float goldHeight;
    private float bgOffset;
    private float ballBottom;
    private float ballTop;

    private float ballHeightOffset = 0;
    private float pgmOrgHeight;

//    private float goldTargetX = 1000;
//    private float goldTargetY = 600;
//    private int goldCount = 0;

//    private int goodIndex;

    private long startTime;
    private int minPitch;
    private int maxPitch;

    private List<LineData> lineDatas = new ArrayList<LineData>();
    private List<ShownItem> shownList = new ArrayList<ShownItem>();
    private List<LineData> bingoDatas = new ArrayList<LineData>();
    private List<ShownItem> bingoList = new ArrayList<ShownItem>();
//    private List<GoldData> goldDatas = new ArrayList<GoldData>();
//    private List<ShownGoldItem> shownGoldList = new ArrayList<ShownGoldItem>();

    public GdxDaFenView(MEngineManager mEngineManager){
        this.mEngineManager = mEngineManager;
        this.mContext = KGameApplication.getInstance();

        setSize(winWidth, winHeight);
        setPosition(0, 0);

        tt_bg = GdxManager.getInstance().bmToTexture(getBgBitmap());
        tt_vLine = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_v_line));
		tt_vLine_pre = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_v_line_pre));
        tt_ball = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_ball));
        tt_ball_pre = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_ball_pre));
//        tt_line = GdxManager.getInstance().bmToTexture(getLineBitmap(android.graphics.Color.parseColor("#ffffff")));//
        tt_line = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_line));
//        tt_line_bingo = GdxManager.getInstance().bmToTexture(getLineBitmap(android.graphics.Color.parseColor("#ff00ff")));//
        tt_line_bingo = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_line_pre));
		tt_perfect = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_perfect));
        tt_good = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_good));
        tt_miss = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_miss));
//		tt_gold = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_gold));
//        tt_gold_box = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_gold_box));
//        tt_gold_box_pre = GdxManager.getInstance().bmToTexture(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.dafen_gold_box_pre));
//        tt_gold_count = GdxManager.getInstance().bmToTexture(getGoldCountBitmap());
		effect_star = new ParticleEffect();
        effect_star.load(Gdx.files.internal("data/star.p"), Gdx.files.internal("data/"));
        effect_star.setPosition(winWidth / 3 - 10, -500);

        lineHeight = tt_line.getHeight();
        goldHeight = 0;//tt_gold.getHeight();
        rangeOffset = goldHeight / 2 + 5;
        lineRange = bgHeight - rangeOffset * 2;
        bgOffset = (winHeight - bgHeight) / 2;
        ballBottom = bgOffset - tt_ball.getHeight() / 2 + 14;
        ballTop = (winHeight + bgHeight) / 2 - tt_ball.getHeight() / 2 - 14;
        ballHeightOffset = bgOffset + rangeOffset - tt_ball.getHeight() / 2;

//        im_gold_box = new Image(tt_gold_box);
//        im_gold_box.setPosition(goldTargetX, goldTargetY);
//        addActor(im_gold_box);
//
//        im_gold_box_pre = new Image(tt_gold_box_pre);
//        im_gold_box_pre.setPosition(goldTargetX, goldTargetY);
//        setImageAlpha(im_gold_box_pre, 0);
//        addActor(im_gold_box_pre);

        Table bg = new Table();
        bg.setBackground(new TextureRegionDrawable(new TextureRegion(tt_bg)));
        bg.setSize(winWidth, bgHeight);
        bg.setPosition(0, bgOffset);
        addActor(bg);

        vLine = new Image(tt_vLine);
        vLine.setPosition(winWidth / 3 - vLine.getWidth() + 11, bg.getY() - 8);
        addActor(vLine);

        vLine_pre = new Image(tt_vLine_pre);
        vLine_pre.setPosition(winWidth / 3 - vLine_pre.getWidth() + 11, bg.getY() - 8);
        setImageAlpha(vLine_pre, 0);
        addActor(vLine_pre);

        container = new Table();
        container.setSize(winWidth, winHeight);
        container.setPosition(0, 0);
        addActor(container);

        ball = new Image(tt_ball);
        ball.setPosition(winWidth / 3 - ball.getWidth() / 2, ballBottom);
        addActor(ball);

        pgmOrgHeight = winHeight / 2 + bgHeight / 2 - 115;

        im_perfect = new Image(tt_perfect);
        im_perfect.setPosition(winWidth / 3 - im_perfect.getWidth() / 2, pgmOrgHeight);
        setImageAlpha(im_perfect, 0.0f);
        addActor(im_perfect);

        im_good = new Image(tt_good);
        im_good.setPosition(winWidth / 3 - im_good.getWidth() / 2, pgmOrgHeight);
        setImageAlpha(im_good, 0.0f);
        addActor(im_good);

        im_miss = new Image(tt_miss);
        im_miss.setPosition(winWidth / 3 - im_miss.getWidth() / 2, pgmOrgHeight);
        setImageAlpha(im_miss, 0.0f);
        addActor(im_miss);

//        tv_gold_count = new Image(tt_gold_count);
//        tv_gold_count.setPosition(goldTargetX + 70, goldTargetY + 28);
//        addActor(tv_gold_count);
    }

    private void setImageAlpha(Image image, float alpha){
        Color c = image.getColor();
        c.set(c.r, c.g, c.b, alpha);
        image.setColor(c);
    }

    int showType = -1;// 0:perfect 1:good 2:miss
    int shownType = -1;// 0:perfect 1:good 2:miss
    private void showPerfect(){
        if (shownType != -1){
            return;
        }

        showType = 0;
    }

    private void showGood(){
        if (shownType != -1){
            return;
        }

        showType = 1;
    }

    private void showMiss(){
        if (shownType != -1){
            return;
        }

        showType = 2;
    }

    private Bitmap getBgBitmap(){
        LinearLayout ll_root = new LinearLayout(mContext);
        ll_root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout ll_bg = new LinearLayout(mContext);
        ll_bg.setBackgroundColor(android.graphics.Color.parseColor("#b2000000"));
        ll_bg.setLayoutParams(new LinearLayout.LayoutParams((int) winWidth, (int) bgHeight));
        ll_root.addView(ll_bg);
        return GdxManager.getInstance().ViewToBmp(ll_root);
    }
    
    private Bitmap getLineBitmap(int color_idx){
        LinearLayout ll_root = new LinearLayout(mContext);
        ll_root.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout ll_bg = new LinearLayout(mContext);
        ll_bg.setBackgroundColor(color_idx);
        ll_bg.setLayoutParams(new LinearLayout.LayoutParams(10, 10));
        ll_root.addView(ll_bg);
        return GdxManager.getInstance().ViewToBmp(ll_root);
    }

//    private Bitmap getGoldCountBitmap(){
//        TextView tv = new TextView(mContext);
//        tv.getPaint().setFakeBoldText(true);
//        tv.setTextSize(28);
//        tv.setTextColor(android.graphics.Color.parseColor("#ffffff"));
//        tv.setText("" + goldCount);
//
//        return GdxManager.getInstance().ViewToBmp(tv);
//    }

    long preChangeTime;
    int time;
    @Override
    public void draw(Batch batch, float parentAlpha) {
//        if (preChangeTime == 0){
//            preChangeTime = System.currentTimeMillis();
//            time = 0;
//            goodIndex = 0;
//        }else{
//            time += System.currentTimeMillis() - preChangeTime;
//            preChangeTime = System.currentTimeMillis();
//        }
//
//
//        if (time >= 120){
//            goodIndex = (goodIndex + 1) % 7;
//            time = 0;
//        }

        synchronized (LK){
            updatePaintList(System.currentTimeMillis() - startTime);
        }

        if (shownType == -1 && showType != -1){
            shownType = showType;
            System.out.println("attach : " + showType);
            if (0 == showType){
                attachEngine(im_perfect);
            }else if (1 == showType){
                attachEngine(im_good);
            }else if (2 == showType){
                attachEngine(im_miss);
            }
        }

        super.draw(batch, parentAlpha);

        effect_star.draw(batch, Gdx.graphics.getDeltaTime());
    }

    private void attachEngine(final Image image){
        new MEngine().registerAccessor(new ActorMTweenAccessor())
                .type(ActorMTweenAccessor.POS_Y)
                .to(winHeight / 2 + bgHeight / 2 - 15)
                .durition(0.5f)
                .attach(image)
                .start(mEngineManager);

        new MEngine().registerAccessor(new ActorMTweenAccessor())
                .type(ActorMTweenAccessor.ALPHA)
                .to(1.0f)
                .durition(1.0f)
                .attach(image)
                .start(mEngineManager);

        new MEngine().registerAccessor(new ActorMTweenAccessor())
                .type(ActorMTweenAccessor.ALPHA)
                .to(0.0f)
                .delay(2)
                .durition(0)
                .setOnEndListener(new MEngine.OnEndListener() {
                    @Override
                    public void end(MEngine engine) {
                        image.setPosition(winWidth / 3 - im_good.getWidth() / 2, pgmOrgHeight);
                        shownType = -1;
                        showType = -1;
                    }
                })
                .attach(image)
                .start(mEngineManager);
    }

//    private void attachGoldEngine(final ShownGoldItem item){
//        new MEngine().registerAccessor(new ActorMTweenAccessor())
//                .type(ActorMTweenAccessor.POS_XY)
//                .to(goldTargetX + 29, goldTargetY + 29)
//                .durition(1.5f)
//                .attach(item.actor)
//                .setOnEndListener(new MEngine.OnEndListener() {
//                    @Override
//                    public void end(MEngine engine) {
//                        if (!item.isAdded) {
//                            item.isAdded = true;
//                            setImageAlpha(item.actor, 0);
//                            attachGoldBoxEngine();
//                            goldCount++;
//                            tt_gold_count.dispose();
//                            tt_gold_count = GdxManager.getInstance().bmToTexture(getGoldCountBitmap());
//                            tv_gold_count.setWidth(tt_gold_count.getWidth());
//                            tv_gold_count.setDrawable(new TextureRegionDrawable(new TextureRegion(tt_gold_count)));
//                            item.data.status = 3;
//                        }
//                    }
//                })
//                .start(mEngineManager);
//    }
//
//    MEngine goldBoxEngine;
//    private void attachGoldBoxEngine(){
//        if(goldBoxEngine != null){
//            goldBoxEngine.stop();
//        }
//
//        setImageAlpha(im_gold_box_pre, 1.0f);
//
//        goldBoxEngine = new MEngine().registerAccessor(new ActorMTweenAccessor())
//                .type(ActorMTweenAccessor.ALPHA)
//                .to(0.0f)
//                .durition(1.0f)
//                .setOnEndListener(new MEngine.OnEndListener() {
//                    @Override
//                    public void end(MEngine engine) {
//                    }
//                })
//                .attach(im_gold_box_pre)
//                .start(mEngineManager);
//    }

    MEngine vLineEngine;
    private void attachVLineEngine(boolean isShow){
        if(vLineEngine != null){
            vLineEngine.stop();
        }

        vLineEngine = new MEngine().registerAccessor(new ActorMTweenAccessor())
        .type(ActorMTweenAccessor.ALPHA)
        .to(isShow ? 1.0f:0.0f)
        .durition(isShow ? (1.0f - vLine_pre.getColor().a) / 1.0f * 1.0f : (vLine_pre.getColor().a - 0) / 1.0f * 1.0f)
        .setOnEndListener(new MEngine.OnEndListener() {
            @Override
            public void end(MEngine engine) {
            }
        })
        .attach(vLine_pre)
        .start(mEngineManager);
    }

    private void updatePaintList(long time){
        if (startTime == 0) {
            return;
        }

        List<LineData> rems = new ArrayList<LineData>();
        for (int i=0;i<lineDatas.size();i++){
            LineData data = lineDatas.get(i);
            if (time > data.time + data.duration + dur / 3){
                rems.add(data);
                continue;
            }

            if (time + dur * 2 / 3 + 60 <= data.time){
                break;
            }

            if (!data.isGold){
                ShownItem item = getActorFromShownList(data);
                if (item == null){
                    ShownItem tmp = new ShownItem();
                    tmp.data = data;
                    tmp.actor = new Image();
                    tmp.actor.setSize(tt_line.getWidth(), tt_line.getHeight());
                    tmp.actor.setDrawable(new NinePatchDrawable(new NinePatch(tt_line, 7, tt_line.getWidth() - 8, 2, tt_line.getHeight() - 13)));
                    float width = (tmp.data.duration * winWidth / dur - 2);
                    tmp.actor.setWidth(width > 15 ? width : 15);
                    tmp.actor.setPosition(winWidth / 3.0f + (tmp.data.time - time) * winWidth / dur + 1, bgOffset + rangeOffset - lineHeight / 2.0f + ((lineRange / (maxPitch - minPitch)) * (tmp.data.pitch - minPitch)));
                    container.addActor(tmp.actor);
                    shownList.add(tmp);
                }else{
                    item.actor.setPosition(winWidth / 3.0f + (item.data.time - time) * winWidth / dur + 1, bgOffset + rangeOffset - lineHeight / 2.0f + ((lineRange / (maxPitch - minPitch)) * (item.data.pitch - minPitch)));
                }
            }
        }
        lineDatas.removeAll(rems);

        List<ShownItem> dels = new ArrayList<ShownItem>();
        for (int i = 0; i < shownList.size(); i++) {
            ShownItem item = shownList.get(i);
            if (time > item.data.time + item.data.duration + dur / 3){
                item.actor.remove();
                dels.add(item);
            }
        }
        shownList.removeAll(dels);

//        List<GoldData> rems3 = new ArrayList<GoldData>();
//        for (int i=0;i<goldDatas.size();i++){
//            GoldData data = goldDatas.get(i);
//            if (data.status != 0 || (time > data.time + data.duration + dur / 3)){
//                rems3.add(data);
//                continue;
//            }
//
//            if (time + dur * 2 / 3 + 60 <= data.time){
//                break;
//            }
//
//            ShownGoldItem item = getItemFromShownGoldList(data);
//            if (item == null){
//                ShownGoldItem tmp = new ShownGoldItem();
//                tmp.data = data;
//                tmp.actor = new GoldImage(tt_gold, 1, 7);
//                tmp.actor.setPosition(winWidth / 3.0f + (tmp.data.time - time) * winWidth / dur + 1, bgOffset + rangeOffset - goldHeight / 2.0f + ((lineRange / (maxPitch - minPitch)) * (tmp.data.pitch - minPitch)));
//                tmp.actor.setShowIndex(goodIndex);
//                container.addActor(tmp.actor);
//                shownGoldList.add(tmp);
//            }else{
//                item.actor.setPosition(winWidth / 3.0f + (item.data.time - time) * winWidth / dur + 1, bgOffset + rangeOffset - goldHeight / 2.0f + ((lineRange / (maxPitch - minPitch)) * (item.data.pitch - minPitch)));
//                item.actor.setShowIndex(goodIndex);
//            }
//        }
//        goldDatas.removeAll(rems3);
//
//        List<ShownGoldItem> dels3 = new ArrayList<ShownGoldItem>();
//        for (int i = 0; i < shownGoldList.size(); i++) {
//            ShownGoldItem item = shownGoldList.get(i);
//            if (item.data.status == 1){
//                item.data.status = 2;
//                attachGoldEngine(item);
//            }else if (item.data.status == 3 || (time > (item.data.time + item.data.duration + dur / 3))){
//                item.actor.remove();
//                dels3.add(item);
//            }
//        }
//        shownGoldList.removeAll(dels3);


        List<LineData> rems2 = new ArrayList<LineData>();
        for (int i=0;i<bingoDatas.size();i++){
            LineData data = bingoDatas.get(i);
            if (time > data.time + data.duration + dur / 3){
                rems2.add(data);
                continue;
            }

            ShownItem item = getActorFromBingoList(data);
            if (item != null){
                float width = (item.data.duration * winWidth / dur - 2);
                item.actor.setWidth(width > 15 ? width : 15);
                item.actor.setPosition(winWidth / 3.0f + (item.data.time - time) * winWidth / dur + 1, bgOffset + rangeOffset - lineHeight / 2.0f + ((lineRange / (maxPitch - minPitch)) * (item.data.pitch - minPitch)));
            }else {
                ShownItem tmp = new ShownItem();
                tmp.data = data;
                tmp.actor = new Image();
                tmp.actor.setSize(tt_line_bingo.getWidth(), tt_line_bingo.getHeight());
                tmp.actor.setDrawable(new NinePatchDrawable(new NinePatch(tt_line_bingo, 7, tt_line_bingo.getWidth() - 8, 3, tt_line_bingo.getHeight() - 12)));
                float width = (tmp.data.duration * winWidth / dur - 2);
                tmp.actor.setWidth(width > 15 ? width : 15);
                tmp.actor.setPosition(winWidth / 3.0f + (tmp.data.time - time) * winWidth / dur + 1, bgOffset + rangeOffset - lineHeight / 2.0f + ((lineRange / (maxPitch - minPitch)) * (tmp.data.pitch - minPitch)));
                container.addActor(tmp.actor);
                bingoList.add(tmp);
            }
        }
        bingoDatas.removeAll(rems2);

        List<ShownItem> dels2 = new ArrayList<ShownItem>();
        for (int i = 0; i < bingoList.size(); i++) {
            ShownItem item = bingoList.get(i);
            if (time > item.data.time + item.data.duration + dur / 3){
                item.actor.remove();
                dels2.add(item);
            }
        }
        bingoList.removeAll(dels2);

//        System.out.println("listData size : " + lineDatas.size() + ", showList size : " + shownList.size() +
//                ", goldData size : " + goldDatas.size() + ", showGoldList size : " + shownGoldList.size() +
//                " ---- bingoDatas size : " + bingoDatas.size() + ", bingoList size : " + bingoList.size());

        if (isConnect) {
            ball.setDrawable(new TextureRegionDrawable(new TextureRegion(tt_ball_pre)));
            attachVLineEngine(true);
            effect_star.setPosition(winWidth / 3 - 10, ball.getY() + ball.getHeight() / 2.0f);
        }else{
            ball.setDrawable(new TextureRegionDrawable(new TextureRegion(tt_ball)));
            attachVLineEngine(false);
            effect_star.setPosition(winWidth / 3 - 10, -500);
        }
    }

    private ShownItem getActorFromShownList(LineData data) {
        for (int i = 0; i < shownList.size(); i++) {
            ShownItem item = shownList.get(i);
            if (item.data == data) {
                return item;
            }
        }

        return null;
    }

//    private ShownGoldItem getItemFromShownGoldList(GoldData data) {
//        for (int i = 0; i < shownGoldList.size(); i++) {
//            ShownGoldItem item = shownGoldList.get(i);
//            if (item.data == data) {
//                return item;
//            }
//        }
//
//        return null;
//    }

    private ShownItem getActorFromBingoList(LineData data) {
        for (int i = 0; i < bingoList.size(); i++) {
            ShownItem item = bingoList.get(i);
            if (item.data == data) {
                return item;
            }
        }

        return null;
    }

    public void prepare(float [] dataArr, int minPitch, int maxPitch){
    	this.maxPitch = maxPitch;
    	this.minPitch = minPitch;
        
        for (int i=0;i < dataArr.length; i += 3){
        	LineData data = new LineData((int)dataArr[i], (int)dataArr[i+1], (int)dataArr[i + 2]);
        	System.out.println(data);
        	lineDatas.add(data);
//			if (i % 5 == 0 || i % 7 == 0){
//                data.isGold = true;
//                if (data.duration * winWidth / dur > tt_gold.getWidth() / 7.0f + 8){
//                    int count = (int) ((data.duration * winWidth / dur) / (tt_gold.getWidth() / 7.0f + 8));
//                    float itemDur = data.duration / count;
//                    float start = (itemDur - tt_gold.getWidth() / 7.0f / winWidth * dur) / 2.0f;
//                    for (int j=0;j<count;j++){
//                        GoldData g = new GoldData(data.time + start + itemDur * j, itemDur, data.pitch);
//                        System.out.println(g);
//                        goldDatas.add(g);
//                    }
//                }
//            }
        }
        
//        System.out.println(dataArr.length + " ======== " + lineDatas.size() + " ========== " + goldDatas.size());
    }
    
    public void start() {
    	startTime = System.currentTimeMillis();		
	}
    
    public void stop() {
    	preBingoTime = 0;
        preLineData = null;
        isConnect = false;
        moveBall(0);
	}
    
    private long preBingoTime = 0;
    private LineData preLineData;
    private boolean isConnect;
    private int prePitch;
    private MEngine ballMoveEngine;
    private int updateCount;
    private int bingoCount;

    int iii;
    
    private void moveBall(int pitch) {
    	float ballHeight = ballBottom;
        if (pitch < minPitch){
            ballHeight = ballBottom;
        }else if (pitch > maxPitch){
            ballHeight = ballTop;
        }else{
            ballHeight = ballHeightOffset + ((pitch - minPitch) * lineRange / (maxPitch - minPitch));
        }

        if (ballMoveEngine != null){
            ballMoveEngine.stop();
        }

        ballMoveEngine = new MEngine()
                .registerAccessor(new ActorMTweenAccessor())
                .type(ActorMTweenAccessor.POS_Y)
                .to(ballHeight)
                .durition(0.13f)
                .attach(ball)
                .setOnEndListener(new MEngine.OnEndListener() {
                    @Override
                    public void end(MEngine e) {
                        ballMoveEngine = null;
                    }
                })
                .start(mEngineManager);
	}
    
    public void updateCurPitch(Long curBingoTime, int pitch){
        synchronized (LK){
            LineData data = getCurData(curBingoTime - startTime);
          if (data != null) {
//              if (iii  < 200){
//                  pitch = (int) data.pitch;
//              }else if (iii < 300){
//                  pitch = 0;
//              }else if (iii > 400){
//                  iii = 0;
//              }
          }

          iii++;
          
            if (pitch != prePitch){
                prePitch = pitch;

                moveBall(pitch);
            }
            
            if (data != null){
                if (updateCount == 100){
                    if (bingoCount > 80){
                        showPerfect();
                    }else if (bingoCount > 60){
                        showGood();
                    }else if (bingoCount < 50 && ((data == null) || !(pitch >= data.pitch - 2 && pitch <= data.pitch + 2))){
                        showMiss();
                    }

                    bingoCount = 0;
                    updateCount = 0;
                }else{
                    updateCount++;
                }
            }

            if ((data == null) || !(pitch >= data.pitch - 2 && pitch <= data.pitch + 2)) {
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

            bingoCount++;

            if (data.isGold){
//                eatGold(preBingoTime, curBingoTime);
            }else{
                LineData ld;
                if (isConnect){
                    ld = bingoDatas.get(bingoDatas.size() - 1);
                    if ((data.time + data.duration) - (curBingoTime - startTime) < 30){
                        ld.duration = data.time + data.duration - ld.time;
                    }else{
                        ld.duration = (int) ((curBingoTime - startTime) - ld.time);
                    }
                    
                    if (ld.time + ld.duration > data.time + data.duration) {
                    	System.out.println("data.time : " + data.time + " , data.dur : " + data.duration + "_______old______ld.time : " + ld.time + ", ld.dur : " + ld.duration);
					}
                }else{
                    int time;
                    int dur;
                    if (preBingoTime - startTime - data.time < 30){
                        time = data.time;
                    }else{
                        time = (int) (preBingoTime - startTime);
                    }

                    if ((data.time + data.duration) - (curBingoTime - startTime) < 30){
                        dur = (int) (data.time + data.duration - (preBingoTime - startTime));
                    }else{
                        dur = (int) (curBingoTime - preBingoTime);
                    }

                    ld = new LineData(time, dur, data.pitch);
                    
                    if (ld.time + ld.duration > data.time + data.duration) {
                    	System.out.println("data.time : " + data.time + " , data.dur : " + data.duration + "______new _______ld.time : " + ld.time + ", ld.dur : " + ld.duration);
                    }
                    
                    bingoDatas.add(ld);
                }
            }

            preLineData = data;
            preBingoTime = curBingoTime;
            isConnect = true;
        }
    }

//    private void eatGold(long preBingoTime, long curBingTime){
//        for (int i=0;i<goldDatas.size();i++){
//            GoldData data = goldDatas.get(i);
//            if (curBingTime - startTime < data.time){
//                break;
//            }
//
//            if (((preBingoTime - startTime > data.time && preBingoTime - startTime < data.time + data.duration) ||
//                    (curBingTime - startTime > data.time && curBingTime - startTime < data.time + data.duration)) &&
//                    data.status == 0
//                    ){
//                data.status = 1;
//            }
//        }
//    }

    private LineData getCurData(long t){
        LineData ret = null;
        for (int i=0;i<lineDatas.size();i++){
            LineData data = lineDatas.get(i);
            if (t > data.time + data.duration){
                continue;
            }

            if (t < data.time){
                break;
            }

            if (t >= data.time && t <= data.time + data.duration){
                ret = data;
                break;
            }
        }

        return ret;
    }

    @Override
    public void dispose() {
        tt_bg.dispose();
        tt_vLine.dispose();
        tt_ball.dispose();
        tt_ball_pre.dispose();
        tt_line.dispose();
        tt_line_bingo.dispose();
        effect_star.dispose();
        tt_perfect.dispose();
        tt_good.dispose();
        tt_miss.dispose();
//        tt_gold.dispose();
//        tt_gold_box.dispose();
//        tt_gold_box_pre.dispose();
//        tt_gold_count.dispose();
    }

}
