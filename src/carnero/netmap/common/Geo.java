package carnero.netmap.common;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.*;

/**
 * Geolocation component operating with NETWORK provider only
 */
public class Geo {

	private LocationManager mManager;
	private final GeoListener mListener = new GeoListener();
	private final ArrayList<SimpleGeoReceiver> mReceivers = new ArrayList<SimpleGeoReceiver>();
	private final HashMap<String, Location> mLocations = new HashMap<String, Location>();

	public Geo(Context context) {
		mManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		init();
	}

	public void addReceiver(SimpleGeoReceiver receiver) {
		if (mReceivers.isEmpty()) {
			init();
		}

		mReceivers.add(receiver);

		selectBestLocation();
	}

	public void removeReceiver(SimpleGeoReceiver receiver) {
		if (mReceivers.contains(receiver)) {
			mReceivers.remove(receiver);
		}

		if (mReceivers.isEmpty()) {
			release();
		}
	}

	/**
	 * Initialize service
	 */
	private void init() {
		List<String> providers = mManager.getAllProviders();
		if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
			mListener.provider = LocationManager.NETWORK_PROVIDER;
			mManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.GEO_TIME, Constants.GEO_DISTANCE, mListener);

			Log.i(Constants.TAG, "Network geolocation initialized");
		}

		if (providers.contains(LocationManager.PASSIVE_PROVIDER)) {
			mListener.provider = LocationManager.PASSIVE_PROVIDER;
			mManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, Constants.GEO_TIME, Constants.GEO_DISTANCE, mListener);

			Log.i(Constants.TAG, "Passive geolocation initialized");
		}
	}

	/**
	 * Release resources before abandoning object
	 */
	public void release() {
		mReceivers.clear();

		if (mManager != null && mListener != null) {
			mManager.removeUpdates(mListener);
		}

		Log.i(Constants.TAG, "Geolocation released");
	}

	/**
	 * Load last known location and use it if newer, or missing
	 */
	public Location getLastLoc() {
		Location latest = null;
		String used = null;
		Location location;
		Long time = Long.MIN_VALUE;

		location = mManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null) {
			time = location.getTime();
			latest = location;
			used = LocationManager.GPS_PROVIDER;
		}

		location = mManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (location != null) {
			if (location.getTime() > time) {
				time = location.getTime();
				latest = location;
				used = LocationManager.NETWORK_PROVIDER;
			}
		}
		location = mManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

		if (location != null) {
			if (location.getTime() > time) {
				latest = location;
				used = LocationManager.PASSIVE_PROVIDER;
			}
		}

		if (latest == null) {
			return null;
		}

		Log.i(Constants.TAG, "Using last location from " + used);

		return latest;
	}

	/**
	 * Pick best avialable location and notifies listeners
	 * Using newest known location
	 */
	private void selectBestLocation() {
		Location location = null;

		long last = Long.MIN_VALUE;
		String used = "";
		final Set<Map.Entry<String, Location>> entries = mLocations.entrySet();
		for (Map.Entry entry : entries) {
			final Location loc = (Location) entry.getValue();

			if (loc.getTime() > last) {
				location = loc;
				last = loc.getTime();
				used = (String) entry.getKey();
			}
		}

		if (location == null) {
			return;
		}

		Log.i(Constants.TAG, "Using location from " + used);

		for (SimpleGeoReceiver receiver : mReceivers) {
			receiver.onLocationChanged(location);
		}
	}

	// class

	public class GeoListener implements LocationListener {

		public String provider;

		public void onLocationChanged(Location location) {
			mLocations.put(provider, location);
		}

		public void onProviderDisabled(String provider) {
			// empty
		}

		public void onProviderEnabled(String provider) {
			// empty
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// empty
		}
	}
}
