(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ParamExecutionJobController', ParamExecutionJobController);

    ParamExecutionJobController.$inject = ['ParamExecutionJob'];

    function ParamExecutionJobController(ParamExecutionJob) {

        var vm = this;

        vm.paramExecutionJobs = [];

        loadAll();

        function loadAll() {
            ParamExecutionJob.query(function(result) {
                vm.paramExecutionJobs = result;
                vm.searchQuery = null;
            });
        }
    }
})();
