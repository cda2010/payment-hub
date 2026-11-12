(function() {
    'use strict';

    angular
        .module('omnicanalApp')
        .controller('TraitementDeleteController',TraitementDeleteController);

    TraitementDeleteController.$inject = ['$uibModalInstance', 'entity', 'Traitement'];

    function TraitementDeleteController($uibModalInstance, entity, Traitement) {
        var vm = this;

        vm.traitement = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Traitement.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
