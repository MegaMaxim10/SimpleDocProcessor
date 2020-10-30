/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpledocprocessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ndadji Maxime
 */
public class SimpleDocProcessor {
    private static final DocConf DEFAULT_GLOBAL_CONF = new DocConf();
    static {
        DEFAULT_GLOBAL_CONF.getGlobalConfiguration().put("confFileName", "doc.conf.json");
        DEFAULT_GLOBAL_CONF.getGlobalConfiguration().put("docFileName", "readme.md");
        DEFAULT_GLOBAL_CONF.getExcludedFolders().add("node_modules");
        DEFAULT_GLOBAL_CONF.getExcludedFolders().add("doc");
        DEFAULT_GLOBAL_CONF.getExcludedFolders().add("dist");
        DEFAULT_GLOBAL_CONF.getExcludedFolders().add(".git");
        DEFAULT_GLOBAL_CONF.getExcludedFolders().add("android");
        DEFAULT_GLOBAL_CONF.getExcludedFolders().add("e2e");
        DEFAULT_GLOBAL_CONF.getExcludedFolders().add("www");
        DEFAULT_GLOBAL_CONF.getExcludedFolders().add("build");
        DEFAULT_GLOBAL_CONF.getExcludedFolders().add("nbproject");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("[INFO] Checking requirements for file generation...");
        File file = new File("doc");
        boolean init = SimpleDocProcessor.isArg(args, "-i");
        if (!file.exists() || !file.canRead() || !file.canWrite()) {
            if (init) {
                try{
                    file = new File("doc/dist/images");
                    file.mkdirs();
                    file = new File("doc/Documentation.md");
                    file.createNewFile();
                    FileWriter myWriter = new FileWriter("doc/Documentation.md");
                    myWriter.write("{{PrintDocumentationHere}}");
                    myWriter.close();
                    
                    file = new File("doc/doc.conf.json");
                    file.createNewFile();
                    myWriter = new FileWriter("doc/doc.conf.json");
                    myWriter.write(DEFAULT_GLOBAL_CONF.toJson());
                    myWriter.close();
                } catch (Exception e) {
                    System.out.println("[ERROR] Unexpected error when creating the basic documentation folder!");
                    return;
                }
            } else {
                System.out.println("[ERROR] Missing readable and writable doc folder! Create it manually and configure it or use the -i argument to automatically generate such a basic folder!");
                return;
            }
        } else {
            file = new File("doc/Documentation.md");
            if (!file.exists() || !file.canRead()) {
                if (init) {
                    try{
                        file.createNewFile();
                        FileWriter myWriter = new FileWriter("doc/Documentation.md");
                        myWriter.write("{{PrintDocumentationHere}}");
                        myWriter.close();
                    }catch (Exception e) {
                        System.out.println("[ERROR] Unexpected error when creating the basic documentation file!");
                        return;
                    }
                }
            }
            
            file = new File("doc/doc.conf.json");
            if (!file.exists() || !file.canRead()) {
                if (init) {
                    try{
                        file.createNewFile();
                        FileWriter myWriter = new FileWriter("doc/doc.conf.json");
                        myWriter.write(DEFAULT_GLOBAL_CONF.toJson());
                        myWriter.close();
                    }catch (Exception e) {
                        System.out.println("[ERROR] Unexpected error when creating the basic configuration file!");
                        return;
                    }
                }
            }
        }
        file = new File("doc/dist/");
        if (file.exists()) {
            System.out.println("[INFO] Cleaning generation folder...");
            DocUtils.deleteFolder(file);
        }
        file = new File("doc/dist/images");
        file.mkdirs();
        System.out.println("[INFO] Scanning the project folder to generate the documentation...");
        Path copied = Paths.get("doc/dist/Documentation.md");
        Path originalPath = Paths.get("doc/Documentation.md");
        try {
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            System.out.println("[ERROR] Unexpected error: cannot copy the basic documentation file.");
            return;
        }
        
        try {
            file = new File("doc/Documentation.md");
            File folder = file.getParentFile();
            DocConf globalConf = DocConf.getFromFile("doc/doc.conf.json");
            Output output = new Output();
            output.setOutputDir(new File("doc/dist"));
            String content = DocUtils.getFileContent("doc/dist/Documentation.md");
            content = content.replaceAll("\\{\\{([ ]){1,}", "{{");
            content = content.replaceAll("([ ]){1,}\\}\\}", "}}");

            //Replace variables
            for (String key : globalConf.getVariables().keySet()) {
                content = content.replaceAll("\\{\\{\\{" + key + "\\}\\}\\}", globalConf.getVariables().get(key));
            }
            
            //Find, copy and replace images
            String rx = "(\\{\\{InsertImageHere[ ]{1,}'[a-zA-Z0-9_./ -]{1,}'\\}\\})";
            Pattern p = Pattern.compile(rx);
            Matcher m = p.matcher(content);
            while (m.find())
            {
                String imgLine = m.group(1);
                String imgPath = imgLine.replaceAll("\\{\\{InsertImageHere[ ]{1,}'", "");
                imgPath = imgPath.replaceAll("'\\}\\}", "");
                //The image is found, trying to copy it then to replace it into the content
                imgPath = folder.getAbsolutePath() + File.separator + imgPath;
                File img = new File(imgPath);
                String destRel = output.getImagesFolderName() + File.separator + new Date().getTime()+ "-" + img.getName();
                String destPath = output.getOutputDir().getAbsolutePath() + File.separator + destRel;
                if (img.exists() && img.isFile() && img.canRead() && !output.getCopiedImages().contains(img.getCanonicalPath())) {
                    copied = Paths.get(destPath);
                    originalPath = img.toPath();
                    Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
                    output.getCopiedImages().add(new File(destPath).getCanonicalPath());
                } else {
                    System.out.println("[Warning] The specified image is not found: " + imgLine + " in file " + file.getCanonicalPath());
                }
                content = content.replace(imgLine, "![" + destRel.replace('\\', '/') + "](" + destRel.replace('\\', '/') + ")");
            }
            file = new File("doc/Documentation.md");
            file = new File(file.getCanonicalPath());
            String docContent = SimpleDocProcessor.getDocContent(file.getParentFile().getParentFile(), globalConf, output);
            if (docContent == null)
                throw new Exception();
            content = content.replace("{{PrintDocumentationHere}}", docContent);
            System.out.println("[INFO] Writing the documentation ouput...");
            try{
                FileWriter myWriter = new FileWriter("doc/dist/Documentation.md");
                myWriter.write(content);
                myWriter.close();
            }catch (Exception e) {
                System.out.println("[ERROR] Unexpected error when writing the documentation ouput!");
                return;
            }
        } catch (Exception ex) {
            System.out.println("[ERROR] Unexpected error: cannot generate the documentation.");
            return;
        }
        System.out.println("[INFO] SUCCESSFUL! GOOD JOB!");
    }
    
    private static DocConf mergeConfigurations(DocConf conf1, DocConf conf2) {
        DocConf finalConf = new DocConf();
        if (conf1.getVariables() != null) {
            conf1.getVariables().keySet().forEach((key) -> {
                finalConf.getVariables().put(key, conf1.getVariables().get(key));
            });
        }
        if (conf1.getGlobalConfiguration() != null) {
            conf1.getGlobalConfiguration().keySet().forEach((key) -> {
                finalConf.getGlobalConfiguration().put(key, conf1.getGlobalConfiguration().get(key));
            });
        }
        if (conf1.getExcludedFolders()!= null) {
            conf1.getExcludedFolders().forEach((key) -> {
                finalConf.getExcludedFolders().add(key);
            });
        }
        
        if (conf2.getVariables() != null) {
            conf2.getVariables().keySet().forEach((key) -> {
                finalConf.getVariables().put(key, conf2.getVariables().get(key));
            });
        }
        if (conf2.getFolderPriority()!= null) {
            conf2.getFolderPriority().forEach((key) -> {
                finalConf.getFolderPriority().add(key);
            });
        }
        if (conf2.getGlobalConfiguration() != null) {
            conf2.getGlobalConfiguration().keySet().forEach((key) -> {
                finalConf.getGlobalConfiguration().put(key, conf2.getGlobalConfiguration().get(key));
            });
        }
        if (conf2.getExcludedFolders()!= null) {
            conf2.getExcludedFolders().forEach((key) -> {
                if (!finalConf.getExcludedFolders().contains(key))
                    finalConf.getExcludedFolders().add(key);
            });
        }
        return finalConf;
    }
    
    private static String getDocContent(File folder, DocConf globalConf, Output output) {
        if (folder != null && folder.isDirectory() && folder.canRead() && !globalConf.getExcludedFolders().contains(folder.getName())) {
            String content = "";
            String localConfPath = folder.getAbsolutePath() + File.separator + globalConf.getGlobalConfiguration().get("confFileName");
            DocConf localConf = DocConf.getFromFile(localConfPath);
            DocConf mergedConf = SimpleDocProcessor.mergeConfigurations(globalConf, localConf != null ? localConf : new DocConf());
            String docPath = folder.getAbsolutePath() + File.separator + mergedConf.getGlobalConfiguration().get("docFileName");
            try {
                content = DocUtils.getFileContent(docPath);
                content = content.replaceAll("\\{\\{([ ]){1,}", "{{");
                content = content.replaceAll("([ ]){1,}\\}\\}", "}}");
                
                //Replace variables
                for (String key : mergedConf.getVariables().keySet()) {
                    content = content.replaceAll("\\{\\{\\{" + key + "\\}\\}\\}", mergedConf.getVariables().get(key));
                }
                
                //Find, copy and replace images
                String rx = "(\\{\\{InsertImageHere[ ]{1,}'[a-zA-Z0-9_./ -]{1,}'\\}\\})";
                Pattern p = Pattern.compile(rx);
                Matcher m = p.matcher(content);
                while (m.find())
                {
                    String imgLine = m.group(1);
                    String imgPath = imgLine.replaceAll("\\{\\{InsertImageHere[ ]{1,}'", "");
                    imgPath = imgPath.replaceAll("'\\}\\}", "");
                    //The image is found, trying to copy it then to replace it into the content
                    imgPath = folder.getAbsolutePath() + File.separator + imgPath;
                    File img = new File(imgPath);
                    String destRel = output.getImagesFolderName() + File.separator + new Date().getTime()+ "-" + img.getName();
                    String destPath = output.getOutputDir().getAbsolutePath() + File.separator + destRel;
                    if (img.exists() && img.isFile() && img.canRead() && !output.getCopiedImages().contains(img.getCanonicalPath())) {
                        Path copied = Paths.get(destPath);
                        Path originalPath = img.toPath();
                        Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
                        output.getCopiedImages().add(new File(destPath).getCanonicalPath());
                    } else {
                        System.out.println("[Warning] The specified image is not found: " + imgLine + " in file " + docPath);
                    }
                    content = content.replace(imgLine, "![" + destRel.replace('\\', '/') + "](" + destRel.replace('\\', '/') + ")");
                }
            } catch (FileNotFoundException ex) {
                System.out.println("[Warning] File not found while processing " + docPath);
            } catch (IOException ex) {
                System.out.println("[Warning] Image could not be treated while processing " + docPath);
            }
            
            File filesList[] = folder.listFiles();
            for(File file : filesList) {
                if (file.isDirectory()) {
                    String subContent = SimpleDocProcessor.getDocContent(file, mergedConf, output);
                    if (subContent != null) {
                        if (content.trim().isEmpty())
                            content = subContent;
                        else
                            content += (System.lineSeparator() + subContent);
                    }
                }
            }
            return content;
        }
        return null;
    }

    private static boolean isArg(String[] args, String i) {
        boolean exists = false;
        if (args != null) {
            for (String arg : args) {
                if (arg.equalsIgnoreCase(i)) {
                    exists = true;
                    break;
                }
            }
        }
        return exists;
    }
    
    public static class Output {
        private File outputDir;
        private ArrayList<String> copiedImages;
        private String imagesFolderName = "images";

        public Output() {
            copiedImages = new ArrayList();
        }

        public File getOutputDir() {
            return outputDir;
        }

        public void setOutputDir(File outputDir) {
            this.outputDir = outputDir;
        }

        public ArrayList<String> getCopiedImages() {
            return copiedImages;
        }

        public void setCopiedImages(ArrayList<String> copiedImages) {
            this.copiedImages = copiedImages;
        }

        public String getImagesFolderName() {
            return imagesFolderName;
        }

        public void setImagesFolderName(String imagesFolderName) {
            this.imagesFolderName = imagesFolderName;
        }
    }
}
