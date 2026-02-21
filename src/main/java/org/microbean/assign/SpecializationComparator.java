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

import java.util.Comparator;

import javax.lang.model.type.TypeMirror;

import org.microbean.construct.Domain;

import static java.util.Objects.requireNonNull;

/**
 * A {@link Comparator} of {@link TypeMirror}s that establishes a <dfn>partial order</dfn> on its arguments, enforcing
 * only that more specialized types precede less specialized ones.
 *
 * <strong>Instances of this class are (deliberately) not consistent with equals.</strong>
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 *
 * @see #compare(TypeMirror, TypeMirror)
 */
public final class SpecializationComparator implements Comparator<TypeMirror> {

  private final Domain domain;

  /**
   * Creates a new {@link SpecializationComparator}.
   *
   * @param domain a non-{@code null} {@link Domain}
   *
   * @exception NullPointerException if {@code domain} is {@code null}
   */
  public SpecializationComparator(final Domain domain) {
    super();
    this.domain = requireNonNull(domain, "domain");
  }

  /**
   * Compares two {@link TypeMirror}s and returns the result.
   *
   * <p>The following rules are evaluated in order to calculate the returned result:</p>
   *
   * <p>If {@code t} {@code ==} {@code s}, or {@linkplain Domain#sameType(TypeMirror, TypeMirror) <code>t</code> and
   * <code>s</code> are the same time}, returns {@code 0}.</p>
   *
   * <p>If {@code t} is {@code null} and {@code s} is not, returns a positive value.</p>
   *
   * <p>If {@code s} is {@code null} and {@code t} is not, returns a negative value.</p>
   *
   * <p>If {@code t} {@linkplain Domain#subtype(TypeMirror, TypeMirror) is a subtype of} {@code s}, returns a negative
   * value.</p>
   *
   * <p>If {@code s} {@linkplain Domain#subtype(TypeMirror, TypeMirror) is a subtype of} {@code t}, returns a positive
   * value.</p>
   *
   * <p>In all other cases {@code 0} is returned.</p>.
   *
   * @param t a {@link TypeMirror}; may be {@code null}
   *
   * @param s a {@link TypeMirror}; may be {@code null}
   *
   * @return a comparison result
   *
   * @see Domain#sameType(TypeMirror, TypeMirror)
   *
   * @see Domain#subtype(TypeMirror, TypeMirror)
   */
  @Override // Comparator<TypeMirror, TypeMirror>
  public final int compare(final TypeMirror t, final TypeMirror s) {
    if (t == s) {
      return 0;
    } else if (t == null) {
      return 1; // nulls right
    } else if (s == null) {
      return -1; // nulls right
    } else if (this.domain.sameType(t, s)) {
      return 0;
    } else if (this.domain.subtype(t, s)) {
      // t is a subtype of s; s is a proper supertype of t
      return -1;
    } else if (this.domain.subtype(s, t)) {
      // s is a subtype of t; t is a proper supertype of s
      return 1;
    } else {
      return 0;
    }
  }

}
