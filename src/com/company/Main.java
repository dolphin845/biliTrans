package com.company;

import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

public class Main {

    private static final String folderSpecialCharacter = "|囧⊙●○⊕◎Θ⊙¤㈱㊣★☆♀◆◇◣◢◥▲▼△▽⊿◤ ◥ ▂ ▃ ▄ ▅ ▆ ▇ █ █ ■ ▓《》/......（）【】";
    private static final String fileSpecialCharacter = "|囧⊙●○⊕◎Θ⊙¤㈱㊣★☆♀◆◇◣◢◥▲▼△▽⊿◤ ◥ ▂ ▃ ▄ ▅ ▆ ▇ █ █ ■ ▓/《》（）【】";

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

    public static String deleteSpecialCharacter(String strDelete, targetType type) {
        String str = "";
        String special = "";
        if (type == targetType.FOLDER) {
            special = folderSpecialCharacter;
        } else {
            special = fileSpecialCharacter;
        }
        try {
            StringTokenizer st1 = new StringTokenizer(strDelete);

            while (st1.hasMoreTokens()) {
                str = str + st1.nextToken(special);
            }
            str = str.trim();
        } catch (Exception e) {
            System.out.println(e);
        }
        return str;
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
        while ((readByte = dataInputStream.readByte()) == 0x30) {
            zeroSize++;
        }
        dataOutputStream.write(readByte);
        byte[] byteBuff = new byte[1024];
        while (dataInputStream.read(byteBuff) != -1) {
            dataOutputStream.write(byteBuff);
            dataOutputStream.flush();
        }
//        byte[] bytes = new byte[(int) inputFile.length() - zeroSize - 1];
//
//        dataInputStream.read(bytes);
//        dataOutputStream.write(bytes);

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
                    String video = "";
                    String audio = "";
                    File[] subFiles = f.listFiles();
                    String fileName = "";
                    for (File subFile : subFiles) {
//                        System.out.println(subFile);
                        if (subFile.getName().contains("30080.m4s") || subFile.getName().contains("30032.m4s") || subFile.getName().contains("30064.m4s") || subFile.getName().contains("30011.m4s") || subFile.getName().contains("30016.m4s")) {
                            transe = true;
                            outputFile = new File(subFile.getParent() + File.separator + "video.m4s");
                            video = outputFile.getAbsolutePath();
                            readToNewFile(subFile, outputFile);
                            System.out.println("video: " + outputFile.getAbsolutePath());
                        }
                        if (subFile.getName().contains("30280.m4s") || subFile.getName().contains("30216.m4s")) {
                            outputFile = new File(subFile.getParent() + File.separator + "audio.m4s");
                            audio = outputFile.getAbsolutePath();
                            readToNewFile(subFile, outputFile);
                            System.out.println("audio: " + audio);
                        }
                        if (subFile.getName().contains("videoInfo")) {
                            String fileContent = readFromFile(subFile.getPath());
                            Gson gson = new Gson();
                            VideoInfo videoInfo = gson.fromJson(fileContent, VideoInfo.class);
                            String groupTitle = videoInfo.getGroupTitle();
                            String folderName = subFile.getParent().substring(0, subFile.getParent().lastIndexOf(File.separator)) + File.separator + groupTitle;
                            folderName = deleteSpecialCharacter(folderName, targetType.FOLDER);
                            File folder = new File(folderName);
                            if (!folder.exists()) {
                                folder.mkdir();
                            }
                            if (groupTitle.equals(videoInfo.getTitle())) {
                                fileName = folderName + File.separator + deleteSpecialCharacter(videoInfo.getTitle(), targetType.FILE);
                            } else {
                                fileName = folderName + File.separator + "P" + videoInfo.getP() + " " + deleteSpecialCharacter(videoInfo.getTitle(), targetType.FILE);
                            }
                        }
                    }
                    if (!new File(fileName).exists()) {
                        cmd = "ffmpeg -i " + video + " -i " + audio + " -codec copy \"" + fileName + ".mp4\"";
                        if (transe) {
                            try {
                                String check = CheckSystem.check();
                                if (check.equals("Mac")) {
                                    ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", "ffmpeg -i " + video + " -i " + audio + " -codec copy \"" + fileName + ".mp4\"");
                                    pb.directory(new File(file.getPath()));
                                    Process proc = pb.start();
                                } else {
                                    Runtime.getRuntime().exec(cmd);
                                }
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
