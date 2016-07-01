package com.imagine.go;

import com.amap.api.navi.enums.NaviType;

/**
 * Constant: 定义程序应用的相关常量
 * 
 * @author Jinhu
 * @date 2016/3/19
 */
public class Constants {

	/* 讯飞语音Appid。 */
	public static final String ID_APP_XFVoice = "56f9c9b6";

	/* 调试模式 。 */
	public static final boolean IS_DEBUG = false;

	/* 导航模式 。 */
	public static final int NAVI_MODE_EMULATOR = NaviType.EMULATOR;
	public static final int NAVI_MODE_GPS = NaviType.GPS;

	/* 搜索标签 。 */
	public static final int NUM_ITEM_LABEL = 9;

	public static final String KEY_ITEM_LABEL_IMG = "ITEM_IMAGE";
	public static final String KEY_ITEM_LABLE_NAME = "ITEM_Name";

	public static final String ITEM_LABLE_FOOD = "美食";
	public static final String ITEM_LABLE_ENTERTAINMENT = "娱乐";
	public static final String ITEM_LABLE_HOTEL = "酒店";
	public static final String ITEM_LABEL_BANK = "银行";
	public static final String ITEM_LABEL_MOVIE = "电影院";
	public static final String ITEM_LABEL_MARKET = "商场";
	public static final String ITEM_LABEL_VIEWSPOT = "景点";
	public static final String ITEM_LABEL_BUS = "公交站";
	public static final String ITEM_LABEL_GAS = "加油站";
	public static final String ITEM_LABEL_WC = "厕所";
	public static final String ITEM_LABEL_STORE = "便利店";
	/*  */

	/* 标记Activity。 */
	public static final int ACTIVITY_MAIN = 0x145;
	public static final int ACTIVITY_MAP = 0x149;
	public static final int ACTIVITY_AR = 0x153;

	/* 程序等待退出。 */
	public static final long TIME_WAIT_EXIT = 2500;
	/* 滚动视图等待下滑 。 */
	public static final long TIME_WAIT_SCROLL_DOWN = 500;

	/* 标记滚动视图下滑。 */
	public static final int EVENT_SCROLL_DOWN = 0x589;
	/* 标签网格显示 。 */
	public static final int EVENT_MGRIDVIEVW_ARISE = 0x689;
	/* Poi搜索处理 。 */
	public static final int EVENT_SEARCH_POI = 0x789;
	/* 弹出拖动条对话框 设置搜素半径 。 */
	public static final int EVENT_SEEKDIALOG_PUSH = 0x889;
	/* 地图缩放比例变小。 */
	public static final int EVENT_MAP_ZOOM_IN = 0x989;
	/* 地图缩放比例变大。 */
	public static final int EVENT_MAP_ZOOM_OUT = 0x990;
	/* 地图导航全览。 */
	public static final int EVENT_MAP_NAVI_OVERVIEW = 0x899;
	/* 重新计算路径 . */
	public static final int EVENT_NAVI_RECALCUL_ROUTE = 0x1001;
	/* 显示ARMarker的提示框 . */
	public static final int EVENT_AR_INFOWINDOW_ARISE = 0x1003;
	/* 分页组件改变. */
	public static final int EVENT_PAGER_CHANGE = 0x1011;
	/* 离线地图更新适配器. */
	public static final int EVENT_OFFLINEMAP_UPDATE_ADPATERS = 0x1021;
	/* 离线地图更新相关城市适配器. */
	public static final int EVENT_OFFLINEMAP_UPDATE_RELATED_CITY_ADPATER = 0x1031;
	/* 离线地图初始化适配器 . */
	public static final int EVENT_OFFLINEMAP_INIT_ADPATERS = 0x1041;
	/* 离线地图启动下载任务 . */
	public static final int EVENT_OFFLINEMAP_DOWNLOAD = 0x1051;
	/* 显示底部对话框 . */
	public static final int EVENT_BOTTOM_DIALOG_SHOW = 0x1061;
	/* 添加自标记地点 . */
	public static final int EVENT_USER_DEFINED_POINT_ADD = 0x1071;
	/* 删除自标记地点 . */
	public static final int EVENT_USER_DEFINED_POINT_DEL = 0x1081;
	/* 程序初始化 载入数据 定位 . */
	public static final int EVENT_INIT = 0x1091;

	/* 启动MapActivity。 */
	public static final int EVENT_ACTIVITY_START_MAP = 0x641;
	/* 启动MainActivity。 */
	public static final int EVENT_ACTIVITY_START_INDEX = 0x741;
	/* 启动ARActivity。 */
	public static final int EVENT_ACTIVITY_START_AR = 0x841;
	/* 启动MapNaviActivity。 */
	public static final int EVENT_ACTIVITY_START_MAP_NAVI = 0x643;
	/* 结束MapNaviActivity。 */
	public static final int EVENT_ACTIVITY_FINISH_MAP_NAVI = 0x645;
	/* 启动ARNaviActivity. */
	public static final int EVENT_ACTIVITY_START_AR_NAVI = 0x943;
	/* 结束ARNaviActivity . */
	public static final int EVENT_ACTIVITY_FINISH_AR_NAVI = 0x945;
	/* 启动WeatherActivity . */
	public static final int EVENT_ACTIVITY_START_WEATHER = 0x1043;
	/* 结束WeatherActivity . */
	public static final int EVENT_ACTIVITY_FINISH_WEATHER = 0x1045;
	/* 启动OfflineMapActivity . */
	public static final int EVENT_ACTIVITY_START_OFFLINEMAP = 0x1053;
	/* 结束OfflineMapActivity . */
	public static final int EVENT_ACTIVITY_FINISH_OFFLINEMAP = 0x1055;
	/* 启动GeoPointSignActivity . */
	public static final int EVENT_ACTIVITY_START_USER_DEFINED_POINT = 0x1063;
	/* 结束1GeoPointSignActivity . */
	public static final int EVENT_ACTIVITY_FINISH_USER_DEFINED_POINT = 0x1065;

	/* Assets */
	/* 已收藏标记的存储文件。 */
	public static final String FILE_COLLECTED_LABEL = "collected_lable";

	/* 标签网格初始透明度。 */
	public static final float VALUE_ALPHA_INIT_MGRIDVIEW = 0.2f;
	/* 默认搜索半径 。 */
	public static final int VALUE_DEFAULT_SEARCH_RADIUS = 3400;
	/* 默认导航路线半径 . */
	public static final int VALUE_DEFAULT_ROUTE_RADIUS = 500;

	/* 侧滑栏选项位置。 */
	/* 搜索半径选项 。 */
	public static final int VALUE_POSITION_DRAWERITEM_RADIUS = 0;
	/* 实况天气选项 . */
	public static final int VALUE_POSITION_DRAWERITEM_WEATHER = 1;
	/* 离线地图选项 . */
	public static final int VALUE_POSITION_DRAWERITEM_OFFLINEMAP = 2;
	/* 标记地点选项 . */
	public static final int VALUE_POSITION_DRAWERITEM_USERPOINT = 3;

	/* 高德地图搜索查询成功返回码。 */
	public static final int CODE_AMAP_SEARCH_SUCCESS_RETURN = 1000;

	/* 搜索无结果标记。 */
	public static final String NO_RESULT = "未找到匹配结果";
	/* 标记视图组件不会被隐藏 。 */
	public static final String NO_HIDE = "NOTHIDE";
	/* 区别地图Maker 已标记的Marker . */
	public static final String TAB_USER_DEFINED_POINT_MARKER = "SG";

	/* 默认相机旋转的角度。 */
	public static final int DEFAULT_CAMERA_DEGREE = 90;
	/* 默认相机视野 . */
	public static final float DEFAULT_CAMERA_VIEW_ANGLE = (float) Math
			.toRadians(45);

	/* 默认雷达图坐标. */
	public static final int DEFAULT_RADAR_X = 1130;
	public static final int DEFAULT_RADAR_Y = 20;

}
