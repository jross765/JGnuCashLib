package org.gnucash.api.read.impl;

//other imports

//automatically created logger for debug and error -output

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gnucash.api.read.GnucashFile;
import org.gnucash.api.read.GnucashObject;
import org.gnucash.api.Const;
import org.gnucash.api.generated.ObjectFactory;
import org.gnucash.api.generated.Slot;
import org.gnucash.api.generated.SlotValue;
import org.gnucash.api.generated.SlotsType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper-Class used to implement functions all gnucash-objects support.
 */
public class GnucashObjectImpl implements GnucashObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(GnucashObjectImpl.class);

    /**
     * the user-defined values.
     */
    private SlotsType mySlots;

    /**
     * The file we belong to.
     */
    private final GnucashFile myFile;

    // -----------------------------------------------------------------

    public GnucashObjectImpl(final GnucashFile myFile) {
	super();

	this.myFile = myFile;
    }

    /**
     * @param slots    ${@link #mySlots}
     * @param gcshFile The file we belong to
     */
    @SuppressWarnings("exports")
    public GnucashObjectImpl(final SlotsType slots, final GnucashFile gcshFile) {
	super();

	this.myFile = gcshFile;
	setSlots(slots);
//      System.err.println("Slots:");
//      for ( Slot slot : getSlots().getSlot() )
//        System.err.println(" - " + slot);
    }

    // -----------------------------------------------------------------

    /**
     * @return Returns the slots.
     * @link #mySlots
     */
    @SuppressWarnings("exports")
    public SlotsType getSlots() {
	return mySlots;
    }

    /**
     * @param slots The slots to set.
     * @link #mySlots
     */
    @SuppressWarnings("exports")
    public void setSlots(final SlotsType slots) {
	if (slots == null) {
	    throw new IllegalArgumentException("null 'slots' given!");
	}

	SlotsType oldSlots = mySlots;
	if (oldSlots == slots) {
	    return; // nothing has changed
	}
	// ::TODO Check with equals as well
	mySlots = slots;

	// we have an xsd-problem saving empty slots so we add a dummy-value
	if (slots.getSlot().isEmpty()) {
	    ObjectFactory objectFactory = new ObjectFactory();

	    SlotValue value = objectFactory.createSlotValue();
	    value.setType(Const.XML_DATA_TYPE_STRING);
	    value.getContent().add(Const.DUMMY_FILL);

	    Slot slot = objectFactory.createSlot();
	    slot.setSlotKey(Const.DUMMY_FILL);
	    slot.setSlotValue(value);

	    slots.getSlot().add(slot);
	}

	// <<insert code to react further to this change here
	PropertyChangeSupport propertyChangeFirer = getPropertyChangeSupport();
	if (propertyChangeFirer != null) {
	    propertyChangeFirer.firePropertyChange("slots", oldSlots, slots);
	}
    }

    /**
     * @return Returns the file.
     * @link #myFile
     */
    public GnucashFile getGnucashFile() {
	return myFile;
    }

//  public void setGnucashFile(GnucashFile gcshFile) {
//    this.myFile = gcshFile;
//  }

    // -----------------------------------------------------------------

    /**
     * @return all keys that can be used with
     *         ${@link #getUserDefinedAttribute(String)}}.
     */
    public Collection<String> getUserDefinedAttributeKeys() {
	List<Slot> slots = getSlots().getSlot();
	List<String> retval = new ArrayList<String>(slots.size());

	for (Slot slot : slots) {
	    retval.add(slot.getSlotKey());
	}

	return retval;
    }

    /**
     * @param name the name of the user-defined attribute
     * @return the value or null if not set
     */
    public String getUserDefinedAttribute(final String name) {
	return getUserDefinedAttributeCore(getSlots().getSlot(), name);
    }

    private String getUserDefinedAttributeCore(final List<Slot> slots, final String name) {
	if ( slots == null )
	    return null;
	
	if ( name.equals("") )
	    return null;
	
	// ---
	
	String nameFirst = "";
	String nameRest = "";
	if ( name.contains(".") ) {
	    String[] nameParts = name.split("\\.");
	    nameFirst = nameParts[0];
	    if ( nameParts.length > 1 ) {
		for ( int i = 1; i < nameParts.length; i++ ) {
		    nameRest += nameParts[i];
		    if ( i < nameParts.length - 1 )
			nameRest += ".";
		}
	    }
	} else {
	    nameFirst = name;
	}
//	System.err.println("np1: '" + nameFirst + "'");
//	System.err.println("np2: '" + nameRest + "'");
	
	// ---
	
	for ( Slot slot : slots ) {
	    if ( slot.getSlotKey().equals(nameFirst) ) {
		if ( slot.getSlotValue().getType().equals("string") || 
		     slot.getSlotValue().getType().equals("integer") || 
		     slot.getSlotValue().getType().equals("guid") ) {
		    List<Object> objList = slot.getSlotValue().getContent();
		    if ( objList == null ||
			 objList.size() == 0 )
			return null;
		    Object value = objList.get(0);
		    if ( value == null )
			return null;
		    if ( ! ( value instanceof String ) ) {
			LOGGER.error("User-defined attribute for key '" + nameFirst + "' may not be a String."
				+ " It is of type [" + value.getClass().getName() + "]");
		    }
		    return value.toString();
		} else if ( slot.getSlotValue().getType().equals("frame") ) {
		    List<Slot> subSlots = new ArrayList<Slot>();
		    for ( Object obj : slot.getSlotValue().getContent() ) {
			if ( obj instanceof Slot ) {
			    Slot subSlot = (Slot) obj;
			    subSlots.add(subSlot);
			}
		    }
		    return getUserDefinedAttributeCore(subSlots, nameRest);
		} else {
		    LOGGER.error("getUserDefinedAttributeCore: Unknown slot type");
		    return "NOT IMPLEMENTED YET";
		}
	    } // if slot-key
	} // for slot

	return null;
    }

    // ------------------------ support for propertyChangeListeners

    /**
     * support for firing PropertyChangeEvents. (gets initialized only if we really
     * have listeners)
     */
    private volatile PropertyChangeSupport myPropertyChange = null;

    /**
     * Returned value may be null if we never had listeners.
     *
     * @return Our support for firing PropertyChangeEvents
     */
    protected PropertyChangeSupport getPropertyChangeSupport() {
	return myPropertyChange;
    }

    /**
     * Add a PropertyChangeListener to the listener list. The listener is registered
     * for all properties.
     *
     * @param listener The PropertyChangeListener to be added
     */
    @SuppressWarnings("exports")
    public final void addPropertyChangeListener(final PropertyChangeListener listener) {
	if (myPropertyChange == null) {
	    myPropertyChange = new PropertyChangeSupport(this);
	}
	myPropertyChange.addPropertyChangeListener(listener);
    }

    /**
     * Add a PropertyChangeListener for a specific property. The listener will be
     * invoked only when a call on firePropertyChange names that specific property.
     *
     * @param propertyName The name of the property to listen on.
     * @param listener     The PropertyChangeListener to be added
     */
    @SuppressWarnings("exports")
    public final void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
	if (myPropertyChange == null) {
	    myPropertyChange = new PropertyChangeSupport(this);
	}
	myPropertyChange.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Remove a PropertyChangeListener for a specific property.
     *
     * @param propertyName The name of the property that was listened on.
     * @param listener     The PropertyChangeListener to be removed
     */
    @SuppressWarnings("exports")
    public final void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
	if (myPropertyChange != null) {
	    myPropertyChange.removePropertyChangeListener(propertyName, listener);
	}
    }

    /**
     * Remove a PropertyChangeListener from the listener list. This removes a
     * PropertyChangeListener that was registered for all properties.
     *
     * @param listener The PropertyChangeListener to be removed
     */
    @SuppressWarnings("exports")
    public synchronized void removePropertyChangeListener(final PropertyChangeListener listener) {
	if (myPropertyChange != null) {
	    myPropertyChange.removePropertyChangeListener(listener);
	}
    }

    // -------------------------------------------------------

    /**
     * Just an overridden ToString to return this classe's name and hashCode.
     *
     * @return className and hashCode
     */
    @Override
    public String toString() {
	return "GnucashObjectImpl@" + hashCode();
    }

}
