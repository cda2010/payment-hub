(function() {
    "use strict";
    angular.module('ptiApp').factory('CancelAlert', ['$uibModal', CancelAlert]);
    // AgentPopup.$inject = [];
    function CancelAlert($uibModal) {
        console.log('***Création du service cancel|confirm alert');

        //Entity,scope,templateUrl

        var service = {
            cancelAlert: cancelAlert,
            confirm: confirm,
            confirmCancel:cancelAlert
        };

        return service;

        function cancelAlert(modalInstance) {
            $uibModal.open({
                templateUrl: 'app/components/cancel/cancel-dialog.html',
                controller: 'CancelController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'sm',
                resolve: {
                    msg: function() {
                        return 'Etes-vous certains de vouloir annuler ?';
                    }
                }
            }).result.then(function() {
                modalInstance.dismiss("cancel");
            }, function() {
                
            });
        }
        /**
         *Permet la confirmation d'une action particulière : suppression, etc
         *Avec le message à afficher fourni en paramètre et les callbacks à appeler
         *suivant l'action choisi.
         */
        function confirm(success, fail, msg,withForm) {
            $uibModal.open({
                templateUrl: 'app/components/cancel/cancel-dialog.html',
                controller: 'CancelController',
                controllerAs: 'vm',
                backdrop: 'static',
                size: 'sm',
                resolve: {
                    msg: function() {
                        var m = msg;
                        if (msg === null || angular.isUndefined(msg) || msg === "") {
                            m = 'Etes-vous certains de vouloir effectuer cette opération ?';
                        }
                        return m;
                    },withForm: function() {
                        var w=withForm;
                        if (withForm === null || angular.isUndefined(withForm) || withForm === "") {
                            w = false;
                        }
                        return w;
                    }
                }
            }).result.then(function(datas) {
                if(datas.result){
                    if (angular.isFunction(success)) {
                        success(datas.code);
                    }
                }
            }, function() {
                if (angular.isFunction(fail)) {
                    fail();
                }
            });
        }

    }
})();