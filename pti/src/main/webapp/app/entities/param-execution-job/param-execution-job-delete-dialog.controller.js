(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ParamExecutionJobDeleteController',ParamExecutionJobDeleteController);

    ParamExecutionJobDeleteController.$inject = ['$uibModalInstance', 'entity', 'ParamExecutionJob'];

    function ParamExecutionJobDeleteController($uibModalInstance, entity, ParamExecutionJob) {
        var vm = this;

        vm.paramExecutionJob = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            ParamExecutionJob.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
