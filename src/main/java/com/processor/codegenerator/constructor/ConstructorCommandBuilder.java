package com.processor.codegenerator.constructor;

import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class ConstructorCommandBuilder {

	private AxonAnnotatedMethod axonAnnotatedMethod;
	String className;

	public ConstructorCommandBuilder(String className,
			AxonAnnotatedMethod axonAnnotatedMethod) {
		super();
		this.axonAnnotatedMethod = axonAnnotatedMethod;
		this.className = className;
	}

	public TypeSpec getCommandClass() {
		// getting the map
		Map<String, TypeMirror> methodParam = axonAnnotatedMethod
				.getMethodParam();

		// set up the helper
		ConstructorBuilderHelper commandBuilderHelper = new ConstructorBuilderHelper(
				axonAnnotatedMethod);

		String commandName = "Create" + className + "Command";
		Builder classBuilder = TypeSpec.classBuilder(commandName)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(commandBuilderHelper.fieldID("command"));

		// adding fields
		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			classBuilder.addField(commandBuilderHelper.field(entry.getValue(),
					entry.getKey()));

		}

		// adding constructor
		classBuilder.addMethod(commandBuilderHelper.constructor());
		classBuilder.addJavadoc("Auto generated! Do not Modify!").addJavadoc("\n");

		TypeSpec command = classBuilder.build();

		return command;
	}
}
