package com.vecondev.buildoptima.service.opensearch;

import static com.vecondev.buildoptima.exception.Error.FAILED_BULK_DOCUMENT;
import static com.vecondev.buildoptima.exception.Error.FAILED_SEARCH;

import com.vecondev.buildoptima.config.properties.OpenSearchConfigProperties;
import com.vecondev.buildoptima.exception.OpenSearchException;
import com.vecondev.buildoptima.model.property.AddressDocument;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.MultiMatchQueryBuilder.Type;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchServiceImpl implements OpenSearchService {

  private static final String[] ADDRESS_FIELD_NAMES = {
    "address_to_search", "address_to_search._2gram", "address_to_search._3gram"
  };
  private static final String AIN_FIELD_NAME = "property_ain";
  private static final String PREFIX_QUERY_NAME = "prefix";
  private final RestHighLevelClient client;
  private final OpenSearchConfigProperties properties;

  @Override
  @Transactional
  public void bulk(List<AddressDocument> addressDocuments) {
    BulkRequest bulkRequests = new BulkRequest();
    if (!addressDocuments.isEmpty()) {
      addressDocuments.forEach(
          address -> {
            IndexRequest indexRequest =
                new IndexRequest(properties.getIndexName())
                    .id(address.getPropertyAin())
                    .source(AddressDocument.getAsMap(address));
            bulkRequests.add(indexRequest);
          });
      try {
        client.bulk(bulkRequests, RequestOptions.DEFAULT);
      } catch (Exception e) {
        throw new OpenSearchException(FAILED_BULK_DOCUMENT);
      }
    }
  }

  /**
   * Perform search operation in OpenSearch by given address.
   *
   * @param address the search criteria
   * @param size the max count of results that can be retrieved
   */
  @Override
  public List<SearchHit> searchByAddress(String address, int size) {
    return search(
        new SearchSourceBuilder()
            .query(
                QueryBuilders.multiMatchQuery(address, ADDRESS_FIELD_NAMES)
                    .type(Type.BOOL_PREFIX)
                    .minimumShouldMatch("100%"))
            .size(size));
  }

  /**
   * Perform search operation in OpenSearch by given ain.
   *
   * @param ain the search criteria
   * @param size the max count of results that can be retrieved within single invocation
   */
  @Override
  public List<SearchHit> searchByAin(String ain, int size) {
    return search(
        new SearchSourceBuilder()
            .query(QueryBuilders.prefixQuery(AIN_FIELD_NAME, ain).queryName(PREFIX_QUERY_NAME))
            .size(size));
  }

  private List<SearchHit> search(SearchSourceBuilder sourceBuilder) {
    try {
      SearchResponse searchResponse =
          client.search(
              new SearchRequest().indices(properties.getIndexName()).source(sourceBuilder),
              RequestOptions.DEFAULT);

      return List.of(searchResponse.getHits().getHits());
    } catch (Exception e) {
      log.warn("Exception occurred while trying to search through properties by ain.");
      throw new OpenSearchException(FAILED_SEARCH);
    }
  }
}
