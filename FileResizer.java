package org.example;

import java.io.*;
import java.nio.channels.FileChannel;

public class FileResizer {
    private File file;
    private long fileSize;//in mb
    private final long fromByteToMegabyte = 1024*1024*1024; //byte to mb
    private final long MAX_SIZE = 100; //size in megabyte

    FileResizer(File file){
        this.file = file;
    }

    public void getBytesFromFile() throws IOException {
        int numberOfChunks = 6;
        final int BUFFER_SIZE = 4096;
        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file.getPath()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int nRead;
        while ((nRead = bis.read(buffer, 0, BUFFER_SIZE)) != -1) {
            baos.write(buffer, 0, nRead);
        }

        byte[] fileBytes = baos.toByteArray();

        long chunkSize = (file.length() + numberOfChunks - 1) / numberOfChunks;
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        for (int i = 0; i < numberOfChunks; i++) {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(file.getAbsolutePath() + "_" + (i + 1) + ".part"));
            long bytesRemaining = chunkSize;
            while (bytesRemaining > 0 && (nRead = raf.read(buffer, 0, (int) Math.min(bytesRemaining, BUFFER_SIZE))) != -1) {
                bw.write(buffer, 0, nRead);
                bytesRemaining -= nRead;
            }
            bw.close();
        }
    }

    public String[] fileFinder(int numberOfChunks){//return all path to files from .part
        String[] filesParts = new String[numberOfChunks];
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < file.getName().lastIndexOf('.'); i++){
            temp.append(file.getName().charAt(i));
        }
        String temp1 = temp.toString();
        for (int i = 0; i < numberOfChunks; i++){
            temp.append("_").append(i).append(".part");
            filesParts[i] = file.getPath()+temp.toString();
            temp = new StringBuilder(temp1);
        }
        return filesParts;
    }

    public File resize(){
        fileSize = file.length()/fromByteToMegabyte;
        if (fileSize > MAX_SIZE){//если >100 mb то разбить файл
            return file;
        }
        return file;
    }
}
