# Optimization: Keep essential attributes for debugging and reflection
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*
-renamesourcefileattribute SourceFile

# Android Components (Safety first for reflection/dynamic instantiation)
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Jetpack & Models
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class com.simple.launcher.retirement.domain.model.** { *; }

# Custom libraries (CRITICAL for auto-registration & reflection)
-keep class * implements com.simple.deeplink.DeeplinkRegister { *; }
-keep class * implements com.simple.auto.register.ModuleInitializer { *; }
-keep class * implements com.simple.launcher.retirement.utils.services.ComponentService { *; }
-keep class * implements com.simple.adapter.ViewItemAdapter { *; }
-keep class * implements com.simple.adapter.ViewItemAdapterProvider { *; }

# WorkManager
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Resources & Config (Safety for dynamic resource access)
-keep class **.R$* {
    public static <fields>;
}
-keep class **.BuildConfig { *; }

# Advertising (AdMob / Play Services Ads)
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# General suppression
-dontnote **
-dontwarn **
-ignorewarnings
