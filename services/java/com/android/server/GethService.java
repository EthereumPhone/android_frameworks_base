package com.android.server;
import geth.*;
import android.os.IGethService;
import com.android.server.SystemService;
import android.content.Context;
import android.util.Log;
import android.os.Environment;


public class GethService extends SystemService{
    private static final String TAG = "GethService";
    private static GethService instance;
    private Node node;
    
    public GethService(Context con) {
        super(con);
        Log.v(TAG, "GethNode, onCreate");
    }
    
    @Override
    public void onStart() {
        Log.v(TAG, "GethNode, onStart" + Environment.getDataDirectory());
        //String pathList = System.getProperty("java.library.path", ".");
        // Setting Verbosity to 4 to see what is happening
        try {
            Geth.setVerbosity(4);
            NodeConfig nodeConfig = Geth.newNodeConfig();
            node = Geth.newNode(Environment.getDataDirectory()+"/.ethNode", nodeConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            node.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.v(TAG, "GethNode, successfully started :)");
        
    }
}
