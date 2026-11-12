(function () {
    'use strict';

    angular
            .module('ptiApp')
            .controller('ChargementTransfertDialogController', ChargementTransfertDialogController);

    ChargementTransfertDialogController.$inject = ['$rootScope', '$http', '$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'RequestUtils', 'CancelAlert', 'Principal', 'Notification', 'ChargementTransfert'];

    function ChargementTransfertDialogController($rootScope, $http, $timeout, $scope, $stateParams, $uibModalInstance, entity, RequestUtils, CancelAlert, Principal, Notification, ChargementTransfert) {
        var vm = this;
        vm.click = false;
        vm.chargementTransfert = entity;
        vm.clear = clear;
        vm.save = save;
        vm.confirm = confirm;

        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }
        $rootScope.$on('ptiApp:chargementEnmasse', function (event, data) {
            $uibModalInstance.close(data);

        });
        function save() {
            vm.isSaving = true;
            if (vm.chargementTransfert.id !== null) {
                ChargementTransfert.update(vm.chargementTransfert, onSaveSuccess, onSaveError);
            } else {
                ChargementTransfert.save(vm.chargementTransfert, onSaveSuccess, onSaveError);
            }
        }

        function confirm() {
            vm.username = vm.chargementTransfert.login;

            console.log('confirmation', vm.chargementTransfert, vm.username);

            CancelAlert.confirm(function () {
                vm.isSaving = true;
                var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
                $http.jsonp(url)
                        .success(function (data, status, headers, config) {
                            RequestUtils.VerifToken.get({
                                token: vm.token,
                                loginData: JSON.stringify(data)
                            }, function () {
                                console.log("Validation of", vm.operation);
                                Principal.identity().then(function (account) {
                                    vm.compte = account;
                                  
                                    ChargementTransfert.valider({id: vm.chargementTransfert.id, user: account.login, canal: 'WEB'}, function (result, headers) {
                                        vm.click = true;
                                          $rootScope.$emit('ptiApp:chargementEnmasse');
                                        var message = headers('X-ptiApp-params');
                                        console.log(headers('X-ptiApp-params'));
                                        Notification.success(message);


                                    });
                                });
                            });

                        }).error(function () {
                    Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
                });
            });
        }


        function onSaveSuccess(result) {
            $scope.$emit('ptiApp:chargementTransfertUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError() {
            vm.isSaving = false;
        }


    }
})();
