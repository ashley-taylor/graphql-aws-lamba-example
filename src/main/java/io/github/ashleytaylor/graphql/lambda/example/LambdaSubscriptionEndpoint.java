package io.github.ashleytaylor.graphql.lambda.example;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.aws.lambda.LambdaSubscriptionControl;
import com.fleetpin.graphql.builder.SchemaBuilder;
import com.fleetpin.graphql.database.manager.dynamo.DynamoDbManager;

import graphql.GraphQL.Builder;
import io.github.ashleytaylor.graphql.lambda.example.schema.user.User;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class LambdaSubscriptionEndpoint extends LambdaSubscriptionControl<User>{

	private DynamoDbManager manager;

	public LambdaSubscriptionEndpoint() throws Exception {
		super(System.getenv("GRAPH_SUBSCRIPTIONS"), System.getenv("API"));
	}

	@Override
	protected void prepare() throws Exception {
		
	}

	@Override
	protected Builder buildGraphQL() throws Exception {
		return SchemaBuilder.build("io.github.ashleytaylor.graphql.lambda.example.schema");

	}

	@Override
	protected DynamoDbManager builderManager() {
		this.manager = new DynamoDbManager.DyanmoDbManagerBuilder().tables(System.getenv("ENTITY_DATABASES")).build();
		return manager;
	}

	@Override
	public CompletableFuture<User> validateUser(String authHeader) {
		//TODO: no actual security normally would call something like cognito
		var db = manager.getDatabase("none");
		var toReturn = db.get(User.class, authHeader); //will just fetch from global scope;
		db.start(toReturn);
		return toReturn;
	}

	@Override
	public String extractUserId(User user) {
		return user.getId();
	}

	@Override
	public AttributeValue extraUserInfo(User user) {
		return null;
	}

	@Override
	public String buildSubscriptionId(String subscription, Map<String, Object> variables) {
		return variables.get("organisationId").toString();
	}

}
