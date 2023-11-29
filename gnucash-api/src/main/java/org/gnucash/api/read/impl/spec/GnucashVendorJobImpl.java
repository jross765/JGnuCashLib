package org.gnucash.api.read.impl.spec;

import org.gnucash.api.basetypes.simple.GCshID;
import org.gnucash.api.read.GnucashFile;
import org.gnucash.api.read.GnucashGenerJob;
import org.gnucash.api.read.GnucashVendor;
import org.gnucash.api.read.impl.GnucashGenerJobImpl;
import org.gnucash.api.read.spec.GnucashVendorJob;
import org.gnucash.api.read.spec.WrongInvoiceTypeException;
import org.gnucash.api.generated.GncV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GnucashVendorJobImpl extends GnucashGenerJobImpl
                                  implements GnucashVendorJob
{
  private static final Logger LOGGER = LoggerFactory.getLogger(GnucashVendorJobImpl.class);

    /**
     * @param peer the JWSDP-object we are facading.
     * @see #jwsdpPeer
     * @param gncFile the file to register under
     */
    @SuppressWarnings("exports")
    public GnucashVendorJobImpl(
            final GncV2.GncBook.GncGncJob peer,
            final GnucashFile gncFile) {
        super(peer, gncFile);
    }

    public GnucashVendorJobImpl(final GnucashGenerJob job) throws WrongInvoiceTypeException {
	super(job.getJwsdpPeer(), job.getFile());

	// No, we cannot check that first, because the super() method
	// always has to be called first.
	if ( job.getOwnerType() != GnucashGenerJob.TYPE_VENDOR )
	    throw new WrongInvoiceTypeException();

	// ::TODO
//	for ( GnucashGenerInvoice invc : job.getInvoices() )
//	{
//	    addInvoice(new GnucashJobInvoiceImpl(invc));
//	}
    }

    // ---------------------------------------------------------------
    
    /**
     * {@inheritDoc}
     */
    public GCshID getVendorId() {
        return getOwnerId();
    }

    /**
     * {@inheritDoc}
     */
    public GnucashVendor getVendor() {
        return file.getVendorByID(getVendorId());
    }

    // -----------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("[GnucashVendorJobImpl:");
	buffer.append(" id: ");
	buffer.append(getId());
	
	buffer.append(" number: ");
	buffer.append(getNumber());
	
	buffer.append(" name: '");
	buffer.append(getName() + "'");
	
	buffer.append(" vendor-id: ");
	buffer.append(getVendorId());
	
	buffer.append(" is-active: ");
	buffer.append(isActive());
	
	buffer.append("]");
	return buffer.toString();
    }

}