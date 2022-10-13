package com.android.server;

import com.android.server.SystemService;
import android.content.Context;
import android.util.Log;
import android.os.Environment;
import android.os.IPrivateWalletService;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.UUID;
import org.web3j.crypto.WalletUtils;



public class PrivateWalletService extends IPrivateWalletService.Stub {
    private static final String TAG = "PrivateWalletService";
    private static WalletService instance;
    private final String walletPath;

    
    public PrivateWalletService() {
        super();
        Log.v(TAG, "PrivateWalletService, onCreate");
        walletPath = Environment.getDataDirectory().getAbsolutePath();
    }

    public void createWallet() {
        try {
            // TODO: Check if wallet exists already
            String fileName = WalletUtils.generateNewWalletFile(
            "password",
            new File(walletPath+"/wallet_file.json"));
            System.out.println(fileName);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
