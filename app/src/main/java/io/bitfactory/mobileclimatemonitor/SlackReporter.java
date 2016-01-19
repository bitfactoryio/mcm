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

import android.util.Log;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.res.StringRes;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import de.greenrobot.event.EventBus;
import io.bitfactory.mobileclimatemonitor.event.HumidityEvent;
import io.bitfactory.mobileclimatemonitor.event.TemperatureEvent;

/**
 * @author andreas@bitfactory.io (Andreas Gawelczyk)
 */
@EBean
public class SlackReporter {

	@StringRes(R.string.slack_webhook_url)
	protected String slackWebhookUrl;

	@StringRes(R.string.slack_channel)
	protected String slackChannel;

	@StringRes(R.string.slack_sender_name)
	protected String slackSenderName;

	private float temperatureValue = 0;

	private float humidityValue = 0;

	@AfterInject
	protected void initSlackReporter() {
		EventBus.getDefault().register(this);
	}

	public void onEventMainThread(TemperatureEvent event) {
		temperatureValue = event.getTemperature();
		sendValuesToSlack();
	}

	public void onEventMainThread(HumidityEvent event) {
		humidityValue = event.getHumidity();
		sendValuesToSlack();
	}

	@Background
	protected void sendValuesToSlack() {

		if (temperatureValue == 0) {
			return;
		}

		if (humidityValue == 0) {
			return;
		}

		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("text", Helper.formatTemperature(temperatureValue)
					+"\n"
					+Helper.formatHumidity(humidityValue));
			jsonObject.put("channel", slackChannel);
			jsonObject.put("username", slackSenderName);
			jsonObject.put("icon_emoji", ":cubimal_chick:");

			MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

			Request request = new Request.Builder()
					.url(new URL(slackWebhookUrl))
					.post(RequestBody.create(MEDIA_TYPE_JSON, jsonObject.toString()))
					.build();

			new OkHttpClient().newCall(request).execute();


		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}

		temperatureValue = 0;
		humidityValue = 0;
	}
}
