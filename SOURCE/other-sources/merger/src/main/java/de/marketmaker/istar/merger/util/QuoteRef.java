package de.marketmaker.istar.merger.util;

import de.marketmaker.istar.domain.instrument.Quote;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zzhao
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class QuoteRef {

  public static final int MAX_DEPTH = 5; // max reference depth

  private final Quote quote;

  private QuoteRef next;

  public static QuoteRef ref(Quote quote) {
    return new QuoteRef(quote);
  }

  public QuoteRef chain(QuoteRef right) {
    if (this.next == null) {
      this.next = right;
      return this;
    }
    if (right.next == null) {
      right.next = this;
      return right;
    }

    QuoteRef ref = this;
    int n = 0; // guard against cyclic reference
    while (ref.next != null && n < MAX_DEPTH) {
      ref = ref.next;
      n++;
    }

    if (n >= MAX_DEPTH) {
      throw new IllegalStateException("quote reference depth exceeds " + MAX_DEPTH);
    }

    ref.next = right;
    return this;
  }
}
