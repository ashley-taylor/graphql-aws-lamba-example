package io.github.ashleytaylor.graphql.lambda.example.schema.board;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fleetpin.graphql.builder.RestrictType;
import com.fleetpin.graphql.builder.annotations.Entity;
import com.fleetpin.graphql.builder.annotations.Id;
import com.fleetpin.graphql.builder.annotations.Query;
import com.fleetpin.graphql.database.manager.Table;
import com.fleetpin.graphql.database.manager.annotations.TableName;

import io.github.ashleytaylor.graphql.lambda.example.ApiContext;
import io.github.ashleytaylor.graphql.lambda.example.directives.AdminOnly;

@Entity
@TableName("roles") //import if inheritance to set table name
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ 
	@Type(value = AdminRole.class, name = "admin"),
	@Type(value = UserRole.class, name = "user"),
})
public abstract class Role extends Table {

	public abstract boolean isAdmin();
	
	
	@Query
	@AdminOnly
	public static CompletableFuture<List<Role>> roles(ApiContext context, @Id String organisationId) {
		return context.getDatabase().query(Role.class);
	}


	public abstract RestrictType<Board> boardAccess(ApiContext context);


	public abstract RestrictType<Ticket> ticketAccess(ApiContext context);


	public abstract boolean allowWrite(ApiContext context, Table entity);


	
	//TODO: role manipulation
	
}
