(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('LogOperationDetailController', LogOperationDetailController);

    LogOperationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'LogOperation'];

    function LogOperationDetailController($scope, $rootScope, $stateParams, previousState, entity, LogOperation) {
        var vm = this;

        vm.logOperation = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:logOperationUpdate', function(event, result) {
            vm.logOperation = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
