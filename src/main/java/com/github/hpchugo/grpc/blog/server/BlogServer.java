package com.github.hpchugo.grpc.blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class BlogServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello gRPC");

        //plaintext server
        Server server = ServerBuilder.forPort(50053)
                .addService(new BlogServiceImpl())
                .build();

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            System.out.println("Recieved shutdown request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));
        server.awaitTermination();
    }
}
