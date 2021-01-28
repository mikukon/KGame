package com.r3studio.KGame.gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Created by double on 15/11/19.
 */
public class GoldImage extends Image{
    private TextureRegion[] walkFrame;
    private int showIndex = 0;
    public GoldImage(Texture texture, int rows, int cols)
    {
        walkFrame = new TextureRegion[rows * cols];
        TextureRegion[][] array = TextureRegion.split(texture, texture.getWidth() / cols, texture.getHeight() / rows);
        for(int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                walkFrame[i * cols + j] = array[i][j];
            }
        }
    }

    public void setShowIndex(int index){
        showIndex = index;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        TextureRegion currentFrame = walkFrame[showIndex];

        if (currentFrame != null){
            if (getScaleX() != 1 || getScaleY() != 1) {
                batch.draw(currentFrame, getX(), getY(), currentFrame.getRegionWidth() * getScaleX(), currentFrame.getRegionHeight() * getScaleY());
            }else if (getWidth() != 0 && getHeight() != 0){
                batch.draw(currentFrame, getX(), getY(), getWidth(), getHeight());
            }else{
                batch.draw(currentFrame, getX(), getY());
            }
        }
    }
}
