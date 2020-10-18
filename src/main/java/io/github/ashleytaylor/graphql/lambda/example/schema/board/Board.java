package io.github.ashleytaylor.graphql.lambda.example.schema.board;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.aws.lambda.util.InputMapper;
import com.fleetpin.graphql.builder.annotations.Entity;
import com.fleetpin.graphql.builder.annotations.Id;
import com.fleetpin.graphql.builder.annotations.Mutation;
import com.fleetpin.graphql.builder.annotations.Query;
import com.fleetpin.graphql.builder.annotations.Restrict;
import com.fleetpin.graphql.builder.annotations.SchemaOption;
import com.fleetpin.graphql.database.manager.Table;
import com.google.common.base.Strings;

import io.github.ashleytaylor.graphql.lambda.example.ApiContext;
import io.github.ashleytaylor.graphql.lambda.example.directives.AdminOnly;
import io.github.ashleytaylor.graphql.lambda.example.permissions.BoardRestrict;

@Entity
@Restrict(BoardRestrict.class)
public class Board extends Table {

	private String name;
	
	
	public String getName() {
		//dynamo turns empty strings to null
		return Strings.nullToEmpty(name);
	}
	
	public CompletableFuture<List<Ticket>> getTickets(ApiContext context) {
		return context.getDatabase().getLinks(this, Ticket.class);
	}
	
	
	@Query
	public static CompletableFuture<Optional<Board>> board(ApiContext context, @Id String organisationId, @Id String boardId) {
		return context.getDatabase().getOptional(Board.class, boardId);
	}
	
	
	
	@Mutation
	@AdminOnly
	public static CompletableFuture<Board> putBoard(ApiContext context, @Id String organisationId, @Id Optional<String> boardId, BoardInput input) {
		CompletableFuture<Board> future;
		if(boardId.isEmpty()) {
			var board = new Board();
			board.setId(context.getDatabase().newId()); //the input validator will catch a null id and error on missing required field
			future = CompletableFuture.completedFuture(board);
		}else {
			future = context.getDatabase().get(Board.class, boardId.get());
		}
		
		return future.thenCompose(board -> {
			InputMapper.assign(board, input);
			return context.getDatabase().put(board);
		});
	}
	
	@Mutation
	@AdminOnly
	public static CompletableFuture<Boolean> deleteBoard(ApiContext context, @Id String organisationId, @Id String boardId) {
		return context.getDatabase().get(Board.class, boardId).thenCompose(board -> {
			if(board == null) {
				return CompletableFuture.completedFuture(false);
			}else {
				return context.getDatabase().delete(board, true).thenApply(__ -> true);
			}
		});
		
	}
	
	@Entity(SchemaOption.INPUT)
	static class BoardInput {
		Optional<String> name;
		
		public void setName(Optional<String> name) {
			this.name = name;
		}
		
	}
	
	
}
