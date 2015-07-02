package com.processor.codegenerator;

import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import org.axonframework.commandhandling.annotation.CommandHandler;
import org.axonframework.commandhandling.annotation.TargetAggregateIdentifier;
import org.axonframework.eventhandling.annotation.EventHandler;
//import org.axonframework.eventsourcing.annotation.AggregateIdentifier;

import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeName;

public class EventCommandBuilderHelper {

	AxonAnnotatedMethod axonAnnotatedMethod;

	public EventCommandBuilderHelper(AxonAnnotatedMethod axonAnnotatedMethod) {
		super();
		this.axonAnnotatedMethod = axonAnnotatedMethod;
	}

	protected FieldSpec field(TypeMirror type, String name) {

		FieldSpec field = FieldSpec.builder(TypeName.get(type), name)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL).build();
		return field;

	}

	protected FieldSpec fieldID(String type) {

		switch (type) {
		case "command": {
			FieldSpec fieldID = FieldSpec.builder(String.class, "id")
					.addAnnotation(TargetAggregateIdentifier.class)
					.addModifiers(Modifier.PUBLIC, Modifier.FINAL).build();
			return fieldID;
		}
		case "event": {
			FieldSpec fieldID = FieldSpec.builder(String.class, "id")
					.addModifiers(Modifier.PUBLIC, Modifier.FINAL).build();
			return fieldID;
		}
		default:
			return null;
		}

	}

	protected MethodSpec constructor() {

		Map<String, TypeMirror> methodParam = axonAnnotatedMethod
				.getMethodParam();

		Builder methodBuilder = MethodSpec.constructorBuilder().addModifiers(
				Modifier.PUBLIC);

		methodBuilder.addParameter(String.class, "id", Modifier.FINAL);
		methodBuilder.addStatement("super()");
		methodBuilder.addStatement("this.$N = $N", "id", "id");
		for (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) {
			methodBuilder.addParameter(TypeName.get(entry.getValue()),
					entry.getKey(), Modifier.FINAL);
			methodBuilder.addStatement("this.$N = $N", entry.getKey(),
					entry.getKey());

		}
		MethodSpec method = methodBuilder.build();
		return method;

	}
	/*
	 * protected MethodSpec commandHandler(String packageName, String
	 * commandName) { Map<String, TypeMirror> methodParam = axonAnnotatedMethod
	 * .getMethodParam(); ClassName command = ClassName.get(packageName,
	 * commandName); String stmt = ""; for (Map.Entry<String, TypeMirror> entry
	 * : methodParam.entrySet()) { if (stmt.equals("")) { stmt += "event." +
	 * entry.getKey(); } else { stmt += ", event." + entry.getKey(); }
	 * 
	 * }
	 * 
	 * MethodSpec commandHandler = MethodSpec.methodBuilder("handle")
	 * .addAnnotation(CommandHandler.class)
	 * .addModifiers(Modifier.PUBLIC).addParameter(command, "command")
	 * .addModifiers(Modifier.FINAL) .addStatement("apply (new$T)",
	 * command).build(); return commandHandler; }
	 * 
	 * protected MethodSpec eventHandler(String className) { String commandName
	 * = this.axonAnnotatedMethod.getCapitalMethodName();
	 * commandName=commandName.replace("CompletedEvent",""); Map<String,
	 * TypeMirror> methodParam = axonAnnotatedMethod .getMethodParam();
	 * 
	 * ClassName event = ClassName.get(null, commandName); String stmt = ""; for
	 * (Map.Entry<String, TypeMirror> entry : methodParam.entrySet()) { if
	 * (stmt.equals("")) { stmt += "event." + entry.getKey(); } else { stmt +=
	 * ", event." + entry.getKey(); }
	 * 
	 * }
	 * 
	 * MethodSpec eventHandler = MethodSpec.methodBuilder("on")
	 * .addParameter(event, "event").addModifiers(Modifier.FINAL)
	 * .addAnnotation(EventHandler.class) .addStatement("$S.$T($S)", className,
	 * event, stmt).build(); return eventHandler; }
	 * 
	 * private MethodSpec getter(String name) { String methodName = "get" +
	 * name.substring(0, 1).toUpperCase() + name.substring(1); return
	 * MethodSpec.methodBuilder(methodName).returns(String.class)
	 * .addStatement("return $S", name).build(); }
	 * 
	 * private MethodSpec setter(String name) { // make the first letter in the
	 * field name captilized String methodName = "set" + name.substring(0,
	 * 1).toUpperCase() + name.substring(1); return
	 * MethodSpec.methodBuilder(methodName) .addParameter(String.class, name)
	 * .addStatement("this.$N = $N", name, name).build(); }
	 */

}
