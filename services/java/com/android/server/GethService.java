package com.android.server;

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
    private String dataDir;
    private Process mainProcess;
    private ProcessBuilder builder;

    public GethService() {
        super();
        dataDir = Environment.getDataDirectory().getAbsolutePath();
        Log.v(TAG, "GethNode, onCreate" + dataDir);

        builder = new ProcessBuilder("/system/bin/nimbus_verified_proxy", "--trusted-block-root=0x474053533033800a60b4676c5d7f36bd21ff7f193ee82fcc59a95a645c18406b", "--web3-url=https://eth-mainnet.g.alchemy.com/v2/wZcbHMBl1Gt4HaXho1M_-4ZcBNTEE0zM");

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
                mainProcess = builder.start();
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
        if (mainProcess != null) {
            mainProcess.destroy();
            mainProcess = null;
        }
        Log.v(TAG, "GethNode, successfully stopped :)");
    }

    public void shutdownWithoutPreference() {
        if (mainProcess != null) {
            mainProcess.destroy();
            mainProcess = null;
        }
        Log.v(TAG, "GethNode, successfully stopped. Without preferences :)");
    }

    public void startGeth() {
        try {
            if (mainProcess == null) {
                mainProcess = builder.start();
            }
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
