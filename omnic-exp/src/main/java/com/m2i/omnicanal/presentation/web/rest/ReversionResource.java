/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.service.cron.TacheProgramme;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cda
 */
@RestController
@RequestMapping("/api")
public class ReversionResource {

    TacheProgramme tacheProgramme;

    public ReversionResource(TacheProgramme tacheProgramme) {
        this.tacheProgramme = tacheProgramme;
    }

    @PostMapping("/extourne")
    @Timed

    public List<ResultMessage> getAllMiddleWare(@RequestParam Long operId) throws Exception {
        return tacheProgramme.executeExtourne(operId);
    }
}
