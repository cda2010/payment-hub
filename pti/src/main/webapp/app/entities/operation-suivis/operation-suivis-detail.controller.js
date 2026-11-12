(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationSuivisDetailController', OperationSuivisDetailController);

    OperationSuivisDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'OperationSuivis', 'Client'];

    function OperationSuivisDetailController($scope, $rootScope, $stateParams, previousState, entity, OperationSuivis, Client) {
        var vm = this;

        vm.operationSuivis = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:operationSuivisUpdate', function(event, result) {
            vm.operationSuivis = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
