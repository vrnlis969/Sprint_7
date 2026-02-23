package com.scooter.api.tests;

import com.scooter.api.clients.OrderClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import com.scooter.api.models.OrdersListResponse;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(AllureJunit5.class)
public class GetOrdersTest {
    private OrderClient orderClient = new OrderClient();

    @Test
    @Description("В теле ответа возвращается список заказов")
    public void testGetOrdersReturnsList() {
        var response = orderClient.getAll()
                .statusCode(200)
                .extract().as(OrdersListResponse.class);

        // Проверяем, что список заказов не null (может быть пустым, но это нормально)
        assertNotNull(response.getOrders(), "Список заказов не должен быть null");
        // Можно также проверить, что каждый заказ содержит ожидаемые поля, но для задания достаточно наличия списка.
    }
}
