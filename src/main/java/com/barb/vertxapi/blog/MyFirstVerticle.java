package com.barb.vertxapi.blog;

import java.util.LinkedHashMap;
import java.util.Map;

import com.barb.vertxapi.domain.Whisky;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MyFirstVerticle extends AbstractVerticle {

  private Map<Integer, Whisky> products = createSomeData();

  @Override
  public void start(final Promise<Void> promise) {

    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.route("/").handler(this::indexHandler);
    router.get("/api/whiskies").handler(this::getWhiskiesHandler);
    router.get("/api/whiskies/:id").handler(this::getWhiskiesHandler);
    router.post("/api/whiskies").handler(this::addWhiskyHandler);
    router.delete("/api/whiskies/:id").handler(this::deleteWhiskyHandler);

    vertx.createHttpServer()
        .requestHandler(router)
        .listen(config().getInteger("http.port", 8080), result -> {
          if (result.succeeded()) {
            promise.complete();
          } else {
            promise.fail(result.cause());
          }
        });
  }

  private void deleteWhiskyHandler(final RoutingContext rc) {
    final HttpServerRequest request = rc.request();
    final HttpServerResponse response = rc.response();
    final String id = request.getParam("id");
    if (id == null) {
      response.setStatusCode(400).end();
    } else {
      products.remove(Integer.valueOf(id));
      response.setStatusCode(204).end();
    }

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

    if (id == null) {
      response.end(Json.encodePrettily(products.values()));
    } else {
      response.end(Json.encodePrettily(products.get(Integer.valueOf(id))));
    }

  }

  private void addWhiskyHandler(final RoutingContext rc) {
    final HttpServerResponse response = rc.response();
    final Whisky whisky = Json.decodeValue(rc.getBodyAsString(), Whisky.class);
    products.put(whisky.getId(), whisky);
    response
        .setStatusCode(201)
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(Json.encodePrettily(whisky));
  }

  private LinkedHashMap<Integer, Whisky> createSomeData() {
    Whisky bowmore = new Whisky("Bowmore 15 Years Laimrig", "Scotland, Islay");
    Whisky talisker = new Whisky("Talisker 57° North", "Scotland, Island");

    final LinkedHashMap<Integer, Whisky> data = new LinkedHashMap<>();
    data.put(bowmore.getId(), bowmore);
    data.put(talisker.getId(), talisker);

    return data;
  }
}
