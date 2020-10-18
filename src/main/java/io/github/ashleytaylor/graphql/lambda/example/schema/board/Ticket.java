package io.github.ashleytaylor.graphql.lambda.example.schema.board;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.reactivestreams.Publisher;

import com.fleetpin.graphql.aws.lambda.util.InputMapper;
import com.fleetpin.graphql.builder.annotations.Entity;
import com.fleetpin.graphql.builder.annotations.Id;
import com.fleetpin.graphql.builder.annotations.Mutation;
import com.fleetpin.graphql.builder.annotations.Query;
import com.fleetpin.graphql.builder.annotations.Restrict;
import com.fleetpin.graphql.builder.annotations.SchemaOption;
import com.fleetpin.graphql.builder.annotations.Subscription;
import com.fleetpin.graphql.database.manager.Table;
import com.google.common.base.Strings;

import io.github.ashleytaylor.graphql.lambda.example.ApiContext;
import io.github.ashleytaylor.graphql.lambda.example.SubscriptionApiContext;
import io.github.ashleytaylor.graphql.lambda.example.permissions.TicketRestrict;

@Entity
@Restrict(TicketRestrict.class)
public class Ticket extends Table {

	private String name;
	private Boolean visible;
	private String description;
	private TicketStatus status;

	public String getName() {
		//dynamo turns empty strings to null
		return Strings.nullToEmpty(name);
	}

	public Boolean isVisible() {
		return visible;
	}

	public String getDescription() {
		//dynamo turns empty strings to null
		return Strings.nullToEmpty(description);
	}

	public TicketStatus getStatus() {
		return status;
	}

	public CompletableFuture<Board> getBoard(ApiContext context) {
		return context.getDatabase().getLink(this, Board.class);
	}

	@Query
	public static CompletableFuture<Optional<Ticket>> ticket(ApiContext context, @Id String organisationId, @Id String ticketId) {
		return context.getDatabase().getOptional(Ticket.class, ticketId);
	}

	@Mutation
	public static CompletableFuture<Ticket> putTicket(ApiContext context, @Id String organisationId, @Id String boardId, @Id Optional<String> ticketId, TicketInput input) {

		CompletableFuture<Ticket> future;
		if(ticketId.isEmpty()) {
			var ticket = new Ticket();
			ticket.setId(context.getDatabase().newId()); //the input validator will catch a null id and error on missing required field
			future = CompletableFuture.completedFuture(ticket);
		}else {
			future = context.getDatabase().get(Ticket.class, ticketId.get());
		}

		return future.thenCompose(ticket -> {
			InputMapper.assign(ticket, input);
			return context.getDatabase()
					.link(ticket, Board.class, boardId)
					.thenCompose(context.getDatabase()::put);
		});
	}

	@Mutation
	public static CompletableFuture<Ticket> deleteTicket(ApiContext context, @Id String organisationId, @Id String ticketId) {
		return context.getDatabase().get(Ticket.class, ticketId).thenCompose(ticket -> {
			//not checking exists graph response can't resolve through links as already deleted but is just an example
			return context.getDatabase().delete(ticket, true);
		});
	}
	
	
	@Subscription
	public static Publisher<Ticket> ticketUpdates(SubscriptionApiContext context, @Id String organisationId) {
		return context.getPublisher();
	}
	
	@Entity(SchemaOption.INPUT)
	@SuppressWarnings("unused")
	public static class TicketInput {
		private Optional<String> name;
		private Optional<Boolean> visible;
		private Optional<String> description;
		private Optional<TicketStatus> status;

		public void setName(Optional<String> name) {
			this.name = name;
		}

		public void setVisible(Optional<Boolean> visible) {
			this.visible = visible;
		}

		public void setDescription(Optional<String> description) {
			this.description = description;
		}

		public void setStatus(Optional<TicketStatus> status) {
			this.status = status;
		}


	}

}
