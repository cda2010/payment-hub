(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('AgenceDeleteController',AgenceDeleteController);

    AgenceDeleteController.$inject = ['$uibModalInstance', 'entity', 'Agence'];

    function AgenceDeleteController($uibModalInstance, entity, Agence) {
        var vm = this;

        vm.agence = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Agence.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
