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

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import java.util.function.Predicate;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;

import javax.lang.model.type.ArrayType;

import org.microbean.construct.Domain;

import org.microbean.construct.element.AnnotationMirrors;
import org.microbean.construct.element.SyntheticAnnotationMirror;
import org.microbean.construct.element.SyntheticAnnotationTypeElement;
import org.microbean.construct.element.UniversalElement;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;
import static java.lang.constant.ConstantDescs.NULL;

import static java.lang.constant.MethodHandleDesc.ofConstructor;

import static java.util.Collections.unmodifiableList;

import static java.util.Objects.requireNonNull;

import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;

/**
 * A utility class for working with <dfn>qualifiers</dfn>.
 *
 * <p>This class is currently not used by other classes in this package. It may be useful in a variety of dependency
 * injection systems.</p>
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 */
public class Qualifiers implements Constable {


  /*
   * Instance fields.
   */


  private final Predicate<? super ExecutableElement> annotationElementInclusionPredicate;

  private final AnnotationMirror metaQualifier;

  private final List<AnnotationMirror> metaQualifiers;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link Qualifiers}.
   *
   * @param domain a non-{@code null} {@link Domain}
   *
   * @exception NullPointerException if {@code domain} is {@code null}
   *
   * @see #Qualifiers(Domain, AnnotationMirror, Predicate)
   */
  public Qualifiers(final Domain domain) {
    this(domain, null, null);
  }

  /**
   * Creates a new {@link Qualifiers}.
   *
   * @param metaQualifier a non-{@code null} {@link AnnotationMirror} to serve as the {@linkplain #metaQualifier() meta-qualifier}
   *
   * @exception NullPointerException if {@code metaQualifier} is {@code null}
   *
   * @see #Qualifiers(Domain, AnnotationMirror, Predicate)
   */
  public Qualifiers(final AnnotationMirror metaQualifier) {
    this(null, requireNonNull(metaQualifier, "metaQualifier"), null);
  }

  /**
   * Creates a new {@link Qualifiers}.
   *
   * @param metaQualifier a non-{@code null} {@link AnnotationMirror} to serve as the {@linkplain #metaQualifier() meta-qualifier}
   *
   * @param annotationElementInclusionPredicate a {@link Predicate} that returns {@code true} if a given {@link
   * ExecutableElement}, representing an annotation element, is to be included in the computation; may be {@code null}
   * in which case it is as if {@code e -> true} were supplied instead
   *
   * @exception NullPointerException if {@code metaQualifier} is {@code null}
   *
   * @see #Qualifiers(Domain, AnnotationMirror, Predicate)
   */
  public Qualifiers(final AnnotationMirror metaQualifier,
                    final Predicate<? super ExecutableElement> annotationElementInclusionPredicate) {
    this(null, requireNonNull(metaQualifier, "metaQualifier"), annotationElementInclusionPredicate);
  }

  /**
   * Creates a new {@link Qualifiers}.
   *
   * @param domain a {@link Domain}; if {@code null}, then {@code metaQualifier} must not be {@code null}
   *
   * @param metaQualifier an {@link AnnotationMirror} to serve as the {@linkplain #metaQualifier() meta-qualifier}; may
   * (commonly) be {@code null} in which case a synthetic meta-qualifier will be used instead; must not be {@code null}
   * if {@code domain} is {@code null}
   *
   * @param annotationElementInclusionPredicate a {@link Predicate} that returns {@code true} if a given {@link
   * ExecutableElement}, representing an annotation element, is to be included in the computation; may be {@code null}
   * in which case it is as if {@code e -> true} were supplied instead
   *
   * @exception NullPointerException if {@code domain} is {@code null} in certain situations
   */
  public Qualifiers(final Domain domain,
                    final AnnotationMirror metaQualifier,
                    final Predicate<? super ExecutableElement> annotationElementInclusionPredicate) {
    super();
    if (metaQualifier == null) {
      final List<? extends AnnotationMirror> as = domain.typeElement("java.lang.annotation.Documented").getAnnotationMirrors();
      assert as.size() == 3; // @Documented, @Retention, @Target, in that order, all annotated in turn with each other
      this.metaQualifier =
        new SyntheticAnnotationMirror(new SyntheticAnnotationTypeElement(List.of(as.get(0), // @Documented
                                                                                 as.get(1), // @Retention(RUNTIME) (happens fortuitously to be RUNTIME)
                                                                                 as.get(2)), // @Target(ANNOTATION_TYPE) (happens fortuitously to be ANNOTATION_TYPE)
                                                                         "Qualifier"));
    } else {
      this.metaQualifier = metaQualifier;
    }
    this.annotationElementInclusionPredicate = annotationElementInclusionPredicate == null ? Qualifiers::returnTrue : annotationElementInclusionPredicate;
    this.metaQualifiers = List.of(this.metaQualifier);
  }


  /*
   * Instance methods.
   */

  // The contains* methods below do not apply only to qualifiers. That makes them smell a little funky here but it's not
  // really worth breaking out, I don't think. The whole annotationElementInclusionPredicate thing may belong in
  // microbean-bean, but it really doesn't *have* to be bean-qualifiers-specific.

  /**
   * Returns {@code true} if and only if the supplied {@link Collection} of {@link AnnotationMirror}s contains an {@link
   * AnnotationMirror} that is {@linkplain AnnotationMirrors#sameAnnotation(AnnotationMirror, AnnotationMirror,
   * Predicate) the same} as the supplied {@link AnnotationMirror}.
   *
   * @param c a non-{@code null} {@link Collection} of {@link AnnotationMirror}s
   *
   * @param a a non-{@code null} {@link AnnotationMirror}
   *
   * @return {@code true} if and only if the supplied {@link Collection} of {@link AnnotationMirror}s contains an {@link
   * AnnotationMirror} that is {@linkplain AnnotationMirrors#sameAnnotation(AnnotationMirror, AnnotationMirror,
   * Predicate) the same} as the supplied {@link AnnotationMirror}
   *
   * @exception NullPointerException if {@code c} or {@code a} is {@code null}
   *
   * @see AnnotationMirrors#sameAnnotation(AnnotationMirror, AnnotationMirror, Predicate)
   *
   * @see AnnotationMirrors#contains(Collection, AnnotationMirror, Predicate)
   *
   * @see #Qualifiers(Domain, AnnotationMirror, Predicate)
   */
  public final boolean contains(final Collection<? extends AnnotationMirror> c, final AnnotationMirror a) {
    return AnnotationMirrors.contains(c, a, this.annotationElementInclusionPredicate);
  }

  /**
   * Returns {@code true} if and only if {@code c0} contains all {@linkplain
   * AnnotationMirrors#sameAnnotation(AnnotationMirror, AnnotationMirror, Predicate) the same} {@link AnnotationMirror}s
   * as are found in {@code c1},
   *
   * @param c0 a non-{@code null} {@link Collection} of {@link AnnotationMirror}s
   *
   * @param c1 a non-{@code null} {@link Collection} of {@link AnnotationMirror}s
   *
   * @return {@code true} if and only if {@code c0} contains all {@linkplain
   * AnnotationMirrors#sameAnnotation(AnnotationMirror, AnnotationMirror, Predicate) the same} {@link AnnotationMirror}s
   * as are found in {@code c1}
   *
   * @exception NullPointerException if either {@code c0} or {@code c1} is {@code null}
   *
   * @see AnnotationMirrors#sameAnnotation(AnnotationMirror, AnnotationMirror, Predicate)
   *
   * @see AnnotationMirrors#containsAll(Collection, Collection, Predicate)
   *
   * @see #Qualifiers(Domain, AnnotationMirror, Predicate)
   */
  public final boolean containsAll(final Collection<? extends AnnotationMirror> c0,
                                   final Collection<? extends AnnotationMirror> c1) {
    return AnnotationMirrors.containsAll(c0, c1, this.annotationElementInclusionPredicate);
  }

  /**
   * Returns a non-{@code null}, determinate {@link Optional} housing a {@link ConstantDesc} describing this {@link
   * Qualifiers}, or an {@linkplain Optional#isEmpty() empty} {@link Optional} if it cannot be described.
   *
   * @return a non-{@code null}, determinate {@link Optional} housing a {@link ConstantDesc} describing this {@link
   * Qualifiers}, or an {@linkplain Optional#isEmpty() empty} {@link Optional} if it cannot be described
   */
  @Override // Constable
  public Optional<? extends ConstantDesc> describeConstable() {
    return (this.metaQualifier instanceof Constable c ? c.describeConstable() : Optional.<ConstantDesc>empty())
      .flatMap(mqDesc -> (this.annotationElementInclusionPredicate == null ?
                          Optional.of(NULL) :
                          this.annotationElementInclusionPredicate instanceof Constable c ?
                          c.describeConstable() :
                          Optional.<ConstantDesc>empty())
               .map(pDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                    ofConstructor(this.getClass().describeConstable().orElseThrow(),
                                                                  Domain.class.describeConstable().orElseThrow(),
                                                                  AnnotationMirror.class.describeConstable().orElseThrow(),
                                                                  Predicate.class.describeConstable().orElseThrow()),
                                                    NULL,
                                                    mqDesc,
                                                    pDesc)));
  }

  /**
   * Returns a non-{@code null}, determinate {@link AnnotationMirror} that can be used to designate (meta-annotate)
   * other annotations as <dfn>qualifiers</dfn>.
   *
   * @return a non-{@code null}, determinate {@link AnnotationMirror} that can be used to designate (meta-annotate)
   * other annotations as <dfn>qualifiers</dfn>
   */
  public final AnnotationMirror metaQualifier() {
    return this.metaQualifier;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link AnnotationMirror} {@linkplain
   * #sameAnnotation(AnnotationMirror, AnnotationMirror) is the same annotation as} the supplied {@link
   * AnnotationMirror}.
   *
   * @param a a non-{@code null} {@link AnnotationMirror}
   *
   * @return {@code true} if and only if if and only if the supplied {@link AnnotationMirror} {@linkplain
   * #sameAnnotation(AnnotationMirror, AnnotationMirror) is the same annotation as} the supplied {@link
   * AnnotationMirror}
   *
   * @exception NullPointerException if {@code a} is {@code null}
   *
   * @see #sameAnnotation(AnnotationMirror, AnnotationMirror)
   */
  public final boolean metaQualifier(final AnnotationMirror a) {
    return this.sameAnnotation(this.metaQualifier(), a);
  }

  /**
   * Returns a non-{@code null}, determinate, immutable {@link List} whose sole element is the {@linkplain
   * #metaQualifier() meta-qualifier} annotation.
   *
   * @return a non-{@code null}, determinate, immutable {@link List} whose sole element is the {@linkplain
   * #metaQualifier() meta-qualifier} annotation
   */
  public final List<AnnotationMirror> metaQualifiers() {
    return this.metaQualifiers;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link AnnotationMirror} has an {@linkplain
   * AnnotationMirror#getAnnotationType() annotation type} declared by a {@link TypeElement} that is {@linkplain
   * javax.lang.model.AnnotatedConstruct#getAnnotationMirrors() annotated with} at least one annotation {@linkplain
   * #metaQualifier(AnnotationMirror) deemed to be the meta-qualifier}.
   *
   * @param a a non-{@code null} {@link AnnotationMirror}
   *
   * @return {@code true} if and only if the supplied {@link AnnotationMirror} has an {@linkplain
   * AnnotationMirror#getAnnotationType() annotation type} declared by a {@link TypeElement} that is {@linkplain
   * javax.lang.model.AnnotatedConstruct#getAnnotationMirrors() annotated with} at least one annotation {@linkplain
   * #metaQualifier(AnnotationMirror) deemed to be the meta-qualifier}
   *
   * @exception NullPointerException if {@code a} is {@code null}
   */
  public final boolean qualifier(final AnnotationMirror a) {
    if (!this.metaQualifier(a)) {
      final TypeElement annotationInterface = (TypeElement)a.getAnnotationType().asElement();
      if (annotationInterface.getKind() == ANNOTATION_TYPE) {
        for (final AnnotationMirror ma : annotationInterface.getAnnotationMirrors()) {
          if (this.metaQualifier(ma)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Returns a non-{@code null}, determinate, immutable {@link List} of {@link AnnotationMirror} instances drawn from
   * the supplied {@link Collection} that were {@linkplain #qualifier(AnnotationMirror) deemed to be qualifiers}.
   *
   * @param as a non-{@code null} {@link Collection} of {@link AnnotationMirror}s
   *
   * @return a non-{@code null}, determinate, immutable {@link List} of {@link AnnotationMirror} instances drawn from
   * the supplied {@link Collection} that were {@linkplain #qualifier(AnnotationMirror) deemed to be qualifiers}
   *
   * @exception NullPointerException if {@code as} is {@code null}
   *
   * @see #qualifier(AnnotationMirror)
   */
  public List<AnnotationMirror> qualifiers(final Collection<? extends AnnotationMirror> as) {
    if (as.isEmpty()) {
      return List.of();
    }
    final List<AnnotationMirror> l = new ArrayList<>(as.size());
    for (final AnnotationMirror a : as) {
      if (this.qualifier(a)) {
        l.add(a);
      }
    }
    return l.isEmpty() ? List.of() : unmodifiableList(l);
  }

  /**
   * Determines whether the two {@link AnnotationMirror}s represent the same (underlying, otherwise opaque) annotation.
   *
   * @param am0 an {@link AnnotationMirror}; may be {@code null}
   *
   * @param am1 an {@link AnnotationMirror}; may be {@code null}
   *
   * @return {@code true} if the supplied {@link AnnotationMirror}s represent the same (underlying, otherwise opaque)
   * annotation; {@code false} otherwise
   *
   * @see AnnotationMirrors#sameAnnotation(AnnotationMirror, AnnotatonMirror, Predicate)
   *
   * @see #Qualifiers(Domain, AnnotationMirror, Predicate)
   */
  public final boolean sameAnnotation(final AnnotationMirror am0, final AnnotationMirror am1) {
    return AnnotationMirrors.sameAnnotation(am0, am1, this.annotationElementInclusionPredicate);
  }

  private static final <X> boolean returnTrue(final X ignored) {
    return true;
  }

}
