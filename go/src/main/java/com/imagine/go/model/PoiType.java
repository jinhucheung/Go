package com.imagine.go.model;

/**
 * PoiType:Poi分类
 * 
 * @author Jinhu
 * @date 2016/3/22
 */
public enum PoiType {

	Catering("餐饮服务"), Entertainment("娱乐场所"), ViewSpot("风景名胜"), //
	Shopping("购物服务"), LifeService("生活服务"), SportLeisure("体育休闲服务"), //
	CarService("汽车服务"), MedicalService("医疗保健服务"), Accommodation("宾馆酒店"), //
	Transportation("交通设施服务"), Finance("银行|自动提款机"), Commonality("公共设施"), //
	BusinessAccommodation("商业住宅"), GovernmentalAgencies("政府机构"), EducationScience(
			"科教文化服务"), //
	SuperMarket("超级市场"), Cinema("电影院"), GasStation("加油站"), BusStation("公交车站"), //
	WC("公共厕所"), Store("便利店"), ShoppingMarket("商场|超级市场"), //
	Default("汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|" + //
			"医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科技文化服务|即通设施服务|" + //
			"金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施|事件活动");

	private final String value;

	private PoiType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
