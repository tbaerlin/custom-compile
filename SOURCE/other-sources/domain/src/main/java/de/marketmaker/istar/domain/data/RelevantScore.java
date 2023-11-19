package de.marketmaker.istar.domain.data;

import java.time.LocalDate;

/**
 * A score with relevance and publishing date.
 *
 * @author zzhao
 */
public interface RelevantScore {

  /**
   * Gets the actual score on a scale from 0 to 100.
   *
   * @return a score
   */
  int getScore();

  /**
   * Gets the relevance of the score on a scale from 0 to 100.
   *
   * @return a relevance scale
   */
  int getRelevance();

  /**
   * Gets the dates when the score is published
   *
   * @return the publishing date
   */
  LocalDate getDate();
}
