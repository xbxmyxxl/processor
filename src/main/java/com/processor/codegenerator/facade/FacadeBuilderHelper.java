package com.processor.codegenerator.facade;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;
import org.axonframework.commandhandling.gateway.CommandGateway;
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

public class FacadeBuilderHelper {

	AxonAnnotatedClass axonAnnotatedClass;
	private String className;

	public FacadeBuilderHelper(AxonAnnotatedClass axonAnnotatedClass) {
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

	public MethodSpec sendCommandForConstructor(
			AxonAnnotatedMethod annotatedMethod) {
		Map<String, TypeMirror> methodParam = annotatedMethod.getMethodParam();

		Builder methodBuilder = MethodSpec.methodBuilder(
				"send" + "Create" + className + "Command")
				.addModifiers(Modifier.PUBLIC);
		methodBuilder.addStatement("String id = $T.randomUUID().toString()",UUID.class);

		String commandParam = "id,";
		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			methodBuilder.addParameter(TypeName.get(entry.getValue()),
					entry.getKey(), Modifier.FINAL);
			commandParam += entry.getKey() + ",";

		}
		commandParam = commandParam.substring(0, commandParam.length() - 1);

		ClassName command = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".command", "Create" + className + "Command");
		methodBuilder.addStatement("commandGateway.send (new $T("
				+commandParam + "))", command);

		MethodSpec method = methodBuilder.build();
		return method;
	}

	public MethodSpec sendCommand(AxonAnnotatedMethod annotatedMethod) {
		Map<String, TypeMirror> methodParam = annotatedMethod.getMethodParam();

		Builder methodBuilder = MethodSpec.methodBuilder(
				"send" + annotatedMethod.getCapitalMethodName() + "Command")
				.addModifiers(Modifier.PUBLIC);

		String commandParam = "id,";
		methodBuilder.addParameter(String.class,"id", Modifier.FINAL);
		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			methodBuilder.addParameter(TypeName.get(entry.getValue()),
					entry.getKey(), Modifier.FINAL);
			commandParam += entry.getKey() + ",";

		}
		commandParam = commandParam.substring(0, commandParam.length() - 1);

		ClassName command = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".command", annotatedMethod.getCapitalMethodName()
				+ "Command");
		methodBuilder.addStatement("commandGateway.send (new $T("
				+ commandParam + "))", command);

		MethodSpec method = methodBuilder.build();
		return method;
	}

	public MethodSpec setterForCommandGateway() {

		Builder constructorBuilder = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC);

		constructorBuilder.addParameter(CommandGateway.class, "commandGateway",
				Modifier.FINAL);
		constructorBuilder.addStatement("this.commandGateway = commandGateway");

		return constructorBuilder.build();

	}

}
