package com.base.orm;

/** Interface to describe the behavior used to create new entities by the
 * CustomTuplizer.
 *
 * @param <T> the type of the entity to be created
 */
public interface EntityFactory<T> {

  /** Creates a new instance of type T
   *
   * @return a T instance, never null.
   */
  T create();
}
