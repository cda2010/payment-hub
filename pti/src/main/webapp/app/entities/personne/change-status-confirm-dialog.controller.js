(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ChangeStatutConfirmController',ChangeStatutConfirmController);

    ChangeStatutConfirmController.$inject = ['$uibModalInstance','entity'];

    function ChangeStatutConfirmController($uibModalInstance,entity) {
        var vm = this;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;
        vm.type=entity;
        if(vm.type==='ACTIVE'){
            vm.type='activer';
        }else {
            vm.type='désactiver';
        }
        console.log(entity);

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete () {
            $uibModalInstance.close(true);
        }
    }
})();
