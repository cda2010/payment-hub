package com.gateway.payment.identity.enrolement;

import java.util.List;

/** Produit ENROLEMENT tel que le catalogue le publie. */
public interface CatalogueEnrolement {

    String CODE = "ENROLEMENT";

    String defautPays();

    List<ParcoursPays> pays();

    ParcoursPays pour(String code);
}
