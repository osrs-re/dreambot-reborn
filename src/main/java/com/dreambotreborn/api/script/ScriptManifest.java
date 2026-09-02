package com.dreambotreborn.api.script;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Metadata used by the Script Manager to discover and present scripts. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScriptManifest
{
    Category category();

    String name();

    String description() default "";

    String author();

    double version();

    String image() default "";

    String _key() default "";
}
