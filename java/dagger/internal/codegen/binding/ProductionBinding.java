/*
 * Copyright (C) 2014 The Dagger Authors.
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

import static dagger.internal.codegen.extension.DaggerStreams.toImmutableSet;
import static dagger.internal.codegen.javapoet.TypeNames.isFutureType;

import androidx.room.compiler.processing.XMethodElement;
import androidx.room.compiler.processing.XType;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.internal.codegen.base.ContributionType;
import dagger.internal.codegen.base.SetType;
import dagger.spi.model.DependencyRequest;
import dagger.spi.model.Key;
import java.util.Optional;
import java.util.stream.Stream;

/** A value object representing the mechanism by which a {@link Key} can be produced. */
@AutoValue
public abstract class ProductionBinding extends ContributionBinding {

  @Override
  public BindingType bindingType() {
    return BindingType.PRODUCTION;
  }

  @Override
  public abstract Optional<ProductionBinding> unresolved();

  @Override
  public ImmutableSet<DependencyRequest> implicitDependencies() {
    return Stream.of(executorRequest(), monitorRequest())
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(toImmutableSet());
  }

  /** What kind of object a {@code @Produces}-annotated method returns. */
  public enum ProductionKind {
    /** A value. */
    IMMEDIATE,
    /** A {@code ListenableFuture<T>}. */
    FUTURE,
    /** A {@code Set<ListenableFuture<T>>}. */
    SET_OF_FUTURE;

    /** Returns the kind of object a {@code @Produces}-annotated method returns. */
    public static ProductionKind fromProducesMethod(XMethodElement producesMethod) {
      if (isFutureType(producesMethod.getReturnType())) {
        return FUTURE;
      } else if (ContributionType.fromBindingElement(producesMethod)
              .equals(ContributionType.SET_VALUES)
          && isFutureType(SetType.from(producesMethod.getReturnType()).elementType())) {
        return SET_OF_FUTURE;
      } else {
        return IMMEDIATE;
      }
    }
  }

  /**
   * Returns the kind of object the produces method returns. All production bindings from
   * {@code @Produces} methods will have a production kind, but synthetic production bindings may
   * not.
   */
  public abstract Optional<ProductionKind> productionKind();

  /** Returns the list of types in the throws clause of the method. */
  public abstract ImmutableList<XType> thrownTypes();

  /**
   * If this production requires an executor, this will be the corresponding request.  All
   * production bindings from {@code @Produces} methods will have an executor request, but
   * synthetic production bindings may not.
   */
  abstract Optional<DependencyRequest> executorRequest();

  /** If this production requires a monitor, this will be the corresponding request.  All
   * production bindings from {@code @Produces} methods will have a monitor request, but synthetic
   * production bindings may not.
   */
  abstract Optional<DependencyRequest> monitorRequest();

  // Profiling determined that this method is called enough times that memoizing it had a measurable
  // performance improvement for large components.
  @Memoized
  @Override
  public boolean requiresModuleInstance() {
    return super.requiresModuleInstance();
  }

  public static Builder builder() {
    return new AutoValue_ProductionBinding.Builder()
        .explicitDependencies(ImmutableList.<DependencyRequest>of())
        .thrownTypes(ImmutableList.<XType>of());
  }

  @Override
  public abstract Builder toBuilder();

  @Memoized
  @Override
  public abstract int hashCode();

  // TODO(ronshapiro,dpb): simplify the equality semantics
  @Override
  public abstract boolean equals(Object obj);

  /** A {@link ProductionBinding} builder. */
  @AutoValue.Builder
  @CanIgnoreReturnValue
  public abstract static class Builder
      extends ContributionBinding.Builder<ProductionBinding, Builder> {

    @Override
    public Builder dependencies(Iterable<DependencyRequest> dependencies) {
      return explicitDependencies(dependencies);
    }

    abstract Builder explicitDependencies(Iterable<DependencyRequest> dependencies);

    abstract Builder productionKind(ProductionKind productionKind);

    @Override
    public abstract Builder unresolved(ProductionBinding unresolved);

    abstract Builder thrownTypes(Iterable<XType> thrownTypes);

    abstract Builder executorRequest(DependencyRequest executorRequest);

    abstract Builder monitorRequest(DependencyRequest monitorRequest);
  }
}
