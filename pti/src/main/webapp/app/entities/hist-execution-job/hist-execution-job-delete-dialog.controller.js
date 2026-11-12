(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('HistExecutionJobDeleteController',HistExecutionJobDeleteController);

    HistExecutionJobDeleteController.$inject = ['$uibModalInstance', 'entity', 'HistExecutionJob'];

    function HistExecutionJobDeleteController($uibModalInstance, entity, HistExecutionJob) {
        var vm = this;

        vm.histExecutionJob = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            HistExecutionJob.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
