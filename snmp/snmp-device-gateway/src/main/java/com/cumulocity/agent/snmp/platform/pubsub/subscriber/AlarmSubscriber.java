package com.cumulocity.agent.snmp.platform.pubsub.subscriber;

import com.cumulocity.agent.snmp.config.GatewayProperties;
import com.cumulocity.agent.snmp.platform.pubsub.service.AlarmPubSub;
import com.cumulocity.sdk.client.alarm.AlarmApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlarmSubscriber extends Subscriber<AlarmPubSub> {

	@Autowired
	private GatewayProperties gatewayProperties;

	@Autowired
	private AlarmApi alarmApi;

	@Override
	public int getConcurrentSubscriptionsCount() {
		// 10% of the total threads available for gateway
		int count = gatewayProperties.getGatewayThreadPoolSize() * 10 / 100;

		return (count <= 0) ? 1 : count;
	}

	@Override
	public boolean isBatchingSupported() {
		return false;
	}

	@Override
	public int getBatchSize() {
		throw new UnsupportedOperationException("Batching is not supported for Alarms");
	}

	@Override
	public void handleMessage(String message) {
		alarmApi.create(new AlarmRepresentation(message));
	}

	public static class AlarmRepresentation extends com.cumulocity.rest.representation.alarm.AlarmRepresentation {

		private String jsonString;

		public AlarmRepresentation() {
		}

		AlarmRepresentation(String jsonString) {
			this.jsonString = jsonString;
		}

		@Override
		public String toJSON() {
			return jsonString;
		}
	}
}
