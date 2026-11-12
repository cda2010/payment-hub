(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PersonneDeleteController',PersonneDeleteController);

    PersonneDeleteController.$inject = ['$uibModalInstance', 'entity', 'Personne'];

    function PersonneDeleteController($uibModalInstance, entity, Personne) {
        var vm = this;

        vm.personne = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Personne.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
