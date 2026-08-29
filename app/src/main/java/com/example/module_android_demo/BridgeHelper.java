package com.example.module_android_demo;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import android.util.Log;

import com.communication.imp.Ch34xTransport;
import com.communication.imp.IDeviceTransport;
import com.communication.imp.UhfServer;
import com.communication.imp.UsbCdcTransport;

/**
 * USB bridge helper -- encapsulates USB device discovery, transport open, and
 * UhfServer lifecycle. Supports both HDSC CDC-ACM devices and WCH CH34x UART devices.
 *
 * Usage:
 *   BridgeHelper bh = new BridgeHelper();
 *   String addr = bh.start(usbManager, usbDevice);  // returns "127.0.0.1:9765" or null
 *   ... use addr with Reader.InitReader_Notype(addr, antCount) ...
 *   bh.stop();
 */
public class BridgeHelper {

    private static final int BRIDGE_PORT = 9765;

    // HDSC Composite Device (CDC-ACM)
    public static final int HDSC_VID = 11912;  // 0x2E88
    public static final int HDSC_PID = 17925;  // 0x4605

    // WCH CH34X UART Device
    public static final int CH34X_VID = 6790;  // 0x1A86
    public static final int CH34X_PID = 29987; // 0x7523

    // Static reference for the currently active bridge (for cross-Activity access)
    private static BridgeHelper sActiveBridge;

    private IDeviceTransport transport;
    private UhfServer uhfServer;

    /**
     * Find the first USB device with a recognized VID/PID.
     * Checks HDSC CDC-ACM first, then WCH CH34X.
     * Returns null if no recognized device found.
     */
    public static UsbDevice findDevice(UsbManager mgr) {
        for (UsbDevice d : mgr.getDeviceList().values()) {
            int vid = d.getVendorId();
            int pid = d.getProductId();
            if ((vid == HDSC_VID && pid == HDSC_PID)
                    || (vid == CH34X_VID && Ch34xTransport.isCh34xPid(pid))) {
                return d;
            }
        }
        return null;
    }

    /**
     * Find HDSC CDC-ACM device only.
     */
    public static UsbDevice findHdscDevice(UsbManager mgr) {
        for (UsbDevice d : mgr.getDeviceList().values()) {
            if (d.getVendorId() == HDSC_VID && d.getProductId() == HDSC_PID) {
                return d;
            }
        }
        return null;
    }

    /**
     * Find WCH CH34X device only.
     */
    public static UsbDevice findCh34xDevice(UsbManager mgr) {
        for (UsbDevice d : mgr.getDeviceList().values()) {
            if (Ch34xTransport.isCh34xDevice(d)) {
                return d;
            }
        }
        return null;
    }

    /**
     * Get transport type name for a USB device (for logging / UI).
     */
    public static String getDeviceTypeName(UsbDevice dev) {
        if (dev == null) return "unknown";
        if (dev.getVendorId() == HDSC_VID && dev.getProductId() == HDSC_PID) {
            return "HDSC_CDC";
        }
        if (Ch34xTransport.isCh34xDevice(dev)) {
            return "CH34X_UART";
        }
        return String.format("unknown(%04X:%04X)", dev.getVendorId(), dev.getProductId());
    }

    /**
     * Start the USB bridge with the appropriate transport for the device type.
     *
     * @param mgr  UsbManager from getSystemService()
     * @param dev  USB device (from findDevice or permission callback)
     * @return "127.0.0.1:9765" on success, null on failure
     */
    public String start(UsbManager mgr, UsbDevice dev) {
        Log.d("BRIDGE", "start() called, dev=" + (dev != null ? dev.getDeviceName() : "null"));
        if (dev == null) return null;

        String devicePath = dev.getDeviceName();
        Log.d("BRIDGE", "devicePath=" + devicePath + " vid=" + dev.getVendorId() + " pid=" + dev.getProductId());

        // Create the appropriate transport based on device type
        boolean isCh34x = Ch34xTransport.isCh34xDevice(dev);
        Log.d("BRIDGE", "isCh34x=" + isCh34x);
        if (isCh34x) {
            Log.d("BRIDGE", "Creating Ch34xTransport...");
            transport = new Ch34xTransport(mgr);
            Log.d("BRIDGE", "Ch34xTransport created");
        } else {
            Log.d("BRIDGE", "Creating UsbCdcTransport...");
            transport = new UsbCdcTransport(mgr);
            Log.d("BRIDGE", "UsbCdcTransport created");
        }

        Log.d("BRIDGE", "Calling transport.open(" + devicePath + ")...");
        boolean opened = transport.open(devicePath);
        Log.d("BRIDGE", "transport.open() returned: " + opened + " connected=" + transport.isConnected());
        if (!opened) {
            transport = null;
            Log.d("BRIDGE", "start() FAILED at transport.open");
            return null;
        }

        try {
            Log.d("BRIDGE", "Creating UhfServer...");
            uhfServer = new UhfServer(transport);
            Log.d("BRIDGE", "UhfServer created, calling InitLocalServer...");
            uhfServer.Set_init_port(BRIDGE_PORT);
            uhfServer.InitLocalServer();
            uhfServer.isrun = true;
            uhfServer.start();
            Log.d("BRIDGE", "UhfServer started on port " + BRIDGE_PORT);
        } catch (Exception e) {
            Log.d("BRIDGE", "UhfServer exception: " + e.getMessage(), e);
            transport.close();
            transport = null;
            uhfServer = null;
            return null;
        }

        sActiveBridge = this;
        Log.d("BRIDGE", "start() SUCCESS, returning 127.0.0.1:" + BRIDGE_PORT);
        return "127.0.0.1:" + BRIDGE_PORT;
    }

    /**
     * Stop the bridge and release all resources.
     */
    public void stop() {
        if (uhfServer != null) {
            uhfServer.isrun = false;
            try {
                uhfServer.StopServer();
            } catch (Exception e) {
                // ignore
            }
            uhfServer = null;
        }
        if (transport != null) {
            transport.close();
            transport = null;
        }
        if (sActiveBridge == this) {
            sActiveBridge = null;
        }
    }

    /**
     * Get the bridge port number.
     */
    public int getPort() {
        return BRIDGE_PORT;
    }

    /**
     * Get the current transport (may be null).
     */
    public IDeviceTransport getTransport() {
        return transport;
    }

    /**
     * Get the currently active bridge (for cross-Activity access).
     */
    public static BridgeHelper getActiveBridge() {
        return sActiveBridge;
    }

    /**
     * Check if a USB bridge is currently active.
     */
    public static boolean isUsbBridgeActive() {
        return sActiveBridge != null
                && sActiveBridge.transport != null
                && sActiveBridge.transport.isConnected();
    }

    /**
     * Get the active transport type name ("HDSC_CDC", "CH34X_UART", or null).
     */
    public static String getActiveTransportType() {
        if (sActiveBridge == null || sActiveBridge.transport == null) return null;
        return sActiveBridge.transport.getType();
    }
}
