package it.cnr.igag.italgas.domain.enumeration;

import java.util.Arrays;

/**
 * The StatoAttualeCassetta enumeration.
 */
public enum StatoAttualeCassetta {
	IN_LAVORAZIONE, RIFIUTATA, TRATTAMENTO_INQUINAMENTO, LAVORAZIONE_TERMINATA, RESTITUITA;

	public static boolean isInEnum(String value) {
		return Arrays.stream(StatoAttualeCassetta.values()).anyMatch(e -> e.name().equals(value));
	}
}
