package io.github.ashleytaylor.graphql.lambda.example.schema.open;

import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.builder.Authorizer;
import com.fleetpin.graphql.builder.RestrictType;
import com.fleetpin.graphql.database.manager.Table;

import graphql.schema.DataFetchingEnvironment;
import io.github.ashleytaylor.graphql.lambda.example.ApiContext;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Board;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Role;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Ticket;
import io.github.ashleytaylor.graphql.lambda.example.schema.user.User;
import io.github.ashleytaylor.graphql.lambda.example.schema.user.UserMembership;

public class AuthorizerNone implements Authorizer {

	public boolean allow(DataFetchingEnvironment env) {
		ApiContext context = env.getContext();
		context.setOrganisationId(env.getArgument("organisationId"));
		context.setMembership(new SignupOnly());
		return true;
	}
	
	static public class SignupOnly extends UserMembership {

		@Override
		public CompletableFuture<Role> getRole(ApiContext context) {
			return CompletableFuture.completedFuture(new AuthorizerNone.SignupRole());
		}
	}
	
	public static class SignupRole extends Role{

		@Override
		public boolean isAdmin() {
			return false;
		}

		@Override
		public RestrictType<Board> boardAccess(ApiContext context) {
			return b -> CompletableFuture.completedFuture(false);
		}

		@Override
		public RestrictType<Ticket> ticketAccess(ApiContext context) {
			return b -> CompletableFuture.completedFuture(false);
		}

		@Override
		public boolean allowWrite(ApiContext context, Table entity) {
			if(entity instanceof User) {
				return entity.getId() == null; //only create a new user, can't be used to edit an existing user
			}
			return false;
		}

	}

}