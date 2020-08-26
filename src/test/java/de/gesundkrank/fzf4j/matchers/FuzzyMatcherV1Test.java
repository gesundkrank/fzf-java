/*
 * Copyright (c) 2020 Jan Graßegger
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.gesundkrank.fzf4j.matchers;

import static de.gesundkrank.fzf4j.matchers.FuzzyMatcherV1.BONUS_BOUNDARY;
import static de.gesundkrank.fzf4j.matchers.FuzzyMatcherV1.BONUS_CAMEL_123;
import static de.gesundkrank.fzf4j.matchers.FuzzyMatcherV1.BONUS_CONSECUTIVE;
import static de.gesundkrank.fzf4j.matchers.FuzzyMatcherV1.BONUS_FIRST_CHAR_MULTIPLIER;
import static de.gesundkrank.fzf4j.matchers.FuzzyMatcherV1.BONUS_NON_WORD;
import static de.gesundkrank.fzf4j.matchers.FuzzyMatcherV1.SCORE_GAP_EXTENSION;
import static de.gesundkrank.fzf4j.matchers.FuzzyMatcherV1.SCORE_GAP_START;
import static de.gesundkrank.fzf4j.matchers.FuzzyMatcherV1.SCORE_MATCH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import de.gesundkrank.fzf4j.models.OrderBy;

/**
 * Tests copied from https://github.com/junegunn/fzf/blob/master/src/algo/algo_test.go
 */
class FuzzyMatcherV1Test {

    private void checkMatch(
            final String input,
            final String pattern,
            final boolean caseSensitive,
            final int expectedStartIndex,
            final int expectedEndIndex,
            final int expectedScore
    ) {
        checkMatch(
                input, pattern, false, caseSensitive, expectedStartIndex, expectedEndIndex,
                expectedScore
        );
    }

    private void checkMatch(
            final String input,
            final String pattern,
            final boolean normalize,
            final boolean caseSensitive,
            final int expectedStartIndex,
            final int expectedEndIndex,
            final int expectedScore
    ) {
        final var matcher = new FuzzyMatcherV1(
                Collections.singletonList(input), OrderBy.SCORE, normalize, caseSensitive);
        final var results = matcher.match(pattern);
        assertThat(results, is(not(empty())));

        final var result = results.get(0);
        assertThat(result.getStart(), is(expectedStartIndex));
        assertThat(result.getEnd(), is(expectedEndIndex));
        assertThat(result.getScore(), is(expectedScore));
    }

    private void checkNoMatch(final String input, final String pattern) {
        final var matcher = new FuzzyMatcherV1(
                Collections.singletonList(input), OrderBy.SCORE, false, true);
        final var results = matcher.match(pattern);
        assertThat(results, is(empty()));
    }

    @Test
    void match() {
        checkMatch("fooBarbaz1", "oBZ", false, 2, 9,
                   SCORE_MATCH * 3 + BONUS_CAMEL_123 + SCORE_GAP_START + SCORE_GAP_EXTENSION * 3
        );
        checkMatch("foo bar baz", "fbb", false, 0, 9,
                   SCORE_MATCH * 3 + BONUS_BOUNDARY * BONUS_FIRST_CHAR_MULTIPLIER
                   + BONUS_BOUNDARY * 2 + 2 * SCORE_GAP_START + 4 * SCORE_GAP_EXTENSION
        );
        checkMatch("/AutomatorDocument.icns", "rdoc", false, 9, 13,
                   SCORE_MATCH * 4 + BONUS_CAMEL_123 + BONUS_CONSECUTIVE * 2
        );
        checkMatch("/man1/zshcompctl.1", "zshc", false, 6, 10,
                   SCORE_MATCH * 4 + BONUS_BOUNDARY * BONUS_FIRST_CHAR_MULTIPLIER
                   + BONUS_BOUNDARY * 3
        );
        checkMatch("/.oh-my-zsh/cache", "zshc", false, 8, 13,
                   SCORE_MATCH * 4 + BONUS_BOUNDARY * BONUS_FIRST_CHAR_MULTIPLIER
                   + BONUS_BOUNDARY * 3 + SCORE_GAP_START
        );
        checkMatch("ab0123 456", "12356", false, 3, 10,
                   SCORE_MATCH * 5 + BONUS_CONSECUTIVE * 3 + SCORE_GAP_START + SCORE_GAP_EXTENSION
        );
        checkMatch("abc123 456", "12356", false, 3, 10,
                   SCORE_MATCH * 5 + BONUS_CAMEL_123 * BONUS_FIRST_CHAR_MULTIPLIER
                   + BONUS_CAMEL_123 * 2 + BONUS_CONSECUTIVE + SCORE_GAP_START + SCORE_GAP_EXTENSION
        );
        checkMatch("foo/bar/baz", "fbb", false, 0, 9,
                   SCORE_MATCH * 3 + BONUS_BOUNDARY * BONUS_FIRST_CHAR_MULTIPLIER
                   + BONUS_BOUNDARY * 2 + 2 * SCORE_GAP_START + 4 * SCORE_GAP_EXTENSION
        );
        checkMatch("fooBarBaz", "fbb", false, 0, 7,
                   SCORE_MATCH * 3 + BONUS_BOUNDARY * BONUS_FIRST_CHAR_MULTIPLIER
                   + BONUS_CAMEL_123 * 2 + 2 * SCORE_GAP_START + 2 * SCORE_GAP_EXTENSION
        );
        checkMatch("foo barbaz", "fbb", false, 0, 8,
                   SCORE_MATCH * 3 + BONUS_BOUNDARY * BONUS_FIRST_CHAR_MULTIPLIER + BONUS_BOUNDARY
                   + SCORE_GAP_START * 2 + SCORE_GAP_EXTENSION * 3
        );
        checkMatch("fooBar Baz", "foob", false, 0, 4,
                   SCORE_MATCH * 4 + BONUS_BOUNDARY * BONUS_FIRST_CHAR_MULTIPLIER
                   + BONUS_BOUNDARY * 3
        );
        checkMatch("xFoo-Bar Baz", "foo-b", false, 1, 6,
                   SCORE_MATCH * 5 + BONUS_CAMEL_123 * BONUS_FIRST_CHAR_MULTIPLIER
                   + BONUS_CAMEL_123 * 2 + BONUS_NON_WORD + BONUS_BOUNDARY
        );
    }

    @Test
    void matchCaseSensitive() {
        checkMatch("fooBarbaz", "oBz", true, 2, 9,
                   SCORE_MATCH * 3 + BONUS_CAMEL_123 + SCORE_GAP_START + SCORE_GAP_EXTENSION * 3
        );

        checkMatch("Foo/Bar/Baz", "FBB", true, 0, 9,
                   SCORE_MATCH * 3 + BONUS_BOUNDARY * (BONUS_FIRST_CHAR_MULTIPLIER + 2)
                   + SCORE_GAP_START * 2 + SCORE_GAP_EXTENSION * 4
        );

        checkMatch("FooBarBaz", "FBB", true, 0, 7,
                   SCORE_MATCH * 3 + BONUS_BOUNDARY * BONUS_FIRST_CHAR_MULTIPLIER
                   + BONUS_CAMEL_123 * 2 + SCORE_GAP_START * 2 + SCORE_GAP_EXTENSION * 2
        );

        checkMatch("FooBar Baz", "FooB", true, 0, 4,
                   SCORE_MATCH * 4 + BONUS_BOUNDARY * BONUS_FIRST_CHAR_MULTIPLIER
                   + BONUS_BOUNDARY * 2 + Math.max(BONUS_CAMEL_123, BONUS_BOUNDARY)
        );

        // Consecutive bonus updated
        checkMatch("foo-bar", "o-ba", false, 2, 6,
                   SCORE_MATCH * 4 + BONUS_BOUNDARY * 3
        );
    }

    @Test
    void matchNormalized() {
        checkMatch("Só Danço Samba", "So", true, false, 0, 2, 56);
        checkMatch("Só Danço Samba", "sodc", true, false, 0, 7, 89);
        checkMatch("Danço", "danco", true, false, 0, 5, 128);
    }

    @Test
    void noMatch() {
        checkNoMatch("fooBarbaz", "oBZ");
        checkNoMatch("Foo Bar Baz", "fbb");
        checkNoMatch("fooBarbaz", "fooBarbazz");
    }

    @Test
    void emptyPattern() {
        checkMatch("foobar", "", false, false, 0, 0, 0);
    }
}
