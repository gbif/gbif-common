/***************************************************************************
 * Copyright 2014 Global Biodiversity Information Facility Secretariat
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ***************************************************************************/

package org.gbif.utils.collection;

import java.lang.reflect.Array;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A HashSet implementation taken from Ontopia
 * which is both faster and more compact than java.util.HashSet
 * <p/>
 * INTERNAL: Implements the Set interface more compactly than
 * java.util.HashSet by using a closed hashtable.
 *
 * @see <a href="http://ontopia.wordpress.com/2009/09/23/a-faster-and-more-compact-set/">Ontopia blog</a>
 * @see <a href="http://code.google.com/p/ontopia/source/browse/trunk/ontopia/src/java/net/ontopia/utils/">Ontopia
 *      source</a>
 */
public class CompactHashSet<T> extends AbstractSet<T> {

  private class CompactHashIterator implements Iterator<T> {

    private int index;
    private int lastReturned = -1;

    /**
     * The modCount value that the iterator believes that the backing
     * CompactHashSet should have. If this expectation is violated,
     * the iterator has detected concurrent modification.
     */
    private int expectedModCount;

    CompactHashIterator() {
      for (index = 0; index < objects.length && (objects[index] == null || objects[index] == deletedObject); index++) {
      }
      expectedModCount = modCount;
    }

    @Override
    public boolean hasNext() {
      return index < objects.length;
    }

    @Override
    public T next() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      int length = objects.length;
      if (index >= length) {
        lastReturned = -2;
        throw new NoSuchElementException();
      }

      lastReturned = index;
      for (index += 1; index < length && (objects[index] == null || objects[index] == deletedObject); index++) {
      }
      if (objects[lastReturned] == nullObject) {
        return null;
      } else {
        return (T) objects[lastReturned];
      }
    }

    @Override
    public void remove() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
      if (lastReturned == -1 || lastReturned == -2) {
        throw new IllegalStateException();
      }
      // delete object
      if (objects[lastReturned] != null && objects[lastReturned] != deletedObject) {
        objects[lastReturned] = deletedObject;
        elements--;
        modCount++;
        expectedModCount = modCount; // this is expected; we made the change
      }
    }
  }

  protected static final int INITIAL_SIZE = 3;

  protected static final double LOAD_FACTOR = 0.75;
  /**
   * This object is used to represent null, should clients add that to the set.
   */
  protected static final Object nullObject = new Object();
  /**
   * When an object is deleted this object is put into the hashtable
   * in its place, so that other objects with the same key
   * (collisions) further down the hashtable are not lost after we
   * delete an object in the collision chain.
   */
  protected static final Object deletedObject = new Object();
  protected int elements;
  protected int freecells;
  protected Object[] objects;

  protected int modCount;

  /**
   * Constructs a new, empty set.
   */
  public CompactHashSet() {
    objects = new Object[INITIAL_SIZE];
    elements = 0;
    freecells = objects.length;
    modCount = 0;
  }

  /**
   * Constructs a new set containing the elements in the specified
   * collection.
   *
   * @param c the collection whose elements are to be placed into this set.
   */
  public CompactHashSet(Collection c) {
    this(c.size());
    addAll(c);
  }

  // ===== SET IMPLEMENTATION =============================================

  /**
   * Constructs a new, empty set.
   */
  public CompactHashSet(int size) {
    // NOTE: If array size is 0, we get a
    // "java.lang.ArithmeticException: / by zero" in add(Object).
    objects = new Object[size == 0 ? 1 : size];
    elements = 0;
    freecells = objects.length;
    modCount = 0;
  }

  /**
   * Adds the specified element to this set if it is not already
   * present.
   *
   * @param x element to be added to this set.
   *
   * @return <tt>true</tt> if the set did not already contain the specified
   *         element.
   */
  @Override
  public boolean add(T x) {
    Object o = x;
    if (o == null) {
      o = nullObject;
    }

    int hash = o.hashCode();
    int index = (hash & 0x7FFFFFFF) % objects.length;
    int offset = 1;
    int deletedix = -1;

    // search for the object (continue while !null and !this object)
    while (objects[index] != null && !(objects[index].hashCode() == hash && objects[index].equals(o))) {

      // if there's a deleted object here we can put this object here,
      // provided it's not in here somewhere else already
      if (objects[index] == deletedObject) {
        deletedix = index;
      }

      index = ((index + offset) & 0x7FFFFFFF) % objects.length;
      offset = offset * 2 + 1;

      if (offset == -1) {
        offset = 2;
      }
    }

    if (objects[index] == null) { // wasn't present already
      if (deletedix != -1) {
        index = deletedix;
      } else {
        freecells--;
      }

      modCount++;
      elements++;
      objects[index] = o;

      // rehash with same capacity
      if (1 - (freecells / (double) objects.length) > LOAD_FACTOR) {
        rehash(objects.length);
        // rehash with increased capacity
        if (1 - (freecells / (double) objects.length) > LOAD_FACTOR) {
          rehash(objects.length * 2 + 1);
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Removes all of the elements from this set.
   */
  @Override
  public void clear() {
    elements = 0;
    for (int ix = 0; ix < objects.length; ix++) {
      objects[ix] = null;
    }
    freecells = objects.length;
    modCount++;
  }

  /**
   * Returns <tt>true</tt> if this set contains the specified element.
   *
   * @param o element whose presence in this set is to be tested.
   *
   * @return <tt>true</tt> if this set contains the specified element.
   */
  @Override
  public boolean contains(Object o) {
    if (o == null) {
      o = nullObject;
    }

    int hash = o.hashCode();
    int index = (hash & 0x7FFFFFFF) % objects.length;
    int offset = 1;

    // search for the object (continue while !null and !this object)
    while (objects[index] != null && !(objects[index].hashCode() == hash && objects[index].equals(o))) {
      index = ((index + offset) & 0x7FFFFFFF) % objects.length;
      offset = offset * 2 + 1;

      if (offset == -1) {
        offset = 2;
      }
    }

    return objects[index] != null;
  }

  /**
   * INTERNAL: Used for debugging only.
   */
  public void dump() {
    System.out.println("Size: " + objects.length);
    System.out.println("Elements: " + elements);
    System.out.println("Free cells: " + freecells);
    System.out.println();
    for (int ix = 0; ix < objects.length; ix++) {
      System.out.println("[" + ix + "]: " + objects[ix]);
    }
  }

  /**
   * Returns <tt>true</tt> if this set contains no elements.
   */
  @Override
  public boolean isEmpty() {
    return elements == 0;
  }

  /**
   * Returns an iterator over the elements in this set. The elements
   * are returned in no particular order.
   *
   * @return an Iterator over the elements in this set.
   *
   * @see ConcurrentModificationException
   */
  @Override
  public Iterator iterator() {
    return new CompactHashIterator();
  }

  /**
   * INTERNAL: Rehashes the hashset to a bigger size.
   */
  protected void rehash(int newCapacity) {
    int oldCapacity = objects.length;
    Object[] newObjects = new Object[newCapacity];

    for (int ix = 0; ix < oldCapacity; ix++) {
      Object o = objects[ix];
      if (o == null || o == deletedObject) {
        continue;
      }

      int hash = o.hashCode();
      int index = (hash & 0x7FFFFFFF) % newCapacity;
      int offset = 1;

      // search for the object
      while (newObjects[index] != null) { // no need to test for duplicates
        index = ((index + offset) & 0x7FFFFFFF) % newCapacity;
        offset = offset * 2 + 1;

        if (offset == -1) {
          offset = 2;
        }
      }

      newObjects[index] = o;
    }

    objects = newObjects;
    freecells = objects.length - elements;
  }

  /**
   * Removes the specified element from the set.
   */
  @Override
  public boolean remove(Object o) {
    if (o == null) {
      o = nullObject;
    }

    int hash = o.hashCode();
    int index = (hash & 0x7FFFFFFF) % objects.length;
    int offset = 1;

    // search for the object (continue while !null and !this object)
    while (objects[index] != null && !(objects[index].hashCode() == hash && objects[index].equals(o))) {
      index = ((index + offset) & 0x7FFFFFFF) % objects.length;
      offset = offset * 2 + 1;

      if (offset == -1) {
        offset = 2;
      }
    }

    // we found the right position, now do the removal
    if (objects[index] != null) {
      // we found the object
      objects[index] = deletedObject;
      modCount++;
      elements--;
      return true;
    } else {
      // we did not find the object
      return false;
    }
  }

  // ===== INTERNAL METHODS ===============================================

  /**
   * Returns the number of elements in this set (its cardinality).
   */
  @Override
  public int size() {
    return elements;
  }

  @Override
  public Object[] toArray() {
    Object[] result = new Object[elements];
    Object[] objects = this.objects;
    int pos = 0;
    for (Object object : objects) {
      if (object != null && object != deletedObject) {
        result[pos++] = object == nullObject ? null : object;
      }
    }
    return result;
  }

  // ===== ITERATOR IMPLEMENTATON =========================================

  @Override
  public Object[] toArray(Object[] a) {
    int size = elements;
    if (a.length < size) {
      a = (Object[]) Array.newInstance(a.getClass().getComponentType(), size);
    }
    Object[] objects = this.objects;
    int pos = 0;
    for (Object object : objects) {
      if (object != null && object != deletedObject) {
        a[pos++] = object == nullObject ? null : object;
      }
    }
    return a;
  }

}
