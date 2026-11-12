(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ExportTableDeleteController',ExportTableDeleteController);

    ExportTableDeleteController.$inject = ['$uibModalInstance', 'entity', 'ExportTable'];

    function ExportTableDeleteController($uibModalInstance, entity, ExportTable) {
        var vm = this;

        vm.exportTable = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ExportTable.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
