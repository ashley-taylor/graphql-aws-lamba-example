# graphql-aws-lamba-example
An example Task board API deminstrating how to use the graphql-aws-lambda library

It currently does not include any user validation with something like cognito.
Authorization header just accepts the value as the user id.



##Deploying
* Run `mvn package` to create fat jar.
* Upload to an s3 bucket
* cf.json is a cloud formation script that can then be used to setup the environement
* can then look at the aws api gateway to find the urls to connect to
 

##Running locally
Can be run locally by starting the VertxRunner main method. Will need to have setup a compatible dynamo table would suggest starting with AWS deployment as will create dynamo tables



##Connecting the apollo client
Example connection inforamtion for apollo client, 
Chrome does sometimes have some header issues with the AWS websocket but below code works with chrome


```

import ApolloClient from 'apollo-client';
import { WebSocketLink } from "apollo-link-ws";
import { split } from 'apollo-link';
import { HttpLink } from 'apollo-link-http';
import { InMemoryCache } from 'apollo-boost';
import {SubscriptionClient} from 'subscriptions-transport-ws'

// Create an http link:
const httpLink = new HttpLink({
  uri: 'https://....execute-api.region.amazonaws.com/...',
  headers: {
    'Authorization': ...
  }
});

// Create a WebSocket link:
const wsLink = new WebSocketLink(new SubscriptionClient(`wss://....execute-api.region.amazonaws.com/...`, {
  reconnect: true,
  connectionParams: async () => {
    return {
      Authorization: ...
    }
  }
}, undefined, []));

const link = split(
  // split based on operation type
  ({ query }) => {
    const definition = getMainDefinition(query);
    return (
      definition.kind === 'OperationDefinition' &&
      definition.operation === 'subscription'
    );
  },
  wsLink,
  httpLink
);

const gqlClient = new ApolloClient(
  {
      link: link,
      cache: new InMemoryCache()
  }
)

```