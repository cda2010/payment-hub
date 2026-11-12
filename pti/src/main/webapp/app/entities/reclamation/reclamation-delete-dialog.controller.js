(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ReclamationDeleteController',ReclamationDeleteController);

    ReclamationDeleteController.$inject = ['$uibModalInstance', 'entity', 'Reclamation'];

    function ReclamationDeleteController($uibModalInstance, entity, Reclamation) {
        var vm = this;

        vm.reclamation = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Reclamation.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
