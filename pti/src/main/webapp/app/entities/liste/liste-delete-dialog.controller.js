(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ListeDeleteController',ListeDeleteController);

    ListeDeleteController.$inject = ['$uibModalInstance', 'entity', 'Liste'];

    function ListeDeleteController($uibModalInstance, entity, Liste) {
        var vm = this;

        vm.liste = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Liste.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
