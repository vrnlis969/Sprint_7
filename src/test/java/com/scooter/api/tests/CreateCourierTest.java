package com.scooter.api.tests;

import com.scooter.api.clients.CourierClient;
import io.qameta.allure.Description;
import io.qameta.allure.junit5.AllureJunit5;
import com.scooter.api.models.Courier;
import com.scooter.api.models.CourierCreateResponse;
import com.scooter.api.models.CourierLogin;
import com.scooter.api.models.CourierLoginResponse;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.apache.http.HttpStatus.*;

@ExtendWith(AllureJunit5.class)
public class CreateCourierTest {
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
                    .statusCode(SC_OK)
                    .body("ok", is(true));
        }
    }

    @Test
    @Description("Курьера можно создать")
    public void testCreateCourierSuccess() {
        courier = Courier.builder()
                .login("ninja" + System.currentTimeMillis())
                .password("1234")
                .firstName("saske")
                .build();

        ValidatableResponse response = courierClient.create(courier);
        response.statusCode(SC_CREATED)
                .body("ok", is(true));

        CourierLogin login = new CourierLogin(courier.getLogin(), courier.getPassword());
        var loginResponse = courierClient.login(login)
                .statusCode(SC_OK)
                .extract().as(CourierLoginResponse.class);
        createdCourierId = loginResponse.getId();
        assertTrue(createdCourierId > 0, "ID курьера должен быть положительным");
    }

    @Test
    @Description("Нельзя создать двух одинаковых курьеров")
    public void testCreateDuplicateCourier() {
        courier = Courier.builder()
                .login("duplicate" + System.currentTimeMillis())
                .password("1234")
                .firstName("test")
                .build();

        courierClient.create(courier)
                .statusCode(SC_CREATED)
                .body("ok", is(true));

        CourierLogin login = new CourierLogin(courier.getLogin(), courier.getPassword());
        var loginResponse = courierClient.login(login)
                .statusCode(SC_OK)
                .extract().as(CourierLoginResponse.class);
        createdCourierId = loginResponse.getId();

        ValidatableResponse response = courierClient.create(courier);
        response.statusCode(SC_CONFLICT)
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    @Description("Нельзя создать курьера без логина")
    public void testCreateCourierMissingLogin() {
        Courier courierWithoutLogin = Courier.builder()
                .password("1234")
                .firstName("saske")
                .build();

        courierClient.create(courierWithoutLogin)
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @Description("Нельзя создать курьера без пароля")
    public void testCreateCourierMissingPassword() {
        Courier courierWithoutPassword = Courier.builder()
                .login("ninja" + System.currentTimeMillis())
                .firstName("saske")
                .build();

        courierClient.create(courierWithoutPassword)
                .statusCode(SC_BAD_REQUEST)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Test
    @Description("Можно создать курьера без имени (поле firstName необязательное)")
    public void testCreateCourierWithoutFirstName() {
        String login = "ninja" + System.currentTimeMillis();
        String password = "1234";
        Courier courierWithoutFirstName = Courier.builder()
                .login(login)
                .password(password)
                .build();

        courierClient.create(courierWithoutFirstName)
                .statusCode(SC_CREATED)
                .body("ok", is(true));

        CourierLogin loginCredentials = new CourierLogin(login, password);
        int id = courierClient.login(loginCredentials)
                .statusCode(SC_OK)
                .extract().as(CourierLoginResponse.class)
                .getId();

        courierClient.delete(id)
                .statusCode(SC_OK)
                .body("ok", is(true));
    }

    @Test
    @Description("Успешный запрос возвращает ok: true")
    public void testCreateCourierReturnsOkTrue() {
        courier = Courier.builder()
                .login("ninja" + System.currentTimeMillis())
                .password("1234")
                .firstName("saske")
                .build();

        var response = courierClient.create(courier)
                .statusCode(SC_CREATED)
                .extract().as(CourierCreateResponse.class);
        assertTrue(response.isOk());

        CourierLogin login = new CourierLogin(courier.getLogin(), courier.getPassword());
        var loginResponse = courierClient.login(login)
                .statusCode(SC_OK)
                .extract().as(CourierLoginResponse.class);
        createdCourierId = loginResponse.getId();
    }
}