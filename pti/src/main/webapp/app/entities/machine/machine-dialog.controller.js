(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('MachineDialogController', MachineDialogController);

    MachineDialogController.$inject = ['$uibModalInstance', '$rootScope', 'ChoisirUo', 'DataUtils', 'Notification', '$http', '$timeout', '$scope', 'Machine','entity'];

    function MachineDialogController($uibModalInstance, $rootScope, ChoisirUo, DataUtils, Notification, $http, $timeout, $scope, Machine,entity) {
        var vm = this;

        vm.clear = clear;
        vm.save = save;
        vm.updateMachine=updateMachine;
        vm.errorMsg = '';
        vm.successMsg = '';
        vm.machine = {};
        vm.byteSize = DataUtils.byteSize;
        vm.openFile = DataUtils.openFile;
        vm.machine = {
            detailsMachine: null,
            detailsMachineContentType: null
        };
        console.log('ent',entity);
        if(entity!=null){
            vm.machine=entity;
        }
        $timeout(function () {
            angular.element('.form-group:eq(1)>input').focus();
        });
        vm.organisation = null;
        vm.isSaving = false;
        vm.choisirUo = function () {
            console.log("choisir uo");
            ChoisirUo.open($scope);
        };


        var unsubscribe = $rootScope.$on('ptiApp:choisirUo', function (event, result) {
            console.log("uo choisi");
            if (result != null) {
                vm.organisation = result;
                vm.machine.organisationId = result.id;
            }
        });

        function clear() {
            vm.machine = {};
        }

        vm.setBordereau = function ($file, operation) {
            console.log("message");
            if ($file) {
                DataUtils.toBase64($file, function (base64Data) {
                    $scope.$apply(function () {
                        operation.detailsMachine = base64Data;
                        operation.detailsMachineContentType = $file.type;
                    });
                });
            }
        };

        vm.clear = function clear() {
            $uibModalInstance.dismiss('cancel');
        };


        function save() {
            vm.isSaving = true;
            Machine.save(vm.machine, onSaveSuccess, onSaveError);
        }

        function updateMachine(){
            vm.isSaving = true;
            Machine.updateMachine(vm.machine, onSaveSuccess, onSaveError);
        }

       vm.startVerif= function startVerif(token) {
            var url = "http://127.0.0.1:55555/x-x-x-x/sign-object/" + vm.username + "?callback=JSON_CALLBACK";
            $http.jsonp(url)
                .success(function (data, status, headers, config) {
                    data.login = angular.copy(vm.username);
                    vm.sendToLogin(JSON.stringify(data), vm.username);
                    RequestUtils.VerifToken.get({
                        token: token ,
                        loginData: JSON.stringify(data)
                    }, function (datas) {
                       Notification.success("Token valide!");
                    });
                }).error(function (data, status, headers, config) {
                Notification.error("Impossible d'assurer l'authentification de votre machine ! Veuillez contacter votre administrateur.");
            });
        };



        function onSaveSuccess(result) {
            $scope.$emit('microfinApp:machineUpdate', result);
            vm.isSaving = false;
            Notification.success("Opération éffectuée.");
            $uibModalInstance.close(result);
        }

        function onSaveError() {
            vm.isSaving = false;
            Notification.error("Erreure interne.");
        }
    }
})();
