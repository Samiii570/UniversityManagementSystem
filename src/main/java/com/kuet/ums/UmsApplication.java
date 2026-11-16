package com.kuet.ums;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

@SpringBootApplication
public class UmsApplication {
    private static final int DEFAULT_PORT = 8081;
    private static final int MAX_TRIES = 50;

    public static void main(String[] args) {
        int preferred = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", String.valueOf(DEFAULT_PORT)));
        int port = findFreePort(preferred);
        if (port != preferred) {
            System.out.println(">>> Port " + preferred + " busy, using port " + port + " instead");
        }
        System.setProperty("server.port", String.valueOf(port));

        ConfigurableApplicationContext ctx = SpringApplication.run(UmsApplication.class, args);
        String actual = ctx.getEnvironment().getProperty("local.server.port", String.valueOf(port));
        System.out.println("\n>>> University Management System is running: http://localhost:" + actual + "\n");
    }

    private static int findFreePort(int start) {
        for (int p = start; p < start + MAX_TRIES; p++) {
            try (ServerSocket socket = new ServerSocket()) {
                socket.setReuseAddress(false);
                socket.bind(new InetSocketAddress(p));
                return p;
            } catch (IOException busy) {
            }
        }
        throw new IllegalStateException("No free port found between " + start + " and " + (start + MAX_TRIES - 1));
    }
}
