package org.pancakelab.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.pancakelab.model.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderManagerTest {

    private static final Logger LOGGER = Logger.getLogger(OrderLog.class.getName());
    private final List<LogRecord> logRecords = new ArrayList<>();
    private Handler logHandler;

    private PancakeService pancakeService = new PancakeService();
    private OrderManager orderManager = new OrderManager(pancakeService);
    private UUID orderId = orderManager.createOrder(10, 20);

    private final static String DARK_CHOCOLATE_PANCAKE_DESCRIPTION = "Delicious pancake with dark chocolate!";
    private final static String MILK_CHOCOLATE_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate!";
    private final static String MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION =
            "Delicious pancake with milk chocolate, hazelnuts!";

    @BeforeEach
    void setUp() {
        logHandler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                logRecords.add(record);
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        };
        LOGGER.addHandler(logHandler);
        LOGGER.setLevel(Level.ALL);
        logHandler.setLevel(Level.ALL);
    }

    @AfterEach
    void tearDown() {
        logRecords.clear();
        LOGGER.removeHandler(logHandler);
    }

    @Test
    @org.junit.jupiter.api.Order(15)
    public void GivenInvalidIngredient_WhenAdding_ThenThrow_Test() {
        orderManager.createOrder(1, 1);
        pancakeService.startNewPancake(orderId);
        String message = assertThrows(IllegalArgumentException.class,
                () -> pancakeService.addIngredientToPancake(orderId, "mustard")).getMessage();
        assertEquals("No ingredient with name mustard", message);
    }

    @Test
    @org.junit.jupiter.api.Order(16)
    public void GivenUnpreparedOrder_WhenDelivering_ThenNull_Test() {
        orderManager.createOrder(1, 1);
        orderManager.completeOrder(orderId);
        Object[] order = orderManager.deliverOrder(orderId);
        assertNull(order);
    }

    @Test
    @org.junit.jupiter.api.Order(20)
    public void GivenOrderExists_WhenAddingPancakes_ThenCorrectNumberOfPancakesAdded_Test() {
        // setup

        // exercise
        addPancakes();

        // verify
        List<String> ordersPancakes = orderManager.viewOrder(orderId);

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
        orderManager.removePancakes(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, orderId, 2);
        orderManager.removePancakes(MILK_CHOCOLATE_PANCAKE_DESCRIPTION, orderId, 3);
        orderManager.removePancakes(MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, orderId, 1);

        // verify
        List<String> ordersPancakes = orderManager.viewOrder(orderId);

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
        orderManager.completeOrder(orderId);

        // verify
        Set<UUID> completedOrdersOrders = orderManager.listCompletedOrders();
        assertTrue(completedOrdersOrders.contains(orderId));

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(50)
    public void GivenOrderExists_WhenPreparingOrder_ThenOrderPrepared_Test() {
        // setup

        // exercise
        orderManager.prepareOrder(orderId);

        // verify
        Set<UUID> completedOrders = orderManager.listCompletedOrders();
        assertFalse(completedOrders.contains(orderId));

        Set<UUID> preparedOrders = orderManager.listPreparedOrders();
        assertTrue(preparedOrders.contains(orderId));

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(60)
    public void GivenOrderExists_WhenDeliveringOrder_ThenCorrectOrderReturnedAndOrderRemovedFromTheDatabase_Test() {
        // setup
        List<String> pancakesToDeliver = orderManager.viewOrder(orderId);

        // exercise
        Object[] deliveredOrder = orderManager.deliverOrder(orderId);
        assertEquals(1, logRecords.size());
        assertEquals(
                "Order %s with 3 pancakes for building %d, room %d out for delivery.".formatted(orderId.toString(), 10,
                        20), logRecords.get(0).getMessage());

        // verify
        Set<UUID> completedOrders = orderManager.listCompletedOrders();
        assertFalse(completedOrders.contains(orderId));

        Set<UUID> preparedOrders = orderManager.listPreparedOrders();
        assertFalse(preparedOrders.contains(orderId));

        List<String> ordersPancakes = orderManager.viewOrder(orderId);

        assertEquals(List.of(), ordersPancakes);
        assertEquals(orderId, ((Order) deliveredOrder[0]).getId());
        assertEquals(pancakesToDeliver, (List<String>) deliveredOrder[1]);

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(70)
    public void GivenOrderExists_WhenCancellingOrder_ThenOrderAndPancakesRemoved_Test() {
        // setup
        orderId = orderManager.createOrder(10, 20);
        addPancakes();

        // exercise
        orderManager.cancelOrder(orderId);

        // verify
        Set<UUID> completedOrders = orderManager.listCompletedOrders();
        assertFalse(completedOrders.contains(orderId));

        Set<UUID> preparedOrders = orderManager.listPreparedOrders();
        assertFalse(preparedOrders.contains(orderId));

        List<String> ordersPancakes = orderManager.viewOrder(orderId);

        assertEquals(List.of(), ordersPancakes);

        // tear down
    }

    private void addPancakes() {
        pancakeService.startNewPancake(orderId);
        pancakeService.addIngredientToPancake(orderId, "dark chocolate");
        orderManager.addPancake(orderId, 3);

        pancakeService.startNewPancake(orderId);
        pancakeService.addIngredientToPancake(orderId, "milk chocolate");
        orderManager.addPancake(orderId, 3);

        pancakeService.startNewPancake(orderId);
        pancakeService.addIngredientToPancake(orderId, "milk chocolate");
        pancakeService.addIngredientToPancake(orderId, "hazelnuts");
        orderManager.addPancake(orderId, 3);
    }
}
