package verticle.security;

import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.auth.mongo.MongoAuthentication;
import io.vertx.reactivex.ext.auth.mongo.MongoUserUtil;
import io.vertx.reactivex.ext.mongo.MongoClient;
import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import verticle.utils.JsonUtils;

import static verticle.api.handlers.ErrorHandlers.*;
import static verticle.api.handlers.StatusHandlers.sendStatusCode;
import static verticle.utils.ValidationUtils.anyRegistrationFieldIsMissing;
import static verticle.utils.ValidationUtils.anyRegistrationFieldIsWrong;

@Slf4j
public class Registrar {

	private final MongoUserUtil userUtil;
	private final MongoClient mongoClient;
	private final MongoAuthentication authProvider;
	private final TokenVault tokenVault;

	public Registrar(MongoUserUtil userUtil, MongoClient mongoClient, MongoAuthentication authProvider,
			TokenVault tokenVault) {
		this.userUtil = userUtil;
		this.mongoClient = mongoClient;
		this.authProvider = authProvider;
		this.tokenVault = tokenVault;
	}

	public void checkUser(RoutingContext ctx) {
		String subject = ctx.user().principal().getString("sub");
		if (!ctx.pathParam("username").equals(subject)) {
			sendStatusCode(ctx, 403);
		} else {
			ctx.next();
		}
	}

	public void validateRegistration(RoutingContext ctx) {
		JsonObject body = JsonUtils.getJsonFromContext(ctx);
		if (anyRegistrationFieldIsMissing(body) || anyRegistrationFieldIsWrong(body)) {
			ctx.fail(400);
		} else {
			ctx.next();
		}
	}

	public void register(RoutingContext ctx) {
		JsonObject body = JsonUtils.getJsonFromContext(ctx);
		if (anyRegistrationFieldIsMissing(body) || anyRegistrationFieldIsWrong(body)) {
			ctx.fail(400);
		} else {
			String username = body.getString("username");
			String password = body.getString("password");

			JsonObject extraInfo = new JsonObject().put("$set", new JsonObject().put("email", body.getString("email"))
					.put("city", body.getString("city")).put("deviceId", body.getString("deviceId")));

			userUtil.rxCreateUser(username, password).flatMapMaybe(docId -> insertExtraInfo(extraInfo, docId))
					.ignoreElement()
					.subscribe(() -> completeRegistration(ctx), err -> handleRegistrationError(ctx, err));

		}

	}

	public void token(RoutingContext ctx) {
		JsonObject payload = ctx.getBodyAsJson();
		String username = payload.getString("username");

		authProvider.rxAuthenticate(payload).subscribe(user -> fetchUser(ctx, username),
				err -> handleAuthenticationError(ctx, err));

	}

	private void fetchUser(RoutingContext ctx, String username) {

		JsonObject query = new JsonObject().put("username", username);

		JsonObject fields = new JsonObject().put("_id", 0).put("username", 1).put("email", 1).put("deviceId", 1)
				.put("city", 1);

		mongoClient.rxFindOne("user", query, fields).toSingle().subscribe(json -> completeFetchRequest(ctx, json),
				err -> handleFetchError(ctx, err));
	}

	private void completeFetchRequest(RoutingContext ctx, JsonObject json) {
		String username = json.getString("username");
		String deviceId = json.getString("deviceId");

		log.info("user found {}", json);
		sendToken(ctx, tokenVault.makeJwtToken(username, deviceId));
	}

	private void sendToken(RoutingContext ctx, String token) {
		ctx.response().putHeader("Content-Type", "application/jwt").end(token);
	}

	public void getUser(RoutingContext ctx, String username) {

		JsonObject query = new JsonObject().put("username", username);

		JsonObject fields = new JsonObject().put("_id", 0).put("username", 1).put("email", 1).put("deviceId", 1)
				.put("city", 1);

		mongoClient.rxFindOne("user", query, fields).toSingle().subscribe(
				json -> ctx.response().end(json.encode()),
				err -> handleFetchError(ctx, err));
	}

	
	private MaybeSource<? extends JsonObject> insertExtraInfo(JsonObject extraInfo, String docId) {
		JsonObject query = new JsonObject().put("_id", docId);
		return mongoClient.rxFindOneAndUpdate("user", query, extraInfo).onErrorResumeNext(err -> {
			return deleteIncompleteUser(query, err);
		});
	}

	private MaybeSource<? extends JsonObject> deleteIncompleteUser(JsonObject query, Throwable err) {
		if (isIndexViolated(err)) {
			return mongoClient.rxRemoveDocument("user", query).flatMap(del -> Maybe.error(err));
		} else {
			return Maybe.error(err);
		}
	}

	private void completeRegistration(RoutingContext ctx) {
		ctx.response().end();
	}

	

}
