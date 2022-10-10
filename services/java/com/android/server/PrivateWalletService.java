package com.android.server;

import com.android.server.SystemService;
import android.content.Context;
import android.util.Log;
import android.os.Environment;
import android.os.IWalletService;
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
    private Context context;

    
    public PrivateWalletService(Context context) {
        super();
        Log.v(TAG, "PrivateWalletService, onCreate");
        walletPath = context.getFilesDir().getAbsolutePath();
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
