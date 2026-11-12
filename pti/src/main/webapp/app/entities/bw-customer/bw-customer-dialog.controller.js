(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BwCustomerDialogController', BwCustomerDialogController);

    BwCustomerDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'BwCustomer'];

    function BwCustomerDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, BwCustomer) {
        var vm = this;

        vm.bwCustomer = entity;
        vm.clear = clear;
        vm.save = save;

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.bwCustomer.id !== null) {
                BwCustomer.update(vm.bwCustomer, onSaveSuccess, onSaveError);
            } else {
                BwCustomer.save(vm.bwCustomer, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:bwCustomerUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
