(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LigneMessageDeleteController',LigneMessageDeleteController);

    LigneMessageDeleteController.$inject = ['$uibModalInstance', 'entity', 'LigneMessage'];

    function LigneMessageDeleteController($uibModalInstance, entity, LigneMessage) {
        var vm = this;

        vm.ligneMessage = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            LigneMessage.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
