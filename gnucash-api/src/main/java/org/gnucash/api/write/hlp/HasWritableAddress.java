package org.gnucash.api.write.hlp;

import org.gnucash.api.read.aux.GCshAddress;
import org.gnucash.api.write.aux.GCshWritableAddress;

public interface HasWritableAddress {

    GCshWritableAddress getWritableAddress();
    
    GCshWritableAddress createWritableAddress();
    
	void removeAddress(GCshWritableAddress impl);

    void setAddress(GCshAddress adr);
}
