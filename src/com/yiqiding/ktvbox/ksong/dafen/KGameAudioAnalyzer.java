package com.yiqiding.ktvbox.ksong.dafen;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Debug;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.example.hellojni.PitchAnalyzer;
import com.yiqiding.ktvbox.config.KTVBoxPathManager;
import com.yiqiding.ktvbox.libutils.KGameLogUtil;
import com.yiqiding.ktvbox.libutils.LogUtil;
import com.yiqiding.ktvbox.widget.KGCGSNotifyView;

/**
 * Created by so898 on 14-7-22.
 */
public class KGameAudioAnalyzer {
	private static final String TAG = "KGameAudioAnalyzer";
	public static final boolean USE_JNI_GETPITCH = true;
    private static final int OCTAVE = 12;
    private static final String notes[] =
            {"C", "C", "D", "E", "E", "F",
                    "F", "G", "A", "A", "B", "B"};
    //生成具体的pcm数据
    private static boolean DEBUG_PCM = true;
    private static int RECORD_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    
    private Audio audio = new Audio();
    //jni实现的pitch分析器
    private volatile int cAnalyzerId = 0;
    FileOutputStream fos = null;  
    DataOutputStream dos = null;
    private String fileName;
    private String fileDir;
    //用于计算这首歌录了多久
    private long startTime = -1;
    //用来表示是否已经停止了录音分析 KGameAudioAnalyzer每次都会初始化 
    private boolean hasStopAudio = false;
    private boolean isRealStartRecord = false;

    public KGameAudioAnalyzer() {
        super();
        //默认debug pcm是负值
        DEBUG_PCM = false;
        Log.d(TAG, "KGameAudioAnalyzer init, init cAnalyzerId = 0");
		setDumpPcmFile("/sdcard/ppp/test.pcm");
    }

    public String getFileName() {
    	if (fileName == null && "".equals(fileName)) {
			fileName = "test";
		}
		return fileName;
	}
    
    private String getCurrentFilePcmPath(){
    	return getFileDir() + getFileName() + ".pcm";
    }
    
    private String getCurrentFileWavPath(){
    	return getFileDir() + getFileName() + ".wav";
    }
    
    private String getFileDir(){
    	if (TextUtils.isEmpty(fileDir)) {
			fileDir = "/sdcard/ppp/";
		}
    	return fileDir;
    }

    /**
     * 设置要dump的文件路径
     * @param fullPath 要生成的录音文件的绝对路径
     */
	public void setDumpPcmFile(String fullPath) {
		//此处设置该变量为true  当前这次k歌打分会生成对应文件
		//一旦重新构造这个类  会把该变量重置
		DEBUG_PCM = true;
		int lastXieGangIndex = fullPath.lastIndexOf("/");
		int lastPointIndex = fullPath.lastIndexOf(".");
		if (lastXieGangIndex == -1){
			fileDir = KTVBoxPathManager.getPathDirByType(KTVBoxPathManager.TYPE_RECORD_FILE);
		}else {//包含结尾的/
			fileDir = fullPath.substring(0, lastXieGangIndex + 1);
		}
		
		if(lastPointIndex == -1){
			this.fileName = "pcm";
		}else {
			fileName = fullPath.substring(lastXieGangIndex + 1, lastPointIndex);
		}
	}

    public String getPitchChar(){
        return notes[audio.note % OCTAVE];
    }

    public int getPitchNum(){
        return audio.note / OCTAVE;
    }

//    public boolean getSharp(){
//        int count = audio.note % OCTAVE;
//        if (count % 1 == 0 || count % 3 == 0 || count % 8 == 0 || count %10 ==0){
//            return true;
//        }
//        return false;
//    }

    public int getPitch(){
    	return (int)((Math.log(audio.frequency/6.875)/Math.log(2))*12 - 3 - 60);
    }

    public double getCents() {
        double tmpCents = audio.cents;
        if (tmpCents < 0)
            return -tmpCents;
        else
    	    return tmpCents;
	}

    public void initAudioRecorder(){
    	audio.input = AudioSource.DEFAULT; //0
        audio.reference = 440;
        audio.filter = false;
        audio.downsample = false;
        audio.initAudioRecorder();
    }
    
    public void start(){
        audio.start();
    }

    public void stop(){
    	hasStopAudio = true;
    	audio.stop();
    }

    protected class Audio implements Runnable
    {
        // Preferences

        protected int input;

        protected boolean filter;
        protected boolean downsample;

        protected double reference;
        protected int sample;
        private int bufferSizeInShort = -1;
        // Data
        protected Thread thread;
        protected double buffer[];
        protected short data[];

        // Output data
        protected double lower;
        protected double higher;
        protected double nearest;
        protected double frequency;
        protected double difference;
        protected double cents;
        protected double fps;

        protected int count;
        protected int note;

        // Private data
        private long timer;
        private int divisor = 1;

        private AudioRecord audioRecord;

        private static final int MAXIMA = 8;
        private static final int OVERSAMPLE = 16;
        private static final int SAMPLES = 16384;
        private static final int RANGE = SAMPLES * 3 / 8;
        private static final int STEP = SAMPLES / OVERSAMPLE;

        // Constructor

        protected Audio()
        {
            buffer = new double[SAMPLES];
            data = new short[STEP];
        }

        // Start audio
        protected void start()
        {

//            if(KgRecordControl.getInstance().isNeedToRecoding()){
//
//                KgRecordControl.getInstance().start();
//
//            }
            isRealStartRecord = true;
            //记录开始录的时间
            startTime = System.currentTimeMillis();
        }

        // Run
        @Override
        public void run()
        {
//            1） android.os.Process.setThreadPriority （int priority）
//        	　　或
//        	　　android.os.Process.setThreadPriority （int tid， int priority）
//        	　　priority：【-20， 19】，高优先级 -> 低优先级
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
            processAudio();
        }

        // Stop
        protected void stop()
        {
            isRealStartRecord = false;

            LogUtil.i("call audioRecord.release, set audioRecord=null, thread=null");
//            KgRecordControl.getInstance().stop();
            long startTime = System.currentTimeMillis();
            if(audioRecord != null) audioRecord.release();
            LogUtil.i("call audioRecord.release() coast time " + (System.currentTimeMillis() - startTime));
            audioRecord = null;
            //@nero
            thread = null;
        }
        
        public void initAudioRecorder(){
        	//初始化jni pitch分析器	
        	if (USE_JNI_GETPITCH) {
        		if (cAnalyzerId != 0) {
        			LogUtil.e("just init NewPitchAnalyzer, but cAnalyzerId is =" + cAnalyzerId 
        					+ ", so first delete it, but you must check this error");
        			PitchAnalyzer.DelPitchAnalyzer(cAnalyzerId);
				}
        		cAnalyzerId = PitchAnalyzer.NewPitchAnalyzer();
        		Log.d(TAG, "PitchAnalyzer.NewPitchAnalyzer, cAnalyzerId=>" + cAnalyzerId);
    		}
        	
        	
            // Sample rates to try
            int rates[] = {11025, 22050, 44100, 8000, 16000, 32000};
            for (int rate: rates) {
                int minBufferSize = AudioRecord.getMinBufferSize(rate, RECORD_CHANNEL,  AudioFormat.ENCODING_PCM_16BIT);
                if (minBufferSize > 0) {
                	sample = rate;
                	bufferSizeInShort = minBufferSize;
                	LogUtil.i("Current using AudioRecord samplerate=" + rate + ", minbuffersize:" + bufferSizeInShort);
                	break;
                } else if (minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    continue;
                } else if (minBufferSize == AudioRecord.ERROR) {
                	continue;
                }
            }
			if (sample == 0 && bufferSizeInShort == -1) {
				thread = null;
				LogUtil.w("bufferSizeInShort == AudioRecord.ERROR , set thread null");
				return;
			}
            
			//TODO 此处是debug代码
			sample = 44100;
        
            
            Log.d(TAG, "[initAudioRecorder]before PitchAnalyzerInit time=>" + System.currentTimeMillis());
            if (USE_JNI_GETPITCH) {
//            	Log.d(TAG, "(long)sample=>" + (long)sample);
            	//获取wav header 传递给jni
            	long totalAudioLen = 3000000 * 2;
				long totalDataLen = totalAudioLen + 36;
				long longSampleRate = (long) sample;
				int channels = 2;
				long byteRate = 16 * longSampleRate * channels / 8;
            	byte[] wavHeader = getWavHeader(totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);
            	PitchAnalyzer.PitchAnalyzerInit(cAnalyzerId, wavHeader);
			}
            Log.d(TAG, "[initAudioRecorder]after PitchAnalyzerInit time=>" + System.currentTimeMillis());
            
            // Set divisor according to sample rate

            // If you change the sample rates, make sure that this code
            // still works correctly, as both arrays get sorted as there
            // is no array.getIndexOf()
//            Arrays.sort(rates);
//            int index = Arrays.binarySearch(rates, (int)sample);
//            int divisors[] = {1, 2, 4, 1, 2, 4};
//            Arrays.sort(divisors);
//            divisor = divisors[index];
//
//            // Calculate fps
//            fps = (sample / divisor) / SAMPLES;
//            final double expect = 2.0 * Math.PI *
//                    STEP / SAMPLES;

            if(audioRecord != null){
                throw  new RuntimeException("audioRecord has already been created");
            }

            // Create the AudioRecord object
            Log.d(TAG, "[initAudioRecorder]before new AudioRecord time=>" + System.currentTimeMillis() + ", sampleRate=>" + sample);
            audioRecord = new AudioRecord(input, sample, RECORD_CHANNEL,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufferSizeInShort * 5);
            //TODO debugCode
//            KgRecordControl.getInstance().setCurrSongSaveName("demo.aac");
//            KgRecordControl.getInstance().setSample(sample);
            Log.d(TAG, "[initAudioRecorder]after new AudioRecord time=>" + System.currentTimeMillis());
            
            // Check state
            int state = audioRecord.getState();
            if (state != AudioRecord.STATE_INITIALIZED) {
                audioRecord.release();
                thread = null;
                LogUtil.w("state != AudioRecord.STATE_INITIALIZED , set thread null");
                return;
            }

//            //@nero
////          // Start recording
            if(!hasStopAudio){
                long startTime = System.currentTimeMillis();
                audioRecord.startRecording();
                startRcordTime = System.currentTimeMillis();
                Log.d(TAG, "audioRecord.startRecording time=>" + startRcordTime);

                // Start the thread
                thread = new Thread(this, "Audio");
                thread.start();

                Log.d(TAG, "[initAudioRecorder]call audioRecord.startRecording() coast " + (System.currentTimeMillis() - startTime) + " ms");
                KGameLogUtil.getInstance().appendError("[initAudioRecorder]call audioRecord.startRecording() coast " + (System.currentTimeMillis() - startTime) + " ms");
            }else{
                LogUtil.w("we already call stop audiorecord but we start, so do nothing");
            }
        }

        private long startRcordTime = 0;

        // Process Audio
        protected void processAudio()
        {

			if (DEBUG_PCM) {
				File dir = new File(getFileDir());
				if (!dir.exists()) {
					dir.mkdir();
				}
				// init pcm file name
				String AudioName = getCurrentFilePcmPath();
				try {
					File file = new File(AudioName);
					if (file.exists()) {
						file.delete();
					}
					Log.d(TAG, "will create pcm file");
					file.createNewFile();
					fos = new FileOutputStream(file);// 建立一个可存取字节的文件
					dos = new DataOutputStream(fos);
					
	            	long totalAudioLen = 3000000 * 3;
					long totalDataLen = totalAudioLen + 36;
					long longSampleRate = (long) sample;
					int channels = 2;
					long byteRate = 16 * longSampleRate * channels / 8;
					dos.write(getWavHeader(totalAudioLen, totalDataLen, longSampleRate, channels, byteRate));
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG, "create file failed");
				}
			}
//			}

            Log.d(TAG, "[processAudio]enter processAudio time=>" + System.currentTimeMillis());

            int bufferSize = bufferSizeInShort * 2;
            byte[] byteBuffer = new byte[bufferSize];
            int size = 0;
            
	    	
	    	Log.d(TAG, "before while time=>" + System.currentTimeMillis());
            while (thread != null)
            {
            	
                // Read a buffer of data
            	if (audioRecord == null) {
					LogUtil.e("audiorecord is null break");
					break;
				}
            	
            	if (data == null) {
            		LogUtil.e("data is null");
				}
            	
            	try {
//                    long startT = System.currentTimeMillis();

                    size = audioRecord.read(byteBuffer, 0, bufferSize);

//                    long useTime = System.currentTimeMillis() - startT;
//                    Log.d(TAG, "read " + size + " audio data coast " + useTime);
				} catch (Exception e) {
					e.printStackTrace();
                    if(audioRecord == null){
                        LogUtil.w("audioRecord is already null, maybe stoped, just break");
                    }
					break;
				}
                
                // Stop the thread if no data
                
                if (size <= 0)
                {
                    thread = null;
                    LogUtil.w("size == 0, set thread null");
                    break;
                }
                
		
				if (DEBUG_PCM) {
					byte[] clone = byteBuffer.clone();
					try {
						dos.write(clone);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
				}
				
				if(!isRealStartRecord && !hasStopAudio) continue;
		
				if (KgRecordControl.getInstance().isNeedToRecoding()) {					
					byte[] c = byteBuffer.clone();
					KgRecordControl.getInstance().queue.offer(c);
				}
                
                if (USE_JNI_GETPITCH) {
                    if(cAnalyzerId != 0 && !hasStopAudio){
//                        Log.d(TAG, "send buffer to PitchJni, time=>" + (System.currentTimeMillis() - startTime));
                        PitchAnalyzer.PitchAnalyzerProcess(cAnalyzerId, byteBuffer, size);
                    }
                }else{

                }
            }

			if (DEBUG_PCM) {
				try {
					dos.close();
					dos = null;
					copyWaveFile(getCurrentFilePcmPath(), getCurrentFileWavPath());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

            //释放jni pitch分析器
        	if (USE_JNI_GETPITCH) {
        		if (cAnalyzerId != 0) {
        			LogUtil.w("will call PitchAnalyzer.DelPitchAnalyzer, reset cAnalyzerId = 0");
        			int cAnalyzerIdTemp = cAnalyzerId;
                    cAnalyzerId = 0;
                    PitchAnalyzer.DelPitchAnalyzer(cAnalyzerIdTemp);
        		}
        	}
        }

        // 这里得到可播放的音频文件
        private void copyWaveFile(String inFilename, String outFilename) {
            FileInputStream in = null;
            FileOutputStream out = null;
            long totalAudioLen = 0;
            long totalDataLen = totalAudioLen + 36;
            long longSampleRate = (long) sample;
            Log.d(TAG, "longSampleRate=>" + longSampleRate);
            Log.d(TAG, "outFilename=>" + outFilename);
            int channels = 1;  
            if (RECORD_CHANNEL == AudioFormat.CHANNEL_IN_MONO) {
            	channels = 1;
			}else if (RECORD_CHANNEL == AudioFormat.CHANNEL_IN_STEREO) {
				channels = 2;
			}
            long byteRate = 16 * longSampleRate * channels / 8;  
            byte[] data = new byte[bufferSizeInShort];  
            try {  
                in = new FileInputStream(inFilename);  
                File oFile = new File(outFilename);
                if (oFile.exists()) {
					oFile.delete();
				}
                oFile.createNewFile();
                out = new FileOutputStream(oFile);  
                totalAudioLen = in.getChannel().size();  
                totalDataLen = totalAudioLen + 36;  
                WriteWaveFileHeader(out, totalAudioLen, totalDataLen,  
                        longSampleRate, channels, byteRate);  
                while (in.read(data) != -1) {  
                    out.write(data);  
                }  
                in.close();  
                out.close();  
            } catch (FileNotFoundException e) {  
                e.printStackTrace();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
            
            Log.d(TAG, "generate wav success");
        }  
      
        /** 
         * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。 
         * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav 
         * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有 
         * 自己特有的头文件。 
         */  
        private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,  
                long totalDataLen, long longSampleRate, int channels, long byteRate)  
                throws IOException {  
            byte[] header = getWavHeader(totalAudioLen, totalDataLen, longSampleRate, channels, byteRate);  
            out.write(header, 0, 44);  
        } 
    
	    private byte[] getWavHeader(long totalAudioLen, long totalDataLen, long longSampleRate, int channels, long byteRate){  
	    	Log.d("KGameAudioAnanlyzer", "param=>" +totalAudioLen+ "," + totalDataLen + "," + longSampleRate + "," + channels + "," + byteRate);
	        byte[] header = new byte[44];  
	        header[0] = 'R'; // RIFF/WAVE header  
	        header[1] = 'I';  
	        header[2] = 'F';  
	        header[3] = 'F';  
	        header[4] = (byte) (totalDataLen & 0xff);  
	        header[5] = (byte) ((totalDataLen >> 8) & 0xff);  
	        header[6] = (byte) ((totalDataLen >> 16) & 0xff);  
	        header[7] = (byte) ((totalDataLen >> 24) & 0xff);  
	        header[8] = 'W';  
	        header[9] = 'A';  
	        header[10] = 'V';  
	        header[11] = 'E';  
	        header[12] = 'f'; // 'fmt ' chunk  
	        header[13] = 'm';  
	        header[14] = 't';  
	        header[15] = ' ';  
	        header[16] = 16; // 4 bytes: size of 'fmt ' chunk  
	        header[17] = 0;  
	        header[18] = 0;  
	        header[19] = 0;  
	        header[20] = 1; // format = 1  
	        header[21] = 0;  
	        header[22] = (byte) channels;  
	        header[23] = 0;  
	        header[24] = (byte) (longSampleRate & 0xff);  
	        header[25] = (byte) ((longSampleRate >> 8) & 0xff);  
	        header[26] = (byte) ((longSampleRate >> 16) & 0xff);  
	        header[27] = (byte) ((longSampleRate >> 24) & 0xff);  
	        header[28] = (byte) (byteRate & 0xff);  
	        header[29] = (byte) ((byteRate >> 8) & 0xff);  
	        header[30] = (byte) ((byteRate >> 16) & 0xff);  
	        header[31] = (byte) ((byteRate >> 24) & 0xff);  
	        header[32] = (byte) (channels * 16 / 8); // block align  
	        header[33] = 0;  
	        header[34] = 16; // bits per sample  
	        header[35] = 0;  
	        header[36] = 'd';  
	        header[37] = 'a';  
	        header[38] = 't';  
	        header[39] = 'a';  
	        header[40] = (byte) (totalAudioLen & 0xff);  
	        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);  
	        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);  
	        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);  
	        return header;
	    }
	
    }
    
    
    /** 
     * 转换short为byte 
     *  
     * @param b 
     * @param s 
     *            需要转换的short 
     * @param index 
     */  
    public void putShort(byte b[], short s, int index) {  
        b[index + 1] = (byte) (s >> 8);  
        b[index + 0] = (byte) (s >> 0);  
    }
    
    public int getJniAnalyserId(){
    	return cAnalyzerId;
    }
}
