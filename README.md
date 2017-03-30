## AvatarStudio
只需要几句代码就可以实现通用app个人中心头像的修改，支持打开原生相机、从原生相册选取及调用原生裁剪功能，适配android 6.0运行时权限及android 7.0以上，无需要您添加繁琐的FileProvider及xml文件。

## 依赖
 <pre><code>dependencies {
	...
	compile 'me.thewyp:avatar:1.0.3'
>}</code></pre>

## 使用
 <pre><code>new AvatarStudio.Builder(activityContext)<br>
                .needCrop(true)<br>
                .setTextColor(Color.BLUE)<br>
                .dimEnabled(true)<br>
               .setAspect(1, 1)<br>
                .setOutput(200, 200)<br>
                .setText("打开相机", "从相册中选取", "取消")<br>
                .show(new AvatarStudio.CallBack() {<br>
                    @Override<br>
                    public void callback(String uri) {<br>
                         Picasso.with(activityContext).load(new File(uri)).into(mImageView);<br>
                    }<br>
                });<br></code></pre>
