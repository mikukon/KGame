package com.r3studio.KGame.gl;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

@SuppressLint("NewApi")
public class GLAnimaImage extends GLActor
{
	private updateCallback listener;

	public void setPaintListener(updateCallback listener) {
		this.listener = listener;
	}

	protected FloatBuffer mTextureBuffer;
    protected float mTextureCoords[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };

    private ByteBuffer mIndexBuffer;
    private byte mIndices[] = {
            0, 1, 2,
            2, 3, 1
    };


	protected int[] mGLTextureCoordBuffer  = new int[1];
	private int[] mGLIndexBuffer  = new int[1];

	private FloatBuffer mVertexBuffer;
    private float mVertices[] = {
            0.0f, 0.0f, 0.0f, //V0
            1.0f, 0.0f, 0.0f, //V1
            0.0f, 1.0f, 0.0f, //V2
            1.0f, 1.0f, 0.0f, //V3
    };

    private int[] mTexture = new int[1];
	private int[] mGLVertexBuffer  = new int[1];

	private float x = 0f;
	private float y = 0f;
	private float z = 0f;
	private float width = 0f;
	private float height = 0f;
	private float scaleX = 1.0f;
	private float scaleY = 1.0f;
	private float rotateAngle = 0.0f;

	protected int rows;
	protected int cols;
	private GifAnimation animation;
	private BaseGLRender render;
	private float alpha = 1.0f;

	public GLAnimaImage(BaseGLRender render, Bitmap bitmap)
	{
		this(render, bitmap, 1, 1, null);
	}

	public GLAnimaImage(BaseGLRender render, Bitmap bitmap, int rows, int cols, GifAnimation a)
	{
		this.render = render;
		this.rows = rows;
		this.cols = cols;
		this.width = bitmap.getWidth() / cols;
		this.height = bitmap.getHeight() / rows;
		this.animation = a;
		genTexture(bitmap);

		initVertex();

		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(mVertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		mVertexBuffer = byteBuffer.asFloatBuffer();
		mVertexBuffer.put(mVertices);
		mVertexBuffer.position(0);

		GLES20.glGenBuffers(1, mGLVertexBuffer, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGLVertexBuffer[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVertices.length * 4, mVertexBuffer, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

		mIndexBuffer = ByteBuffer.allocateDirect(mIndices.length);
		mIndexBuffer.put(mIndices);
		mIndexBuffer.position(0);

		GLES20.glGenBuffers(1, mGLIndexBuffer, 0);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mGLIndexBuffer[0]);
		GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, mIndices.length, mIndexBuffer, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

		ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(mTextureCoords.length * 4);
		byteBuffer2.order(ByteOrder.nativeOrder());
		mTextureBuffer = byteBuffer2.asFloatBuffer();

		GLES20.glGenBuffers(1, mGLTextureCoordBuffer, 0);
		initTextureCoords(0);
	}

	public void dispose()
	{
		GLES20.glDeleteTextures(1, mTexture, 0);
		GLES20.glDeleteBuffers(1, mGLVertexBuffer, 0);
		GLES20.glDeleteBuffers(1, mGLTextureCoordBuffer, 0);
		GLES20.glDeleteBuffers(1, mGLIndexBuffer, 0);
	}

	int preIndex = -1;
	private void initTextureCoords(int index){
		if (index == preIndex){
			return;
		}

		if (listener != null){
			listener.update(index);
		}

		preIndex = index;
		float [] mTextureCoordsTmp  = new float[]{
				1.0f * ((index % cols) / (cols * 1.0f)), 1.0f * (index / cols / (rows * 1.0f) + (1.0f / rows)),
				1.0f * ((index % cols) / (cols * 1.0f)) + (1.0f / cols), 1.0f * (index / cols / (rows * 1.0f)) + (1.0f / rows),
				1.0f * ((index % cols) / (cols * 1.0f)), 1.0f * (index / cols / (rows * 1.0f)),
				1.0f * ((index % cols) / (cols * 1.0f)) + (1.0f / cols), 1.0f * (index / cols / (rows * 1.0f)),
		};

		mTextureBuffer.put(mTextureCoordsTmp);
		mTextureBuffer.position(0);

		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGLTextureCoordBuffer[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mTextureCoordsTmp.length * 4, mTextureBuffer, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	private float rotateCenterX = 0.0f;
	private float rotateCenterY = 0.0f;
	
	private void initVertex()
	{
		mVertices[0] = 0 + (-rotateCenterX);
		mVertices[1] = 0 + (-rotateCenterY);
		
		mVertices[3] = this.width + (-rotateCenterX);
		mVertices[4] = 0 + (-rotateCenterY);
		
		mVertices[6] = 0 + (-rotateCenterX);
		mVertices[7] = this.height + (-rotateCenterY);
		
		mVertices[9] = this.width + (-rotateCenterX);
		mVertices[10] = this.height + (-rotateCenterY);
	}
	
	private void resetVertex() {
		initVertex();
		mVertexBuffer.put(mVertices);
		mVertexBuffer.position(0);

		GLES20.glGenBuffers(1, mGLVertexBuffer, 0);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGLVertexBuffer[0]);
		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, mVertices.length * 4, mVertexBuffer, GLES20.GL_STATIC_DRAW);
		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
	}
	
	private void genTexture(Bitmap bitmap)
	{
        GLES20.glGenTextures(1, mTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture[0]);  
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);          
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);  
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);  
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
	}
	
	public void draw() {
		if (animation != null){
			initTextureCoords(animation.getKeyFrameIndex());
		}

		Matrix.setIdentityM(render.mModelMatrix, 0);
		Matrix.translateM(render.mModelMatrix, 0, x, y, z);
		Matrix.scaleM(render.mModelMatrix, 0, scaleX, scaleY, 0);
		Matrix.rotateM(render.mModelMatrix, 0, rotateAngle, 0, 0, 1);
		Matrix.multiplyMM(render.mMVPMatrix, 0, render.mViewMatrix, 0, render.mModelMatrix, 0);
        Matrix.multiplyMM(render.mMVPMatrix, 0, render.mProjectionMatrix, 0, render.mMVPMatrix, 0);

		GLES20.glUseProgram(render.mShaderProgram);
		
        GLES20.glUniformMatrix4fv(render.mMVPMatrixHandle, 1, false, render.mMVPMatrix, 0); 
        GLES20.glUniform4f(render.mColorHandle, 1.0f, 1.0f, 1.0f, alpha);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGLVertexBuffer[0]);
        GLES20.glVertexAttribPointer(render.mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glEnableVertexAttribArray(render.mPositionHandle);
        
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGLTextureCoordBuffer[0]);
        GLES20.glVertexAttribPointer(render.mTextureCoordHandle, 2, GLES20.GL_FLOAT, false, 0, 0);
        GLES20.glEnableVertexAttribArray(render.mTextureCoordHandle);
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture[0]);
        GLES20.glUniform1i(render.mTextureHandle, 0);
  

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mGLIndexBuffer[0]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, 6, GLES20.GL_UNSIGNED_BYTE, 0);
  
        GLES20.glDisableVertexAttribArray(render.mPositionHandle);
        GLES20.glDisableVertexAttribArray(render.mTextureCoordHandle);
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        
        GLES20.glUseProgram(0);


	}

	public void setPosition(float x, float y)
	{
		this.x = x;
		this.y = y;
	}
	
	public void setX(float x)
	{
		this.x = x;
	}
	
	public void setY(float y)
	{
		this.y = y;
	}
	
	public void setZ(float z)
	{
		this.z = z;
	}
	
	public void setSize(float width, float height)
	{
		this.width = width;
		this.height = height;
	}
	
	public void setWidth(float width)
	{
		this.width = width;
	}
	
	public void setHeight(float height)
	{
		this.height = height;
	}

	public float getX()
	{
		return this.x;
	}

	public float getY()
	{
		return this.y;
	}

	public float getZ()
	{
		return this.z;
	}
	
	public float getWidth()
	{
		return this.width;
	}
	
	public float getHeight()
	{
		return this.height;
	}

	public float getScaleX(){
		return this.scaleX;
	}

	public void setScaleX(float scale){
		this.width *= scale;
		this.scaleX = scale;
	}

	public float getScaleY(){
		return this.scaleY;
	}

	public void setScaleY(float scale){
		this.scaleY = scale;
		this.height *= scale;
	}

	public void setScale(float x, float y){
		this.scaleX = x;
		this.scaleY = y;
		this.width *= x;
		this.height *= y;
	}

	public float getRotateAngle() {
		return rotateAngle;
	}

	public void setRotateAngle(float rotateAngle) {
		this.rotateAngle = rotateAngle;
	}


	public void paint() {
		draw();
	}

	public interface updateCallback{
		public void update(int key);
	}

	public float getRotateCenterX() {
		return rotateCenterX;
	}

	public void setRotateCenterX(float rotateCenterX) {
		this.rotateCenterX = rotateCenterX;
		resetVertex();
	}

	public float getRotateCenterY() {
		return rotateCenterY;
	}

	public void setRotateCenterY(float rotateCenterY) {
		this.rotateCenterY = rotateCenterY;
		resetVertex();
	}
	
	public void setRotateCenter(float rotateCenterX, float rotateCenterY) {
		this.rotateCenterX = rotateCenterX;
		this.rotateCenterY = rotateCenterY;
		resetVertex();
	}

	@Override
	public float getAlpha() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAlpha(float alpha) {
		// TODO Auto-generated method stub
		
	}
}
