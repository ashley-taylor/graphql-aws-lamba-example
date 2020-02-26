package io.github.ashleytaylor.graphql.lambda.example;

import java.util.concurrent.CompletionStage;

import com.fleetpin.graphql.aws.lambda.ContextGraphQL;
import com.fleetpin.graphql.builder.annotations.Context;
import com.fleetpin.graphql.database.manager.Database;

import io.github.ashleytaylor.graphql.lambda.example.schema.user.User;
import io.github.ashleytaylor.graphql.lambda.example.schema.user.UserMembership;

@Context
public class ApiContext implements ContextGraphQL {

	private final User user;
	private final Database database;

	private String organisationId;
	private UserMembership membership;

	public ApiContext(String organisationId, User user, Database database) {
		this.organisationId = organisationId;
		this.user = user;
		this.database = database;
	}

	public String getOrganisationId() {
		return organisationId;
	}

	public Database getDatabase() {
		return database;
	}

	public User getUser() {
		return user;
	}

	@Override
	public void start(CompletionStage<?> complete) {
		database.start(complete.toCompletableFuture());
	}

	public void setOrganisationId(String organisationId) {
		this.organisationId = organisationId;
		database.setOrganisationId(organisationId);
	}

	public void setMembership(UserMembership membership) {
		this.membership = membership;
	}

	public UserMembership getMembership() {
		return membership;
	}

}
