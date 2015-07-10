package com.processor.codegenerator.eventcommand;

import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class EventBuilder {
	
	private AxonAnnotatedMethod axonAnnotatedMethod;

	public EventBuilder(AxonAnnotatedMethod axonAnnotatedMethod) {
		super();
		this.axonAnnotatedMethod = axonAnnotatedMethod;
	}

	public TypeSpec getEventClass() {
		//getting the map
		Map<String, TypeMirror> methodParam = axonAnnotatedMethod
				.getMethodParam();
		
		//set up the helper
		CommandEventBuilderHelper eventBuilderHelper = new CommandEventBuilderHelper(
				axonAnnotatedMethod);
		String className = axonAnnotatedMethod.getMethodName();
		String commandName = Character.toUpperCase(className.charAt(0)) + className.substring(1)
				+ "CompletedEvent";
		Builder classBuilder = TypeSpec.classBuilder(commandName)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(eventBuilderHelper.fieldID("event"));

		//adding fields
		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			classBuilder.addField(eventBuilderHelper.field(entry.getValue(),
					entry.getKey()));

		}
		
		//adding constructor
		classBuilder.addMethod(eventBuilderHelper.constructor());

		classBuilder.addJavadoc("Auto generated! Do not Modify!").addJavadoc("\n");

		TypeSpec event = classBuilder.build();

		return event;
	}
}
