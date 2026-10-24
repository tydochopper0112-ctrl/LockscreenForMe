# Add project specific ProGuard rules here.
-keepclassmembers class * extends androidx.preference.Preference {
    public <init>(android.content.Context, android.util.AttributeSet);
}
