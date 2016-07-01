package com.imagine.go.model;

/**
 * LabelModel:搜索标签适配模型 LabelApdater:标签适配器
 * 
 * @author Jinhu
 * @date 2016/3/19
 */
public class LabelModel {

	/* 标签图 . */
	private int imgId;

	/* 标签名 . */
	private String name;

	public LabelModel(int imgId, String name) {
		this.imgId = imgId;
		this.name = name;
	}

	public int getImgId() {
		return imgId;
	}

	public void setImgId(int imgId) {
		this.imgId = imgId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "ImgId : " + imgId + "    /  name :" + name;
	}

}
