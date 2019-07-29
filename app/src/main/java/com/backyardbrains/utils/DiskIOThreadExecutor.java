package com.backyardbrains.utils;

import androidx.annotation.NonNull;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Executor that runs a task on a new background thread.
 *
 * @author Tihomir Leka <tihomir at backyardbrains.com>
 */
public class DiskIOThreadExecutor implements Executor {

    private final Executor mDiskIO;

    public DiskIOThreadExecutor() {
        mDiskIO = Executors.newSingleThreadExecutor();
    }

    @Override public void execute(@NonNull Runnable command) {
        mDiskIO.execute(command);
    }
}