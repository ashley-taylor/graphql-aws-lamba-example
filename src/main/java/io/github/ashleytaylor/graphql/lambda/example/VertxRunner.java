package io.github.ashleytaylor.graphql.lambda.example;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent;
import com.google.common.net.HttpHeaders;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class VertxRunner {


	public static void main(String[] args) throws Exception {

		LambdaEndpoint lambda = new LambdaEndpoint();
		var vertx = Vertx.vertx();
		HttpServer server = vertx.createHttpServer();

		Router router = Router.router(vertx);


		router.route().handler(t -> {
			t.response().putHeader("Access-Control-Allow-Origin", "*");
			t.response().putHeader("Access-Control-Request-Method", "POST");
			t.response().putHeader("Access-Control-Allow-Headers", "Content-Type, authorization, authrequired");
			t.next();

		});

		router.route(HttpMethod.OPTIONS, "/graphql").handler(t -> {
			t.response().end();
		});
		router.errorHandler(-1, t -> {
			t.failure().printStackTrace();
			t.response().setStatusCode(512);
			t.response().end();
		});



		router.route().handler(BodyHandler.create());
		router.route("/graphql").handler(handler -> {

			APIGatewayV2ProxyRequestEvent input = new APIGatewayV2ProxyRequestEvent();
			input.setBody(handler.getBodyAsString());

			var auth = handler.request().getHeader(HttpHeaders.AUTHORIZATION);

			Map<String, String> headers = new HashMap<>();
			headers.put(HttpHeaders.AUTHORIZATION, auth);

			input.setHeaders(headers);
			//will block the vertx thread but this is only intended as a demo
			var response = lambda.handleRequest(input, null);
			handler.response().setStatusCode(response.getStatusCode());
			handler.response().end(response.getBody());


		});

		int port = Integer.parseInt(getenv("PORT", "3000"));
		server.requestHandler(router).listen(port);
		System.out.println("Started webserver on port " + port);

	}

	private static String getenv(String name, String def) {
		var toReturn = System.getenv(name);
		if(toReturn == null) {
			toReturn = System.getProperty(name, def);
		}
		return toReturn;
	}
}
