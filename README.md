# 星座空间(star-zone）

## 简介
关于星座的社交App
操作系统：Android
兼容Android4.4.0 - 8.1.0

Java后台代码在

[https://github.com/liushaoming/star-zone](https://github.com/liushaoming/star-zone)

## 效果图
* logo
<br/>

![](/doc/image/ic_custom_app.png)

<br/>
* 界面
<br/>

![](/doc/image/poster-1.png)

![](/doc/image/poster-2.png)

![](/doc/image/poster-3.png)

**开发者需要注意**
技术通讯使用了小米推送MiPush和腾讯免费的短信服务。
大家如果真的需要使用这些功能，需要自己去相关的官网上去注册小米开发者账号和腾讯云账号
获取相应的appid和key填入到<code>com.appjishu.starzone.constant.MiSdkConstant</code>
并修改Java后台的sms短信的开发者配置信息和misdk的开发者信息， 基本都是一些constant类，大家自己在代码中找找位置
命名基本是apikey， secret之类的。
<hr/>
