# Add project specific ProGuard rules here.
# ExecuTorch loads native code via JNI; keep its binding classes.
-keep class org.pytorch.executorch.** { *; }
-keep class com.facebook.jni.** { *; }
-dontwarn org.pytorch.executorch.**
