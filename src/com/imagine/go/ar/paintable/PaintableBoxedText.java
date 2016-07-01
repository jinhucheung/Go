package com.imagine.go.ar.paintable;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;

/**
 * 绘制Marker文本框
 * 
 * @author Jinhu
 * @date 2016/4/16
 */
public class PaintableBoxedText extends PaintableObject {

	/* 文本框布局属性. */
	private float width = 0, height = 0;
	private float areaWidth = 0, areaHeight = 0;
	private List<String> lineList = null;
	private String[] lines = null;
	private float[] lineWidths = null;
	private float lineHeight = 0;
	private float maxLineWidth = 0;
	private float pad = 0;

	/* 文本框基本属性. */
	private String txt = null;
	private float fontSize = 12;
	private int borderColor = COLOR_BORDER;
	private int backgroundColor = COLOR_BACKGROUND;
	private int textColor = COLOR_TEXT;

	private static final int COLOR_BORDER = Color.rgb(255, 255, 255);
	private static final int COLOR_BACKGROUND = Color.argb(160, 255, 255, 255);
	private static final int COLOR_TEXT = Color.argb(180, 0, 0, 0);

	public PaintableBoxedText(String txt, float fontSize, float maxWidth) {
		this(txt, fontSize, maxWidth, COLOR_BORDER, COLOR_BACKGROUND,
				COLOR_TEXT);
	}

	public PaintableBoxedText(String txt, float fontSize, float maxWidth,
			int borderColor, int bgColor, int textColor) {
		set(txt, fontSize, maxWidth, borderColor, bgColor, textColor);
	}

	public void set(String txt, float fontSize, float maxWidth,
			int borderColor, int bgColor, int textColor) {
		if (null == txt)
			throw new NullPointerException();

		this.borderColor = borderColor;
		this.backgroundColor = bgColor;
		this.textColor = textColor;
		this.pad = getTextAsc();

		set(txt, fontSize, maxWidth);
	}

	public void set(String txt, float fontSize, float maxWidth) {
		if (null == txt)
			throw new NullPointerException();

		try {
			prepTxt(txt, fontSize, maxWidth);
		} catch (Exception e) {
			e.printStackTrace();
			prepTxt("TEXT PARSE ERROR", 12, 200);
		}

	}

	private void prepTxt(String txt, float fontSize, float maxWidth) {
		if (null == txt)
			throw new NullPointerException();

		setFontSize(fontSize);

		this.txt = txt;
		this.fontSize = fontSize;
		this.areaWidth = maxWidth - pad;
		lineHeight = getTextAsc() + getTextDesc();

		if (null == lineList)
			lineList = new ArrayList<String>();
		else
			lineList.clear();

		BreakIterator boundary = BreakIterator.getWordInstance();
		boundary.setText(txt);

		int start = boundary.first();
		int end = boundary.next();
		int prevEnd = start;
		while (BreakIterator.DONE != end) {
			String line = txt.substring(start, end);
			String prevLine = txt.substring(start, prevEnd);
			float lineWidth = getTextWidth(line);

			if (lineWidth > areaWidth) {
				if (prevLine.length() > 0) {
					lineList.add(prevLine);
				}
				start = prevEnd;
			}

			prevEnd = end;
			end = boundary.next();
		}

		String line = txt.substring(start, prevEnd);
		lineList.add(line);

		if (null == lines || line.length() != lineList.size()) {
			lines = new String[lineList.size()];
		}

		if (null == lineWidths || lineWidths.length != lineList.size()) {
			lineWidths = new float[lineList.size()];
		}
		lineList.toArray(lines);

		maxLineWidth = 0;
		for (int i = 0; i < lines.length; i++) {
			lineWidths[i] = getTextWidth(lines[i]);
			if (maxLineWidth < lineWidths[i])
				maxLineWidth = lineWidths[i];
		}
		areaWidth = maxLineWidth;
		areaHeight = lineHeight * lines.length;

		width = areaWidth + pad * 2;
		height = areaHeight + pad * 2;

	}

	@Override
	public void setAlpha(int a) {
		this.borderColor = Color.rgb(255, 255, 255);
		this.backgroundColor = Color.argb(adj(a), 255, 255, 255);
		this.textColor = Color.argb(adj(a), 0, 0, 0);
	}

	public void adjAlpha(int off) {
		this.borderColor = Color.rgb(255, 255, 255);
		this.backgroundColor = Color.argb(adj(160 + off), 255, 255, 255);
		this.textColor = Color.argb(adj(180 + off), 0, 0, 0);
	}

	private int adj(int alpha) {
		if (alpha > 255) {
			alpha = 255;
		} else if (alpha < 0) {
			alpha = 0;
		}
		return alpha;
	}

	@Override
	public float getWidth() {
		return width;
	}

	@Override
	public float getHeight() {
		return height;
	}

	@Override
	public void paint(Canvas canvas) {
		if (null == canvas)
			throw new NullPointerException();

		setFontSize(fontSize);

		setFill(true);
		setColor(backgroundColor);
		paintRoundedRect(canvas, 0, 0, width, height);

		setFill(false);
		setColor(borderColor);
		paintRoundedRect(canvas, 0, 0, width, height);

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			setFill(true);
			setStrokeWidth(0);
			setColor(textColor);
			paintText(canvas, pad, pad + lineHeight * i + getTextAsc(), line);
		}

	}

}
