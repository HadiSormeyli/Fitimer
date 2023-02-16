# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.android.raya.intervaltimer.service.* {*;}


-keepattributes Signature
-keepattributes *Annotation*

-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { *; }

-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keepattributes Signature, InnerClasses, EnclosingMethod

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-dontwarn javax.annotation.**

-dontwarn kotlin.Unit

-dontwarn retrofit2.-KotlinExtensions


-dontwarn javax.annotation.**

-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-dontwarn org.codehaus.mojo.animal_sniffer.*

-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn org.codehaus.mojo.animal_sniffer.*
##---------------End: proguard configuration for okio  ----------




-keep public class com.google.android.gms.ads.**{
   public *;
}

# For old ads classes
-keep public class com.google.ads.**{
   public *;
}

# For mediation
-keepattributes *Annotation*

# Other required classes for Google Play Services
# Read more at http://developer.android.com/google/play-services/setup.html
-keep class * extends java.util.ListResourceBundle {
   protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
   public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
   @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
   public static final ** CREATOR;
}

-keep class com.chartboost.** { *; }

-keepclassmembers enum * { *; }
-keep class **.R$* { *; }
-keep interface ir.tapsell.sdk.NoProguard
-keep interface ir.tapsell.sdk.NoNameProguard
-keep class * implements ir.tapsell.sdk.NoProguard { *; }
-keep interface * extends ir.tapsell.sdk.NoProguard { *; }
-keep enum * implements ir.tapsell.sdk.NoProguard { *; }
-keepnames class * implements ir.tapsell.sdk.NoNameProguard { *; }
-keepnames class * extends android.app.Activity
-keep class ir.tapsell.plus.model.** { *; }
-keep class ir.tapsell.sdk.models.** { *; }

-keep class ir.tapsell.sdk.nativeads.TapsellNativeVideoAdLoader$Builder {*;}
-keep class ir.tapsell.sdk.nativeads.TapsellNativeBannerAdLoader$Builder {*;}

-keepclasseswithmembers class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keep interface ir.tapsell.plus.NoProguard
-keep interface * extends ir.tapsell.plus.NoProguard { *; }
-keep class * implements ir.tapsell.plus.NoProguard { *; }


-dontwarn com.applovin.**
-keep class com.applovin.** { *; }
-keep class com.google.android.gms.ads.identifier.** { *; }

##---------------End: proguard configuration for AppLovin  ----------

-keep public class com.bumptech.glide.**

# For communication with AdColony's WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# TapsellPlus needs their formal names
-keepnames public class com.google.android.gms.ads.MobileAds
-keepnames public class com.unity3d.services.monetization.IUnityMonetizationListener
-keepnames public class com.adcolony.sdk.AdColony
-keepnames public class com.google.android.gms.ads.identifier.AdvertisingIdClient
-keepnames public class com.chartboost.sdk.Chartboost
-keepnames public class com.applovin.sdk.AppLovinSdkSettings