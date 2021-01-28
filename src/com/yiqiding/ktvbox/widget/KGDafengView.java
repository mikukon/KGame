package com.yiqiding.ktvbox.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.animation.DecelerateInterpolator;

import com.android.carl.carllib.opengl.CLAniTexture;
import com.android.carl.carllib.opengl.CLTexture;
import com.android.carl.carllib.opengl.GLViewRect.RenderRequest;
import com.android.carl.carllib.opengl.NDCLTexture;
import com.android.carl.carllib.opengl.RotateCLTexture;
import com.android.carl.carllib.opengl.TextureData;
import com.android.carl.carllib.util.LL;
import com.java.carl.utils.RadomPathGenerator;
import com.yiqiding.ktvbox.R;
import com.yiqiding.ktvbox.libutils.LogUtil;

//import com.android.carl.carllib.util.LL;

/*
 * create by Carlyle Lee
 * 
 * @Warning: DO NOT CHANGE THIS FILE!
 * 
 */
public class KGDafengView extends GLSurfaceView implements
		GLSurfaceView.Renderer, RenderRequest {
	private static final String TAG = "KGDafengView";
	MediaPlayer mMediaPlayer;
	int state;
	boolean data_been_set;
	final int STATE_INI = 1, STATE_CREATED = 2, STATE_PREPARED = 4,
			STATE_STARTED = 5, STATE_STOP = 100
			;
	boolean data_loaded;
	boolean start;

	// [pitch raw data
	float raw_data[];
	float per_t;
	int maxP, minP;
	// 屏幕宽度，屏幕高度
	int dafenViewWidth, dafenviewHeight;
	boolean if_draw_cur;
	int mcx, mcy, heightPerLevel, baseHeight;
	int raw_line_ht;
	/**
	 * KGArray 第一个位置表示水平起始位置 第二个位置水平终点位置 第三个是垂直方向画的位置
	 */
	int[] waveLineData;
	/**
	 * KGArray中的数据 3个一组 
	 */
	private static final int GROUP_COUNT = 3;
	int current_pitch;
	GL10 _mgl;
	float allPastPx;// [cps]
	int end_dt;
	/**
	 * 表示显示一屏时候 波浪图数据的起始位置（对应x）和结束位置（对应y）
	 */
	Point oneScreenDataRange = new Point();
	int mdbar_wd = 2;
	int cur_hfh, cur_hf;
	boolean isVaild = true;
	boolean isShowNodes = true;
	private long timeInterVal = 230;// ms
	private long lastMoveTime = 0;

	// raw data]
	public KGDafengView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		// TODO Auto-generated constructor stub
	}

	public KGDafengView(Context context) {
		super(context);
		init();
		// TODO Auto-generated constructor stub
	}

	// [load resources]
	private void init() {
		// setState(STATE_INI);
		setZOrderMediaOverlay(true);
		setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
		setRenderer(this);
		mdbar_wd = (int) (getResources().getDisplayMetrics().density * mdbar_wd);
	}

	// [began to run]
	public void start(int musicCurrentPos, long cutTime) {
		Log.d(TAG, "musicCurrentPos=>" + musicCurrentPos + ",cutTime=>" + cutTime + ",(musicCurrentPos - cutTime)=>" + (musicCurrentPos - cutTime));
		synchronized (LK) {
			if (state < STATE_PREPARED) {
				LL.t(this, "state erro: called start in a wrong state");
				return;
			}
			start = true;
			// comment by zry, dafen song can't start
			setState(STATE_STARTED);
			// state=STATE_STARTED;
		}
		allPastPx = 0;
		// [start to go!]
		start_time = System.currentTimeMillis() - (musicCurrentPos - cutTime);
		Log.i("dafenview", "dafenview start_time" + start_time);

	}

	PrepareLis pls;

	public void setPrepareLis(PrepareLis l) {
		pls = l;
	}

	public interface PrepareLis {
		// [called when prepared, thus start can be called]
		public void onPrepared(KGDafengView dafenview, String serialId);
	}

	public static final int LEFT = 0, RIGHT = 1, CENTER = 2;
	private int dis_x = 0;// , start_md;//[start loop position]

	// public void setStartLocation(int p){
	// if(p<0||p>2){
	// p=CENTER;
	// }
	// start_md=p;
	//
	// }

	int time_one_screen = 10000;// [how much time one screen stand for]
	/**
	 * 单位像素所花费的时间
	 */
	int timePerPx;

	public void setScreenTime(int t) {// [bug]
		time_one_screen = t;
		if (time_one_screen > 90000)
			time_one_screen = 90000;
		else if (time_one_screen < 1000)
			time_one_screen = 1000;

		// [blocked v3.0: no use]
		// if(mwd!=0)
		// time_2_sx=time_one_screen/mwd;
	}

	// TODO new interface
	/**
	 *
	 * @param ar
	 *            format : startTime liveTime pitch ：打分文件的原始数据信息
	 * @param point_per_frame
	 *            countTime ：每一点代表的时间
	 * @param maxPitch
	 *            >0 ：传入的最大的picth 值
	 * @param minPitch
	 *            >0 ：传入的最小的pictch值
	 *
	 *            传入打分文件的数据
	 */
	private String songSerialId;

	public void setPichArrayLoaded(float[] ar, float point_per_frame,
			int maxPitch, int minPitch, String serialId) {
		// comment by zry, dafen song can't start
		// setState(STATE_INI);
		this.songSerialId = serialId;
		mMediaPlayer = null;
		if (ar != null && ar.length > 0) {
			data_loaded = true;
			raw_data = ar;
			maxP = maxPitch;
			minP = minPitch;
			if (maxP < minP || minP < 0) {
				throw new IllegalStateException("set pictch array maxP < minP "
						+ maxP + "  " + minP);
			}

			per_t = point_per_frame;

		} else {
			LL.t(this, "set Picth array , ineffective data");
			return;
		}

		synchronized (LK) {
			if (state < STATE_CREATED) {
				data_been_set = true;
				return;
			} else if (state > STATE_CREATED) {

				throw new IllegalStateException(
						"reset data array before stop kg view");

			}
		}

		// while(_mgl==null){
		// LL.t(this, "wait for sufaceCreated");
		// };
		if (_mgl == null) {
			LL.t(this, "gl context is null");
			return;
		}

		queueEvent(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				prepareData(_mgl);
				setState(STATE_PREPARED);
				if (pls != null) {
					LogUtil.d("call pls onPrepared");
					pls.onPrepared(KGDafengView.this, songSerialId);
				}
			}

		});
	}

	// [CALLED BY OUT SIDE
	/*
	 * public void setPitchArray_(int ar[], float point_per_frame,int
	 * maxPicth,int minPitch){
	 * 
	 * 
	 * if(ar!=null&&ar.length>0){ data_loaded=true; raw_data=ar; maxP=maxPicth;
	 * minP=minPitch; // KGArray=ar; per_t=point_per_frame;
	 * 
	 * }else{ LL.t(this, "set Picth array , ineffective data"); return; }
	 * 
	 * 
	 * 
	 * 
	 * synchronized(LK){ if(state!=STATE_STARTED&&state!=STATE_CREATED){
	 * setState(STATE_DATA_SET); return; } }
	 * 
	 * if(_mgl==null){ LL.t(this, "gl context is null"); return; }
	 * 
	 * 
	 * queueEvent(new Runnable(){
	 * 
	 * @Override public void run() { // TODO Auto-generated method stub
	 * prepareData(_mgl); setState(STATE_PREPARED); if(pls!=null){
	 * Log.w("KGDaFengView", "call pls onPrepared");
	 * pls.onPrepared(KGDafengView.this); } }
	 * 
	 * });
	 * 
	 * }
	 */

	// [drawable set
	int dr_sing, dr_sang, ballNormalResource, ballPressedResource;

	public void setDrawableNotSing(int dr) {
		dr_sing = dr;

	}

	public void setDrawableSang(int dr) {
		dr_sang = dr;
	}

	public void setDrawableCurrentPicth(int dr, int vice) {
		ballNormalResource = dr;
		ballPressedResource = vice;
	}

	// drawable set]
	/*
	 * 传入：当前的Pitch 值
	 */
	public void setCurrentPictch(int picth) {
		// Log.i("setCurrentPictch", "setCurrentPictch->"+silent_level+"");
		long cur = System.currentTimeMillis();

		if (ballTexture == null) {
			return;
		}
		if (picth == -1) {
			isVaild = false;
		} else {
			isVaild = true;
		}

		if (cur - lastMoveTime < timeInterVal) {

		} else {
			lastMoveTime = cur;
			if (picth < minP) {// ||picth>maxP
				current_pitch = silent_level;// (silent_level-max_level_ht)*ht_per_y+base_ht;
				ballTexture.move2(oneThirdScreenWidth - cur_hf, current_pitch, 30);//
				// return;
			} else if (picth > maxP) {
				current_pitch = -cur_hfh;
				// LL.e(this, "set current pt is called  "+current_pitch);
				ballTexture.move2(oneThirdScreenWidth - cur_hf, current_pitch, 30);//
				// return;
			} else {

				current_pitch = (getLevelByHT(picth) - min_level_ht) * heightPerLevel
						+ baseHeight;
				// LL.e(this, "set current pt is called  "+current_pitch);
				ballTexture.move2(oneThirdScreenWidth - cur_hf, current_pitch, 30);//
			}
		}

		color_dropes(current_pitch + cur_hfh);

	}

	// CALLED BY OUTSIDE]

	Object LK = new Object();

	public void setState(int st) {
		synchronized (LK) {
			state = st;
		}
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		Log.i("KGDafenView", "onSurfaceCreated");
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		gl.glClearColor(0, 0, 0, 0);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_ALPHA_TEST);
		gl.glAlphaFunc(GL10.GL_GREATER, 0.1f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		Log.i("KGDafenView", "onSurfaceChanged");
		dafenViewWidth = width;
		dafenviewHeight = height;
		mcx = dafenViewWidth >> 1;// 向右位移一位相当于除以2
		mcy = dafenviewHeight >> 1;// 向右位移一位相当于除以2
		_mgl = gl;

		CLTexture.iniCordsParse(dafenViewWidth, dafenviewHeight);
		hanleFrameChange(gl);

		synchronized (LK) {

			if (state >= STATE_CREATED) {
				return;
			} else {
				// modify by zry, use setState() replace direct set.
				setState(STATE_CREATED);
				// state=STATE_CREATED;
				if (!data_been_set) {
					return;
				}

			}

		}

		// prepareData(gl);
		// setState(STATE_PREPARED);
		// if(pls!=null)pls.onPrepared(this);
		//

		queueEvent(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				prepareData(_mgl);
				setState(STATE_PREPARED);
				if (pls != null) {
					Log.w(TAG, "call pls onPrepared");
					pls.onPrepared(KGDafengView.this, songSerialId);
				}
			}

		});

		return;

	}

	Object LK_perfect = new Object();
	java.util.LinkedList<CLTexture> perfect_list = new java.util.LinkedList<CLTexture>();

	private void draw_perfect(GL10 gl) {
		java.util.LinkedList<CLTexture> list = null;
		synchronized (LK_perfect) {
			if (perfect_list.size() == 0){
				
				return;
			}
			Log.d(TAG, "will draw perfect_list size=" + perfect_list.size());

			list = (LinkedList<CLTexture>) perfect_list.clone();
		}

		Iterator<CLTexture> it = list.iterator();
		while (it.hasNext()) {
			CLTexture clt = it.next();
			clt.draw(gl);
		}

	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// TODO Auto-generated method stub

		if (ballBgBar == null)
			return;

		gl.glClearColor(0, 0, 0, 0.4f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		// long tm=System.currentTimeMillis();

		bgSang.draw(gl);
		
		if (start) {
			// LogUtil.w("mdbar=>" + mdbar);
			drawLines(gl);
		}
		ballBgBar.draw(gl);
		
		ballTexture.draw(gl);

		draw_perfect(gl);

		// LL.time_gap(this, "draw gap", tm);

	}

	public boolean isCreated() {
		return _mgl == null ? false : true;
	}

	private void hanleFrameChange(GL10 gl) {
		timePerPx = time_one_screen / dafenViewWidth;
		gl.glViewport(0, 0, dafenViewWidth, dafenviewHeight);
	}

	int min_level_ht;
	int silent_level = -1;

	private void prepareData(GL10 gl) {

		timePerPx = time_one_screen / dafenViewWidth;// 换算 走过1px需要多少ms

		// [bit map

		if (!loadTexture(gl)) {
			return;
		}

		// [define line_ht < mht/8]
		int tmp_ht = dafenviewHeight / 8;// 将当前页面分成8级
		if (raw_line_ht > tmp_ht)
			raw_line_ht = tmp_ht;
		line_ht = raw_line_ht;

		// bit map]

		int max = raw_data.length;
		waveLineData = new int[max];

		// [figure dis
		int mdis = 0;
		// switch(start_md){
		// case CENTER:
		// mdis=mwd>>1;
		// break;
		// case RIGHT:
		// mdis=mwd;
		// break;
		// default:
		// if_draw_cur=true;
		// }

		mdis = dafenViewWidth / 3;// 将屏幕从水平方向分割三列
		oneThirdScreenWidth = mdis;

		mdis += dis_x;

		// [special code

		// mdis-=25*time_2_sx;

		// E special code]

		// ]

		// some margin not considered
		int ballHeight = ballTexture.getHeight();
		if (ballHeight < line_ht)
			ballHeight = line_ht;

		int totalLevelHeight = (int) (dafenviewHeight - getPaddingBottom() - getPaddingTop() - 1.5 * ballHeight);// [will
																							// change
																							// here
																							// ,
																							// if
																							// we
																							// dont
																							// use
																							// mht]
		// level_box_ht-=(line_ht<<1);
		baseHeight = getPaddingTop();// +rim>>1;
		// n_bs_c_cy=base_ht+level_box_ht+ct_cur.getHeight()>>1;//[blocked ,
		// v3.0]

		// int absm=maxP-minP;

		// int absm=Math.abs(maxP);
		// int absT=Math.abs(minP);
		// if(absm>absT){
		// // if(absm>MAX_PICTCH)absm=MAX_PICTCH;
		// // absm++;
		// maxP=absm;
		// minP=-absm;
		// absm=absm<<1;
		// }else{
		// // if(absT>MAX_PICTCH)absT=MAX_PICTCH;
		// // absm++;
		// maxP=absT;
		// minP=-absT;
		// absm=absT<<1;
		// }
		//

		waveLines = new SingDraw[waveLineData.length / GROUP_COUNT];
		
		int max_level_ht_temp = getLevelByHT(minP);// 对pitch进行分级
		min_level_ht = getLevelByHT(maxP);
		// silent_level=min_level_ht;

		// ht_per_y=level_box_ht/(min_level_ht-max_level_ht+2);//(T_LEVEL);//absm;//[+-48]
		heightPerLevel = totalLevelHeight / (max_level_ht_temp - min_level_ht);// 计算每一个pitch所占用的高度
		baseHeight += ballHeight / 2;// ballHeight是小球的高度
		// int cy=mht>>1;
		boolean test = false;
		
		int lengthTemp = waveLines.length;
		
		for (int i = 0; i < lengthTemp; i++) {// max是所有打点的个数
			int dataIndex = i * GROUP_COUNT;
//			log("dataIndex=>" + dataIndex);
			
			waveLineData[dataIndex] = (int) (raw_data[dataIndex] / timePerPx + mdis);
			waveLineData[dataIndex + 1] = (int) (waveLineData[dataIndex] + raw_data[dataIndex + 1] / timePerPx);// [set
																				// to
																				// end
																				// ,
																				// not
																				// lenth]
			// KGArray[i+2]=raw_data[i+2]*ht_per_y+mcy;//[right now - will be on
			// top]

			// if(test){
			// test=false;
			// KGArray[i+2]=(getLevelByHT(minP)-max_level_ht)*ht_per_y+base_ht;
			// }else{
			// test=true;
			// KGArray[i+2]=(getLevelByHT(maxP)-max_level_ht)*ht_per_y+base_ht;
			// }
			waveLineData[dataIndex + 2] = (getLevelByHT((int) raw_data[dataIndex + 2]) - min_level_ht)
					* heightPerLevel + baseHeight;// [new v.2.0.0, use T level to define
											// all levels]
			waveLines[i] = new SingDraw(waveLineData, dataIndex);

		}

		end_dt = waveLineData[waveLineData.length - 3] + waveLineData[waveLineData.length - 2];
		// setCurrentPictch(-1);
		// int ctx=mwd>>1;
		cur_hf = ballTexture.getWidth() >> 1;
		cur_hfh = ballTexture.getHeight() >> 1;

		// current_pitch=(getLevelByHT(minP)-max_level_ht)*ht_per_y+base_ht;
		current_pitch = totalLevelHeight + baseHeight;
		silent_level = current_pitch;
		baseHeight -= cur_hfh;

		ballTexture.layout(oneThirdScreenWidth - cur_hf, current_pitch, dafenViewWidth, dafenviewHeight);
		// setCurrentPictch(-1);
		ballBgBar.layout(oneThirdScreenWidth - mdbar_wd, 0, mdbar_wd << 1, dafenviewHeight, dafenViewWidth, dafenviewHeight);
		bgSang.layout(0, 0, oneThirdScreenWidth, dafenviewHeight, dafenViewWidth, dafenviewHeight);

		// test_setCurrentPitch();
	}

	// [new version , sepa all data to 10 levels;
	// level define
	final int MX_LV0 = 40, MX_LV1 = 41, MX_LV2 = 43, MX_LV3 = 45, MX_LV4 = 47,
			MX_LV5 = 48, MX_LV6 = 50, MX_LV7 = 52, MX_LV8 = 53, MX_LV9 = 55,
			MX_LV10 = 57, MX_LV11 = 59, MX_LV12 = 60, MX_LV13 = 62,
			MX_LV14 = 64, MX_LV15 = 65, MX_LV16 = 67, MX_LV17 = 69,
			MX_LV18 = 71, MX_LV19 = 72, MX_LV20 = 74, MX_LV21 = 76,
			MX_LV22 = 78, MX_LV23 = 80, MX_LV24 = 82, MX_LV25 = 84,
			MX_LV26 = 85, MX_LV27 = 87, MX_LV28 = 89;
	final int T_LEVEL = 52;// 29;
	int test_rt = 0;

	private int getLevelByHT(int da) {

		// [v2.0 grade setting 38-89
		int rs = 89 - da;
		if (rs < 0)
			return -1;
		else if (rs > 89)
			return 89;
		return rs;

		// }

		// [old grade setting]
		// if(da<MX_LV0){
		// return 29;
		// }else if(da<MX_LV1){
		// return 28;
		// }else if(da<MX_LV2){
		// return 27;
		// }else if(da<MX_LV3){
		// return 26;
		// }else if(da<MX_LV4){
		// return 25;
		// }else if(da<MX_LV5){
		// return 24;
		// }else if(da<MX_LV6){
		// return 23;
		// }else if(da<MX_LV7){
		// return 22;
		// }else if(da<MX_LV8){
		// return 21;
		// }else if(da<MX_LV9){
		// return 20;
		// }else if(da<MX_LV10){
		// return 19;
		// }else if(da<MX_LV11){
		// return 18;
		// }else if(da<MX_LV12){
		// return 17;
		// }else if(da<MX_LV13){
		// return 16;
		// }else if(da<MX_LV14){
		// return 15;
		// }else if(da<MX_LV15){
		// return 14;
		// }else if(da<MX_LV16){
		// return 13;
		// }else if(da<MX_LV17){
		// return 12;
		// }else if(da<MX_LV18){
		// return 11;
		// }else if(da<MX_LV19){
		// return 10;
		// }else if(da<MX_LV20){
		// return 9;
		// }else if(da<MX_LV21){
		// return 8;
		// }else if(da<MX_LV22){
		// return 7;
		// }else if(da<MX_LV23){
		// return 6;
		// }else if(da<MX_LV24){
		// return 5;
		// }else if(da<MX_LV25){
		// return 4;
		// }else if(da<MX_LV26){
		// return 3;
		// }else if(da<MX_LV27){
		// return 2;
		// }else if(da<MX_LV28){
		// return 1;
		// }
		// return 0;

		//

		// if(test_rt>=10)test_rt=0;
		// return test_rt++;
	}

	// E level]

	private void test_setCurrentPitch() {

		test_max = getLevelByHT(0) * heightPerLevel + baseHeight;
		test_min = getLevelByHT(40) * heightPerLevel + baseHeight;

		ValueAnimator vm = ValueAnimator.ofInt(test_min, test_max);
		vm.setDuration(250);
		vm.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				// current_pitch=(Integer)animation.getAnimatedValue();
				// LL.e(this, "set cur pic"+current_pitch);
				// setCurrentPictch(pic);

				// rc_cur_p.top=current_pitch+base_rc_cur_p.top;
				// rc_cur_p.bottom=current_pitch+base_rc_cur_p.bottom;
			}
		});
		vm.setRepeatCount(ValueAnimator.INFINITE);
		vm.setRepeatMode(ValueAnimator.REVERSE);
		vm.start();

	}

	int test_max, test_min;
	int line_ht;
	SingDraw waveLines[];
	CLAniTexture ballTexture;
	CLTexture ct_center_line;
	// CLTexture ct_perfect,ct_good,ct_greate;
	CLTexture ct_music;
	CLTexture ballBgBar;

	// ]

	class SingDraw extends CLTexture {
		int _id;
		boolean turnd;
		
		List<LineStar> starList;
		
		public SingDraw(int data[], int ps) {

			super();
			_id = ps;
			setTextureData(sins);
			int hf = line_ht >> 1;
			layout(data[ps], data[ps + 2] - hf, data[ps + 1] - data[ps],
					line_ht, dafenViewWidth, dafenviewHeight);

			starList = new ArrayList<KGDafengView.LineStar>();
			int goldCount = calcCountOfConvertStar();
			int left = getLeftI();
			for (int i = 0; i < goldCount; i++) {
				
				LineStar star = new LineStar(this, left, getTopI(), 8);
				starList.add(star);
				
				left += star.realWidth ;
			}
		}

		public void turn() {
			if (turnd)
				return;
			turnd = true;
			setTextureData(sans);
			// this.invalidate();
			
		}
		
		private int calcCountOfConvertStar(){
			int goldWidth = goldTexture.bwd + 8;//8px 是金币与金币之间的间距
			int res = this.getWidth() / goldWidth;
//			Log.d(TAG, "singDraw width=>" + getWidth() + ",goldWidth=>" + goldWidth + ",res=>" + res);
			return res;
		}
		
		public void checkHasHitGold(int left, int top, int right, int bottom){
			if (starList == null) return;
			
			int size = starList.size();
			for (int i = 0; i < size; i++) {
				LineStar star = starList.get(i);
				if(star.intersects(left, top, right, bottom)){
					star.hitStar = true;
				}
			}
		}
	}
	
	
	class LineStar extends CLTexture {
		SingDraw parent; 
		boolean hitStar;
		int realWidth = 0;

		public LineStar(SingDraw parent, int leftPos, int topPos, int rightMargin) {
			super();
			this.parent = parent;
			setTextureData(goldTexture);
			int hf = goldTexture.bht >> 1;
			layout(leftPos, topPos, goldTexture.bwd + rightMargin, goldTexture.bht, dafenViewWidth, dafenviewHeight);
			
			realWidth = getWidth() + rightMargin;
		}
		
		public boolean intersects(int left, int top, int right, int bottom) {
	        return this.getLeftI() < right && left < this.getRightI() && this.getTopI() < bottom && top < this.getBottomI();
	    }
		
		public boolean intersects(Rect a, Rect b) {
	        return a.left < b.right && b.left < a.right && a.top < b.bottom && b.top < a.bottom;
	    }
	}

	TextureData sins, sans;
	
	TextureData goldTexture;

	TextureData msc, nd_good, nd_great, nd_perfect; 
	/**
	 * 
	 */
	TextureData ballNormalTextureData; 
	TextureData ballPressedTextureData;

	private boolean loadTexture(GL10 gl) {

		if (dr_sing == 0 || dr_sang == 0 || ballNormalResource == 0) {
			LL.e(this, "must set drawable");
			return false;
		}

		Resources res = getResources();

		CLTexture tmpct = new CLTexture();
		Bitmap bt_pft = BitmapFactory.decodeResource(res, R.drawable.music_001);
		tmpct.setBitmap(gl, bt_pft);
		msc = CLTexture.makeTextureData(tmpct.getTexture(), bt_pft);
		bt_pft.recycle();

		bt_pft = BitmapFactory.decodeResource(res, R.drawable.good);
		tmpct.setBitmap(gl, bt_pft);
		nd_good = CLTexture.makeTextureData(tmpct.getTexture(), bt_pft);
		bt_pft.recycle();

		bt_pft = BitmapFactory.decodeResource(res, R.drawable.great);
		tmpct.setBitmap(gl, bt_pft);
		nd_great = CLTexture.makeTextureData(tmpct.getTexture(), bt_pft);
		bt_pft.recycle();
		bt_pft = BitmapFactory.decodeResource(res, R.drawable.perfect);
		tmpct.setBitmap(gl, bt_pft);
		nd_perfect = CLTexture.makeTextureData(tmpct.getTexture(), bt_pft);
		bt_pft.recycle();

		bt_pft = BitmapFactory.decodeResource(res, ballNormalResource);
		tmpct.setBitmap(gl, bt_pft);
		ballNormalTextureData = CLTexture.makeTextureData(tmpct.getTexture(), bt_pft);
		bt_pft.recycle();

		bt_pft = BitmapFactory.decodeResource(res, ballPressedResource);
		tmpct.setBitmap(gl, bt_pft);
		ballPressedTextureData = CLTexture.makeTextureData(tmpct.getTexture(), bt_pft);
		bt_pft.recycle();
		
		//add by zry
		bt_pft = BitmapFactory.decodeResource(res, R.drawable.gold);
		tmpct.setBitmap(gl, bt_pft);
		goldTexture = CLTexture.makeTextureData(tmpct.getTexture(), bt_pft);
		bt_pft.recycle();

		bt_pft = null;

		Bitmap bsins = BitmapFactory.decodeResource(res, dr_sing);
		Bitmap bsans = BitmapFactory.decodeResource(res, dr_sang);
		tmpct.setBitmap(gl, bsins);
		sins = CLTexture.makeTextureData(tmpct.getTexture(), bsins);
		tmpct.setBitmap(gl, bsans);
		sans = CLTexture.makeTextureData(tmpct.getTexture(), bsans);
		bsins.recycle();
		bsans.recycle();

		raw_line_ht = sins.bht;
		if (raw_line_ht < sans.bht)
			raw_line_ht = sans.bht;

		ballTexture = new CLAniTexture(null);
		ballTexture.setTextureData(ballNormalTextureData);

		ballBgBar = new CLTexture();
		ballBgBar.setBGColor(0xB2d5d5d5);
//		ballBgBar.setBGColor(0xB2FF0000);

		bgSang = new CLTexture();
		bgSang.setBGColor(0xb2000000);

		return true;

	}
	/**
	 * 真正开始时候的时间戳记录
	 */
	long start_time;
	float testF = 1;
	Object LK_2 = new Object();

	private void drawLines(GL10 gl) {
		figureRange();

		int waveDataStartPos = oneScreenDataRange.x;
		int waveDataEndPos = oneScreenDataRange.y;

		if (waveDataStartPos != -1) {

			gl.glPushMatrix();

			float test_trans = CLTexture.parseDeltaInt((int) allPastPx, 0).x;
			gl.glTranslatef(-test_trans, 0, 0);

			int waveLineStartPos = waveDataStartPos / 3;
			int waveLineEndPos = waveDataEndPos / 3;

			if (waveLineStartPos < 0) {
				LL.e(this, "erro:c_stt is <0  pta:" + waveLines.length
						+ " raw len: " + waveLineData.length);
				waveLineStartPos = 0;
			}
			if (waveLineEndPos > waveLines.length) {
				LL.e(this, "erro:c_stt is <0  pta:" + waveLines.length
						+ " raw len: " + waveLineData.length);
				waveLineStartPos = 0;
				waveLineEndPos = waveLines.length;
			}
//			List<LineStar> dropList = new ArrayList<KGDafengView.LineStar>();
			for (int i = waveLineStartPos; i <= waveLineEndPos; i++) {
				waveLines[i].draw(gl);
//				SingDraw singdraw = waveLines[i];
//				
//				dropList.clear();
//				List<LineStar> starList = singdraw.starList;
//				for (int j = 0; j < starList.size(); j++) {
//					LineStar star = starList.get(j);
//					if (star.hitStar) {
//						Log.w(TAG, "star was hit, show star fly, add to drop list");
//						dropList.add(star);
//					}
//					starList.get(j).draw(gl);
//				}
//				
//				starList.removeAll(dropList);
			}


//			/**[[block by zry  hide red hit effect	
			// LL.d(this, "match list size "+match_list.size());
			java.util.LinkedList<ColoredDropes> _list = null;
			synchronized (LK_2) {// [rm unused]

				java.util.Iterator<ColoredDropes> cd = match_list.iterator();
				while (cd.hasNext()) {
					ColoredDropes d = cd.next();
					// LL.d(this,d._id+" <? "+curx);

					if (d._id < waveDataStartPos) {
						cd.remove();
						continue;
					}
				}

				_list = (LinkedList<ColoredDropes>) match_list.clone();

			}

			if (_list != null) {

				java.util.Iterator<ColoredDropes> cd = _list.iterator();
				while (cd.hasNext()) {
					ColoredDropes d = cd.next();
//					Log.d(TAG, "draw coloredDropes width=>" + d.getWidth());
					d.draw(gl);
				}

			}
//			block end]]*/
			gl.glPopMatrix();

		}

	}

	public void stop() {
		LogUtil.i("call dafenView stop()");
		if (state == STATE_CREATED) {
			return;
		}
		onEnd();
	}

	private boolean figureRange() {
		// if(mMediaPlayer==null){
		// return;
		// }

		allPastPx = (System.currentTimeMillis() - start_time) / timePerPx;
		// try {
		// scx=(mMediaPlayer.getCurrentPosition())/time_2_sx;
		// } catch (Exception e) {
		// e.printStackTrace();
		// LogUtil.i("mMediaPlayer.getCurrentPosition() Excetion");
		// return;
		// }

		if (waveLineData == null || timePerPx == 0) {
			stop();
			return false;
		}

		float ks_time_stt = allPastPx;
		float ks_time_edd = ks_time_stt + dafenViewWidth;

		// [find first and end position;

		if (ks_time_edd < waveLineData[0]) {
			return true;
		} else if (end_dt < ks_time_stt) {
			// RUN_DAR.run();
			stop();
			return false;
		}

		// crd+=rt;
		// LL.e(this, rt+"");

		int p = getPositionStart(oneScreenDataRange.x, ks_time_stt);

		if (p == -1) {
			stop();
			return false;
		} else {
			oneScreenDataRange.x = p;
		}

		p = getPositionEnd(oneScreenDataRange.y, ks_time_edd);
		if (p == -1) {
			oneScreenDataRange.y = waveLineData.length - 1;
		} else {
			oneScreenDataRange.y = p;
		}

		return true;

	}

	private int getPositionStart(int stt, float limit) {
//		log("[getPositionStart]stt=" + stt + ",limit=" + limit);
		int p = stt;
		int max_p = waveLineData.length - 1;

		while (p < max_p) {
			if (waveLineData[p + 1] < limit) {// [blank]
				p += GROUP_COUNT;
			} else {
				break;
			}
		}
		if (p >= max_p)
			return -1;
		return p;// [call on end]
	}

	private int getPositionEnd(int stt, float limit) {
//		log("[getPositionEnd]stt=" + stt + ",limit=" + limit);
		int p = stt;
		int max_p = waveLineData.length - 1;

		while (p < max_p) {
			if (waveLineData[p] < limit) {// [blank]
//				log("[getPositionEnd]waveLineData["+p+"]=" + waveLineData[p] + " is lowlimit=" + limit);
				p += GROUP_COUNT;
			} else {
				break;
			}
		}
		
		if (p >= max_p)
			return -1;

		return p;// [call on end]
	}

	private void onEnd() {
		setCurrentPictch(-1);
		synchronized (LK) {
			start = false;
			// modify by zry, use setState() replace direct set.
			setState(STATE_CREATED);
			// state=STATE_CREATED;
			data_been_set = false;
			allPastPx = 0;
			mMediaPlayer = null;
			// add by zry 如果手动停止了打分view 就删除回调
			// pls=null;

			if (match_list != null) {
				synchronized (LK_2) {
					match_list.clear();
				}

			}
			color_start = 0;
			oneScreenDataRange.set(0, 0);
		}
	}

	/*
	 * private void testCode(){
	 * 
	 * int ar[]={0,5,8,20,8,28,30,9,-19, 40,3,20,50,6,-10,60,8,38,
	 * 70,6,-30,80,8,-8,90,9,9, 100,8,-30};
	 * 
	 * // setScreenTime(10000);//(12000000);
	 * 
	 * setPitchArray(ar, 1, 80, -1);
	 * 
	 * 
	 * 
	 * }
	 */

	/*
	 * CLTexture CLT=new CLTexture();
	 * 
	 * 
	 * public void testPlay(){
	 * 
	 * 
	 * if(ct_cur!=null) ct_cur.play(4); }
	 * 
	 * public void test_load_cur(GL10 gl){ if(ct_cur!=null)return;
	 * 
	 * Bitmap bps[]=new Bitmap[dr_cur.length]; int len=bps.length; for(int
	 * i=0;i<len;i++){ bps[i]=BitmapFactory.decodeResource(getResources(),
	 * dr_cur[i]); if(bps[i]==null){
	 * 
	 * len=i; for(i=0;i<len;i++){ bps[i].recycle(); }
	 * 
	 * LL.t(this, "cur b is null"); } }
	 * 
	 * 
	 * 
	 * ct_cur=new CLAniTexture(dr_cur);
	 * 
	 * ct_cur.setBitmap(gl, bps); for(int i=0;i<len;i++){ bps[i].recycle(); }
	 * 
	 * ct_cur.layout(100, 300, mwd, mht);
	 * 
	 * }
	 * 
	 * 
	 * public void testMove2(int ps){ ct_cur.translate(0, ps, 0); }
	 */

	public static final int GOOD = 1, PERFECT = 2, MISS = 0;

	public interface PerformanceCallBack {
		public void onPerformance(int p);
	}

	PerformanceCallBack pcb;

	public void setPerformanceCallback(PerformanceCallBack p) {
		pcb = p;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		LL.e(this, "destroy ");
		onEnd();
		// setState(STATE_INI);
		super.surfaceDestroyed(holder);

	}

	private void _my_show_nd(int[] fx, final CLTexture clt) {

		RadomPathGenerator rdp = new RadomPathGenerator(fx);
		ValueAnimator ani = rdp.getXPathAnimator();
		ani.setDuration(1500);
		ani.setInterpolator(new DecelerateInterpolator());
		ani.setTarget(clt);

		ani.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				isShowNodes = false;
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				isShowNodes = true;
				synchronized (LK_perfect) {
					perfect_list.remove(clt);
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				isShowNodes = true;
				synchronized (LK_perfect) {
					perfect_list.remove(clt);
				}
			}
		});

		ani.start();

		synchronized (LK_perfect) {
			Log.d(TAG, "will call perfect_list.add");
			perfect_list.add(clt);
		}

	}

	public void showMusicNodes() {
		final RotateCLTexture clt = new RotateCLTexture();

		clt.setTextureData(msc);
		int left = oneThirdScreenWidth - (clt.getWidth() >> 1);
		int top = mcy - (clt.getHeight() >> 1);
		Log.d(TAG, "music node left=>" + left + ",top=>" + top);
		clt.layout(left, top, dafenViewWidth, dafenviewHeight);

		int fx[] = { oneThirdScreenWidth, mcy, 0, 0 };
		int pwd = getMeasuredWidth();

		int cwd = pwd >> 1;
		fx[2] = (int) ((Math.random() + 1) * cwd);
//		fx[3] = -(clt.getHeight() + 2);
		fx[3] = fx[1] + clt.getHeight() + 2;

		_my_show_nd(fx, clt);

	}

	public void showGood() {
		_show_text(nd_good);
	}

	public void showGreate() {
		_show_text(nd_great);
	}

	public void showPerfect() {
		_show_text(nd_perfect);
	}

	private void _show_text(TextureData td) {

		final NDCLTexture clt = new NDCLTexture();

		clt.setTextureData(td);
		int left = oneThirdScreenWidth - clt.getWidth() / 2;
		int top = dafenviewHeight + 10;
		clt.layout(left, top, dafenViewWidth, dafenviewHeight);

		int fx[] = { left, top, left + 1000, 0 };

		// fx[2]=(int) ((Math.random()+1)*cwd);
		fx[3] = dafenviewHeight * 1 / 4;
		Log.d(TAG, "[_show_text]........");
		_my_show_nd(fx, clt);

	}

	// [v3:
	java.util.LinkedList<ColoredDropes> match_list = new java.util.LinkedList<ColoredDropes>();

	class ColoredDropes extends CLTexture {
		public int _id, _ht;
		int m_tp;

		public ColoredDropes(int id, int topI, int ht) {
			_id = id;
			_ht = ht;
			m_tp = topI;
			// this.setBGColor(0xFFFFB6C1);
			this.setTextureData(sans);
		}

		public void setEnd(int end) {
			LL.d(this, "set end " + end + " former:  " + this.getLeftI()
					+ "   " + this.getRightI());
			this.layout(this.getLeftI(), this.getTopI(), end - this.getLeftI(),
					_ht, KGDafengView.this.dafenViewWidth, KGDafengView.this.dafenviewHeight);
		}

		public void setRank(int start, int end) {
			this.layout(start, m_tp, end - start, _ht, KGDafengView.this.dafenViewWidth,
					KGDafengView.this.dafenviewHeight);
			LL.d(this, "layout wd is " + (end - start));
		}
	}

	private SingDraw getCurrentTexture() {
		if (oneScreenDataRange.x == -1)
			return null;
		int c_stt = oneScreenDataRange.x / 3;
		int c_end = oneScreenDataRange.y / 3;
		float cur = oneThirdScreenWidth;
		cur += allPastPx;
		for (int i = c_stt; i <= c_end; i++) {
			SingDraw sd = waveLines[i];
			if (sd.getRightI() > cur && sd.getLeftI() < cur) {
				return sd;
			}
		}
		return null;
	}

	int color_start;
	int color_id;

	// [cl: is current pitch hy]
	private void color_dropes(int cl) {

		SingDraw sd = getCurrentTexture();
		if (sd == null) {
			color_start = 0;
			return;
		}

		// 如果是传入的pitch为－1，
		if (!isVaild) {
			if (color_start != 0) {
				color_start = 0;
			}
			return;
		}

		// LL.e(this, "current texture id is "+sd._id);

		synchronized (LK_2) {

			// cl=sd.getTopI()+10;//[test code]
			if (cl > sd.getTopI() - 2 * heightPerLevel
					&& cl < sd.getBottomI() + 2 * heightPerLevel) {// [collusion]

				//comment by zry, 
				post(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (isShowNodes) {
							Log.d(TAG, "will call showMusicNodes");
							showMusicNodes();
						}
//						showPerfect();
					}

				});

//				turn_cur_on();

				int lts = oneThirdScreenWidth;
				lts = (int) (allPastPx + lts);

				if (color_start == 0) {
					color_start = lts;
					color_id = sd._id;
					Log.d(TAG, "color_start is 0, just set color_start=lts");
					return;
				}

				ColoredDropes cd = null;
				if (match_list.size() > 0)
					cd = match_list.getLast();

				if(cd != null && cd._id == sd._id){
					Log.d(TAG, "colorDropes setEnd " + lts);
					cd.setEnd(lts);
				}else {
					if(color_id == sd._id){						
						cd = new ColoredDropes(sd._id, sd.getTopI(),
								sd.getHeight());
						Log.d(TAG, "color_start=>" + color_start + ",lts=>" + lts + ",(lts - color_start)=>" + (lts - color_start));
						cd.setRank(color_start, lts);
						match_list.addLast(cd);
					}
				}
				//add by zry 
//				sd.checkHasHitGold(cd.getLeftI(), cd.getTopI(), cd.getRightI(), cd.getBottomI());
				
				color_start = lts;
				color_id = sd._id;
//				if (cd == null) {
//					if (color_id == sd._id) {
//						cd = new ColoredDropes(sd._id, sd.getTopI(),
//								sd.getHeight());
//						Log.d(TAG, "----color_start=>" + color_start + ",lts=>" + lts + ",(lts - color_start)=>" + (lts - color_start));
//						cd.setRank(color_start, lts);
//
//						match_list.addLast(cd);
//
//					}
//				} else {
//
//					if (cd._id == sd._id) {
//						cd.setEnd(lts);
//					} else if (color_id == sd._id) {
//						cd = new ColoredDropes(sd._id, sd.getTopI(),
//								sd.getHeight());
//						Log.d(TAG, "====color_start=>" + color_start + ",lts=>" + lts + ",(lts - color_start)=>" + (lts - color_start));
//						cd.setRank(color_start, lts);
//						match_list.addLast(cd);
//					}
//
//				}
			} else {

				if (color_start != 0) {
					Log.d(TAG, "reset color_start 0");
					color_start = 0;
				}
				// turn_cur_off();

			}

		}

	}

	public void setmMediaPlayer(MediaPlayer mMediaPlayer) {
		this.mMediaPlayer = mMediaPlayer;
	}

	boolean isOn = false;

	private void turn_cur_on() {
		if (isOn)
			return;
		isOn = true;
		ballTexture.setTextureData(ballPressedTextureData);
	}

	private void turn_cur_off() {
		if (!isOn)
			return;
		isOn = false;
		ballTexture.setTextureData(ballNormalTextureData);
	}

	int oneThirdScreenWidth;
	/**
	 * 屏幕左侧区域 以小球所在的分割线分割 左侧唱过以后的部分
	 */
	CLTexture bgSang;
	// ]

	
	private void log(String msg){
		Log.d("KGDafengView", msg);
	}
}
