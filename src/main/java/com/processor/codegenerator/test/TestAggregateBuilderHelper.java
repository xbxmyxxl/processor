package com.processor.codegenerator.test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

public class TestAggregateBuilderHelper {

	AxonAnnotatedClass axonAnnotatedClass;
	private String className;

	public TestAggregateBuilderHelper(AxonAnnotatedClass axonAnnotatedClass) {
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

	public MethodSpec testBeforeSetup() {

		ClassName constructorCommand = ClassName.get(
				axonAnnotatedClass.getPackageName() + ".aggregate", className
						+ "RootAggregate");
		MethodSpec.Builder testCommandEvent = MethodSpec
				.methodBuilder("setUp")
				.addException(Exception.class)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Before.class)
				.addStatement(
						" fixture = $T.newGivenWhenThenFixture($T.class)",
						Fixtures.class, constructorCommand);
		return testCommandEvent.build();

	}

	public String randomParam(TypeMirror type) {
		if (type.toString().equals("java.lang.String")) {
			int number = 5;

			SecureRandom random = new SecureRandom();
			return "\"" + new BigInteger(130, random).toString(32) + "\"";
		} else if (type.toString().equals("java.lang.Integer")
				|| type.toString().equals("int")) {
			Random rand = new Random();

			// nextInt is normally exclusive of the top value,
			// so add 1 to make it inclusive
			Integer randomNum = rand.nextInt((100 - 0) + 1);

			return randomNum.toString();

		} else if (type.toString().equals("boolean")) {
			Random rand = new Random();

			// nextInt is normally exclusive of the top value,
			// so add 1 to make it inclusive
			Integer randomNum = rand.nextInt(2);
			if (randomNum == 1)
				return "true";
			else
				return "false";

		} else if (type.toString().equals("char")) {
			final String alphabet = "abcdefghijklmmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ,./;'[]1234567890-=`<>?:{}+~!@#$%^&*()";
			Random rand = new Random();
			return new StringBuilder().append("\'")
					.append(alphabet.charAt(rand.nextInt(alphabet.length())))
					.append("\'").toString();

		}
		return null;
	}

	public MethodSpec testCommandEvent(AxonAnnotatedMethod annotatedMethod) {
		Map<String, TypeMirror> methodParam = annotatedMethod.getMethodParam();

		ClassName constructor = ClassName.get(
				axonAnnotatedClass.getPackageName() + ".event", className
						+ "CreatedEvent");

		Map<String, TypeMirror> constructorParam = null;
		if (axonAnnotatedClass.getClassConstructorMethods().size() == 1)
			constructorParam = axonAnnotatedClass.getClassConstructorMethods()
					.get(0).getMethodParam();

		String eventName = annotatedMethod.getCapitalMethodName()
				+ "CompletedEvent";

		ClassName event = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".event", eventName);

		String commandName = annotatedMethod.getCapitalMethodName() + "Command";

		ClassName command = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".command", commandName);

		Random rand = new Random();
		Integer randomNum = rand.nextInt((100 - 0) + 1);
		String testParam = "\"" + randomNum.toString() + "\"" + ",";
		String constructorTestParam = testParam;

		if (!constructorParam.isEmpty()) {
			for (Map.Entry<String, TypeMirror> entry : constructorParam
					.entrySet()) {
				constructorTestParam += randomParam(entry.getValue()) + ",";

			}
		}
		constructorTestParam = constructorTestParam.substring(0,
				constructorTestParam.length() - 1);
		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			testParam += randomParam(entry.getValue()) + ",";

		}
		testParam = testParam.substring(0, testParam.length() - 1);

		MethodSpec.Builder testCommandEvent = MethodSpec
				.methodBuilder("test" + annotatedMethod.getCapitalMethodName())
				.addException(Exception.class)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Test.class)
				.addStatement(
						"fixture.given(new $T($L)).when(new $T(" + testParam
								+ ")).expectEvents(new $T($L))", constructor,
						constructorTestParam, command, event, testParam);
		return testCommandEvent.build();

	}

	public MethodSpec testConstructor(AxonAnnotatedMethod annotatedMethod) {
		ClassName constructorCommand = ClassName.get(
				axonAnnotatedClass.getPackageName() + ".command", "Create"
						+ className + "Command");
		ClassName constructorEvent = ClassName.get(
				axonAnnotatedClass.getPackageName() + ".event", className
						+ "CreatedEvent");

		Map<String, TypeMirror> constructorParam = null;
		if (axonAnnotatedClass.getClassConstructorMethods().size() == 1)
			constructorParam = axonAnnotatedClass.getClassConstructorMethods()
					.get(0).getMethodParam();

		Random rand = new Random();
		Integer randomNum = rand.nextInt((100 - 0) + 1);
		String constructorTestParam = "\"" + randomNum.toString() + "\"" + ",";

		if (!constructorParam.isEmpty()) {
			for (Map.Entry<String, TypeMirror> entry : constructorParam
					.entrySet()) {
				constructorTestParam += randomParam(entry.getValue()) + ",";

			}
		}

		constructorTestParam = constructorTestParam.substring(0,
				constructorTestParam.length() - 1);

		MethodSpec.Builder testCommandEvent = MethodSpec
				.methodBuilder("test" + "Create" + className)
				.addException(Exception.class)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Test.class)
				.addStatement(
						"fixture.given().when(new $T(" + constructorTestParam
								+ ")).expectEvents(new $T($L))",
						constructorCommand, constructorEvent,
						constructorTestParam);
		return testCommandEvent.build();

	}

}
