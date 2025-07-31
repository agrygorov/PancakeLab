package org.pancakelab.model.pancakes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Pancake {

    private final UUID id;
    private final List<Ingredient> ingredients = new ArrayList<>();

    public Pancake() {
        this.id = UUID.randomUUID();
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public String description() {
        return "Delicious pancake with " +
               ingredients.stream().map(Ingredient::getDisplayName).collect(Collectors.joining(", ")) + "!";
    }

    public List<Ingredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pancake pancake = (Pancake) o;
        return Objects.equals(id, pancake.id) && Objects.equals(ingredients, pancake.ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ingredients);
    }
}
