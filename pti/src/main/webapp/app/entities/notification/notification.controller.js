(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('NotificationController', NotificationController);

    NotificationController.$inject = ['$state', 'Notifications',  '$scope', 'TableService', 'Notification'];

    function NotificationController($state, Notifications, $scope, TableService, Notification) {

        var vm = this;
        vm.init=init;
        vm.typeNotifications=['SMS','MESSAGEPUSH','EMAIL'];
        vm.itemSearch={
            "typeNotification":null,
            "dateEnvoi":null,
            "destination":null
        };
        vm.afficheFiltre=false;
        vm.afficherFiltre=afficherFiltre;
        vm.chercherNot=chercherNot;
        vm.ResendSmS=ResendSmS;
       function ResendSmS(notification) {
            console.log('notification',notification);
            if(notification.typeNotification==="SMS"){
                Notifications.sendSMS({numero:notification.destinataire,message:notification.contenu}, function (reponse) {
                console.log(reponse);
                if(reponse.errCode===0) {
                    Notification.success('Message Envoyer');
                } else {
                    Notification.error('Echec d\'envoi du message');
                }
            });
            }else if (notification.typeNotification==="EMAIL"){
                Notifications.sendSMail({ mail:notification.destinataire,message:notification.contenu}, function (reponse) {
                console.log(reponse);
                if(reponse.errCode===0) {
                    Notification.success('Message Envoyer');
                } else {
                    Notification.error('Echec d\'envoi du message');
                }
            });
            }
            
        }
        function afficherFiltre(){
            vm.afficheFiltre=!vm.afficheFiltre;
            if(!vm.afficheFiltre){
                vm.itemSearch={
                    "typeNotification":null,
                    "dateEnvoi":null,
                    "destination":null
                };
                init();
            }
        }
        init();
        function chercherNot() {
            console.log('test',vm.itemSearch.destination,vm.itemSearch.typeNotification,vm.itemSearch.dateEnvoi);
            init();
        }
        function init(){
            var addItemData = {
                templateUrl: 'app/entities/notification/notification-dialog.html',
                controller: 'NotificationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: function () {
                        return {
                            dateHeureEnvoie: null,
                            contenu: null,
                            destinataire: null,
                            isDelivred: null,
                            typeNotification: null,
                            id: null,
                            clientId:null
                        };
                    }
                }
            };

            var editItemData = {
                templateUrl: 'app/entities/notification/notification-dialog.html',
                controller: 'NotificationDialogController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return false;
                    },
                    isEdit: function () {
                        return true;
                    },
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };

            var detailItemData = {
                templateUrl: 'app/entities/notification/notification-dialog.html',
                controller: 'NotificationDialogController',
                controllerAs: 'vm',
                size: 'md',
                resolve: {
                    isViewing: function () {
                        return true;
                    },
                    isEdit: function () {
                        return false;
                    },
                    entity: null //valeur déterminé à l'appel de la boite,
                }
            };
            var deleteItemData = {
                templateUrl: 'app/entities/notification/notification-delete-dialog.html',
                    controller: 'NotificationDeleteController',
                controllerAs: 'vm',
                size: 'sm',
                resolve: {
                    entity: null //valeur déterminé à l'appel de la boite
                }
            };
            var dialogData = {
                addData: addItemData,
                editData: editItemData,
                detailData: detailItemData,
                deleteData: deleteItemData
            };
            var entitySearchParams = function () {
                return {};
            };
            var entityParams = function () {
                return {
                    "typeNotification": vm.itemSearch.typeNotification,
                    "dateEnvoi":vm.itemSearch.dateEnvoi,
                    "destination":vm.itemSearch.destination
                };
            };
            var datas = {
                scope: $scope,
                vm: vm,
                entity: Notifications,
                dialogData: dialogData,
                entitySearch: Notifications,
                entitySearchParams: entitySearchParams,
                entityParams: entityParams,
                loadCallBack: vm.load
            };
            TableService.init(datas);

        }
    }
})();
