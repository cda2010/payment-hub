(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('BwAccountDialogController', BwAccountDialogController);

    BwAccountDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'BwAccount', 'BwCustomer'];

    function BwAccountDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, BwAccount, BwCustomer) {
        var vm = this;

        vm.bwAccounts = entity;
        vm.clear = clear;
        vm.save = save;
      //  vm.bwcustomers = BwCustomer.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.bwAccount.id !== null) {
                BwAccount.update(vm.bwAccount, onSaveSuccess, onSaveError);
            } else {
                BwAccount.save(vm.bwAccount, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('ptiApp:bwAccountUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
