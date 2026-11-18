package com.ksv.tactive.fileservice;

final class BoqExtractionUtils {

	private BoqExtractionUtils() {
	}

	static boolean isLastLevel(Double quantity, String uom) {
		return quantity != null || (uom != null && !uom.isBlank());
	}
}
