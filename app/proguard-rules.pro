# Keep all classes that might be dynamically created
-keep class com.example.clickcounter.** { *; }
-keep class androidx.appcompat.** { *; }

# Don't optimize the bytecode (causes issues on some devices)
-dontoptimize
-dontobfuscate

# Keep generic signatures
-keepattributes Signature
-keepattributes *Annotation*

# Keep line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
