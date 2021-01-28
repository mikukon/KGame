package com.r3studio.KGame.gl;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class BaseGLRender implements Renderer
{
	private final String mVertexShaderCode =  
			"uniform mat4 u_MVPMatrix; \n" +  
            "attribute vec4 a_position; \n" +  
            "attribute vec2 a_texCoord; \n" + 
            "varying vec2 v_texCoord; \n" +  
            "void main() \n" +  
            "{ \n"+   
            "	v_texCoord = a_texCoord; \n" +  
            "	gl_Position = u_MVPMatrix * a_position; \n" +
            "} \n";  
  
    private final String mFragmentShaderCode =   
            "precision mediump float; \n" +  
            "uniform sampler2D u_Texture; \n" +
            "uniform vec4 u_Color; \n" +
            "varying vec2 v_texCoord; \n" +   
            "void main() \n" + 
            "{ \n" +  
            "	gl_FragColor = texture2D(u_Texture, v_texCoord) * u_Color; \n" +  
            "} \n";

    public float[] mModelMatrix = new float[16]; //模型矩阵
    public float[] mViewMatrix = new float[16]; //视图矩阵  
    public float[] mProjectionMatrix = new float[16]; //投影矩阵
    public float[] mMVPMatrix = new float[16]; 
    public int mShaderProgram;
    public int mMVPMatrixHandle;  
    public int mTextureHandle;
    public int mPositionHandle;
    public int mColorHandle;
    public int mTextureCoordHandle;   
    
	protected int mScreenWidth = 1280;
	protected int mScreenHeight = 800;
	
	public BaseGLRender(int width, int height)
	{
		mScreenWidth = width;
		mScreenHeight = height;
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig egl)
	{
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
//		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		GLES20.glEnable(GLES20.GL_TEXTURE_2D);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		
		createShaderProgram();
	}
	
	@Override
	public void onDrawFrame(GL10 gl)
	{
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{
		GLES20.glViewport(0, 0, width, height);

//        Matrix.orthoM(mProjectionMatrix, 0, 0.0f, width, 0.0f, height, 1.0f, 10.0f);
        Matrix.frustumM(mProjectionMatrix, 0, 0.0f, width, 0.0f, height, 1.0f, 100.0f);
        Matrix.setLookAtM(mViewMatrix, 0, 0.0f, 0.0f, 1.000001f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
	}
	
	private void createShaderProgram()
	{
		//create a vertex shader
		int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER); 
        GLES20.glShaderSource(vertexShader, mVertexShaderCode);  
        GLES20.glCompileShader(vertexShader);
        int[] compileStatus = new int[1]; 
        GLES20.glGetShaderiv(vertexShader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if(compileStatus[0] == 0)   
        {   
            Log.e("young", "Error compiling shader: " + GLES20.glGetShaderInfoLog(vertexShader));   
            GLES20.glDeleteShader(vertexShader);
            throw new RuntimeException("Error creating vertex shader.");
        }   
        
        //create a fragment  shader
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, mFragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);
        GLES20.glGetShaderiv(fragmentShader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if(compileStatus[0] == 0)   
        {   
            Log.e("young", "Error compiling shader: " + GLES20.glGetShaderInfoLog(fragmentShader));   
            GLES20.glDeleteShader(fragmentShader);
            throw new RuntimeException("Error creating fragment shader.");
        }
        
        mShaderProgram = GLES20.glCreateProgram(); 
        GLES20.glAttachShader(mShaderProgram, vertexShader);
        GLES20.glAttachShader(mShaderProgram, fragmentShader);
        GLES20.glLinkProgram(mShaderProgram);
        int[] linkStatus = new int[1];   
        GLES20.glGetProgramiv(mShaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);    
        if(linkStatus[0] == 0)
        {   
            Log.e("young", "Error compiling program: " + GLES20.glGetProgramInfoLog(mShaderProgram));   
            GLES20.glDeleteProgram(mShaderProgram);
            throw new RuntimeException("Error creating shader program."); 
        }
        
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_MVPMatrix");   
        mTextureHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_Texture");
        mColorHandle = GLES20.glGetUniformLocation(mShaderProgram, "u_Color"); 
        mPositionHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_position");
        mTextureCoordHandle = GLES20.glGetAttribLocation(mShaderProgram, "a_texCoord");  
	}
	
	public void setWidth(int width)
	{
		this.mScreenWidth = width;
	}
	
	public int getWidth()
	{
		return this.mScreenWidth;
	}
	
	public void setHeight(int height)
	{
		this.mScreenHeight = height;
	}
	
	public int getHeight()
	{
		return this.mScreenHeight;
	}
	
}
