/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2025–2026 microBean™.
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

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import java.util.function.Predicate;

import javax.lang.model.element.Element;
import javax.lang.model.element.QualifiedNameable;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import org.microbean.construct.Domain;

import static java.lang.constant.ConstantDescs.BSM_INVOKE;

import static java.lang.constant.MethodHandleDesc.ofConstructor;

import static java.util.Comparator.comparing;

import static java.util.HashSet.newHashSet;

import static java.util.Objects.requireNonNull;

/**
 * A utility class that assists with working with a {@link Domain} instance.
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #supertypes(TypeMirror, Predicate)
 *
 * @see Domain
 */
public class Types implements Constable {


  /*
   * Instance fields.
   */


  private final Comparator<TypeMirror> c;

  private final Domain domain;


  /*
   * Constructors.
   */


  /**
   * Creates a new {@link Types}.
   *
   * @param domain a non-{@code null} {@link Domain}
   *
   * @exception NullPointerException if {@code domain} is {@code null}
   */
  public Types(final Domain domain) {
    super();
    this.c = comparing(TypeMirror::getKind, PrimitiveAndReferenceTypeKindComparator.INSTANCE)
      .thenComparing(new SpecializationComparator(domain))
      .thenComparing(Types::erasedName);
    this.domain = domain;
  }


  /*
   * Instance methods.
   */


  /**
   * Returns an {@link Optional} housing a {@link ConstantDesc} that represents this {@link Types}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>The default implementation of this method relies on the presence of a {@code public} constructor that accepts a
   * single {@link Domain}-typed argument.</p>
   *
   * <p>The {@link Optional} returned by an invocation of this method may be, and often will be, {@linkplain
   * Optional#isEmpty() empty}.</p>
   *
   * @return an {@link Optional} housing a {@link ConstantDesc} that represents this {@link Types}; never {@code null}
   *
   * @see Constable#describeConstable()
   */
  @Override // Constable
  public Optional<? extends ConstantDesc> describeConstable() {
    return (this.domain instanceof Constable c ? c.describeConstable() : Optional.<ConstantDesc>empty())
      .map(domainDesc -> DynamicConstantDesc.of(BSM_INVOKE,
                                                ofConstructor(this.getClass().describeConstable().orElseThrow(),
                                                              Domain.class.describeConstable().orElseThrow()),
                                                domainDesc));
  }

  /**
   * Returns the {@link Domain} affiliated with this {@link Types} instance.
   *
   * @return the {@link Domain} affiliated with this {@link Types} instance; never {@code null}
   */
  protected final Domain domain() {
    return this.domain;
  }

  /**
   * Returns {@code true} if and only if the supplied {@link Object} is equal to this {@link Types}.
   *
   * <p>The supplied {@link Object} is considered to be equal to this {@link Types} if and only if {@linkplain
   * Object#getClass() its class} is that of this {@link Types}, and if the return value of an invocation of this {@link
   * Types}' {@link #domain() domain()} method {@linkplain Object#equals(Object) is equal to} the {@link Domain}
   * returned by an invocation of the other {@link Types}' {@link #domain() domain()} method.</p>
   *
   * @param other an {@link Object}; may be {@code null}
   *
   * @return {@code true} if and only if the supplied {@link Object} is equal to this {@link Types}
   *
   * @see #domain()
   */
  @Override // Object
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    } else if (other != null && other.getClass() == this.getClass()) {
      return this.domain.equals(((Types)other).domain);
    } else {
      return false;
    }
  }

  /**
   * Returns a hashcode for this {@link Types} based solely on {@linkplain #domain() its <code>Domain</code>}.
   *
   * @return a hashcode for this {@link Types}
   */
  @Override // Object
  public int hashCode() {
    return this.domain.hashCode();
  }

  private final boolean isInterface(final Element e) {
    return e.getKind().isInterface();
  }

  private final boolean isInterface(final TypeMirror t) {
    return t.getKind() == TypeKind.DECLARED && isInterface(((DeclaredType)t).asElement());
  }

  /**
   * Returns a non-{@code null}, determinate {@link SupertypeList} of the supertypes of the supplied {@link TypeMirror}.
   *
   * <p>No element in the returned {@link List} will be {@code null}.</p>
   *
   * <p>The supplied {@link TypeMirror} will be the first element in the returned {@link SupertypeList}. (The supertype
   * relation is reflexive.)</p>
   *
   * <p>No two elements in the list will be {@linkplain Objects#equals(Object) equal} or {@linkplain
   * Domain#sameType(TypeMirror, TypeMirror) the same type}. (The {@link List} is an ordered set.)</p>
   *
   * <p>This method returns determinate values.</p>
   *
   * <p>The elements of the returned {@link List} will be in the following (partial) order:</p>
   *
   * <ol>
   *
   * <li>The supplied {@link TypeMirror} precedes any other elements.</li>
   *
   * <li>{@linkplain Domain#subtype(TypeMirror, TypeMirror) Subtypes} precede supertypes.</li>
   *
   * <li>Non-interface types precede interface types.</li>
   *
   * </ol>
   *
   * @param t a non-{@code null} {@link TypeMirror}
   *
   * @return a non-{@code null}, determinate {@link SupertypeList} of the supertypes of the supplied {@link TypeMirror}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   *
   * @see #supertypes(TypeMirror, Predicate)
   *
   * @see Domain#directSupertypes(TypeMirror)
   *
   * @spec https://docs.oracle.com/javase/specs/jls/se23/html/jls-4.html#jls-4.10 Java Language Specification, section
   * 4.10
   *
   * @spec https://docs.oracle.com/javase/specs/jls/se23/html/jls-4.html#jls-4.10.2 Java Language Specification, section
   * 4.10.2
   */
  public final SupertypeList supertypes(final TypeMirror t) {
    return this.supertypes(t, Types::returnTrue);
  }

  /**
   * Returns a non-{@code null}, determinate {@link SupertypeList} of the supertypes of the supplied {@link TypeMirror},
   * filtered using the supplied {@link Predicate}.
   *
   * <p>No element in the returned {@link SupertypeList} will be {@code null}.</p>
   *
   * <p>Unless the supplied {@link Predicate} prevents it, the supplied {@link TypeMirror} will be the first element in
   * the returned {@link SupertypeList}. (The supertype relation is reflexive.)</p>
   *
   * <p>No two elements in the list will be {@linkplain Objects#equals(Object) equal} or {@linkplain
   * Domain#sameType(TypeMirror, TypeMirror) the same type}. (The {@link SupertypeList} is an ordered set.)</p>
   *
   * <p>The elements of the returned {@link SupertypeList} will be in the following (partial) order:</p>
   *
   * <ol>
   *
   * <li>{@linkplain Domain#subtype(TypeMirror, TypeMirror) Subtypes} precede supertypes.</li>
   *
   * <li>Non-interface types precede interface types.</li>
   *
   * </ol>
   *
   * @param t a non-{@code null} {@link TypeMirror}
   *
   * @param p a non-{@code null} {@link Predicate}
   *
   * @return a non-{@code null}, determinate {@link SupertypeList} of the supertypes of the supplied {@link TypeMirror},
   * filtered using the supplied {@link Predicate}
   *
   * @exception NullPointerException if any argument is {@code null}
   *
   * @see Domain#directSupertypes(TypeMirror)
   *
   * @spec https://docs.oracle.com/javase/specs/jls/se23/html/jls-4.html#jls-4.10 Java Language Specification, section
   * 4.10
   *
   * @spec https://docs.oracle.com/javase/specs/jls/se23/html/jls-4.html#jls-4.10.2 Java Language Specification, section
   * 4.10.2
   */
  public final SupertypeList supertypes(final TypeMirror t, final Predicate<? super TypeMirror> p) {
    // This list will start by holding non-interface types. Once it has served its purpose here, it will be reused for a
    // different purpose.
    ArrayList<TypeMirror> types = new ArrayList<>(7); // arbitrary size
    final ArrayList<TypeMirror> interfaceTypes = new ArrayList<>(17); // arbitrary size
    this.supertypes(t, p, types, interfaceTypes, newHashSet(13)); // arbitrary size
    final int interfaceIndex;
    if (interfaceTypes.isEmpty()) {
      interfaceIndex = -1;
      if (types.isEmpty()) {
        return SupertypeList.of();
      }
    } else {
      if (interfaceTypes.size() > 1) {
        // interface types need to be sorted because they can show up in multiple implements clauses and you want the
        // set to be determinate.
        //
        // (Non-interface supertypes are already sorted from most-specific to least and will not contain primitive
        // types. By extension, array types will precede declared types (java.lang.Object). If t is a type variable,
        // then either its supertype will be another type variable or a declared type, so type variables precede
        // everything.)
        interfaceTypes.sort(this.c);
      }
      if (types.isEmpty()) {
        interfaceIndex = 0;
        types = interfaceTypes;
      } else {
        interfaceIndex = types.size();
        types.addAll(interfaceTypes);
      }
    }
    return new SupertypeList(types, interfaceIndex);
  }

  private final void supertypes(final TypeMirror t,
                                final Predicate<? super TypeMirror> p,
                                final ArrayList<? super TypeMirror> nonInterfaceTypes,
                                final ArrayList<? super TypeMirror> interfaceTypes,
                                final Set<? super String> seen) {
    if (seen.add(erasedName(t))) {
      if (p.test(t)) {
        if (isInterface(t)) {
          interfaceTypes.add(t); // reflexive
        } else {
          nonInterfaceTypes.add(t); // reflexive
        }
      }
      for (final TypeMirror directSupertype : this.domain.directSupertypes(t)) {
        this.supertypes(directSupertype, p, nonInterfaceTypes, interfaceTypes, seen); // recursive
      }
    }
  }


  /*
   * Static methods.
   */


  private static final String erasedName(final CharSequence cs) {
    return cs instanceof String s ? s : cs.toString();
  }

  private static final String erasedName(final Element e) {
    return e instanceof QualifiedNameable qn ? erasedName(qn) : erasedName(e.getSimpleName());
  }

  private static final String erasedName(final QualifiedNameable qn) {
    final CharSequence n = qn.getQualifiedName();
    return n == null || n.isEmpty() ? erasedName(qn.getSimpleName()) : erasedName(n);
  }

  /**
   * Returns the non-{@code null}, determinate <dfn>erased name</dfn> of the supplied {@link TypeMirror}.
   *
   * <p><dfn>Erased name</dfn> is not a term defined or used by the Java Language Specification. Its definition and
   * contract follow.</p>
   *
   * <p>This method is primarily for use in calculating tiebreaking values for type-related {@linkplain
   * Comparator#compare(Object, Object) comparison operations} where type erasure can be in effect. It is also useful in
   * testing scenarios. Most users will never call this method.</p>
   *
   * <p>The erased name of a {@linkplain TypeKind#DECLARED declared type} of any kind is the erased name of its
   * declaring {@link Element}.</p>
   *
   * <p>The erased name of an {@linkplain Element element} is its {@linkplain QualifiedNameable#getQualifiedName()
   * qualified name}, if it has one, or its {@linkplain Element#getSimpleName() simple name} if it does not.</p>
   *
   * <p>The erased name of the {@linkplain TypeKind#BOOLEAN <code>boolean</code> primitive type} is {@code boolean}.</p>
   *
   * <p>The erased name of the {@linkplain TypeKind#BYTE <code>byte</code> primitive type} is {@code byte}.</p>
   *
   * <p>The erased name of the {@linkplain TypeKind#CHAR <code>char</code> primitive type} is {@code char}.</p>
   *
   * <p>The erased name of the {@linkplain TypeKind#DOUBLE <code>double</code> primitive type} is {@code double}.</p>
   *
   * <p>The erased name of the {@linkplain TypeKind#FLOAT <code>float</code> primitive type} is {@code float}.</p>
   *
   * <p>The erased name of the {@linkplain TypeKind#INT <code>int</code> primitive type} is {@code int}.</p>
   *
   * <p>The erased name of the {@linkplain TypeKind#LONG <code>long</code> primitive type} is {@code long}.</p>
   *
   * <p>The erased name of the {@linkplain TypeKind#SHORT <code>short</code> primitive type} is {@code short}.</p>
   *
   * <p>The erased name of the {@linkplain TypeKind#VOID <code>void</code> type} is {@code void}.</p>
   *
   * <p>The erased name of a {@linkplain TypeKind#TYPEVAR type variable} is the erased name of its declaring {@link
   * javax.lang.model.element.TypeParameterElement TypeParameterElement}.</p>
   *
   * <p>The erased name of an {@linkplain TypeKind#INTERSECTION intersection type} is the {@link String} formed by
   * joining the erased names of its {@linkplain IntersectionType#getBounds() bounds} with "{@code &}".</p>
   *
   * <p>The erased name of an {@linkplain TypeKind#ARRAY array type} is the {@link String} formed by concatenating the
   * erased name of its {@linkplain ArrayType#getComponentType() component type} with "{@code []}".</p>
   *
   * <p>The erased name of any other {@link TypeMirror} is the return value of an invocation of its {@link
   * Object#toString() toString()} method.</p>
   *
   * @param t a non-{@code null} {@link TypeMirror}
   *
   * @return the non-{@code null}, determinate <dfn>erased name</dfn> of the supplied {@link TypeMirror}
   *
   * @exception NullPointerException if {@code t} is {@code null}
   */
  public static final String erasedName(final TypeMirror t) {
    return switch (t.getKind()) {
    case ARRAY -> erasedName(((ArrayType)t).getComponentType()) + "[]";
    case BOOLEAN -> "boolean";
    case BYTE -> "byte";
    case CHAR -> "char";
    case DECLARED -> erasedName(((DeclaredType)t).asElement());
    case DOUBLE -> "double";
    case FLOAT -> "float";
    case INT -> "int";
    case INTERSECTION -> {
      final StringJoiner sj = new java.util.StringJoiner("&");
      for (final TypeMirror bound : ((IntersectionType)t).getBounds()) {
        sj.add(erasedName(bound));
      }
      yield sj.toString();
    }
    case LONG -> "long";
    case SHORT -> "short";
    case TYPEVAR -> erasedName(((TypeVariable)t).asElement());
    case VOID -> "void";
    default -> t.toString();
    };
  }

  private static final <X> boolean returnTrue(final X ignored) {
    return true;
  }

}
