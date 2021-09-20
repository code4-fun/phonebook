package com.phonebook.ApiTests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import java.util.ArrayList;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class ApiTests {
  /**
   * Tests that the endpoint returns 200 status code
   * and the header Content-Type: application/json is present.
   */
  @Test()
  public void getAllUsersTest(){
    given().when().get("http://localhost:8080/users")
        .then()
        .statusCode(200)
        .header("Content-Type", "application/json");
  }

  /**
   * Tests the creation of new user.
   */
  @Test
  public void createNewUserTest(){
    given().body("{\"firstName\": \"Alex\", \"lastName\": \"Smith\"}")
        .contentType("application/json")
        .when().post("http://localhost:8080/users")
        .then()
        .statusCode(201)
        .header("Content-Type", "application/json")
        .body("firstName", equalTo("Alex"))
        .body("lastName", equalTo("Smith"));
  }

  /**
   * Tests that the creation of new user is impossible without
   * the firstName field.
   */
  @Test
  public void unableToCreateNewUserTest(){
    String body = "{\"lastName\": \"Smith\"}";
    given().body(body).contentType("application/json")
        .when().post("http://localhost:8080/users")
        .then()
        .statusCode(400)
        .header("Content-Type", "application/json")
        .body("firstName", anyOf(equalTo("должно быть задано"),
            equalTo("не может быть пусто"),
            equalTo("must not be null"),
            equalTo("must not be blank")));
  }

  /**
   * Tests the deleting of the user.
   * To make sure the user to be deleted exists in the
   * database it is created first.
   */
  @Test
  public void deleteUserTest(){
    given().body("{\"firstName\": \"Mike\", \"lastName\": \"Smith\"}")
        .contentType("application/json")
        .when().post("http://localhost:8080/users")
        .then()
        .statusCode(201)
        .header("Content-Type", "application/json")
        .body("firstName", equalTo("Mike"))
        .body("lastName", equalTo("Smith"));

    ArrayList<Integer> ids = given().when()
        .get("http://localhost:8080/users/search?name=Mike")
        .then()
        .statusCode(200)
        .extract().path("id");

    given().when().delete("http://localhost:8080/users/" +
        ids.get(new Random().nextInt(ids.size())))
        .then()
        .statusCode(202);
  }

  /**
   * Tests the searching of the user.
   * Before searching user by its name the user is created.
   */
  @Test
  public void searchUserTest(){
    given().body("{\"firstName\": \"Max\", \"lastName\": \"Smith\"}")
        .contentType("application/json")
        .when().post("http://localhost:8080/users")
        .then()
        .statusCode(201)
        .header("Content-Type", "application/json")
        .body("firstName", equalTo("Max"))
        .body("lastName", equalTo("Smith"));

    given().when().get("http://localhost:8080/users/search?name=Max")
        .then()
        .statusCode(200)
        .header("Content-Type", "application/json")
        .body("$.size()", greaterThan(0))
        .body("[0].firstName", equalTo("Max"))
        .body("[0].lastName", equalTo("Smith"));
  }

  /**
   * Tests the creation of the contact.
   * First the user is created then the contact id added to this user.
   */
  @Test
  public void createContactTest(){
    given().body("{\"firstName\": \"Jordan\", \"lastName\": \"Smith\"}")
        .contentType("application/json")
        .when().post("http://localhost:8080/users")
        .then()
        .statusCode(201)
        .header("Content-Type", "application/json")
        .body("firstName", equalTo("Jordan"))
        .body("lastName", equalTo("Smith"));

    ArrayList<Integer> ids = given().when()
        .get("http://localhost:8080/users/search?name=Jordan")
        .then()
        .statusCode(200)
        .extract().path("id");

    given().body("{\"firstName\": \"Arthur\", \"lastName\": \"Harper\", " +
        "\"phone\": \"5555555555\", \"email\": \"art@gmail.com\"}")
        .contentType("application/json")
        .when().post("http://localhost:8080/users/" +
        ids.get(new Random().nextInt(ids.size())) + "/contacts")
        .then()
        .statusCode(201)
        .header("Content-Type", "application/json")
        .body("firstName", equalTo("Arthur"))
        .body("lastName", equalTo("Harper"))
        .body("phone", equalTo("5555555555"))
        .body("email", equalTo("art@gmail.com"));
  }

  /**
   * Tests that the contact cannot be created since the
   * phone and the email provided have a wrong format.
   */
  @Test
  public void unableCreateContactTest(){
    given().body("{\"firstName\": \"Jonas\", \"lastName\": \"Smith\"}")
        .contentType("application/json")
        .when().post("http://localhost:8080/users")
        .then()
        .statusCode(201)
        .header("Content-Type", "application/json")
        .body("firstName", equalTo("Jonas"))
        .body("lastName", equalTo("Smith"));

    ArrayList<Integer> ids = given().when()
        .get("http://localhost:8080/users/search?name=Jonas")
        .then()
        .statusCode(200)
        .extract().path("id");

    given().body("{\"firstName\": \"Lee\", \"lastName\": \"Cochran\", " +
        "\"phone\": \"777777777\", \"email\": \"art@gmail.\"}")
        .contentType("application/json")
        .when().post("http://localhost:8080/users/" +
        ids.get(new Random().nextInt(ids.size())) + "/contacts")
        .then()
        .statusCode(400)
        .header("Content-Type", "application/json")
        .body("phone", anyOf(containsString("должно соответствовать шаблону"),
            containsString("must match")))
        .body("email", anyOf(containsString("email определен в неверном формате"),
            containsString("must be a well-formed email address")));
  }
}