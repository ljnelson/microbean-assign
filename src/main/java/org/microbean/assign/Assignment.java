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

import javax.lang.model.element.Element;

import static java.util.Objects.requireNonNull;

/**
 * An assignment of a value to an {@link Annotated Annotated&lt;? extends Element&gt;}.
 *
 * @param <R> the value type; must be logically assignable to the {@linkplain Element#asType() type of the assignee}
 *
 * @param assignee the {@link Annotated Annotated&lt;? extends Element&gt;}; must not be {@code null}
 *
 * @param value the value; may be {@code null}
 *
 * @author <a href="https://about.me/lairdnelson" target="_top">Laird Nelson</a>
 */
// You're going to be tempted to replace the value component with a Supplier component. Don't do it. An assignment is a
// value that belongs to, e.g., a field, so even if the value "came from" none/dependent/prototype scope, it was already
// sourced and "belongs to" the field.
public final record Assignment<R>(Annotated<? extends Element> assignee, R value) {

  /**
   * Creates a new {@link Assignment}.
   *
   * @param assignee the {@link Annotated Annotated&lt;? extends Element&gt;}; must not be {@code null}
   *
   * @param value the contextual reference; may be {@code null}
   */
  public Assignment {
    requireNonNull(assignee, "assignee");
  }

}
