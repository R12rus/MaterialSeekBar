# MaterialSeekBar

[![](https://jitpack.io/v/R12rus/MaterialSeekBar.svg)](https://jitpack.io/#R12rus/MaterialSeekBar)
[![GitHub license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://opensource.org/licenses/MIT)

# Installation
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Step 2.
```
  dependencies {
	        compile 'com.github.R12rus:MaterialSeekBar:0.0.1'
	}
```

<p align="center">
    <img src="/images/example.gif?raw=true" width="480" height="640" />
</p>

# Usage
Example of usage in xml layout
```
    <r12.materialseekbar.MaterialSeekBar
        android:id="@+id/seekbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingTop="10dp"
        app:alwaysShowBubble="false"
        app:showMinMax="true" />
```
