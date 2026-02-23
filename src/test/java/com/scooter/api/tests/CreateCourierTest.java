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
        // Если курьер был создан и его ID сохранён, удаляем его
        if (createdCourierId != 0) {
            courierClient.delete(createdCourierId)
                    .statusCode(200)
                    .body("ok", is(true));
        }
    }

    @Test
    @Description("Курьера можно создать")
    public void testCreateCourierSuccess() {
        // Генерируем уникальные данные
        courier = Courier.builder()
                .login("ninja" + System.currentTimeMillis())
                .password("1234")
                .firstName("saske")
                .build();

        ValidatableResponse response = courierClient.create(courier);
        response.statusCode(201)
                .body("ok", is(true));

        // После создания нужно залогиниться, чтобы получить id для удаления
        CourierLogin login = new CourierLogin(courier.getLogin(), courier.getPassword());
        var loginResponse = courierClient.login(login)
                .statusCode(200)
                .extract().as(CourierLoginResponse.class);
        createdCourierId = loginResponse.getId();
        assertTrue(createdCourierId > 0, "ID курьера должен быть положительным");
    }

    @Test
    @Description("Нельзя создать двух одинаковых курьеров")
    public void testCreateDuplicateCourier() {
        // Создаём первого курьера
        courier = Courier.builder()
                .login("duplicate" + System.currentTimeMillis())
                .password("1234")
                .firstName("test")
                .build();

        courierClient.create(courier)
                .statusCode(201)
                .body("ok", is(true));

        // Получаем ID для удаления
        CourierLogin login = new CourierLogin(courier.getLogin(), courier.getPassword());
        var loginResponse = courierClient.login(login)
                .statusCode(200)
                .extract().as(CourierLoginResponse.class);
        createdCourierId = loginResponse.getId();

        // Пытаемся создать курьера с таким же логином
        ValidatableResponse response = courierClient.create(courier);
        response.statusCode(409)                     // реальный статус от сервера
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
                .statusCode(400)
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
                .statusCode(400)
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

        // Создаём курьера без имени
        courierClient.create(courierWithoutFirstName)
                .statusCode(201)
                .body("ok", is(true));

        // Логинимся, чтобы получить ID для удаления
        CourierLogin loginCredentials = new CourierLogin(login, password);
        int id = courierClient.login(loginCredentials)
                .statusCode(200)
                .extract().as(CourierLoginResponse.class)
                .getId();

        // Удаляем курьера (не сохраняем в поле класса, чтобы не мешать @AfterEach)
        courierClient.delete(id)
                .statusCode(200)
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
                .statusCode(201)
                .extract().as(CourierCreateResponse.class);
        assertTrue(response.isOk());

        // Логинимся для получения id удаления
        CourierLogin login = new CourierLogin(courier.getLogin(), courier.getPassword());
        var loginResponse = courierClient.login(login)
                .statusCode(200)
                .extract().as(CourierLoginResponse.class);
        createdCourierId = loginResponse.getId();
    }
}