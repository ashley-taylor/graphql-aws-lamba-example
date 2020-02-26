package io.github.ashleytaylor.graphql.lambda.example.schema.board;

import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fleetpin.graphql.builder.RestrictType;
import com.fleetpin.graphql.builder.annotations.Entity;
import com.fleetpin.graphql.database.manager.Table;

import io.github.ashleytaylor.graphql.lambda.example.ApiContext;

@Entity
public class AdminRole extends Role{

	public static final String ID = "admin";

	@Override
	@JsonIgnore
	public boolean isAdmin() {
		return true;
	}

	@Override
	public RestrictType<Board> boardAccess(ApiContext context) {
		return ticket -> CompletableFuture.completedFuture(true);
	}

	@Override
	public RestrictType<Ticket> ticketAccess(ApiContext context) {
		return ticket -> CompletableFuture.completedFuture(true);

	}

	@Override
	public boolean allowWrite(ApiContext context, Table entity) {
		return true;
	}

}
