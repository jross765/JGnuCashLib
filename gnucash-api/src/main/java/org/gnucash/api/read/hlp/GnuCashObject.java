package org.gnucash.api.read.hlp;

import org.gnucash.api.read.GnuCashFile;

/**
 * Interface all GnuCash entities implement.
 */
public interface GnuCashObject {

    /**
     * @return the file we belong to.
     */
    GnuCashFile getGnuCashFile();

}
