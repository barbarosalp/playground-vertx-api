package com.barb.vertxapi.verticles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import com.barb.vertxapi.utils.Consts;
import io.reactivex.Maybe;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.SocketAddress;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;

public class DataVerticle extends AbstractVerticle {

  private static final int MAX_RECONNECT_RETRIES = 16;

  private RedisAPI redisApi;
  private static final Random RANDOM = new Random();
  private Map<String, Consumer<Message<JsonObject>>> dbActions;

  @Override
  public void start(final Promise<Void> promise) {

    dbActions = new HashMap<>();
    dbActions.put("set", this::dbSetHandler);
    dbActions.put("get", this::dbGetHandler);

    initRedis(event -> {
      if (event.succeeded()) {
        System.out.println("Data verticle is deployed successfully");
        promise.complete();
      } else {
        System.out.println("Data verticle could not be deployed.");
        promise.fail(event.cause());
      }
    });

    vertx.eventBus().consumer(Consts.EVENT_BUS_DATA_API, this::handleApiMessage);
  }

  private void handleApiMessage(final Message<JsonObject> message) {

    final String action = message.headers().get("action");
    final Consumer<Message<JsonObject>> handler = dbActions.getOrDefault(action, this::dbFallbackHandler);
    handler.accept(message);
  }

  private void dbSetHandler(final Message<JsonObject> message) {
    final String redisKey = "whisky:" + RANDOM.nextInt(1000);
    final String body = message.body().encode();
    final ArrayList<String> p = new ArrayList<>();
    p.add(redisKey);
    p.add(body);
    redisApi.rxSet(p)
        .subscribe(
            success -> message.reply(""),
            error -> message.fail(500, error.getMessage())
        );
  }

  private void dbGetHandler(final Message<JsonObject> message) {
    final String id = "whisky:" + message.body().getString("id");
    redisApi.rxGet(id)
        .switchIfEmpty(Maybe.error(Throwable::new))
        .map(response -> new JsonObject(response.toString()))
        .subscribe(
            item -> message.reply(item),
            error -> message.fail(500, error.getMessage())
        );
  }

  private void dbFallbackHandler(final Message<JsonObject> message) {
    message.fail(500, "Unknown action type.");
  }

  private void initRedis(Handler<AsyncResult<Redis>> handler) {

    final String host = config().getString(Consts.REDIS_HOST);
    final Integer port = config().getInteger(Consts.REDIS_PORT);
    final RedisOptions options = new RedisOptions()
        .addEndpoint(SocketAddress.inetSocketAddress(port, host))
        .setSelect(config().getInteger(Consts.REDIS_DB, 0));

    Redis.createClient(vertx, options)
        .rxConnect()
        .subscribe(
            client -> {
              client.exceptionHandler(e -> attemptReconnect(0));
              redisApi = RedisAPI.api(client);
              handler.handle(Future.succeededFuture());
            },
            error -> {
              error.printStackTrace();
              handler.handle(Future.failedFuture(error.getCause()));
            }
        );
  }

  private void attemptReconnect(int retry) {
    if (retry < MAX_RECONNECT_RETRIES) {
      // we should stop now, as there's nothing we can do.
      System.out.println("Redis max reconnect attempt is reached. `" + MAX_RECONNECT_RETRIES + "` times.");
      return;
    }

    System.out.println("Redis, trying to reconnect... Attempt " + retry);
    vertx.setTimer(250, timer -> initRedis(onReconnect -> {
      if (onReconnect.failed()) {
        attemptReconnect(retry + 1);
      }
    }));

  }
}
