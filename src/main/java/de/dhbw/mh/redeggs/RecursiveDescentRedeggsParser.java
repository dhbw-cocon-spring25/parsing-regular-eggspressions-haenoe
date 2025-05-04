package de.dhbw.mh.redeggs;

import static de.dhbw.mh.redeggs.CodePointRange.range;
import static de.dhbw.mh.redeggs.CodePointRange.single;

/**
 * A parser for regular expressions using recursive descent parsing.
 * This class is responsible for converting a regular expression string into a
 * tree representation of a {@link RegularEggspression}.
 */
public class RecursiveDescentRedeggsParser {

	/**
	 * The symbol factory used to create symbols for the regular expression.
	 */
	protected final SymbolFactory symbolFactory;

	/**
	 * Constructs a new {@code RecursiveDescentRedeggsParser} with the specified
	 * symbol factory.
	 *
	 * @param symbolFactory the factory used to create symbols for parsing
	 */
	public RecursiveDescentRedeggsParser(SymbolFactory symbolFactory) {
		this.symbolFactory = symbolFactory;
	}

	/**
	 * Parses a regular expression string into an abstract syntax tree (AST).
	 *
	 * This class uses recursive descent parsing to convert a given regular
	 * expression into a tree structure that can be processed or compiled further.
	 * The AST nodes represent different components of the regex such as literals,
	 * operators, and groups.
	 *
	 * @param regex the regular expression to parse
	 * @return the {@link RegularEggspression} representation of the parsed regex
	 * @throws RedeggsParseException if the parsing fails or the regex is invalid
	 */
	public RegularEggspression parse(String regex) throws RedeggsParseException {
		// TODO: Implement the recursive descent parsing to convert `regex` into an AST.
		// This is a placeholder implementation to demonstrate how to create a symbol.

		// Create a new symbol using the symbol factory
		VirtualSymbol symbol = symbolFactory.newSymbol()
				.include(single('_'), range('a', 'z'), range('A', 'Z'))
				.andNothingElse();

		// Return a dummy Literal RegularExpression for now
		return new RegularEggspression.Literal(symbol);
	}
}
