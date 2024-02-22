package org.gnucash.api.write.impl.hlp;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import org.gnucash.api.Const;
import org.gnucash.api.generated.ObjectFactory;
import org.gnucash.api.generated.Slot;
import org.gnucash.api.generated.SlotValue;
import org.gnucash.api.read.hlp.GnucashObject;
import org.gnucash.api.read.impl.hlp.GnucashObjectImpl;
import org.gnucash.api.write.GnucashWritableFile;
import org.gnucash.api.write.hlp.GnucashWritableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of GnucashObjectImpl to allow read-write access instead of
 * read-only access.
 */
public class GnucashWritableObjectImpl // extends GnucashObjectImpl <-- NO, WE DO NOT EXTEND
		                               implements GnucashWritableObject 
{

	/**
	 * Automatically created logger for debug and error-output.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(GnucashWritableObjectImpl.class);

	// ---------------------------------------------------------------

	private GnucashObjectImpl gcshObj;

	/**
	 * support for firing PropertyChangeEvents. (gets initialized only if we really
	 * have listeners)
	 */
	private volatile PropertyChangeSupport myPtyChg = null;

	// ---------------------------------------------------------------

	public GnucashWritableObjectImpl() {
		super();
		// TODO implement constructor for GnucashWritableObjectHelper
	}

	/**
	 * @param obj the object we are helping with
	 */
	public GnucashWritableObjectImpl(final GnucashObjectImpl obj) {
		super();
		setGnucashObject(obj);
	}

	// ---------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public GnucashWritableFile getWritableGnucashFile() {
		return ((GnucashWritableObject) getGnucashObject()).getWritableGnucashFile();
	}

	/**
	 * {@inheritDoc}
	 */
	public GnucashWritableFile getFile() {
		return (GnucashWritableFile) getGnucashObject().getGnucashFile();
		// return ((GnucashWritableObject) getGnucashObject()).getWritableGnucashFile();
	}

	/**
	 * Remove slots with dummy content
	 */
	public void cleanSlots() {
		if ( gcshObj.getSlots() == null )
			return;

		for ( Slot slot : gcshObj.getSlots().getSlot() ) {
			if ( slot.getSlotKey().equals(Const.SLOT_KEY_DUMMY) ) {
				gcshObj.getSlots().getSlot().remove(slot);
				break;
			}
		}
	}

	// ---------------------------------------------------------------

	/**
	 * @param name  the name of the user-defined attribute
	 * @param value the value or null if not set
	 * @see {@link GnucashObject#getUserDefinedAttribute(String)}
	 */
	public void setUserDefinedAttribute(final String name, final String value) {
		List<Slot> slots = getGnucashObject().getSlots().getSlot();
		for ( Slot slot : slots ) {
			if ( slot.getSlotKey().equals(name) ) {
				LOGGER.debug("setUserDefinedAttribute: (name=" + name + ", value=" + value
						+ ") - overwriting existing slot ");

				slot.getSlotValue().getContent().clear();
				slot.getSlotValue().getContent().add(value);
				getFile().setModified(true);
				return;
			}
		}

		ObjectFactory objectFactory = new ObjectFactory();
		Slot newSlot = objectFactory.createSlot();
		newSlot.setSlotKey(name);
		SlotValue newValue = objectFactory.createSlotValue();
		newValue.setType(Const.XML_DATA_TYPE_STRING);
		newValue.getContent().add(value);
		newSlot.setSlotValue(newValue);
		LOGGER.debug("setUserDefinedAttribute: (name=" + name + ", value=" + value + ") - adding new slot ");

		slots.add(newSlot);

		getFile().setModified(true);
	}

	// ------------------------ support for propertyChangeListeners

	/**
	 * Returned value may be null if we never had listeners.
	 *
	 * @return Our support for firing PropertyChangeEvents
	 */
	protected PropertyChangeSupport getPropertyChangeSupport() {
		return myPtyChg;
	}

	/**
	 * Add a PropertyChangeListener to the listener list. The listener is registered
	 * for all properties.
	 *
	 * @param listener The PropertyChangeListener to be added
	 */
	public final void addPropertyChangeListener(final PropertyChangeListener listener) {
		if ( myPtyChg == null ) {
			myPtyChg = new PropertyChangeSupport(this);
		}
		myPtyChg.addPropertyChangeListener(listener);
	}

	/**
	 * Add a PropertyChangeListener for a specific property. The listener will be
	 * invoked only when a call on firePropertyChange names that specific property.
	 *
	 * @param ptyName  The name of the property to listen on.
	 * @param listener The PropertyChangeListener to be added
	 */
	public final void addPropertyChangeListener(final String ptyName, final PropertyChangeListener listener) {
		if ( myPtyChg == null ) {
			myPtyChg = new PropertyChangeSupport(this);
		}
		myPtyChg.addPropertyChangeListener(ptyName, listener);
	}

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param ptyName  The name of the property that was listened on.
	 * @param listener The PropertyChangeListener to be removed
	 */
	public final void removePropertyChangeListener(final String ptyName, final PropertyChangeListener listener) {
		if ( myPtyChg != null ) {
			myPtyChg.removePropertyChangeListener(ptyName, listener);
		}
	}

	/**
	 * Remove a PropertyChangeListener from the listener list. This removes a
	 * PropertyChangeListener that was registered for all properties.
	 *
	 * @param listener The PropertyChangeListener to be removed
	 */
	public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
		if ( myPtyChg != null ) {
			myPtyChg.removePropertyChangeListener(listener);
		}
	}

	// ---------------------------------------------------------------

	/**
	 * @return Returns the gnucashObject.
	 */
	public GnucashObjectImpl getGnucashObject() {
		return gcshObj;
	}

	/**
	 * @param obj The gnucashObject to set.
	 */
	public void setGnucashObject(final GnucashObjectImpl obj) {
		if ( obj == null ) {
			throw new IllegalArgumentException("null GnuCash-object given!");
		}

		GnucashObjectImpl oldObj = this.gcshObj;
		if ( oldObj == obj ) {
			return; // nothing has changed
		}

		this.gcshObj = obj;
		// <<insert code to react further to this change here
		PropertyChangeSupport ptyChgFirer = getPropertyChangeSupport();
		if ( ptyChgFirer != null ) {
			ptyChgFirer.firePropertyChange("gnucashObject", oldObj, obj);
		}
	}

	// ---------------------------------------------------------------

	/**
	 * Just an overridden ToString to return this classe's name and hashCode.
	 *
	 * @return className and hashCode
	 */
	@Override
	public String toString() {
		return "GnucashWritableObjectHelper@" + hashCode();
	}

}