package com.android.server;
import geth.Node;
import geth.NodeConfig;
import geth.Geth;
import android.os.IGethService;
import com.android.server.SystemService;
import android.content.Context;
import android.util.Log;

public class GethService extends SystemService{
    private static final String TAG = "GethService";
    private static GethService instance;
    private Node node;
    
    public GethService(Context con) {
        super(con);
    }
    
    @Override
    public void onStart() {
        Log.v(TAG, "GethNode, onStart");
        Log.v(TAG, "GethNode, the path: " + System.getProperty("user.dir"));
        // Create Nodeconfig
        NodeConfig nodeConfig = Geth.newNodeConfig();
        // Setting Verbosity to 4 to see what is happening
        Geth.setVerbosity(4);
        try {
            node = Geth.newNode("/.ethNode", nodeConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            node.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
