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
import static org.apache.http.HttpStatus.*;

@ExtendWith(AllureJunit5.class)
public class LoginCourierTest {
    private CourierClient courierClient;
    private Courier courier;
    private int courierId;

    @BeforeEach
    public void setUp() {
        courierClient = new CourierClient();
        // Создаём уникального курьера для тестов логина
        courier = Courier.builder()
                .login("login" + System.currentTimeMillis())
                .password("pass123")
                .firstName("Name")
                .build();
        courierClient.create(courier).statusCode(SC_CREATED);
        // Логинимся, чтобы получить id
        CourierLogin login = new CourierLogin(courier.getLogin(), courier.getPassword());
        courierId = courierClient.login(login)
                .statusCode(SC_OK)
                .extract().as(CourierLoginResponse.class)
                .getId();
    }

    @AfterEach
    public void cleanUp() {
        courierClient.delete(courierId).statusCode(SC_OK);
    }

    @Test
    @Description("Курьер может авторизоваться")
    public void testLoginSuccess() {
        CourierLogin login = new CourierLogin(courier.getLogin(), courier.getPassword());
        int id = courierClient.login(login)
                .statusCode(SC_OK)
                .extract().as(CourierLoginResponse.class)
                .getId();
        assertEquals(courierId, id, "ID должен совпадать с созданным");
    }

    @Test
    @Description("Запрос без логина должен возвращать 400")
    public void testLoginMissingLogin() {
        CourierLogin loginWithoutLogin = new CourierLogin(null, courier.getPassword());
        courierClient.login(loginWithoutLogin)
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @Description("Запрос без пароля должен возвращать 400, но сейчас сервер возвращает 504 (проблема API)")
    public void testLoginMissingPassword() {
        CourierLogin loginWithoutPass = new CourierLogin(courier.getLogin(), null);
        ValidatableResponse response = courierClient.login(loginWithoutPass);

        int statusCode = response.extract().statusCode();
        if (statusCode == SC_GATEWAY_TIMEOUT) { // 504
            fail("Сервер вернул 504 Gateway Timeout вместо ожидаемого 400. Это проблема API.");
        }
        response.statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @Description("Система вернёт ошибку, если неправильно указать логин или пароль")
    public void testLoginWrongCredentials() {
        CourierLogin wrongPass = new CourierLogin(courier.getLogin(), "wrong");
        courierClient.login(wrongPass)
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));

        CourierLogin wrongLogin = new CourierLogin("nonexistent", courier.getPassword());
        courierClient.login(wrongLogin)
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @Description("Если авторизоваться под несуществующим пользователем, запрос возвращает ошибку")
    public void testLoginNonExistentUser() {
        CourierLogin nonExistent = new CourierLogin("ghost", "pass");
        courierClient.login(nonExistent)
                .statusCode(SC_NOT_FOUND)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @Description("Успешный запрос возвращает id")
    public void testLoginReturnsId() {
        CourierLogin login = new CourierLogin(courier.getLogin(), courier.getPassword());
        int id = courierClient.login(login)
                .statusCode(SC_OK)
                .extract().as(CourierLoginResponse.class)
                .getId();
        assertTrue(id > 0);
    }
}