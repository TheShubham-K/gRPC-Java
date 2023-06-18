package com.github.shubham.grpc.blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;

public class BlogServer {

        public static void main(String[] args) throws IOException, InterruptedException {
            System.out.println("Hello gRPC from Blog server");
            Logger logger = LoggerFactory.getLogger(BlogServer.class);

            Server server = ServerBuilder.forPort(50051)
                    .addService(new BlogServiceImpl()) // for reflection
                    .build();
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Received Shutdown Request");
                server.shutdown();
                logger.info("Successfully stopped the server");
                System.out.println("Successfully stopped the server");
            }));

            server.awaitTermination();
        }


}
