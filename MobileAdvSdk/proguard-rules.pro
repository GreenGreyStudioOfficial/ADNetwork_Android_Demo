# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
-keepclassmembers class com.mobidriven.presentation.MraidController {
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

-keepnames class com.mobidriven.AdvSDK

-keep class com.mobidriven.AdvSDK {
   public *;
}

-keepclassmembers enum com.mobidriven.datasource.domain.model.AdvertiseType, com.mobidriven.datasource.domain.model.AdvReqType, com.mobidriven.datasource.domain.model.InitializationErrorType, com.mobidriven.datasource.domain.model.LoadErrorType, com.mobidriven.datasource.domain.model.ShowCompletionState, com.mobidriven.datasource.domain.model.ShowErrorType {
    public *;
}

-keep interface com.mobidriven.IAdInitializationListener, com.mobidriven.IAdShowListener, com.mobidriven.IAdLoadListener
-keepclassmembers interface com.mobidriven.IAdInitializationListener, com.mobidriven.IAdShowListener, com.mobidriven.IAdLoadListener {
    public <methods>;
}
