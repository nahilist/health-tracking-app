# Keep Health Connect record metadata used across platform boundaries.
-keep class androidx.health.connect.client.records.** { *; }
-dontwarn javax.annotation.**

# Room entities are referenced by generated code and must keep their constructors.
-keep,allowobfuscation,allowoptimization class com.pulsehealth.app.data.local.**
