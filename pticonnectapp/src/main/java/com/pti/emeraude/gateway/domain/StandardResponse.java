/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway.domain;

/**
 *
 * @author cda
 */
public class StandardResponse {
    private ResponseStatus responseStatus;
  
  public ResponseStatus getResponseStatus()
  {
    return this.responseStatus;
  }
  
  public void setResponseStatus(ResponseStatus responseStatus)
  {
    this.responseStatus = responseStatus;
  }
}
