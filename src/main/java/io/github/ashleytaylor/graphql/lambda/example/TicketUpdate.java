package io.github.ashleytaylor.graphql.lambda.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fleetpin.graphql.aws.lambda.ContextGraphQL;
import com.fleetpin.graphql.aws.lambda.LambdaCache;
import com.fleetpin.graphql.aws.lambda.LambdaSubscriptionSource;
import com.fleetpin.graphql.builder.SchemaBuilder;
import com.fleetpin.graphql.database.manager.dynamo.DynamoDbManager;

import graphql.GraphQL;
import io.github.ashleytaylor.graphql.lambda.example.schema.board.Ticket;
import io.github.ashleytaylor.graphql.lambda.example.schema.user.User;
import io.reactivex.rxjava3.core.Flowable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class TicketUpdate extends LambdaSubscriptionSource<DynamodbEvent, TicketUpdate.WrapperTicket> {

	private DynamoDbManager manager;

	public TicketUpdate() throws Exception {
		super("ticketUpdates", System.getenv("GRAPH_SUBSCRIPTIONS"), System.getenv("API"), Duration.ofSeconds(30), Duration.ZERO);
	}

	@Override
	public Void handleRequest(DynamodbEvent input, Context context) {
		try {
			for(var record: input.getRecords()) {
				if(!record.getDynamodb().getKeys().get("id").getS().startsWith("tickets")) {
					continue;
				}
				var image = record.getDynamodb().getNewImage();
				if(image == null) {
					continue; //TODO handle delete?
				}
				var node = toJsonNode(image);
				try {
					var ticket = SchemaBuilder.MAPPER.treeToValue(node.get("item"), Ticket.class);
					String organisationId = record.getDynamodb().getNewImage().get("organisationId").getS();
					process(new WrapperTicket(organisationId, ticket)).get();
				} catch (InterruptedException | ExecutionException | IOException e) {
					throw new RuntimeException(e);
				}
			}
			return null;
		}finally {
			LambdaCache.evict();
		}
	}

	private ObjectNode toJsonNode(Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> image) {
		var object = SchemaBuilder.MAPPER.createObjectNode();
		image.forEach((key, value) -> object.set(key, toJsonNode(value)));
		return object;
	}
	private JsonNode toJsonNode(com.amazonaws.services.dynamodbv2.model.AttributeValue value) {

		if(value.getBOOL() != null) {
			return BooleanNode.valueOf(value.getBOOL());
		}
		if(value.getNULL() != null && value.getNULL()) {
			return NullNode.instance;
		}
		if(value.getB() != null) {
			return BinaryNode.valueOf(toArray(value.getB()));
		}
		if(value.getN() != null) {
			double v = Double.parseDouble(value.getN());
			if(Math.floor(v) == v) {
				return LongNode.valueOf(Long.parseLong(value.getN()));
			}
			return DoubleNode.valueOf(v);
		}
		if(value.getS() != null) {
			return TextNode.valueOf(value.getS());
		}

		if(value.getBS() != null) {
			ArrayNode arrayNode = SchemaBuilder.MAPPER.createArrayNode();
			for(var b: value.getBS()) {
				arrayNode.add(BinaryNode.valueOf(toArray(b)));
			}
			return arrayNode;
		}
		if(value.getL() != null) {
			ArrayNode arrayNode = SchemaBuilder.MAPPER.createArrayNode();
			for(com.amazonaws.services.dynamodbv2.model.AttributeValue l: value.getL()) {
				arrayNode.add(toJsonNode(l));	 
			}
			return arrayNode;
		}

		if(value.getNS() != null) {
			ArrayNode arrayNode = SchemaBuilder.MAPPER.createArrayNode();
			for(String s: value.getNS()) {
				arrayNode.add(TextNode.valueOf(s));
			}
			return arrayNode;
		}
		if(value.getSS() != null) {
			ArrayNode arrayNode = SchemaBuilder.MAPPER.createArrayNode();
			for(String s: value.getSS()) {
				arrayNode.add(TextNode.valueOf(s));
			}
			return arrayNode;
		}
		if(value.getM() != null) {
			ObjectNode objNode = SchemaBuilder.MAPPER.createObjectNode();
			if(value.getM().isEmpty()) {
				return NullNode.instance;
			}
			value.getM().forEach((key, v) -> {
				objNode.set(key, toJsonNode(v));
			});
			return objNode;
		}
		throw new RuntimeException("Unsupported type " + value);


	}

	private byte[] toArray(ByteBuffer b) {
		byte[] toreturn = new byte[b.remaining()];
		b.get(toreturn);
		return toreturn;

	}

	@Override
	protected void prepare() throws Exception {
		this.manager = DynamoDbManager.builder().tables(System.getenv("ENTITY_DATABASES").split(",")).build();
	}

	@Override
	protected GraphQL buildGraphQL() throws Exception {
		return SchemaBuilder.build("io.github.ashleytaylor.graphql.lambda.example.schema").build();
	}


	@Override
	protected DynamoDbManager builderManager() {
		return manager;
	}

	@Override
	public CompletableFuture<ContextGraphQL> buildContext(Flowable<WrapperTicket> publisher, String userId, AttributeValue additionalUserInfo, Map<String, Object> variables) {
		System.out.println("context");
		String organisationId;
		Object organisationIdObj = variables.get("organisationId").toString();
		if(organisationIdObj != null) {
			organisationId = organisationIdObj.toString();
		}else {
			organisationId = null;
		}
		Flowable<Ticket> tickets = Flowable.fromPublisher(publisher).map(w -> w.ticket);
		var database = manager.getDatabase(organisationId);
		CompletableFuture<ContextGraphQL> toReturn =  database.get(User.class, userId).thenApply(user -> new SubscriptionApiContext(organisationId,  user, manager.getDatabase(organisationId), tickets));
		database.start(toReturn);
		return toReturn;
	}

	@Override
	public String buildSubscriptionId(WrapperTicket type) {
		return type.organiastionId;
	}
	
	static class WrapperTicket {
		private final String organiastionId;
		private final Ticket ticket;
		WrapperTicket(String organiastionId, Ticket ticket) {
			this.organiastionId = organiastionId;
			this.ticket = ticket;
		}
		
		
	}

}
