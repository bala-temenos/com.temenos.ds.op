package com.temenos.ds.op.xtext.generator.ui;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

public class GenerationTimeLogger {

	public static final String TIME = "time";
	public static final String COUNT = "resources";

	private static GenerationTimeLogger timeLogger;
	private XMLMemento memento;

	private GenerationTimeLogger() {
		memento = XMLMemento.createWriteRoot("Generators");
	}

	public static GenerationTimeLogger getInstance() {
		if (timeLogger == null) {
			timeLogger = new GenerationTimeLogger();
		}
		return timeLogger;
	}

	public XMLMemento getMemento() {
		return memento;
	}

	public void updateTime(String id, int time) {
		IMemento idMemento = memento.getChild(id);
		if (idMemento == null) {
			idMemento = memento.createChild(id);
		}
		if(idMemento.getInteger(TIME) == null){
			idMemento.putInteger(TIME, time);
		} else {
			int duration = idMemento.getInteger(TIME);
			idMemento.putInteger(TIME, duration + time);
		}
	}
	
	public void updateCount(String id, int count){
		IMemento idMemento = memento.getChild(id);
		if (idMemento == null) {
			idMemento = memento.createChild(id);
		}
		if(idMemento.getInteger(COUNT) == null){
			idMemento.putInteger(COUNT, count);
		} else {
			int oldCount = idMemento.getInteger(COUNT);
			idMemento.putInteger(COUNT, oldCount + count);
		}
	}
	
	public void reset(){
		if(memento.getChildren() != null && memento.getChildren().length != 0)
		for(IMemento generator : memento.getChildren()){
			generator.putInteger(COUNT, 0);
			generator.putInteger(TIME, 0);
		}
	}
}
