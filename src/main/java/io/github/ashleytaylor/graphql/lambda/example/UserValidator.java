package io.github.ashleytaylor.graphql.lambda.example;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.aws.lambda.CognitoValidator;
import com.fleetpin.graphql.database.manager.dynamo.DynamoDbManager;

import io.github.ashleytaylor.graphql.lambda.example.schema.user.User;
import io.github.ashleytaylor.graphql.lambda.example.schema.user.UserGroup;

public class UserValidator {

	private final DynamoDbManager dynamoDb;
	private final CognitoValidator validator;

	public UserValidator(DynamoDbManager dynamoDb, CognitoValidator validator) {
		this.dynamoDb = dynamoDb;
		this.validator = validator;
	}
	
	
	public CompletableFuture<User> validate(String authHeader) {
		try {
			if (authHeader != null && authHeader.startsWith("Bearer ")) {
				var token = authHeader.substring(7);
				var decoded = validator.verify(token);
				String cognitoUserName = decoded.get("username").asText();
				
				Set<UserGroup> groups = new HashSet<>();
				var groupsNode = decoded.get("cognito:groups");
				if(groupsNode != null) {
					for(var group: groupsNode) {
						groups.add(UserGroup.valueOf(group.asText().toUpperCase()));
					}
				}
				var db = dynamoDb.getDatabase("none");
				//each entry can add two additional indexes beside id lookup.
				//on is scoped to the organisation or in this case the board
				//the other is globally scoped. Because users can belong to multiple boards we use the global index
				var toReturn = db.queryGlobal(User.class, cognitoUserName).thenApply(users -> {
					return buildUser(cognitoUserName, users.stream().findAny().orElse(null), groups);
				});
				db.start(toReturn);
				return toReturn;
						
			}
		}catch (Exception e) {
			return CompletableFuture.failedFuture(new RuntimeException("Bad token"));
		}
		return CompletableFuture.completedFuture(null);
	}

	public static User buildUser(String cognitoUserName, User user, Set<UserGroup> groups) {
		if(user == null && groups.contains(UserGroup.DEVELOPERS)) {
			//special case where does not need to exist in the database to get a user object;
			user = new User();
			user.setEmail("unknown");
			user.setFirstName("automatic");
			user.setLastName("developer");
			user.setCognitoId(cognitoUserName);
			user.setId(cognitoUserName);
			user.setGroups(groups);
			return user;
		}
		user.setGroups(groups);
		return user;
	}

}
