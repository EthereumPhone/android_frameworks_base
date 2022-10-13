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



public class WalletService extends IWalletService.Stub {
    private static final String TAG = "WalletService";
    private static WalletService instance;

    
    public WalletService() {
        super();
        Log.v(TAG, "WalletService, onCreate");
    }

    public String createSession() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public boolean isWalletConnected(String session) {
        // To implement
        Log.v(TAG, "isWalletConnected, "+session);
        return false;
    }

    public void connectToWallet(String session) {
        Log.v(TAG, "connectToWallet, "+session);
    }

    public void sendTransaction(String session, String from, String to, String data) {
        Log.v(TAG, "sendTransaction, "+session);
    }

    public String signMessage(String session, String message) {
        Log.v(TAG, "signMessage, "+session+": "+message);
        return "TODO";
    }
    
}
