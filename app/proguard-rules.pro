# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

-dontwarn javax.lang.model.**
-dontwarn javax.annotation.**
-dontwarn autovalue.shaded.**
-dontwarn com.google.auto.value.**

# Keep generic models if needed, though room handles its own rules
