package io.github.ashleytaylor.graphql.lambda.example.permissions;

import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.builder.RestrictType;
import com.fleetpin.graphql.builder.RestrictTypeFactory;

import graphql.schema.DataFetchingEnvironment;
import io.github.ashleytaylor.graphql.lambda.example.ApiContext;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Board;

public class BoardRestrict implements RestrictTypeFactory<Board> {

	@Override
	public CompletableFuture<RestrictType<Board>> create(DataFetchingEnvironment env) {
		ApiContext context = env.getContext();
		if (context.getMembership() == null) {
			return CompletableFuture.completedFuture(asset -> CompletableFuture.completedFuture(false));
		} else {
			return context.getMembership().getRole(context).thenApply(role -> {
				return role.boardAccess(context);
			});

		}
	}

}
