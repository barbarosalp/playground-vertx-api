package com.barb.vertxapi;

import com.barb.vertxapi.verticles.HttpVerticle;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.config.ConfigRetriever;
import io.vertx.reactivex.core.Vertx;

public class App {

  public static void main(String[] args) {

    Vertx vertx = Vertx.vertx();
    ConfigStoreOptions fileStore = new ConfigStoreOptions()
        .setType("file")
        .setFormat("properties")
        .setConfig(new JsonObject().put("path", "application.properties"));

    ConfigStoreOptions envStore = new ConfigStoreOptions().setType("env");

    ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions();
    retrieverOptions.addStore(fileStore).addStore(envStore);
    ConfigRetriever retriever = ConfigRetriever.create(vertx, retrieverOptions);

    retriever.rxGetConfig()
        .map(config -> new DeploymentOptions().setConfig(config))
        .flatMap(options -> vertx.rxDeployVerticle(HttpVerticle.class.getName(), options).map(any -> options))
        .subscribe(
            deployId -> System.out.println("App started successfully!"),
            error -> {
              error.printStackTrace();
              System.exit(1);
            }
        );
  }
}
