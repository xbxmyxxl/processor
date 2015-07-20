package com.processor.codegenerator.facade;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;

import com.processor.codegenerator.aggregate.AggregateBuilderHelper;
import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class FacadeBuilder {

	private AxonAnnotatedClass axonAnnotatedClass;

	public FacadeBuilder(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
	}

	public TypeSpec getFacadeClass() {
		// getting the map

		// set up the helper
		FacadeBuilderHelper facadeBuilderHelper = new FacadeBuilderHelper(
				axonAnnotatedClass);
		String className = axonAnnotatedClass.getClassName();
		className = className + "Facade";

		// add the fields
		TypeSpec.Builder classBuilder = TypeSpec
				.classBuilder(className)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(
						FieldSpec
								.builder(CommandGateway.class, "commandGateway")
								.addModifiers(Modifier.PRIVATE, Modifier.FINAL)
								.build())
				.addField(facadeBuilderHelper.fieldUnSavedCommandList());
		classBuilder.addMethod(facadeBuilderHelper.constructor());

		/* directly send the command */
		// modifier
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassModifierMethods()) {
			classBuilder.addMethod(facadeBuilderHelper
					.sendCommand(annotatedMethod));
		}

		// constructors
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassConstructorMethods()) {
			classBuilder.addMethod(facadeBuilderHelper
					.sendCommandForConstructor(annotatedMethod));
		}

		/* save the command to a list */
		// modifier
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassModifierMethods()) {
			classBuilder.addMethod(facadeBuilderHelper
					.saveCommand(annotatedMethod));
		}

		// constructors
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassConstructorMethods()) {
			classBuilder.addMethod(facadeBuilderHelper
					.saveCommandForConstructor(annotatedMethod));
		}

		classBuilder.addMethod(facadeBuilderHelper.clearUnsavedCommands());
		classBuilder.addMethod(facadeBuilderHelper.getUnsavedCommands());
		classBuilder.addMethod(facadeBuilderHelper.send());
		classBuilder.addJavadoc("Auto generated! Do not Modify!").addJavadoc(
				"\n");

		TypeSpec command = classBuilder.build();

		return command;
	}
}
