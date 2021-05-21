package verticle.utils;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.RoutingContext;

public class JsonUtils {
	
	/**
	 * Utility methods
	 * 
	 * @param ctx
	 * @return
	 */
	public static JsonObject getJsonFromContext(RoutingContext ctx) {
		if (ctx.getBody().length() == 0) {
			return new JsonObject();
		} else {
			return ctx.getBodyAsJson();
		}
	}

}
