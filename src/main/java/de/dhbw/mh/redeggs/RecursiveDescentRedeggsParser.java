package de.dhbw.mh.redeggs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
    private String regex;
    private int position;

    /**
     * Constructs a new {@code RecursiveDescentRedeggsParser} with the specified
     * symbol factory.
     *
     * @param symbolFactory the factory used to create symbols for parsing
     */
    public RecursiveDescentRedeggsParser(SymbolFactory symbolFactory) {
        this.symbolFactory = symbolFactory;
    }

    private char peek() {
        return this.regex.charAt(this.position);
    }

    private boolean atEnd() {
        return this.position == this.regex.length();
    }

    private boolean check(char toCheck) {
        return !this.atEnd() && this.peek() == toCheck;
    }

    private boolean checkLiteral() {
        if (this.atEnd()) return false;

        char toCheck = this.peek();
        return (toCheck == '_') || (toCheck >= 'a' && toCheck <= 'z') || (toCheck >= 'A' && toCheck <= 'Z') || (toCheck >= '0' && toCheck <= '9');
    }

    private char consume() {
        char current = this.regex.charAt(this.position);
        this.position++;

        return current;
    }

    private RegularEggspression regex() throws RedeggsParseException {
        RegularEggspression concat = this.concat();

        return this.union(concat);
    }

    private RegularEggspression union(RegularEggspression rhs) throws RedeggsParseException {
        if (this.check('|')) {
            this.consume();
            RegularEggspression concat = this.concat();
            RegularEggspression suffix = this.union(concat);

            return new RegularEggspression.Alternation(rhs, suffix);
        } else if (this.atEnd() || this.check(')')) {
            return rhs;
        }

        throw new RedeggsParseException("expected '|' or EOF", this.position);
    }

    private RegularEggspression concat() throws RedeggsParseException {
        RegularEggspression kleene = this.kleene();
        RegularEggspression suffix = this.suffix();

        if (suffix instanceof RegularEggspression.EmptyWord) {
            return kleene;
        }

        return new RegularEggspression.Concatenation(kleene, suffix);
    }

    private RegularEggspression suffix() throws RedeggsParseException {
        if (this.check('[') || this.check('(') || this.checkLiteral()) {
            RegularEggspression kleene = this.kleene();
            RegularEggspression suffix = this.suffix();

            if (suffix instanceof RegularEggspression.EmptyWord) {
                return kleene;
            }

            return new RegularEggspression.Concatenation(kleene, suffix);
        } else if (this.check('|') || this.atEnd() || this.check(')')) {
            return new RegularEggspression.EmptyWord();
        }

        throw new RedeggsParseException("expected '[', '(', literal or EOF", this.position);
    }

    private RegularEggspression kleene() throws RedeggsParseException {
        RegularEggspression base = this.base();

        return this.star(base);
    }

    private RegularEggspression star(RegularEggspression lhs) throws RedeggsParseException {
        if (this.check('*')) {
            this.consume();
            return new RegularEggspression.Star(lhs);
        } else if (this.checkLiteral() || this.check('[') || this.check('(') || this.check('|') || this.atEnd() || this.check(')')) {
            return lhs;
        }

        throw new RedeggsParseException("expected a literal, '[', '(', '|' or EOF", this.position);
    }

    private RegularEggspression base() throws RedeggsParseException {
        if (this.checkLiteral()) {
            char literal = this.consume();

            return new RegularEggspression.Literal(this.symbolFactory.newSymbol().include(single(literal)).andNothingElse());
        } else if (this.check('(')) {
            this.consume();

            RegularEggspression regex = this.regex();

            if (!this.check(')')) {
                throw new RedeggsParseException("Input ended unexpectedly, expected symbol ')'", this.position);
            }

            this.consume();

            return regex;
        } else if (this.check('[')) {
            this.consume();

            boolean negation = this.negation();
            List<CodePointRange> inhalt = this.inhalt();
            List<CodePointRange> range = this.range();

            CodePointRange[] ranges = Stream.concat(inhalt.stream(), range.stream()).toArray(CodePointRange[]::new);

            var builder = this.symbolFactory.newSymbol();

            if (negation) {
                builder.exclude(ranges);
            } else {
                builder.include(ranges);
            }

            if (!this.check(']')) {
                throw new RedeggsParseException("expected ']'", this.position);
            }

            this.consume();

            return new RegularEggspression.Literal(builder.andNothingElse());
        } else if (this.check('ε')) {
            this.consume();

            return new RegularEggspression.EmptyWord();
        } else if (this.check('∅')) {
            this.consume();

            return new RegularEggspression.EmptySet();
        }

        throw new RedeggsParseException("expected literal, '(' or '['", this.position);
    }

    private boolean negation() throws RedeggsParseException {
        if (this.check('^')) {
            this.consume();
            return true;
        } else if (this.checkLiteral()) {
            return false;
        }

        throw new RedeggsParseException("expected '^' or a literal", this.position);
    }

    private List<CodePointRange> inhalt() throws RedeggsParseException {
        char literal = this.literal();

        return this.rest(literal);
    }

    private List<CodePointRange> range() throws RedeggsParseException {
        if (this.checkLiteral()) {
            List<CodePointRange> inhalt = this.inhalt();
            List<CodePointRange> range = this.range();

            return Stream.concat(inhalt.stream(), range.stream()).toList();
        } else if (this.check(']')) {
            return new ArrayList<>();
        }

        throw new RedeggsParseException("expected a literal or ']", this.position);
    }

    private List<CodePointRange> rest(char lhs) throws RedeggsParseException {
        if (this.check('-')) {
            this.consume();
            char rhs = this.literal();

            List<CodePointRange> ranges = new ArrayList<>();
            ranges.add(CodePointRange.range(lhs, rhs));

            return ranges;
        } else if (this.checkLiteral() || this.check(']')) {
            List<CodePointRange> inhalt = new ArrayList<>();
            inhalt.add(single(lhs));

            return inhalt;
        }

        throw new RedeggsParseException("expected '-', literal or ']'", this.position);
    }

    private char literal() throws RedeggsParseException {
        if (this.checkLiteral()) {
            return this.consume();
        }

        throw new RedeggsParseException("expected literal", this.position);
    }


    /**
     * Parses a regular expression string into an abstract syntax tree (AST).
     * <p>
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
        this.position = 0;
        this.regex = regex;

        RegularEggspression expr = null;

        while (!this.atEnd()) {
            expr = this.regex();
        }

        return expr;
    }
}
