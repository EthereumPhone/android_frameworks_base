package com.android.server;

import com.android.server.SystemService;
import android.content.Context;
import android.util.Log;
import android.os.Environment;
import android.os.IPrivateWalletService;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.UUID;
import org.web3j.crypto.WalletUtils;

public class PrivateWalletService extends IPrivateWalletService.Stub {
    private static final String TAG = "PrivateWalletService";
    private static WalletService instance;
    private final String walletPath;
    private ArrayList<String> allSessions;
    private HashMap<String, String> allRequests;

    public PrivateWalletService() {
        super();
        Log.v(TAG, "PrivateWalletService, onCreate");
        walletPath = Environment.getDataDirectory().getAbsolutePath();
    }

    public void createWallet() {
        Log.d(TAG, "Running create Wallet");
        try {
            String fileName = WalletUtils.generateNewWalletFile(
                    "password",
                    new File(walletPath + "/wallet_file.json"));
            System.out.println(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pushDecision(String requestId, String response) {
        loadDatabase();
        allRequests.put(requestId, response);
        saveDatabase();
    }

    public void saveDatabase() {
        try {
            FileOutputStream fos = new FileOutputStream(dataDir + "/mydb1.fil");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(allSessions);
            oos.close();

            FileOutputStream fos2 = new FileOutputStream(dataDir + "/mydb2.fil");
            ObjectOutputStream oos2 = new ObjectOutputStream(fos2);
            oos2.writeObject(allRequests);
            oos2.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void loadDatabase() {
        try {
            FileInputStream fis = new FileInputStream(dataDir + "/mydb1.fil");
            ObjectInputStream ois = new ObjectInputStream(fis);
            allSessions = (ArrayList<String>) ois.readObject();
            ois.close();

            FileInputStream fis2 = new FileInputStream(dataDir + "/mydb2.fil");
            ObjectInputStream ois2 = new ObjectInputStream(fis2);
            allRequests = (HashMap<String, String>) ois2.readObject();
            ois2.close();
        } catch (IOException e) {
            allSessions = new ArrayList<String>();
            allRequests = new HashMap<String, String>();
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            allSessions = new ArrayList<String>();
            allRequests = new HashMap<String, String>();
            e.printStackTrace();
        }
    }

}
