package io.github.ashleytaylor.graphql.lambda.example.schema.user;

import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.builder.annotations.Mutation;

import io.github.ashleytaylor.graphql.lambda.example.ApiContext;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.AdminRole;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Organisation;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Role;

public class CreateOrganisation {
	//needs to be outside here as security role won't allow here
		@Mutation
		public static CompletableFuture<Organisation> createOrganisation(ApiContext context, String name, ZoneId timezone) {
			Organisation organisation = new Organisation();
			String id = context.getDatabase().newId();
			organisation.setId(id); //need to set id for db scoping
			context.setOrganisationId(id);
			organisation.setName(name);
			organisation.setTimezone(timezone);
			return context.getDatabase().put(organisation).thenCompose(org -> {
				
				UserMembership membership = new UserMembership();
				membership.setOrganisationId(org.getId());
				
				//use same id as user as is how a user finds all organisations related to
				membership.setId(context.getUser().getId());
				
				return context.getDatabase().put(membership) //write membership
					.thenCompose(__ -> context.getDatabase().link(membership, User.class, context.getUser().getId())) // link to user
					.thenCompose(__ -> context.getDatabase().link(membership, Role.class, AdminRole.ID))
					.thenApply(__ -> org); //we want to return the organisation after all db writes;
				
				
			});
		}
}
