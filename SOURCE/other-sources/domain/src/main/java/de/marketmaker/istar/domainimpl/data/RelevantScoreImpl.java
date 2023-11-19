package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.RelevantScore;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zzhao
 */
@Getter
@RequiredArgsConstructor
public class RelevantScoreImpl implements RelevantScore, Serializable {

  static final long serialVersionUID = 1L;

  private final int score;

  private final int relevance;

  private final LocalDate date;
}
