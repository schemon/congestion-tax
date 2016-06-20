/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package toll.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.simarv.toll.R;
import toll.db.Settings;
import toll.fee.Vehicle;
import toll.geofence.GeoFenceHandler;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;

/**
 */
public class MainActivity extends AppCompatActivity {

	protected static final String TAG = "MainActivity";

	private GeoFenceHandler geoFenceHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		geoFenceHandler = new GeoFenceHandler(this);
		if (geoFenceHandler.hasStarted()) {
			finishAndStartListActivity();
		}
	}

	private void finishAndStartListActivity() {
		startActivity(new Intent(this, ListActivity.class));
		finish();
	}

	public void startGeofenceCar(View view) {
		new Settings(this).setVehicle(Vehicle.Car);
		addGeofenceAndCheckForPermission();
	}

	public void startGeofenceOther(View view) {
		new Settings(this).setVehicle(Vehicle.Other);
		addGeofenceAndCheckForPermission();
	}

	private void addGeofenceAndCheckForPermission() {
		if (hasPermission()) {
			addGeoFence();
		} else {
			askForPermission();
		}
	}

	private boolean hasPermission() {
		return ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED;
	}

	private void askForPermission() {
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
			new AlertDialog.Builder(this)
					.setMessage(R.string.Location_is_really_needed)
					.setPositiveButton(R.string.Request, new DialogInterface.OnClickListener() {
						@Override public void onClick(DialogInterface dialog, int which) {
							requestPermission();
						}
					}).show();
		} else {
			requestPermission();
		}
	}

	private void requestPermission() {
		ActivityCompat.requestPermissions(
				this,
				new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
				1124);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (requestCode == 1124) {
			addGeoFence();
		}
	}

	private void addGeoFence() {
		if (!geoFenceHandler.hasStarted()) {
			geoFenceHandler.start(new ResultCallback() {
				@Override public void onResult(Result result) {
					if (geoFenceHandler.hasStarted()) {
						finishAndStartListActivity();
					}
				}
			});
		}
	}

}
