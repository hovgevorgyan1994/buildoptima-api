package com.vecondev.buildoptima.parameters.endpoints;

public class FaqCategoryEndpointUris implements EndpointUris {

  @Override
  public String baseUri() {
    return "/faq/category";
  }

  @Override
  public String deleteByIdUri() {
    return baseUri() + "/{id}";
  }

  @Override
  public String updateUri() {
    return baseUri() + "/{id}";
  }

  @Override
  public String creationUri() {
    return baseUri();
  }

  @Override
  public String fetchUri() {
    return baseUri() + "/fetch";
  }

  @Override
  public String getByIdUri() {
    return baseUri() + "/{id}";
  }

  @Override
  public String getAllUri() {
    return baseUri();
  }


}
