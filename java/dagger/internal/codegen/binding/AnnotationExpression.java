/*
 * Copyright (C) 2016 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.internal.codegen.binding;

import static androidx.room.compiler.processing.XTypeKt.isArray;
import static androidx.room.compiler.processing.compat.XConverters.toJavac;
import static dagger.internal.codegen.binding.SourceFiles.classFileName;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableList;
import static dagger.internal.codegen.extension.DaggerStreams.toImmutableMap;
import static dagger.internal.codegen.javapoet.CodeBlocks.makeParametersCodeBlock;
import static dagger.internal.codegen.javapoet.CodeBlocks.toParametersCodeBlock;
import static dagger.internal.codegen.xprocessing.XAnnotationValues.hasAnnotationValue;
import static dagger.internal.codegen.xprocessing.XAnnotationValues.hasArrayValue;
import static dagger.internal.codegen.xprocessing.XAnnotationValues.hasByteValue;
import static dagger.internal.codegen.xprocessing.XAnnotationValues.hasCharValue;
import static dagger.internal.codegen.xprocessing.XAnnotationValues.hasDoubleValue;
import static dagger.internal.codegen.xprocessing.XAnnotationValues.hasEnumValue;
import static dagger.internal.codegen.xprocessing.XAnnotationValues.hasFloatValue;
import static dagger.internal.codegen.xprocessing.XAnnotationValues.hasLongValue;
import static dagger.internal.codegen.xprocessing.XAnnotationValues.hasShortValue;
import static dagger.internal.codegen.xprocessing.XAnnotationValues.hasStringValue;
import static dagger.internal.codegen.xprocessing.XAnnotationValues.hasTypeValue;
import static dagger.internal.codegen.xprocessing.XElements.getSimpleName;
import static dagger.internal.codegen.xprocessing.XTypes.asArray;

import androidx.room.compiler.processing.XAnnotation;
import androidx.room.compiler.processing.XAnnotationValue;
import androidx.room.compiler.processing.XMethodElement;
import androidx.room.compiler.processing.XType;
import androidx.room.compiler.processing.XTypeElement;
import com.google.common.collect.ImmutableMap;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import dagger.internal.codegen.xprocessing.XElements;

/**
 * Returns an expression creating an instance of the visited annotation type. Its parameter must be
 * a class as generated by {@link dagger.internal.codegen.writing.AnnotationCreatorGenerator}.
 *
 * <p>Note that {@code AnnotationValue#toString()} is the source-code representation of the value
 * <em>when used in an annotation</em>, which is not always the same as the representation needed
 * when creating the value in a method body.
 *
 * <p>For example, inside an annotation, a nested array of {@code int}s is simply {@code {1, 2, 3}},
 * but in code it would have to be {@code new int[] {1, 2, 3}}.
 */
public final class AnnotationExpression {
  private final XAnnotation annotation;
  private final ClassName creatorClass;

  AnnotationExpression(XAnnotation annotation) {
    this.annotation = annotation;
    this.creatorClass = getAnnotationCreatorClassName(annotation.getType().getTypeElement());
  }

  /**
   * Returns an expression that calls static methods on the annotation's creator class to create an
   * annotation instance equivalent the annotation passed to the constructor.
   */
  CodeBlock getAnnotationInstanceExpression() {
    return getAnnotationInstanceExpression(annotation);
  }

  private CodeBlock getAnnotationInstanceExpression(XAnnotation annotation) {
    ImmutableMap<String, XType> valueTypesByName =
        annotation.getType().getTypeElement().getDeclaredMethods().stream()
            .filter(method -> method.getParameters().isEmpty())
            .collect(toImmutableMap(XElements::getSimpleName, XMethodElement::getReturnType));
    return CodeBlock.of(
        "$T.$L($L)",
        creatorClass,
        createMethodName(annotation.getType().getTypeElement()),
        makeParametersCodeBlock(
            annotation.getAnnotationValues().stream()
                .map(
                    value -> {
                      String name =
                          value.getName(); // SUPPRESS_GET_NAME_CHECK: This is not XElement
                      return getValueExpression(value, valueTypesByName.get(name));
                    })
                .collect(toImmutableList())));
  }

  /**
   * Returns the name of the generated class that contains the static {@code create} methods for an
   * annotation type.
   */
  public static ClassName getAnnotationCreatorClassName(XTypeElement annotationType) {
    ClassName annotationTypeName = annotationType.getClassName();
    return annotationTypeName
        .topLevelClassName()
        .peerClass(classFileName(annotationTypeName) + "Creator");
  }

  public static String createMethodName(XTypeElement annotationType) {
    return "create" + getSimpleName(annotationType);
  }

  /**
   * Returns an expression that evaluates to a {@code value} of a given type on an {@code
   * annotation}.
   */
  CodeBlock getValueExpression(XAnnotationValue value, XType valueType) {
    return isArray(valueType)
        ? CodeBlock.of(
            "new $T[] $L",
            asArray(valueType).getComponentType().getRawType().getTypeName(),
            visit(value))
        : visit(value);
  }

  private CodeBlock visit(XAnnotationValue value) {
    if (hasEnumValue(value)) {
      return CodeBlock.of(
          "$T.$L",
          value.asEnum().getEnclosingElement().getClassName(),
          getSimpleName(value.asEnum()));
    } else if (hasAnnotationValue(value)) {
      return getAnnotationInstanceExpression(value.asAnnotation());
    } else if (hasTypeValue(value)) {
      return CodeBlock.of("$T.class", value.asType().getTypeName());
    } else if (hasStringValue(value)) {
      return CodeBlock.of("$S", value.asString());
    } else if (hasByteValue(value)) {
      return CodeBlock.of("(byte) $L", value.asByte());
    } else if (hasCharValue(value)) {
      // TODO(bcorso): This relies on AnnotationValue.toString() to properly output escaped
      // characters like '\n'. See https://github.com/square/javapoet/issues/698.
      return CodeBlock.of("$L", toJavac(value));
    } else if (hasDoubleValue(value)) {
      return CodeBlock.of("$LD", value.asDouble());
    } else if (hasFloatValue(value)) {
      return CodeBlock.of("$LF", value.asFloat());
    } else if (hasLongValue(value)) {
      return CodeBlock.of("$LL", value.asLong());
    } else if (hasShortValue(value)) {
      return CodeBlock.of("(short) $L", value.asShort());
    } else if (hasArrayValue(value)) {
      return CodeBlock.of(
          "{$L}",
          value.asAnnotationValueList().stream().map(this::visit).collect(toParametersCodeBlock()));
    } else {
      return CodeBlock.of("$L", value.getValue());
    }
  }
}
