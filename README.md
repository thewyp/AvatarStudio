## 概要
只需要几句代码就可以实现通用app个人中心头像的修改，支持打开原生相机、从原生相册选取及调用原生裁剪功能，适配android 6.0运行时权限及android 7.0以上，不需要您添加繁琐的FileProvider及xml文件。
<br/>
<br/>
[download demo apk](https://raw.githubusercontent.com/thewyp/AvatarStudio/master/app-debug.apk)
<br/>
<br/>
&nbsp;![image](https://github.com/thewyp/AvatarStudio/blob/master/preview/pre1.jpg) &nbsp;&nbsp; ![image](https://github.com/thewyp/AvatarStudio/blob/master/preview/pre2.jpg)
## 依赖
<pre><code>dependencies {
                ...
                compile 'me.thewyp:avatar:1.0.3'
             }</code></pre>


## 使用
 <pre><code>new AvatarStudio.Builder(activityContext)
                            .needCrop(true)//是否裁剪，默认裁剪
                            .setTextColor(Color.BLUE)
                            .dimEnabled(true)//背景是否dim 默认true
                            .setAspect(1, 1)//裁剪比例 默认1：1
                            .setOutput(200, 200)//裁剪大小 默认200*200
                            .setText("打开相机", "从相册中选取", "取消")
                            .show(new AvatarStudio.CallBack() {
                                @Override
                                public void callback(String uri) {
                                     //uri为图片路径
                                     Picasso.with(activityContext).load(new File(uri)).into(mImageView);
                                }
                            });
</code></pre>
