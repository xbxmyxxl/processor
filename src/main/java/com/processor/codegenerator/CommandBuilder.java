package com.processor.codegenerator;

import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class CommandBuilder {
	
	private AxonAnnotatedMethod axonAnnotatedMethod;

	public CommandBuilder(AxonAnnotatedMethod axonAnnotatedMethod) {
		super();
		this.axonAnnotatedMethod = axonAnnotatedMethod;
	}

	public TypeSpec getCommandClass() {
		//getting the map
		Map<String, TypeMirror> methodParam = axonAnnotatedMethod
				.getMethodParam();
		
		//set up the helper
		EventCommandBuilderHelper commandBuilderHelper = new EventCommandBuilderHelper(
				axonAnnotatedMethod);
		String className = axonAnnotatedMethod.getMethodName();
		String commandName = Character.toUpperCase(className.charAt(0)) + className.substring(1)+ "Command";

		Builder classBuilder = TypeSpec.classBuilder(commandName)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(commandBuilderHelper.fieldID("command"));

		//adding fields
		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			classBuilder.addField(commandBuilderHelper.field(entry.getValue(),
					entry.getKey()));

		}
		
		//adding constructor
		classBuilder.addMethod(commandBuilderHelper.constructor());

		TypeSpec command = classBuilder.build();

		return command;
	}
}
