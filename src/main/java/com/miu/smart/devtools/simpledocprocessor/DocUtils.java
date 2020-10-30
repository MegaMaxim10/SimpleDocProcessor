/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.miu.smart.devtools.simpledocprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

/**
 *
 * @author Ndadji Maxime
 */
public class DocUtils {
    public static String getFileContent(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        StringBuilder fileContents = new StringBuilder((int)file.length());        

        try (Scanner scanner = new Scanner((Readable) new BufferedReader(new FileReader(file)))) {
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                fileContents.append(line);
                if(scanner.hasNextLine())
                    fileContents.append(System.lineSeparator());
            }
            return fileContents.toString();
        }
    }
    
    public static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File filesList[] = folder.listFiles();
            for(File file : filesList) {
                if (file.isDirectory()) {
                    DocUtils.deleteFolder(file);
                } else {
                    file.delete();
                }
            }
            folder.delete();
        }
    }
}
