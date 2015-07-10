package com.processor.codegenerator.handler;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.annotation.AbstractAnnotatedAggregateRoot;

import com.processor.codegenerator.aggregate.AggregateBuilderHelper;
import com.processor.codegenerator.facade.FacadeBuilderHelper;
import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

public class InterfaceCommandHandlerBuilder {

	private AxonAnnotatedClass axonAnnotatedClass;

	public InterfaceCommandHandlerBuilder(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
	}

	public MethodSpec commandValidator(AxonAnnotatedMethod annotatedMethod) {
		ClassName command = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".command", annotatedMethod.getCapitalMethodName()
				+ "Command");
		return MethodSpec
				.methodBuilder(
						annotatedMethod.getCapitalMethodName() + "Command"
								+ "Validator").addParameter(command, "command")
				.returns(boolean.class)

				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}

	public MethodSpec defaultCommandValidator(
			AxonAnnotatedMethod annotatedMethod) {
		ClassName command = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".command", annotatedMethod.getCapitalMethodName()
				+ "Command");
		return MethodSpec
				.methodBuilder(
						annotatedMethod.getCapitalMethodName() + "Command"
								+ "Validator").addParameter(command, "command")
				.returns(boolean.class).addStatement("return true")
				.addModifiers(Modifier.PUBLIC).build();

	}

	public MethodSpec commandValidatorForConstructor(
			AxonAnnotatedMethod annotatedMethod) {
		ClassName command = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".command", "Create" + this.axonAnnotatedClass.getClassName()
				+ "Command");
		return MethodSpec
				.methodBuilder(
						"Create" + axonAnnotatedClass.getClassName()
								+ "Command" + "Validator")
				.returns(boolean.class).addParameter(command, "command")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).build();

	}

	public MethodSpec defaultCommandValidatorForConstructor(
			AxonAnnotatedMethod annotatedMethod) {
		ClassName command = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".command", "Create" + this.axonAnnotatedClass.getClassName()
				+ "Command");
		return MethodSpec
				.methodBuilder(
						"Create" + axonAnnotatedClass.getClassName()
								+ "Command" + "Validator")
				.returns(boolean.class).addParameter(command, "command")
				.addStatement("return true").addModifiers(Modifier.PUBLIC)
				.build();

	}

	public TypeSpec getInterface() {

		TypeSpec.Builder validatorInterfaceBuilder = TypeSpec.interfaceBuilder(
				axonAnnotatedClass.getClassName() + "CommandValidator")
				.addModifiers(Modifier.PUBLIC);
		// modifier
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassModifierMethods()) {
			validatorInterfaceBuilder.addMethod(this
					.commandValidator(annotatedMethod));
			// validatorInterfaceBuilder.addMethod(this
			// .defaultCommandValidator(annotatedMethod));

		}

		// constructors
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassConstructorMethods()) {
			validatorInterfaceBuilder.addMethod(this
					.commandValidatorForConstructor(annotatedMethod));
			// validatorInterfaceBuilder.addMethod(this
			// .defaultCommandValidatorForConstructor(annotatedMethod));
		}

		return validatorInterfaceBuilder.build();
	}

	public TypeSpec getDefaultCommmandHandler() throws Exception {

		//ClassLoader classLoader = 	User.class
		//		.getClassLoader();
		String interfaceCommandValidatorName = axonAnnotatedClass
				.getPackageName()+"."
				+ axonAnnotatedClass.getClassName()
				+ "CommandValidator";
		//Class interfaceCommandValidator = classLoader
		//		.loadClass(interfaceCommandValidatorName);
		// System.out.println("aClass.getName() = " + aClass.getName());

		String interfaceName = axonAnnotatedClass.getPackageName()+"."+axonAnnotatedClass.getClassName()+"CommandValidator";
		TypeSpec.Builder validatorInterfaceBuilder = TypeSpec
				.classBuilder(
						axonAnnotatedClass.getClassName() + "CommandValidator"
								+ "Default").addModifiers(Modifier.PUBLIC)
				.addMethod(emptyConstructor())
			 	.addSuperinterface(ClassName.bestGuess(interfaceName));
		// modifier
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassModifierMethods()) {
			validatorInterfaceBuilder.addMethod(this
					.defaultCommandValidator(annotatedMethod));

		}

		// constructors
		for (AxonAnnotatedMethod annotatedMethod : axonAnnotatedClass
				.getClassConstructorMethods()) {
			validatorInterfaceBuilder.addMethod(this
					.defaultCommandValidatorForConstructor(annotatedMethod));
		}

		return validatorInterfaceBuilder.build();
	}

	protected MethodSpec emptyConstructor() {

		MethodSpec emptyConstructor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC).addStatement("super()").build();
		return emptyConstructor;
	}
}
