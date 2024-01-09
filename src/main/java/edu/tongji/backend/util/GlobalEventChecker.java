package edu.tongji.backend.util;

import lombok.Getter;

import java.util.EventListener;
import java.util.EventObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.concurrent.CompletableFuture;

public class GlobalEventChecker {
    @Getter
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    @Getter
    private static CompletableFuture<Void> bCalledFuture = new CompletableFuture<>();
}

