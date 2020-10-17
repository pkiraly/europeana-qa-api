package de.gwdg.europeanaqa.api.indexer;

import de.gwdg.metadataqa.api.schema.EdmFullBeanSchema;
import de.gwdg.metadataqa.api.schema.Schema;
import de.gwdg.metadataqa.api.util.FileUtils;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class IndexerTest {

  @Test
  public void name() throws IOException, URISyntaxException {
    String json = FileUtils.readFirstLineFromResource("indexer/sample.json");
    assertNotNull(json);
    Schema schema = new EdmFullBeanSchema();

    Indexer indexer = new Indexer(schema);
    SolrInputDocument doc = indexer.processLine(json);

    assertEquals(10, doc.size());
    assertEquals(
      "/2058621/LoCloud_census_1891_eacc5e45_664f_413f_895e_a17f256f5992",
      doc.getField("id").getFirstValue());
    assertEquals(6, doc.getField("dataProvider_i").getFirstValue());
    assertEquals(144, doc.getField("provider_i").getFirstValue());
    assertEquals(2078, doc.getField("collection_i").getFirstValue());
    assertEquals(22, doc.getField("language_i").getFirstValue());
    assertEquals(76, doc.getField("country_i").getFirstValue());
    assertEquals(Arrays.asList("Folketelling 1891 - Fredrikstad - personsedler - side 3499"),
      doc.getField("dc_title_txt").getValues());
    assertEquals(Arrays.asList("Folketelling 1891 - Fredrikstad - personsedler - side 3499"),
      doc.getField("dc_title_ss").getValues());
    assertEquals(Arrays.asList("Tellingskrets 1\nKommunenr: 0103\nSted: Fredrikstad kjøpstad\nSide: 3499"),
      doc.getField("dc_description_txt").getValues());
    assertEquals(Arrays.asList("Tellingskrets 1\nKommunenr: 0103\nSted: Fredrikstad kjøpstad\nSide: 3499"),
      doc.getField("dc_description_ss").getValues());
  }
}
