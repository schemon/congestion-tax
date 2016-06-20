package toll.geofence;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;

/**
 * Created by simarv on 2016-06-06.
 */
public class GeoFenceService extends IntentService {
	/**
	 * Creates an IntentService.  Invoked by your subclass's constructor.
	 *
	 * @param name Used to name the worker thread, important only for debugging.
	 */
	public GeoFenceService(String name) {
		super(name);
	}

	public GeoFenceService() {
		super("GeoFenceService");
	}

	@Override protected void onHandleIntent(Intent intent) {
		Log.d(this.getClass().getSimpleName(), "received: " + intent.toString());

		GeoFenceHandler handler = new GeoFenceHandler(this);
		if (handler.hasStarted()) {
			handler.startBlocking(new ResultCallback() {
				@Override public void onResult(Result result) {
					Log.d(this.getClass().getSimpleName(), "started " + result.getStatus().isSuccess());
				}
			});
		}
	}
}
