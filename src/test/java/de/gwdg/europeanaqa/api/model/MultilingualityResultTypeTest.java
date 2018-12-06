package de.gwdg.europeanaqa.api.model;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MultilingualityResultTypeTest {

  @Test
  public void testValueOf() {
    assertThat(
      MultilingualityResultType.NORMAL,
      is(MultilingualityResultType.valueOf("NORMAL"))
    );
    assertThat(
      MultilingualityResultType.EXTENDED,
      is(MultilingualityResultType.valueOf("EXTENDED"))
    );
  }

  @Test
  public void testValue() {
    assertThat(0, is(MultilingualityResultType.NORMAL.value()));
    assertThat(1, is(MultilingualityResultType.EXTENDED.value()));
  }
}
