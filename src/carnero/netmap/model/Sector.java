package carnero.netmap.model;

import carnero.netmap.common.LocationUtil;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Sector {

	// public String operator;
	public XY index;
	public int type;
	public double signalAverage;
	public double signalCount;
	public LatLng center;
	public List<LatLng> corners;

	public Sector() {
		// empty
	}

	public Sector(XY index, int type) {
		this.index = index;
		this.center = LocationUtil.getSectorCenter(index);
		this.corners = LocationUtil.getSectorHexagon(center);
		this.type = type;
	}

	public Sector(LatLng position, int type) {
		this.index = LocationUtil.getSectorXY(position);
		this.center = LocationUtil.getSectorCenter(index);
		this.corners = LocationUtil.getSectorHexagon(center);
		this.type = type;
	}

	public LatLng getCenter() {
		if (center == null) {
			center = LocationUtil.getSectorCenter(index.x, index.y);
		}

		return center;
	}

	public List<LatLng> getCorners() {
		if (corners == null) {
			corners = LocationUtil.getSectorHexagon(getCenter());
		}

		return corners;
	}
}