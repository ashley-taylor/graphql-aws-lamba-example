package io.github.ashleytaylor.graphql.lambda.example;

public class TicketUpdate {
//public class TicketUpdate  extends LambdaSubscriptionSource<DynamodbEvent, Ticket> {
//
//	private DynamoDbManager manager;
//
//	public TicketUpdate() throws Exception {
//		super("tickets", System.getenv("GRAPH_SUBSCRIPTIONS"), System.getenv("API"));
//	}
//
//	@Override
//	public Void handleRequest(DynamodbEvent input, Context context) {
//		for(var record: input.getRecords()) {
//			var image = record.getDynamodb().getNewImage();
//			var node = toJsonNode(image);
//			try {
//				var ticket = SchemaBuilder.MAPPER.treeToValue(node.get("item"), Ticket.class);
//				process(ticket).get();
//			} catch (InterruptedException | ExecutionException | IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
//		return null;
//	}
//
//	private ObjectNode toJsonNode(Map<String, com.amazonaws.services.dynamodbv2.model.AttributeValue> image) {
//		var object = SchemaBuilder.MAPPER.createObjectNode();
//		image.forEach((key, value) -> object.set(key, toJsonNode(value)));
//		return object;
//	}
//	private JsonNode toJsonNode(com.amazonaws.services.dynamodbv2.model.AttributeValue value) {
//
//		if(value.getBOOL() != null) {
//			return BooleanNode.valueOf(value.getBOOL());
//		}
//		if(value.getNULL() != null && value.getNULL()) {
//			return NullNode.instance;
//		}
//		if(value.getB() != null) {
//			return BinaryNode.valueOf(toArray(value.getB()));
//		}
//		if(value.getN() != null) {
//			double v = Double.parseDouble(value.getN());
//			if(Math.floor(v) == v) {
//				return LongNode.valueOf(Long.parseLong(value.getN()));
//			}
//			return DoubleNode.valueOf(v);
//		}
//		if(value.getS() != null) {
//			return TextNode.valueOf(value.getS());
//		}
//
//		if(value.getBS() != null) {
//			ArrayNode arrayNode = SchemaBuilder.MAPPER.createArrayNode();
//			for(var b: value.getBS()) {
//				arrayNode.add(BinaryNode.valueOf(toArray(b)));
//			}
//			return arrayNode;
//		}
//		if(value.getL() != null) {
//			ArrayNode arrayNode = SchemaBuilder.MAPPER.createArrayNode();
//			for(com.amazonaws.services.dynamodbv2.model.AttributeValue l: value.getL()) {
//				arrayNode.add(toJsonNode(l));	 
//			}
//			return arrayNode;
//		}
//
//		if(value.getNS() != null) {
//			ArrayNode arrayNode = SchemaBuilder.MAPPER.createArrayNode();
//			for(String s: value.getNS()) {
//				arrayNode.add(TextNode.valueOf(s));
//			}
//			return arrayNode;
//		}
//		if(value.getSS() != null) {
//			ArrayNode arrayNode = SchemaBuilder.MAPPER.createArrayNode();
//			for(String s: value.getSS()) {
//				arrayNode.add(TextNode.valueOf(s));
//			}
//			return arrayNode;
//		}
//		if(value.getM() != null) {
//			ObjectNode objNode = SchemaBuilder.MAPPER.createObjectNode();
//			if(value.getM().isEmpty()) {
//				return NullNode.instance;
//			}
//			value.getM().forEach((key, v) -> {
//				objNode.set(key, toJsonNode(v));
//			});
//			return objNode;
//		}
//		throw new RuntimeException("Unsupported type " + value);
//
//
//	}
//
//	private byte[] toArray(ByteBuffer b) {
//		byte[] toreturn = new byte[b.remaining()];
//		b.get(toreturn);
//		return toreturn;
//
//	}
//
//	@Override
//	protected void prepare() throws Exception {
//		this.manager = DynamoDbManager.builder().tables(System.getenv("ENTITY_DATABASES").split(",")).build();
//	}
//
//	@Override
//	protected GraphQL buildGraphQL() throws Exception {
//		return SchemaBuilder.build("io.github.ashleytaylor.graphql.lambda.example.schema").build();
//	}
//
//
//	@Override
//	protected DynamoDbManager builderManager() {
//		return manager;
//	}
//
//	@Override
//	public CompletableFuture<ContextGraphQL> buildContext(Flowable<Ticket> publisher, String userId, AttributeValue additionalUserInfo, Map<String, Object> variables) {
//		System.out.println("context");
//		String organisationId = null;
//		Object organisationIdObj = variables.get("organisationId");
//		if(organisationIdObj != null) {
//			organisationId = organisationIdObj.toString();
//		}
//		var context = new SubscribeApiContext(publisher, organisationId, manager.getDatabase(organisationId));
//		return CompletableFuture.completedFuture(context);
//	}
//
//	@Override
//	public String buildSubscriptionId(Ticket type) {
//		return manager.;
//	}

}
