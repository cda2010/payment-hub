(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('FileExportParamDeleteController',FileExportParamDeleteController);

        FileExportParamDeleteController.$inject = ['$uibModalInstance', 'entity', 'FileExportParam'];

    function FileExportParamDeleteController($uibModalInstance, entity, FileExportParam) {
        var vm = this;

        vm.fileExportParam = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            FileExportParam.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();