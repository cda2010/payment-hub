(function() {
    'use strict';

    angular
        .module('omnicanalPresentationApp')
        .controller('ParametreDeleteController',ParametreDeleteController);

    ParametreDeleteController.$inject = ['$uibModalInstance', 'entity', 'Parametre'];

    function ParametreDeleteController($uibModalInstance, entity, Parametre) {
        var vm = this;

        vm.parametre = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Parametre.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
