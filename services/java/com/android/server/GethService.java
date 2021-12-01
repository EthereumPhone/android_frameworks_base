import geth.*;


public class GethService extends IGethService.Stub{
    private static final String TAG = "GethService";
    private static GethService instance;
    private Node node;
    private Context ctx;
    public GethService() {
        // Create Nodeconfig
        NodeConfig nodeConfig = Geth.newNodeConfig();
        // Setting Verbosity to 4 to see what is happening
        Geth.setVerbosity(4);
        ctx = Geth.newContext();
        try {
            node = Geth.newNode(getFilesDir()+"/.ethNode", nodeConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            node.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public EthereumClient getEthClient() {
        try {
            return node.getEthereumClient();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
