package com.ksv.tactive.fileservice;

final class BoqExtractionUtils {

	private BoqExtractionUtils() {
	}

	static boolean isLastLevel(Double quantity, String uom) {
		if (quantity != null) {
			return quantity != 0d;
		}
		return uom != null && !uom.isBlank();
	}
}
