package com.github.shubham.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("blog");

    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {

        System.out.println("Received Create Blog request: " + request);

        Blog blog = request.getBlog();
        Document doc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        System.out.println("Inserting Blog...");
        // we insert/create the document in mongodb
        collection.insertOne(doc);

        // we retrieve the MongoDB generated ID
        String id = doc.getObjectId("_id").toString();
        System.out.println("Inserted blog id: " + id);

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id).build())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {

        System.out.println("Received read blog request: " + request);
        String blogId = request.getBlogId();
        System.out.println("Searching for a blog with id: " + blogId);

        Document result = null;

        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception caught in server while searching for blogId " + blogId);
            System.out.println("Message from the error: " + e.getMessage());
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id does not exist")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result == null) {
            // we don't have a matching blog
            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id does not exist")
                            .asRuntimeException()
            );
        } else {
            System.out.println("Blog found sending response");
            Blog blog = documentToBlog(result);
            responseObserver.onNext(
                    ReadBlogResponse
                            .newBuilder()
                            .setBlog(blog)
                            .build()
            );
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {

        System.out.println("Received Update blog request: " + request);
        Blog blog = request.getBlog();
        String blogId = blog.getId();
        System.out.println("Searching for a blog so that we can update it");

        Document result = null;

        try {
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception caught in server while searching for blogId " + blogId);
            System.out.println("Message from the error: " + e.getMessage());
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id does not exist")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result == null) {
            // we don't have a matching blog
            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id does not exist")
                            .asRuntimeException()
            );
        } else {
            System.out.println("Blog found Updating the blog");
            Document docReplacement = new Document("author_id", blog.getAuthorId())
                    .append("title", blog.getTitle())
                    .append("content", blog.getContent())
                    .append("_id", new ObjectId(blogId));

            System.out.println("Replacing the blog in database...");
            collection.replaceOne(eq(
                            "_id",
                            result.getObjectId("_id")
                    ),
                    docReplacement);

            System.out.println("Replaced! Sending as a response");
            responseObserver.onNext(
                    UpdateBlogResponse
                            .newBuilder()
                            .setBlog(documentToBlog(docReplacement))
                            .build()
            );
            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {

        System.out.println("Received delete blog request");
        String blogId = request.getBlogId();
        DeleteResult result = null;
        try {
            System.out.println("searching for the blog id for deletion");
            result = collection.deleteOne(eq("_id", new ObjectId(blogId)));
        } catch (Exception e) {
            System.out.println("Blog not found");
            e.printStackTrace();
            System.out.println("Exception caught in server while searching for blogId " + blogId);
            System.out.println("Message from the error: " + e.getMessage());
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id does not exist")
                            .augmentDescription(e.getLocalizedMessage())
                            .asRuntimeException()
            );
        }

        if (result.getDeletedCount() == 0) {
            // we don't have a matching blog
            System.out.println("Blog not found");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The blog with the corresponding id does not exist")
                            .asRuntimeException()
            );
        } else {
            System.out.println("blog was deleted");
            responseObserver.onNext(
                    DeleteBlogResponse
                            .newBuilder()
                            .setBlogId(blogId)
                            .build()
            );
            responseObserver.onCompleted();
        }

    }

    private Blog documentToBlog(Document result) {
        return Blog.newBuilder()
                .setAuthorId(result.getString("author_id"))
                .setTitle(result.getString("title"))
                .setContent(result.getString("content"))
                .setId(result.getObjectId("_id").toString())
                .build();
    }

    @Override
    public void listBlog(ListBlogRequest request, StreamObserver<ListBlogResponse> responseObserver) {
        System.out.println("Received list blog request");

        // Streaming back the request to the client
        collection.find().iterator().forEachRemaining(document -> responseObserver.onNext(
                ListBlogResponse
                        .newBuilder()
                        .setBlog(documentToBlog(document))
                        .build()
        ));

        responseObserver.onCompleted();
    }
}
