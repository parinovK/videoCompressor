package org.example;

import java.io.File;
import java.util.*;

/*
Проблема - переполнение памяти при обработке файлов большого объема, outOfMemory. решение:
1)Делить файл на части, если он имеет вес ~>500мб, сжимать и соединять обратно.
Exception in thread "Thread-3" Exception in thread "Thread-1" java.lang.OutOfMemoryError: Java heap space
 */
public class App 
{
    static final float version = 0.1f;
    static final String appName = "VideoCompressor by 1scnd_";

    static boolean findExtension(String name){//определяем расширение
        String[] allowedExtension = {".mov", ".mp4"};
        StringBuilder extension = new StringBuilder();
        int i = name.length()-1;
        while(name.charAt(i) != '.'){
            extension.append(name.charAt(i));
            i--;
        }
        extension.append('.');
        extension.reverse();
        for (String temp : allowedExtension)
            if (temp.contentEquals(extension))
                return true;
        return false;
    }

    static List<String> searchFilesInDirectory(File file, List<String> allFiles){
        if (file.isDirectory()){
            File[] tempArray = file.listFiles();
            assert tempArray != null;
            for (File i : tempArray)
                searchFilesInDirectory(i, allFiles);
        }
        else if (findExtension(file.getName()))
            allFiles.add(file.getPath());
        return allFiles;
    }

    public static void splitList(List<String> allFiles, String output){
        File file;
        for (String path : allFiles) {
            file = new File(path);
            System.out.println("Вес файла " + file.getName() + ": " + file.length()/1000000 + " mb");
        }
        System.out.println("\nКоличество файлов: " + allFiles.size());
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("\nВ системе обнаружено " + cores + " ядер ЦП.\n" +
                "Во избежание проблем с производительностью в обычных программах все ядра не будут использованы.");
        cores /= 2;
        List<List<String>> distributedData = new LinkedList<>();
        System.out.println("Задача будет расределена для " + cores + " ядер");
        for (int i = 0; i < cores; i++)
            distributedData.add(new LinkedList<>());
        int count = 0;
        while (count != allFiles.size()){
            for (var list : distributedData) {
                if (count != allFiles.size()){
                    list.add(allFiles.get(count));
                    count++;
                }
            }
        }
        count = 0;
        for (var list : distributedData)
            count += list.size();
        if (count == allFiles.size())
            System.out.println("Файлы успешно разделены между потоками.");
        else {
            System.err.println("Файлы поделены неверно. Во избежание проблем сжатие невозможно.");
            distributedData.clear();
        }
        allFiles.clear();
        if (!distributedData.isEmpty())
            runThreads(distributedData, output);
    }

    public static void runThreads(List<List<String>>distributedData, String output){
        int count = 0;
        for (var list : distributedData)
            if (!list.isEmpty())
                count++;
        Compressor[] compressors = new Compressor[count];
        for (int i = 0; i < compressors.length; i++)
            if (!distributedData.get(i).isEmpty())
                compressors[i] = new Compressor(distributedData.get(i), output);
        for(Compressor compressor : compressors)
            compressor.start();
    }

    public static void main( String[] args)
    {
        System.out.println(appName);
        System.out.println("Версия компрессора: " + version);
        System.out.println("Введите путь до папки с видео: ");
        File path = new File (new Scanner(System.in).nextLine());
        System.out.println("Введите путь до папки с выходными файлами: ");
        String output = new Scanner(System.in).nextLine();
        List<String> allFiles = new ArrayList<>();
        allFiles = searchFilesInDirectory(path, allFiles);
        if (!allFiles.isEmpty()) {
            splitList(allFiles, output);
        }
        else System.out.println("Медиафайлов в пути с типом \"видео\" не найдено.");
    }
}
