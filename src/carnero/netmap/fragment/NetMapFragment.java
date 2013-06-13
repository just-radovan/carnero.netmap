package carnero.netmap.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import carnero.netmap.App;
import carnero.netmap.R;
import carnero.netmap.common.*;
import carnero.netmap.listener.OnLocationObtainedListener;
import carnero.netmap.model.Bts;
import carnero.netmap.model.BtsCache;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

public class NetMapFragment extends MapFragment implements SimpleGeoReceiver {

	private Geo mGeo;
	private GoogleMap mMap;
	private TelephonyManager mTelephony;
	private Location mLastLocation;
	private Marker mMyPosition;
	private HashMap<String, Marker> mBtsMarkers = new HashMap<String, Marker>();
	private LocationListener mLocationListener = new LocationListener();
	private float mZoomDefault = 16f;
	final private StatusListener mListener = new StatusListener();

	@Override
	public void onActivityCreated(Bundle state) {
		super.onActivityCreated(state);

		mTelephony = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
		final View mapView = super.onCreateView(inflater, container, state);
		setMapTransparent((ViewGroup) mapView);

		return mapView;
	}

	@Override
	public void onResume() {
		super.onResume();

		initializeMap();

		mTelephony.listen(mListener, PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CELL_INFO | PhoneStateListener.LISTEN_DATA_ACTIVITY);

		mGeo = ((App) getActivity().getApplication()).getGeolocation();
		mGeo.addReceiver(this);
	}

	@Override
	public void onPause() {
		mGeo.removeReceiver(this);

		mTelephony.listen(mListener, PhoneStateListener.LISTEN_NONE);

		mMap.clear();
		mMap = null;

		super.onPause();
	}

	public void initializeMap() {
		if (mMap == null) {
			mMap = getMap();

			if (mMap == null) {
				return; // map is not yet available
			}

			UiSettings settings = mMap.getUiSettings();
			settings.setCompassEnabled(true);
			settings.setZoomControlsEnabled(false);
			settings.setMyLocationButtonEnabled(false);

			mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			mMap.setMyLocationEnabled(false);

			if (mMap.getMaxZoomLevel() < mZoomDefault) {
				mZoomDefault = mMap.getMaxZoomLevel();
			}
		}
	}

	public void onLocationChanged(Location location) {
		mLastLocation = location;

		// setMyMarker();
	}

	public void getCellInfo() {
		getCellInfo(null);
	}

	public void getCellInfo(CellLocation cell) {
		if (cell == null) {
			cell = mTelephony.getCellLocation();
		}
		final int type = mTelephony.getNetworkType();

		if (cell instanceof GsmCellLocation) {
			GsmCellLocation gsmCell = (GsmCellLocation) cell;

			if (gsmCell.getLac() >= 0 && gsmCell.getCid() >= 0) {
				final Bts bts = BtsCache.get(gsmCell.getLac(), gsmCell.getCid(), type);
				bts.getLocation(mLocationListener);
			}
		} else if (cell instanceof CdmaCellLocation) {
			Log.w(Constants.TAG, "CDMA location not implemented");
		}
	}

	private void setMapTransparent(ViewGroup group) {
		int cnt = group.getChildCount();

		for (int i = 0; i < cnt; i ++) {
			View child = group.getChildAt(i);

			if (child instanceof ViewGroup) {
				setMapTransparent((ViewGroup) child);
			} else {
				child.setBackgroundColor(0x00000000);
			}
		}
	}

	// classes

	private class LocationListener implements OnLocationObtainedListener {

		public void onLocationObtained(Bts bts) {
			if (mMap == null || bts.location == null) {
				return;
			}

			Log.d(Constants.TAG, "Location obtained: " + bts.lac + ":" + bts.cid + ", type " + bts.type);

			final String id = Bts.getId(bts);
			final int level = Util.getNetworkLevel(bts.type);

			int pinResource;
			switch (level) {
				case Constants.NET_LEVEL_2:
					pinResource = R.drawable.pin_level_2;
					break;
				case Constants.NET_LEVEL_3:
					pinResource = R.drawable.pin_level_3;
					break;
				case Constants.NET_LEVEL_4:
					pinResource = R.drawable.pin_level_4;
					break;
				case Constants.NET_LEVEL_5      :
					pinResource = R.drawable.pin_level_5;
					break;
				default:
					pinResource = R.drawable.pin_level_1;
			}

			Marker marker = mBtsMarkers.get(id);
			if (marker != null) {
				marker.remove();
			}

			final MarkerOptions options = new MarkerOptions();
			options.position(bts.location);
			options.icon(BitmapDescriptorFactory.fromResource(pinResource));
			options.anchor(0.5f, 1.0f);

			marker = mMap.addMarker(options);
			mBtsMarkers.put(id, marker);
		}
	}

	public class StatusListener extends PhoneStateListener {

		@Override
		public void onCellLocationChanged(CellLocation cell) {
			getCellInfo(cell);
		}

		@Override
		public void onDataActivity(int direction) {
			getCellInfo();
		}

		@Override
		public void onCellInfoChanged(List<CellInfo> info) {
			getCellInfo();
		}
	}
}