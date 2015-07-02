package com.processor.codegenerator;

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

	protected MethodSpec constructor() {
		ClassName createCommand = ClassName.get(
				axonAnnotatedClass.getPackageName() + ".command", "Create"
						+ className + "Command");

		Builder methodBuilder = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addParameter(createCommand, "command");

		methodBuilder.addStatement("this.id = id");
		methodBuilder.addStatement("this.$N = $N", className, className);

		MethodSpec method = methodBuilder.build();
		return method;

	}
/*
	private MethodSpec getter(String name) {
		String methodName = "get" + name.substring(0, 1).toUpperCase()
				+ name.substring(1);
		return MethodSpec.methodBuilder(methodName).returns(String.class)
				.addStatement("return $S", name).build();
	}*/

	private MethodSpec setter(String name) {
		// make the first letter in the field name captilized
		String methodName = "set" + name.substring(0, 1).toUpperCase()
				+ name.substring(1);
		return MethodSpec.methodBuilder(methodName)
				.addParameter(String.class, name)
				.addStatement("this.$N = $N", name, name).build();
	}

	protected MethodSpec commandHandler(AxonAnnotatedMethod axonAnnotatedMethod) {
		Map<String, TypeMirror> methodParam = axonAnnotatedMethod
				.getMethodParam();

		String commandName = axonAnnotatedMethod.getCapitalMethodName()
				+ "Command";
		String eventName = axonAnnotatedMethod.getCapitalMethodName()
				+ "CompletedEvent";
		ClassName event = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".event", eventName);
		ClassName command = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".command", commandName);

		String eventParam = "command.id, ";

		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			eventParam += "command." + entry.getKey() + ",";

		}

		eventParam = eventParam.substring(0, eventParam.length() - 1);
		MethodSpec.Builder commandHandler = MethodSpec.methodBuilder("handle")
				.addAnnotation(CommandHandler.class)
				.addParameter(command, "command").addModifiers(Modifier.FINAL)
				.addStatement("apply (new $T(" + eventParam + "))", event);

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
				.addModifiers(Modifier.FINAL)
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
				.addModifiers(Modifier.FINAL)
				.addAnnotation(EventHandler.class)
				.addStatement(
						className + "= new " + className + "(" + eventParam
								+ ")");
		return eventHandler.build();
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

	public MethodSpec testBeforeSetup() {

		MethodSpec.Builder testCommandEvent = MethodSpec
				.methodBuilder("setUp")
				.addException(Exception.class)
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(Before.class)
				.addStatement(
						" fixture = $T.newGivenWhenThenFixture($T.class)",
						Fixtures.class,axonAnnotatedClass.getClassType());
		return testCommandEvent.build();

	}

	public String randomParam(TypeMirror type) {
		if (type.toString().equals("java.lang.String")) {

			SecureRandom random = new SecureRandom();
			return new BigInteger(130, random).toString(32);
		} else if (type.toString().equals("java.lang.Integer")){
			Random rand = new Random();

		    // nextInt is normally exclusive of the top value,
		    // so add 1 to make it inclusive
		    Integer randomNum = rand.nextInt((100- 0) + 1)  ;

		    return randomNum.toString();

		}
		return null;
	}

	public MethodSpec testCommandEvent(AxonAnnotatedMethod annotatedMethod) {
		Map<String, TypeMirror> methodParam = annotatedMethod.getMethodParam();
		String eventName = annotatedMethod.getCapitalMethodName()
				+ "CompletedEvent";

		ClassName event = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".event", eventName);

		String commandName = annotatedMethod.getCapitalMethodName() + "Command";

		ClassName command = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".command", commandName);
		
		Random rand = new Random();
	    Integer randomNum = rand.nextInt((100- 0) + 1);
		String testParam = "\""+randomNum.toString()+"\""+",";

		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			testParam += "\""+randomParam(entry.getValue())+"\"" + ",";

		}
		testParam = testParam.substring(0, testParam.length() - 1);

		MethodSpec.Builder testCommandEvent = MethodSpec
				.methodBuilder("test" + annotatedMethod.getCapitalMethodName())
				.addException(Exception.class)
				.addParameter(event, "event")
				.addModifiers(Modifier.FINAL)
				.addAnnotation(Test.class)
				.addStatement(
						"fixture.given().when(new $T(" + testParam
								+ ")).expectEvents(new $T($L))", command,event, testParam);
		return testCommandEvent.build();

	}

}
