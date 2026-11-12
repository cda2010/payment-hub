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
public class ResponseStatus {
    private String statusCode;
  private Object msg;
  
  public ResponseStatus(String statusCode, String msg)
  {
    this.statusCode = statusCode;
    this.msg = msg;
  }
  
  public ResponseStatus() {}
  
  public String getStatusCode()
  {
    return this.statusCode;
  }
  
  public void setStatusCode(String statusCode)
  {
    this.statusCode = statusCode;
  }
  
  public Object getMsg()
  {
    return this.msg;
  }
  
  public void setMsg(Object msg)
  {
    this.msg = msg;
  }
}
