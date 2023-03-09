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
import android.content.Intent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class GethService extends IGethService.Stub {
    private static final String TAG = "GethService";
    private static GethService instance;
    private String dataDir;
    private Process mainProcess;
    private ProcessBuilder builder;
    private final String[] nimbusCommand = {"/system/bin/nimbus_verified_proxy", "--trusted-block-root=0x1bb93b69018fedbd185061dc773c5766a2e20923bf9db4edfef256099c2149c4", "--web3-url=https://eth-mainnet.g.alchemy.com/v2/wZcbHMBl1Gt4HaXho1M_-4ZcBNTEE0zM"};
    private final String heliosCommand = "./system/bin/helios";
    private String currentCommand = "nimbus";
    private Context context;
    private Thread standardOutputThread;
    private Thread errorOutputThread;

    public GethService(Context con) {
        super();
        dataDir = Environment.getDataDirectory().getAbsolutePath();
        Log.v(TAG, "GethNode, onCreate" + dataDir);
        this.context = con;

        builder = new ProcessBuilder(nimbusCommand);

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
                redirectProcessOutput(mainProcess);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.v(TAG, "GethNode, successfully started :)");
        } else {
            Log.v(TAG, "GethNode, not started :)");
        }

        
    }

    public void redirectProcessOutput(Process process) {
        InputStream inputStream = process.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader buffer = new BufferedReader(reader);
      
        standardOutputThread = new Thread(() -> {
          try {
            String line;
            while ((line = buffer.readLine()) != null) {
              System.out.println(TAG +": " +line);
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
        standardOutputThread.start();
      
        InputStream errorStream = process.getErrorStream();
        InputStreamReader errorReader = new InputStreamReader(errorStream);
        BufferedReader errorBuffer = new BufferedReader(errorReader);
      
        errorOutputThread = new Thread(() -> {
          try {
            String line;
            while ((line = errorBuffer.readLine()) != null) {
                System.out.println(TAG +": " +line);
            }
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
        errorOutputThread.start();
    }
    

    public String getEnodeURL() {
        return "";
    }

    public void shutdownGeth() {
        savePreference(dataDir + "/currentStatus.txt", false);
        if (mainProcess != null) {
            mainProcess.destroy();
            mainProcess = null;
            updateViews();
        }
        Log.v(TAG, "GethNode, successfully stopped :)");
    }

    public void shutdownWithoutPreference() {
        if (mainProcess != null) {
            mainProcess.destroy();
            mainProcess = null;
            updateViews();
        }
        Log.v(TAG, "GethNode, successfully stopped. Without preferences :)");
    }

    public void startGeth() {
        try {
            if (mainProcess == null) {
                mainProcess = builder.start();
                redirectProcessOutput(mainProcess);
                updateViews();
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

    public void changeClient(String client) {
        if (mainProcess != null) {
            mainProcess.destroy();
            mainProcess = null;
            updateViews();
        }
        if (client.equals("Nimbus")) {
            builder = new ProcessBuilder(nimbusCommand);
            currentCommand = "Nimbus";
        } else if (client.equals("Helios")) {
            builder = new ProcessBuilder(heliosCommand);
            currentCommand = "Helios";
        }
    }

    public String getCurrentClient() {
        return currentCommand;
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

    public boolean isRunning() {
        if (mainProcess != null) {
            return mainProcess.isAlive();
        }
        return false;
    }

    private void updateViews() {
        Intent intent = new Intent("NODE_UPDATE");
        context.sendBroadcast(intent);
    }

}
