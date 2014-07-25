package com.cumulocity.tixi.server.model.txml;

import static com.cumulocity.tixi.server.components.txml.TXMLDateAdapter.dateFormatter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class LogBuilder {
	
	private static final AtomicLong seq = new AtomicLong();
	
	private final Log log = new Log();
	private RecordItemSet itemSet;
	
	public static LogBuilder aLog() {
		return new LogBuilder();
	}
	
	public LogBuilder withNewRecordItemSet(String id, String dateTime) throws Exception {
		return withNewRecordItemSet(id, dateFormatter().parse(dateTime));
	}
	
	public LogBuilder withNewRecordItemSet(Date date) throws Exception {
		return withNewRecordItemSet("record_" + seq.getAndIncrement(), date);
	}
	
	public LogBuilder withNewRecordItemSet(String id, Date date) throws Exception {
		itemSet = new RecordItemSet(id, date);
		log.getRecordItemSets().add(itemSet);
		return this;
	}
	
	
	public LogBuilder withId(String id) {
		log.setId(id);
		return this;
	}
	
	public LogBuilder withRecordItem(String id, BigDecimal value) {
		RecordItem logItem = new RecordItem(id, value);
		itemSet.getRecordItems().add(logItem);
		return this;
	}
	
	public Log build() {
		return log;
	}
}
