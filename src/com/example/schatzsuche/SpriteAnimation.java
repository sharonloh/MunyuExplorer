package com.example.schatzsuche;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class SpriteAnimation {
	private static final String TAG = SpriteAnimation.class.getSimpleName();
	
	private Bitmap bitmap;
	private Rect sourceRect;
	private int frameNr;
	private int currentFrame;
	private long frameTicker;
	private int framePeriod;
	
	private int spriteWidth;
	private int spriteHeight;
	
	private int x;
	private int y;
	
	public SpriteAnimation(Bitmap bitmap, int x, int y, int Width, int Height, int fps, int frameCount){
		this.bitmap = bitmap;
		this.x = x;
		this.y = y;
		currentFrame = 0;
		frameNr = frameCount;
		spriteWidth = bitmap.getWidth() / frameCount;
		spriteHeight = bitmap.getHeight();
		sourceRect = new Rect(0,0, spriteWidth,spriteHeight);
		framePeriod = 1000/fps;
		frameTicker = 01;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Rect getSourceRect() {
		return sourceRect;
	}

	public void setSourceRect(Rect sourceRect) {
		this.sourceRect = sourceRect;
	}

	public int getFrameNr() {
		return frameNr;
	}

	public void setFrameNr(int frameNr) {
		this.frameNr = frameNr;
	}

	public int getCurrentFrame() {
		return currentFrame;
	}

	public void setCurrentFrame(int currentFrame) {
		this.currentFrame = currentFrame;
	}

	public long getFrameTicker() {
		return frameTicker;
	}

	public void setFrameTicker(long frameTicker) {
		this.frameTicker = frameTicker;
	}

	public int getFramePeriod() {
		return framePeriod;
	}

	public void setFramePeriod(int framePeriod) {
		this.framePeriod = framePeriod;
	}

	public int getSpriteWidth() {
		return spriteWidth;
	}

	public void setSpriteWidth(int spriteWidth) {
		this.spriteWidth = spriteWidth;
	}

	public int getSpriteHeight() {
		return spriteHeight;
	}

	public void setSpriteHeight(int spriteHeight) {
		this.spriteHeight = spriteHeight;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public void update(long gameTime){
		if (gameTime > frameTicker + framePeriod){
			frameTicker = gameTime;
			currentFrame++;
			if (currentFrame >= frameNr){
				currentFrame = 0;
			}
		}
		this.sourceRect.left = currentFrame * spriteWidth;
		this.sourceRect.right = this.sourceRect.left + spriteWidth;
	}
	
	public void draw(Canvas canvas){
		Rect destRect = new Rect(getX(),getY(),getX()+spriteWidth,getY()+spriteHeight);
		canvas.drawBitmap(bitmap, sourceRect, destRect, null);
		//Paint paint = new Paint();
		//paint.setARGB(50, 0, 255, 0);
	}
}
