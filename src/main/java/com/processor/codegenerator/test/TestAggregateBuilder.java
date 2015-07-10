package com.processor.codegenerator.test;

import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;
import org.axonframework.test.FixtureConfiguration;

import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class TestAggregateBuilder {

	private AxonAnnotatedClass axonAnnotatedClass;

	public TestAggregateBuilder(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
	}

	public TypeSpec getAggregateClass() {
		// getting the map

		// set up the helper
		TestAggregateBuilderHelper testAggregateBuilderHelper = new TestAggregateBuilderHelper(
				axonAnnotatedClass);
		

		FieldSpec fixture = FieldSpec
				.builder(getParameterizedTestClass(), "fixture")
				.addModifiers(Modifier.PRIVATE).build();
		String testAggregateName = axonAnnotatedClass.getClassName()+"RootAggregateTest";
		// add the fields
		TypeSpec.Builder testBuilder = TypeSpec.classBuilder(testAggregateName )
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(fixture);

		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassModifierMethods()) {
			testBuilder.addMethod(testAggregateBuilderHelper
					.testCommandEvent(annotatedMethod));
		}
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassConstructorMethods()) {
			testBuilder.addMethod(testAggregateBuilderHelper
					.testConstructor(annotatedMethod));
		}

		// adding constructor
		testBuilder.addMethod(testAggregateBuilderHelper.testBeforeSetup());
		testBuilder.addJavadoc("Auto generated! Do not Modify!").addJavadoc(
				"\n");

		TypeSpec command = testBuilder.build();

		return command;
	}

	public ParameterizedTypeName getParameterizedTestClass() {

		ClassName aggregateName = ClassName.get(
				axonAnnotatedClass.getPackageName() + ".aggregate",
				axonAnnotatedClass.getClassName() + "RootAggregate");

		ParameterizedTypeName parameterizedFixture = ParameterizedTypeName
				.get(ClassName.get("org.axonframework.test",
						"FixtureConfiguration"), aggregateName);
		return parameterizedFixture;
	}
}
