
### Version 2.0.0
**Breaking change!** The library was split into a core library (`ckchangelog-core`) and a library for the UI part
(`ckchangelog-dialog`).

The core library provides the base functionality like parsing the XML file and remembering the version code of the last
app version. This allows users of the core library to easily provide their own visualization of the Change Log.  

The `ckchangelog-dialog` library provides the simple dialog from ckChangeLog 1.x that renders the Change Log in a
`WebView` inside an `AlertDialog`. 

#### Update from ChangeLog 1.x
 
Replace the old entry in the dependency block with this:

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
```
dependencies {
     compile 'com.github.hannesa2:ChangeLog:V2.0.0'
}
```

Then replace the old ckChangeLog code in your Activity's `onCreate()` method with this:

```java
DialogChangeLog changeLog = DialogChangeLog.newInstance(this);
if (changeLog.isFirstRun()) {
    changeLog.getLogDialog().show();
}
```

Advanced functionality like getting the last version code is available via the `ChangeLog` instance that can be
retrieved by using `DialogChangeLog#getChangeLog()`.  
Example: `dialogChangeLog.getChangeLog().isFirstRunEver()`