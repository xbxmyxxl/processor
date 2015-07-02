package com.processor.codegenerator.eventcommand;

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

public class ConstructorBuilderHelper {

	AxonAnnotatedMethod axonAnnotatedMethod;

	public ConstructorBuilderHelper(AxonAnnotatedMethod axonAnnotatedMethod) {
		super();
		this.axonAnnotatedMethod = axonAnnotatedMethod;
	}

	public FieldSpec field(TypeMirror type, String name) {

		FieldSpec field = FieldSpec.builder(TypeName.get(type), name)
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL).build();
		return field;

	}

	public FieldSpec fieldID(String type) {

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

	public MethodSpec constructor() {

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

}
