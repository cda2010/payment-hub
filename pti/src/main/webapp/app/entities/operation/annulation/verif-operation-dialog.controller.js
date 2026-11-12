(function () {
    'use strict';

    angular
        .module('ptiApp')
        .controller('VerifOperationController', VerifOperationController);

    VerifOperationController.$inject = ['$uibModal', 'Utils', 'BASE_URL', 'CancelAlert', 'OperationUtils', 'Notification', 'RequestUtils', '$timeout', '$scope', '$stateParams', '$uibModalInstance'];

    function VerifOperationController($uibModal, Utils, BASE_URL, CancelAlert, OperationUtils, Notification, RequestUtils, $timeout, $scope, $stateParams, $uibModalInstance) {
        var vm = this;
        vm.clear = clear;
        vm.verification = verification;
        vm.isOperationValide = false;
        console.log('$stateParams',$stateParams);

        vm.canVerif = true;

        function clear() {
            $uibModalInstance.dismiss('cancel');
        }





        /*
            Verification de l'existence d'une operation  d'envoi cash
            En utilisant le code de l'operation
        */
        function verification() {
            vm.isOperationValide = false;
            RequestUtils.VerificationOperationExpiree.get({
                codeRetrait: vm.operation.codeRetrait
            }, function (datas) {
                if (datas != null) {
                    vm.operation = datas;
                    $uibModalInstance.close(true);
                    vm.detailsOperations(vm.operation, 'app/entities/operation/envoi_cash/init-operation.html', 'OperationDialogController', true, false);
                }
            });
        }
        vm.canUpdate = true;

        vm.verificationByCodeOperation = function verificationByCodeOperation() {
            vm.canUpdate = true;
            console.log("verification de l'operation à travers le code de l'operation");
            vm.isOperationValide = false;
            RequestUtils.OperationSearch.get({
                codeOperation: vm.operation.codeOperation
            }, function (datas) {
                console.log('result',datas);
                if (datas != null) {
                    vm.operation = datas;
                    if ((vm.operation.code == "ACCOUNT_CASH" || vm.operation.code == "ACCOUNT_ACCOUNT") && vm.operation.statut == "PAYE") {
                        console.log('annulation operation ACCOUNT_CASH ou ACCOUNT_ACCOUNT--');
                        vm.detailsOperations(vm.operation, 'app/entities/operation/account-cash/account-cash.html', 'AccountCashController', false, true);
                    } else if (vm.operation.code == "RETRAIT" && vm.operation.statut == "PAYE") {
                        console.log('annulation operation RETRAIT .--');
                        vm.detailsOperations(vm.operation, 'app/entities/operation/retrait/retrait-dialog.html', 'RetraitDialogController', false, true, 'lg');
                    } else if (vm.operation.code == "ENVOI_CASH" && (vm.operation.statut == "PAYE" || vm.operation.statut == "ENATTENTE" || vm.operation.statut == "SUSPENDU")) {
                        console.log('annulation operation ENVOI_CASH .--');
                        vm.detailsOperations(vm.operation, 'app/entities/operation/envoi_cash/init-operation.html', 'OperationDialogController', false, true);
                    }else if (vm.operation.statut == "ENATTENTE" || vm.operation.statut == "SUSPENDU" ) {
                        console.log('annulation operation ENATTENTE ou SUSPENDU--');
                        vm.detailsOperations(vm.operation, 'app/entities/operation/annulation/operation-view-annulation.html', 'AccountCashController', false, true,false,true);
                    }
                    else if (vm.operation.code == "CASH_ACCOUNT") {
                        console.log('annulation operation CASH_ACCOUNT .--');
                        vm.detailsOperations(vm.operation, 'app/entities/operation/cash-to-account/cash-to-account-dialog.html', 'CashToAccountDialogController', false, true);
                    } else {
                vm.canUpdate = false;
                    }
                }
            });
        };

        vm.detailsOperations = function (operation, templateUrl, controllerUrl, isExpired, isMaj,isCanceled,estAnnuler, size = 'lg') {
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
                    isCanceled:isCanceled,
                    isChange: false,
                    estAnnuler:estAnnuler
                }
            });
        };
    }
})();
