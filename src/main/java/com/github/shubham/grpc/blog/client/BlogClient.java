package com.github.shubham.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {

    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC Client for Blog");
        BlogClient main = new BlogClient();
        main.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

//        Blog blog = Blog.newBuilder()
//                .setAuthorId("Shubham")
//                .setTitle("New Blog!")
//                .setContent("Hello world this is my first blog")
//                .build();
//
//
//        CreateBlogResponse response = blogClient.createBlog(
//                CreateBlogRequest.newBuilder()
//                        .setBlog(blog)
//                        .build()
//        );
//
//        System.out.println("Received create blog response: " + response);
//        System.out.println(response.toString());

//        String blogId = response.getBlog().getId();
//        ReadBlogResponse readBlogResponse = blogClient.readBlog(
//                ReadBlogRequest
//                        .newBuilder()
//                        .setBlogId(blogId)
//                        .build()
//        );
//        System.out.println("Read blog response: \n" + readBlogResponse);

        /**
         * to trigger a NOT_FOUND error
         *          try{
         *             ReadBlogResponse readBlogResponseNotFound = blogClient.readBlog(
         *                     ReadBlogRequest
         *                             .newBuilder()
         *                             .setBlogId("6491e2fda6w4733f734083e9")
         *                             .build()
         *             );
         *             System.out.println("Read blog with fake-id: " + readBlogResponseNotFound);
         *         } catch (RuntimeException e) {
         *             e.printStackTrace();
         *             System.out.println("Exception recorded: "+ e.getMessage());
         *         }
         */


//        Blog newBlog = Blog.newBuilder()
//                .setId(blogId)
//                .setAuthorId("Changed Author")
//                .setTitle("New Blog! (ReUpdated)")
//                .setContent("Hello world this is my first blog with Update request! I've added some more content")
//                .build();

//        System.out.println("Updating blog...");
//        UpdateBlogResponse updateBlogResponse = blogClient.updateBlog(
//                UpdateBlogRequest
//                        .newBuilder()
//                        .setBlog(newBlog)
//                        .build()
//        );
//        System.out.println("Updated blog response: " + updateBlogResponse.toString());
//
//        System.out.println("Deleting blog...");
//        DeleteBlogResponse deleteBlogResponse = blogClient.deleteBlog(
//                DeleteBlogRequest
//                        .newBuilder()
//                        .setBlogId(blogId)
//                        .build()
//        );
//        System.out.println("Deleted blog response: " + deleteBlogResponse.toString());

        // we list the blog in our database
        blogClient.listBlog(ListBlogRequest.newBuilder().build()).forEachRemaining(
                listBlogResponse -> System.out.println(listBlogResponse.getBlog())
        );

        System.out.println("Shutting down the channel");
        channel.shutdown();
    }
}
