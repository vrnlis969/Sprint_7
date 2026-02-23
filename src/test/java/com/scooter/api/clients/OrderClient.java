package com.scooter.api.clients;


import io.restassured.response.ValidatableResponse;
import com.scooter.api.models.Order;

import static io.restassured.RestAssured.given;

public class OrderClient extends BaseClient {
    private static final String ORDER_PATH = "/api/v1/orders";

    public ValidatableResponse create(Order order) {
        return given()
                .spec(getBaseSpec())
                .body(order)
                .when()
                .post(ORDER_PATH)
                .then();
    }

    public ValidatableResponse getAll() {
        return given()
                .spec(getBaseSpec())
                .when()
                .get(ORDER_PATH)
                .then();
    }
}