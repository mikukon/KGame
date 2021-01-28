package com.r3studio.KGame.gdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;

import css.engine.MEngineAccessor;


public class ActorMTweenAccessor implements MEngineAccessor<Actor>
{
	public static final int POS_X = 1;
	public static final int POS_Y = 2;
	public static final int POS_XY = 3;
	public static final int CPOS_XY = 4;
	public static final int SCALE_XY = 5;
	public static final int ROTATION = 6;
	public static final int ALPHA = 7;

	@Override
	public int getValues(Actor target, int tweenType, float[] returnValues)
	{
		switch( tweenType )
		{
			case POS_X:
				returnValues[0] = target.getX();
				return 1;
			case POS_Y:
				returnValues[0] = target.getY();
				return 1;
			case POS_XY:
				returnValues[0] = target.getX();
				returnValues[1] = target.getY();
				return 2;
			case CPOS_XY:
				returnValues[0] = target.getX() + target.getWidth()/2;
				returnValues[1] = target.getY() + target.getHeight()/2;
				return 2;

			case SCALE_XY:
				returnValues[0] = target.getScaleX();
				returnValues[1] = target.getScaleY();
				return 2;

			case ROTATION:
				returnValues[0] = target.getRotation();
				return 1;
			case ALPHA:
				returnValues[0] = target.getColor().a;
				return 1;

			default:
				return -1;
		}
	}

	@Override
	public void setValues(Actor target, int tweenType, float[] newValues)
	{
		switch( tweenType )
		{
			case POS_X:
				target.setX(newValues[0]);
				break;
			case POS_Y:
				target.setY(newValues[0]);
				break;
			case POS_XY:
				target.setX(newValues[0]);
				target.setY(newValues[1]);
				break;
			case CPOS_XY:
				target.setX(newValues[0] - target.getWidth() / 2);
				target.setY(newValues[1] - target.getHeight()/2);
				break;
			case SCALE_XY:
				target.setScaleX(newValues[0]);
				target.setScaleY(newValues[1]);
				break;
			case ROTATION:
				target.setRotation(newValues[0]);
				break;

			case ALPHA:
				Color c = target.getColor();
				c.set( c.r , c.g , c.b , newValues[0] );
				target.setColor( c );
				break;
			default:
				break;
		}
	}
}
