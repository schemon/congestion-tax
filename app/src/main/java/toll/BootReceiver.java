package toll;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import toll.geofence.GeoFenceService;

/**
 * Created by simarv on 2016-06-06.
 */
public class BootReceiver extends BroadcastReceiver {

	@Override public void onReceive(Context context, Intent intent) {
		Log.d(this.getClass().getSimpleName(), "received: " +intent.toString());
		context.startService(new Intent(context, GeoFenceService.class));
	}
}
