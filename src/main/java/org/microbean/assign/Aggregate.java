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

import java.util.Collection;
import java.util.SequencedSet;

import javax.lang.model.element.Element;

import javax.lang.model.AnnotatedConstruct;

import java.util.function.Function;

import static java.util.Collections.unmodifiableSequencedSet;

import static java.util.LinkedHashSet.newLinkedHashSet;

/**
 * An object with {@linkplain #dependencies() dependencies}.
 *
 * <p>By default, {@link Aggregate}s have {@linkplain #EMPTY_DEPENDENCIES no dependencies}.</p>
 *
 * @author <a href="https://about.me/lairdnelson/" target="_top">Laird Nelson</a>
 *
 * @see #dependencies()
 */
public interface Aggregate {


  /*
   * Static fields.
   */


  /**
   * An immutable, empty {@link SequencedSet} of {@link Assignment}s.
   */
  public static final SequencedSet<Assignment<?>> EMPTY_ASSIGNMENTS = unmodifiableSequencedSet(newLinkedHashSet(0));

  /**
   * An immutable, empty {@link SequencedSet} of {@link Element}s.
   */
  public static final SequencedSet<Annotated<Element>> EMPTY_DEPENDENCIES = unmodifiableSequencedSet(newLinkedHashSet(0));


  /*
   * Default instance methods.
   */


  /**
   * Returns an immutable, determinate, {@link SequencedSet} of {@link Annotated Annotated&lt;? extends Element&gt;}
   * instances.
   *
   * <p>If an {@link Annotated Annotated&lt;? extends Element&gt;} in the set represents this very {@link Aggregate}
   * implementation, undefined behavior, including the possibility of infinite loops, may result (an {@link Aggregate}
   * may not have itself as a dependency).</p>
   *
   * <p>Note that it is permissible for an {@link Annotated Annotated&lt;? extends Element&gt;} in the set to represent
   * another type.</p>
   *
   * <p>The default implementation of this method returns the value of the {@link #EMPTY_DEPENDENCIES} field.</p>
   *
   * @return an immutable, determinate, {@link SequencedSet} of {@link Annotated Annotated&lt;? extends Element&gt;}
   * instances; never {@code null}
   *
   * @see Annotated
   *
   * @see Annotated#of(AnnotatedConstruct)
   */
  public default SequencedSet<? extends Annotated<? extends Element>> dependencies() {
    return EMPTY_DEPENDENCIES;
  }

  /**
   * A convenience method that assigns a contextual reference to each of this {@link Aggregate}'s {@link Annotated
   * Annotated&lt;? extends Element&gt;}-typed {@linkplain #dependencies() dependencies} and returns the resulting
   * {@link SequencedSet} of {@link Assignment}s.
   *
   * <p><strong>Note:</strong> Undefined behavior may result if an {@link Annotated Annotated&lt;? extends Element&gt;}
   * in the {@linkplain #dependencies() dependencies} represents this {@link Aggregate} implementation (an {@link
   * Aggregate} may not have itself as a dependency).</p>
   *
   * <p>Typically there is no need to override this method.</p>
   *
   * <p>Usage of this method is not required.</p>
   *
   * @param r a {@link Function} that retrieves a contextual reference suitable for an {@link Annotated Annotated&lt;?
   * extends AnnotatedConstruct&gt;}; if {@link #dependencies()} returns a non-empty {@link SequencedSet} then this
   * argument must not be {@code null}
   *
   * @return an immutable {@link SequencedSet} of {@link Assignment} instances; never {@code null}
   *
   * @exception NullPointerException if {@code r} is {@code null}
   */
  // (Convenience.)
  public default SequencedSet<? extends Assignment<?>> assign(final Function<? super Annotated<? extends AnnotatedConstruct>, ?> r) {
    final Collection<? extends Annotated<? extends Element>> ds = this.dependencies();
    if (ds == null || ds.isEmpty()) {
      return EMPTY_ASSIGNMENTS;
    }
    final SequencedSet<Assignment<?>> assignments = newLinkedHashSet(ds.size());
    ds.forEach(d -> assignments.add(new Assignment<>(d, r.apply(d))));
    return unmodifiableSequencedSet(assignments);
  }

}
