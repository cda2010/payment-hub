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
public interface  HttpClientConfigConstants {
    // Connection pool
  int MAX_ROUTE_CONNECTIONS     = 40;
  int MAX_TOTAL_CONNECTIONS     = 40;
  int MAX_LOCALHOST_CONNECTIONS = 80;

  // Keep alive
  int DEFAULT_KEEP_ALIVE_TIME = 20 * 1000; // 20 sec

  // Timeouts
  int CONNECTION_TIMEOUT = 30 * 1000; // 30 sec, the time for waiting until a connection is established
  int REQUEST_TIMEOUT    = 30 * 1000; // 30 sec, the time for waiting for a connection from connection pool
  int SOCKET_TIMEOUT     = 60 * 1000; // 60 sec, the time for waiting for data

  // Idle connection monitor
  int IDLE_CONNECTION_WAIT_TIME = 30 * 1000; // 30 sec
  
}
