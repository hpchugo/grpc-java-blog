package com.github.hpchugo.grpc.blog.server;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private final MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private final MongoDatabase database = mongoClient.getDatabase("mydb");
    private final MongoCollection<Document> collection = database.getCollection("blog");//table

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {
        System.out.println("Received Create Blog request");
        Blog blog = request.getBlog();
        Document doc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());
        //insert (create) the document in MongoDB
        System.out.println("Inserting blog...");
        collection.insertOne(doc);

        //retrieve the MongoDB generated ID
        String id = doc.getObjectId("_id").toString();
        System.out.printf("Inserted blog: %s\n", id);

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id).build()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
        System.out.println("Received Read Blog request");
        String blogId = request.getBlogId();
        Document result = findBlogById(blogId);

        if(result == null) {
            System.out.println("Blog not found");
            responseObserver.onError(Status.NOT_FOUND.withDescription("The blog with the corresponding id was not found").asRuntimeException());
        }else {
            System.out.println("Blog found, sending response");
            Blog blog = documentToBlog(result);
            responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(blog).build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        Blog blog = request.getBlog();
        Document result = findBlogById(blog.getId());
        if(result == null) {
            System.out.println("Blog not found");
            responseObserver.onError(Status.NOT_FOUND.withDescription("The blog with the corresponding id was not found").asRuntimeException());
        }else{
            Document replacement = new Document("author_id", blog.getAuthorId())
                    .append("title", blog.getTitle())
                    .append("content", blog.getContent())
                    .append("_id", new ObjectId(blog.getId()));

            System.out.println("Replacing blog in database");
            collection.replaceOne(eq("_id",result.getObjectId("_id")), replacement);
            System.out.println("Replaced! Sending as a response");

            responseObserver.onNext(UpdateBlogResponse.newBuilder()
                    .setBlog(documentToBlog(replacement))
                    .build());
            responseObserver.onCompleted();
        }
    }

    private Document findBlogById(String blogId){
        System.out.println("Searching for a blog");
        try{
            return collection.find(eq("_id", new ObjectId(blogId))).first();
        }catch (Exception e){
            throw new StatusRuntimeException(Status.fromCode(Status.Code.NOT_FOUND)
                    .augmentDescription(e.getLocalizedMessage())
                    .withDescription("The blog with the corresponding id was not found"));
        }
    }
    private Blog documentToBlog(Document document){
        return Blog.newBuilder()
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .setId(document.getObjectId("_id").toString())
                .build();
    }
}
