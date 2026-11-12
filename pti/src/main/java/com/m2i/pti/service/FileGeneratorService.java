package com.m2i.pti.service;


import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FileGeneratorService {
    private static final String DIRECTORY = "src/main/resources/codes/";
    private static final String FORMAT = ".txt";
    private static final String FORMAT_CSV = ".csv";
    private static final String CSV_SEPARATOR = ";";

    public File generateFile(List<String> codes, String fileName) {
        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            File file = new File(DIRECTORY + fileName + FORMAT);
            file.setWritable(false);
            //   Set<PosixFilePermission> permissions= EnumSet.of(PosixFilePermission.OWNER_READ);
            //   Files.setPosixFilePermissions(file.toPath(),permissions);
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);
            int size = codes.size();
            for (String code : codes) {
                bw.append(code + "\n");
            }
            return file;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Génère un fichier csv, à partir d'un objet java( bean )
     *
     * @param data
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static String produceCsvData(Object[] data, String[] attrs) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, InvocationTargetException {
        if (data.length == 0) {
            return "";
        }
        Class classType = data[0].getClass();
        StringBuilder builder = new StringBuilder();
        Method[] methods = classType.getDeclaredMethods();
        List<Method> methodList = new ArrayList<>();
        int verif = 0;

        if (attrs != null && attrs.length > 0) {

            for (String attr : attrs) {
                builder.append(Character.toLowerCase(attr.charAt(0))).append(attr.substring(1)).append(CSV_SEPARATOR);
            }

            //builder.deleteCharAt(builder.length() - 1);
            builder.append('\n');

            for (String attr : attrs) {
                Optional<Method> m = Arrays.stream(methods).filter(method -> method.getName().toUpperCase().endsWith(attr.toUpperCase())).findFirst();
                if (m.isPresent()) {
                    methodList.add(m.get());
                }
            }
            for (Object d : data) {
                for (Method m : methodList) {
                    if (m.getParameterTypes().length == 0) {
                        //if (m.getName().startsWith("get") || m.getName().startsWith("is")) {
                        final Object obj = m.invoke(d);
                        builder.append(obj != null ? obj.toString() : "").append(CSV_SEPARATOR);
                        verif++;
                        //  }
                    }
                }
                builder.append('\n');
            }

        } else {
            for (Method m : methods) {
                if (m.getParameterTypes().length == 0) {
                    if (m.getName().startsWith("get")) {//La première lettre de l'attribut est en minuscule
                        final String attr = m.getName().substring(3);
                        builder.append(Character.toLowerCase(attr.charAt(0))).append(attr.substring(1)).append(CSV_SEPARATOR);
                    } else if (m.getName().startsWith("is")) {
                        final String attr = m.getName().substring(2);
                        builder.append(Character.toLowerCase(attr.charAt(0))).append(attr.substring(1)).append(CSV_SEPARATOR);
                    }
                }
            }
            // builder.deleteCharAt(builder.length() - 1);
            builder.append('\n');
            for (Object d : data) {
                for (Method m : methods) {
                    if (m.getParameterTypes().length == 0) {
                        if (m.getName().startsWith("get") || m.getName().startsWith("is")) {
                            final Object obj = m.invoke(d);
                            builder.append(obj != null ? obj.toString() : "").append(CSV_SEPARATOR);
                        }
                    }
                }
                builder.append('\n');
            }
        }
        // builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }


    public static File generateCSV(File csvFileName, Object[] data, String[] attrs) {
        OutputStreamWriter fw = null;
        try {
            fw = new OutputStreamWriter(new FileOutputStream(csvFileName), StandardCharsets.UTF_8);
            if (!csvFileName.exists())
                csvFileName.createNewFile();
            fw.write(produceCsvData(data, attrs));
            fw.flush();
        } catch (Exception e) {
            System.out.println("Error while generating csv from data. Error message : " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e) {
                }
                fw = null;
            }
        }
        return csvFileName;
    }

    /**
     * Génère un fichier csv, à partir d'un objet java( bean )
     *
     * @param data
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private static String produceCsvData(Object[] data) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (data.length == 0) {
            return "";
        }

        Class classType = data[0].getClass();
        StringBuilder builder = new StringBuilder();

        Method[] methods = classType.getDeclaredMethods();
        for (Method m : methods) {
            if (m.getParameterTypes().length == 0) {
                if (m.getName().startsWith("get")) {//La première lettre de l'attribut est en minuscule
                    final String attr = m.getName().substring(3);
                    builder.append(Character.toLowerCase(attr.charAt(0))).append(attr.substring(1)).append(CSV_SEPARATOR);
                } else if (m.getName().startsWith("is")) {
                    final String attr = m.getName().substring(2);
                    builder.append(Character.toLowerCase(attr.charAt(0))).append(attr.substring(1)).append(CSV_SEPARATOR);
                }
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append('\n');
        for (Object d : data) {
            for (Method m : methods) {
                if (m.getParameterTypes().length == 0) {
                    if (m.getName().startsWith("get") || m.getName().startsWith("is")) {
                        final Object obj = m.invoke(d);
                        builder.append(obj != null ? obj.toString() : "").append(CSV_SEPARATOR);
                    }
                }
            }
            builder.append('\n');
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }


    public static boolean generateCSV(File csvFileName, Object[] data) {
        OutputStreamWriter fw = null;
        try {
            fw = new OutputStreamWriter(new FileOutputStream(csvFileName), StandardCharsets.UTF_8);
            if (!csvFileName.exists())
                csvFileName.createNewFile();
            fw.write(produceCsvData(data));
            fw.flush();
        } catch (Exception e) {
            System.out.println("Error while generating csv from data. Error message : " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (Exception e) {
                }
                fw = null;
            }
        }
        return true;
    }

}
