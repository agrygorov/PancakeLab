package org.pancakelab.model.pancakes;

import java.util.Arrays;

public enum Ingredient {

    DARK_CHOCOLATE("dark chocolate"),
    WHIPPED_CREAM("whipped cream"),
    MILK_CHOCOLATE("milk chocolate"),
    HAZELNUTS("hazelnuts");


    private final String displayName;

    public static Ingredient getByName(String name) {
        return Arrays.stream(values())
                .filter(i -> i.displayName.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No ingredient with name " + name));
    }

    Ingredient(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
