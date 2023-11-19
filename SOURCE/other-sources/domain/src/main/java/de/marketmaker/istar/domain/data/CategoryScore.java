package de.marketmaker.istar.domain.data;

import java.util.List;

/**
 * A score with a category name, total score and detailed sub category scores if any.
 *
 * @author zzhao
 */
public interface CategoryScore {

  /**
   * Gets the code, i.e. abbreviation of this category. For a describing name use {@link
   * #getName()}.
   *
   * @return the code for the category
   */
  String getCode();

  /**
   * Gets the localized name.
   *
   * @return a localized name
   */
  LocalizedString getName();

  /**
   * Gets the total score.
   *
   * @return the total score
   */
  RelevantScore getTotal();

  /**
   * Gets the detailed sub category scores, can be empty.
   *
   * @return a list of category scores, can be empty, never null
   */
  List<CategoryScore> getDetails();
}
