package org.opengeo.data.importer.job;

import java.util.concurrent.FutureTask;

public class Task<V> extends FutureTask<V> {

    ProgressMonitor monitor;
    Throwable error;
    boolean recieved = false;

    public Task(Job<V> job) {
        super(job);
        monitor = new ProgressMonitor();
        job.setMonitor(monitor);
    }

    public ProgressMonitor getMonitor() {
        return monitor;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }

    public void recieve() {
        recieved = true;
    }

    public boolean isRecieved() {
        return recieved;
    }
}
