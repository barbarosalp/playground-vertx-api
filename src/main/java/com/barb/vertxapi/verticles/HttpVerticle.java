package com.barb.vertxapi.verticles;

import com.barb.vertxapi.api.WhiskyRequest;
import com.barb.vertxapi.utils.Consts;
import io.reactivex.Single;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class HttpVerticle extends AbstractVerticle {


  @Override
  public void start(final Promise<Void> promise) {

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.route("/").handler(this::indexHandler);
    router.get("/api/whiskies").handler(this::getWhiskiesHandler);
    router.get("/api/whiskies/:id").handler(this::getWhiskiesHandler);
    router.post("/api/whiskies").handler(this::addWhiskyHandler);
    router.delete("/api/whiskies/:id").handler(this::deleteWhiskyHandler);

    final Integer applicationPort = config().getInteger(Consts.APPLICATION_PORT, 8080);

    vertx.createHttpServer()
        .requestHandler(router)
        .rxListen(applicationPort)
        .subscribe(
            success -> promise.complete(),
            error -> promise.fail(error.getCause())
        );
  }

  private void indexHandler(final RoutingContext rc) {
    final HttpServerResponse response = rc.response();
    response
        .putHeader("content-type", "text/html")
        .end("<h1>Hello from my first Vert.x 3 application</h1>");
  }

  private void getWhiskiesHandler(final RoutingContext rc) {
    final HttpServerRequest request = rc.request();
    final HttpServerResponse response = rc.response();
    final String id = request.getParam("id");
    response
        .putHeader("content-type", "application/json; charset=utf-8");

    //    if (id == null) {
    //      response.end(Json.encodePrettily(products.values()));
    //    } else {
    //      final Whisky whisky = products.get(Integer.valueOf(id));
    //      if (whisky == null) {
    //        response.setStatusCode(404).end();
    //      } else {
    //        response.end(Json.encodePrettily(whisky));
    //      }
    //    }

    response.end();
  }

  private void addWhiskyHandler(final RoutingContext rc) {
    final HttpServerResponse response = rc.response();

    response
        .putHeader("content-type", "application/json; charset=utf-8");

    Single.just(rc.getBodyAsJson())
        .map(body -> body.mapTo(WhiskyRequest.class))
        .flatMap(whiskyRequest -> {
          final DeliveryOptions options = new DeliveryOptions().addHeader("action", "set");
          return vertx.eventBus().<String>rxRequest(Consts.EVENT_BUS_DATA_API, JsonObject.mapFrom(whiskyRequest), options);
        })
        .subscribe(
            responseItem -> response.setStatusCode(201).end(),
            error -> response.setStatusCode(500).end(error.getMessage())
        );
  }

  private void deleteWhiskyHandler(final RoutingContext rc) {
    final HttpServerRequest request = rc.request();
    final HttpServerResponse response = rc.response();
    final String id = request.getParam("id");
    if (id == null) {
      response.setStatusCode(400).end();
    } else {
      response.setStatusCode(204).end();
    }
  }
}
