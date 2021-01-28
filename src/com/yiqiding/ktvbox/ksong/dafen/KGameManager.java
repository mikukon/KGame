package com.yiqiding.ktvbox.ksong.dafen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.hellojni.PitchAnalyzer;
import com.yiqiding.ktvbox.BuildConfig;
import com.yiqiding.ktvbox.R;
import com.yiqiding.ktvbox.config.KTVBoxPathManager;
import com.yiqiding.ktvbox.ksong.dafen.KSongDaFenManager.KSongResultCallback;
import com.yiqiding.ktvbox.ksong.dafen.KSongDaFenManager.PitchCallBack;
import com.yiqiding.ktvbox.libutils.KGameLogUtil;
import com.yiqiding.ktvbox.libutils.LogUtil;
import com.yiqiding.ktvbox.rpc.socket.data.SendKGChanllengeResults;
import com.yiqiding.ktvbox.structure.KGameJiepaiEntity;

import de.greenrobot.event.EventBus;

/**
 * Created by so898 on 14-7-22.
 */
public class KGameManager {
	private static final String TAG = "KGameManager";
	private KGameFileInfo fileInfo = null;
	private ArrayList<Integer> pitches = null;
	private ArrayList<KGamePitchInfo> allValidTemplatePoint = null;
	private int invalidPointCount = 0;// 无效点的数量
	private int allValidPointCount = 0;// 所有的有效点数量
	private int unCatchPointCount = 0;// 没有采集到的点
	private ArrayList<Float> frameLengthPitchList = null;
	private KGameAudioAnalyzer analyzer = null;

	// private static boolean DEBUG_KGAME = BuildConfig.DEBUG;
	private static boolean DEBUG_KGAME = false;
	private static boolean DEBUG_WRITE = false;

	private KGameResultListener listener;

	private float countTime = 0;
	private int pastCount = 0;
	private Timer timer = null;
	private float score = 0;
	private int uselessPts = 0;

	private long delayTime = 2000;

	private boolean isStart;

	private static final int TYPE_ONE = 1;
	private static final int TYPE_TWO = 2;
	private static int currentType = TYPE_TWO;

	private ArrayList<Double> allScores = null;

	private float stable;
	private double expressive;

	private static KGameManager mInstance;

	// 用于生成模版pitch计算使用
	private List<Integer> allRecordPitch;
	private String kgamePath;
	private String fileName;
	private int maxPitch = 0;
	private int minPitch = 70;

	private int perfectCount;
	private int greatCount;
	private int goodCount;
	private int coolCount;
	private int badCount;
	private int missCount;

	private boolean hasKGameResult = false;
	private KSongResultCallback kSongResultCallback;

	private SendKGChanllengeResults chanllengeResult;

	private ScoreFilter2 scoreFilter;
	// 打分模式仅仅只计算分数 不再通知UI刷新
	private boolean dafenMode = false;

	// k歌闯关赛 多久上报一次进度
	private static final int UPLOAD_FREQUNCE_TIME = 1000;
	private long lastUploadTime = 0;//
	private EventDafenProcess edprocessEvent;
	private EventDafenPingJia pingJiaEvent;

	private List<KgBcInfo> kgBcInofList;
	private KGamePingJiaListener pingJiaListener;
	private Context context;
	private MediaPlayer mMediaPlayer;
	private int lessThanFiveNum = 0;
	private int zeroNum = 0;

	/**
	 * 用于动态截取打分文件以及视频 实现快速打分功能
	 */
	private long cutStartTimeGlobal = 0;
	private PitchFilter pitchFilter;
	private long jniBasetime = 0;

	public interface KGamePingJiaListener {
		void showPingJia(int pingJiaId);
	}

	private boolean hasUpLoadResult = false;
	private boolean needToShutDown = false;

	private KGameManager(Context con) {
		// 如果是调试版本 debug直接生效
		// 如果是release版本 debug跟随设置
		this.context = con;
	}

	public static KGameManager getInstance(Context con) {
		if (mInstance == null) {
			mInstance = new KGameManager(con);
		}
		return mInstance;
	}

	public void setListener(KGameResultListener l) {
		listener = l;
	}

	public boolean isStart() {
		return isStart;
	}

	private BufferedWriter bw = null;

	private int ChaoShiNum = 0;
	private static final int MAX_TIMEOUT_NUM = 5;
	private void startWrite() {
		String path = "/sdcard/RecordScore.txt";
		File mFile = new File(path);
		try {
			if (!mFile.exists()) {
				mFile.createNewFile();
			}

			bw = new BufferedWriter(new FileWriter(mFile, true));
			bw.append("======================================================\n");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void write(String text) {
		try {
			bw.append(text + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeJiezou(String text) {
		// try {
		// jiezouBw.append(text + "\n");
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
	}

	private void writeError(String text) {
		KGameLogUtil.getInstance().appendError(text);
	}

	private void stopWrite() {
		try {
			if (bw == null) {
				return;
			}
			bw.append("======================================================\n");
			bw.flush();
			bw.close();
			bw = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param con
	 * @param sid
	 * @param cutStartTime
	 *            起始截取掉的时间
	 */
	public void loadKGameOfSid(Context con, long sid, long cutStartTime) {
		// fileInfo =
		// KGameFileAnalyzer.getGameFileFromServer("http://192.168.1.233/kgame/"
		// + sid + ".kgame");
		String path = KTVBoxPathManager
				.getPathDirByType(KTVBoxPathManager.TYPE_DAFEN_TEMPLATE)
				+ sid
				+ ".kgame";

		loadKGameOfSid(context, path, cutStartTime);
	}

	public void loadKGameOfSid(Context con, long sid) {
		// fileInfo =
		// KGameFileAnalyzer.getGameFileFromServer("http://192.168.1.233/kgame/"
		// + sid + ".kgame");
		String path = KTVBoxPathManager
				.getPathDirByType(KTVBoxPathManager.TYPE_DAFEN_TEMPLATE)
				+ sid
				+ ".kgame";

		loadKGameOfSid(context, path, 0);
	}

	public void initAudioRecorder() {
		if (analyzer == null) {
			analyzer = new KGameAudioAnalyzer();
		}
		analyzer.initAudioRecorder();
	}

	long lastPastTime = 0;
	public void start(int musicCurrentPos){
		if (null == fileInfo) return;
		if (DEBUG_KGAME) startWrite();
		if (null != timer) {
			LogUtil.i("timer not null, cancel, new");
			timer.cancel();
			timer.purge();
		}
		hasUpLoadResult = false;
		isStart = true;
		pastCount = (int) (1 / countTime);
		score = 0;
		stable = 0;
		perfectCount = 0;
		greatCount = 0;
		goodCount = 0;
		coolCount = 0;
		badCount = 0;
		missCount = 0;
		lessThanFiveNum = 0;
		zeroNum = 0;
		needToShutDown = false;
		chanllengeResult = new SendKGChanllengeResults();
		invalidPointCount = 0;
		unCatchPointCount = 0;
		ChaoShiNum = 0;
		allRecordPitch = new ArrayList<Integer>();
		if (analyzer != null) analyzer.start();
		if (pcCallback != null) {
			pcCallback.onStart(musicCurrentPos, cutStartTimeGlobal);
		}

		scoreFilter = new ScoreFilter2(10000);
		pitchFilter = new PitchFilter(maxPitch, minPitch);

		hasKGameResult = false;
		timer = new Timer();
		// 打分功能的相对时间流逝起点时间
		final long musicPosOffset = musicCurrentPos - cutStartTimeGlobal;
		Log.d(TAG, "musicCurrentPos=>" + musicCurrentPos + ",cutTime=>" + cutStartTimeGlobal + ",musicPosOffset=>" + musicPosOffset);
		KGameLogUtil.getInstance().appendLog("musicCurrentPos=>" + musicCurrentPos + ",cutTime=>" + cutStartTimeGlobal + ",musicPosOffset=>" + musicPosOffset);

		//设置与打分模板对比的基础时间
		final long baseStartTime = System.currentTimeMillis() - musicPosOffset;

		//TODO 定义该变量目的是为了让录音线程先开始 获取打分才会有数据
		final long calcThreadDelayTime = 80;
		//设置向jni获取数据的基础时间
		jniBasetime = System.currentTimeMillis() + calcThreadDelayTime;

		Log.d(TAG, "get pitch thread startTime=" + baseStartTime);
		
		final int period = (int) countTime;
		Log.d(TAG, "period=>" + period);
		// 创建一个打分事件 抛给上屏回显
		edprocessEvent = new EventDafenProcess();
		EventBus.getDefault().post(edprocessEvent);
		//
		pingJiaEvent = new EventDafenPingJia();
		EventBus.getDefault().post(pingJiaEvent);
		final long endTime = (long) (allValidTemplatePoint.get(allValidTemplatePoint.size()-1).getStartTime()+5000);

//		lastPastTime = 0;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				// final long pastTime;
				// if(mMediaPlayer==null){
				// return;
				// }else{
				// try {
				// pastTime = mMediaPlayer.getCurrentPosition();
				// } catch (Exception e) {
				// e.printStackTrace();
				// LogUtil.i("mMediaPlayer.getCurrentPosition() Excetion");
				// return;
				// }
				//
				// }
				/**
				 * 用于跟模板时间戳进行对比
				 */
				final long pastTime = System.currentTimeMillis() - baseStartTime;
//				if(pastTime < lastPastTime){
//					Log.e(TAG, "pastTime lower than lastPastTime " + (lastPastTime - pastTime));
//				}
//				if(pastTime - lastPastTime > 1000){
//					Log.d(TAG, "pastTime=>" + pastTime+"-----------"  + ",allValidTemplatePoint.size()=>" + allValidTemplatePoint.size());
//					lastPastTime = pastTime;
//				}
				int size = allValidTemplatePoint.size();
				//final long endTime = (long) (allValidTemplatePoint.get(size-1).getStartTime()+10000);
				KGamePitchInfo point = null;
				boolean doCalc = false;
				if (size > 0) {
					point = allValidTemplatePoint.get(0);
					if(point == null){
						Log.e(TAG, "point is null, return, allValidTemplatePoint size=>" + allValidTemplatePoint.size());
						return;
					}
					final float pointStartTime = point.getStartTime();
					//	LogUtil.i("pointStartTime"+pointStartTime+"-----------");
					//		Log.d(TAG, "pastTime==>" + pastTime + ",endTime=>" + endTime+"size"+allValidTemplatePoint.size());
					if (pastTime > pointStartTime) {
						if (pastTime >= endTime) {
							needToShutDown = true;
						} else {
							doCalc = true;
						}

					}
				}

//				Log.d(TAG, "doCalc=>" + doCalc);
				boolean realDoCalc = doCalc;
				doCalc = true;
				if (doCalc) {
					// int getPitch = pitches.get(pastCount);
					if(point == null){
						Log.e(TAG, "after doCalc, point is null, return, allValidTemplatePoint size=>" + allValidTemplatePoint.size());
						return;
					}
					int getPitch = point.getPitch();
					float analyzerPitch = -11111111;
					double cent = 0;
					/**
					 * 录音过去了多久时间
					 */
					final long audioPastTime = System.currentTimeMillis() - jniBasetime;

					if (KGameAudioAnalyzer.USE_JNI_GETPITCH) {
						long now = System.currentTimeMillis();
						//long startTimeDelta = now-baseStartTime;
						long startTimeDelta = (long) (allValidTemplatePoint.get(0).getStartTime());

						int analyserId = 0;
						if (analyzer != null) {
							analyserId = analyzer.getJniAnalyserId();
						}
						if (analyserId == 0) {
							LogUtil.i("analyserId=>" + analyserId + ",analyzer obj=>" + analyzer + ", will call onError");
							if (kSongResultCallback != null) {
								kSongResultCallback.onError("pitch analyzer has been deleted, will stop timer , stop dafen");
							}else {
								LogUtil.e("kSongResultCallback is null");
							}
							stop();
							return;
						}
						if (analyzer != null) {
//							Log.d(TAG, "will fetch pitch, send param pastTime=>" + audioPastTime);
							analyzerPitch = PitchAnalyzer
									.PitchAnalyzerGetPitch(
											analyzer.getJniAnalyserId(),
//											startTimeDelta - musicPosOffset + 200,
											audioPastTime/*+ 200*/,
											period);
//							Log.d(TAG, "analyzerPitch=>" + analyzerPitch);
						}

						if(realDoCalc){
//							Log.d(TAG, "realDoCalc, allValidTemplatePoint list remove position 0,analyzerPitch=>" + analyzerPitch);
							allValidTemplatePoint.remove(0);
						}

						if(analyzerPitch==-2){
							ChaoShiNum++;
							Log.d(TAG, "ChaoShiNum=>" + ChaoShiNum +"startTimeDelta==>"+startTimeDelta+",pastTime==>" + audioPastTime +",analyzerPitch"+analyzerPitch);
							if(ChaoShiNum >= MAX_TIMEOUT_NUM){
								Log.w(TAG, "more than " + MAX_TIMEOUT_NUM + " times occur -2, reset Jni and sleep 40ms");
								PitchAnalyzer.PitchAnalyzerReset(analyzer.getJniAnalyserId());
								//reset ChaoShiNum
								ChaoShiNum = 0;

								try {
									//wait for audio data send to jni
									Thread.sleep(40);
									jniBasetime = System.currentTimeMillis();
									Log.w(TAG, "rest jniBasetime=>" + jniBasetime);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}

							return;
						}

						//reset ChaoShiNum
						ChaoShiNum = 0;

						//analyzerPitch = getPitchFromLocal(startTimeDelta, period);
						if(analyzer!=null){
							cent = calcCentByPitch(PitchAnalyzer
									.PitchAnalyzerGetFrequency(
											analyzer.getJniAnalyserId(),
//											(startTimeDelta - cutStartTimeGlobal) + 200,
											audioPastTime,
											period));// 分贝输入
						}
					} else {
						analyzerPitch = analyzer.getPitch();
					}
					// double cent = analyzer.getCents();//分贝输入
//					Log.d(TAG, "analyzerPitch =>" + analyzerPitch + "cent=>" + cent);
					//TODO 没有波浪图的位置也需要展示小球当前位置
//					if (pcCallback != null && getPitch != -200) {
					if (pcCallback != null) {
						// long now = System.currentTimeMillis();
						// long startTimeDelta = now - baseStartTime;
						// Log.d(TAG, "will call currentPitch at startTime=" +
						// startTimeDelta + "ms, analyzerPitch=>" +
						// analyzerPitch);
						
						final int filterPitch = pitchFilter.filterPitch((int)(pastTime + 200), (int) analyzerPitch);
//						Log.d(TAG, "analyzerPitch=>" + (int) analyzerPitch + ",filterPitch=>" + filterPitch);
						pcCallback.currentPitch(filterPitch);
					}

					if(!realDoCalc) {
//						Log.d(TAG, "current time not real calc score, just show pitch, return");
						return;
					}

					float tmpScore = score;

					switch (currentType) {
					case TYPE_ONE:
						// useCalcTypeOne(getPitch, analyzerPitch);
						break;
					case TYPE_TWO:
						useCalcTypeTwo(getPitch, analyzerPitch, cent);
						break;
					default:
						break;
					}

					allRecordPitch.add((int) analyzerPitch);

					if (DEBUG_WRITE) {
						write("template=" + getPitch + " analyzerPitch="
								+ analyzerPitch + " diff="
								+ (getPitch - analyzerPitch) + ",score="
								+ (score - tmpScore) + ",cents="
								+ analyzer.getCents());
					}

				} else {

//					Log.d(TAG, "start time is " + point.getStartTime() + ", but pastTime is " + pastTime + ", so do nothing");
				}
				if (needToShutDown) {
					allValidTemplatePoint.clear();
				}
				if (allValidTemplatePoint.size() == 0) {
					LogUtil.i("allValidPointCount" + unCatchPointCount
							+ "unCatchPointCount" + invalidPointCount
							+ "unCatchPointCount" + unCatchPointCount);
					// float result = score/(pitches.size() - uselessPts);
					if (DEBUG_KGAME)
						Log.d(TAG, "score=>" + score
								+ ",allValidTemplatePoint size=>"
								+ allValidPointCount + ",invalidPointCount=>"
								+ invalidPointCount + ",unCatchPointCount=>"
								+ unCatchPointCount);
					// 音准
					int num = zeroNum > 130 ? 130 : zeroNum;
					float result = score
							/ (allValidPointCount - invalidPointCount - num - lessThanFiveNum)
							* 100;
					// 精确到小数点后2位
					result = get2floatNum(result);
					float pefect = (float) (perfectCount * 100.0 / allRecordPitch
							.size());

					int tempMiss = missCount + invalidPointCount;
					if (tempMiss > allRecordPitch.size()) {
						tempMiss = allRecordPitch.size();
					}

					float miss = (float) (tempMiss * 100.0 / allRecordPitch
							.size());

					// 稳定性
					float stableResult = ((allValidPointCount - invalidPointCount) - unCatchPointCount)
							* 100 / (allValidPointCount - invalidPointCount);

					// 表现力
					if (DEBUG_KGAME)
						Log.d(TAG, "expressive=>" + expressive
								+ ",allValidPointCount=>" + allValidPointCount
								+ ",invalidPointCount=" + invalidPointCount
								+ ",unCatchPointCount=>" + unCatchPointCount);
					double expressiveResult = 60 + (expressive / (allValidPointCount
							- invalidPointCount - unCatchPointCount)) * 2;
					if (expressiveResult > 100)
						expressiveResult = 100;
					if (expressiveResult < 0) {
						expressiveResult = 0;
					}
					// 节奏
					float rhythmCount = 0;
					// float tmpRhythmCount = result * 2 - 1;
					// Log.d(TAG, "allScores size=> " + allScores.size()
					// + ",tmpRhythmCount=>" + tmpRhythmCount);
					for (int i = 0; i < allScores.size(); i++) {

						rhythmCount += (float) Math.abs(allScores.get(i)
								- result);

						if (DEBUG_KGAME)
							writeJiezou("i=>" + i + " score=>"
									+ allScores.get(i) + " rhythmCount++="
									+ rhythmCount);
					}
					rhythmCount = rhythmCount * 100 / allScores.size();
					float rhythm = (float) (1 / (rhythmCount * 0.0045) + 60);
					if (rhythm >= 100) {
						rhythm = 100;
					}
					Log.d(TAG, "rhythmCount=>" + rhythmCount
							+ ",allScores.size()=>" + allScores.size()
							+ ",rhythm=>" + rhythm);
					if (DEBUG_KGAME)
						writeJiezou("(allScores.size() - rhythmCount)/allScores.size()="
								+ rhythm);
					// 技法
					float technique = 0;
					for (int i = 0; i < allScores.size(); i++) {
						if (allScores.get(i) > 0.99) {
							technique++;
						}
					}
					// technique = (technique / allScores.size()) * 400;
					double techRate = technique * 100 / allScores.size() + 1;
					Log.d(TAG, "technique=>" + technique
							+ ",allScores.size()=>" + allScores.size()
							+ ",techRate=>" + techRate);
					technique = (float) (Math.log(techRate + 5) * 22 - 2);
					if (technique > 100) {
						technique = 100;
					}

					// 总分
					double lastOutput = result / 100 * 0.4 + stableResult / 100
							* 0.2 + technique / 100 * 0.2 + rhythm / 100 * 0.2;
					lastOutput = lastOutput * 100;

					if (DEBUG_KGAME)
						Log.i(TAG, "Result: " + result);
					if (DEBUG_KGAME)
						Log.i(TAG, "Stable: " + stableResult);
					if (DEBUG_KGAME)
						Log.i(TAG, "Expressive: " + expressiveResult);
					if (DEBUG_KGAME)
						Log.i(TAG, "Rhythm: " + rhythm);
					if (DEBUG_KGAME)
						Log.i(TAG, "Technique: " + technique);
					if (DEBUG_KGAME)
						Log.i(TAG, "Last Result: " + lastOutput);
					if (DEBUG_KGAME)
						write("Result: " + result);
					if (DEBUG_KGAME)
						write("Stable: " + stableResult);
					if (DEBUG_KGAME)
						write("Expressive: " + expressiveResult);
					if (DEBUG_KGAME)
						write("Rhythm: " + rhythm);
					if (DEBUG_KGAME)
						write("Technique: " + technique);
					if (DEBUG_KGAME)
						write("Last Result: " + lastOutput);
					// 结束时上传一下分数
					if (edprocessEvent != null) {
						edprocessEvent.setScore(result);
						edprocessEvent.setMissRate(miss);
						edprocessEvent.setPerfectRate(pefect);
						EventBus.getDefault().post(edprocessEvent);
						hasUpLoadResult = true;
					}
					stop();

					int[] arr = { perfectCount, greatCount, goodCount,
							coolCount, badCount, missCount, };
					if (result == 0 || allScores.size() <= 20) {
						rhythm = 0;
						expressiveResult = 0;
						stableResult = 0;
						technique = 0;
						allScores.clear();
					}
					fillChanllengeResult(1, result, arr, (int) rhythm,
							(int) expressiveResult, (int) stableResult,
							(int) technique, (int) result, miss, pefect);

					LogUtil.i("kgame result=>" + chanllengeResult.toString());
					listener.KGameResult(chanllengeResult);

					float[] floatAllScore = new float[allScores.size()];
					int length = floatAllScore.length;
					for (int i = 0; i < length; i++) {
						floatAllScore[i] = (float) ((double) allScores.get(i));
					}
					listener.allScoreWhenEnd(floatAllScore);

					KGameJiepaiEntity jiepai = new KGameJiepaiEntity();
					jiepai.setPerfectNum(perfectCount);
					jiepai.setGreatNum(greatCount);
					jiepai.setGoodNum(goodCount);
					jiepai.setCoolNum(coolCount);
					jiepai.setBadNum(badCount);
					jiepai.setMissNum(missCount);
					listener.jiepaiWhenEnd(jiepai);
					hasKGameResult = true;
					if (kSongResultCallback != null) {
						LogUtil.i("this time we real calc kgame result, invoke callback realHasKSongResult");
						kSongResultCallback.realHasKSongResult();
					}

				}
				if (!hasUpLoadResult) {
					tryUploadScoreProcess(pastTime);
				}

//				Log.d(TAG,
//						"timer task one task coast time =>"
//								+ (System.currentTimeMillis() - methodStartTime)
//								+ " ms");
			}
		}, calcThreadDelayTime, period / 2);
	}

	protected float get2floatNum(float result) {
		int scale = 2;// 设置位数
		int roundingMode = 4;// 表示四舍五入，可以选择其他舍值方式，例如去尾，等等.
		BigDecimal bd = new BigDecimal((double) result);
		bd = bd.setScale(scale, roundingMode);
		result = bd.floatValue();
		return result;
	}

	public void setDelayTime(long time) {
		delayTime = time;
	}

	public boolean hasKGameResult() {
		return hasKGameResult;
	}

	public void stop() {
		LogUtil.i("stop...");
		if (null != timer) {
			timer.cancel();
			timer.purge();
			timer = null;
		} else {
			LogUtil.w("timer is null");
		}
		if (mMediaPlayer != null) {
			mMediaPlayer = null;
		}

		if (DEBUG_KGAME)
			stopWrite();
		isStart = false;
		if (analyzer != null) {
			analyzer.stop();
			analyzer = null;
		} else {
			LogUtil.w("analyzer is null");
		}
		if (pcCallback != null) {
			pcCallback.onStop();
			pcCallback = null;
		} else {
			LogUtil.w("pcCallback is null");
		}
		// 重新恢复为非打分歌曲模式
		setDafenMode(false);
		// #885 K歌比赛结束，排行榜未弹出 连续k歌时候出现的bug
		uselessPts = 0;

		// 重置打分过程中上报相关的变量
		edprocessEvent = null;
		pingJiaEvent = null;
		lastUploadTime = 0;
	}

	private double calcCentByPitch(double frequency) {
		// if (analyzerPitch == -1) {
		// return 0;
		// }else {
		// double frequency = Math.pow(2, (analyzerPitch + 60 + 3)/12) * 6.875;
		// Cents relative to reference
		double cf = -12.0 * log2(440 / frequency);
		double nearest = 440 * Math.pow(2.0, Math.round(cf) / 12.0);
		double cents = -12.0 * log2(nearest / frequency) * 100.0;
		return cents;
		// }
	}

	// Log2
	protected double log2(double d) {
		return Math.log(d) / Math.log(2.0);
	}

	// bill 的打分算法
	private void useCalcTypeOne(int getPitch, int analyzerPitch) {
		if (getPitch == -200) {
			// do nothing
		} else if (analyzerPitch < -200) {
			// do nothin
		} else if (getPitch == analyzerPitch
				|| (getPitch - 5 <= analyzerPitch && getPitch + 5 >= analyzerPitch)) {
			score++;
		} else if (getPitch - 8 <= analyzerPitch
				&& getPitch + 8 >= analyzerPitch) {
			score += 0.9;
		} else if (getPitch - 10 <= analyzerPitch
				&& getPitch + 10 >= analyzerPitch) {
			score += 0.8;
		} else if (getPitch - 12 <= analyzerPitch
				&& getPitch + 12 >= analyzerPitch) {
			score += 0.7;
		} else if (getPitch - 15 <= analyzerPitch
				&& getPitch + 15 >= analyzerPitch) {
			score += 0.6;
		} else if (getPitch - 20 <= analyzerPitch
				&& getPitch + 20 >= analyzerPitch) {
			score += 0.5;
		} else {
			score += 0.4;
		}
	}

	private void useCalcTypeTwo(int getPitch, float analyzerPitch, double cent) {
		if (getPitch == -1) {
			invalidPointCount++;
			// do nothing
		} else if (analyzerPitch <= -1) {
			unCatchPointCount++;
			score += 0.0;
			missCount++;
			// Log.d(TAG, "template Pitch=>" + getPitch + ",analyzerPitch=>" +
			// analyzerPitch + ",score=>0.5");
			// do nothing
		} else {
			float abs = Math.abs(getPitch - analyzerPitch);
			// Log.d(TAG, "res abs=>" + abs);

			// int difference = getPitch - analyzerPitch;
			// if (difference < 0)
			// difference = -difference;
			//
			// if (difference <= 14 && pcCallback != null) {
			// pcCallback.onPitchMatch();
			// }
			//
			// if (difference == 0) {
			// perfectCount++;
			// } else if (difference < 4) {
			// greatCount++;
			// } else if (difference < 11) {
			// goodCount++;
			// } else if (difference < 21) {
			// coolCount++;
			// } else {
			// missCount++;
			// }

			// Binary-Search
			double res;
			try {
				res = BinarySearch(abs);
			} catch (Exception e) {
				res = 50;
				e.printStackTrace();
			}

			if (res >= 96) {
				perfectCount++;

				if (pingJiaEvent != null) {
					pingJiaEvent.setResouceId(R.drawable.perfect);
					EventBus.getDefault().post(pingJiaEvent);
				}

				if (pingJiaListener != null) {
					pingJiaListener.showPingJia(R.drawable.perfect);
				}

			} else if (res >= 91) {
				greatCount++;
				if (pingJiaEvent != null) {
					pingJiaEvent.setResouceId(R.drawable.great);
					EventBus.getDefault().post(pingJiaEvent);
				}
				if (pingJiaListener != null) {
					pingJiaListener.showPingJia(R.drawable.great);
				}
			} else if (res >= 81) {
				goodCount++;
				if (pingJiaEvent != null) {
					pingJiaEvent.setResouceId(R.drawable.good);
					EventBus.getDefault().post(pingJiaEvent);
				}
				if (pingJiaListener != null) {
					pingJiaListener.showPingJia(R.drawable.good);
				}
			} else if (res >= 71) {
				coolCount++;
			} else if (res >= 61) {
				badCount++;
			} else {
				// Log.i("score <=60 missCount++","score"+
				// score+"analyzerPitch"+analyzerPitch);
				missCount++;
			}
			res = res / 100;
			// Log.d(TAG, "template abs=>" + abs + ",res=>" +
			// res+"perfect"+perfectCount+"great"+greatCount+"good"+goodCount+"cool"+coolCount+"bad"+badCount+"miss"+missCount
			// );

			if (res == 0.0) {
				zeroNum++;
			}
			if (res > 0.0 && res < 0.65 && lessThanFiveNum < 121) {
				lessThanFiveNum++;
			} else {
				score += res;
			}
			expressive += cent;
			if (allScores.size() != 0) {
				double tmpDecount = res - allScores.get(allScores.size() - 1);
				if (tmpDecount < -0.01) {
					stable++;
				}
			}

			allScores.add(res);
			// Log.d(TAG, "template Pitch=>" + getPitch + ",analyzerPitch=>" +
			// analyzerPitch + ",score=>" + res);
		}
	}

	PitchCallBack pcCallback;

	public void setPitchCallBack(PitchCallBack callback) {
		pcCallback = callback;
	}

	// modify by ytxu 2015-9-24 ---> the private method is not called, and the
	// jiepai array`s size is 6, but used index is over it`s size
	// private void fillChanllengeResultDefault(float score){
	// chanllengeResult.score = score;
	// chanllengeResult.biaoxianli = 0;
	// int[] jiepai = {0,0,0,0,0,0};
	// chanllengeResult.initJiepai(jiepai[0], jiepai[1], jiepai[2], jiepai[3],
	// jiepai[4],jiepai[5],jiepai[6],jiepai[7]);
	// chanllengeResult.jiezou = 0;
	// chanllengeResult.jiqiao = 0;
	// chanllengeResult.normal = 0;
	// chanllengeResult.wending = 0;
	// chanllengeResult.yinzhun = 0;
	// }

	private void fillChanllengeResult(int normal, float score, int[] jiepai,
			int jiezou, int biaoxianli, int wending, int jiqiao, int yinzhun,
			float miss, float pefect) {
		chanllengeResult.normal = normal;
		chanllengeResult.score = score;
		chanllengeResult.initJiepai(jiepai[0], jiepai[1], jiepai[2], jiepai[3],
				jiepai[4], jiepai[5], miss, pefect);
		chanllengeResult.jiezou = jiezou;
		chanllengeResult.biaoxianli = biaoxianli;
		chanllengeResult.jiqiao = jiqiao;
		chanllengeResult.wending = wending;
		chanllengeResult.yinzhun = yinzhun;
	}

	public void setKSongResultCallback(KSongResultCallback callback) {
		kSongResultCallback = callback;
		LogUtil.w("kSongResultCallback=>" + kSongResultCallback);
	}

	public boolean hasKSongResultCallback() {
		return kSongResultCallback != null;
	}

	public void setDafenMode(boolean isDafenMode) {
		dafenMode = true;
	}

	/**
	 * 设置要dump的文件路径
	 * 
	 * @param 要生成的录音文件的绝对路径
	 */
	public void setDumpPcmPath(String fullPath) {
		if (analyzer != null) {
			analyzer.setDumpPcmFile(fullPath);
		}
	}

	/**
	 * 根据当前计算的得分 上报给上屏显示
	 */

	private void tryUploadScoreProcess(long pastTime) {
		// 计算距离上一次上报过去了多久
		long offset = pastTime - lastUploadTime;
		// 如果时间大于上报间隔变量UPLOAD_FREQUNCE_TIME 上报 更新时间
		if (offset >= UPLOAD_FREQUNCE_TIME) {
			lastUploadTime = pastTime;
//			Log.d(TAG, "time duration bigger than 1s, upload");
			upLoadScore(pastTime);
		}else {
//			Log.d(TAG, "time duration lower than 1s, do nothing");
		}
	}

	// private void tryUploadScoreProcess(long pastTime) {
	// long pointTime = UPLOAD_FREQUNCE_TIME * uploadTimes + uploadTimes;
	// if (pastTime >= pointTime) {
	// uploadTimes++;
	// Log.d(TAG, "uploadTimes=>" + uploadTimes);
	// upLoadScore();
	//
	// }
	// }

	private void upLoadScore(long pastTime) {
		int size = allRecordPitch.size();
		if (size == 0) {
			return;
		}
		//为了打分的公平性 用户没唱或者录不到的时候的点也计算
		int tempMiss = missCount+ invalidPointCount;
//		Log.w(TAG, "template invalidPointCount=>" + invalidPointCount);
		if(tempMiss>size){
			tempMiss=size;
		}

		float perfectRate = (float) (perfectCount * 100.0 / size);
		float missRate = (float) (tempMiss * 100.0 / size);
		// 这个只是音程的计算 最后得分是要集合音准 表现力等。
//		float result = score / size * 100;
		int num = zeroNum > 130 ? 130 : zeroNum;
		int denominator = size - invalidPointCount - num - lessThanFiveNum;
		float result = denominator == 0 ? 0f : score / denominator * 100;
		final float temp = result;
//		Log.d(TAG, "score=>" + score + ",size=>" + size + ",invalidPointCount=>" + invalidPointCount + ",num=>" + num + ", lessThanFiveNum=>" + lessThanFiveNum);
		result = scoreFilter.filterScore(result, pastTime);
		Log.d(TAG, "orignal score=>" + temp + ",newScore=>" + result);
//		Log.d(TAG, "score=>" + score + ",size=>" + size);
//		Log.d(TAG, "perfectRate=>" + perfectRate + ",missRate=>" + missRate + ",result=>" + result);
		if(edprocessEvent!=null){
			edprocessEvent.setPerfectRate(perfectRate);
			edprocessEvent.setMissRate(missRate);
//			if(BuildConfig.DEBUG){
//				edprocessEvent.setScore(99);
//			}else{
				edprocessEvent.setScore(result);
//			}
			EventBus.getDefault().post(edprocessEvent);
		}
	}

	private float BinarySearch(float abs) throws Exception {
		if (kgBcInofList.size() == 0) {
			throw new Exception("KGDafeng jizhi  load failed");
		}
		int low = 0;
		int high = kgBcInofList.size() - 1;
		KgBcInfo.compareType type;
		while ((low <= high) && (low <= kgBcInofList.size() - 1)
				&& (high <= kgBcInofList.size() - 1)) {
			int middle = low + ((high - low) >> 1);

			type = kgBcInofList.get(middle).isContain(abs);
			switch (type) {
			case EQUALS:
				return kgBcInofList.get(middle).getRes();
			case LESSTHAN:
				high = middle - 1;
				break;
			case MORETHAN:
				low = middle + 1;
				break;
			default:
				break;
			}
		}
		return 0;
	}

	public void setPingJiaListener(KGamePingJiaListener pingJiaListener) {
		this.pingJiaListener = pingJiaListener;
	}

	public void setmMediaPlayer(MediaPlayer mMediaPlayer) {
		this.mMediaPlayer = mMediaPlayer;
	}

	public void loadKGameOfSid(Context con, String kgamePath) {
		loadKGameOfSid(con, kgamePath, 0);
	}

	// TODO debugCode
	public void loadKGameOfSid(Context con, String kgamePath, long cutStartTime) {
		Log.d(TAG, "cutStartTime=>" + cutStartTime);
		String path = kgamePath;

		// TODO temp code must be delete
		// path = "/sdcard/1780809234.kgame";

		cutStartTimeGlobal = cutStartTime;

		File mFile = new File(path);
		if (!mFile.exists()) {
			LogUtil.e("loadKGame faied because file " + path + " is not exits");
			writeError("loadKGame faied because file " + path + " is not exits");
			return;
		}

		fileInfo = KGameFileAnalyzer.getGameFileFromLocal(path);
		if (kgBcInofList == null) {
			kgBcInofList = KGameFileAnalyzer.getKgBcInfoFileFromLocal(context
					.getResources());
			// kgBcInofList =
			// KGameFileAnalyzer.getKgBcInfoFileFromLocal("/sdcard/1.csv");
		}

		if (null == fileInfo) {
			LogUtil.i("ytxu wy findbugs: fileInfo is null...");
			writeError("ytxu wy findbugs: fileInfo is null , return function loadKGameOfSid");
			return;
		}
		LogUtil.d("fileInfo=>" + fileInfo);
		// 新的模版中已经有了countTime
		// countTime = 60 / (fileInfo.getBpm() * 4);

		countTime = fileInfo.getCountTime();
		LogUtil.d("countTime=>" + countTime);
		if (countTime <= 0) {
			countTime = 1000;
			LogUtil.w("countTime =" + countTime
					+ " is lower than 0, so give it a default value , 1000");
		}

		if (fileInfo == null) {
			writeError("fileInfo is null , return function loadKGameOfSid");
			return;
		}
		if (allRecordPitch == null)
			allRecordPitch = new ArrayList<Integer>();
//		if (analyzer == null) {
//			LogUtil.i("init new analyzer");
//			analyzer = new KGameAudioAnalyzer();
//		} else {
//			LogUtil.i("analyzer is already init, do nothing");
//		}
		LogUtil.i("analyzer id=>" + analyzer);
		pitches = new ArrayList<Integer>();
		allScores = new ArrayList<Double>();
		uselessPts = 0;
		expressive = 0;
		frameLengthPitchList = new ArrayList<Float>();
		allValidTemplatePoint = new ArrayList<KGamePitchInfo>();
		// int tmpGap = ((int)fileInfo.getGap() < 0 ? -(int)fileInfo.getGap() :
		// (int)fileInfo.getGap());
		for (int i = 0; i < 2 / countTime; i++) {
			pitches.add(-200);
			uselessPts++;
		}
		if (fileInfo == null) {
			LogUtil.e("fileInfo is null");
		}

		// TODO debug code
		int summaryDropCount = 0;
		int detailDropCount = 0;

		int summaryCount = 0;
		
		for (KGamePitchInfo info : fileInfo.getPitchInfos()) {
			if (info == null) {
				LogUtil.e(" info is null ");
				continue;
			}
			switch (info.getType()) {
			case SUMMARY:
				
				summaryCount++;
				// drop item depend on cuttime
				// 此处是新版本功能
				if (info.getStartTime() < cutStartTime) {
					summaryDropCount++;
					continue;
				}

				int p = info.getPitch();
				if (p > maxPitch) {
					maxPitch = p;
				} else if (p < minPitch && p != -1) {
					minPitch = p;
				}
				frameLengthPitchList.add(info.getStartTime() - cutStartTime + 30);
				frameLengthPitchList.add(info.getLength());
				frameLengthPitchList.add((float) info.getPitch());
				break;
			case DETAIL:
				// drop item depend on cuttime
				// 此处是新版本功能
				if(frameLengthPitchList.size() ==0){
					detailDropCount++;
					continue;
				}
//				LogUtil.i("detail newDropTime=>" + newDropTime);
				//校正打分点时间
				info.setStartTime((info.getStartTime() - cutStartTime));
				allValidTemplatePoint.add(info);
				break;

			default:
				break;
			}
		}

		LogUtil.i("summaryCount=>" + summaryCount);
		
		LogUtil.i("cutStartTime=>" + cutStartTime + ", summaryDropCount=>"
				+ summaryDropCount + ";;detailDropCount=>" + detailDropCount);

		allValidPointCount = allValidTemplatePoint.size();
		Log.d(TAG, "allValidPointCount=>" + allValidPointCount);
		if (allValidPointCount == 0) {
			LogUtil.e("allValidPointCount is zero, may be the template is error, path=>"
					+ path);
			if (kSongResultCallback != null) {
				kSongResultCallback.onError("parse template error");
			} else {
				LogUtil.e("kSongResultCallback is null");
			}
		}

		if (kSongResultCallback != null) {
			kSongResultCallback.onLoadKGameTemplateOver();
		}

		initAudioRecorder();

		if (pcCallback != null) {
			int size = frameLengthPitchList.size();
			float[] callbackarr = new float[size];
			for (int i = 0; i < callbackarr.length; i++) {
				callbackarr[i] = frameLengthPitchList.get(i);
			}
			LogUtil.i("will call onPichArrayLoaded");
			if (pcCallback == null) {
				LogUtil.i("callback is null, maybe this kg has been canceled");
			} else {
				pcCallback.onPichArrayLoaded(callbackarr, countTime, maxPitch,
						minPitch, "1000000");
			}
		} else {
			LogUtil.w("callback is null, maybe this kg has been canceled, auto invoke stop()");
			stop();
		}
	}
	
//	long timerStart = 0;
}
