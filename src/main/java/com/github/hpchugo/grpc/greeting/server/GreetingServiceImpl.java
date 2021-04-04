package com.github.hpchugo.grpc.greeting.server;

import com.proto.greet.*;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;

public class GreetingServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {
    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        //extract fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();

        //create the response
        GreetResponse response = GreetResponse.newBuilder()
                .setResult("Hello " + firstName)
                .build();

        //send the response
        responseObserver.onNext(response);

        //complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTimes(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver) {
        String firstName = request.getGreeting().getFirstName();
        try {
            for (int i = 0; i < 10; i++) {
                String result = "Hello " + firstName + ", response number: " + i;
                GreetManyTimesResponse response = GreetManyTimesResponse.newBuilder()
                        .setResult(result)
                        .build();
                responseObserver.onNext(response);
                Thread.sleep(1000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public StreamObserver<LongGreetRequest> longGreet(StreamObserver<LongGreetResponse> responseObserver) {
        StringBuffer result = new StringBuffer();
        return new StreamObserver<>() {
            @Override
            public void onNext(LongGreetRequest value) {
                result.append(String.format("Hello %s", value.getGreeting().getFirstName()));
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onNext(LongGreetResponse.newBuilder().setResult(result.toString()).build());
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<GreetEveryoneRequest> greetEveryone(StreamObserver<GreetEveryoneResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(GreetEveryoneRequest value) {

                responseObserver.onNext(GreetEveryoneResponse.newBuilder()
                        .setResult(String.format("Hello %s", value.getGreeting().getFirstName()))
                        .build());
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public void greetWithDeadline(GreetWithDeadlineRequest request, StreamObserver<GreetWithDeadlineResponse> responseObserver) {
        Context current = Context.current();
        try {
            for (int i = 0; i < 3; i++) {
                if(!current.isCancelled()) {
                    System.out.println("Sleeping for 100ms");
                    Thread.sleep(100);
                }
                else {
                    return;
                }
            }

            System.out.println("Sending response");
            responseObserver.onNext(
                    GreetWithDeadlineResponse.newBuilder()
                            .setResult(String.format("Hello %s", request.getGreeting().getFirstName()))
                            .build()
            );
            responseObserver.onCompleted();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
