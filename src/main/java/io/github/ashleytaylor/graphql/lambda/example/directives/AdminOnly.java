package io.github.ashleytaylor.graphql.lambda.example.directives;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.fleetpin.graphql.builder.DirectiveCaller;
import com.fleetpin.graphql.builder.annotations.Directive;
import com.google.common.base.Throwables;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.github.ashleytaylor.graphql.lambda.example.ApiContext;

@Retention(RUNTIME)
@Directive(AdminOnly.Directive.class)
public @interface AdminOnly {

	public static class Directive implements DirectiveCaller<AdminOnly>{

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Object process(AdminOnly annotation, DataFetchingEnvironment env, DataFetcher<?> fetcher) throws Exception {
			ApiContext context = env.getContext();
			return context.getMembership().getRole(context).thenCompose(role -> {
				if(role.isAdmin()) {
					try {
						Object toReturn = fetcher.get(env);
						if(toReturn instanceof CompletableFuture) {
							return (CompletionStage) toReturn;
						}else {
							return CompletableFuture.completedFuture(toReturn);
						}
					} catch (Exception e) {
						Throwables.throwIfUnchecked(e);
						throw new RuntimeException(e);
					}
				}else {

					throw new RuntimeException("Only the Owner may call this endpoint");
				}
			});
		}

	}
}
