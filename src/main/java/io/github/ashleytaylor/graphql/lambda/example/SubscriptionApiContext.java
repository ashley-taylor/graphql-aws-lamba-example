package io.github.ashleytaylor.graphql.lambda.example;

import org.reactivestreams.Publisher;

import com.fleetpin.graphql.builder.annotations.Context;
import com.fleetpin.graphql.database.manager.Database;

import io.github.ashleytaylor.graphql.lambda.example.schema.board.Ticket;
import io.github.ashleytaylor.graphql.lambda.example.schema.user.User;

@Context
public class SubscriptionApiContext extends ApiContext {

	private final Publisher<Ticket> publisher;
	
	public SubscriptionApiContext(String organisationId, User user, Database database, Publisher<Ticket> publisher) {
		super(organisationId, user, database);
		this.publisher = publisher;
	}

	public Publisher<Ticket> getPublisher() {
		return publisher;
	}

}
