package com.processor.codegenerator.gui.controller;

import java.util.LinkedList;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.annotation.AnnotationCommandHandlerAdapter;

import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class ClassControllerBuilder {

	AxonAnnotatedClass axonAnnotatedClass;

	public ClassControllerBuilder(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
	}

	protected FieldSpec view() {

		ClassName view = ClassName.get(this.axonAnnotatedClass.getPackageName()
				+ ".gui.view", this.axonAnnotatedClass.getClassName() + "View");
		ParameterizedTypeName parameterizedList = ParameterizedTypeName.get(
				ClassName.get(LinkedList.class), view);
		FieldSpec field = FieldSpec.builder(parameterizedList, "view")
				.addModifiers(Modifier.PRIVATE).build();
		return field;
	}

	protected FieldSpec facade() {
		ClassName facade = ClassName.get(
				this.axonAnnotatedClass.getPackageName(),
				this.axonAnnotatedClass.getClassName() + "Facade");

		FieldSpec field = FieldSpec.builder(facade, "facade")
				.addModifiers(Modifier.PRIVATE).build();
		return field;
	}

	protected FieldSpec model() {
		ClassName model = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + ".gui.model",
				this.axonAnnotatedClass.getClassName() + "Model");

		FieldSpec field = FieldSpec.builder(model, "model")
				.addModifiers(Modifier.PRIVATE).build();
		return field;
	}

	public MethodSpec addView() {
		ClassName view = ClassName.get(this.axonAnnotatedClass.getPackageName()
				+ ".gui.view", this.axonAnnotatedClass.getClassName() + "View");
		return MethodSpec.methodBuilder("addView")
				.addModifiers(Modifier.PUBLIC).addParameter(view, "classView")
				.addStatement("this.view.add(classView)").build();

	}

	public MethodSpec save() {
		MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("save")
				.addParameter(String.class, "id").addModifiers(Modifier.PUBLIC);
		methodBuilder
				.addStatement("model.addToCopiedRepo(model.returnAggregateById(id))");
		methodBuilder.addStatement("notifyView()");
		return methodBuilder.build();

	}

	public MethodSpec notifyViewDefault() {

		return MethodSpec.methodBuilder("notifyView")
				.addModifiers(Modifier.PUBLIC).build();

	}

	public MethodSpec notifyView() {
		ClassName aggregate = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + ".aggregate",
				this.axonAnnotatedClass.getClassName() + "RootAggregate");

		ClassName classView = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + ".gui.view",
				this.axonAnnotatedClass.getClassName() + "View");
		String classViewInstance = this.axonAnnotatedClass.getLowerClassName()
				+ "View";

		MethodSpec.Builder methodBuilder = MethodSpec
				.methodBuilder("notifyView")
				.addParameter(aggregate, "aggregate")
				.addModifiers(Modifier.PUBLIC);

		methodBuilder
				.beginControlFlow("for($T $L : view)", classView,
						classViewInstance)
				.addStatement("$L.updateView(aggregate)", classViewInstance)
				.endControlFlow();

		return methodBuilder.build();

	}

	public MethodSpec returnAggregatebyId() {
		ClassName aggregateRoot = ClassName.get(
				axonAnnotatedClass.getPackageName() + ".aggregate",
				axonAnnotatedClass.getClassName() + "RootAggregate");
		return MethodSpec.methodBuilder("returnAggregateById")
				.addParameter(String.class, "id").returns(aggregateRoot)
				.addStatement("return this.model.returnAggregateById(id)")
				.addModifiers(Modifier.PUBLIC).build();

	}

	public MethodSpec undo() {
		ClassName aggregateRoot = ClassName.get(
				axonAnnotatedClass.getPackageName() + ".aggregate",
				axonAnnotatedClass.getClassName() + "RootAggregate");

		ParameterizedTypeName parameterizedList = ParameterizedTypeName.get(
				LinkedList.class, Object.class);

		MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("undo")
				.addModifiers(Modifier.PUBLIC).addParameter(String.class, "id");

		methodBuilder
				.beginControlFlow("try")
				.addStatement(
						"$T unSavedCommands = ($T)this.facade.getUnsavedCommands()",
						parameterizedList, parameterizedList)
				.addStatement(
						"unSavedCommands.remove(unSavedCommands.size()-1)")
				.addStatement(
						"UserRootAggregate copiedAggregate = new $T(model.returnOriginalCopiedAggregateById(id));",
						aggregateRoot)
				.addStatement("$T adapter = new $T(copiedAggregate)",
						AnnotationCommandHandlerAdapter.class,
						AnnotationCommandHandlerAdapter.class)
				.beginControlFlow("for (Object command: unSavedCommands)")
				.addStatement(
						"adapter.handle($T.asCommandMessage(command), null);",
						GenericCommandMessage.class)
				.addStatement("this.notifyView(new $T(copiedAggregate))",
						aggregateRoot)
				.endControlFlow()
				.addStatement(
						"model.addToEditableRepo(new $T(copiedAggregate))",
						aggregateRoot).endControlFlow()
				.beginControlFlow("catch (Throwable e)")
				.addStatement("throw new RuntimeException(e)")
				.endControlFlow();

		return methodBuilder.build();

	}

	public MethodSpec redo() {
		ParameterizedTypeName parameterizedList = ParameterizedTypeName.get(
				LinkedList.class, Object.class);

		MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("redo")
				.addModifiers(Modifier.PUBLIC);
		methodBuilder.addStatement(
				"$T unSavedCommands = this.facade.getUnsavedCommands()",
				parameterizedList);
		methodBuilder
				.addStatement("Object command = unSavedCommands.get(unSavedCommands.size()-1)");

		methodBuilder.addStatement("facade.send(command)");

		methodBuilder.addStatement("unSavedCommands.add(command)");

		return methodBuilder.build();

	}

	public TypeSpec getClassController() {

		TypeSpec.Builder viewInterfaceBuilder = TypeSpec
				.classBuilder(
						this.axonAnnotatedClass.getClassName() + "Controller")
				.addField(this.model()).addField(this.facade())
				.addField(this.view())
				.addJavadoc("Auto generated! Do not Modify!").addJavadoc("\n")
				.addModifiers(Modifier.PUBLIC);

		viewInterfaceBuilder.addMethod(this.save()).addMethod(this.undo())
				.addMethod(this.redo()).addMethod(this.returnAggregatebyId())
				.addMethod(this.notifyViewDefault())
				.addMethod(this.notifyView()).addMethod(this.addView());
		// modifier
		for (AxonAnnotatedMethod axonAnnotatedMethod : axonAnnotatedClass
				.getClassModifierMethods()) {
			viewInterfaceBuilder.addMethod(this
					.sendCommand(axonAnnotatedMethod));

		}
		// constructors
		for (AxonAnnotatedMethod axonAnnotatedMethod : axonAnnotatedClass
				.getClassConstructorMethods()) {
			viewInterfaceBuilder.addMethod(this
					.sendCommandForConstructor(axonAnnotatedMethod));
		}
		return viewInterfaceBuilder.build();
	}

	private MethodSpec sendCommandForConstructor(
			AxonAnnotatedMethod axonAnnotatedMethod) {
		String methodName = "create" + axonAnnotatedClass.getClassName();
		String commandName = "send" + "Create"
				+ axonAnnotatedClass.getClassName() + "Command";
		MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
				.addModifiers(Modifier.PUBLIC).returns(String.class);
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
		methodBuilder.addStatement("return facade.$L($L)", commandName,
				commandParam);

		return methodBuilder.build();
	}

	private MethodSpec sendCommand(AxonAnnotatedMethod axonAnnotatedMethod) {
		String methodName = axonAnnotatedMethod.getMethodName();
		String sendCommandName = "send"
				+ axonAnnotatedMethod.getCapitalMethodName() + "Command";
		String saveCommandName = "save"
				+ axonAnnotatedMethod.getCapitalMethodName() + "Command";
		MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
				.addModifiers(Modifier.PUBLIC);
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
		methodBuilder.addStatement("facade.$L($L)", sendCommandName, commandParam);
		methodBuilder.addStatement("facade.$L($L)", saveCommandName, commandParam);

		return methodBuilder.build();
	}

}
