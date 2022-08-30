package com.vecondev.buildoptima.service.property.address;

import com.vecondev.buildoptima.model.property.AddressDocument;
import java.util.List;

public interface OpenSearchService {

  void bulk(List<AddressDocument> addressDocuments);
}
