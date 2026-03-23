package com.garemobilegb.search.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SearchSynonymsTest {

  @Test
  void stripAccents_removesCombiningMarks() {
    assertThat(SearchSynonyms.stripAccents("Gabú")).isEqualTo("gabu");
    assertThat(SearchSynonyms.stripAccents("Bafatá")).isEqualTo("bafata");
  }

  @Test
  void expandNormalizedQuery_includesTypoVariants() {
    var v = SearchSynonyms.expandNormalizedQuery("gabu");
    assertThat(v).contains("gabú");
  }
}
