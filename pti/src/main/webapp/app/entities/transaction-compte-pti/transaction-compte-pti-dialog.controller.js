(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TransactionComptePtiDialogController', TransactionComptePtiDialogController);

    TransactionComptePtiDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', '$q', 'entity', 'TransactionComptePti', 'Operation'];

    function TransactionComptePtiDialogController ($timeout, $scope, $stateParams, $uibModalInstance, $q, entity, TransactionComptePti, Operation) {
        var vm = this;

        vm.transactionComptePti = entity;
        vm.clear = clear;
        vm.datePickerOpenStatus = {};
        vm.openCalendar = openCalendar;
        vm.save = save;
        vm.operations = Operation.query({filter: 'transactioncomptepti-is-null'});
        $q.all([vm.transactionComptePti.$promise, vm.operations.$promise]).then(function() {
            if (!vm.transactionComptePti.operationId) {
                return $q.reject();
            }
            return Operation.get({id : vm.transactionComptePti.operationId}).$promise;
        }).then(function(operation) {
            vm.operations.push(operation);
        });

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.transactionComptePti.id !== null) {
                TransactionComptePti.update(vm.transactionComptePti, onSaveSuccess, onSaveError);
            } else {
                TransactionComptePti.save(vm.transactionComptePti, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:transactionComptePtiUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }

        vm.datePickerOpenStatus.date = false;

        function openCalendar (date) {
            vm.datePickerOpenStatus[date] = true;
        }
    }
})();
