# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/altimetrik/Developer/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# please KEEP ALL THE NAMES
-keepclasseswithmembers class com.altimetrik.adf.** { *; }

-keep interface com.altimetrik.adf.**

# keep android_res/...
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keep class **.R$*