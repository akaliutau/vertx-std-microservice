package verticle.api;

import io.reactivex.Completable;
import io.vertx.ext.auth.mongo.MongoAuthenticationOptions;
import io.vertx.ext.auth.mongo.MongoAuthorizationOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.mongo.MongoAuthentication;
import io.vertx.reactivex.ext.auth.mongo.MongoUserUtil;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import lombok.extern.slf4j.Slf4j;
import verticle.config.DBConfig;
import verticle.config.WebConfig;
import verticle.security.Registrar;
import verticle.security.TokenVault;

import java.io.IOException;

@Slf4j
public class ProfileVerticle extends AbstractVerticle {

	private static final int HTTP_PORT = 4000;
	private static final String prefix = "/api/v1";

	private TokenVault tokenVault;
	private Registrar registrar;

	private MongoClient mongoClient;
	private MongoAuthentication authProvider;
	private MongoUserUtil userUtil;


	@Override
	public Completable rxStart() {

		try {
			tokenVault = new TokenVault(vertx);
		} catch (IOException e) {
			return Completable.error(e);
		}

		Router router = Router.router(vertx);
		mongoClient = MongoClient.createShared(vertx, DBConfig.mongoConfig());

		authProvider = MongoAuthentication.create(mongoClient, new MongoAuthenticationOptions());
		userUtil = MongoUserUtil.create(mongoClient, new MongoAuthenticationOptions(), new MongoAuthorizationOptions());
		registrar = new Registrar(userUtil, mongoClient, authProvider, tokenVault);

		router.route().handler(WebConfig.cors());

		BodyHandler bodyHandler = BodyHandler.create();
		router.post().handler(bodyHandler);
		router.put().handler(bodyHandler);


		// Auth
		router.post("/register").handler(registrar::validateRegistration).handler(registrar::register);

		router.post(prefix + "/token").handler(registrar::token);

		// Profile
		router.get(prefix + "/:username").handler(tokenVault.getJwtHandler()).handler(registrar::checkUser)
				.handler(this::fetchUser);

		return vertx.createHttpServer().requestHandler(router).rxListen(HTTP_PORT).ignoreElement();
	}

	private void fetchUser(RoutingContext ctx) {
		this.registrar.getUser(ctx, ctx.pathParam("username"));
	}

	public static void main(String[] args) {
		System.setProperty("vertx.log-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
		Vertx vertx = Vertx.vertx();
		vertx.rxDeployVerticle(new ProfileVerticle()).subscribe(
				ok -> log.info("HTTP server started on port {}", HTTP_PORT), err -> log.error("Error: {}", err));
	}
}
