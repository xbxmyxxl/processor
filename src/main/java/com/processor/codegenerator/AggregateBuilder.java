package com.processor.codegenerator;

import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;

import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class AggregateBuilder {

	private AxonAnnotatedClass axonAnnotatedClass;

	public AggregateBuilder(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
	}

	public TypeSpec getAggregateClass() {
		// getting the map

		// set up the helper
		AggregateBuilderHelper aggregateBuilderHelper = new AggregateBuilderHelper(
				axonAnnotatedClass);
		String className = axonAnnotatedClass.getClassName();
		String aggregateName = className + "RootAggregate";
		TypeMirror classType = axonAnnotatedClass.getClassType();

		// add the fields
		TypeSpec.Builder classBuilder = TypeSpec.classBuilder(aggregateName)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(aggregateBuilderHelper.fieldID())
				.addField(aggregateBuilderHelper.field(classType, className));

		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassModifierMethods()) {
			classBuilder.addMethod(aggregateBuilderHelper.commandHandler(annotatedMethod));
			classBuilder.addMethod(aggregateBuilderHelper.eventHandler(annotatedMethod));
		}
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassAccessorMethods()) {
			classBuilder.addMethod(aggregateBuilderHelper.accessMethod(annotatedMethod));
		}
		
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassConstructorMethods()) {
			classBuilder.addMethod(aggregateBuilderHelper.eventHandlerForConstructor(annotatedMethod));
		}

		// adding constructor
		classBuilder.addMethod(aggregateBuilderHelper.constructor());
		classBuilder.superclass(AbstractAnnotatedAggregateRoot.class);
		classBuilder.addJavadoc("Auto generated! Do not Modify!").addJavadoc("\n");

		TypeSpec command = classBuilder.build();

		return command;
	}
}
