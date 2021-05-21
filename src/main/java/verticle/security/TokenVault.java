package verticle.security;

import java.io.IOException;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.auth.jwt.JWTAuth;
import io.vertx.reactivex.ext.web.handler.JWTAuthHandler;
import lombok.Getter;
import verticle.crypto.CryptoHelper;

public class TokenVault {

	private final JWTAuth jwtAuth;

	@Getter
	private final JWTAuthHandler jwtHandler;

	public TokenVault(Vertx vertx) throws IOException {

		String publicKey;
		String privateKey;
		publicKey = CryptoHelper.publicKey();
		privateKey = CryptoHelper.privateKey();

		jwtAuth = JWTAuth.create(vertx,
				new JWTAuthOptions().addPubSecKey(new PubSecKeyOptions().setAlgorithm("RS256").setBuffer(publicKey))
						.addPubSecKey(new PubSecKeyOptions().setAlgorithm("RS256").setBuffer(privateKey)));
		jwtHandler = JWTAuthHandler.create(jwtAuth);

	}

	public String makeJwtToken(String username, String deviceId) {
		JsonObject claims = new JsonObject().put("deviceId", deviceId);
		JWTOptions jwtOptions = new JWTOptions().setAlgorithm("RS256").setExpiresInMinutes(10_080) // 7 days
				.setIssuer("10k-steps-api").setSubject(username);
		return jwtAuth.generateToken(claims, jwtOptions);
	}

}
