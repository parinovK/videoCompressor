package org.example;

import io.github.techgnious.IVCompressor;
import io.github.techgnious.dto.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.List;

public class Compressor extends Thread{
    IVCompressor compressor;
    List<String> allFiles_path;
    private final String OUTPUT_PATH;
    IVAudioAttributes audioAttribute = new IVAudioAttributes();
    IVVideoAttributes videoAttribute = new IVVideoAttributes();
    IVSize ivSize = new IVSize();

    Compressor(List<String> allFiles_path, String output) {
        this.compressor = new IVCompressor();
        this.allFiles_path = allFiles_path;
        OUTPUT_PATH = output + "\\";
        videoAttribute.setBitRate(1280000);
        ivSize.setWidth(640);
        ivSize.setHeight(480);
        videoAttribute.setSize(ivSize);
        videoAttribute.setFrameRate(60);
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        for(String file_path : allFiles_path)
        {
            File file = new File(file_path);
            try {
                if ((int)file.length()/(1024*1024) > 250){
                    System.err.println("Файл \"" + file.getName() + "\" не будет обработан, во избежание исключения OutOfMemoryError");
                }
                else {
                    byte[] compressFile = compressor.encodeVideoWithAttributes(Files.readAllBytes(file.toPath()), VideoFormats.MP4, audioAttribute, videoAttribute);
                    FileOutputStream out = new FileOutputStream(OUTPUT_PATH + file.getName());
                    out.write(compressFile, 0, compressFile.length);
                    out.flush();
                    out.close();
                    System.out.println("Файл " + file.getName() + " успешно сжат");
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        int resultTime = (int)((System.currentTimeMillis()-start)/1000);
        if (resultTime > 60)
            System.out.println("time: " + resultTime/60 + " мин.");
        else System.out.println("time: " + resultTime + " сек.");
        System.out.println("Поток закончил работу!");
    }
}