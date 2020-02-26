package io.github.ashleytaylor.graphql.lambda.example;

import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.aws.lambda.GraphQLQuery;
import com.fleetpin.graphql.aws.lambda.LambdaGraphQL;
import com.fleetpin.graphql.builder.SchemaBuilder;
import com.fleetpin.graphql.database.manager.dynamo.DynamoDbManager;

import graphql.GraphQL;
import io.github.ashleytaylor.graphql.lambda.example.permissions.DatabasePermissions;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.AdminRole;
import io.github.ashleytaylor.graphql.lambda.example.schema.user.User;

public class LambdaEndpoint extends LambdaGraphQL<User, ApiContext>{

	private DynamoDbManager manger;

	public LambdaEndpoint() throws Exception {
		this.manger = DynamoDbManager.builder().tables(System.getenv("ENTITY_DATABASES").split(",")).build();
		
		//TODO: this should be in a separate endpoint called after a release not every time a lambda starts
		
		var db = manger.getDatabase("ignore");
		
		AdminRole admin = new AdminRole();
		admin.setId("admin");
		
		//adding an entry to the global area will be visible to everyone
		//if they edit a global object will take a local copy as part of their account
		db.putGlobal(admin).get();
	}
	
	@Override
	protected GraphQL buildGraphQL() throws Exception {
		return SchemaBuilder.build("io.github.ashleytaylor.graphql.lambda.example.schema").build();
	}

	@Override
	protected CompletableFuture<User> validate(String authHeader) {
		//TODO: no actual security normally would call something like cognito
		var db =manger.getDatabase("none");
		var toReturn = db.get(User.class, authHeader); //will just fetch from global scope;
		db.start(toReturn);
		return toReturn;
	}

	@Override
	protected ApiContext buildContext(User user, GraphQLQuery query) {
		String organisationId = null;
		Object organisationIdObj = query.getVariables().get("organisationId");
		if(organisationIdObj != null) {
			organisationId = organisationIdObj.toString();
		}
		DatabasePermissions writePermissions = new DatabasePermissions();
		var context = new ApiContext(organisationId, user, manger.getDatabase(organisationId, writePermissions));
		writePermissions.setContext(context);
		return context;
	}

}
