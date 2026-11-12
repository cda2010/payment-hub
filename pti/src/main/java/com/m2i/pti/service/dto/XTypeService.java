package com.m2i.pti.service.dto;

public class XTypeService {
  String typeService;
  Long nombre;

  public String getTypeService() {
    return this.typeService;
  }

  public void setTypeService(String typeService) {
    this.typeService = typeService;
  }

  public Long getNombre() {
    return this.nombre;
  }

  public void setNombre(Long nombre) {
    this.nombre = nombre;
  }

  @Override
  public String toString() {
    return (
      "{" +
      " typeService='" +
      getTypeService() +
      "'" +
      ", nombre='" +
      getNombre() +
      "'" +
      "}"
    );
  }
}
