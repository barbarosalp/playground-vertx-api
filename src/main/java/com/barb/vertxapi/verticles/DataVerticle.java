package com.barb.vertxapi.verticles;

import java.util.ArrayList;
import java.util.Random;

import com.barb.vertxapi.utils.Consts;
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

  private Redis redisClient;
  private RedisAPI redisApi;
  private static final Random RANDOM = new Random();

  @Override
  public void start(final Promise<Void> promise) {

    initRedisClient(event -> {
      if (event.succeeded()) {
        redisApi = RedisAPI.api(redisClient);
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
    final String redisKey = "whisky:" + RANDOM.nextInt();

    switch (action) {
      case "set":

        final String body = message.body().encode();
        final ArrayList<String> p = new ArrayList<>();
        p.add(redisKey);
        p.add(body);
        redisApi.rxSet(p)
            .subscribe(
                success -> message.reply(""),
                error -> message.fail(500, error.getMessage())
            );

        break;
      case "get":

        redisApi.rxGet(redisKey)
            .subscribe(
                success -> message.reply(success.toString()),
                error -> message.fail(500, error.getMessage())
            );

        break;
      default:
        break;
    }
  }

  private void initRedisClient(Handler<AsyncResult<Redis>> handler) {

    final String host = config().getString(Consts.REDIS_HOST);
    final Integer port = config().getInteger(Consts.REDIS_PORT);
    final RedisOptions options = new RedisOptions()
        .addEndpoint(SocketAddress.inetSocketAddress(port, host))
        .setSelect(config().getInteger(Consts.REDIS_DB, 0));

    Redis.createClient(vertx, options)
        .rxConnect()
        .subscribe(
            client -> {
              redisClient = client;
              redisClient.exceptionHandler(e -> attemptReconnect(0));
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
    vertx.setTimer(250, timer -> initRedisClient(onReconnect -> {
      if (onReconnect.failed()) {
        attemptReconnect(retry + 1);
      }
    }));

  }
}
