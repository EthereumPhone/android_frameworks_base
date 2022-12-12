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
import org.web3j.crypto.*;
import android.content.Intent;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;
import java.security.Provider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.Security;
import java.io.FileWriter;

public class PrivateWalletService extends IPrivateWalletService.Stub {
    private static final String TAG = "PrivateWalletService";
    private static WalletService instance;
    private String walletPath;
    private ArrayList<String> allSessions;
    private HashMap<String, String> allRequests;
    private String dataDir;
    private Credentials credentials;
    private Web3j web3j;

    public PrivateWalletService() {
        super();
        Log.v(TAG, "PrivateWalletService, onCreate");
        dataDir = Environment.getDataDirectory().getAbsolutePath();
        Provider provider = setupBouncyCastle();
        try {
            if (!doesWalletExist()) {
                createWallet();
                credentials = WalletUtils.loadCredentials(
                        "password",
                        walletPath);
            } else {
                loadWalletPath();
                credentials = WalletUtils.loadCredentials(
                        "password",
                        walletPath);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        web3j = Web3j.build(new HttpService());

        removeBouncyCastle(provider);

    }

    private Provider setupBouncyCastle() {
        final Provider provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (provider == null) {
            // Web3j will set up the provider lazily when it's first used.
            return provider;
        }
        if (provider.getClass().equals(BouncyCastleProvider.class)) {
            // BC with same package name, shouldn't happen in real life.
            return provider;
        }
        // Android registers its own BC provider. As it might be outdated and might not
        // include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and
        // because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        return provider;
    }

    private void removeBouncyCastle(Provider provider) {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        Security.insertProviderAt(provider, 1);
    }

    private boolean doesWalletExist() {
        File file = new File(dataDir, "wallet_path.txt");
        if (!file.exists()) {
            return false;
        }
        return true;
    }

    private void loadWalletPath() {
        try {
            File file = new File(dataDir, "wallet_path.txt");
            Scanner scanner = new Scanner(file);
            walletPath = scanner.useDelimiter("\\Z").next();
            scanner.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public void createWallet() {
        Log.d(TAG, "Running create Wallet");
        try {
            String fileName = WalletUtils.generateNewWalletFile(
                    "password",
                    new File(dataDir));
            System.out.println(fileName);
            File file = new File(dataDir, "wallet_path.txt");
            FileWriter myWriter = new FileWriter(file);
            myWriter.write(new File(dataDir, fileName).getAbsolutePath());
            myWriter.close();
            walletPath = new File(dataDir, fileName).getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pushDecision(String requestId, String response) {
        loadDatabase();
        allRequests.put(requestId, response);
        saveDatabase();
    }

    public void sendTransaction(String requestId, String to, String value, String data, String nonce, String gasPrice,
            String gasAmount, int chainId) {
        try {
            loadDatabase();
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    new BigInteger(nonce), new BigInteger(gasPrice), new BigInteger(gasAmount), to,
                    new BigInteger(value), data);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            allRequests.put(requestId, hexValue);
            saveDatabase();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    public void signMessage(String requestId, String message) { // type = true for personal sign
        loadDatabase();
        boolean type = false;
        if (type) {
            // Use personal_sign
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            byte[] personalMessage = Hash.sha3(messageBytes);
            byte[] signedMessage = Sign.signMessage(personalMessage, credentials.getEcKeyPair(), false);
            String hexValue = Numeric.toHexString(signedMessage);
            allRequests.put(requestId, hexValue);
            saveDatabase();
        } else {
            // Use eth_sign
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            byte[] signedMessage = Sign.signMessage(messageBytes, credentials.getEcKeyPair(), false);
            String hexValue = Numeric.toHexString(signedMessage);
            allRequests.put(requestId, hexValue);
            saveDatabase();
        }
        
    }

    public void getAddress(String requestId) {
        loadDatabase();
        allRequests.put(requestId, Keys.toChecksumAddress(credentials.getAddress()));
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
