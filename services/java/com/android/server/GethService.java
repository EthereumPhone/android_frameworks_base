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
    private Runnable startRunnable;
    private Runnable stopRunnable;

    static {
        System.loadLibrary("helios");
    }

    public GethService() {
        super();
        dataDir = Environment.getDataDirectory().getAbsolutePath();
        Log.v(TAG, "GethNode, onCreate" + dataDir);

        startRunnable = new Runnable(){
            public void run(){
                startClient();
            }
        };

        stopRunnable = new Runnable(){
            public void run(){
                stopClient();
            }
        };

        // If file doesnt exist, create it
        if(!doesFileExist(dataDir+"/currentStatus.txt")) {
            try {
                File file = new File(dataDir+"/currentStatus.txt");
                file.createNewFile();
                FileWriter myWriter = new FileWriter(dataDir+"/currentStatus.txt");
                myWriter.write("false");
                myWriter.close();
            } catch(IOException exception) {
                exception.printStackTrace();
            }
        }

        if (wantToStart(dataDir+"/currentStatus.txt")) {
            // Setting Verbosity to 4 to see what is happening
            try {
                new Thread(startRunnable).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.v(TAG, "GethNode, successfully started :)");
        } else {
            Log.v(TAG, "GethNode, not started :)");
        }

        
    }

    public String getEnodeURL() {
        return "";
    }

    public void shutdownGeth() {
        savePreference(dataDir + "/currentStatus.txt", false);
        new Thread(stopRunnable).start();
        Log.v(TAG, "GethNode, successfully stopped :)");
    }

    public void shutdownWithoutPreference() {
        new Thread(stopRunnable).start();
        Log.v(TAG, "GethNode, successfully stopped. Without preferences :)");
    }

    public native void startClient();
    public native void stopClient();

    public void startGeth() {
        try {
            new Thread(startRunnable).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        savePreference(dataDir + "/currentStatus.txt", true);
        Log.v(TAG, "GethNode, successfully started :)");
    }

    public boolean doesFileExist(String filePathString) {
        File f = new File(filePathString);
        return f.exists() && !f.isDirectory();
    }

    public boolean wantToStart(String filename) {
        if (!doesFileExist(filename)) {
            try {
                File file = new File(filename);
                file.createNewFile();
                FileWriter myWriter = new FileWriter(filename);
                myWriter.write("true");
                myWriter.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            return true;
        }
        String content = "true";
        try {
            Scanner scanner = new Scanner(new File(filename));
            content = scanner.useDelimiter("\\Z").next();
            scanner.close();
        } catch (FileNotFoundException exception) {
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
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }

}
