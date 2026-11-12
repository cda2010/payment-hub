/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.fcm.service;

import com.m2i.pti.fcm.domain.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import org.springframework.stereotype.Service;
/**
 *
 * @author cda
 */
@Service
public class ApiFcmService {

    public void sendFcmMessage(String title, String body) {
        Sender sender = new Sender("AIzaSyBUvXfTK2yw_XywSBTu1bQO9u1bDwhf7Vs");
        NotificationFcm notification = new NotificationFcm(title, body);

        notification.setSound("default");

        notification.setClickAction("FCM_PLUGIN_ACTIVITY");
        MessageNotification message = new MessageNotification.MessageBuilder()
                .toToken("/topics/all")
                //.collapseKey("chat-app")
                .contentAvailable(true)
                .priority(MessageNotification.Priority.HIGH) // or just type "normal" or "high"
                //  .timeToLive(3)
                //.restrictedPackageName("com.m2i.mobile.corisacademia")
                .addData("objet", title)
                .addData("contenu", body)
                .notification(notification)
                .build();
        sender.send(message, new Callback() {

            @Override
            public void onResponse(Call call, Response rspns) {
                if (rspns.isSuccessful()) {
                    System.out.print("Hooray!");
                }
            }

            @Override
            public void onFailure(Call call, Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
        });

    }
}
