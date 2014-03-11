package carnero.netmap.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import carnero.netmap.common.Constants;
import carnero.netmap.model.Bts;

public class BtsDb {

    public static boolean isSaved(SQLiteDatabase db, Bts bts) {
        boolean saved = false;

        Cursor cursor = null;
        try {
            // select all BTS' with no particular order
            final StringBuilder where = new StringBuilder();
            where.append(DatabaseStructure.COLUMNS_BTS.LAC);
            where.append(" = ");
            where.append(bts.lac);
            where.append(" and ");
            where.append(DatabaseStructure.COLUMNS_BTS.CID);
            where.append(" = ");
            where.append(bts.cid);
            where.append(" and ");
            where.append(DatabaseStructure.COLUMNS_BTS.NETWORK);
            where.append(" <= ");
            where.append(bts.network);

            cursor = db.query(
                    DatabaseStructure.TABLE.BTS,
                    new String[]{DatabaseStructure.COLUMNS_BTS.ID},
                    where.toString(),
                    null,
                    null,
                    null,
                    "1"
            );

            if (cursor != null && cursor.getCount() > 0) {
                saved = true;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return saved;
    }

    public static boolean save(SQLiteDatabase db, Bts bts) {
        if (BtsDb.isSaved(db, bts)) {
            return true;
        }

        final StringBuilder where = new StringBuilder();
        where.append(DatabaseStructure.COLUMNS_BTS.LAC);
        where.append(" = ");
        where.append(bts.lac);
        where.append(" and ");
        where.append(DatabaseStructure.COLUMNS_BTS.CID);
        where.append(" = ");
        where.append(bts.cid);

        final ContentValues values = new ContentValues();
        values.put(DatabaseStructure.COLUMNS_BTS.LAC, bts.lac);
        values.put(DatabaseStructure.COLUMNS_BTS.CID, bts.cid);
        values.put(DatabaseStructure.COLUMNS_BTS.NETWORK, bts.network);
        if (bts.location != null) {
            values.put(DatabaseStructure.COLUMNS_BTS.LATITUDE, bts.location.latitude);
            values.put(DatabaseStructure.COLUMNS_BTS.LONGITUDE, bts.location.longitude);
        }

        // update
        try {
            int rows = db.update(DatabaseStructure.TABLE.BTS, values, where.toString(), null);
            if (rows > 0) {
                Log.i(Constants.TAG, "BTS " + bts + " was updated (save)");
                return true;
            }
        } catch (Exception e) {
            // pokemon
        }

        // insert new
        try {
            long id = db.insert(DatabaseStructure.TABLE.BTS, null, values);
            if (id > 0) {
                Log.i(Constants.TAG, "BTS " + bts + " was saved");
                return true;
            }
        } catch (Exception e) {
            // pokemon
        }

        return false;
    }

    public static boolean updateNetwork(SQLiteDatabase db, Bts bts) {
        final StringBuilder where = new StringBuilder();
        where.append(DatabaseStructure.COLUMNS_BTS.LAC);
        where.append(" = ");
        where.append(bts.lac);
        where.append(" and ");
        where.append(DatabaseStructure.COLUMNS_BTS.CID);
        where.append(" = ");
        where.append(bts.cid);

        final ContentValues values = new ContentValues();
        values.put(DatabaseStructure.COLUMNS_BTS.NETWORK, bts.network);

        // update
        try {
            int rows = db.update(DatabaseStructure.TABLE.BTS, values, where.toString(), null);
            if (rows > 0) {
                Log.i(Constants.TAG, "BTS " + bts + " was updated (network)");
                return true;
            }
        } catch (Exception e) {
            // pokemon
        }

        return false;
    }

    public static boolean updateLocation(SQLiteDatabase db, Bts bts) {
        if (bts.location == null) {
            return false;
        }

        final StringBuilder where = new StringBuilder();
        where.append(DatabaseStructure.COLUMNS_BTS.LAC);
        where.append(" = ");
        where.append(bts.lac);
        where.append(" and ");
        where.append(DatabaseStructure.COLUMNS_BTS.CID);
        where.append(" = ");
        where.append(bts.cid);

        final ContentValues values = new ContentValues();
        values.put(DatabaseStructure.COLUMNS_BTS.LATITUDE, bts.location.latitude);
        values.put(DatabaseStructure.COLUMNS_BTS.LONGITUDE, bts.location.longitude);

        // update
        try {
            int rows = db.update(DatabaseStructure.TABLE.BTS, values, where.toString(), null);
            if (rows > 0) {
                Log.i(Constants.TAG, "BTS " + bts + " was updated (location)");
                return true;
            }
        } catch (Exception e) {
            // pokemon
        }

        return false;
    }

    public static Bts load(SQLiteDatabase db, long lac, long cid) {
        Bts bts = null;

        Cursor cursor = null;
        try {
            final StringBuilder where = new StringBuilder();
            where.append(DatabaseStructure.COLUMNS_BTS.LAC);
            where.append(" = ");
            where.append(lac);
            where.append(" and ");
            where.append(DatabaseStructure.COLUMNS_BTS.CID);
            where.append(" = ");
            where.append(cid);

            cursor = db.query(
                    DatabaseStructure.TABLE.BTS,
                    DatabaseStructure.PROJECTION.BTS,
                    where.toString(),
                    null,
                    null,
                    null,
                    "1"
            );

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                final int idxLac = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LAC);
                final int idxCid = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.CID);
                final int idxType = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.NETWORK);
                final int idxLatitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LATITUDE);
                final int idxLongitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LONGITUDE);

                bts = new Bts();
                bts.lac = cursor.getLong(idxLac);
                bts.cid = cursor.getLong(idxCid);
                bts.network = cursor.getInt(idxType);
                if (!cursor.isNull(idxLatitude) && !cursor.isNull(idxLongitude)) {
                    bts.location = new LatLng(cursor.getDouble(idxLatitude), cursor.getDouble(idxLongitude));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return bts;
    }

    public static List<Bts> loadAll(SQLiteDatabase db) {
        final ArrayList<Bts> btsList = new ArrayList<Bts>();

        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseStructure.TABLE.BTS,
                    DatabaseStructure.PROJECTION.BTS,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();

                final int idxLac = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LAC);
                final int idxCid = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.CID);
                final int idxType = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.NETWORK);
                final int idxLatitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LATITUDE);
                final int idxLongitude = cursor.getColumnIndex(DatabaseStructure.COLUMNS_BTS.LONGITUDE);

                Bts bts;
                do {
                    bts = new Bts();
                    bts.lac = cursor.getLong(idxLac);
                    bts.cid = cursor.getLong(idxCid);
                    bts.network = cursor.getInt(idxType);
                    if (!cursor.isNull(idxLatitude) && !cursor.isNull(idxLongitude)) {
                        bts.location = new LatLng(cursor.getDouble(idxLatitude), cursor.getDouble(idxLongitude));
                    }

                    btsList.add(bts);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return btsList;
    }
}
