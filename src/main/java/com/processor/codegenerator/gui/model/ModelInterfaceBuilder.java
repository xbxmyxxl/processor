package com.processor.codegenerator.gui.model;

import java.util.LinkedList;

import javax.lang.model.element.Modifier;

import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class ModelInterfaceBuilder {

	AxonAnnotatedClass axonAnnotatedClass;

	public ModelInterfaceBuilder(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
	}

	public MethodSpec sychronizeChangeById() {
		return MethodSpec.methodBuilder("sychronizedChangeById")
				.addParameter(String.class, "id")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}


	public MethodSpec returnAggregateById() {
		return MethodSpec.methodBuilder("returnAggregateById")
				.addParameter(String.class, "id")
				.returns(Object.class)
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}


	public TypeSpec getInterface() {

		TypeSpec.Builder viewInterfaceBuilder = TypeSpec
				.interfaceBuilder("Model")
				.addJavadoc("Auto generated! Do not Modify!").addJavadoc("\n")
				.addModifiers(Modifier.PUBLIC);

		viewInterfaceBuilder.addMethod(this.returnAggregateById())
				.addMethod(this.sychronizeChangeById());
		return viewInterfaceBuilder.build();
	}

}
