package verticle.config;

import io.vertx.core.json.JsonObject;

public class DBConfig {
	
	public static JsonObject mongoConfig() {
		return new JsonObject().put("host", "localhost").put("port", 27017).put("db_name", "profiles");
	}


}
