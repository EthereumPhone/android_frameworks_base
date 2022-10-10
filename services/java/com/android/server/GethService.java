package com.android.server;

import geth.*;
import com.android.server.SystemService;
import android.content.Context;
import android.util.Log;
import android.os.Environment;
import android.os.IGethService;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileNotFoundException;


public class GethService extends IGethService.Stub {
    private static final String TAG = "GethService";
    private static GethService instance;
    private Node node;
    private String dataDir;
    
    public GethService() {
        super();
        dataDir = Environment.getDataDirectory().getAbsolutePath();
        Log.v(TAG, "GethNode, onCreate" + dataDir);

        // If file doesnt exist, create it
        if(!doesFileExist(dataDir+"/currentStatus.txt")) {
            try {
                File file = new File(dataDir+"/currentStatus.txt");
                file.createNewFile();
                FileWriter myWriter = new FileWriter(dataDir+"/currentStatus.txt");
                myWriter.write("true");
                myWriter.close();
            } catch(IOException exception) {
                exception.printStackTrace();
            }
        }

        if (wantToStart(dataDir+"/currentStatus.txt")) {
            // Setting Verbosity to 4 to see what is happening
            try {
                Geth.setVerbosity(4);
                NodeConfig nodeConfig = Geth.newNodeConfig();
                node = Geth.newNode(dataDir+"/.ethNode", nodeConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                node.start();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.v(TAG, "GethNode, successfully started :)");
        } else {
            Log.v(TAG, "GethNode, not started :)");
        }

        
    }

    public String getEnodeURL() {
        return node.getNodeInfo().getEnode();
    }

    public void shutdownGeth() {
        try {
            node.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        savePreference(dataDir+"/currentStatus.txt", false);
        Log.v(TAG, "GethNode, successfully stopped :)");
    }

    public void shutdownWithoutPreference() {
        try {
            node.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(TAG, "GethNode, successfully stopped. Without preferences :)");
    }
    
    public void startGeth() {
        try {
            Geth.setVerbosity(4);
            NodeConfig nodeConfig = Geth.newNodeConfig();
            node = Geth.newNode(Environment.getDataDirectory()+"/.ethNode", nodeConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            node.start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        savePreference(dataDir+"/currentStatus.txt", true);
        Log.v(TAG, "GethNode, successfully started :)");
    }

    public boolean doesFileExist(String filePathString) {
        File f = new File(filePathString);
        return f.exists() && !f.isDirectory();
    }

    public boolean wantToStart(String filename) {
        if(!doesFileExist(filename)) {
            try {
                File file = new File(filename);
                file.createNewFile();
                FileWriter myWriter = new FileWriter(filename);
                myWriter.write("true");
                myWriter.close();
            } catch(IOException exception) {
                exception.printStackTrace();
            }
            
            return true;
        }
        String content = "true";
        try {
            content = new Scanner(new File(filename)).useDelimiter("\\Z").next();
        } catch(FileNotFoundException exception) {
            exception.printStackTrace();
        }

        if (content.equals("true")) {
            return true;
        } else if (content.equals("false")) {
            return false;
        }

        return true;
    }

    public void savePreference(String filename, boolean preference) {
        try {
            FileWriter myWriter = new FileWriter(filename);
            if (preference) {
                myWriter.write("true");
            } else {
                myWriter.write("false");
            }
            myWriter.close();
        } catch(IOException exception) {
            exception.printStackTrace();
        }
        
    }
    
}
