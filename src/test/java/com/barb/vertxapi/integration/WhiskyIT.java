package com.barb.vertxapi.integration;

import com.barb.vertxapi.domain.Whisky;
import com.jayway.restassured.RestAssured;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.jayway.restassured.RestAssured.delete;
import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;

public class WhiskyIT {
  @BeforeClass
  public static void configureRestAssured() {
    RestAssured.baseURI = "http://localghost";
    RestAssured.port = Integer.getInteger("it.http.port", 30080);
  }

  @AfterClass
  public static void unconfigureRestAssured() {
    RestAssured.reset();
  }

  @Test
  public void checkThatWeCanRetrieveIndividualProduct() {
    // Get the list of bottles, ensure it's a success and extract the first id.
    final int id = RestAssured.get("/api/whiskies").then()
        .assertThat()
        .statusCode(200)
        .extract()
        .jsonPath().getInt("find { it.name=='Bowmore 15 Years Laimrig' }.id");

    // Now get the individual resource and check the content
    RestAssured.get("/api/whiskies/" + id).then()
        .assertThat()
        .statusCode(200)
        .body("name", CoreMatchers.equalTo("Bowmore 15 Years Laimrig"))
        .body("origin", CoreMatchers.equalTo("Scotland, Islay"))
        .body("id", CoreMatchers.equalTo(id));
  }

//  @Test
//  public void checkWeCanAddAndDeleteAProduct() {
//    // Create a new bottle and retrieve the result (as a Whisky instance).
//    Whisky whisky = given()
//        .body("{\"name\":\"Jameson\", \"origin\":\"Ireland\"}")
//        .request()
//        .post("/api/whiskies")
//        .thenReturn()
//        .as(Whisky.class);
//
//    MatcherAssert.assertThat(whisky.getName(),Matchers.is("Jameson"));
//    MatcherAssert.assertThat(whisky.getOrigin(), Matchers.is("Ireland") );
//    MatcherAssert.assertThat(whisky.getId(), Matchers.greaterThan(0));
//
//    // Check that it has created an individual resource, and check the content.
//    get("/api/whiskies/" + whisky.getId()).then()
//        .assertThat()
//        .statusCode(200)
//        .body("name", CoreMatchers.equalTo("Jameson"))
//        .body("origin", CoreMatchers.equalTo("Ireland"))
//        .body("id", CoreMatchers.equalTo(whisky.getId()));
//
//    // Delete the bottle
//    delete("/api/whiskies/" + whisky.getId()).then().assertThat().statusCode(204);
//
//    // Check that the resource is not available anymore
//    get("/api/whiskies/" + whisky.getId()).then()
//        .assertThat()
//        .statusCode(404);
//  }
}
