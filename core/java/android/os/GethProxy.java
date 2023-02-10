package android.os;
/**
 * /framework/base/core/java/android/os/GethProxy.java
 * It will be available in framework through import android.os.GethProxy;
 * Use this Singleton class to call the functionality of GethService
 * @author mhaas.eth
 */
import android.annotation.NonNull;
import android.util.Log;
public class GethProxy {
    private static final String TAG = "GethProxy";
    private static GethProxy myProxy;
    private final IGethService mIMyService;
    /**
     * Use {@link #getmyProxy} to get the myProxy instance.
     */
    GethProxy(IGethService myService) {
        if (myService == null) {
            throw new IllegalArgumentException("my service is null");
        }
        mIMyService = myService;
    }
    /** Get a handle to the MyService.
     * @return the MyService, or null.
     */
    @NonNull
    public static GethProxy getGethProxy() {
        Object mThingLock = new Object();
        synchronized (mThingLock) {
            if (myProxy == null) {
                IBinder binder = android.os.ServiceManager.getService("geth");
                if (binder != null) {
                    IGethService managerService = IGethService.Stub.asInterface(binder);
                    myProxy = new GethProxy(managerService);
                } else {
                    Log.e(TAG, "Service binder is null");
                }
            }
            return myProxy;
        }
        
    }

    @NonNull
    public String getEnodeUrl(){
        try {
            return mIMyService.getEnodeURL();
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @NonNull
    public void shutdownGeth(){
        try {
            mIMyService.shutdownGeth();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public void shutdownWithoutPreference(){
        try {
            mIMyService.shutdownWithoutPreference();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public void startGeth(){
        try {
            mIMyService.startGeth();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public String getCurrentClient(){
        try {
            return mIMyService.getCurrentClient();
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @NonNull
    public void changeClient(@NonNull String client){
        try {
            mIMyService.changeClient(client);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public boolean isRunning() {
        try {
            return mIMyService.isRunning();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get the binder of IMyService.
     
    public GethService getMyService() {
        return mIMyService;
    }
    */
}