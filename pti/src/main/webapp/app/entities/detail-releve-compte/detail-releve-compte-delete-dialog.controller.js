(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('DetailReleveCompteDeleteController',DetailReleveCompteDeleteController);

    DetailReleveCompteDeleteController.$inject = ['$uibModalInstance', 'entity', 'DetailReleveCompte'];

    function DetailReleveCompteDeleteController($uibModalInstance, entity, DetailReleveCompte) {
        var vm = this;

        vm.detailReleveCompte = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            DetailReleveCompte.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
