package com.github.shubham.grpc.greeting.client;

import com.proto.dummy.DummyServiceGrpc;
import com.proto.greet.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient {

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC Client");
        GreetingClient client = new GreetingClient();
        client.run();

    }

    public void run() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        // doUnaryCall(channel);
        doServerStreamingCall(channel);
        // doClientStreamingCall(channel);


        // finally we shut down the channel
        System.out.println("Shutting Down Channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {

        // Old and dummy code
        DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);

        System.out.println("Creating stub");
        // created a greet service client (blocking synchronous)
        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
        // Async greet service client (asynchronous)
        // GreetServiceGrpc.GreetServiceFutureStub greetClient = GreetServiceGrpc.newFutureStub(channel);

        // Unary service client (blocking synchronous)
        // created a protocol buffer greeting message
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Shubham")
                .setLastName("Kumar")
                .build();

        // do the same for a GreetRequest
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        // call the RPC and get back a GreetResponse (protocol buffer)
        GreetResponse greetResponse = greetClient.greet(greetRequest);

        // print the greeting response
        System.out.println(greetResponse.getResult());
    }
    private void doServerStreamingCall(ManagedChannel channel) {

        GreetServiceGrpc.GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);
        GreetManyTimesRequest greetingManyTimesRequest = GreetManyTimesRequest
                .newBuilder()
                .setGreeting(Greeting.newBuilder().setFirstName("Shubham"))
                .build();

        // we stream the response (in a blocking manner)
        greetClient.greetManyTimes(greetingManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> {
                    System.out.println(greetManyTimesResponse.getResult());
                });
    }
    private void doClientStreamingCall(ManagedChannel channel) {}
}
