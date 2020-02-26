package io.github.ashleytaylor.graphql.lambda.example.schema.user;

import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.builder.annotations.Entity;
import com.fleetpin.graphql.builder.annotations.Id;
import com.fleetpin.graphql.database.manager.Table;
import com.fleetpin.graphql.database.manager.annotations.GlobalIndex;

import io.github.ashleytaylor.graphql.lambda.example.ApiContext;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Organisation;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Role;

@Entity
public class UserMembership extends Table{

	private String organisationId;
	
	@Override
	@Id
	@GlobalIndex //so a user can find all memberships
	public String getId() {
		return super.getId();
	}
	
	public void setOrganisationId(String organisationId) {
		this.organisationId = organisationId;
	}
	
	public CompletableFuture<Organisation> getOrganisation(ApiContext context) {
		context.setOrganisationId(organisationId);
		return context.getDatabase().get(Organisation.class, organisationId); //can't use linking as need to set boardId
	}
	
	public CompletableFuture<User> getUser(ApiContext context) {
		return context.getDatabase().getLink(this, User.class);
	}

	public CompletableFuture<Role> getRole(ApiContext context) {
		return context.getDatabase().getLink(this, Role.class);
	}
}
