package io.github.ashleytaylor.graphql.lambda.example.schema.user;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.builder.Authorizer;
import com.fleetpin.graphql.builder.RestrictType;
import com.fleetpin.graphql.database.manager.Table;

import graphql.schema.DataFetchingEnvironment;
import io.github.ashleytaylor.graphql.lambda.example.ApiContext;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Board;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Role;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Ticket;

public class AuthorizerUser implements Authorizer {

	public boolean allow(DataFetchingEnvironment env) {
		ApiContext context = env.getContext();
		context.setOrganisationId(env.getArgument("organisationId"));
		context.setMembership(new UserPermissions());
		return context.getUser() != null;
	}
	
	
	static public class UserPermissions extends UserMembership {

		@Override
		public CompletableFuture<Role> getRole(ApiContext context) {
			return CompletableFuture.completedFuture(new AuthorizerUser.EditUserRole());
		}
	}
	
	public static class EditUserRole extends Role{

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
				return Objects.equals(entity.getId(), context.getUser().getId()); //only allowed to edit yourself or create a new organisation
			}
			return true; //only other write is the organisation creation
		}

	}

}