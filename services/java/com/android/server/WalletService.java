package com.android.server;

import android.content.Context;
import android.os.Environment;
import android.os.IWalletService;
import android.util.Log;
import com.android.server.SystemService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import org.web3j.crypto.WalletUtils;
import android.content.Intent;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.EOFException;

public class WalletService extends IWalletService.Stub {

  private static final String TAG = "WalletService";
  private static WalletService instance;
  private Context context;
  private String dataDir;
  private ArrayList<String> allSessions;
  private HashMap<String, String> allRequests;

  public WalletService(Context con) {
    super();
    Log.v(TAG, "WalletService, onCreate");
    dataDir = Environment.getDataDirectory().getAbsolutePath();
    context = con;
    allSessions = new ArrayList<>();
    allRequests = new HashMap<>();
    saveDatabase();
  }

  public String createSession() {
    UUID uuid = UUID.randomUUID();
    allSessions.add(uuid.toString());
    saveDatabase();
    return uuid.toString();
  }

  public boolean isWalletConnected(String session) {
    // To implement
    Log.v(TAG, "isWalletConnected, " + session);
    return allSessions.contains(session);
  }

  public void connectToWallet(String session) {
    Log.v(TAG, "connectToWallet, " + session);
  }

  public String sendTransaction(
      String session,
      String to,
      String value,
      String data,
      String nonce,
      String gasPrice,
      String gasAmount,
      int chainId) {
    Log.v(TAG, "sendTransaction, " + session);
    if (allSessions.contains(session)) {
      UUID uuid = UUID.randomUUID();
      allRequests.put(uuid.toString(), "notfulfilled");
      saveDatabase();

      Intent in = new Intent("requestToSystemUI");
      in.putExtra("method", "sendTransaction");
      in.putExtra("requestID", uuid.toString());
      in.putExtra("to", to);
      in.putExtra("value", value);
      in.putExtra("data", data);
      in.putExtra("nonce", nonce);
      in.putExtra("gasPrice", gasPrice);
      in.putExtra("gasAmount", gasAmount);
      in.putExtra("chainId", chainId);
      context.sendBroadcast(in);
      return uuid.toString();
    }
    return "no session";
  }

  public String signMessage(String session, String message, boolean type) {
    Log.v(TAG, "signMessage, " + session + ": " + message);
    if (allSessions.contains(session)) {
      UUID uuid = UUID.randomUUID();
      allRequests.put(uuid.toString(), "notfulfilled");
      saveDatabase();

      Intent in = new Intent("requestToSystemUI");
      in.putExtra("method", "signMessage");
      in.putExtra("requestID", uuid.toString());
      in.putExtra("message", message);
      in.putExtra("type", type);
      context.sendBroadcast(in);
      return uuid.toString();
    }
    return "no session";
  }

  public String hasBeenFulfilled(String requestID) {
    loadDatabase();
    if (allRequests.get(requestID) == null) {
      return "notfulfilled";
    }
    return allRequests.get(requestID);
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
    } catch (Exception e) {
      allSessions = new ArrayList<String>();
      allRequests = new HashMap<String, String>();
      saveDatabase();
      e.printStackTrace();
    }
  }

  public String getUserDecision() {
    UUID uuid = UUID.randomUUID();
    Intent in = new Intent("requestToSystemUI");
    allRequests.put(uuid.toString(), "notfulfilled");
    in.putExtra("method", "getDecision");
    in.putExtra("requestID", uuid.toString());
    context.sendBroadcast(in);
    saveDatabase();
    return uuid.toString();
  }

  public String getAddress(String session) {
    UUID uuid = UUID.randomUUID();
    Intent in = new Intent("requestToSystemUI");
    allRequests.put(uuid.toString(), "notfulfilled");
    in.putExtra("method", "getAddress");
    in.putExtra("requestID", uuid.toString());
    context.sendBroadcast(in);
    saveDatabase();
    return uuid.toString();
  }

}
