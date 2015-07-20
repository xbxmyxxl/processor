package com.processor.codegenerator.gui.model;

import java.util.LinkedList;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

import org.axonframework.unitofwork.DefaultUnitOfWork;
import org.axonframework.unitofwork.UnitOfWork;

import com.processor.parse.AxonAnnotatedClass;
import com.processor.parse.AxonAnnotatedMethod;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.MethodSpec.Builder;

public class ClassModelBuilder {

	AxonAnnotatedClass axonAnnotatedClass;

	public ClassModelBuilder(AxonAnnotatedClass axonAnnotatedClass) {
		super();
		this.axonAnnotatedClass = axonAnnotatedClass;
	}

	protected FieldSpec editableRepository() {

		ClassName repository = ClassName.get("org.axonframework.repository",
				"Repository");
		ClassName aggregateRoot = ClassName.bestGuess(axonAnnotatedClass
				.getPackageName()
				+ ".aggregate."
				+ axonAnnotatedClass.getClassName() + "RootAggregate");
		ParameterizedTypeName parameterizedRepository = ParameterizedTypeName
				.get(repository, aggregateRoot);

		FieldSpec field = FieldSpec
				.builder(parameterizedRepository, "editableRepository")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build();
		return field;

	}

	protected FieldSpec copiedRepository() {

		ClassName repository = ClassName.get("org.axonframework.repository",
				"Repository");
		ClassName aggregateRoot = ClassName.bestGuess(axonAnnotatedClass
				.getPackageName()
				+ ".aggregate."
				+ axonAnnotatedClass.getClassName() + "RootAggregate");
		ParameterizedTypeName parameterizedRepository = ParameterizedTypeName
				.get(repository, aggregateRoot);

		FieldSpec field = FieldSpec
				.builder(parameterizedRepository, "copiedRepository")
				.addModifiers(Modifier.PRIVATE, Modifier.FINAL).build();
		return field;

	}

	public MethodSpec addToEditableRepo() {
		ClassName aggregateRoot = ClassName.bestGuess(axonAnnotatedClass
				.getPackageName()
				+ ".aggregate."
				+ axonAnnotatedClass.getClassName() + "RootAggregate");

		return MethodSpec
				.methodBuilder("addToEditableRepo")
				.addParameter(aggregateRoot, "aggregateRoot")
				.addStatement("editableRepository.add(aggregateRoot)")
				.addModifiers(Modifier.PUBLIC).build();

	}
	public MethodSpec addToCopiedRepo() {
		ClassName aggregateRoot = ClassName.bestGuess(axonAnnotatedClass
				.getPackageName()
				+ ".aggregate."
				+ axonAnnotatedClass.getClassName() + "RootAggregate");

		return MethodSpec
				.methodBuilder("addToCopiedRepo")
				.addParameter(aggregateRoot, "aggregateRoot")
				.addStatement("copiedRepository.add(aggregateRoot)")
				.addModifiers(Modifier.PUBLIC).build();

	}
	
	public MethodSpec sychronizeChangeById() {
		ClassName aggregateRoot = ClassName.bestGuess(axonAnnotatedClass
				.getPackageName()
				+ ".aggregate."
				+ axonAnnotatedClass.getClassName() + "RootAggregate");

		return MethodSpec
				.methodBuilder("sychronizedChangeById")
				.addParameter(String.class, "id")
				.addStatement("$T aggregateRoot = editableRepository.load(id)",
						aggregateRoot)
				.addStatement("copiedRepository.add(aggregateRoot)")
				.addModifiers(Modifier.PUBLIC).build();

	}
	

	protected MethodSpec returnAggregateById() {

		ClassName aggregate;
		String aggregateName = axonAnnotatedClass.getClassName()
				+ "RootAggregate";
		aggregate = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".aggregate", aggregateName);

		MethodSpec.Builder loadAggregateMethod = MethodSpec
				.methodBuilder("returnAggregateById")
				.addParameter(String.class, "id").addModifiers(Modifier.PUBLIC);
		loadAggregateMethod.addStatement("$T unitOfWork = $T.startAndGet()",
				UnitOfWork.class, DefaultUnitOfWork.class);
		loadAggregateMethod.returns(aggregate);
		loadAggregateMethod.beginControlFlow("try")
				.addStatement("return editableRepository.load(id)")
				.endControlFlow().beginControlFlow("finally")
				.addStatement("unitOfWork.commit()").endControlFlow();

		return loadAggregateMethod.build();

	}
	
	protected MethodSpec returnOriginalCopiedAggregateById() {

		ClassName aggregate;
		String aggregateName = axonAnnotatedClass.getClassName()
				+ "RootAggregate";
		aggregate = ClassName.get(axonAnnotatedClass.getPackageName()
				+ ".aggregate", aggregateName);

		MethodSpec.Builder loadAggregateMethod = MethodSpec
				.methodBuilder("returnOriginalCopiedAggregateById")
				.addParameter(String.class, "id").addModifiers(Modifier.PUBLIC);
		loadAggregateMethod.addStatement("$T unitOfWork = $T.startAndGet()",
				UnitOfWork.class, DefaultUnitOfWork.class);
		loadAggregateMethod.returns(aggregate);
		loadAggregateMethod.beginControlFlow("try")
				.addStatement("return copiedRepository.load(id)")
				.endControlFlow().beginControlFlow("finally")
				.addStatement("unitOfWork.commit()").endControlFlow();

		return loadAggregateMethod.build();

	}

	public TypeSpec getClassController() {

		ClassName modelInterface = ClassName.get(
				this.axonAnnotatedClass.getPackageName() + ".gui.model",
				"Model");
		TypeSpec.Builder viewInterfaceBuilder = TypeSpec
				.classBuilder(this.axonAnnotatedClass.getClassName() + "Model")
				.addField(this.editableRepository())
				.addField(this.copiedRepository())
				.addSuperinterface(modelInterface)
				.addJavadoc("Auto generated! Do not Modify!").addJavadoc("\n")
				.addModifiers(Modifier.PUBLIC);

		viewInterfaceBuilder.addMethod(this.returnAggregateById())
		.addMethod(this.returnOriginalCopiedAggregateById())
		.addMethod(this.sychronizeChangeById())
		.addMethod(this.constructor())
		.addMethod(this.addToCopiedRepo())
		.addMethod(this.addToEditableRepo());

		return viewInterfaceBuilder.build();
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

		constructorBuilder.addParameter(parameterizedRepository,
				"editableRepository", Modifier.FINAL).addParameter(
				parameterizedRepository, "copiedRepository", Modifier.FINAL);
		constructorBuilder
				.addStatement("this.editableRepository = editableRepository");

		constructorBuilder
				.addStatement("this.copiedRepository = copiedRepository");

		return constructorBuilder.build();

	}

}
