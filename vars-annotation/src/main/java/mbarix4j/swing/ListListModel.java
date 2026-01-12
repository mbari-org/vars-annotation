/*
 * Copyright 2005 MBARI
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1 
 * (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/copyleft/lesser.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package mbarix4j.swing;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;
import javax.swing.AbstractListModel;

//~--- classes ----------------------------------------------------------------

/**
 * <p>Allows a JList to use any <code>java.util.List</code> as its data source.
 * I don't understand why swing doesn't implement it this way in the first place
 * by default. The default <code>ListModel</code> uses a vector for storage.<p>
 *
 * <p>I know, I know..it's a funny name. </p>
 *
 * <p>This was implented so that an <code>AssociationList</code> (cf. The VARS
 * project) could be displayed in a JTable.</p>
 *
 * <h2>Useage</h2>
 * <pre>
 * import mbarix4j.swing.ListListModel;
 * import mbarix4j.util.SortedArrayList;
 * import javax.swing.JList;
 *
 * // myList could be any class that implements the List interface. Here
 * // we use a list that sorts it's contents.
 * List myList = Collections.synchronizedList(new SortedArrayList());
 * ListListModel model = new ListListModel(myList);
 *
 * // JList is a swing component that allows users to select one or more
 * // items from a list.
 * JList jList = new JList();
 * jList.setModel(model);
 * </pre>
 *
 * @author <a href="mailto:brian@mbari.org">Brian Schlining</a>
 * @version $Id: ListListModel.java 332 2006-08-01 18:38:46Z hohonuuli $
 * @see javax.swing.ListModel
 * @see javax.swing.AbstractListModel
 */
public class ListListModel extends AbstractListModel
        implements List, Serializable, MutableListModel {

    /**
     * 
     */
    private static final long serialVersionUID = -5606745348920993367L;
    /**
	 * Internal representation of data. Any object that implements the <code>java.util.List</code> interface can be used.
	 * @uml.property  name="delegate"
	 */
    protected List delegate;

    //~--- constructors -------------------------------------------------------

    /**
     * Convienice constructor. Allows the creation of a list model from a preexisting
     *  <code>List</code> object.
     * @param list user supplied <code>List</code> to build a <code>ListModel</code>
     *  around. Note the List should be synchronized. FOr example, <code> List list =
     * Collections.synchronizedList(new ArrayList());
     */
    public ListListModel(List list) {
        this.delegate = list;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Appends the specified element to the end of this list (optional operation). <p>
     * Lists that support this operation may place limitations on what
     * elements may be added to this list.  In particular, some lists will refuse to add null elements, and others will impose
     * restrictions on the type of elements that may be added.  List
     * classes should clearly specify in their documentation any restrictions on what elements may be added.
     *
     * @param obj
     * @return <tt>true</tt> (as per the general contract of the <tt>Collection.add</tt> method).
     */
    public boolean add(Object obj) {
        int index = delegate.size();
        boolean rv = delegate.add(obj);
        fireIntervalAdded(this, index, index);
        return rv;
    }

    /**
     * Inserts the specified element at the specified position in this list. <p>
     * Throws an <tt>ArrayIndexOutOfBoundsException</tt> if the index is out of range (index &lt; 0 || index &gt;= size()).
     * @param index index at which the specified element is to be inserted.
     * @param element element to be inserted.
     */
    public void add(int index, Object element) {
        delegate.add(index, element);
        fireIntervalAdded(this, index, index);
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator (optional operation).  The behavior of this
     * operation is unspecified if the specified collection is modified while
     * the operation is in progress.  (Note that this will occur if the specified collection is this list, and it's nonempty.)
     * @param c collection whose elements are to be added to this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @see #add(Object)
     */
    public boolean addAll(Collection c) {
        int delegateSize = delegate.size();
        boolean changed = delegate.addAll(c);
        if (changed) {
            int newSize = delegateSize + c.size() - 1;
            fireIntervalAdded(this, delegateSize, newSize);
        }

        return changed;
    }

    /**
     * Inserts all of the elements in the specified collection into this
     * list at the specified position (optional operation).  Shifts the
     * element currently at that position (if any) and any subsequent
     * elements to the right (increases their indices).  The new elements
     * will appear in this list in the order that they are returned by the
     * specified collection's iterator.  The behavior of this operation is
     * unspecified if the specified collection is modified while the
     * operation is in progress.  (Note that this will occur if the specified collection is this list, and it's nonempty.)
     * @param index index at which to insert first element from the specified collection.
     * @param c elements to be inserted into this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     */
    public boolean addAll(int index, Collection c) {
        return delegate.addAll(index, c);
    }

    /**
     * Removes all of the elements from this list.  The list will
     * be empty after this call returns (unless it throws an exception).
     */
    public void clear() {
        int index1 = delegate.size() - 1;
        delegate.clear();

        if (index1 >= 0) {
            fireIntervalRemoved(this, 0, index1);
        }
    }

    /**
     * Tests if the specified object is a component in this list.
     * @param   elem   an object.
     * @return  <code>true</code> if the specified object is the same as a component in this list
     * @see Vector#contains(Object)
     */
    public boolean contains(Object elem) {
        return delegate.contains(elem);
    }

    /**
     * Returns <tt>true</tt> if this list contains all of the elements of the specified collection.
     * @param c collection to be checked for containment in this list.
     * @return <tt>true</tt> if this list contains all of the elements of the specified collection.
     * @see #contains(Object)
     */
    public boolean containsAll(Collection c) {
        return delegate.containsAll(c);
    }

    /**
     * Returns the component at the specified index. Throws an <tt>ArrayIndexOutOfBoundsException</tt> if the index
     * is negative or not less than the size of the list. <blockquote>
     * <b>Note:</b> Although this method is not deprecated, the preferred
     * method to use is <tt>get(int)</tt>, which implements the
     * <tt>List</tt> interface defined in the 1.2 Collections framework. </blockquote>
     * @param      index   an index into this list.
     * @return     the component at the specified index.
     * @see #get(int)
     * @see Vector#elementAt(int)
     */
    public Object elementAt(int index) {
        return delegate.get(index);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Returns the element at the specified position in this list. <p>
     * Throws an <tt>ArrayIndexOutOfBoundsException</tt> if the index is out of range (index &lt; 0 || index &gt;= size()).
     * @param index index of element to return.
     *
     * @return
     */
    public Object get(int index) {
        return delegate.get(index);
    }

    /**
     * Returns the component at the specified index. <blockquote>
     * <b>Note:</b> Although this method is not deprecated, the preferred
     * method to use is <tt>get(int)</tt>, which implements the
     * <tt>List</tt> interface defined in the 1.2 Collections framework. </blockquote>
     * @param      index   an index into this list.
     * @return     the component at the specified index.
     * @see #get(int)
     */
    public Object getElementAt(int index) {
        return delegate.get(index);
    }

    /**
     * Accessor method to obtain a reference to the <code>List</code> that is
     * used internally to store information. Use at your own risk. Modifications
     * to the <code>List</code> that are not made through the <code>ListListModel</code> object will not fire events that signal
     * modification of the data contained in the <code>List</code>.
     * @return
     */
    public List getList() {
        return delegate;
    }

    /**
     * Returns the number of components in this list. <p> This method is identical to <tt>size()</tt>, which implements the
     * <tt>List</tt> interface defined in the 1.2 Collections framework.
     * This method exists in conjunction with <tt>setSize()</tt> so that "size" is identifiable as a JavaBean property.
     * @return  the number of components in this list.
     * @see #size()
     */
    public int getSize() {
        try {
            return delegate.size();
        } catch (NullPointerException npe) {
            System.out.println("Error: Can't get valid list size");
            return 0;
        }
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Searches for the first occurence of the given argument.
     * @param   elem   an object.
     * @return  the index of the first occurrence of the argument in this
     * list; returns <code>-1</code> if the object is not found.
     * @see Vector#indexOf(Object)
     */
    public int indexOf(Object elem) {
        return delegate.indexOf(elem);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Tests if this list has no components.
     * @return  <code>true</code> if and only if this list has no components, that is, its size is zero;
     * <code>false</code> otherwise.
     * @see Vector#isEmpty()
     */
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * @return an iterator over the elements in this list in proper sequence.
     */
    public Iterator iterator() {
        return delegate.iterator();
    }

    /**
     * Returns the index of the last occurrence of the specified object in this list.
     * @param   elem   the desired component.
     * @return  the index of the last occurrence of the specified object in
     * this list; returns <code>-1</code> if the object is not found.
     * @see Vector#lastIndexOf(Object)
     */
    public int lastIndexOf(Object elem) {
        return delegate.lastIndexOf(elem);
    }

    /**
     * Returns a list iterator of the elements in this list (in proper sequence).
     * @return a list iterator of the elements in this list (in proper sequence).
     */
    public ListIterator listIterator() {
        return delegate.listIterator();
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list.  The
     * specified index indicates the first element that would be returned by
     * an initial call to the <tt>next</tt> method.  An initial call to
     * the <tt>previous</tt> method would return the element with the specified index minus one.
     * @param index index of first element to be returned from the list iterator (by a call to the <tt>next</tt> method).
     * @return a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list.
     */
    public ListIterator listIterator(int index) {
        return delegate.listIterator(index);
    }

    /**
     * Lets the views of the model know that they need to refresh. This is useful
     * if an List gets updated using its methods rather than through the ListModel
     * methods.
     *
     */
    public void refreshView() {
        fireContentsChanged(this, 0, size() - 1);
    }

    /**
     * Deletes the component at the specified index. <p> Throws an <tt>ArrayIndexOutOfBoundsException</tt> if the index
     * is invalid. <blockquote> <b>Note:</b> Although this method is not deprecated, the preferred
     * method to use is <tt>remove(int)</tt>, which implements the
     * <tt>List</tt> interface defined in the 1.2 Collections framework. </blockquote>
     * @param      index   the index of the object to remove.
     * @see #remove(int)
     * @see Vector#removeElementAt(int)
     *
     * @return
     */
    public Object remove(int index) {
        Object obj = delegate.remove(index);
        fireIntervalRemoved(this, index, index);
        return obj;
    }

    /**
     * Removes the first (lowest-indexed) occurrence of the argument from this list.
     * @param   obj   the component to be removed.
     * @return  <code>true</code> if the argument was a component of this list; <code>false</code> otherwise.
     * @see Vector#removeElement(Object)
     */
    public boolean remove(Object obj) {
        int index = delegate.indexOf(obj);
        boolean rv = delegate.remove(obj);
        fireIntervalRemoved(this, index, index);
        return rv;
    }

    /**
     * Removes from this list all the elements that are contained in the specified collection (optional operation).
     * @param c collection that defines which elements will be removed from this list.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean removeAll(Collection c) {
        return delegate.removeAll(c);
    }

    /**
     * Deletes the components at the specified range of indexes. The removal is inclusive, so specifying a range of (1,5)
     * removes the component at index 1 and the component at index 5, as well as all components in between. <p>
     * Throws an <tt>ArrayIndexOutOfBoundsException</tt> if the index was invalid.
     * Throws an <tt>IllegalArgumentException</tt> if <tt>fromIndex</tt> &gt; <tt>toIndex</tt>.
     * @param      fromIndex the index of the lower end of the range
     * @param      toIndex   the index of the upper end of the range
     * @see        #remove(int)
     */
    public void removeRange(int fromIndex, int toIndex) {
        for (int i = toIndex; i >= fromIndex; i--) {
            delegate.remove(i);
        }

        fireIntervalRemoved(this, fromIndex, toIndex);
    }

    /**
     * Retains only the elements in this list that are contained in the
     * specified collection (optional operation).  In other words, removes
     * from this list all the elements that are not contained in the specified collection.
     * @param c collection that defines which elements this set will retain.
     * @return <tt>true</tt> if this list changed as a result of the call.
     * @see #remove(Object)
     * @see #contains(Object)
     */
    public boolean retainAll(Collection c) {
        return delegate.retainAll(c);
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Replaces the element at the specified position in this list with the specified element. <p>
     * Throws an <tt>ArrayIndexOutOfBoundsException</tt> if the index is out of range (index &lt; 0 || index &gt;= size()).
     * @param index index of element to replace.
     * @param element element to be stored at the specified position.
     * @return the element previously at the specified position.
     */
    public Object set(int index, Object element) {
        Object rv = delegate.get(index);
        delegate.set(index, element);
        fireContentsChanged(this, index, index);
        return rv;
    }

    /**
     * Sets the component at the specified <code>index</code> of this
     * list to be the specified object. The previous component at that position is discarded. <p>
     * Throws an <tt>ArrayIndexOutOfBoundsException</tt> if the index is invalid. <blockquote>
     * <b>Note:</b> Although this method is not deprecated, the preferred
     * method to use is <tt>set(int,Object)</tt>, which implements the
     * <tt>List</tt> interface defined in the 1.2 Collections framework. </blockquote>
     * @param      obj     what the component is to be set to.
     * @param      index   the specified index.
     * @see #set(int,Object)
     * @see List#set(int, Object)
     */
    public void setElementAt(int index, Object obj) {
        delegate.set(index, obj);
        fireContentsChanged(this, index, index);
    }

    /**
     * Sets the <code>java.util.List</code> object used by the <code>ListListModel</code>. Setting the list will fire the
     * required events to update the UI.
     * @param list An object that implements the <code>java.util.List</code> interface
     */
    public void setList(List list) {
        int indexOld = delegate.size() - 1;
        int indexNew = list.size() - 1;
        this.delegate = list;
        fireIntervalRemoved(this, 0, indexOld);
        fireIntervalAdded(this, 0, indexNew);
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Returns the number of components in this list.
     * @return  the number of components in this list.
     * @see Vector#size()
     */
    public int size() {
        return delegate.size();
    }

    /**
     * Returns a view of the portion of this list between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.  (If
     * <tt>fromIndex</tt> and <tt>toIndex</tt> are equal, the returned list is
     * empty.)  The returned list is backed by this list, so changes in the
     * returned list are reflected in this list, and vice-versa.  The returned
     * list supports all of the optional list operations supported by this list.<p>
     * This method eliminates the need for explicit range operations (of
     * the sort that commonly exist for arrays).   Any operation that expects
     * a list can be used as a range operation by passing a subList view
     * instead of a whole list.  For example, the following idiom removes a range of elements from a list: <pre>
     *      list.subList(from, to).clear();
     * </pre> Similar idioms may be constructed for <tt>indexOf</tt> and
     * <tt>lastIndexOf</tt>, and all of the algorithms in the <tt>Collections</tt> class can be applied to a subList.<p>
     * The semantics of this list returned by this method become undefined if
     * the backing list (i.e., this list) is <i>structurally modified</i> in
     * any way other than via the returned list.  (Structural modifications are
     * those that change the size of this list, or otherwise perturb it in such
     * a fashion that iterations in progress may yield incorrect results.)
     *
     * @param i
     * @param j
     * @return a view of the specified range within this list.
     */
    public List subList(int i, int j) {
        return delegate.subList(i, j);
    }

    /*
     *  The remaining methods are included for compatibility with the
     * Java 2 platform Vector class.
     */

    /**
     * Returns an array containing all of the elements in this list in the correct order. <p>
     * Throws an <tt>ArrayStoreException</tt> if the runtime type of the array
     * a is not a supertype of the runtime type of every element in this list.
     * @return an array containing the elements of the list.
     * @see Vector#toArray()
     */
    public Object[] toArray() {
        return delegate.toArray();
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence; the runtime type of the returned array is that of the specified array.  Obeys the general contract of the
     * <tt>Collection.toArray(Object[])</tt> method.
     * @param a the array into which the elements of this list are to
     * be stored, if it is big enough; otherwise, a new array of the same runtime type is allocated for this purpose.
     * @return  an array containing the elements of this list.
     */
    public Object[] toArray(Object[] a) {
        return delegate.toArray(a);
    }

    /**
     * Returns a string that displays and identifies this object's properties.
     * @return a String representation of this object
     */
    public String toString() {
        return delegate.toString();
    }

    public boolean isCellEditable(int index) {
        return true;
    }

    public void setValueAt(Object value, int index) {
        delegate.add(index, value);
    }
}
