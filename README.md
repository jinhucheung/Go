导航犬(Go)
==========================
导航犬(Go)是一款基于Android的增强现实导航软件，其地图服务由高德地图提供。使用者将手机摄像头对着一个方向，屏幕自动显示出此视野方向内的所有用户感兴趣事物的地理资讯，以全新展示形式实现附近地点探索，包括餐饮、酒店、购物、景点等，并提供从用户所在地到目的地的导航路线图、语音导航以及实景导航指引。
      

演示视频
-------------------------
[![IMAGE ALT TEXT](http://img.youtube.com/vi/XZV129LJO4M/0.jpg)](https://www.youtube.com/watch?v=XZV129LJO4M)

      
功能说明
-------------------------
- 搜索兴趣点信息
- 实景以及地图导航
- 预报天气
- 离线地图下载
- 用户定义兴趣点信息
- 语音播报以及语音识别

项目框架
-------------------------
导航犬增强现实模块是基于[ProAndroidAugmentedReality](https://github.com/RaghavSood/ProAndroidAugmentedReality)框架。此外，地图服务组件是使用[高德地图SDK](http://lbs.amap.com),而语音组件使用[讯飞语音](http://www.xfyun.cn)。在UI设计上引入了[material_menu_library](https://github.com/HiKumho/material_menu_library)和[sweet-alert-dialog](https://github.com/HiKumho/XAlertDialogLibrary)。

     
项目导入
-------------------------
1. 注册API KEY      
项目使用高德地图以及讯飞语音，导入项目至本地前，需要注册相应的SDK的API Key。注册完成后，高德地图API Key在[AndroidManifest.xml文件](https://github.com/HiKumho/Go/blob/master/AndroidManifest.xml#L63)写入,讯飞语音API Key在[Constants类](https://github.com/HiKumho/Go/blob/master/src/com/imagine/go/Constants.java#L14)中写入。
2. 引入UI库     
高德地图以及讯飞语音所需要的库已经在libs中，我们还需要引入上述的两个UI库。

     
程序安装
-------------------------
程序要求Android4.0以上版本，点击下载[APK文件](https://raw.githubusercontent.com/HiKumho/Go/master/bin/Go.apk)

      
License
-------------------------
Go is released under GPLv3.0



