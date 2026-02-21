/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2026 microBean™.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.microbean.assign;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import java.util.function.Predicate;

import javax.lang.model.AnnotatedConstruct;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import org.microbean.construct.element.AnnotationMirrors;

import static org.microbean.construct.element.AnnotationMirrors.containsAll;
import static org.microbean.construct.element.AnnotationMirrors.sameAnnotation;

/**
 * An {@link Annotated} implementation wrapping an {@link AnnotatedConstruct}.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 */
// This deliberately doesn't implement TypeMirror or Element and delegate operations since it is used only for caching.
// It would be nice to ensure this class does not have to be public.
// This class may become an inner or nested class of something else.
final class CacheableAnnotatedConstruct<T extends AnnotatedConstruct> implements Annotated<T> {

  private final List<AnnotationMirror> annotations;

  private final T annotated;

  private final Predicate<? super ExecutableElement> p;

  private int hashCode;

  /**
   * Creates a new {@link CacheableAnnotatedConstruct}.
   *
   * @param annotated a non-{@code null} {@link AnnotatedConstruct}
   *
   * @param p a {@link Predicate} that returns {@code true} if a given {@link ExecutableElement}, representing an
   * annotation interface element, is to be included in equality and hashcode computations; may be {@code null} in which
   * case it is as if {@code e -> true} were supplied instead
   *
   * @exception NullPointerException if {@code annotated} is {@code null}
   *
   * @see AnnotationMirrors#sameAnnotation(AnnotationMirror, AnnotationMirror, Predicate)
   *
   * @see AnnotationMirrors#hashCode(AnnotationMirror, Predicate)
   */
  CacheableAnnotatedConstruct(final T annotated, final Predicate<? super ExecutableElement> p) {
    super();
    this.p = p == null ? ee -> true : p;
    if (annotated instanceof Element e) {
      final List<AnnotationMirror> as = new ArrayList<>(e.asType().getAnnotationMirrors());
      as.addAll(e.getAnnotationMirrors());
      this.annotations = List.copyOf(as);
    } else {
      this.annotations = List.copyOf(annotated.getAnnotationMirrors());
    }
    this.annotated = annotated;
  }

  /**
   * Returns a non-{@code null}, immutable, determinate {@link List} of the {@link AnnotationMirror}s supplied at
   * {@linkplain #CacheableAnnotatedConstruct(AnnotatedConstruct, Predicate) construction time}.
   *
   * @return a non-{@code null}, immutable, determinate {@link List} of the {@link AnnotationMirror}s supplied at
   * {@linkplain #CacheableAnnotatedConstruct(AnnotatedConstruct, Predicate) construction time}
   *
   * @see #CacheableAnnotatedConstruct(AnnotatedConstruct, Predicate)
   */
  @Override // Annotated<T>
  public final List<AnnotationMirror> annotations() {
    return this.annotations;
  }

  /**
   * Returns the determinate annotated object supplied at {@linkplain #CacheableAnnotatedConstruct(AnnotatedConstruct,
   * Predicate) construction time}.
   *
   * @return the determinate annotated object supplied at {@linkplain #CacheableAnnotatedConstruct(AnnotatedConstruct,
   * Predicate) construction time}
   *
   * @see #CacheableAnnotatedConstruct(AnnotatedConstruct, Predicate)
   */
  @Override // Annotated<T>
  public final T annotated() {
    return this.annotated;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Object} is equal to this {@link CacheableAnnotatedConstruct}.
   *
   * <p>Two {@link CacheableAnnotatedConstruct}s are considered equal when their {@linkplain #annotated() annotated object}s are
   * {@linkplain Object#equals(Object) equal} and when each {@link AnnotationMirror} is the {@linkplain
   * AnnotationMirrors#sameAnnotation(AnnotationMirror, AnnotationMirror, Predicate) same as} every other {@link
   * AnnotationMirror}.</p>
   *
   * @param other the {@link Object} to test; may be {@code null} in which case {@code false} will be returned
   *
   * @return {@code true} if and only if the supplied {@link Object} is equal to this {@link CacheableAnnotatedConstruct}
   *
   * @see AnnotationMirrors#sameAnnotation(AnnotationMirror, AnnotationMirror, Predicate)
   */
  @Override // Object
  public final boolean equals(final Object other) {
    return this == other || switch (other) {
    case null -> false;
    case CacheableAnnotatedConstruct<? extends AnnotatedConstruct> her ->
      this.getClass() == her.getClass() &&
      Objects.equals(this.annotated(), her.annotated()) &&
      containsAll(this.annotations(), her.annotations(), this.p) &&
      containsAll(her.annotations(), this.annotations(), her.p);
    default -> false;
    };
  }

  /**
   * Returns a non-zero hashcode for this {@link CacheableAnnotatedConstruct} based on both its {@linkplain
   * #annotations() annotations} and its {@linkplain #annotated() annotated object}.
   *
   * @return a non-zero hashcode for this {@link CacheableAnnotatedConstruct} based on both its {@linkplain
   * #annotations() annotations} and its {@linkplain #annotated() annotated object}
   *
   * @see #annotations()
   *
   * @see #annotated()
   *
   * @see AnnotationMirrors#hashCode(AnnotationMirror, Predicate)
   */
  @Override // Object
  public final int hashCode() {
    if (this.hashCode == 0) { // volatile not needed, computation is determinate and deterministic
      int hashCode = 17 * 31 + (this.annotated == null ? 0 : this.annotated().hashCode());
      for (final AnnotationMirror a : this.annotations()) {
        hashCode = 17 * hashCode + AnnotationMirrors.hashCode(a, this.p);
      }
      this.hashCode = hashCode;
    }
    return hashCode;
  }

  @Override // Object
  public final String toString() {
    return this.annotations() + " " + this.annotated();
  }

}
