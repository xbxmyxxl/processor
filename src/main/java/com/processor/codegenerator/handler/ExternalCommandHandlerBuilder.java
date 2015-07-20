package com.processor.codegenerator.handler;


import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.repository.Repository;

import com.processor.codegenerator.aggregate.AggregateBuilderHelper;
import com.processor.codegenerator.facade.FacadeBuilderHelper;
import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class ExternalCommandHandlerBuilder {

	private AxonAnnotatedClass axonAnnotatedClass;

	public ExternalCommandHandlerBuilder(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
	}

	public TypeSpec getHandler() {
		// getting the map

		// set up the helper
		ExternalCommandHandlerBuilderHelper handlerBuilderHelper = new ExternalCommandHandlerBuilderHelper(
				axonAnnotatedClass);
		String className = axonAnnotatedClass.getClassName();
		className = className + "CommandHandler";
		TypeMirror classType = axonAnnotatedClass.getClassType();

		// add the fields
		TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
				.addModifiers(Modifier.PUBLIC)
				.addField(handlerBuilderHelper.repositoryField())
		.addField(handlerBuilderHelper.validatorField());
		
		classBuilder.addMethod(handlerBuilderHelper.constructor());
		classBuilder.addMethod(handlerBuilderHelper.defaultConstructor());

		//modifier
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassModifierMethods()) {
			classBuilder.addMethod(handlerBuilderHelper.commandHandler(annotatedMethod));
		}
		
		//constructors
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassConstructorMethods()) {
			classBuilder.addMethod(handlerBuilderHelper.commandHandlerForConstructor(annotatedMethod));
		}
		classBuilder.addMethod(handlerBuilderHelper.loadRootAggregateById());
		classBuilder.addJavadoc("Auto generated! Do not Modify!").addJavadoc("\n");

		TypeSpec command = classBuilder.build();

		return command;
	}
}
