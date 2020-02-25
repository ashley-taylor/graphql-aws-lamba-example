package io.github.ashleytaylor.graphql.lambda.example.schema.board;

import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.builder.Authorizer;

import graphql.schema.DataFetchingEnvironment;
import io.github.ashleytaylor.graphql.lambda.example.ApiContext;

public class AuthorizerOrganisation implements Authorizer {

	public CompletableFuture<Boolean> allow(DataFetchingEnvironment env) {
		ApiContext context = env.getContext();
		context.setOrganisationId(env.getArgument("organisationId"));
		if (context.getUser() == null) {
			return CompletableFuture.completedFuture(false);
		}
		return context.getUser().getMembership(context, context.getOrganisationId()).thenApply(membership -> {
			if (membership == null) {
				return false;
			}
			context.setMembership(membership);
			return true;
		});
	}

}