package android.os;
/**
 * /framework/base/core/java/android/os/WalletProxy.java
 * It will be available in framework through import android.os.WalletProxy;
 * Use this Singleton class to call the functionality of WalletService
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
                IBinder binder = android.os.ServiceManager.getService("wallet");
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
    public String sendTransaction(@NonNull String session, @NonNull String to, @NonNull String value, @NonNull String data, @NonNull String nonce, @NonNull String gasPrice, @NonNull String gasAmount, @NonNull int chainId){
        try {
            return mIMyService.sendTransaction(session, to, value, data, nonce, gasPrice, gasAmount, chainId);
        } catch(Exception e) {  
            e.printStackTrace();
            return "error";
        }
    }

    @NonNull
    public String signMessage(@NonNull String session, @NonNull String message, @NonNull Boolean type) {
        try {
            return mIMyService.signMessage(session, message, type);
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @NonNull
    public String hasBeenFulfilled(@NonNull String requestID) {
        try {
            return mIMyService.hasBeenFulfilled(requestID);
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @NonNull
    public String getUserDecision() {
        try {
            return mIMyService.getUserDecision();
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @NonNull
    public String getAddress(@NonNull String session) {
        try {
            return mIMyService.getAddress(session);
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}