# Puzzle packs are parsed with kotlinx.serialization. The library ships its own R8 rules;
# these keep the generated serializers for our models just in case.
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class io.github.soulhacler.encrypted.core.model.**$$serializer { *; }
-keepclassmembers class io.github.soulhacler.encrypted.core.model.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
