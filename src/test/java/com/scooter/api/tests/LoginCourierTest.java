package com.scooter.api.tests;

import com.scooter.api.clients.CourierClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import com.scooter.api.models.Courier;
import com.scooter.api.models.CourierLogin;
import com.scooter.api.models.CourierLoginResponse;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(AllureJunit5.class)
public class LoginCourierTest {
    private CourierClient courierClient;
    private int createdCourierId;
    private Courier courier;

    @BeforeEach
    public void setUp() {
        courierClient = new CourierClient();
    }

    @AfterEach
    public void cleanUp() {
        if (createdCourierId != 0) {
            courierClient.delete(createdCourierId)
                    .statusCode(200)
                    .body("ok", is(true));
        }
    }

    private Courier createUniqueCourier() {
        Courier courier = Courier.builder()
                .login("login" + System.currentTimeMillis())
                .password("pass123")
                .firstName("Name")
                .build();
        courierClient.create(courier).statusCode(201);
        return courier;
    }

    private int loginAndGetId(Courier courier) {
        CourierLogin login = new CourierLogin(courier.getLogin(), courier.getPassword());
        return courierClient.login(login)
                .statusCode(200)
                .extract().as(CourierLoginResponse.class)
                .getId();
    }

    @Test
    @Description("Курьер может авторизоваться")
    public void testLoginSuccess() {
        courier = createUniqueCourier();
        createdCourierId = loginAndGetId(courier);
        assertTrue(createdCourierId > 0);
    }

    @Test
    @Description("Запрос без логина должен возвращать 400")
    public void testLoginMissingLogin() {
        courier = createUniqueCourier();
        createdCourierId = loginAndGetId(courier);

        CourierLogin loginWithoutLogin = new CourierLogin(null, courier.getPassword());
        courierClient.login(loginWithoutLogin)
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @Description("Запрос без пароля должен возвращать 400, но сейчас сервер возвращает 504 (проблема API)")
    public void testLoginMissingPassword() {
        courier = createUniqueCourier();
        createdCourierId = loginAndGetId(courier);

        CourierLogin loginWithoutPass = new CourierLogin(courier.getLogin(), null);
        ValidatableResponse response = courierClient.login(loginWithoutPass);

        int statusCode = response.extract().statusCode();
        if (statusCode == 504) {
            fail("Сервер вернул 504 Gateway Timeout вместо ожидаемого 400. Это проблема API.");
        }
        response.statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @Description("Система вернёт ошибку, если неправильно указать логин или пароль")
    public void testLoginWrongCredentials() {
        courier = createUniqueCourier();
        createdCourierId = loginAndGetId(courier);

        CourierLogin wrongPass = new CourierLogin(courier.getLogin(), "wrong");
        courierClient.login(wrongPass)
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));

        CourierLogin wrongLogin = new CourierLogin("nonexistent", courier.getPassword());
        courierClient.login(wrongLogin)
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @Description("Если авторизоваться под несуществующим пользователем, запрос возвращает ошибку")
    public void testLoginNonExistentUser() {
        CourierLogin nonExistent = new CourierLogin("ghost", "pass");
        courierClient.login(nonExistent)
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @Description("Успешный запрос возвращает id")
    public void testLoginReturnsId() {
        courier = createUniqueCourier();
        int id = loginAndGetId(courier);
        createdCourierId = id;
        assertTrue(id > 0);
    }
}