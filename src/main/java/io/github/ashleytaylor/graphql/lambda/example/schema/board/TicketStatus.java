package io.github.ashleytaylor.graphql.lambda.example.schema.board;

import com.fleetpin.graphql.builder.annotations.Entity;

@Entity
public enum TicketStatus {

	DONE, INPROGRESS, TODO
}
