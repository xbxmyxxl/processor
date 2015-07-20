package com.processor.codegenerator.gui.view;

import java.util.LinkedList;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class ClassViewBuilder {

	private AxonAnnotatedClass axonAnnotatedClass;

	public ClassViewBuilder(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
	}

	public MethodSpec constructor() {
		ClassName controller = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + "gui.controller",
				this.axonAnnotatedClass.getClassName() + "Controller");
		return MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameter(controller, "controller")
				.addStatement("this.controller = controller").build();

	}

	public MethodSpec issueCommand(AxonAnnotatedMethod axonAnnotatedMethod) {
		MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(
				"issue" + axonAnnotatedMethod.getCapitalMethodName()
						+ "Command").addModifiers(Modifier.PUBLIC);
		Map<String, TypeMirror> methodParam = axonAnnotatedMethod
				.getMethodParam();

		methodBuilder.addParameter(String.class, "id");
		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			methodBuilder.addParameter(TypeName.get(entry.getValue()),
					entry.getKey());
		}

		String commandParam = "id,";
		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			commandParam += entry.getKey() + ",";
		}
		commandParam = commandParam.substring(0, commandParam.length() - 1);
		
		ClassName classController = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + ".gui.controller",
				this.axonAnnotatedClass.getClassName() + "Controller");
		String classControllerInstance = 
				this.axonAnnotatedClass.getLowerClassName() + "Controller";
		
		methodBuilder.beginControlFlow("for($T $L : controller)", classController,classControllerInstance)
		.addStatement("$L.$L($L)",classControllerInstance,axonAnnotatedMethod.getMethodName(), commandParam)
		.endControlFlow();

		return methodBuilder.build();

	}

	public MethodSpec issueCommandForConstructor(
			AxonAnnotatedMethod axonAnnotatedMethod) {
		MethodSpec.Builder methodBuilder = MethodSpec
				.methodBuilder(
						"issue" + "Create" + axonAnnotatedClass.getClassName()
								+ "Command").addModifiers(Modifier.PUBLIC)
				.returns(String.class);

		Map<String, TypeMirror> methodParam = axonAnnotatedMethod
				.getMethodParam();

		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			methodBuilder.addParameter(TypeName.get(entry.getValue()),
					entry.getKey());
		}

		String commandParam = "";
		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			commandParam += entry.getKey() + ",";
		}
		commandParam = commandParam.substring(0, commandParam.length() - 1);
		
		ClassName classController = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + ".gui.controller",
				this.axonAnnotatedClass.getClassName() + "Controller");
		String classControllerInstance = 
				this.axonAnnotatedClass.getLowerClassName() + "Controller";
		
		methodBuilder.beginControlFlow("for($T $L : controller)", classController,classControllerInstance)
		.addStatement("$L.create$L($L)",classControllerInstance,axonAnnotatedClass.getClassName(), commandParam)
		.endControlFlow();

		return methodBuilder.build();

	}

	public MethodSpec fillView() {
		return MethodSpec.methodBuilder("fillView")
				.addModifiers(Modifier.PUBLIC).build();

	}

	public MethodSpec updateView() {
		ClassName aggregate = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + ".aggregate",
				this.axonAnnotatedClass.getClassName() + "RootAggregate");
		return MethodSpec.methodBuilder("updateView")
				.addAnnotation(Override.class)
				.addParameter(Object.class, "aggregate")
				.addStatement("this.aggregate = ($T)aggregate", aggregate)
				.addModifiers(Modifier.PUBLIC).build();

	}

	public MethodSpec initializeView() {
		return MethodSpec.methodBuilder("initializeView")
				.addAnnotation(Override.class).addStatement("return")
				.addModifiers(Modifier.PUBLIC).build();

	}
	public MethodSpec addController() {
		ClassName controller = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + ".gui.controller",
				this.axonAnnotatedClass.getClassName() + "Controller");
		return MethodSpec.methodBuilder("addController")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(controller,"classController")
				.addStatement("this.controller.add(classController)").build();

	}

	protected FieldSpec controllerList() {

		ClassName controller = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + ".gui.controller",
				this.axonAnnotatedClass.getClassName() + "Controller");
		
		ParameterizedTypeName parameterizedList = ParameterizedTypeName.get(
				ClassName.get(LinkedList.class), controller);

		FieldSpec field = FieldSpec.builder(parameterizedList, "controller")
				.addModifiers(Modifier.PRIVATE).build();
		return field;

	}

	protected FieldSpec aggregate() {
		ClassName aggregate = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + ".aggregate",
				this.axonAnnotatedClass.getClassName() + "RootAggregate");

		FieldSpec field = FieldSpec.builder(aggregate, "aggregate")
				.addModifiers(Modifier.PRIVATE).build();
		return field;

	}

	public TypeSpec getClassView() {

		TypeSpec.Builder classViewBuilder = TypeSpec
				.classBuilder(this.axonAnnotatedClass.getClassName() + "View")
				.addJavadoc("Auto generated! Do not Modify!")
				.addJavadoc("\n")
				.addSuperinterface(
						ClassName.get(this.axonAnnotatedClass.getPackageName()
								+ ".gui.view", "View"))
				.addModifiers(Modifier.PUBLIC);
		classViewBuilder.addField(controllerList()).addField(aggregate());

		classViewBuilder.addMethod(this.fillView())
				.addMethod(this.initializeView()).addMethod(this.updateView())
				.addMethod(this.addController());

		// modifier
		for (AxonAnnotatedMethod axonAnnotatedMethod : axonAnnotatedClass
				.getClassModifierMethods()) {
			classViewBuilder.addMethod(this.issueCommand(axonAnnotatedMethod));

		}
		// constructors
		for (AxonAnnotatedMethod axonAnnotatedMethod : axonAnnotatedClass
				.getClassConstructorMethods()) {
			classViewBuilder.addMethod(this
					.issueCommandForConstructor(axonAnnotatedMethod));
		}
		return classViewBuilder.build();
	}
}
