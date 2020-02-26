package io.github.ashleytaylor.graphql.lambda.example.permissions;

import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.database.manager.Table;
import com.fleetpin.graphql.database.manager.access.ModificationPermission;

import io.github.ashleytaylor.graphql.lambda.example.ApiContext;

public class DatabasePermissions implements ModificationPermission {

	private ApiContext context;

	@Override
	public CompletableFuture<Boolean> apply(Table t) {
		return context.getMembership().getRole(context).thenApply(role -> {
			return role.allowWrite(context, t);
		});
	}

	public void setContext(ApiContext context) {
		this.context = context;
		
	}

}
