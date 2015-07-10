package com.processor;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.mirror.declaration.Modifier;

public class test {
	/**
	 * @throws IOException
	 * 
	 */
	public static void main(String[] args) throws IOException {
		TypeSpec hello = TypeSpec
				.classBuilder("Hello")
				.addMethod(
						MethodSpec.methodBuilder("sortByLength").build())
				.build();
		String eventName = "com.processor.AxonAnnotation";
		ClassName event = ClassName.bestGuess(eventName);
		TypeSpec comparator = TypeSpec
				.classBuilder("myclass")
				.addType(hello)                                   
				.addSuperinterface(event)
				.addMethod(
						MethodSpec
								.methodBuilder("compare")
								.addParameter(String.class, "a")
								.addParameter(String.class, "b")
								.addStatement(
										"return $N.length() - $N.length()",
										"a", "b").build()).build();
		

		TypeSpec helloWorld = TypeSpec
				.classBuilder("HelloWorld")
				.addMethod(
						MethodSpec
								.methodBuilder("sortByLength")
								.addParameter(
										ParameterizedTypeName.get(List.class,
												String.class), "strings")
								.addStatement("$T.sort($N, $L)",
										Collections.class, "strings",
										comparator).build()).build();
		JavaFile javaFile = JavaFile.builder("com.example.helloworld",
				helloWorld).build();

		javaFile.writeTo(System.out);

	}
}
