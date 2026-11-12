/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.pentaho;

/**
 *
 * @author cda
 */
public class ReplicationDomaine {

    String tableSource;
    String tableDest;
    String delScript;

    public ReplicationDomaine(String tableSource, String tableDest) {
        this.tableSource = tableSource;
        this.tableDest = tableDest;
    }

    public String getDelScript() {
        return delScript;
    }

    public void setDelScript(String delScript) {
        this.delScript = delScript;
    }

}
