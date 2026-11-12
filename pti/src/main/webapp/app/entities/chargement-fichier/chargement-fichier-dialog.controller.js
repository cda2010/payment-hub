(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChargementFichierDialogController', ChargementFichierDialogController);

    ChargementFichierDialogController.$inject = ['$timeout', '$scope','$rootScope', '$http','$stateParams', '$uibModalInstance', 'RequestUtils','CancelAlert','Principal','Notification','entity', 'ChargementFichier'];

    function ChargementFichierDialogController ($timeout, $scope,$rootScope,$http, $stateParams, $uibModalInstance,RequestUtils,CancelAlert,Principal,Notification, entity, ChargementFichier) {
        var vm = this;

        vm.chargementFichier = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
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
            if (vm.chargementMessage.id !== null) {
                ChargementFichier.update(vm.chargementFichier, onSaveSuccess, onSaveError);
            } else {
                ChargementFichier.save(vm.chargementFichier, onSaveSuccess, onSaveError);
            }
        }

        function confirm() {
            vm.username = vm.chargementFichier.login;

            console.log('confirmation', vm.chargementFichier, vm.username);

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
                                ChargementFichier.valider({id: vm.chargementFichier.id, user: account.login, canal: 'WEB'}, function (result, headers) {
                                    vm.click = true;
                                    $rootScope.$emit('ptiApp:chargementFichiersEnmasse');
                                    var message = headers('X-ptiApp-params');
                                    console.log(headers('X-ptiApp-params'));
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
            $scope.$emit('ptiApp:chargementMessageUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.date_chargement = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
