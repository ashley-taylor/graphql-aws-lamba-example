package io.github.ashleytaylor.graphql.lambda.example.schema.board;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.aws.lambda.util.InputMapper;
import com.fleetpin.graphql.builder.annotations.Entity;
import com.fleetpin.graphql.builder.annotations.Id;
import com.fleetpin.graphql.builder.annotations.Mutation;
import com.fleetpin.graphql.builder.annotations.Query;
import com.fleetpin.graphql.builder.annotations.SchemaOption;
import com.fleetpin.graphql.database.manager.Table;

import io.github.ashleytaylor.graphql.lambda.example.ApiContext;
import io.github.ashleytaylor.graphql.lambda.example.directives.AdminOnly;
import io.github.ashleytaylor.graphql.lambda.example.schema.user.UserMembership;

@Entity
public class Organisation extends Table {

	private String name;
	private ZoneId timezone;
	
	
	public String getName() {
		return name;
	}
	public ZoneId getTimezone() {
		return timezone;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setTimezone(ZoneId timezone) {
		this.timezone = timezone;
	}
	
	public CompletableFuture<List<Board>> getBoards(ApiContext context) {
		return context.getDatabase().query(Board.class); //returns all tickets within the board as the database is scoped to only read from just this board
	}
	
	
	@Query
	public static CompletableFuture<Optional<Organisation>> organisation(ApiContext context, @Id String organisationId) {
		return context.getDatabase().getOptional(Organisation.class, organisationId);
	}
	
	@Query
	@AdminOnly
	//made this a query instead of link of the board as is very sensitive data so make it explicitly requested
	public static CompletableFuture<List<UserMembership>> members(ApiContext context, @Id String organisationId) {
		return context.getDatabase().query(UserMembership.class);
	}
	
	@Mutation
	@AdminOnly
	public static CompletableFuture<Organisation> updateOrganisation(ApiContext context, @Id String organisationId, OrganisationInput organisationInput) {

		return context.getDatabase().get(Organisation.class, organisationId).thenCompose(organisation -> {
			//will map fields that share the same name
			//if a field is of type Optional and the optional itself is null does not replace the existing value as graph query never specified the field
			//if a field is of type Optional and its empty will null the field
			InputMapper.assign(organisation, organisationInput);
			return context.getDatabase().put(organisation);
		});
	}
	
	@Entity(SchemaOption.INPUT)
	static class OrganisationInput {
		Optional<String> name;
		Optional<ZoneId> timezone;
		
		public void setName(Optional<String> name) {
			this.name = name;
		}
		
		public void setTimezone(Optional<ZoneId> timezone) {
			this.timezone = timezone;
		}
	}
	
	
}
