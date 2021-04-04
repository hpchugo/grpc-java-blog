package com.github.hpchugo.grpc.greeting.client;

import com.proto.greet.*;
import com.proto.greet.GreetServiceGrpc.GreetServiceBlockingStub;
import com.proto.greet.GreetServiceGrpc.GreetServiceStub;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.System.*;

public class GreetingClient {
    public static void main(String[] args) {
        out.println("Hello I'm a gRPC client");
        GreetingClient main = new GreetingClient();
        main.run();
    }

    public void run() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext() //Disable ssl
                .build();

/*
        doUnaryCall(channel);
        doServerStreamingCall(channel);
        doClientStreamingCall(channel);
        doBiDirectionalStreamingCall(channel);

 */
        doUnaryWithDeadlineCall(channel);
        out.println("Shutting down channel");
        channel.shutdown();
    }

    private void doUnaryCall(ManagedChannel channel) {
        //created a greet service client (blocking - synchronous)
        GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        out.println("Creating stub");

        //created a protocol buffer greeting message
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Hugo")
                .setLastName("Cordeiro")
                .build();
        //do the same for a GreetRequest
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        //call the RPC and get back a GreetResponse(protocol buffers)
        GreetResponse greetResponse = greetClient.greet(greetRequest);

        out.println(greetResponse.getResult());
    }

    private void doServerStreamingCall(ManagedChannel channel) {
        //created a greet service client (blocking - synchronous)
        GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        GreetManyTimesRequest greetManyTimesRequest = GreetManyTimesRequest.newBuilder()
                .setGreeting(Greeting.newBuilder()
                        .setFirstName("Hugo"))
                .build();

        greetClient.greetManyTimes(greetManyTimesRequest)
                .forEachRemaining(greetManyTimesResponse -> out.println(greetManyTimesResponse.getResult()));

    }

    private void doClientStreamingCall(ManagedChannel channel) {
        //created a greet service client (blocking - asynchronous)
        GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);
        StreamObserver<LongGreetRequest> requestObserver = asyncClient.longGreet(new StreamObserver<>() {
            @Override
            public void onNext(LongGreetResponse value) {
                out.println("Received a response from the server");
                out.println(value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                out.println("Server has completed something");
                latch.countDown();
            }
        });

        for (String name : Arrays.asList("Kakarot", "Vegeta", "Gohan", "Piccolo")) {
            out.printf("Sending: %s\n", name);
            requestObserver.onNext(LongGreetRequest.newBuilder()
                    .setGreeting(Greeting.newBuilder()
                            .setFirstName(name))
                    .build());
        }

        //we tell the server that the client is done sending data
        requestObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doBiDirectionalStreamingCall(ManagedChannel channel) {
        //created a greet service client (blocking - asynchronous)
        GreetServiceGrpc.GreetServiceStub asyncClient = GreetServiceGrpc.newStub(channel);
        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<GreetEveryoneRequest> requestObserver = asyncClient.greetEveryone(new StreamObserver<>() {
            @Override
            public void onNext(GreetEveryoneResponse value) {
                out.printf("Response from server: %s\n", value.getResult());
            }

            @Override
            public void onError(Throwable t) {
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                out.println("Server is done sending data");
                latch.countDown();
            }
        });
        Arrays.asList("Kakarot", "Vegeta", "Gohan", "Piccolo").forEach(
                name -> {
                    out.printf("Sending: %s\n", name);
                    requestObserver.onNext(GreetEveryoneRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder()
                                    .setFirstName(name))
                            .build());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
        );
        requestObserver.onCompleted();
        try {
            latch.await(3L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void doUnaryWithDeadlineCall(ManagedChannel channel) {
        GreetServiceBlockingStub greetClient = GreetServiceGrpc.newBlockingStub(channel);

        out.println("Creating stub");

        try {
            out.println("Sending a request with a deadline of 3000ms");
            GreetWithDeadlineResponse response = greetClient.withDeadline(Deadline.after(3000, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder().setFirstName("Hugo").getDefaultInstanceForType())
                            .build());
            out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                out.println("Deadline has been exceed, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }


        try {
            out.println("Sending a request with a deadline of 100ms");
            GreetWithDeadlineResponse response = greetClient.withDeadline(Deadline.after(100, TimeUnit.MILLISECONDS))
                    .greetWithDeadline(GreetWithDeadlineRequest.newBuilder()
                            .setGreeting(Greeting.newBuilder().setFirstName("Hugo").getDefaultInstanceForType())
                            .build());
            out.println(response.getResult());
        } catch (StatusRuntimeException e) {
            if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
                out.println("Deadline has been exceed, we don't want the response");
            } else {
                e.printStackTrace();
            }
        }
    }

}
