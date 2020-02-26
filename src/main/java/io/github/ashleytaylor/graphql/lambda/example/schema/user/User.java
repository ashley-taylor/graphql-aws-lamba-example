package io.github.ashleytaylor.graphql.lambda.example.schema.user;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.builder.annotations.Entity;
import com.fleetpin.graphql.builder.annotations.Query;
import com.fleetpin.graphql.database.manager.Table;
import com.fleetpin.graphql.database.manager.annotations.GlobalIndex;

import io.github.ashleytaylor.graphql.lambda.example.ApiContext;

@Entity
public class User extends Table {



	private String email;
	private String firstName;
	private String lastName;
	private String cognitoId;
	private Set<UserGroup> groups;

	public String getEmail() {
		return email;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	@GlobalIndex
	public String getCognitoId() {
		return cognitoId;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setCognitoId(String cognitoId) {
		this.cognitoId = cognitoId;
	}
	

	public void setGroups(Set<UserGroup> groups) {
		this.groups = groups;
	}
	
	public Set<UserGroup> getGroups() {
		return groups;
	}

	public CompletableFuture<List<UserMembership>> getMemberships(ApiContext context) {
		return context.getDatabase().queryGlobal(UserMembership.class, getId()); //membership uses same Id as the user so we can do a global query since db is scoped by organisation
	}
	
	public CompletableFuture<UserMembership> getMembership(ApiContext context, String organisationId) {
		if(groups != null && groups.contains(UserGroup.DEVELOPERS)) { //example how staff could have full access without being a member of every organisation
			return CompletableFuture.completedFuture(new AdminOrganisationLink(this, organisationId));
		}
		return context.getDatabase().get(UserMembership.class, getId());
	}
	
	@Query
	public static User me(ApiContext context) {
		return context.getUser();
	}

	public static class AdminOrganisationLink extends UserMembership {

		private User user;

		public AdminOrganisationLink(User user, String organisationId) {
			this.user = user;
			setOrganisationId(organisationId);
		}
		@Override
		public CompletableFuture<User> getUser(ApiContext context) {
			return CompletableFuture.completedFuture(user);
		}
	}
	
	
}
