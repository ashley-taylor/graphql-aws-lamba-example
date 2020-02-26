package io.github.ashleytaylor.graphql.lambda.example.schema.board;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fleetpin.graphql.builder.RestrictType;
import com.fleetpin.graphql.builder.annotations.Entity;
import com.fleetpin.graphql.database.manager.Table;

import io.github.ashleytaylor.graphql.lambda.example.ApiContext;
import io.github.ashleytaylor.graphql.lambda.example.schema.user.User;

@Entity
public class UserRole extends Role{

	
	private boolean write;
	
	@Override
	public boolean isAdmin() {
		return false;
	}
	
	public boolean isWrite() {
		return write;
	}
	
	
	public CompletableFuture<List<Board>> getWhitelistBoards(ApiContext context) {
		return context.getDatabase().getLinks(this, Board.class);
	}

	@Override
	public RestrictType<Board> boardAccess(ApiContext context) {
		
		var allowedBoards = context.getDatabase().getLinkIds(this, Board.class);
		return board -> {
			return CompletableFuture.completedFuture(allowedBoards.contains(board.getId()));
		};
	}

	@Override
	public RestrictType<Ticket> ticketAccess(ApiContext context) {
		var allowedBoards = context.getDatabase().getLinkIds(this, Board.class);
		
		return ticket -> {
			var boards = context.getDatabase().getLinkIds(ticket, Board.class);
			return CompletableFuture.completedFuture(allowedBoards.containsAll(boards));
		};
	}

	@Override
	public boolean allowWrite(ApiContext context, Table entity) {
		if(entity instanceof User) {
			return context.getUser().getId().equals(entity.getId()); //can only edit your own user
		}
		if(write) {
			var allowedBoards = context.getDatabase().getLinkIds(this, Board.class);
			if(entity instanceof Board) {
				return allowedBoards.contains(entity.getId());
			}else if(entity instanceof Ticket) {
				return allowedBoards.containsAll(context.getDatabase().getLinkIds(entity, Board.class));
			}
		}
		return false;
	}
	
	

}
