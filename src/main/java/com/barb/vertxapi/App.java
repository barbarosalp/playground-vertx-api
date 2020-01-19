package com.barb.vertxapi;

import com.barb.vertxapi.blog.MyFirstVerticle;
import io.vertx.core.Vertx;

public class App {

  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(MyFirstVerticle.class.getName(), event -> {
      if (event.succeeded()) {
        System.out.println("App started successfully!");
      } else {
        System.out.println("App could not be started!");
        System.exit(1);
      }
    });
  }

}
