package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.CategoryScore;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.RelevantScore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author zzhao
 */
@RequiredArgsConstructor
@Getter
public class CategoryScoreImpl implements CategoryScore, Serializable {

  static final long serialVersionUID = 1L;

  private final String code;

  private final LocalizedString name;

  private final RelevantScore total;

  private List<CategoryScore> details = Collections.emptyList();

  public CategoryScoreImpl addDetail(CategoryScore detail) {
    if (this.details.isEmpty()) {
      this.details = new ArrayList<>();
    }
    this.details.add(detail);
    return this;
  }
}
