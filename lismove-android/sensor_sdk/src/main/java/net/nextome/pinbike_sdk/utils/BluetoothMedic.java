package net.nextome.lismove_sdk.utils;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.List;

/**
 *
 * Utility class for checking the health of the bluetooth stack on the device by running two kinds
 * of tests: scanning and transmitting.  The class looks for specific failure codes from these
 * tests to determine if the bluetooth stack is in a bad state and if so, optionally cycle power to
 * bluetooth to try and fix the problem.  This is known to work well on some Android devices.
 *
 * The tests may be called directly, or set up to run automatically approximately every 15 minutes.
 * To set up in an automated way:
 *
 * <code>
 *   BluetoothMedic medic = BluetoothMedic.getInstance();
 *   medic.enablePowerCycleOnFailures(context);
 *   medic.enablePeriodicTests(context, BluetoothMedic.SCAN_TEST | BluetoothMedic.TRANSMIT_TEST);
 * </code>
 *
 * To set up in a manual way:
 *
 * <code>
 *   BluetoothMedic medic = BluetoothMedic.getInstance();
 *   medic.enablePowerCycleOnFailures(context);
 *   if (!medic.runScanTest(context)) {
 *     // Bluetooth stack is in a bad state
 *   }
 *   if (!medic.runTransmitterTest(context)) {
 *     // Bluetooth stack is in a bad state
 *   }
 *
 */

@SuppressWarnings("javadoc")
public class BluetoothMedic {

    /**
     * Indicates that no test should be run by the BluetoothTestJob
     */
    @SuppressWarnings("WeakerAccess")
    public static final int NO_TEST = 0;
    /**
     * Indicates that the transmitter test should be run by the BluetoothTestJob
     */
    @SuppressWarnings("WeakerAccess")
    public static final int TRANSMIT_TEST = 2;
    /**
     * Indicates that the bluetooth scan test should be run by the BluetoothTestJob
     */
    @SuppressWarnings("WeakerAccess")
    public static final int SCAN_TEST = 1;
    private static final String TAG = BluetoothMedic.class.getSimpleName();
    @Nullable
    private BluetoothAdapter mAdapter;
    @NonNull
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mTestType = 0;
    @Nullable
    private Boolean mTransmitterTestResult = null;
    @Nullable
    private Boolean mScanTestResult = null;
    private boolean mNotificationsEnabled = false;
    private boolean mNotificationChannelCreated = false;
    private int mNotificationIcon = 0;
    private long mLastBluetoothPowerCycleTime = 0L;
    private static final long MIN_MILLIS_BETWEEN_BLUETOOTH_POWER_CYCLES = 60000L;
    @Nullable
    private static BluetoothMedic sInstance;
    private boolean powerCycleOnFailureEnabled = true;
    @Nullable
    private Context mContext = null;

    public void resetBluetooth() {
        if (powerCycleOnFailureEnabled) {
            BluetoothMedic.this.cycleBluetoothIfNotTooSoon();
        }
    }

    @RequiresApi(21)
    private BroadcastReceiver mBluetoothEventReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.d(BluetoothMedic.TAG, "Broadcast notification received.");
            int errorCode;
            String action = intent.getAction();
            if (action != null) {
                if(action.equalsIgnoreCase("onScanFailed")) {
                    errorCode = intent.getIntExtra("errorCode", -1);
                    if(errorCode == 2) {
                        Log.d(BluetoothMedic.TAG,
                                "Detected a SCAN_FAILED_APPLICATION_REGISTRATION_FAILED.  We need to cycle bluetooth to recover");
                        BluetoothMedic.this.cycleBluetoothIfNotTooSoon();
                    }
                } else if(action.equalsIgnoreCase("onStartFailed")) {
                    errorCode = intent.getIntExtra("errorCode", -1);
                    if(errorCode == 4) {
                        BluetoothMedic.this.cycleBluetoothIfNotTooSoon();
                    }
                } else {
                    Log.d(BluetoothMedic.TAG, "Unknown event.");
                }
            }
        }
    };



    /**
     * Get a singleton instance of the BluetoothMedic
     * @return
     */
    public static BluetoothMedic getInstance() {
        if(sInstance == null) {
            sInstance = new BluetoothMedic();
        }
        return sInstance;
    }

    private BluetoothMedic() {
    }

    @RequiresApi(21)
    private void initializeWithContext(Context context) {
        if (this.mAdapter == null) {
            BluetoothManager manager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
            if(manager == null) {
                throw new NullPointerException("Cannot get BluetoothManager");
            } else {
                this.mAdapter = manager.getAdapter();
            }
        }
    }

    /**
     * If set to true, bluetooth will be power cycled on any tests run that determine bluetooth is
     * in a bad state.
     *
     * @param context
     */
    @SuppressWarnings("unused")
    @RequiresApi(21)
    public void enablePowerCycleOnFailures(Context context) {
        mContext = context.getApplicationContext();
        powerCycleOnFailureEnabled = true;
        initializeWithContext(context);
        Log.d(TAG,
                "Medic monitoring for transmission and scan failure notifications");
    }

    /**
     * Starts up a brief blueooth scan with the intent of seeing if it results in an error condition
     * indicating the bluetooth stack may be in a bad state.
     *
     * If the failure error code matches a pattern known to be associated with a bad bluetooth stack
     * state, then the bluetooth stack is turned off and then back on after a short delay in order
     * to try to recover.
     *
     * @return false if the test indicates a failure indicating a bad state of the bluetooth stack
     */
    @SuppressWarnings({"unused","WeakerAccess"})
    @RequiresApi(21)
    public boolean runScanTest(final Context context) {
        initializeWithContext(context);
        this.mScanTestResult = null;
        Log.i(TAG, "Starting scan test");
        final long testStartTime = System.currentTimeMillis();
        if (this.mAdapter != null) {
            final BluetoothLeScanner scanner = this.mAdapter.getBluetoothLeScanner();
            final ScanCallback callback = new ScanCallback() {
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    BluetoothMedic.this.mScanTestResult = true;
                    Log.i(BluetoothMedic.TAG, "Scan test succeeded");
                    try {
                        scanner.stopScan(this);
                    }
                    catch (IllegalStateException e) { /* do nothing */ } // caught if bluetooth is off here
                }

                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    Log.d(BluetoothMedic.TAG, "Sending onScanFailed event");
                    BluetoothMedic.this.resetBluetooth();
                    if(errorCode == 2) {
                        Log.w(BluetoothMedic.TAG,
                                "Scan test failed in a way we consider a failure");
                        BluetoothMedic.this.mScanTestResult = false;
                    } else {
                        Log.i(BluetoothMedic.TAG,
                                "Scan test failed in a way we do not consider a failure");
                        BluetoothMedic.this.mScanTestResult = true;
                    }

                }
            };
            if(scanner != null) {
                try {
                    scanner.startScan(callback);
                    while (this.mScanTestResult == null) {
                        Log.d(TAG, "Waiting for scan test to complete...");

                        try {
                            Thread.sleep(1000L);
                        } catch (InterruptedException e) { /* do nothing */ }

                        if (System.currentTimeMillis() - testStartTime > 5000L) {
                            Log.d(TAG, "Timeout running scan test");
                            break;
                        }
                    }
                    scanner.stopScan(callback);
                } catch (IllegalStateException e) {
                    Log.d(TAG, "Bluetooth is off.  Cannot run scan test.");
                } catch (NullPointerException e) {
                    // Needed to stop a crash caused by internal NPE thrown by Android.  See issue #636
                    Log.e(TAG, "NullPointerException. Cannot run scan test.", e);
                }
            }
            else {
                Log.d(TAG, "Cannot get scanner");
            }
        }

        Log.d(TAG, "scan test complete");
        return this.mScanTestResult == null || this.mScanTestResult;
    }

    /**
     *
     * Configure whether to send user-visible notification warnings when bluetooth power is cycled.
     *
     * @param enabled if true, a user-visible notification is sent to tell the user when
     * @param icon the icon drawable to use in notifications (e.g. R.drawable.notification_icon)
     */
    @SuppressWarnings("unused")
    @RequiresApi(21)
    public void setNotificationsEnabled(boolean enabled, int icon) {
        this.mNotificationsEnabled = enabled;
        this.mNotificationIcon = icon;
    }

    @RequiresApi(21)
    private boolean cycleBluetoothIfNotTooSoon() {
        try {
            long millisSinceLastCycle = System.currentTimeMillis() - this.mLastBluetoothPowerCycleTime;
            if (millisSinceLastCycle < MIN_MILLIS_BETWEEN_BLUETOOTH_POWER_CYCLES) {
                Log.d(TAG, "Not cycling bluetooth because we just did so " +
                        millisSinceLastCycle + " milliseconds ago.");
                return false;
            } else {
                this.mLastBluetoothPowerCycleTime = System.currentTimeMillis();
                Log.d(TAG, "Power cycling bluetooth");
                this.cycleBluetooth();
                return true;
            }
        } catch (Exception e) {
            // Scanner not ready
            return false;
        }
    }

    @RequiresApi(21)
    private void cycleBluetooth() {
        Log.d(TAG, "Power cycling bluetooth");
        Log.d(TAG, "Turning Bluetooth off.");
        if (mAdapter != null) {
            this.mAdapter.disable();
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    Log.d(BluetoothMedic.TAG, "Turning Bluetooth back on.");
                    if (BluetoothMedic.this.mAdapter != null) {
                        BluetoothMedic.this.mAdapter.enable();
                    }
                }
            }, 1000L);
        } else {
            Log.w(TAG, "Cannot cycle bluetooth.  Manager is null.");
        }
    }
}