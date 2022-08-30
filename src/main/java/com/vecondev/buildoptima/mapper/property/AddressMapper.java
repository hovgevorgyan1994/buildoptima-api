package com.vecondev.buildoptima.mapper.property;

import com.vecondev.buildoptima.dto.property.AddressDto;
import com.vecondev.buildoptima.model.property.Address;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
@DecoratedWith(AddressMapperDecorator.class)
public interface AddressMapper {

  AddressDto mapToDto(Address address);

  List<AddressDto> mapToDtoList(List<Address> addresses);
}
