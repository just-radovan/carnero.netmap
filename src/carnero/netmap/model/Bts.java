package carnero.netmap.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import carnero.netmap.common.BtsLocationDownloader;
import carnero.netmap.common.Constants;
import carnero.netmap.listener.OnLocationObtainedListener;

public class Bts {

    public long lac;
    public long cid;
    // public String operator;
    public int network;
    public LatLng location;
    public LatLng locationNew;
    // status
    public boolean locationNA = false;
    // internal status
    private boolean mLoading = false;

    public Bts() {
        // empty
    }

    public Bts(int lac, int cid, int network) {
        this.lac = lac;
        this.cid = cid;
        this.network = network;

        Log.d(Constants.TAG, this + " = " + this.network + ", update");
    }

    public static String getId(Bts bts) {
        return getId(bts.lac, bts.cid);
    }

    public static String getId(long lac, long cid) {
        final StringBuilder sb = new StringBuilder();
        sb.append(Long.toString(lac));
        sb.append(":");
        sb.append(Long.toString(cid));

        return sb.toString();
    }

    public void getLocation(OnLocationObtainedListener listener) {
        if (location != null) {
            return;
        }

        if (!mLoading && !locationNA) {
            new BtsLocationDownloader(this, listener).execute();
        }
    }

    public void setLoading() {
        mLoading = true;
    }

    public void clearLoading() {
        mLoading = false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Long.toHexString(lac).toUpperCase());
        sb.append(":");
        sb.append(Long.toHexString(cid).toUpperCase());

        return sb.toString();
    }
}
