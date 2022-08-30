package com.vecondev.buildoptima.service.property.address;

import com.vecondev.buildoptima.config.properties.OpenSearchConfigProperties;
import com.vecondev.buildoptima.exception.Error;
import com.vecondev.buildoptima.exception.OpenSearchException;
import com.vecondev.buildoptima.model.property.AddressDocument;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenSearchServiceImpl implements OpenSearchService {

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
        throw new OpenSearchException(Error.FAILED_BULK_DOCUMENT);
      }
    }
  }
}
