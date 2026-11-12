(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementCompteDialogController', ChargementCompteDialogController);

    ChargementCompteDialogController.$inject = ['$http','$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'RequestUtils','CancelAlert','Principal','Notification','ChargementCompte'];

    function ChargementCompteDialogController ($http,$timeout, $scope, $stateParams, $uibModalInstance, entity,RequestUtils,CancelAlert,Principal,Notification, ChargementCompte) {
        var vm = this;

        vm.chargementCompte = entity;
        console.log('fff',vm.chargementCompte);
        vm.clear = clear;
        vm.save = save;
        vm.confirm=confirm;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.chargementCompte.id !== null) {
                ChargementCompte.update(vm.chargementCompte, onSaveSuccess, onSaveError);
            } else {
                ChargementCompte.save(vm.chargementCompte, onSaveSuccess, onSaveError);
            }
        }

        function confirm() {
            vm.username=vm.chargementCompte.login;

            console.log('confirmation',vm.chargementCompte,vm.username);

            CancelAlert.confirm(function () {
                vm.isSaving=true;
                var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
                $http.jsonp(url)
                    .success(function (data, status, headers, config) {
                        RequestUtils.VerifToken.get({
                            token: vm.token,
                            loginData: JSON.stringify(data)
                        }, function () {
                            console.log("Validation of",vm.operation);
                            Principal.identity().then(function (account) {
                                vm.compte = account;
                                ChargementCompte.valider({
                                    id: vm.chargementCompte.id,
                                    user: account.login,
                                    canal: 'WEB'
                                }, function (result, headers) {
                                    var message = headers('X-ptiApp-params');
                                    Notification.success(message);
                                    $uibModalInstance.close();
                                });
                            });
                        });

                    }).error(function () {
                    Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
                });
            });
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:chargementCompteUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
