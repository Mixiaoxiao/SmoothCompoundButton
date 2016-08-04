SmoothCompoundButton
===============

Android CompoundButtons (Switch, CheckBox, RadioButton) in Material Design, works on Android 4.0+(SDK 14).

`SmoothCompoundButton` 是全套Material风格的Switch、CheckBox和RadioButton组件，纯Java代码(非贴图)像素级复刻了Material动画与阴影效果，支持Android 4.0+。基本实现了在不同Android版本上与Material风格一致的效果，体验优于官方AppCompat，可能是目前最好的Material风格CompoundButton组件之一。

Screenshots 
-----

![SmoothCompoundButton](https://raw.github.com/Mixiaoxiao/SmoothCompoundButton/master/Screenshots/GIF-allinone.gif) 

* Android 4.x [Switch - Click to show GIF](https://raw.github.com/Mixiaoxiao/SmoothCompoundButton/master/Screenshots/GIF-switch_4x.gif),  [CheckBox - Click to show GIF](https://raw.github.com/Mixiaoxiao/SmoothCompoundButton/master/Screenshots/GIF-checkbox_4x.gif),  [RadioButton - Click to show GIF](https://raw.github.com/Mixiaoxiao/SmoothCompoundButton/master/Screenshots/GIF-radiobutton_4x.gif)

* Android 5.0+ [Switch - Click to show GIF](https://raw.github.com/Mixiaoxiao/SmoothCompoundButton/master/Screenshots/GIF-switch_5+.gif),
[CheckBox - Click to show GIF](https://raw.github.com/Mixiaoxiao/SmoothCompoundButton/master/Screenshots/GIF-checkbox_5+.gif),
[RadioButton - Click to show GIF](https://raw.github.com/Mixiaoxiao/SmoothCompoundButton/master/Screenshots/GIF-radiobutton_5+.gif)

Sample APK
-----

[SmoothCompoundButtonSample.apk](https://raw.github.com/Mixiaoxiao/SmoothCompoundButton/master/SmoothCompoundButtonSample.apk)


Features 特性
-----

* 完整地复刻了Material风格动画效果，顺滑！流畅！
* 继承于Button，本身支持显示文字，无需配合TextView使用
* 无文字时使按钮标记等比例充满View，这个在某些场景中很有用
* Switch的thumb具有“逼真”的阴影，像素级代码实现而非贴图
* toogle动画过程中的颜色变化为渐变效果
* 按下效果和Material风格一致，可超出View布局边界，5.0+为原生ripple，4.x使用StateListDrawable实现类似效果
* 支持“正确的”padding，系统的CheckBox和RadioButton对padding的处理很不合常理，见Sample
* 支持ClickMarkOnly，是否仅按下“按钮标记”部分为toogle（即忽略点击文字部分）
* 支持ReverseMarkPosition，是否反转“正常的按钮标记位置”
* 支持RTL从右到左布局，不过懒得处理Switch的的按钮标记的左右了
* 支持Disabled状态而无需任何手动配置
* 无！任！何Resources！对，你没有听错，只需导入[jar](https://raw.github.com/Mixiaoxiao/SmoothCompoundButton/master/smoothcompoundbuttonlibrary.jar)即可（原谅我这个强迫症患者）


Usage 用法
-----

* 只需导入[jar](https://raw.github.com/Mixiaoxiao/SmoothCompoundButton/master/smoothcompoundbuttonlibrary.jar)
* 使用SmoothSwitch、SmoothCheckBox、SmoothRadioButton、SmoothRadioGroup替换掉系统对应的类即可
* Java api和系统CompoundButton完全一致，额外增加的一个方法:

```java
public void setChecked(boolean checked, boolean withAnimation, boolean notifyOnCheckedChangeListener) 
```

* 其他配置见下面的Attrs


Attrs 属性
--------

除了支持android:checked，由于本Library无！任！何Resources！故从其他View中“借来”了几个属性：

|attr|format|description|notice|
|---|:---|:---|:---:|
|android:adjustViewBounds|boolean|ClickMarkOnly，是否仅可点击按钮标记部分|默认false，即整体可点击|
|android:cropToPadding|boolean|ReverseMarkPosition，是否反转“正常的按钮标记位置”|默认false，即正常位置|
|android:tint|color|MarkColor，按钮标记的颜色|如果是单个颜色则取为state_checked时的颜色，如果是selector会分别取两种对应状态的颜色|

* 如果没有指定android:gravity，则设置为Gravity.CENTER_VERTICAL
* 其实android:checked也是借来的，这个是CompoundButton的属性，而SmoothCompoundButton的基类是继承于Button



Developed By
------------

Mixiaoxiao - <xiaochyechye@gmail.com> or <mixiaoxiaogogo@163.com>



License
-----------

    Copyright 2016 Mixiaoxiao

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
