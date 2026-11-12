(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('PtiJobDetailsDetailController', PtiJobDetailsDetailController);

    PtiJobDetailsDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'PtiJobDetails', 'HistExecutionJob'];

    function PtiJobDetailsDetailController($scope, $rootScope, $stateParams, previousState, entity, PtiJobDetails, HistExecutionJob) {
        var vm = this;

        vm.ptiJobDetails = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:ptiJobDetailsUpdate', function(event, result) {
            vm.ptiJobDetails = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
