package com.github.hpchugo.grpc.blog.client;

import com.proto.greet.*;
import com.proto.greet.GreetServiceGrpc.GreetServiceBlockingStub;
import com.proto.greet.GreetServiceGrpc.GreetServiceStub;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

import javax.net.ssl.SSLException;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;

public class BlogClient {
    public static void main(String[] args) throws SSLException {
        out.println("Hello I'm a gRPC client for Blog");
        BlogClient main = new BlogClient();
        main.run();
    }

    public void run() throws SSLException {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext() //Disable ssl
                .build();

        ManagedChannel secureChannel = NettyChannelBuilder
                .forAddress("localhost", 50051)
                .sslContext(GrpcSslContexts
                        .forClient()
                        .trustManager(new File("ssl/ca.crt"))
                        .build())
                .build();



        out.println("Shutting down channel");
        channel.shutdown();
    }
}
