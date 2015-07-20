package com.processor.codegenerator.handler;

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
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.annotation.EventHandler;
import org.axonframework.eventsourcing.annotation.AggregateIdentifier;
import org.axonframework.repository.Repository;
import org.axonframework.test.Fixtures;
import org.axonframework.unitofwork.DefaultUnitOfWork;
import org.axonframework.unitofwork.UnitOfWork;
import org.junit.Before;
import org.junit.Test;

import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeName;

public class ExternalCommandHandlerBuilderHelper {

	AxonAnnotatedClass axonAnnotatedClass;
	private String className;

	public ExternalCommandHandlerBuilderHelper(
			AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
		this.className = axonAnnotatedClass.getClassName();
	}

	/*
	 * public ParameterizedTypeName getParameterizedSuperClass() { ClassName
	 * repository = ClassName.get("org.axonframework.repository", "Repository");
	 * ParameterizedTypeName parameterizedRepository = ParameterizedTypeName
	 * .get(repository, TypeName.get(axonAnnotatedClass.getClassType())); return
	 * parameterizedRepository; }
	 */

	protected FieldSpec repositoryField() {

		ClassName repository = ClassName.get("org.axonframework.repository",
				"Repository");
		ClassName aggregateRoot = ClassName.bestGuess(axonAnnotatedClass
				.getPackageName()
				+ ".aggregate."
				+ axonAnnotatedClass.getClassName() + "RootAggregate");
		ParameterizedTypeName parameterizedRepository = ParameterizedTypeName
				.get(repository, aggregateRoot);

		FieldSpec field = FieldSpec
				.builder(parameterizedRepository, "repository")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build();
		return field;

	}

	protected FieldSpec validatorField() {

		String validatorClassName = axonAnnotatedClass.getClassName()
				+ "CommandValidator";
		ClassName validator = ClassName.get(
				axonAnnotatedClass.getPackageName(), validatorClassName);
		;
		String validatorName = axonAnnotatedClass.getLowerClassName()
				+ "CommandValidator";
		FieldSpec field = FieldSpec.builder(validator, validatorName)
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build();
		return field;

	}

	public MethodSpec constructor() {
		ClassName repository = ClassName.get("org.axonframework.repository",
				"Repository");
		ClassName aggregateRoot = ClassName.bestGuess(axonAnnotatedClass
				.getPackageName()
				+ ".aggregate."
				+ axonAnnotatedClass.getClassName() + "RootAggregate");
		ParameterizedTypeName parameterizedRepository = ParameterizedTypeName
				.get(repository, aggregateRoot);

		Builder constructorBuilder = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC);

		constructorBuilder.addParameter(parameterizedRepository, "repository",
				Modifier.FINAL);
		constructorBuilder.addStatement("this.repository = repository");

		String validatorName = axonAnnotatedClass.getLowerClassName()
				+ "CommandValidator";
		String validatorClassName = axonAnnotatedClass.getClassName()
				+ "CommandValidator";

		ClassName validator = ClassName.get(
				axonAnnotatedClass.getPackageName(), validatorClassName);

		constructorBuilder.addParameter(validator, validatorName,
				Modifier.FINAL);
		constructorBuilder.addStatement("this.$L = $L", validatorName,
				validatorName);

		return constructorBuilder.build();

	}

	public MethodSpec defaultConstructor() {

		ClassName repository = ClassName.get("org.axonframework.repository",
				"Repository");
		ClassName aggregateRoot = ClassName.bestGuess(axonAnnotatedClass
				.getPackageName()
				+ ".aggregate."
				+ axonAnnotatedClass.getClassName() + "RootAggregate");
		ParameterizedTypeName parameterizedRepository = ParameterizedTypeName
				.get(repository, aggregateRoot);
		Builder constructorBuilder = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC);

		constructorBuilder.addParameter(parameterizedRepository, "repository",
				Modifier.FINAL);
		constructorBuilder.addStatement("this.repository = repository");

		String validatorName = axonAnnotatedClass.getLowerClassName()
				+ "CommandValidator";
		String validatorClassName = axonAnnotatedClass.getClassName()
				+ "CommandValidator" + "Default";

		ClassName validator = ClassName.get(
				axonAnnotatedClass.getPackageName(), validatorClassName);

		constructorBuilder.addStatement("this.$L = new $T()", validatorName,
				validator);

		return constructorBuilder.build();

	}

	protected FieldSpec fieldID() {
		FieldSpec fieldID = FieldSpec.builder(String.class, "id")
				.addAnnotation(AggregateIdentifier.class)
				.addModifiers(Modifier.PRIVATE).build();
		return fieldID;

	}

	protected MethodSpec commandHandler(AxonAnnotatedMethod axonAnnotatedMethod) {

		ClassName command;
		String commandName = axonAnnotatedMethod.getCapitalMethodName()
				+ "Command";
		command = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".command", commandName);

		ClassName aggregate;
		String aggregateName = axonAnnotatedClass.getClassName()
				+ "RootAggregate";
		aggregate = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".aggregate", aggregateName);
		String variableAggregateName = axonAnnotatedClass.getLowerClassName()
				+ "RootAggregate";

		String commandParam = "";
		Map<String, TypeMirror> methodValidatorParam = axonAnnotatedMethod
				.getMethodParam();
		for (Map.Entry<String, TypeMirror> entry : methodValidatorParam
				.entrySet()) {
			commandParam += "command." + entry.getKey() + ",";

		}
		commandParam = commandParam.substring(0, commandParam.length() - 1);

		MethodSpec.Builder commandHandler = MethodSpec.methodBuilder("handle")
				.addAnnotation(CommandHandler.class)
				.addParameter(command, "command").addModifiers(Modifier.FINAL);

		commandHandler.addStatement("$L $L = ($L)repository.load(command.id)",
				aggregate.simpleName(), variableAggregateName,
				aggregate.simpleName());

		String validatorName = axonAnnotatedMethod.getMethodName()
				+ "CommandValidator";
		ClassName validator = ClassName.get(
				axonAnnotatedClass.getPackageName(), validatorName);

		String validatorClassName = axonAnnotatedClass.getLowerClassName()
				+ "CommandValidator";

		commandHandler
				.beginControlFlow("if($L.$T(command))", validatorClassName,
						validator)
				.addStatement("$L.handle(command)", variableAggregateName)
				.endControlFlow();

		return commandHandler.build();
	}

	protected MethodSpec commandHandlerForConstructor(
			AxonAnnotatedMethod axonAnnotatedMethod) {

		ClassName command;
		String commandName = "Create" + axonAnnotatedClass.getClassName()
				+ "Command";
		command = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".command", commandName);

		ClassName aggregate;
		String aggregateName = axonAnnotatedClass.getClassName()
				+ "RootAggregate";
		aggregate = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".aggregate", aggregateName);
		String variableAggregateName = axonAnnotatedClass.getLowerClassName()
				+ "RootAggregate";

		String commandParam = "";
		Map<String, TypeMirror> methodValidatorParam = axonAnnotatedMethod
				.getMethodParam();
		for (Map.Entry<String, TypeMirror> entry : methodValidatorParam
				.entrySet()) {
			commandParam += "command." + entry.getKey() + ",";

		}
		commandParam = commandParam.substring(0, commandParam.length() - 1);

		MethodSpec.Builder commandHandler = MethodSpec.methodBuilder("handle")
				.addAnnotation(CommandHandler.class)
				.addParameter(command, "command").addModifiers(Modifier.FINAL);

		String validatorName = "create" + axonAnnotatedClass.getClassName()
				+ "CommandValidator";
		ClassName validator = ClassName.get(
				axonAnnotatedClass.getPackageName(), validatorName);

		String validatorClassName = axonAnnotatedClass.getLowerClassName()
				+ "CommandValidator";

		commandHandler
				.beginControlFlow("if($L.$T(command))", validatorClassName,
						validator)
				.addStatement("$L $L = new $L(command)", aggregateName,
						variableAggregateName, aggregateName)
				.addStatement("repository.add($L)", variableAggregateName)
				.endControlFlow();

		return commandHandler.build();

	}

	protected MethodSpec loadRootAggregateById() {


		ClassName aggregate;
		String aggregateName = axonAnnotatedClass.getClassName()
				+ "RootAggregate";
		aggregate = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".aggregate", aggregateName);


		MethodSpec.Builder loadAggregateMethod = MethodSpec
				.methodBuilder("loadUserRootAggregateById")
				.addParameter(String.class, "id").addModifiers(Modifier.FINAL);
		loadAggregateMethod.addStatement("$T unitOfWork = $T.startAndGet()",
				UnitOfWork.class, DefaultUnitOfWork.class);
		loadAggregateMethod.returns(aggregate);
		loadAggregateMethod.beginControlFlow("try")
				.addStatement("return repository.load(id)").endControlFlow()
				.beginControlFlow("finally")
				.addStatement("unitOfWork.commit()").endControlFlow();

		return loadAggregateMethod.build();

	}

}
