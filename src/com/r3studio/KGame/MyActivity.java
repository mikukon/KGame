package com.r3studio.KGame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.carl.carllib.util.CLUtilLoader;
import com.r3studio.KGame.gdx.GdxManager;
import com.r3studio.KGame.gl.MGLSurfaceView;
import com.yiqiding.ktvbox.R;
import com.yiqiding.ktvbox.ksong.dafen.EventDafenPingJia;
import com.yiqiding.ktvbox.ksong.dafen.EventDafenProcess;
import com.yiqiding.ktvbox.ksong.dafen.KGameManager;
import com.yiqiding.ktvbox.ksong.dafen.KGameManager.KGamePingJiaListener;
import com.yiqiding.ktvbox.ksong.dafen.KGameResultListener;
import com.yiqiding.ktvbox.ksong.dafen.KSongDaFenManager.PitchCallBack;
import com.yiqiding.ktvbox.rpc.socket.data.SendKGChanllengeResults;
import com.yiqiding.ktvbox.structure.KGameJiepaiEntity;
import com.yiqiding.ktvbox.widget.KGDafengView;
import com.yiqiding.ktvbox.widget.imageViewWithAnimation;

import de.greenrobot.event.EventBus;

/**
 * kgame文件要求：
 * <p>
 * 1./sdcard/kg_temps下
 * </p>
 * <p>
 * 2.名字规则：盘符.名字.serialid.original_track.sound_track.kgame
 * </p>
 * <p>
 * e.g:04.77174304.1780500253.0.1.kgame
 * </p>
 * 
 * @author zgwang
 */
public class MyActivity extends Activity implements KGameResultListener,
		SurfaceHolder.Callback {
	private static final String TAG = "MyActivity";
	static final String DIR = "/sdcard/kg_temps";
	// static final String DIR = "/data/data/com.yiqiding.ktvbox";
	SurfaceView surface;
	KGDafengView sv_kg;
	View exit;
	TextView tips;
	View start;
	View restart;
	View switchOriginal;
	View pauseStart;
	View pre;
	View next;
	EditText input;
	EditText resourceIp;
	View play;
	View reload;
	Button recordTime;

	imageViewWithAnimation pingJiaView;

	MGLSurfaceView mglsfv;
	
	MediaPlayer mp;
	KGameManager manager;

	int mIndex = -1;
	List<ItemInfo> mList;
	ProgressDialog loadDialog;
	boolean isError = false;
	boolean isOriginal = false;

	private boolean mediaIsPrepared;
	private boolean dafenViewPrepared;

	private PitchCallBack callback;

//	private long cutTime = 120708;
	private long cutTime = 0;

	KGamePingJiaListener listener = new KGamePingJiaListener() {

		@Override
		public void showPingJia(final int pingJiaId) {
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
//					if (pingJiaView == null) {
//						pingJiaView = (imageViewWithAnimation) findViewById(R.id.ping_jia_view);
//					}
//					pingJiaView.setImageResource(pingJiaId);
				}
			});

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(
				android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
				android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		CLUtilLoader.init(this);
		setContentView(R.layout.main);

		iniItems();
		
		tv = (TextView) findViewById(R.id.time);
		surface = (SurfaceView) findViewById(R.id.surface);
		sv_kg = (KGDafengView) findViewById(R.id.sv_kg);
		exit = findViewById(R.id.exit);
		tips = (TextView) findViewById(R.id.tips);
		start = findViewById(R.id.start);
		restart = findViewById(R.id.restart);
		switchOriginal = findViewById(R.id.switchOriginal);
		pauseStart = findViewById(R.id.pauseStart);
		pre = findViewById(R.id.pre);
		next = findViewById(R.id.next);
		input = (EditText) findViewById(R.id.input);
		resourceIp = (EditText) findViewById(R.id.resourceIp);
		play = findViewById(R.id.play);
		reload = findViewById(R.id.reload);
		recordTime = (Button) findViewById(R.id.recordTime);
		recordTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "recordStartTime");
				// KGameLogUtil.getInstance().appendLog("record time");
			}
		});

		surface.getHolder().addCallback(this);
		sv_kg.setScreenTime(6800);
		// sv_kg.setRefreshTimeInterval(60);
		// sv_kg.setStartLocation(KGDafengView.CENTER);
		// sv_kg.setDrawableCurrentPicth(getResources().getDrawable(R.drawable.qiu));
		// sv_kg.setCurrentPicthDrawable(R.drawable.qiu);
		// sv_kg.setDrawableCurrentPicth(new int[]{R.drawable.button_light01,
		// R.drawable.button_light02,
		// R.drawable.button_light03,
		// R.drawable.button_light04});

		sv_kg.setDrawableSang(R.drawable.kg_line_on);
		sv_kg.setDrawableNotSing(R.drawable.kg_line_off);
		sv_kg.setDrawableCurrentPicth(R.drawable.kg_pnt_off,
				R.drawable.kg_pnt_on);

		sv_kg.setVisibility(View.GONE);

		mglsfv = (MGLSurfaceView) findViewById(R.id.msv);
		mglsfv.setVisibility(View.GONE);
		View gdxview = GdxManager.getInstance().initForGdxView(this);
		FrameLayout.LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addContentView(gdxview, lp);
		gdxview.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return false;
			}
		});
		
		// Bitmap bm=BitmapFactory.decodeResource(getResources(),
		// R.drawable.qiu);
		// int mwd=bm.getWidth();
		// int height=(int) (mwd*1.2f);
		// Bitmap nbm=Bitmap.createScaledBitmap(bm, mwd, height, true);
		// BitmapDrawable bmd=new BitmapDrawable(nbm);
		// if(nbm!=bm)
		// bm.recycle();

		// sv_kg.setDrawableCurrentPicth(bmd);
		//
		// sv_kg.setDrawableSang(new
		// int[]{R.drawable.button_yellow_left,R.drawable.button_yellow_middle,R.drawable.button_yellow_right});
		// sv_kg.setDrawableNotSing(new
		// int[]{R.drawable.button_blue_left,R.drawable.button_blue_middle,R.drawable.button_blue_right});
		sv_kg.setPrepareLis(new KGDafengView.PrepareLis() {
			@Override
			public void onPrepared(KGDafengView dafenview, String serialId) {
				Log.d(TAG, "dafenview  prepared");
				dafenViewPrepared = true;
				judgeCanStart();
			}
		});
		GdxManager.getInstance().gdxViewListener.setPrepareLis(new KGDafengView.PrepareLis() {
			@Override
			public void onPrepared(KGDafengView dafenview, String serialId) {
				Log.d(TAG, "dafenview  prepared");
				dafenViewPrepared = true;
				judgeCanStart();
			}
		});
		pingJiaView = (imageViewWithAnimation) findViewById(R.id.ping_jia_view);
		mp = new MediaPlayer();
		mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			public void onPrepared(MediaPlayer mediaplayer) {
				Log.d(TAG, "mediaplayer  prepared");
				mediaIsPrepared = true;
				judgeCanStart();
			}
		});
		mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				isError = false;
				tips.setText("播放失败");
				if (mp.isPlaying()) {
					mp.stop();
				}
				mp.reset();
				return true;
			}
		});
		
		mp.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mIndex++;
				isError = false;
				if (mIndex >= mList.size()) {
					isError = false;
					tips.setText("出错了，没有下一首或者无法播放");
				} else {
					play();
				}
			}

		});

		exit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		start.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String ip = resourceIp.getText().toString();
				if (TextUtils.isEmpty(ip)) {
					Toast.makeText(MyActivity.this, "必须先填个资源服务器IP",
							Toast.LENGTH_SHORT).show();
					return;
				}
				ItemInfo.resourceIp = ip;
				Log.d("MyActivity", "ItemInfo.resourceIp=>"
						+ ItemInfo.resourceIp);
				if (isError) {
					return;
				}
				mIndex = 0;
				play();
			}
		});

		restart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isError) {
					return;
				}
				if (mIndex < 0) {
					mIndex = 0;
				}

				stop();
				play();

			}
		});

		switchOriginal.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isError) {
					return;
				}
				ItemInfo item = mList.get(mIndex);
				if (mp != null) {
					if (isOriginal) {
						isOriginal = false;
						mp.selectTrack(item.sound);
					} else {
						isOriginal = true;
						mp.selectTrack(item.original);
					}
				}
			}
		});

		pauseStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isError) {
					return;
				}
				if (mp != null) {
					if (mp.isPlaying()) {
						mp.pause();
					} else {
						mp.start();
					}
				}
			}
		});

		pre.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isError) {
					return;
				}
				if (mIndex - 1 < 0) {
					tips.setText("没有上一首了");
					return;
				}
				mIndex--;
				play();
			}
		});

		next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// if (isError) {
				// return;
				// }

				if (mIndex + 1 >= mList.size()) {
					tips.setText("没有下一首了");
					return;
				}

				mIndex++;
				play();
			}
		});

		play.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isError) {
					return;
				}
				if (mList == null || mList.isEmpty()) {
					tips.setText("没有加载本地数据");
					return;
				}
				String in = input.getText().toString();
				if (in.length() <= 0) {
					tips.setText("输入的serialid不合法");
					return;
				}
				int index = mList.indexOf(new ItemInfo(in));
				if (mIndex < 0) {
					tips.setText("本地数据不存在此serialid的kgame文件");
					return;
				}
				mIndex = index;
				play();
			}
		});

		reload.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// loadDialog.setMessage("正在加载本地数据...");
				// loadDialog.show();
				// if (getLocData()) {
				// loadDialog.dismiss();
				// tips.setText("本地数据加载完成");
				// }
				play();
			}
		});

		manager = KGameManager.getInstance(MyActivity.this);
		manager.setListener(this);
		manager.setPingJiaListener(listener);
		callback = new PitchCallBack() {

			@Override
			public void onStop() {
				GdxManager.getInstance().gdxViewListener.stop();
			}

			@Override
			public void onStart(int musicCurrentPos, long cutTime) {
				Log.d(TAG, "current mediaplayer pos=>" + mp.getCurrentPosition());
//				sv_kg.start(mp.getCurrentPosition(), cutTime);
//				mglsfv.start();
				GdxManager.getInstance().gdxViewListener.start();
			}

			@Override
			public void onPitchMatch() {

			}

			@Override
			public void currentPitch(int picth) {
				// DOTO debugCode
//				if (picth < 54) {
//					int offset = (int) (Math.random() * (73 - 54));
//					picth = offset + 54;
//					// Log.d(TAG,
//					// "current pitch lower than min, after offset =>" + picth);
//				} else {
//					// Log.d(TAG, "current pitch=>" + picth);
//				}
//				sv_kg.setCurrentPictch(picth);
//				mglsfv.updataCurPitch(picth);
				GdxManager.getInstance().gdxViewListener.updateCurPitch(System.currentTimeMillis(), picth);
			}

			@Override
			public void onPichArrayLoaded(float[] ar, float point_per_frame,
					int maxPitch, int minPitch, String sirialId) {
				Log.d(TAG, "maxPitch=>" + maxPitch + ", minPitch=>" + minPitch);
				float pst = ar[0];
//				sv_kg.setPichArrayLoaded(ar, point_per_frame, maxPitch,
//						minPitch, sirialId);

				if (ar != null && ar.length > 0)
					postShowLeng((int) pst, point_per_frame);
				
//				mglsfv.prepare(ar, minPitch, maxPitch);
				GdxManager.getInstance().gdxViewListener.prepare(ar, minPitch, maxPitch);
			}
		};
		manager.setPitchCallBack(callback);

		EventBus.getDefault().register(this);
	}

	public void onEventBackgroundThread(EventDafenPingJia pingjia) {

	}

	public void onEventMainThread(EventDafenProcess process) {
		Log.d(TAG, "current score" + process.getScore());
		tv.setText(process.getScore() + "");
	}

	private void play() {

		stop();
		try {
			// Debug.startMethodTracing("/sdcard/kgame_call.trace");

			isError = true;
			final ItemInfo item = mList.get(mIndex);
			manager.setPitchCallBack(callback);
			new Thread() {
				public void run() {
					manager.loadKGameOfSid(MyActivity.this,
							DIR + "/" + item.getFileName(), cutTime);
				};
			}.start();
			if (mp.isPlaying()) {
				mp.stop();
			}
			// mp.setDisplay(null);
			mp.reset();
			//TODO 此处是测试代码 写死了播放的内容
			mp.setDataSource("/sdcard/1234567.mp4");
//			mp.setDataSource(item.getUrl());
			mp.setDisplay(surface.getHolder());
			mp.prepareAsync();
		} catch (Exception e) {
			e.printStackTrace();
			isError = false;
			tips.setText("出错了，没有下一首或者无法播放");
		}
	}

	// private boolean getLocData() {
	// File f = new File(DIR);
	// if (!f.exists()) {
	// tips.setText("本地没有数据 [目录：" + DIR + "]");
	// loadDialog.dismiss();
	// isError = true;
	// return false;
	// }
	//
	// File[] fs = f.listFiles(new FilenameFilter() {
	//
	// @Override
	// public boolean accept(File dir, String filename) {
	// if (filename.endsWith(".kgame")) {
	// return true;
	// }
	// return false;
	// }
	// });
	//
	// if (fs == null || fs.length <= 0) {
	// tips.setText("本地没有数据 [目录：" + DIR + "]");
	// loadDialog.dismiss();
	// isError = true;
	// return false;
	// }
	//
	// try {
	// mList = new ArrayList<ItemInfo>();
	// ItemInfo item = null;
	// for (File file : fs) {
	// String n = file.getName();
	// String[] ns = n.split("\\.");
	// // 盘符.名字.serialid.original_track.sound_track.kgame
	// item = new ItemInfo(n, ns[0], ns[1], ns[2], ns[3], ns[4]);
	// mList.add(item);
	// tips.setText("正在载入..." + n);
	// }
	// return true;
	// } catch (Exception e) {
	// e.printStackTrace();
	// tips.setText("kgame文件名不正确 [盘符.serialid.kgame]");
	// loadDialog.dismiss();
	// isError = true;
	// return false;
	// }
	// }

	private void switchTrack() {
		//TODO 暂时在小米上不工作 
//		ItemInfo item = mList.get(mIndex);
//		isOriginal = false;
//		mp.selectTrack(item.sound);
	}

	@Override
	public void finish() {
		super.finish();
		mp.stop();
		manager.stop();
	}

	@Override
	public void showProgress(String show) {
		// text.setText(show);
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		// mp.setDisplay(surfaceHolder);

	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2,
			int i3) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
	}

	Handler han = new Handler();

	private void postShowLeng(final int len, final float per) {
		han.postDelayed(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(MyActivity.this,
						"first len" + len + " per: " + per, Toast.LENGTH_LONG)
						.show();
			}

		}, 500);
	}

	private void _onPrepared() {
		dafenViewPrepared = false;
		mediaIsPrepared = false;

		isError = false;
		switchTrack();
		// manager.initAudioRecorder();
		final long startTime = System.currentTimeMillis();
		mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
			@Override
			public void onSeekComplete(MediaPlayer mp) {
				Log.d(TAG,"seek to " + cutTime + " coast time " + (System.currentTimeMillis() - startTime));
				// KGameLogUtil.getInstance().appendLog("seek to " + cutTime +
				// " coast time " + (System.currentTimeMillis() - startTime));
				Log.d(TAG, "before start, expect seek pos=>" + cutTime + ",real pos=>" + (mp.getCurrentPosition()));
				Log.d(TAG, "media player startTime =" + System.currentTimeMillis());
				mp.start();
				Log.d(TAG, "after start, expect seek pos=>" + cutTime + ",real pos=>" + (mp.getCurrentPosition()));
				manager.start(mp.getCurrentPosition());
//				new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						manager.start(mp.getCurrentPosition());
//					}
//				}, 2);
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						iniTime();
					}
				});
			}
		});
		mp.seekTo((int) cutTime);
		
		
	}

	private void judgeCanStart() {
		if (dafenViewPrepared && mediaIsPrepared) {
			Log.d(TAG, "dafenViewPrepared and mediaIsPrepared all prepared");
			_onPrepared();
		} else {
			Log.w(TAG, "don't call _onPrepared, mediaIsPrepared=>"
					+ mediaIsPrepared + ",dafenViewPrepared=>"
					+ dafenViewPrepared);
		}
	}

	private String[] readFiles() {
		File fl = new File("/sdcard/tsp_list.txt");
		List<String> resultList = new ArrayList<String>();
		if (fl.exists()) {

			// FileInputStream fls;
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(fl));
				String line;
				while ((line = reader.readLine()) != null) {
					resultList.add(line);
				}
				reader.close();
				String[] resultArr = new String[resultList.size()];
				resultList.toArray(resultArr);
				return resultArr;
				// fls = new FileInputStream(fl);
				// InputStreamReader inps=new InputStreamReader(fls);
				// char bf[]=new char[2048];
				// try {
				// int n=inps.read(bf);
				// String s=new String(bf,0,n);
				// return s.split("\r\n");
				//
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;

	}

	private void iniItems() {
		String fls[] = readFiles();
		if (fls == null || fls.length == 0)
			return;
		mList = new ArrayList<ItemInfo>(fls.length);
		ItemInfo item = null;
		int num = 0;
		for (String n : fls) {
			num++;
			Log.d("MyActivity", "n==>" + n + ",num=>" + num);
			String[] ns = n.split("\\.");
			if (ns == null || ns.length < 5)
				continue;
			item = new ItemInfo(n, ns[0], ns[1], ns[2], ns[3], ns[4]);
			mList.add(item);
		}

		Toast.makeText(this, "click play", Toast.LENGTH_LONG).show();
	}

	TextView tv;
	long frm_time;
	Runnable run_time = new Runnable() {

		@Override
		public void run() {
			long nt = System.currentTimeMillis() - frm_time;
			tv.setText((nt / 1000.f) + "");
			han.postDelayed(run_time, 1000);

		}

	};

	// Handler han=new Handler();
	private void iniTime() {

//		frm_time = System.currentTimeMillis();
//		run_time.run();

	}

	// [to surport next song]
	private void stop() {
		if (manager.isStart())
			manager.stop();
		sv_kg.stop();
		han.removeCallbacks(run_time);

	}

	@Override
	public void allScoreWhenEnd(float[] scoreArr) {
		// TODO Auto-generated method

	}

	@Override
	public void jiepaiWhenEnd(KGameJiepaiEntity jiepai) {
		// TODO Auto-generated method stub

	}

	@Override
	public void KGameResult(final SendKGChanllengeResults result) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(MyActivity.this, "得分" + result.toString(),
						Toast.LENGTH_LONG).show();

			}
		});
	}
}
