## AvatarStudio
只需要几句代码就可以实现通用app个人中心头像的修改，支持打开原生相机、从原生相册选取及调用原生裁剪功能，适配android 6.0运行时权限及android 7.0以上，无需要您添加繁琐的FileProvider及xml文件。<br/>
![image](https://github.com/thewyp/AvatarStudio/blob/master/preview/pre1.jpg)![image](https://github.com/thewyp/AvatarStudio/blob/master/preview/pre2.jpg)
## 依赖
<pre><code>
             dependencies {
                ...
                compile 'me.thewyp:avatar:1.0.3'
             }
</code></pre>

## 使用
 <pre><code>
             new AvatarStudio.Builder(activityContext)
                            .needCrop(true)
                            .setTextColor(Color.BLUE)
                            .dimEnabled(true)
                            .setAspect(1, 1)
                            .setOutput(200, 200)
                            .setText("打开相机", "从相册中选取", "取消")
                            .show(new AvatarStudio.CallBack() {
                                @Override
                                public void callback(String uri) {
                                     Picasso.with(activityContext).load(new File(uri)).into(mImageView);
                                }
                            });
</code></pre>
