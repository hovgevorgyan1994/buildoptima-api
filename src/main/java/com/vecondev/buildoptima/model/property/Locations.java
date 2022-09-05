package com.vecondev.buildoptima.model.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Locations implements Serializable {

  @Serial private static final long serialVersionUID = -2968234697866860765L;
  private Centroid centroid;
  private Set<Polygon> polygons;
}
