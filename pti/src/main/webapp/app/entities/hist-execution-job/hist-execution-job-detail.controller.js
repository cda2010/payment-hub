(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('HistExecutionJobDetailController', HistExecutionJobDetailController);

    HistExecutionJobDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'HistExecutionJob', 'PtiJobDetails'];

    function HistExecutionJobDetailController($scope, $rootScope, $stateParams, previousState, entity, HistExecutionJob, PtiJobDetails) {
        var vm = this;

        vm.histExecutionJob = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:histExecutionJobUpdate', function(event, result) {
            vm.histExecutionJob = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
