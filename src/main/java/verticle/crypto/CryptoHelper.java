package verticle.crypto;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CryptoHelper {

	public static String publicKey() throws IOException {
		return read("public_key.pem");
	}

	public static String privateKey() throws IOException {
		return read("private_key.pem");
	}

	private static String read(String file) throws IOException {
		Path path = Paths.get(".secrets", file);
		if (!path.toFile().exists()) {
			path = Paths.get("..", ".secrets", file);
		}
		return String.join("\n", Files.readAllLines(path, StandardCharsets.UTF_8));
	}
}
