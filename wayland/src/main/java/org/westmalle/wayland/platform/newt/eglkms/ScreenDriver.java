package org.westmalle.wayland.platform.newt.eglkms;


import com.jogamp.nativewindow.DefaultGraphicsScreen;
import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.MonitorMode;
import jogamp.newt.MonitorModeProps;
import jogamp.newt.ScreenImpl;
import org.westmalle.wayland.platform.newt.eglkms.drm.*;

public class ScreenDriver extends ScreenImpl {

    private drmModeConnector connector;
    private drmModeEncoder   encoder;

    @Override
    protected void createNativeImpl() {
        init();
        this.aScreen = new DefaultGraphicsScreen(getDisplay().getGraphicsDevice(),
                                                 this.screen_idx);
    }


    private void init() {
        final DisplayDriver displayDriver = (DisplayDriver) getDisplay();

        int i;

        final drmModeRes resources = DrmLibrary.INSTANCE.drmModeGetResources(displayDriver.getFd());
        if (resources == null) {
            System.err.println("drmModeGetResources failed\n");
            System.exit(1);
        }

        for (i = 0; i < resources.count_connectors; i++) {
            this.connector = DrmLibrary.INSTANCE.drmModeGetConnector(displayDriver.getFd(),
                                                                     resources.connectors.getInt(i * 4));
            if (this.connector == null) {
                continue;
            }
            if (this.connector.connection == drmModeConnection.DRM_MODE_CONNECTED &&
                this.connector.count_modes > 0) {
                break;
            }
            DrmLibrary.INSTANCE.drmModeFreeConnector(this.connector);
        }

        if (i == resources.count_connectors) {
            System.err.println("No currently active connector found.\n");
            System.exit(1);
        }

        for (i = 0; i < resources.count_encoders; i++) {
            this.encoder = DrmLibrary.INSTANCE.drmModeGetEncoder(displayDriver.getFd(),
                                                                 resources.encoders.getInt(i * 4));
            if (this.encoder == null) {
                continue;
            }
            if (this.encoder.encoder_id == this.connector.encoder_id) {
                break;
            }
            DrmLibrary.INSTANCE.drmModeFreeEncoder(this.encoder);
        }
    }

    @Override
    protected void closeNativeImpl() {
        DrmLibrary.INSTANCE.drmModeFreeConnector(this.connector);
    }

    @Override
    protected int validateScreenIndex(final int idx) {
        return 0;
    }

    @Override
    protected void collectNativeMonitorModesAndDevicesImpl(final MonitorModeProps.Cache cache) {
        //TODO collect info from init method
        int[] props = new int[MonitorModeProps.NUM_MONITOR_MODE_PROPERTIES_ALL];
        int   i     = 0;
        props[i++] = MonitorModeProps.NUM_MONITOR_MODE_PROPERTIES_ALL;
        props[i++] = this.connector.modes.hdisplay; // width
        props[i++] = this.connector.modes.vdisplay; // height
        props[i++] = ScreenImpl.default_sm_bpp; // FIXME
        props[i++] = ScreenImpl.default_sm_rate * 100; // FIXME
        props[i++] = this.connector.modes.flags; // flags
        props[i++] = 0; // mode_idx
        props[i++] = 0; // rotation
        final MonitorMode currentMode = MonitorModeProps.streamInMonitorMode(null,
                                                                             cache,
                                                                             props,
                                                                             0);

        props = new int[MonitorModeProps.MIN_MONITOR_DEVICE_PROPERTIES - 1 - MonitorModeProps.NUM_MONITOR_MODE_PROPERTIES];
        i = 0;
        props[i++] = props.length;
        props[i++] = this.encoder.crtc_id; // crt_idx
        props[i++] = 0; // is-clone
        props[i++] = 1; // is-primary
        props[i++] = ScreenImpl.default_sm_widthmm; // FIXME
        props[i++] = ScreenImpl.default_sm_heightmm; // FIXME
        props[i++] = 0; // rotated viewport x pixel-units
        props[i++] = 0; // rotated viewport y pixel-units
        props[i++] = this.connector.modes.hdisplay; // rotated viewport width pixel-units
        props[i++] = this.connector.modes.vdisplay; // rotated viewport height pixel-units
        props[i++] = 0; // rotated viewport x window-units
        props[i++] = 0; // rotated viewport y window-units
        props[i++] = this.connector.modes.hdisplay; // rotated viewport width window-units
        props[i++] = this.connector.modes.vdisplay; // rotated viewport height window-units
        MonitorModeProps.streamInMonitorDevice(cache,
                                               this,
                                               currentMode,
                                               null,
                                               cache.monitorModes,
                                               props,
                                               0,
                                               null);
    }

    @Override
    protected MonitorMode queryCurrentMonitorModeImpl(final MonitorDevice monitor) {
        //TODO collect info from init method

        return null;
    }

    @Override
    protected boolean setCurrentMonitorModeImpl(final MonitorDevice monitor,
                                                final MonitorMode mode) {
        //TODO collect info from init method

        return false;
    }
}
