package com.scooter.api.clients;

import io.restassured.response.ValidatableResponse;
import com.scooter.api.models.Courier;
import com.scooter.api.models.CourierLogin;

import static io.restassured.RestAssured.given;

public class CourierClient extends BaseClient {
    private static final String COURIER_PATH = "/api/v1/courier";

    public ValidatableResponse create(Courier courier) {
        return given()
                .spec(getBaseSpec())
                .body(courier)
                .when()
                .post(COURIER_PATH)
                .then();
    }

    public ValidatableResponse login(CourierLogin login) {
        return given()
                .spec(getBaseSpec())
                .body(login)
                .when()
                .post(COURIER_PATH + "/login")
                .then();
    }

    public ValidatableResponse delete(int id) {
        return given()
                .spec(getBaseSpec())
                .when()
                .delete(COURIER_PATH + "/" + id)
                .then();
    }
}
