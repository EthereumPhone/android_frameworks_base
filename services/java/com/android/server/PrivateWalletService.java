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
import java.nio.ByteBuffer;

public class PrivateWalletService extends IPrivateWalletService.Stub {
    private static final String TAG = "PrivateWalletService";
    private static WalletService instance;
    private String walletPath;
    private String dataDir;
    private Credentials credentials;
    private Web3j web3j;
    private int chainId = 1;
    private SharedState sharedState;

    public PrivateWalletService(SharedState sharedState) {
        super();
        Log.v(TAG, "PrivateWalletService, onCreate");
        dataDir = Environment.getDataDirectory().getAbsolutePath();
        this.sharedState = sharedState;
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
            if (doesFileExist(dataDir+"savedChainId.txt")) {
                Scanner myReader = new Scanner(new File(dataDir+"savedChainId.txt"));
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    chainId = Integer.parseInt(data);
                }
                myReader.close();
            } else {
                File file = new File(dataDir, "savedChainId.txt");
                FileWriter myWriter = new FileWriter(file);
                myWriter.write(Integer.toString(chainId));
                myWriter.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        web3j = Web3j.build(new HttpService());

        removeBouncyCastle(provider);

    }

    private boolean doesFileExist(String filePathString) {
        File f = new File(filePathString);
        return f.exists() && !f.isDirectory();
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

    public void getChainId(String requestId) {
        sharedState.fulfillRequest(requestId, Integer.toString(chainId));
    }

    public void changeChainId(int chainId) {
        this.chainId = chainId;
        // Save chainId to file
        savePreference(dataDir+"savedChainId.txt", chainId);
    }

    public void savePreference(String filename, int saveChainId) {
        try {
            FileWriter myWriter = new FileWriter(filename);
            myWriter.write(Integer.toString(saveChainId));
            myWriter.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void pushDecision(String requestId, String response) {
        sharedState.fulfillRequest(requestId, response);
    }

    public void sendTransaction(String requestId, String to, String value, String data, String nonce, String gasPrice,
            String gasAmount) {
        try {
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    new BigInteger(nonce), new BigInteger(gasPrice), new BigInteger(gasAmount), to,
                    new BigInteger(value), data);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            sharedState.fulfillRequest(requestId, hexValue);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void signMessage(String requestId, String message, String type) { // type = true for personal sign
        Log.v(TAG, "PrivateWalletService, signMessage. message: " + message + ", type: " + type + ", requestId: "
                + requestId);
        try {
            if (type.equals("pesonal_sign_hex")) {
                Log.v(TAG, "PrivateWalletService, signMessage, pesonal_sign_hex");
                // Use personal_sign
                byte[] messageBytes = hexToString(message.substring(2)).getBytes(StandardCharsets.UTF_8);
                Sign.SignatureData signature = Sign.signPrefixedMessage(messageBytes, credentials.getEcKeyPair());
                String r = Numeric.toHexString(signature.getR());
                String s = Numeric.toHexString(signature.getS()).substring(2);
                String v = Numeric.toHexString(signature.getV()).substring(2);
                String hexValue = new StringBuilder(r).append(s).append(v).toString();
                sharedState.fulfillRequest(requestId, hexValue);
                Log.v(TAG, "PrivateWalletService, signMessage, pesonal_sign_hex, hexValue: " + hexValue);
            } else if(type.equals("personal_sign")) {
                // Sign using personal_sign
    
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
    
                Sign.SignatureData signature = Sign.signPrefixedMessage(messageBytes, credentials.getEcKeyPair());
    
                byte[] retval = new byte[65];
                System.arraycopy(signature.getR(), 0, retval, 0, 32);
                System.arraycopy(signature.getS(), 0, retval, 32, 32);
                System.arraycopy(signature.getV(), 0, retval, 64, 1);
    
                String signedMessage = Numeric.toHexString(retval);
                sharedState.fulfillRequest(requestId, signedMessage);
            } else if (type.equals("eth_signTypedData")) {
                try {
                    StructuredDataEncoder dataEncoder = new StructuredDataEncoder(message);
                    byte[] hashStructuredData = dataEncoder.hashStructuredData();
                    
                    Sign.SignatureData signature = Sign.signMessage(hashStructuredData, credentials.getEcKeyPair(), false);
    
                    ByteBuffer sigBuffer = ByteBuffer.allocate(signature.getR().length + signature.getS().length + 1);
                    sigBuffer.put(signature.getR());
                    sigBuffer.put(signature.getS());
                    sigBuffer.put(signature.getV());
    
                    String signedMessage = Numeric.toHexString(sigBuffer.array());
    
                    sharedState.fulfillRequest(requestId, signedMessage);
                }catch (Exception e) {
                    e.printStackTrace();
                    sharedState.fulfillRequest(requestId, "error");
                }
                
            }
        } catch(Exception exception) {
            exception.printStackTrace();
            sharedState.fulfillRequest(requestId, "error");
        }
    }

    public String signTypedData(String message) {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        Sign.SignatureData signature = Sign.signPrefixedMessage(messageBytes, credentials.getEcKeyPair());
        byte[] retval = new byte[65];
        System.arraycopy(signature.getR(), 0, retval, 0, 32);
        System.arraycopy(signature.getS(), 0, retval, 32, 32);
        System.arraycopy(signature.getV(), 0, retval, 64, 1);
        String signedMessage = Numeric.toHexString(retval);
        return signedMessage;
    }

    public static String hexToString(String hex) {
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        // 49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            // grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            // convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            // convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }
        return sb.toString();
    }

    public void getAddress(String requestId) {
        sharedState.fulfillRequest(requestId, Keys.toChecksumAddress(credentials.getAddress()));
    }

}
