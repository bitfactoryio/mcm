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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;

import de.greenrobot.event.EventBus;
import io.bitfactory.mobileclimatemonitor.event.HumidityEvent;
import io.bitfactory.mobileclimatemonitor.event.TemperatureEvent;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

	@SystemService
	protected AlarmManager alarmManager;

	@Bean
	protected SlackReporter slackReporter;

	@ViewById(R.id.startBtn)
	protected Button startBtn;

	@ViewById(R.id.stopBtn)
	protected Button stopBtn;

	@ViewById(R.id.statusTxt)
	protected TextView statusTxt;

	@ViewById(R.id.temperatureTxt)
	protected TextView temperatureTxt;

	@ViewById(R.id.humidityTxt)
	protected TextView humidityTxt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		EventBus.getDefault().register(this);
	}

	@AfterViews
	protected void initMainActivity() {
	}

	@Override
	protected void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	@Click(R.id.startBtn)
	protected void onStartBtnClick() {
		startSlackReport(9);
		startSlackReport(13);
		startSlackReport(18);

		statusTxt.setText("alarms running for 9am, 1pm, 6pm");
	}

	@Click(R.id.stopBtn)
	protected void onStopBtnClick() {
		cancelSlackReport(9);
		cancelSlackReport(13);
		cancelSlackReport(18);

		statusTxt.setText("alarms canceled for 9am, 1pm, 6pm");
	}

	@Click(R.id.nowBtn)
	protected void onNowBtnClick() {
		Intent intent = new Intent(this, SensorRunner_.class);
		sendBroadcast(intent);
	}

	private void startSlackReport(int hour) {
		Calendar calendar = Calendar.getInstance();

		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, hour, new Intent(this, SensorRunner_.class), PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
	}

	private void cancelSlackReport(int hour) {
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, hour, new Intent(this, SensorRunner_.class), PendingIntent.FLAG_UPDATE_CURRENT);

		try {
			alarmManager.cancel(pendingIntent);
		} catch (Exception e) {
			Log.e(MainActivity.class.getSimpleName(), "AlarmManager update was not canceled. " + e.toString());
		}

	}

	public void onEventMainThread(TemperatureEvent event) {
		temperatureTxt.setText(Helper.formatTemperature(event.getTemperature()));
	}

	public void onEventMainThread(HumidityEvent event) {
		humidityTxt.setText(Helper.formatHumidity(event.getHumidity()));
	}
}
