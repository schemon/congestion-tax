package toll.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by simarv on 2016-06-05.
 */
public class GeoFenceHandler implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

	private final Context context;
	GoogleApiClient mGoogleApiClient;

	/**
	 * Used when requesting to add or remove geofences.
	 */
	private PendingIntent mGeofencePendingIntent;


	public GeoFenceHandler(Context context) {
		this.context = context;
		buildGoogleApiClient();
	}


	/**
	 * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
	 */
	protected synchronized void buildGoogleApiClient() {
		 mGoogleApiClient = new GoogleApiClient.Builder(context)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();
	}

	/**
	 * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
	 * specified geofences. Handles the success or failure results returned by addGeofences().
	 */
	public void startBlocking(ResultCallback callback) {
		buildClient(callback).blockingConnect();
	}

	public void start(ResultCallback callback) {
		buildClient(callback).connect();
	}

	private GoogleApiClient buildClient(final ResultCallback callback) {
		final GoogleApiClient client = new GoogleApiClient.Builder(context)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();

		client.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
			@Override public void onConnected(Bundle bundle) {
				addFences(client, callback);
			}

			@Override public void onConnectionSuspended(int i) {

			}
		});

		return client;
	}

	private void addFences(GoogleApiClient client, final ResultCallback callback) {
		if (!client.isConnected()) {
			Toast.makeText(context, "Not connected", Toast.LENGTH_SHORT).show();
			return;
		}

		try {
			LocationServices.GeofencingApi.addGeofences(
					client,
					// The GeofenceRequest object.
					getGeofencingRequest(),
					// A pending intent that that is reused when calling removeGeofences(). This
					// pending intent is used to generate an intent when a matched geofence
					// transition is observed.
					getGeofencePendingIntent()
			).setResultCallback(new ResultCallback<Status>() {
				@Override public void onResult(Status status) {
					if(status.isSuccess()) {
						setStatus(true);
						callback.onResult(status);
					}
					handleResult(status);
				}
			});

		} catch (SecurityException securityException) {
			// Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
			logSecurityException(securityException);
		}
	}

	/**
	 * Removes geofences, which stops further notifications when the device enters or exits
	 * previously registered geofences.
	 */
	public void stop(final ResultCallback callback) {
		final GoogleApiClient client = new GoogleApiClient.Builder(context)
				.addOnConnectionFailedListener(this)
				.addApi(LocationServices.API)
				.build();

		client.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
			@Override public void onConnected(Bundle bundle) {
				removeFences(client, callback);
			}

			@Override public void onConnectionSuspended(int i) {

			}
		});


		client.connect();

	}

	private void removeFences(GoogleApiClient client, final ResultCallback callback) {
		if (!client.isConnected()) {
			Toast.makeText(context, "Not connected", Toast.LENGTH_SHORT).show();
			return;
		}
		try {
			// Remove geofences.
			LocationServices.GeofencingApi.removeGeofences(
					client,
					// This is the same pending intent that was used in addGeofences().
					getGeofencePendingIntent()
			).setResultCallback(new ResultCallback<Status>() {
				@Override public void onResult(Status status) {
					if(status.isSuccess()) {
						setStatus(false);
						callback.onResult(status);
					}
					handleResult(status);
				}
			}); // Result processed in onResult().
		} catch (SecurityException securityException) {
			// Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
			logSecurityException(securityException);
		}
	}

	private void logSecurityException(SecurityException securityException) {
		Log.e(GeoFenceHandler.class.getSimpleName(), "Invalid location permission. " +
				"You need to use ACCESS_FINE_LOCATION with geofences", securityException);
	}

	/**
	 * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
	 * Also specifies how the geofence notifications are initially triggered.
	 */
	private GeofencingRequest getGeofencingRequest() {
		GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

		// The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
		// GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
		// is already inside that geofence.
		builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

		// Get the geofences used. Geofence data is hard coded in this sample.
		// Add the geofences to be monitored by geofencing service.
		builder.addGeofences(buildGeofenceList());

		// Return a GeofencingRequest.
		return builder.build();
	}

	/**
	 * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
	 * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
	 * current list of geofences.
	 *
	 * @return A PendingIntent for the IntentService that handles geofence transitions.
	 */
	private PendingIntent getGeofencePendingIntent() {
		// Reuse the PendingIntent if we already have it.
		if (mGeofencePendingIntent != null) {
			return mGeofencePendingIntent;
		}
		Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
		// We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
		// addGeofences() and removeGeofences().
		return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	/**
	 * This sample hard codes geofence data. A real app might dynamically create geofences based on
	 * the user's location.
	 */
	public List<Geofence> buildGeofenceList() {
		List<Geofence> geofenceList = new ArrayList<>();
		for (Map.Entry<String, LatLng> entry : Constants.BAY_AREA_LANDMARKS.entrySet()) {

			geofenceList.add(new Geofence.Builder()
					// Set the request ID of the geofence. This is a string to identify this
					// geofence.
					.setRequestId(entry.getKey())

					// Set the circular region of this geofence.
					.setCircularRegion(
							entry.getValue().latitude,
							entry.getValue().longitude,
							Constants.GEOFENCE_RADIUS_IN_METERS
					)

					// Set the expiration duration of the geofence. This geofence gets automatically
					// removed after this period of time.
					.setExpirationDuration(Geofence.NEVER_EXPIRE)

					// Set the transition types of interest. Alerts are only generated for these
					// transition. We track entry and exit transitions in this sample.
					.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
							Geofence.GEOFENCE_TRANSITION_EXIT)

					// Create the geofence.
					.build());
		}
		return geofenceList;
	}



	/**
	 * Runs when a GoogleApiClient object successfully connects.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.i(GeoFenceHandler.class.getSimpleName(), "Connected to GoogleApiClient");
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Refer to the javadoc for ConnectionResult to see what error codes might be returned in
		// onConnectionFailed.
		Log.i(GeoFenceHandler.class.getSimpleName(), "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
	}

	@Override
	public void onConnectionSuspended(int cause) {
		// The connection to Google Play services was lost for some reason.
		Log.i(GeoFenceHandler.class.getSimpleName(), "Connection suspended");

		// onConnected() will be called again automatically when the service reconnects
	}


	/**
	 * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
	 * Either method can complete successfully or with an error.
	 *
	 * Since this activity implements the {@link ResultCallback} interface, we are required to
	 * define this method.
	 *
	 * @param status The Status returned through a PendingIntent when addGeofences() or
	 *               removeGeofences() get called.
	 */
	public void handleResult(Status status) {
		if (status.isSuccess()) {
			// Update state and save in shared preferences.
			// Retrieve an instance of the SharedPreferences object.

			// Update the UI. Adding geofences enables the Remove Geofences button, and removing
			// geofences enables the Add Geofences button.
			Log.d(this.getClass().getSimpleName(), "Status: " +status.getStatusMessage());
		} else {
			// Get the status code for the error and log it using a user-friendly message.
			String errorMessage = GeofenceErrorMessages.getErrorString(context,
					status.getStatusCode());
			Log.e(GeoFenceHandler.class.getSimpleName(), errorMessage);
		}
	}

	private void setStatus(boolean hasStarted) {
		SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = mSharedPreferences.edit();
		editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, hasStarted);
		editor.apply();
	}

	public boolean hasStarted() {
		SharedPreferences mSharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);

		return mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);
	}
}
