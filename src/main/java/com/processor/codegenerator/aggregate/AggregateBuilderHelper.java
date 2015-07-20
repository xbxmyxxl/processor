package com.processor.codegenerator.aggregate;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.axonframework.test.Fixtures;
import org.junit.Before;
import org.junit.Test;

import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeName;

public class AggregateBuilderHelper {

	AxonAnnotatedClass axonAnnotatedClass;
	private String className;

	public AggregateBuilderHelper(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
		this.className = axonAnnotatedClass.getClassName();
	}

	protected FieldSpec field(TypeMirror type, String name) {

		FieldSpec field = FieldSpec.builder(TypeName.get(type), name)
				.addModifiers(Modifier.PRIVATE).build();
		return field;

	}

	protected FieldSpec fieldID() {
		FieldSpec fieldID = FieldSpec.builder(String.class, "id")
				.addAnnotation(AggregateIdentifier.class)
				.addModifiers(Modifier.PRIVATE).build();
		return fieldID;

	}

	protected MethodSpec commandHandler(AxonAnnotatedMethod axonAnnotatedMethod) {

		MethodSpec.Builder commandHandler = MethodSpec.methodBuilder("handle")
				.addAnnotation(CommandHandler.class);

		String eventParam;
		ClassName event;
		ClassName command;
		{
			Map<String, TypeMirror> methodParam = axonAnnotatedMethod
					.getMethodParam();

			String commandName = axonAnnotatedMethod.getCapitalMethodName()
					+ "Command";
			String eventName = axonAnnotatedMethod.getCapitalMethodName()
					+ "CompletedEvent";
			event = ClassName.get(axonAnnotatedClass.getPackageName()
					+ ".event", eventName);
			command = ClassName.get(axonAnnotatedClass.getPackageName()
					+ ".command", commandName);

			eventParam = "command.id, ";

			for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
				eventParam += "command." + entry.getKey() + ",";

			}

			eventParam = eventParam.substring(0, eventParam.length() - 1);
		}
		commandHandler.addStatement("boolean flag = true")
				.addParameter(command, "command")
				.addModifiers(Modifier.FINAL, Modifier.PUBLIC);

		for (ExecutableElement element : axonAnnotatedMethod
				.getCommandValidator()) {

			String validatorName = element.getSimpleName().toString();
			ClassName validator = ClassName.get(
					axonAnnotatedClass.getPackageName(), className);
			String validatorParam = "";
			Map<String, TypeMirror> methodValidatorParam = axonAnnotatedMethod
					.getMethodParam();
			for (Map.Entry<String, TypeMirror> entry : methodValidatorParam
					.entrySet()) {
				validatorParam += "command." + entry.getKey() + ",";

			}
			validatorParam = validatorParam.substring(0,
					validatorParam.length() - 1);

			commandHandler.addStatement("flag = flag && $L.$L($L)", className,
					validatorName, validatorParam);

		}
		commandHandler.beginControlFlow("if(flag)")
				.addStatement("apply (new $T(" + eventParam + "))", event)
				.endControlFlow();

		return commandHandler.build();
	}

	protected MethodSpec eventHandler(AxonAnnotatedMethod axonAnnotatedMethod) {
		Map<String, TypeMirror> methodParam = axonAnnotatedMethod
				.getMethodParam();
		String eventName = axonAnnotatedMethod.getCapitalMethodName()
				+ "CompletedEvent";
		String methodForEventName = axonAnnotatedMethod.getMethodName();
		ClassName event = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".event", eventName);

		String eventParam = "";

		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			eventParam += "event." + entry.getKey() + ",";

		}
		eventParam = eventParam.substring(0, eventParam.length() - 1);

		MethodSpec.Builder eventHandler = MethodSpec
				.methodBuilder("on")
				.addParameter(event, "event")
				.addModifiers(Modifier.FINAL, Modifier.PUBLIC)
				.addAnnotation(EventHandler.class)
				.addStatement("this.id = event.id")
				.addStatement(
						className + "." + methodForEventName + "(" + eventParam
								+ ")");
		return eventHandler.build();
	}

	protected MethodSpec eventHandlerForConstructor(
			AxonAnnotatedMethod axonAnnotatedMethod) {
		Map<String, TypeMirror> methodParam = axonAnnotatedMethod
				.getMethodParam();
		String eventName = axonAnnotatedClass.getClassName() + "CreatedEvent";

		ClassName event = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".event", eventName);

		String eventParam = "";

		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			eventParam += "event." + entry.getKey() + ",";

		}
		eventParam = eventParam.substring(0, eventParam.length() - 1);

		MethodSpec.Builder eventHandler = MethodSpec
				.methodBuilder("on")
				.addParameter(event, "event")
				.addModifiers(Modifier.FINAL, Modifier.PUBLIC)
				.addAnnotation(EventHandler.class)
				.addStatement("this.id = event.id")
				.addStatement(
						className + "= new " + className + "(" + eventParam
								+ ")");
		return eventHandler.build();
	}

	protected MethodSpec commandHandlerForConstructor(
			AxonAnnotatedMethod axonAnnotatedMethod) {
		ClassName createCommand = ClassName.get(
				axonAnnotatedClass.getPackageName() + ".command", "Create"
						+ className + "Command");

		String eventName = className + "CreatedEvent";
		ClassName event = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".event", eventName);

		Map<String, TypeMirror> methodParam = axonAnnotatedMethod
				.getMethodParam();

		String constructorParam = "command.id, ";

		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			constructorParam += "command." + entry.getKey() + ",";

		}
		constructorParam = constructorParam.substring(0,
				constructorParam.length() - 1);

		Builder methodBuilder = MethodSpec
				.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addStatement("apply (new $T(" + constructorParam + "))", event)
				.addParameter(createCommand, "command");

		methodBuilder.addAnnotation(CommandHandler.class);

		MethodSpec method = methodBuilder.build();
		return method;

	}

	protected MethodSpec accessMethod(AxonAnnotatedMethod annotatedMethod) {
		String methodName = annotatedMethod.getMethodName();

		MethodSpec eventHandler = MethodSpec.methodBuilder(methodName)
				.addModifiers(Modifier.PUBLIC)
				.returns(TypeName.get(annotatedMethod.getMethodReturn()))
				.addStatement("return " + className + "." + methodName + "()")
				.build();
		return eventHandler;
	}

	protected MethodSpec emptyConstructor() {

		MethodSpec emptyConstructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC).addStatement("super()").build();
		return emptyConstructor;
	}

	protected MethodSpec copyConstructor() {

		ClassName aggregateRoot = ClassName.get(
				axonAnnotatedClass.getPackageName() + ".aggregate",
				axonAnnotatedClass.getClassName() + "RootAggregate");

		MethodSpec.Builder copyConstructor = MethodSpec
				.constructorBuilder()
				.addParameter(aggregateRoot, "aggregateRoot")
				.addModifiers(Modifier.PUBLIC)
				.addStatement("this.id = aggregateRoot.id")
				.addStatement("this.$L = aggregateRoot.$L.clone()",
						this.axonAnnotatedClass.getClassName(),this.axonAnnotatedClass.getClassName());
		return copyConstructor.build();
	}

}
