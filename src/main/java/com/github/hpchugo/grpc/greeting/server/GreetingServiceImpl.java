package com.github.hpchugo.grpc.greeting.server;

import com.proto.greet.*;
import io.grpc.stub.StreamObserver;

public class GreetingServiceImpl extends GreetServiceGrpc.GreetServiceImplBase {
    @Override
    public void greet(GreetRequest request, StreamObserver<GreetResponse> responseObserver) {
        //extract fields we need
        Greeting greeting = request.getGreeting();
        String firstName = greeting.getFirstName();

        //create the response
        StringBuffer result = new StringBuffer("Hello ");
        result.append(firstName);
        GreetResponse response = GreetResponse.newBuilder()
                .setResult(result.toString())
                .build();

        //send the response
        responseObserver.onNext(response);

        //complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void greetManyTime(GreetManyTimesRequest request, StreamObserver<GreetManyTimesResponse> responseObserver){
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
       }catch (InterruptedException e){
           e.printStackTrace();
       }
       finally {
           responseObserver.onCompleted();
       }
    }
}
