package org.pancakelab.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.pancakelab.model.Order;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderManagerTest {
    private PancakeService pancakeService = new PancakeService();
    private OrderManager orderManager = new OrderManager();
    private Order order = null;

    private final static String DARK_CHOCOLATE_PANCAKE_DESCRIPTION = "Delicious pancake with dark chocolate!";
    private final static String MILK_CHOCOLATE_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate!";
    private final static String MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION =
            "Delicious pancake with milk chocolate, hazelnuts!";

    @Test
    @org.junit.jupiter.api.Order(10)
    public void GivenOrderDoesNotExist_WhenCreatingOrder_ThenOrderCreatedWithCorrectData_Test() {
        // setup

        // exercise
        order = orderManager.createOrder(10, 20);

        assertEquals(10, order.getBuilding());
        assertEquals(20, order.getRoom());

        // verify

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(15)
    public void GivenInvalidIngredient_WhenAdding_ThenThrow_Test() {
        orderManager.createOrder(1, 1);
        pancakeService.startNewPancake(order.getId());
        String message = assertThrows(IllegalArgumentException.class,
                () -> pancakeService.addIngredientToPancake(order.getId(), "mustard")).getMessage();
        assertEquals("No ingredient with name mustard", message);
    }

    @Test
    @org.junit.jupiter.api.Order(16)
    public void GivenUnpreparedOrder_WhenDelivering_ThenNull_Test() {
        orderManager.createOrder(1, 1);
        orderManager.completeOrder(order.getId());
        Object[] order = orderManager.deliverOrder(this.order.getId());
        assertNull(order);
    }

    @Test
    @org.junit.jupiter.api.Order(20)
    public void GivenOrderExists_WhenAddingPancakes_ThenCorrectNumberOfPancakesAdded_Test() {
        // setup

        // exercise
        addPancakes();

        // verify
        List<String> ordersPancakes = orderManager.viewOrder(order.getId());

        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), ordersPancakes);

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(30)
    public void GivenPancakesExists_WhenRemovingPancakes_ThenCorrectNumberOfPancakesRemoved_Test() {
        // setup

        // exercise
        orderManager.removePancakes(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, order.getId(), 2);
        orderManager.removePancakes(MILK_CHOCOLATE_PANCAKE_DESCRIPTION, order.getId(), 3);
        orderManager.removePancakes(MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, order.getId(), 1);

        // verify
        List<String> ordersPancakes = orderManager.viewOrder(order.getId());

        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), ordersPancakes);

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(40)
    public void GivenOrderExists_WhenCompletingOrder_ThenOrderCompleted_Test() {
        // setup

        // exercise
        orderManager.completeOrder(order.getId());

        // verify
        Set<UUID> completedOrdersOrders = orderManager.listCompletedOrders();
        assertTrue(completedOrdersOrders.contains(order.getId()));

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(50)
    public void GivenOrderExists_WhenPreparingOrder_ThenOrderPrepared_Test() {
        // setup

        // exercise
        orderManager.prepareOrder(order.getId());

        // verify
        Set<UUID> completedOrders = orderManager.listCompletedOrders();
        assertFalse(completedOrders.contains(order.getId()));

        Set<UUID> preparedOrders = orderManager.listPreparedOrders();
        assertTrue(preparedOrders.contains(order.getId()));

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(60)
    public void GivenOrderExists_WhenDeliveringOrder_ThenCorrectOrderReturnedAndOrderRemovedFromTheDatabase_Test() {
        // setup
        List<String> pancakesToDeliver = orderManager.viewOrder(order.getId());

        // exercise
        Object[] deliveredOrder = orderManager.deliverOrder(order.getId());

        // verify
        Set<UUID> completedOrders = orderManager.listCompletedOrders();
        assertFalse(completedOrders.contains(order.getId()));

        Set<UUID> preparedOrders = orderManager.listPreparedOrders();
        assertFalse(preparedOrders.contains(order.getId()));

        List<String> ordersPancakes = orderManager.viewOrder(order.getId());

        assertEquals(List.of(), ordersPancakes);
        assertEquals(order.getId(), ((Order) deliveredOrder[0]).getId());
        assertEquals(pancakesToDeliver, (List<String>) deliveredOrder[1]);

        // tear down
        order = null;
    }

    @Test
    @org.junit.jupiter.api.Order(70)
    public void GivenOrderExists_WhenCancellingOrder_ThenOrderAndPancakesRemoved_Test() {
        // setup
        order = orderManager.createOrder(10, 20);
        addPancakes();

        // exercise
        orderManager.cancelOrder(order.getId());

        // verify
        Set<UUID> completedOrders = orderManager.listCompletedOrders();
        assertFalse(completedOrders.contains(order.getId()));

        Set<UUID> preparedOrders = orderManager.listPreparedOrders();
        assertFalse(preparedOrders.contains(order.getId()));

        List<String> ordersPancakes = orderManager.viewOrder(order.getId());

        assertEquals(List.of(), ordersPancakes);

        // tear down
    }

    private void addPancakes() {
        pancakeService.startNewPancake(order.getId());
        pancakeService.addIngredientToPancake(order.getId(), "dark chocolate");
        pancakeService.addPancake(order, 3);

        pancakeService.startNewPancake(order.getId());
        pancakeService.addIngredientToPancake(order.getId(), "milk chocolate");
        pancakeService.addPancake(order, 3);

        pancakeService.startNewPancake(order.getId());
        pancakeService.addIngredientToPancake(order.getId(), "milk chocolate");
        pancakeService.addIngredientToPancake(order.getId(), "hazelnuts");
        pancakeService.addPancake(order, 3);
    }
}
