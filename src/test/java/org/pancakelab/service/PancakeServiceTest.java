package org.pancakelab.service;

import org.junit.jupiter.api.Test;
import org.pancakelab.model.Order;
import org.pancakelab.model.pancakes.Ingredient;
import org.pancakelab.model.pancakes.Pancake;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PancakeServiceTest {

    @Test
    void removeExistingPancakeFully() {
        Order order = new Order(1, 1);
        Pancake pancake = createPancake(Ingredient.DARK_CHOCOLATE);
        order.getPancakes().put(pancake, 2);

        order.removePancakes(pancake.description(), 2);

        assertTrue(order.getPancakes().isEmpty());
    }

    @Test
    void removePartialQuantityOfPancakes() {
        Order order = new Order(1, 1);
        Pancake pancake = createPancake(Ingredient.HAZELNUTS);
        order.getPancakes().put(pancake, 5);

        order.removePancakes(pancake.description(), 3);

        assertEquals(2, order.getPancakes().get(pancake));
    }

    @Test
    void removeMoreThanExistsRemovesAll() {
        Order order = new Order(1, 1);
        Pancake pancake = createPancake(Ingredient.MILK_CHOCOLATE);
        order.getPancakes().put(pancake, 2);

        order.removePancakes(pancake.description(), 5);

        assertTrue(order.getPancakes().isEmpty());
    }

    @Test
    void removePancakeThatDoesNotExistThrowsException() {
        Order order = new Order(1, 1);
        Pancake pancake = createPancake(Ingredient.DARK_CHOCOLATE);
        order.getPancakes().put(pancake, 2);

        String message = assertThrows(IllegalArgumentException.class,
                () -> order.removePancakes("non existent pancake", 2)).getMessage();

        assertEquals("No pancake with description: \"non existent pancake\" found", message);
    }

    private Pancake createPancake(Ingredient... ingredientNames) {
        Pancake pancake = new Pancake();
        Arrays.stream(ingredientNames).forEach(pancake::addIngredient);
        return pancake;
    }
}
