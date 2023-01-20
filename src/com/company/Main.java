package com.company;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {


    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Arg0 must be a folder path!");
            return;
        }

        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("The path is valid!");
            return;
        }
        try {
            findFolder(file);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String readFromFile(String filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return new String(bytes);
    }

    private static void readToNewFile(File inputFile, File outFile) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(inputFile);
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);
        FileOutputStream fileOutputStream = new FileOutputStream(outFile);
        DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

        int readByte;
        int zeroSize = 0;
        while((readByte = dataInputStream.readByte()) == 0x30){
            zeroSize++;
        }
        dataOutputStream.write(readByte);
        byte[] bytes = new byte[(int)inputFile.length() - zeroSize - 1];

        dataInputStream.read(bytes);
        dataOutputStream.write(bytes);

        dataOutputStream.close();
        fileOutputStream.close();
        dataInputStream.close();
        fileInputStream.close();
    }

    private static void findFolder(File file) throws Exception {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            List<File> allFiles = Arrays.asList(files);
            Collections.sort(allFiles);
            for (File f : allFiles) {
                if (f.isDirectory()) {
                    boolean transe = false;
                    String cmd = "";
                    File outputFile;
                    String video  ="";
                    String audio = "";
                    File[] subFiles = f.listFiles();
                    String fileName = "";
                    for (File subFile : subFiles) {
//                        System.out.println(subFile);
                        if (subFile.getName().contains("30080.m4s") || subFile.getName().contains("30032.m4s")) {
                            transe = true;
                            outputFile = new File(subFile.getParent() + "\\video.m4s");
                            video = outputFile.getAbsolutePath();
                            readToNewFile(subFile, outputFile);
                            System.out.println("video: " + outputFile.getAbsolutePath());
                        }
                        if (subFile.getName().contains("30280.m4s")) {
                            outputFile = new File(subFile.getParent() + "\\audio.m4s");
                            audio = outputFile.getAbsolutePath();
                            readToNewFile(subFile, outputFile);
                            System.out.println("audio: " + audio);
                        }
                        if (subFile.getName().contains("videoInfo")) {
                            String fileContent = readFromFile(subFile.getPath());
                            Gson gson = new Gson();
                            VideoInfo videoInfo = gson.fromJson(fileContent, VideoInfo.class);
                            String groupTitle = videoInfo.getGroupTitle();
                            String folderName = subFile.getParent().substring(0, subFile.getParent().lastIndexOf('\\')) + "\\" + groupTitle;
                            File folder = new File(folderName);
                            if (!folder.exists()) {
                                folder.mkdir();
                            }
                            fileName = folderName + "\\" + videoInfo.getTitle();
                        }
                    }
                    if (!new File(fileName).exists()) {
                        cmd = "ffmpeg -i " + video + " -i " + audio + " -codec copy \"" + fileName + ".mp4\"";
                        if (transe) {
                            try {
                                Runtime.getRuntime().exec(cmd);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        System.out.println(cmd);
                    }
                }
            }
        }
    }
}
