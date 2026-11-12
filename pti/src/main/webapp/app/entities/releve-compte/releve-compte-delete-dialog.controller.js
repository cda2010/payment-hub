(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReleveCompteDeleteController',ReleveCompteDeleteController);

    ReleveCompteDeleteController.$inject = ['$uibModalInstance', 'entity', 'ReleveCompte'];

    function ReleveCompteDeleteController($uibModalInstance, entity, ReleveCompte) {
        var vm = this;

        vm.releveCompte = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ReleveCompte.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
