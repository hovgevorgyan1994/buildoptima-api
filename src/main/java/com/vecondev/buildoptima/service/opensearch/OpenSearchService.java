package com.vecondev.buildoptima.service.opensearch;

import com.vecondev.buildoptima.model.property.AddressDocument;
import java.util.List;
import org.opensearch.search.SearchHit;

public interface OpenSearchService {

  void bulk(List<AddressDocument> addressDocuments);

  List<SearchHit> searchByAddress(String address, int size);

  List<SearchHit> searchByAin(String ain, int size);
}
