package org.pancakelab.service;

import org.pancakelab.model.Order;
import org.pancakelab.model.pancakes.Ingredient;
import org.pancakelab.model.pancakes.Pancake;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PancakeService {
    private final Map<UUID, Pancake> pancakesInProgress = new ConcurrentHashMap<>();

    public void startNewPancake(UUID orderId) {
        Pancake pancake = new Pancake();
        pancakesInProgress.put(orderId, pancake);
    }

    public void addIngredientToPancake(UUID orderId, String ingredient) {
        Pancake pancake = pancakesInProgress.get(orderId);
        if (pancake == null) {
            throw new IllegalStateException("No in-progress pancake found. Call startNewPancake() method first.");
        }
        pancake.addIngredient(Ingredient.getByName(ingredient));
    }

    public void addPancake(Order order, int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Count of pancakes should be positive");
        }
        Pancake pancake = pancakesInProgress.remove(order.getId());
        if (pancake == null) {
            return;
        }
        order.addPancake(pancake, count);
        OrderLog.logAddPancake(order, pancake.description(), order.getPancakes().size());
    }
}
