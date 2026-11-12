(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PersonnePhysiqueDeleteController',PersonnePhysiqueDeleteController);

    PersonnePhysiqueDeleteController.$inject = ['$uibModalInstance', 'entity', 'PersonnePhysique'];

    function PersonnePhysiqueDeleteController($uibModalInstance, entity, PersonnePhysique) {
        var vm = this;

        vm.personnePhysique = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            PersonnePhysique.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
