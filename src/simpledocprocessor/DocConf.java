/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpledocprocessor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 *
 * @author Ndadji Maxime
 */
public class DocConf {
    private HashMap<String, String> variables;
    private ArrayList<String> folderPriority;
    private HashMap<String, String> globalConfiguration;
    private ArrayList<String> excludedFolders;

    public DocConf() {
        this.variables = new HashMap<>();
        this.folderPriority = new ArrayList<>();
        this.globalConfiguration = new HashMap<>();
        this.excludedFolders = new ArrayList<>();
    }

    public DocConf(HashMap<String, String> variables, ArrayList<String> folderPriority, HashMap<String, String> globalConfiguration, ArrayList<String> excludedFolders) {
        this.variables = variables;
        this.folderPriority = folderPriority;
        this.globalConfiguration = globalConfiguration;
        this.excludedFolders = excludedFolders;
    }

    public HashMap<String, String> getVariables() {
        return variables;
    }

    public void setVariables(HashMap<String, String> variables) {
        this.variables = variables;
    }

    public ArrayList<String> getFolderPriority() {
        return folderPriority;
    }

    public void setFolderPriority(ArrayList<String> folderPriority) {
        this.folderPriority = folderPriority;
    }

    public HashMap<String, String> getGlobalConfiguration() {
        return globalConfiguration;
    }

    public void setGlobalConfiguration(HashMap<String, String> globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }

    public ArrayList<String> getExcludedFolders() {
        return excludedFolders;
    }

    public void setExcludedFolders(ArrayList<String> excludedFolders) {
        this.excludedFolders = excludedFolders;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.variables);
        hash = 37 * hash + Objects.hashCode(this.folderPriority);
        hash = 37 * hash + Objects.hashCode(this.globalConfiguration);
        hash = 37 * hash + Objects.hashCode(this.excludedFolders);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DocConf other = (DocConf) obj;
        if (!Objects.equals(this.variables, other.variables)) {
            return false;
        }
        if (!Objects.equals(this.folderPriority, other.folderPriority)) {
            return false;
        }
        if (!Objects.equals(this.globalConfiguration, other.globalConfiguration)) {
            return false;
        }
        return Objects.equals(this.excludedFolders, other.excludedFolders);
    }
    
    public static DocConf getFromFile(String filePath) {
        try {
            String fileContent = DocUtils.getFileContent(filePath);
            final GsonBuilder builder = new GsonBuilder();
            final Gson gson = builder.create();
            return gson.fromJson(fileContent, DocConf.class);
        } catch (FileNotFoundException ex) {
            return null;
        }
    }
    
    public String toJson() {
        String json = null;
    	final GsonBuilder builder = new GsonBuilder();
        final Gson gson = builder.create();
        json = gson.toJson(this, DocConf.class);
    	return json;
    }
}
