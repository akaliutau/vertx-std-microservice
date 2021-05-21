package verticle.api.handlers;

import static verticle.api.handlers.StatusHandlers.fail500;

import java.util.NoSuchElementException;

import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorHandlers {

	public static void handleAuthError(RoutingContext ctx, Throwable err) {
		log.error("Authentication error", err);
		ctx.fail(401);
	}

	public static void handleAuthenticationError(RoutingContext ctx, Throwable err) {
		log.error("Authentication problem {}", err.getMessage());
		ctx.response().setStatusCode(401).end();
	}

	public static void handleFetchError(RoutingContext ctx, Throwable err) {
		if (err instanceof NoSuchElementException) {
			ctx.fail(404);
		} else {
			fail500(ctx, err);
		}
	}

	public static void handleRegistrationError(RoutingContext ctx, Throwable err) {
		if (isIndexViolated(err)) {
			log.error("Registration failure: {}", err.getMessage());
			ctx.fail(409);
		} else {
			fail500(ctx, err);
		}
	}

	public static boolean isIndexViolated(Throwable err) {
		return err.getMessage().contains("E11000");
	}

}
