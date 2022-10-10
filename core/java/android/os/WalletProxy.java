package android.os;
/**
 * /framework/base/core/java/android/os/WalletProxy.java
 * It will be available in framework through import android.os.WalletProxy;
 * Use this Singleton class to call the public functionality of WalletService
 * @author mhaas.eth
 */
import android.annotation.NonNull;
import android.util.Log;
public class WalletProxy {
    private static final String TAG = "WalletProxy";
    private static WalletProxy myProxy;
    private final IWalletService mIMyService;
    /**
     * Use {@link #getmyProxy} to get the myProxy instance.
     */
    WalletProxy(IWalletService myService) {
        if (myService == null) {
            throw new IllegalArgumentException("my service is null");
        }
        mIMyService = myService;
    }
    /** Get a handle to the MyService.
     * @return the MyService, or null.
     */
    @NonNull
    public static WalletProxy getWalletProxy() {
        Object mThingLock = new Object();
        synchronized (mThingLock) {
            if (myProxy == null) {
                IBinder binder = android.os.ServiceManager.getService("wallet_proxy");
                if (binder != null) {
                    IWalletService managerService = IWalletService.Stub.asInterface(binder);
                    myProxy = new WalletProxy(managerService);
                } else {
                    Log.e(TAG, "Service binder is null");
                }
            }
            return myProxy;
        }
        
    }

    @NonNull
    public String createSession(){
        try {
            return mIMyService.createSession();
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @NonNull
    public boolean isWalletConnected(@NonNull String session) {
        try {
            return mIMyService.isWalletConnected(session);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @NonNull
    public void connectToWallet(@NonNull String session) {
        try {
            mIMyService.connectToWallet(session);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public void sendTransaction(@NonNull String session, @NonNull String from, @NonNull String to, @NonNull String data) {
        try {
            mIMyService.sendTransaction(session, from, to, data);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public String signMessage(@NonNull String session, @NonNull String message) {
        try {
            return mIMyService.signMessage(session, message);
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

   
}