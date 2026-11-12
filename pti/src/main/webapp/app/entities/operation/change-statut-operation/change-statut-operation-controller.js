(function () {
    'use strict';
    angular
        .module('ptiApp')
        .controller('ChangeStatutOperationController',ChangeStatutOperationController);

    ChangeStatutOperationController.$inject= ['$uibModal', 'Utils', 'BASE_URL', 'CancelAlert','Notification', 'RequestUtils', '$timeout', '$scope', '$stateParams', '$uibModalInstance'];
    function ChangeStatutOperationController($uibModal,Utils,BASE_URL,CancelAlert,Notification,RequestUtils,$timeout,$scope,$stateParams,$uibModalInstance) {
        var vm = this;
        vm.clear = clear;
        vm.verificationByCodeOperation=verificationByCodeOperation;
        vm.isOperationValide = false;
        vm.canVerif = true;
        vm.canUpdate = true;
        console.log('yes');
        function clear() {
            $uibModalInstance.dismiss('cancel');
        }

        function verificationByCodeOperation() {
            vm.canUpdate = true;
            console.log("verification de l'operation à travers le code de l'operation");
            vm.isOperationValide = false;
            RequestUtils.OperationSearch.get({
                codeOperation: vm.operation.codeOperation,
            }, function (datas) {
                console.log(datas);
                if (datas != null) {
                    vm.operation = datas;
                    if(vm.operation.code == "ACCOUNT_CASH") {
                        vm.detailsOperations(vm.operation, 'app/entities/operation/account-cash/account-cash.html', 'AccountCashController');
                    } else if(vm.operation.code == "ENVOI_CASH") {
                        if(vm.operation.statut==='ENCOURS') {
                            vm.detailsOperations(vm.operation, 'app/entities/operation/envoi_cash/init-operation.html', 'OperationDialogController', true);
                        } else {
                                Notification.error("Statut de l'opération non ENCOURS.");
                          }
                    } else {
                        vm.canUpdate = false;
                    }
                }
            });
        }

        vm.detailsOperations = function (operation, templateUrl, controllerUrl, isChange,isExpired, isMaj, size = 'lg') {
            $uibModalInstance.close(true);
            $uibModal.open({
                templateUrl: templateUrl,
                controller: controllerUrl,
                controllerAs: 'vm',
                backdrop: 'static',
                size: size,
                resolve: {
                    entity: function () {
                        return operation;
                    },
                    isViewing: true,
                    typeService: function () {
                        return operation.code;
                    },
                    isExpired: isExpired,
                    isMaj: isMaj,
                    isChange: isChange
                }
            });
        };
    }

})();
