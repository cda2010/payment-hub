(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('ParamExecutionJobDetailController', ParamExecutionJobDetailController);

    ParamExecutionJobDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'ParamExecutionJob', 'PtiJobDetail'];

    function ParamExecutionJobDetailController($scope, $rootScope, $stateParams, previousState, entity, ParamExecutionJob, PtiJobDetail) {
        var vm = this;

        vm.paramExecutionJob = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:paramExecutionJobUpdate', function(event, result) {
            vm.paramExecutionJob = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
