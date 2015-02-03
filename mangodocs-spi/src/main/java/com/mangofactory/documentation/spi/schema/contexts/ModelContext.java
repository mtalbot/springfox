package com.mangofactory.documentation.spi.schema.contexts;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Objects;
import com.mangofactory.documentation.builders.ModelBuilder;
import com.mangofactory.documentation.spi.DocumentationType;
import com.mangofactory.documentation.spi.schema.AlternateTypeProvider;

import java.lang.reflect.Type;
import java.util.Set;

import static com.google.common.collect.Sets.*;

public class ModelContext {
  private final Type type;
  private final boolean returnType;
  private final DocumentationType documentationType;

  private final ModelContext parentContext;
  private final boolean renderContainerType;
  private final Set<ResolvedType> seenTypes = newHashSet();
  private final ModelBuilder modelBuilder;
  private final AlternateTypeProvider alternateTypeProvider;

  ModelContext(Type type, boolean returnType, boolean renderContainerType, DocumentationType documentationType,
          AlternateTypeProvider alternateTypeProvider) {
    this.renderContainerType = renderContainerType;
    this.documentationType = documentationType;
    this.alternateTypeProvider = alternateTypeProvider;
    this.parentContext = null;
    this.type = type;
    this.returnType = returnType;
    this.modelBuilder = new ModelBuilder();
  }

  ModelContext(ModelContext parentContext, ResolvedType input) {
    this.parentContext = parentContext;
    this.type = input;
    this.returnType = parentContext.isReturnType();
    this.documentationType = parentContext.getDocumentationType();
    this.modelBuilder = new ModelBuilder();
    this.alternateTypeProvider = parentContext.alternateTypeProvider;
    renderContainerType = false;
  }

  public Type getType() {
    return type;
  }

  public ResolvedType resolvedType(TypeResolver resolver) {
    return resolver.resolve(getType());
  }

  public boolean isReturnType() {
    return returnType;
  }

  public boolean shouldRenderContainerType() {
    return renderContainerType;
  }

  public AlternateTypeProvider getAlternateTypeProvider() {
    return alternateTypeProvider;
  }

  public ResolvedType alternateFor(ResolvedType resolved) {
    return alternateTypeProvider.alternateFor(resolved);
  }

  public static ModelContext inputParam(Type type,
      DocumentationType documentationType,
      AlternateTypeProvider alternateTypeRules) {

    return new ModelContext(type, false, true, documentationType, alternateTypeRules);
  }

  public static ModelContext inputParamWithoutContainerType(Type type,
      DocumentationType documentationType,
     AlternateTypeProvider alternateTypeProvider) {
    return new ModelContext(type, false, false, documentationType, alternateTypeProvider);
  }

  public static ModelContext returnValueWithoutContainerType(Type type,
      DocumentationType documentationType,
      AlternateTypeProvider alternateTypeProvider) {
    return new ModelContext(type, true, false, documentationType, alternateTypeProvider);
  }

  public static ModelContext returnValue(Type type,
      DocumentationType documentationType,
      AlternateTypeProvider alternateTypeProvider) {
    return new ModelContext(type, true, true, documentationType, alternateTypeProvider);
  }

  public static ModelContext fromParent(ModelContext context, ResolvedType input) {
    return new ModelContext(context, input);
  }

  public boolean hasSeenBefore(ResolvedType resolvedType) {
    return seenTypes.contains(resolvedType)
            || seenTypes.contains(new TypeResolver().resolve(resolvedType.getErasedType()))
            || parentHasSeenBefore(resolvedType);
  }

  public DocumentationType getDocumentationType() {
    return documentationType;
  }

  private boolean parentHasSeenBefore(ResolvedType resolvedType) {
    if (parentContext == null) {
      return false;
    }
    return parentContext.hasSeenBefore(resolvedType);
  }

  public ModelBuilder getBuilder() {
    return modelBuilder;
  }

  public void seen(ResolvedType resolvedType) {
    seenTypes.add(resolvedType);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)  {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ModelContext that = (ModelContext) o;

    return Objects.equal(type, that.type) &&
            Objects.equal(documentationType, that.documentationType) &&
            Objects.equal(returnType, that.returnType);

  }

  @Override
  public int hashCode() {
    return Objects.hashCode(type, documentationType, returnType);
  }
}