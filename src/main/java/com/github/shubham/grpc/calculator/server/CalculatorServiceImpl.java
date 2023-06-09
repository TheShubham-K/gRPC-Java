package com.github.shubham.grpc.calculator.server;

import com.proto.calculator.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {

    @Override
    public void sum(SumRequest request, StreamObserver<SumResponse> responseObserver) {
        // super.sum(request, responseObserver);

        SumResponse sumResponse = SumResponse.newBuilder()
                .setSumResult(request.getFirstNumber() + request.getSecondNumber())
                .build();

        responseObserver.onNext(sumResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void primeNumberDecomposition(PrimeNumberDecompositionRequest request, StreamObserver<PrimeNumberDecompositionResponse> responseObserver) {

        long number = request.getNumber();
        long divisor = 2L;

        while (number > 1) {
            if (number % divisor == 0) {
                number = number / divisor;
                responseObserver.onNext(
                        PrimeNumberDecompositionResponse
                                .newBuilder()
                                .setPrimeFactor(divisor)
                                .build()
                );
            } else {
                divisor = divisor + 1;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<ComputeAverageRequest> computeAverage(StreamObserver<ComputeAverageResponse> responseObserver) {

        return new StreamObserver<ComputeAverageRequest>() {

            // running sum and count
            int sum = 0;
            int count = 0;

            @Override
            public void onNext(ComputeAverageRequest value) {
                // increment the sum
                sum += value.getNumber();
                // increment the count
                count += 1;
            }

            @Override
            public void onError(Throwable t) {
                System.out.println("Error in compute: " + t.toString());
            }

            @Override
            public void onCompleted() {

                double average = (double) sum / count;
                responseObserver.onNext(ComputeAverageResponse.newBuilder()
                        .setAverage(average)
                        .build()
                );
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<FindMaximumRequest> findMaximum(StreamObserver<FindMaximumResponse> responseObserver) {
        return new StreamObserver<FindMaximumRequest>() {

            long currentMaximum = 0L;

            @Override
            public void onNext(FindMaximumRequest value) {
                long currentNumber = value.getNumber();
                if (currentNumber > currentMaximum) {
                    currentMaximum = currentNumber;
                    responseObserver.onNext(FindMaximumResponse.newBuilder().setMaximum(currentMaximum).build());
                } else {
                    // do nothing
                }
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onCompleted();
            }

            @Override
            public void onCompleted() {
                // send the current last maximum
                responseObserver.onNext(FindMaximumResponse.newBuilder().setMaximum(currentMaximum).build());
                // server is done sending data
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void squareRoot(SquareRootRequest request, StreamObserver<SquareRootResponse> responseObserver) {
        long number = request.getNumber();
        if (number >= 0) {
            double numberRoot = Math.sqrt(number);
            responseObserver.onNext(
                    SquareRootResponse
                            .newBuilder()
                            .setNumberRoot(numberRoot)
                            .build()
            );
            responseObserver.onCompleted();
        } else {
            // we construct the exception
            responseObserver.onError(
                    Status.INVALID_ARGUMENT
                            .withDescription("The Number being sent is not positive")
                            .augmentDescription("Number sent: " + number)
                            .asRuntimeException()
            );
        }
    }
}
