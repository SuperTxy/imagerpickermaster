
Gradle
------
Add it in your root build.gradle at the end of repositories: 

```
repositories {
    maven {
        url  "https://dl.bintray.com/supertxy/maven" 
    }
}
  
```
Add the dependency dependencies 

```
dependencies {
	compile 'com.supertxy.media:imagepicker:v1.6.5'
}
```
Features
------
1、可以选择图片或视频（限制时长12s）,选中的图片可在底部显示，并按选择的顺序标号。
2、点击图片可查看大图（视频），视频可播放。
3、kotlin
4、自定义拍照和摄像功能
	- 闪光灯和切换摄像头的实现
	- 手动聚焦显示聚焦框
	- 双指缩放
	- 仿微信摄像按钮，限定时长12s
	- 拍摄完后自动循环播放
Use
------
主要有两个选择的界面一个普通不带视频的CommonPickerActivity，另一个是带视频操作的PickerActivity。
1、`CommonPickerActivity.startForResult(this,12, medias) `
```
	/**
         * @param context
         * @param maxSelect 一次最大可选择图片数
         * @param initialSelect  初始选中的列表（上一次选中的）
         */
        fun startForResult(context: Activity, maxSelect: Int, initialSelect: ArrayList<Media>) {
            val intent = Intent(context, PickerActivity::class.java)
            intent.putExtra(PickerSettings.MAX_SELECT, maxSelect)
            intent.putExtra(PickerSettings.INITIAL_SELECT, initialSelect)
            context.startActivityForResult(intent, PickerSettings.PICKER_REQUEST_CODE)
        }
```
2、`PickerActivity.startForResult(this,6, medias)`
 
	


