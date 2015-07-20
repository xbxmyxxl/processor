package com.processor.codegenerator.gui.view;

import java.util.LinkedList;

import javax.lang.model.element.Modifier;

import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class ViewInterfaceBuilder {

	AxonAnnotatedClass axonAnnotatedClass;

	public ViewInterfaceBuilder(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
	}

	public MethodSpec fillView() {
		return MethodSpec.methodBuilder("fillView")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}

	public MethodSpec addController() {
		ClassName controller = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + ".gui.controller",
				"Controller");

		return MethodSpec.methodBuilder("addController")
				.addParameter(controller, "controller", Modifier.FINAL)
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}

	public MethodSpec updateView() {
		return MethodSpec.methodBuilder("updateView")
				.addParameter(ArrayTypeName.get(Object.class), "object")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}

	public MethodSpec initializeView() {
		return MethodSpec.methodBuilder("initializeView")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}

	public TypeSpec getInterface() {

		TypeSpec.Builder viewInterfaceBuilder = TypeSpec
				.interfaceBuilder("View")
				.addJavadoc("Auto generated! Do not Modify!").addJavadoc("\n")
				.addModifiers(Modifier.PUBLIC);

		viewInterfaceBuilder.addMethod(this.fillView())
				.addMethod(this.initializeView()).addMethod(this.updateView())
				.addMethod(this.addController());
		return viewInterfaceBuilder.build();
	}

}
