package com.communication.imp;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.tools.log.LogD;

import cn.wch.uartlib.WCHUARTManager;

/**
 * CH34X UART Transport using WCH CH34XUARTDriver.
 * Implements IDeviceTransport so it can be used interchangeably
 * with UsbCdcTransport in BridgeHelper / UhfServer.
 *
 * Usage:
 *   Ch34xTransport t = new Ch34xTransport(usbManager);
 *   if (t.open("/dev/bus/usb/002/002")) {
 *       t.write(data, 2000);
 *       int n = t.read(buf, 1000);
 *       t.close();
 *   }
 */
public class Ch34xTransport implements IDeviceTransport {

    // WCH CH34X chip
    public static final int CH34X_VID = 6790;  // 0x1A86
    public static final int CH34X_PID = 29987; // 0x7523

    // Common CH34X PIDs for reference
    public static final int[] CH34X_PIDS = {
            29987, // 0x7523 - CH343
            21778, // 0x5512 - CH340
            21795, // 0x5523 - CH341
            21970, // 0x55E2 - CH340
            21971, // 0x55E3
            21972, // 0x55E4
            21973, // 0x55E5
            21974, // 0x55E6
            21975, // 0x55E7
            21976, // 0x55E8
            29986, // 0x7522 - CH343
    };

    private final UsbManager usbManager;
    private UsbDevice usbDevice;
    private String devicePath;
    private int writeTimeout = 2000;
    private int readTimeout = 1000;

    public Ch34xTransport(UsbManager mgr) {
        this.usbManager = mgr;
    }

    // ---------- configurable parameters ----------

    public void setWriteTimeout(int ms) { this.writeTimeout = ms; }
    public void setReadTimeout(int ms)  { this.readTimeout = ms; }

    // ---------- open / close ----------

    @Override
    public boolean open(String path) {
        this.devicePath = path;

        // Find device by path
        usbDevice = findByPath(path);
        if (usbDevice == null) {
            LogD.LOGD("Ch34xTransport: device not found at " + path);
            return false;
        }

        // Check VID/PID
        int vid = usbDevice.getVendorId();
        int pid = usbDevice.getProductId();
        if (vid != CH34X_VID || !isCh34xPid(pid)) {
            LogD.LOGD(String.format("Ch34xTransport: VID/PID mismatch (expected %04X:CH34x, got %04X:%04X)",
                    CH34X_VID, vid, pid));
            // Continue anyway -- caller may be using a compatible device
        }

        // Open via WCH driver
        try {
            WCHUARTManager.getInstance().openDevice(usbDevice);
        } catch (Exception e) {
            LogD.LOGD("Ch34xTransport: openDevice exception: " + e.getMessage());
            usbDevice = null;
            return false;
        }

        // Configure serial parameters: 115200 8N1
        try {
            WCHUARTManager.getInstance().setSerialParameter(
                    usbDevice,   // device
                    0,           // serialIndex (COM1)
                    115200,      // baudRate
                    8,           // dataBits
                    1,           // stopBits
                    0,           // parity (0=NONE)
                    false        // flowControl
            );
        } catch (Exception e) {
            LogD.LOGD("Ch34xTransport: setSerialParameter exception: " + e.getMessage());
        }

        // Give the driver time to initialize
        try {
            Thread.sleep(300);
        } catch (InterruptedException ignored) {}

        if (!isConnected()) {
            LogD.LOGD("Ch34xTransport: device not connected after open");
            usbDevice = null;
            return false;
        }

        LogD.LOGD(String.format("Ch34xTransport: opened %s VID=%04X PID=%04X",
                path, vid, pid));
        return true;
    }

    @Override
    public void close() {
        // WCHUARTManager does not expose closeDevice();
        // the driver manages lifecycle internally via isConnected()
        usbDevice = null;
    }

    @Override
    public boolean isConnected() {
        return usbDevice != null
                && WCHUARTManager.getInstance().isConnected(usbDevice);
    }

    @Override
    public String getAddress() {
        return devicePath;
    }

    @Override
    public String getType() {
        return "CH34X_UART";
    }

    // ---------- read / write ----------

    @Override
    public int write(byte[] data, int timeout) {
        if (!isConnected()) {
            LogD.LOGD("Ch34xTransport: write failed - not connected");
            return -1;
        }
        try {
            int ret = WCHUARTManager.getInstance().writeData(usbDevice, 0, data, data.length, timeout);
            if (ret < 0) {
                LogD.LOGD("Ch34xTransport: write error code=" + ret);
            }
            return ret;
        } catch (Exception e) {
            LogD.LOGD("Ch34xTransport: write exception: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public int read(byte[] buffer, int timeout) {
        if (!isConnected()) {
            return -1;
        }

        long deadline = System.currentTimeMillis() + timeout;
        while (System.currentTimeMillis() < deadline) {
            if (!isConnected()) {
                return -1;
            }

            byte[] data;
            try {
                data = WCHUARTManager.getInstance().readData(usbDevice, 0);
            } catch (Exception e) {
                LogD.LOGD("Ch34xTransport: read exception: " + e.getMessage());
                return -1;
            }
            if (data != null && data.length > 0) {
                int n = Math.min(data.length, buffer.length);
                System.arraycopy(data, 0, buffer, 0, n);
                return n;
            }

            // Poll every 10ms
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                return -1;
            }
        }

        return 0; // timeout, no data
    }

    /**
     * Check if the given PID is a known CH34x PID.
     */
    public static boolean isCh34xPid(int pid) {
        for (int p : CH34X_PIDS) {
            if (p == pid) return true;
        }
        return false;
    }

    /**
     * Check if a USB device is a CH34x chip.
     */
    public static boolean isCh34xDevice(UsbDevice dev) {
        return dev.getVendorId() == CH34X_VID && isCh34xPid(dev.getProductId());
    }

    // ---------- internal ----------

    private UsbDevice findByPath(String path) {
        for (UsbDevice d : usbManager.getDeviceList().values()) {
            if (d.getDeviceName().equals(path)) {
                return d;
            }
        }
        return null;
    }
}
