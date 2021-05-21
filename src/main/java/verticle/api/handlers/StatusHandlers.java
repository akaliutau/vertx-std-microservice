package verticle.api.handlers;

import io.vertx.reactivex.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StatusHandlers {

	public static void sendStatusCode(RoutingContext ctx, int code) {
		ctx.response().setStatusCode(code).end();
	}

	public static void sendBadGateway(RoutingContext ctx, Throwable err) {
		log.error("Error: {}", err.getMessage());
		ctx.fail(502);
	}

	public static void fail500(RoutingContext ctx, Throwable err) {
		log.error("Error {}", err.getMessage());
		ctx.fail(500);
	}

}
