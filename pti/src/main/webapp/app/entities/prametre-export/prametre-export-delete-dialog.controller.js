(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PrametreExportDeleteController',PrametreExportDeleteController);

    PrametreExportDeleteController.$inject = ['$uibModalInstance', 'entity', 'PrametreExport'];

    function PrametreExportDeleteController($uibModalInstance, entity, PrametreExport) {
        var vm = this;

        vm.prametreExport = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            PrametreExport.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
