/*
 * ██████╗ ██╗████████╗███████╗ █████╗  ██████╗████████╗ ██████╗ ██████╗ ██╗   ██╗
 * ██╔══██╗██║╚══██╔══╝██╔════╝██╔══██╗██╔════╝╚══██╔══╝██╔═══██╗██╔══██╗╚██╗ ██╔╝
 * ██████╔╝██║   ██║   █████╗  ███████║██║        ██║   ██║   ██║██████╔╝ ╚████╔╝
 * ██╔══██╗██║   ██║   ██╔══╝  ██╔══██║██║        ██║   ██║   ██║██╔══██╗  ╚██╔╝
 * ██████╔╝██║   ██║   ██║     ██║  ██║╚██████╗   ██║   ╚██████╔╝██║  ██║   ██║
 * ╚═════╝ ╚═╝   ╚═╝   ╚═╝     ╚═╝  ╚═╝ ╚═════╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝   ╚═╝
 *
 * Copyright (c) 2015 Bitfactory UG (haftungsbeschränkt)
 * https://www.bitfactory.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.bitfactory.mobileclimatemonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.androidannotations.annotations.EReceiver;
import org.androidannotations.annotations.SystemService;

import de.greenrobot.event.EventBus;
import io.bitfactory.mobileclimatemonitor.event.HumidityEvent;
import io.bitfactory.mobileclimatemonitor.event.TemperatureEvent;

/**
 * @author andreas@bitfactory.io (Andreas Gawelczyk)
 */
@EReceiver
public class SensorRunner extends BroadcastReceiver {

	@SystemService
	protected SensorManager sensorManager;

	private Sensor tempertemperatureSensor;

	private Sensor humiditySensor;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("-AG-", "onReceive");
		initSensorRunner();
	}

	public void initSensorRunner() {
		tempertemperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		sensorManager.registerListener(temperatureSensorListener, tempertemperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);

		humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
		sensorManager.registerListener(humiditySensorListener, humiditySensor, SensorManager.SENSOR_DELAY_NORMAL);
	}

	private SensorEventListener temperatureSensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			sensorManager.unregisterListener(temperatureSensorListener);
			EventBus.getDefault().postSticky(new TemperatureEvent(event.values[0]));
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	private SensorEventListener humiditySensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			sensorManager.unregisterListener(humiditySensorListener);
			EventBus.getDefault().postSticky(new HumidityEvent(event.values[0]));
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};
}
