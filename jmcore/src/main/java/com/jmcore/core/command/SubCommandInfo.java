package com.jmcore.core.command;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SubCommandInfo {
    // Optionally, you can add metadata fields here
}