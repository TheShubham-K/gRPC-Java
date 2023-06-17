package com.github.shubham.grpc.greeting.client;

import com.proto.dummy.DummyServiceGrpc;
import com.proto.greet.*;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import org.checkerframework.checker.units.qual.Time;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GreetingClient {

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC Client");
        GreetingClient client = new GreetingClient();
        client.run();

    }

    public void run() {
        /**
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();
         */

        // With server authentication SSL/TLS; custom CA root certificates
        ChannelCredentials creds = null;
        try {
            creds = TlsChannelCredentials.newBuilder()
                    .trustManager(new File("ssl/ca.crt"))
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ManagedChannel secureChannel = Grpc.newChannelBuilder("localhost:50051", creds)
                .build();

        /**
         * All gRPC-APIs calls with plan text (Non-SSL)
         */

        // doUnaryCall(channel);
        // doServerStreamingCall(channel);
        // doClientStreamingCall(channel);
        // doBiDiStreamingCall(channel);
        // doUnaryCallWithDeadline(channel);

        /**
         * All gRPC-APIs calls with Secured Channel (SSL)
         */
         doUnaryCall(secureChannel);
        // doServerStreamingCall(secureChannel);
        // doClientStreamingCall(secureChannel);
        // doBiDiStreamingCall(secureChannel);
        // doUnaryCallWithDeadline(secureChannel);

        // finally we shut down the channel
        System.out.println("Shutting Down Channel");
        secureChannel.shutdown();
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

    private void doClientStreamingCall(ManagedChannel channel) {

        // create an asynchronous client (stub)
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<LongGreetRequest> requestStreamObserver = asyncClient.longGreet(new StreamObserver<>() {
            @Override
            public void onNext(LongGreetResponse value) {
                // we get a response from the server
                System.out.println("Received a response from the server");
                System.out.println(value.getResult());
                // onNext will be called only once
            }

            @Override
            public void onError(Throwable t) {
                // we get an error response from the server
            }

            @Override
            public void onCompleted() {
                // the server is done sending us data to the client
                // onCompleted will be called right after onNext()
                System.out.println("Server has completed sending us something");
                latch.countDown();
            }
        });

        // streaming message #1
        System.out.println("Sending message #1");
        requestStreamObserver.onNext(
                LongGreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                                .setFirstName("Shubham")
                                .build())
                        .build()
        );

        // streaming message #2
        System.out.println("Sending message #2");
        requestStreamObserver.onNext(
                LongGreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                                .setFirstName("John")
                                .build())
                        .build()
        );

        // streaming message #3
        System.out.println("Sending message #3");
        requestStreamObserver.onNext(
                LongGreetRequest.newBuilder()
                        .setGreeting(Greeting.newBuilder()
                                .setFirstName("Marcel")
                                .build())
                        .build()
        );

        // we tell the server that the client is done sending data to the server
        requestStreamObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void doBiDiStreamingCall(ManagedChannel channel) {
        // create an asynchronous client (stub)
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestStreamObserver = asyncClient.greetEveryone(new StreamObserver<>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                System.out.println("Response from the server: " + value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error from the server: " + t.getMessage());
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Server is done sending data");
                latch.countDown();
            }
        });

        Arrays.asList("Stephane", "John", "Marc", "Patricia").forEach(
                name -> {
                    System.out.println("Sending: " + name);
                    requestStreamObserver.onNext(GreetEveryoneRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder()
                                    .setFirstName(name))
                            .build());
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        requestStreamObserver.onCompleted();
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        }
    }

    private void doUnaryCallWithDeadline(ManagedChannel channel) {
        GreetServiceGrpc.GreetServiceBlockingStub blockingStub = GreetServiceGrpc.newBlockingStub(channel);

        // first call (3000ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 3000 ms");
            GreetWithDeadlinesResponse response = blockingStub.withDeadlineAfter(3000, TimeUnit.MILLISECONDS)
                    .greetWithDeadlines(GreetWithDeadlinesRequest.newBuilder().setGreeting(
                                    Greeting.newBuilder().setFirstName("Shubham")
                            ).build()
                    );
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }

        // second call (100ms deadline)
        try {
            System.out.println("Sending a request with a deadline of 100 ms");
            GreetWithDeadlinesResponse response = blockingStub.withDeadline(Deadline.after(100, TimeUnit.MICROSECONDS))
                    .greetWithDeadlines(GreetWithDeadlinesRequest.newBuilder().setGreeting(
                                    Greeting.newBuilder().setFirstName("Shubham")
                            ).build()
                    );
            System.out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                System.out.println("Deadline has exceeded, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }
    }
}
