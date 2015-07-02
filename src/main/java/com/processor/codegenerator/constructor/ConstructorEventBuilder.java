package com.processor.codegenerator.constructor;

import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class ConstructorEventBuilder {
	
	private AxonAnnotatedMethod axonAnnotatedMethod;
	String className;

	public ConstructorEventBuilder(String className,AxonAnnotatedMethod axonAnnotatedMethod) {
		super();
		this.axonAnnotatedMethod = axonAnnotatedMethod;
		this.className = className;
	}

	public TypeSpec getEventClass() {
		//getting the map
		Map<String, TypeMirror> methodParam = axonAnnotatedMethod
				.getMethodParam();
		
		//set up the helper
		ConstructorBuilderHelper eventBuilderHelper = new ConstructorBuilderHelper(
				axonAnnotatedMethod);

		String commandName = className
				+ "CreatedEvent";
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

		TypeSpec event = classBuilder.build();

		return event;
	}
}
