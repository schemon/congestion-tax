package toll;

import android.content.Context;
import android.content.SharedPreferences;

import toll.fee.Vehicle;

/**
 * Created by simarv on 2016-06-11.
 */
public class Settings {

	private final Context context;

	public Settings(Context context) {
		this.context = context;
	}


	private SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences("settings", Context.MODE_PRIVATE);
	}


	public void setVehicle(Vehicle vehicle) {
		getSharedPreferences().edit().putString("vehicle", vehicle.toString()).apply();
	}

	public Vehicle getVehicle() {
		return Vehicle.valueOf(getSharedPreferences().getString("vehicle", Vehicle.Unknown.toString()));
	}



}
