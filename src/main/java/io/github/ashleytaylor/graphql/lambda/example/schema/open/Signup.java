package io.github.ashleytaylor.graphql.lambda.example.schema.open;

import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.builder.annotations.Entity;
import com.fleetpin.graphql.builder.annotations.Mutation;
import com.fleetpin.graphql.builder.annotations.SchemaOption;

import io.github.ashleytaylor.graphql.lambda.example.ApiContext;
import io.github.ashleytaylor.graphql.lambda.example.schema.user.User;

public class Signup {

	
	@Mutation
	public static CompletableFuture<User> createUser(ApiContext context, UserInput user) {
		User toSave = new User();
		toSave.setFirstName(user.firstName);
		toSave.setLastName(user.lastName);
		toSave.setEmail(user.email);
		
		return context.getDatabase().putGlobal(toSave);
	}
	
	
	@Entity(SchemaOption.INPUT)
	public static class UserInput {
		private String email;
		private String firstName;
		private String lastName;
		
		public void setEmail(String email) {
			this.email = email;
		}
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	}
	
}
