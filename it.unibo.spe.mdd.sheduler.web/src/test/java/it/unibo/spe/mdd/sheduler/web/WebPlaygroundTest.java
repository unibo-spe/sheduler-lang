package it.unibo.spe.mdd.sheduler.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Regression test for the web playground: the generated index.html hardcodes webjar and Xtext versions,
 * which silently break (404) when dependencies are bumped without regenerating/aligning them.
 */
public class WebPlaygroundTest {

	// quoted asset paths in index.html (versioned webjars/xtext paths, generated resources); skips requirejs module ids
	private static final Pattern ASSET = Pattern.compile("\"((?:webjars/|xtext/\\d|xtext-resources/)[^\"]+)\"");

	private static Server server;
	private static String baseUrl;
	private static final HttpClient HTTP = HttpClient.newHttpClient();

	@BeforeAll
	static void startServer() throws Exception {
		server = ServerLauncher.createServer(new InetSocketAddress("localhost", 0));
		server.start();
		baseUrl = server.getURI().toString();
	}

	@AfterAll
	static void stopServer() throws Exception {
		server.stop();
	}

	private static HttpResponse<String> get(String path) throws Exception {
		return HTTP.send(HttpRequest.newBuilder(URI.create(baseUrl + path)).build(),
				HttpResponse.BodyHandlers.ofString());
	}

	@Test
	void allAssetsReferencedByIndexAreServed() throws Exception {
		HttpResponse<String> index = get("index.html");
		assertEquals(200, index.statusCode());
		List<String> assets = new ArrayList<>();
		Matcher m = ASSET.matcher(index.body());
		while (m.find()) {
			String path = m.group(1);
			assets.add(path.matches(".*\\.(js|css)$") ? path : path + ".js"); // requirejs module ids omit .js
		}
		assertFalse(assets.isEmpty(), "no assets found in index.html");
		for (String asset : assets) {
			assertEquals(200, get(asset).statusCode(), "asset not served: " + asset);
		}
	}

	@Test
	void contentAssistReturnsProposals() throws Exception {
		HttpResponse<String> response = HTTP.send(
				HttpRequest.newBuilder(URI.create(baseUrl + "xtext-service/assist?resource=test.shed"))
						.header("Content-Type", "application/x-www-form-urlencoded")
						.POST(HttpRequest.BodyPublishers.ofString("fullText=&caretOffset=0"))
						.build(),
				HttpResponse.BodyHandlers.ofString());
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"proposal\":\"pool\""), response.body());
	}
}
