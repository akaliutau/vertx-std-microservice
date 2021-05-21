package verticle.utils;

import java.util.regex.Pattern;

import io.vertx.core.json.JsonObject;

public class ValidationUtils {

	private final static Pattern validUsername = Pattern.compile("\\w[\\w+|-]*");
	private final static Pattern validDeviceId = Pattern.compile("\\w[\\w+|-]*");

	// Email regex from
	// https://www.owasp.org/index.php/OWASP_Validation_Regex_Repository
	private final static Pattern validEmail = Pattern
			.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");

	public static boolean anyRegistrationFieldIsMissing(JsonObject body) {
		return !(body.containsKey("username") && body.containsKey("password") && body.containsKey("email")
				&& body.containsKey("city") && body.containsKey("deviceId"));
	}

	public static boolean anyRegistrationFieldIsWrong(JsonObject body) {
		return !validUsername.matcher(body.getString("username")).matches()
				|| !validEmail.matcher(body.getString("email")).matches() || body.getString("password").trim().isEmpty()
				|| !validDeviceId.matcher(body.getString("deviceId")).matches();
	}

}
