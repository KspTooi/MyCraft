package com.ksptool.mycraft.world.properties;

import java.util.Arrays;
import java.util.Collection;

public class BooleanProperty extends BlockProperty<Boolean> {
    public static final Collection<Boolean> BOOLEAN_VALUES = Arrays.asList(false, true);

    public BooleanProperty(String name) {
        super(name, Boolean.class, BOOLEAN_VALUES);
    }

    public static BooleanProperty create(String name) {
        return new BooleanProperty(name);
    }
}

