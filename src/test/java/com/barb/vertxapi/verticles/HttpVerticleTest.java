package com.barb.vertxapi.verticles;

import java.io.IOException;
import java.net.ServerSocket;

import com.barb.vertxapi.utils.Consts;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class HttpVerticleTest {

  public static final int ONE_SECOND = 1000;
  private Vertx vertx;
  private Integer port;

  @Before
  public void setup(TestContext context) throws IOException {
    vertx = Vertx.vertx();
    port = getRandomPort();
    final DeploymentOptions options = new DeploymentOptions().setConfig(new JsonObject().put(Consts.APPLICATION_PORT, port));
    vertx.deployVerticle(HttpVerticle.class.getName(), options, context.asyncAssertSuccess());
  }

  private int getRandomPort() throws IOException {
    ServerSocket socket = new ServerSocket(0);
    final int localPort = socket.getLocalPort();
    socket.close();
    return localPort;
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void testMyApplication(TestContext context) {

    final Async async = context.async();

    WebClient.create(vertx)
        .get(port, "localhost", "/")
        .rxSend()
        .subscribe(
            response -> {
              context.assertTrue(response.bodyAsString().contains("Hello"));
              async.complete();
            });
    async.awaitSuccess(ONE_SECOND);
  }

  //  @Test
  //  public void testAddWhiskyShouldWork(final TestContext context) {
  //    final Async async = context.async();
  //    WebClient client = WebClient.create(vertx);
  //    final Whisky body = new Whisky("Barbaros", "Kusadasi, Turkey");
  //    client
  //        .post(port, "localhost", "/api/whiskies")
  //        .putHeader("content-type", "application/json")
  //        .sendJson(body, r -> {
  //          context.assertEquals(r.result().statusCode(), 201);
  //          context.assertTrue(r.result().headers().get("content-type").contains("application/json"));
  //
  //          final Whisky whisky = Json.decodeValue(r.result().bodyAsString(), Whisky.class);
  //          context.assertEquals(whisky.getName(), "Barbaros");
  //          context.assertEquals(whisky.getOrigin(), "Kusadasi, Turkey");
  //          async.complete();
  //        });
  //  }
}
