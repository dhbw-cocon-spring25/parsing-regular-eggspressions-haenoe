package de.dhbw.mh.redeggs;

/**
 * Exception thrown when an error occurs during regular expression parsing. This
 * exception includes information about the parsing error and the location
 * (character offset) in the input string where the error occurred.
 */
public class RedeggsParseException extends Exception {

	/**
	 * The unique serial version identifier for this exception class.
	 */
	@java.io.Serial
	private static final long serialVersionUID = -7112812927348106687L;

	/**
	 * The zero-based character offset in the input string where the parsing error
	 * occurred.
	 */
	private final int errorOffset;

	/**
	 * Constructs a new {@code RedeggsParseException} with the specified detail
	 * message and offset.
	 * 
	 * @param message     the detail message, which provides more information about
	 *                    the exception and can be retrieved later using
	 *                    {@link #getMessage()}.
	 * @param errorOffset the position (zero-based) in the input string where the
	 *                    error occurred during parsing.
	 */
	public RedeggsParseException(String message, int errorOffset) {
		super(message + " at position " + (errorOffset + 1) + ".");
		this.errorOffset = errorOffset;
	}

	/**
	 * Returns the zero-based character offset in the input where the parsing error
	 * occurred.
	 *
	 * @return the zero-based character offset of the parsing error
	 */
	public int getErrorOffset() {
		return errorOffset;
	}
}
