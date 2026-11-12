(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('TypeCompteDeleteController',TypeCompteDeleteController);

    TypeCompteDeleteController.$inject = ['$uibModalInstance', 'entity', 'TypeCompte'];

    function TypeCompteDeleteController($uibModalInstance, entity, TypeCompte) {
        var vm = this;

        vm.typeCompte = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TypeCompte.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
