# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class com.mobileadvsdk.presentation.MraidController {
   public *;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepattributes MethodParameters
-keepparameternames

-keepnames class com.mobileadvsdk.AdvSDK

-keep class com.mobileadvsdk.AdvSDK {
   public *;
}

-keepclassmembers enum com.mobileadvsdk.datasource.domain.model.AdvertiseType, com.mobileadvsdk.datasource.domain.model.AdvReqType, com.mobileadvsdk.datasource.domain.model.InitializationErrorType, com.mobileadvsdk.datasource.domain.model.LoadErrorType, com.mobileadvsdk.datasource.domain.model.ShowCompletionState, com.mobileadvsdk.datasource.domain.model.ShowErrorType {
    public *;
}

-keep interface com.mobileadvsdk.IAdInitializationListener, com.mobileadvsdk.IAdShowListener, com.mobileadvsdk.IAdLoadListener
-keepclassmembers interface com.mobileadvsdk.IAdInitializationListener, com.mobileadvsdk.IAdShowListener, com.mobileadvsdk.IAdLoadListener {
    public <methods>;
}
