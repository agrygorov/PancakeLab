package org.pancakelab.model;

import org.pancakelab.model.pancakes.Pancake;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class Order {
    private final UUID id;
    private final int building;
    private final int room;
    private final Map<Pancake, Integer> pancakes = new LinkedHashMap<>();
    private OrderStatus orderStatus = OrderStatus.CREATED;

    public Order(int building, int room) {
        this.id = UUID.randomUUID();
        if (building < 1 || room < 1) {
            throw new IllegalArgumentException("Building and room number should be positive");
        }
        this.building = building;
        this.room = room;
    }

    public UUID getId() {
        return id;
    }

    public int getBuilding() {
        return building;
    }

    public int getRoom() {
        return room;
    }

    public void addPancake(Pancake pancake, int count) {
        pancakes.merge(pancake, count, Integer::sum);
    }

    public int removePancakes(String description, int count) {
        int removedCount;
        Optional<Pancake> pancakeToRemove =
                pancakes.keySet().stream().filter(pancake -> pancake.description().equals(description)).findFirst();
        if (pancakeToRemove.isEmpty()) {
            throw new IllegalArgumentException("No pancake with description: \"" + description + "\" found");
        }
        Pancake pancake = pancakeToRemove.get();
        Integer pancakesCount = pancakes.get(pancake);
        if (pancakesCount <= count) {
            pancakes.remove(pancake);
            removedCount = pancakesCount;
        } else {
            pancakes.put(pancake, pancakesCount - count);
            removedCount = count;
        }
        return removedCount;
    }

    public List<String> viewOrder() {
        return pancakes.entrySet()
                .stream()
                .flatMap(entry -> Collections.nCopies(entry.getValue(), entry.getKey().description()).stream())
                .collect(Collectors.toList());
    }

    public Map<Pancake, Integer> getPancakes() {
        return pancakes;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
