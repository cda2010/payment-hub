(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('TableauCumulDeleteController',TableauCumulDeleteController);

    TableauCumulDeleteController.$inject = ['$uibModalInstance', 'entity', 'TableauCumul'];

    function TableauCumulDeleteController($uibModalInstance, entity, TableauCumul) {
        var vm = this;

        vm.tableauCumul = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            TableauCumul.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
