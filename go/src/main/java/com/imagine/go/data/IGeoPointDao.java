package com.imagine.go.data;

import java.util.List;

import com.imagine.go.model.GeoPoint;

/**
 * GeoPoint数据访问接口模型
 * 
 * @author Jinhu
 * @date 2016/5/21
 */
public interface IGeoPointDao {

	/**
	 * 根据Id获得GeoPoint
	 * 
	 * @param pointId
	 * @return
	 */
	public GeoPoint fetchGeoPointById(int pointId);

	/**
	 * 获得GeoPoint列表
	 * 
	 * @return
	 */
	public List<GeoPoint> fetchAllGeoPoints();

	/**
	 * 添加GeoPoint
	 * 
	 * @param point
	 * @return
	 */
	public boolean addGeoPoint(GeoPoint point);

	/**
	 * 添加GeoPoint列表
	 * 
	 * @param points
	 * @return
	 */
	public boolean addGeoPoints(List<GeoPoint> points);

	/**
	 * 删除所有GeoPoint
	 * 
	 * @return
	 */
	public boolean deleteAllGeoPoints();

	/**
	 * 删除GeoPoint
	 * 
	 * @param pointId
	 * @return
	 */
	public boolean deleteGeoPoint(int pointId);

	/**
	 * 修改GeoPoint名
	 * 
	 * @param pointId
	 * @return
	 */
	public boolean updateGeoPointName(int pointId, String pointName);

	/**
	 * 修改GeoPoint URL
	 * 
	 * @param pointId
	 * @return
	 */
	public boolean updateGeoPointURL(int pointId, String url);
}
