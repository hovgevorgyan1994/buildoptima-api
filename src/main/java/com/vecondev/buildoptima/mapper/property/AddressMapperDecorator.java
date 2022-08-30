package com.vecondev.buildoptima.mapper.property;

import com.vecondev.buildoptima.dto.property.AddressDto;
import com.vecondev.buildoptima.model.property.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

public abstract class AddressMapperDecorator implements AddressMapper {

  @Autowired
  @Qualifier("delegate")
  private AddressMapper mapper;

  @Override
  public AddressDto mapToDto(Address address) {
    return mapper.mapToDto(address);
  }

  @Override
  public List<AddressDto> mapToDtoList(List<Address> addresses) {
    List<AddressDto> addressDtos = new ArrayList<>();
    addresses.forEach(address -> addressDtos.add(mapToDto(address)));
    return addressDtos;
  }
}
