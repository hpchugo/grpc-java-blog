package com.github.hpchugo.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.*;
import static java.lang.System.out;

public class BlogClient {
    public static void main(String[] args){
        out.println("Hello I'm a gRPC client for Blog");
        BlogClient main = new BlogClient();
        main.run();
    }

    public void run() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50053)
                .usePlaintext() //Disable ssl
                .build();

        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);
        Blog blog = Blog.newBuilder()
                .setAuthorId("Kakarot")
                .setTitle("New Blog!")
                .setContent("Hello World this is my first blog")
                .build();
        out.println("Shutting down channel");

        CreateBlogResponse response = blogClient.createBlog(CreateBlogRequest.newBuilder().setBlog(blog).build());
        out.printf("Received create blog response %s", response.toString());

        out.println("Reading blog...");
        String blogId = response.getBlog().getId();
        ReadBlogResponse readBlogResponse = blogClient.readBlog(ReadBlogRequest.newBuilder().setBlogId(blogId).build());
        out.printf("Received create blog response %s", readBlogResponse.toString());


        Blog blogUpdated = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("Piccolo")
                .setTitle("New Updated Blog!")
                .setContent("Hello World this is my first blog! Updated")
                .build();

        out.println("Updating blog...");
        UpdateBlogResponse updateBlogResponse =blogClient.updateBlog(UpdateBlogRequest.newBuilder().setBlog(blogUpdated).build());
        out.printf("Updated blog\n Received update blog response %s", updateBlogResponse.toString());

        out.println("Deleting blog...");
        DeleteBlogResponse deleteBlogResponse =blogClient.deleteBlog(DeleteBlogRequest.newBuilder().setBlogId(blogId).build());
        out.printf("Deleted blog\n Received update blog response %s", deleteBlogResponse.toString());

        out.println("Listing blog...");
        blogClient.listBlog(ListBlogRequest.newBuilder().build()).forEachRemaining(listBlogResponse -> out.println(listBlogResponse.getBlog().toString()));

        channel.shutdown();
        out.println("Shutting down channel");
    }
}
