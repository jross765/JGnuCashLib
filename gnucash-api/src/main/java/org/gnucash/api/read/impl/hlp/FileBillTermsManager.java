package org.gnucash.api.read.impl.hlp;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gnucash.api.basetypes.simple.GCshID;
import org.gnucash.api.generated.GncGncBillTerm;
import org.gnucash.api.generated.GncV2;
import org.gnucash.api.read.aux.GCshBillTerms;
import org.gnucash.api.read.impl.GnucashFileImpl;
import org.gnucash.api.read.impl.aux.GCshBillTermsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBillTermsManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileBillTermsManager.class);

	// ---------------------------------------------------------------

	private GnucashFileImpl gcshFile;

	private Map<GCshID, GCshBillTerms> bllTrmMap = null;

	// ---------------------------------------------------------------

	public FileBillTermsManager(GnucashFileImpl gcshFile) {
		this.gcshFile = gcshFile;
		init(gcshFile.getRootElement());
	}

	// ---------------------------------------------------------------

	private void init(final GncV2 pRootElement) {
		bllTrmMap = new HashMap<GCshID, GCshBillTerms>();

		List<Object> bookElements = pRootElement.getGncBook().getBookElements();
		for ( Object bookElement : bookElements ) {
			if ( !(bookElement instanceof GncGncBillTerm) ) {
				continue;
			}
			GncGncBillTerm jwsdpPeer = (GncGncBillTerm) bookElement;
			GCshBillTermsImpl billTerms = new GCshBillTermsImpl(jwsdpPeer, gcshFile);
			bllTrmMap.put(billTerms.getID(), billTerms);
		}

		LOGGER.debug("init: No. of entries in bill terms map: " + bllTrmMap.size());
	}

	protected GCshBillTermsImpl createBillTerms(final GncGncBillTerm jwsdpBllTrm) {
		GCshBillTermsImpl bllTrm = new GCshBillTermsImpl(jwsdpBllTrm, gcshFile);
		LOGGER.debug("Generated new bill terms: " + bllTrm.getID());
		return bllTrm;
	}

	// ---------------------------------------------------------------

	public void addBillTerms(GCshBillTerms bllTrm) {
		bllTrmMap.put(bllTrm.getID(), bllTrm);
		LOGGER.debug("Added bill terms to cache: " + bllTrm.getID());
	}

	public void removeBillTerms(GCshBillTerms bllTrm) {
		bllTrmMap.remove(bllTrm.getID());
		LOGGER.debug("Removed bill terms from cache: " + bllTrm.getID());
	}

	// ---------------------------------------------------------------

	public GCshBillTerms getBillTermsByID(final GCshID id) {
		if ( bllTrmMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		return bllTrmMap.get(id);
	}

	public GCshBillTerms getBillTermsByName(final String name) {
		if ( bllTrmMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		for ( GCshBillTerms billTerms : bllTrmMap.values() ) {
			if ( billTerms.getName().equals(name) ) {
				return billTerms;
			}
		}

		return null;
	}

	public Collection<GCshBillTerms> getBillTerms() {
		if ( bllTrmMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		return bllTrmMap.values();
	}

	// ---------------------------------------------------------------

	public int getNofEntriesBillTermsMap() {
		return bllTrmMap.size();
	}

}
