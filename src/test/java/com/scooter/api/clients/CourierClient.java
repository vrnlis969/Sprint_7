package com.scooter.api.clients;

import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import com.scooter.api.models.Courier;
import com.scooter.api.models.CourierLogin;

import static io.restassured.RestAssured.given;

public class CourierClient extends BaseClient {
    private static final String COURIER_PATH = "/api/v1/courier";

    @Step("Создание курьера: {courier.login}")
    public ValidatableResponse create(Courier courier) {
        return given()
                .spec(getBaseSpec())
                .body(courier)
                .when()
                .post(COURIER_PATH)
                .then();
    }

    @Step("Логин курьера: {login.login}")
    public ValidatableResponse login(CourierLogin login) {
        return given()
                .spec(getBaseSpec())
                .body(login)
                .when()
                .post(COURIER_PATH + "/login")
                .then();
    }

    @Step("Удаление курьера с id {id}")
    public ValidatableResponse delete(int id) {
        return given()
                .spec(getBaseSpec())
                .when()
                .delete(COURIER_PATH + "/" + id)
                .then();
    }
}