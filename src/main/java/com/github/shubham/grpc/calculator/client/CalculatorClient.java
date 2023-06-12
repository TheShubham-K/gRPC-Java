package com.github.shubham.grpc.calculator.client;

import com.proto.calculator.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.w3c.dom.ls.LSOutput;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    public static void main(String[] args) {
        CalculatorClient main = new CalculatorClient();
        main.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        /**
         * APIs call
         */
        // doUnaryCall(channel);
        // doServerStreamingCall(channel);
        // doClientStreamingCall(channel);
        doBiDiStreamingCall(channel);

        System.out.println("Shutting down the channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {

        // created a greet service client (blocking - synchronous)

        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub
                = CalculatorServiceGrpc.newBlockingStub(channel);

        // Unary API Server
        SumRequest request = SumRequest.newBuilder()
                .setFirstNumber(10)
                .setSecondNumber(25)
                .build();

        SumResponse response = stub.sum(request);
        System.out.println(request.getFirstNumber() + " + "
                + request.getSecondNumber() + " = " + response.getSumResult());
    }

    private void doServerStreamingCall(ManagedChannel channel) {

        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub
                = CalculatorServiceGrpc.newBlockingStub(channel);

        long number = 5678999089032923090L;
        stub.primeNumberDecomposition(
                        PrimeNumberDecompositionRequest.newBuilder()
                                .setNumber(number)
                                .build())
                .forEachRemaining(primeNumberDecompositionResponse ->
                        System.out.println(primeNumberDecompositionResponse.getPrimeFactor())
                );
    }

    private void doClientStreamingCall(ManagedChannel channel) {

        CalculatorServiceGrpc.CalculatorServiceStub asyncStub = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<ComputeAverageRequest> requestStreamObserver = asyncStub.computeAverage(new StreamObserver<ComputeAverageResponse>() {
            @Override
            public void onNext(ComputeAverageResponse value) {

                System.out.println("Received a response from the server");
                System.out.println("Average from the server: " + value.getAverage());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending us data");
                latch.countDown();
            }
        });

        // we send 10000000 messages to our server (client streaming)
        for (int i = 0; i < 100000; i++) {
            requestStreamObserver.onNext(ComputeAverageRequest.newBuilder()
                    .setNumber(i)
                    .build());
        }

        // we expect the average to be sum_of(100000) / 100000 = 7049.82704
        requestStreamObserver.onCompleted();
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        }
    }


    private void doBiDiStreamingCall(ManagedChannel channel) {

        CalculatorServiceGrpc.CalculatorServiceStub asyncStub = CalculatorServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<FindMaximumRequest> requestStreamObserver = asyncStub.findMaximum(new StreamObserver<>() {
            @Override
            public void onNext(FindMaximumResponse value) {
                System.out.println("Go new maximum from server: " + value.getMaximum());
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onCompleted() {
                System.out.println("Server has completed sending us data");
                latch.countDown();
            }
        });

        Arrays.asList(3, 5, 17, 9, 8, 30, 12).forEach(number -> {
            System.out.println("Sending number: " + number);
            requestStreamObserver.onNext(FindMaximumRequest.newBuilder()
                    .setNumber(number)
                    .build()
            );
        });

        // we expect the average to be sum_of(100000) / 100000 = 7049.82704
        requestStreamObserver.onCompleted();
        try {
            latch.await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        }
    }

}
