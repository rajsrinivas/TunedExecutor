package com.rajsrinivas.common.concurrent.traffic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

class DiskWriteTask implements Runnable {

    private final long sizeInMB;
    private static final long MBS = 1024 * 1024L;
    public DiskWriteTask() { this(10);};
    public DiskWriteTask(int sizeInMB) { this.sizeInMB = sizeInMB;}


    @Override
    public void run() {
        try {
            String uuid = UUID.randomUUID().toString();
            File f = Files.createTempFile(uuid, ".diskWrite").toFile();
            BufferedWriter w = new BufferedWriter(new FileWriter(f));
            for( int i =0; i < (sizeInMB * MBS) / 32 ; i++){
                w.write(uuid);
            }
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
