package org.pancakelab.service;

import org.pancakelab.model.Order;
import org.pancakelab.model.OrderStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OrderManager {

    private final Map<UUID, Order> orders = new ConcurrentHashMap<>();

    public Order createOrder(int building, int room) {
        Order order = new Order(building, room);
        orders.put(order.getId(), order);
        return order;
    }

    public void removePancakes(String description, UUID orderId, int count) {
        Order order = orders.get(orderId);
        int removedCount = order.removePancakes(description, count);
        OrderLog.logRemovePancakes(order, description, removedCount, order.getPancakes().size());
    }

    public void cancelOrder(UUID orderId) {
        Order order = orders.remove(orderId);
        OrderLog.logCancelOrder(order, order.getPancakes().size());
    }

    public void completeOrder(UUID orderId) {
        orders.get(orderId).setOrderStatus(OrderStatus.COMPLETED);
    }

    public Set<UUID> listCompletedOrders() {
        return orders.entrySet()
                .stream()
                .filter(entry -> OrderStatus.COMPLETED.equals(entry.getValue().getOrderStatus()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public List<String> viewOrder(UUID orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            return Collections.emptyList();
        }
        return order.viewOrder();
    }

    public void prepareOrder(UUID orderId) {
        orders.get(orderId).setOrderStatus(OrderStatus.PREPARED);
    }

    public Set<UUID> listPreparedOrders() {
        return orders.entrySet()
                .stream()
                .filter(entry -> OrderStatus.PREPARED.equals(entry.getValue().getOrderStatus()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Object[] deliverOrder(UUID orderId) {
        Order order = orders.get(orderId);
        if (OrderStatus.PREPARED != order.getOrderStatus()) {
            return null;
        }
        List<String> pancakesToDeliver = order.viewOrder();
        OrderLog.logDeliverOrder(order, pancakesToDeliver.size());

        orders.remove(orderId);

        return new Object[]{order, pancakesToDeliver};
    }
}
