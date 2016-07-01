package com.imagine.go.util;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * AnimationFactory:产生动画工产类
 * 
 * @author Jinhu
 * @date 2016/3/25
 */
public class AnimationFactory {
	/**
	 * 生成一个旋转动画
	 * 
	 * @param fromDegrees
	 *            旋转初始角
	 * @param toDegrees
	 *            旋转最终角
	 * @param duration
	 *            动画持续时间
	 * @return 返回一个旋转动画
	 */
	public static RotateAnimation rotateAnimation(float fromDegrees,
			float toDegrees, int duration) {
		// 按中心点旋转(toDegrees-fromDegrees)度
		RotateAnimation anim = new RotateAnimation(fromDegrees, toDegrees,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim.setDuration(duration);
		// 动画结束后停留在动画最后的位置
		anim.setFillAfter(true);
		return anim;
	}

	/**
	 * 生成一个平移动画
	 * 
	 * @param fromXDelta
	 * @param toXDelta
	 * @param fromYDelta
	 * @param toYDelta
	 * @param duration
	 * @param index
	 * @param count
	 * @return
	 */
	public static TranslateAnimation translateAnimation(float fromXDelta,
			float toXDelta, float fromYDelta, float toYDelta, int duration,
			int index, int count) {
		TranslateAnimation anim = new TranslateAnimation(fromXDelta, toXDelta,
				fromYDelta, toYDelta);
		anim.setFillAfter(true);
		anim.setDuration(duration);
		anim.setStartOffset((index * 100) / count);
		return anim;
	}

	public static TranslateAnimation translateAnimation(float fromXDelta,
			float toXDelta, float fromYDelta, float toYDelta, int duration) {
		TranslateAnimation anim = new TranslateAnimation(fromXDelta, toXDelta,
				fromYDelta, toYDelta);
		anim.setDuration(duration);
		return anim;
	}

	/**
	 * 生成一个缩放动画
	 * 
	 * @param fromX
	 * @param toX
	 * @param fromY
	 * @param toY
	 * @param duration
	 * @return
	 */
	public static ScaleAnimation scaleAnimation(float fromX, float toX,
			float fromY, float toY, int duration) {
		// 缩放动画
		ScaleAnimation scaleAnim = new ScaleAnimation(fromX, toX, fromY, toY,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		scaleAnim.setDuration(duration);
		scaleAnim.setFillAfter(true);
		return scaleAnim;
	}

	/**
	 * 生成一个缩放且透明动画集
	 * 
	 * @param fromX
	 * @param toX
	 * @param fromY
	 * @param toY
	 * @param duration
	 * @return
	 */
	public static Animation scaleAlphaAnimation(float fromX, float toX,
			float fromY, float toY, int duration) {
		// 缩放动画
		ScaleAnimation scaleAnim = new ScaleAnimation(fromX, toX, fromY, toY,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		// 透明度动画
		AlphaAnimation alphaAnim = new AlphaAnimation(1f, 0f);

		AnimationSet animset = new AnimationSet(true);
		animset.addAnimation(scaleAnim);
		animset.addAnimation(alphaAnim);

		animset.setDuration(duration);
		animset.setFillAfter(true);
		return animset;
	}

	/**
	 * 生成透明度动画
	 * 
	 * @param startAlpha
	 * @param endAlpha
	 * @param duration
	 * @return
	 */
	public static Animation alphaAnimation(float startAlpha, float endAlpha,
			int duration) {
		// 透明度动画
		AlphaAnimation alphaAnim = new AlphaAnimation(startAlpha, endAlpha);
		alphaAnim.setDuration(duration);
		alphaAnim.setFillAfter(true);
		return alphaAnim;
	}

	/**
	 * 加载动画文件夹
	 * 
	 * @param context
	 * @param id
	 * @return
	 * @throws Resources.NotFoundException
	 */
	public static Animation loadAnimation(Context context, int id)
			throws Resources.NotFoundException {

		XmlResourceParser parser = null;
		try {
			parser = context.getResources().getAnimation(id);
			return createAnimationFromXml(context, parser);
		} catch (XmlPullParserException ex) {
			Resources.NotFoundException rnf = new Resources.NotFoundException(
					"Can't load animation resource ID #0x"
							+ Integer.toHexString(id));
			rnf.initCause(ex);
			throw rnf;
		} catch (IOException ex) {
			Resources.NotFoundException rnf = new Resources.NotFoundException(
					"Can't load animation resource ID #0x"
							+ Integer.toHexString(id));
			rnf.initCause(ex);
			throw rnf;
		} finally {
			if (parser != null)
				parser.close();
		}
	}

	private static Animation createAnimationFromXml(Context c,
			XmlPullParser parser) throws XmlPullParserException, IOException {

		return createAnimationFromXml(c, parser, null,
				Xml.asAttributeSet(parser));
	}

	private static Animation createAnimationFromXml(Context c,
			XmlPullParser parser, AnimationSet parent, AttributeSet attrs)
			throws XmlPullParserException, IOException {
		Animation anim = null;

		int type;
		int depth = parser.getDepth();

		while (((type = parser.next()) != XmlPullParser.END_TAG || parser
				.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

			if (type != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();

			if (name.equals("set")) {
				anim = new AnimationSet(c, attrs);
				createAnimationFromXml(c, parser, (AnimationSet) anim, attrs);
			} else if (name.equals("alpha")) {
				anim = new AlphaAnimation(c, attrs);
			} else if (name.equals("scale")) {
				anim = new ScaleAnimation(c, attrs);
			} else if (name.equals("rotate")) {
				anim = new RotateAnimation(c, attrs);
			} else if (name.equals("translate")) {
				anim = new TranslateAnimation(c, attrs);
			} else {
				try {
					anim = (Animation) Class.forName(name)
							.getConstructor(Context.class, AttributeSet.class)
							.newInstance(c, attrs);
				} catch (Exception te) {
					throw new RuntimeException("Unknown animation name: "
							+ parser.getName() + " error:" + te.getMessage());
				}
			}

			if (parent != null) {
				parent.addAnimation(anim);
			}
		}

		return anim;

	}
}
