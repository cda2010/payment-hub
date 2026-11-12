(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FileExportDeleteController',FileExportDeleteController);

        FileExportDeleteController.$inject = ['$uibModalInstance', 'entity', 'FileExport'];

    function FileExportDeleteController($uibModalInstance, entity, FileExport) {
        var vm = this;

        vm.fileExport = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            FileExport.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
