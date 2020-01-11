package com.barb.vertxapi;

import com.barb.vertxapi.blog.MyFirstVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class App {

  public static void main(String[] args) {

    final DeploymentOptions options =
        new DeploymentOptions()
            .setConfig(new JsonObject().put("http.port", 8080));

    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(MyFirstVerticle.class.getName(), options, event -> {
      if (event.succeeded()) {
        System.out.println("App started successfully!");
      } else {
        System.out.println("App could not be started!");
        System.exit(1);
      }
    });
  }

}
