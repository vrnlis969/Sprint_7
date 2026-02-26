package com.scooter.api.tests;

import com.scooter.api.clients.OrderClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import com.scooter.api.models.Order;
import com.scooter.api.models.OrderCreateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.apache.http.HttpStatus.*;

@ExtendWith(AllureJunit5.class)
public class CreateOrderTest {
    private OrderClient orderClient = new OrderClient();

    private static Stream<Arguments> provideColorOptions() {
        return Stream.of(
                Arguments.of(Collections.singletonList("BLACK")),
                Arguments.of(Collections.singletonList("GREY")),
                Arguments.of(Arrays.asList("BLACK", "GREY")),
                Arguments.of(Collections.emptyList()) // пустой список = цвет не указан
        );
    }

    @ParameterizedTest
    @MethodSource("provideColorOptions")
    @Description("Создание заказа с различными вариантами цвета (параметризованный тест)")
    public void testCreateOrderWithColors(List<String> colors) {
        Order order = Order.builder()
                .firstName("Naruto")
                .lastName("Uchiha")
                .address("Konoha, 142 apt.")
                .metroStation("4")
                .phone("+7 800 355 35 35")
                .rentTime(5)
                .deliveryDate("2020-06-06")
                .comment("Saske, come back to Konoha")
                .color(colors)
                .build();

        var response = orderClient.create(order)
                .statusCode(SC_CREATED)
                .extract().as(OrderCreateResponse.class);

        assertTrue(response.getTrack() > 0, "Трек заказа должен быть положительным числом");
    }

    @Test
    @Description("Создание заказа без указания цвета (поле color отсутствует)")
    public void testCreateOrderWithoutColor() {
        Order order = Order.builder()
                .firstName("Naruto")
                .lastName("Uchiha")
                .address("Konoha, 142 apt.")
                .metroStation("4")
                .phone("+7 800 355 35 35")
                .rentTime(5)
                .deliveryDate("2020-06-06")
                .comment("Saske, come back to Konoha")
                .color(null) // поле не будет включено в JSON
                .build();

        var response = orderClient.create(order)
                .statusCode(SC_CREATED)
                .extract().as(OrderCreateResponse.class);

        assertTrue(response.getTrack() > 0, "Трек заказа должен быть положительным числом");
    }
}