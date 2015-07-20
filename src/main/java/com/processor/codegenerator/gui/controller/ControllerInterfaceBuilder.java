package com.processor.codegenerator.gui.controller;

import java.util.LinkedList;

import javax.lang.model.element.Modifier;

import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class ControllerInterfaceBuilder {

	AxonAnnotatedClass axonAnnotatedClass;

	public ControllerInterfaceBuilder(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
	}

	public MethodSpec save() {
		return MethodSpec.methodBuilder("save")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}

	public MethodSpec notifyView() {
		return MethodSpec.methodBuilder("notifyView")
				.addParameter(Object.class, "object")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}

	public MethodSpec returnAggregateById() {
		return MethodSpec.methodBuilder("returnAggregateById")
				.addParameter(String.class, "id")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}

	public MethodSpec undo() {
		return MethodSpec.methodBuilder("undo")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}

	public MethodSpec redo() {
		return MethodSpec.methodBuilder("redo")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}

	public TypeSpec getInterface() {

		TypeSpec.Builder viewInterfaceBuilder = TypeSpec
				.interfaceBuilder("Controller")
				.addJavadoc("Auto generated! Do not Modify!").addJavadoc("\n")
				.addModifiers(Modifier.PUBLIC);

		viewInterfaceBuilder.addMethod(this.save()).addMethod(this.undo())
				.addMethod(this.redo()).addMethod(this.returnAggregateById())
				.addMethod(this.notifyView());
		return viewInterfaceBuilder.build();
	}

}
