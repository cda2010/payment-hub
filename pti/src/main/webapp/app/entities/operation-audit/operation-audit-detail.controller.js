(function() {
    'use strict';

    angular
        .module('ptiApp')
        .controller('OperationAuditDetailController', OperationAuditDetailController);

    OperationAuditDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'OperationAudit', 'Operation'];

    function OperationAuditDetailController($scope, $rootScope, $stateParams, previousState, entity, OperationAudit, Operation) {
        var vm = this;

        vm.operationAudit = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('ptiApp:operationAuditUpdate', function(event, result) {
            vm.operationAudit = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
